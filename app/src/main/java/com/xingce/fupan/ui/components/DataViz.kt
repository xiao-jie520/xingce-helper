package com.xingce.fupan.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingce.fupan.ui.theme.LocalFluent
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** 环形正确率：扫角动画 + 渐变描边 + 外发光 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp,
    startColor: Color? = null,
    endColor: Color? = null,
    trackColor: Color? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val p = LocalFluent.current
    val c1 = startColor ?: p.accent
    val c2 = endColor ?: p.accentLight
    val track = trackColor ?: p.track
    val anim = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        anim.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val arcSize = Size(size.width - sw, size.height - sw)
            val topLeft = Offset(sw / 2f, sw / 2f)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round)
            )
            val sweep = 360f * anim.value
            if (sweep > 0.5f) {
                rotate(-90f) {
                    drawArc(
                        color = c2.copy(alpha = 0.16f),
                        startAngle = 0f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = sw * 2.2f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(c1, c2, c1),
                            center = center
                        ),
                        startAngle = 0f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = sw, cap = StrokeCap.Round)
                    )
                }
            }
        }
        content()
    }
}

/** 横向数据条：进入时从左往右生长 */
@Composable
fun AnimatedBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    color: Color? = null,
    brush: Brush? = null,
    trackColor: Color? = null,
    delayMillis: Int = 0
) {
    val p = LocalFluent.current
    val target = fraction.coerceIn(0f, 1f)
    val anim = remember { Animatable(0f) }

    LaunchedEffect(target, delayMillis) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        anim.animateTo(target, tween(durationMillis = 720, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor ?: p.track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(anim.value)
                .clip(RoundedCornerShape(50))
                .background(brush ?: SolidColor(color ?: p.accent))
        )
    }
}

/**
 * 折线趋势图：路径从左到右揭示，面积渐变填充，末点带光晕。
 */
@Composable
fun TrendChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 132.dp,
    accent: Color? = null,
    accentAlt: Color? = null
) {
    val p = LocalFluent.current
    val c1 = accent ?: p.accent
    val c2 = accentAlt ?: p.accentLight
    val marker = p.accentAlt
    val anim = remember { Animatable(0f) }

    LaunchedEffect(values) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(durationMillis = 1000, easing = FastOutSlowInEasing))
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            if (values.isEmpty()) return@Canvas
            val labelPx = 9.sp.toPx()
            val leftPad = 34.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = labelPx * 0.7f + 6f
            val bottomPad = 8f
            val chartH = size.height - topPad - bottomPad
            val chartW = size.width - leftPad - rightPad

            // 自适应纵轴区间，避免小幅波动被拉平
            val minV = values.min()
            val maxV = values.max()
            var lower = (minV - 0.06f).coerceAtLeast(0f)
            var upper = (maxV + 0.06f).coerceAtMost(1f)
            if (upper - lower < 0.12f) {
                val mid = (upper + lower) / 2f
                lower = (mid - 0.06f).coerceAtLeast(0f)
                upper = (mid + 0.06f).coerceAtMost(1f)
            }
            val span = (upper - lower).coerceAtLeast(0.01f)

            // 网格 + 纵轴刻度
            val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
            val axisPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = p.textTertiary.toArgb()
                textSize = labelPx
            }
            for (i in 0..2) {
                val y = topPad + chartH * (i / 2f)
                drawLine(
                    color = p.divider,
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + chartW, y),
                    strokeWidth = 1f,
                    pathEffect = dash
                )
                val tick = upper - span * (i / 2f)
                val text = "${(tick * 100f).roundToInt()}%"
                val tw = axisPaint.measureText(text)
                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    leftPad - 7.dp.toPx() - tw,
                    y + labelPx * 0.35f,
                    axisPaint
                )
            }

            if (values.size == 1) {
                val cy = topPad + chartH * ((upper - values[0].coerceIn(0f, 1f)) / span)
                val cx = leftPad + chartW / 2f
                drawCircle(color = c1.copy(alpha = 0.16f), radius = 16f, center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = 6f, center = Offset(cx, cy))
                drawCircle(color = c1, radius = 6f, center = Offset(cx, cy), style = Stroke(3f))
                return@Canvas
            }

            val stepX = chartW / (values.size - 1)
            fun px(i: Int) = leftPad + stepX * i
            fun py(v: Float) = topPad + chartH * ((upper - v.coerceIn(0f, 1f)) / span)

            val linePath = Path()
            values.forEachIndexed { i, v ->
                val x = px(i)
                val y = py(v)
                if (i == 0) linePath.moveTo(x, y) else {
                    // 平滑曲线（简单的二次贝塞尔）
                    val prevX = px(i - 1)
                    val prevY = py(values[i - 1])
                    val midX = (prevX + x) / 2f
                    linePath.cubicTo(midX, prevY, midX, y, x, y)
                }
            }

            val areaPath = Path().apply {
                addPath(linePath)
                lineTo(px(values.size - 1), topPad + chartH)
                lineTo(px(0), topPad + chartH)
                close()
            }

            clipRect(left = leftPad - 1f, right = leftPad + chartW * anim.value + 1f) {
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(c1.copy(alpha = 0.26f), c1.copy(alpha = 0.02f)),
                        startY = topPad,
                        endY = topPad + chartH
                    )
                )
                drawPath(
                    path = linePath,
                    brush = Brush.horizontalGradient(listOf(c1, c2)),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )

                values.forEachIndexed { i, v ->
                    val c = Offset(px(i), py(v))
                    val isLast = i == values.size - 1
                    if (isLast) {
                        drawCircle(color = marker.copy(alpha = 0.22f), radius = 12f, center = c)
                        drawCircle(color = Color.White, radius = 5.5f, center = c)
                        drawCircle(color = marker, radius = 5.5f, center = c, style = Stroke(3f))
                    } else {
                        drawCircle(color = Color.White, radius = 3.6f, center = c)
                        drawCircle(color = c1.copy(alpha = 0.9f), radius = 3.6f, center = c, style = Stroke(2.2f))
                    }
                }
            }
        }

        if (labels.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            val shown = if (labels.size <= 5) labels else listOf(
                labels.first(),
                labels[labels.size / 2],
                labels.last()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 4.dp),
                horizontalArrangement = if (shown.size == 1) Arrangement.Center else Arrangement.SpaceBetween
            ) {
                shown.forEachIndexed { i, label ->
                    Text(
                        text = label,
                        color = p.textTertiary,
                        fontSize = 9.5.sp,
                        textAlign = when {
                            i == 0 -> TextAlign.Start
                            i == shown.size - 1 -> TextAlign.End
                            else -> TextAlign.Center
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** 迷你水平进度（用于卡片内的小指标） */
@Composable
fun TinyProgress(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    val p = LocalFluent.current
    val anim = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        anim.animateTo(fraction.coerceIn(0f, 1f), tween(600, easing = LinearEasing))
    }
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(p.track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(anim.value)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

/** 三柱迷你图，用于历史卡片 */
@Composable
fun MiniBars(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 26.dp
) {
    val p = LocalFluent.current
    Box(modifier = modifier.height(height), contentAlignment = Alignment.BottomStart) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom
        ) {
            val items = if (values.isEmpty()) listOf(0f) else values.takeLast(5)
            items.forEachIndexed { i, v ->
                val anim = remember(items) { Animatable(0f) }
                LaunchedEffect(items) {
                    delay(i * 60L)
                    anim.animateTo(v.coerceIn(0.05f, 1f), tween(520, easing = FastOutSlowInEasing))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(anim.value)
                        .padding(horizontal = 1.5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (i == items.size - 1) color
                            else color.copy(alpha = if (p.dark) 0.35f else 0.28f)
                        )
                )
            }
        }
    }
}
