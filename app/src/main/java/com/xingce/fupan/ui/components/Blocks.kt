package com.xingce.fupan.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingce.fupan.data.Insight
import com.xingce.fupan.data.SectionResult
import com.xingce.fupan.data.Tone
import com.xingce.fupan.ui.theme.LocalFluent
import com.xingce.fupan.util.Fmt
import kotlinx.coroutines.delay

/** 正确率对应的语义色 */
@Composable
fun accuracyColor(accuracy: Float): Color {
    val p = LocalFluent.current
    return when {
        accuracy >= 0.75f -> p.success
        accuracy >= 0.60f -> p.accent
        accuracy >= 0.45f -> p.warning
        else -> p.danger
    }
}

@Composable
fun toneColor(tone: Tone): Color {
    val p = LocalFluent.current
    return when (tone) {
        Tone.GOOD -> p.success
        Tone.INFO -> p.accent
        Tone.WARN -> p.warning
        Tone.BAD -> p.danger
    }
}

/** 入场揭示动画：淡入 + 上浮 + 轻微放大，可设延迟做交错 */
@Composable
fun Reveal(
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(300, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(420, easing = FastOutSlowInEasing)) { it / 5 } +
            scaleIn(initialScale = 0.985f, animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f)),
        exit = androidx.compose.animation.ExitTransition.None
    ) {
        content()
    }
}

/** 板块数据行：名称 + 对/总 + 正确率 + 生长条 */
@Composable
fun SectionBarRow(
    section: SectionResult,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    highlight: Boolean = false,
    trailingText: String? = null
) {
    val p = LocalFluent.current
    val color = accuracyColor(section.accuracy)
    Column(modifier = modifier.padding(vertical = 7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = section.name,
                color = p.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = p.textTertiary,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = "${section.correct}/${section.total}",
                color = p.textTertiary,
                fontSize = 11.5.sp
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = Fmt.percent(section.accuracy, 0),
                color = color,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(7.dp))
        AnimatedBar(
            fraction = section.accuracy,
            modifier = Modifier.fillMaxWidth(),
            height = 7.dp,
            brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.85f), color)),
            delayMillis = delayMillis
        )
    }
}

/** 诊断条目 */
@Composable
fun InsightRow(
    insight: Insight,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    val color = toneColor(insight.tone)
    Row(modifier = modifier.padding(vertical = 9.dp)) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = insight.title,
                    color = p.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (insight.badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(color.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = insight.badge,
                            color = color,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = insight.detail,
                color = p.textSecondary,
                fontSize = 11.8.sp,
                lineHeight = 17.sp
            )
        }
    }
}

/** 空状态插图 + 引导 */
@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    badges: List<String> = emptyList()
) {
    val p = LocalFluent.current
    AcrylicCard(modifier = modifier, strong = true, corner = 28.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(p.accent.copy(alpha = 0.14f), p.accentLight.copy(alpha = 0.10f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.linearGradient(listOf(p.accent, p.accentLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = title, color = p.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                color = p.textTertiary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            if (badges.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    badges.forEach { PillChip(text = it) }
                }
            }
        }
    }
}

/** 二次确认弹窗 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = false
) {
    val p = LocalFluent.current
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (p.dark) Color(0xFF171D27) else Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = p.textPrimary
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 13.sp,
                color = p.textSecondary,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (danger) p.danger else p.accent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(text = "取消", color = p.textSecondary, fontSize = 13.5.sp)
            }
        }
    )
}

/** 关于弹窗：长按首页标题唤起，开发者签名藏在这里 */
@Composable
fun AboutDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    val p = LocalFluent.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(listOf(p.acrylicStrong, p.acrylic)))
                .border(
                    1.dp,
                    Brush.verticalGradient(listOf(p.strokeTop, p.strokeBottom)),
                    RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(p.accent, p.accentLight))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "行测复盘",
                color = p.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "v$versionName",
                color = p.textTertiary,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(1.dp)
                    .background(p.divider)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "把每一次模考都变成可复用的经验",
                color = p.textSecondary,
                fontSize = 11.5.sp
            )
            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(p.track)
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "设计与开发",
                    color = p.textTertiary,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "我不是冰墩墩",
                    color = p.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Hu_Tao111",
                    color = p.accent,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "所有成绩数据仅保存在本机",
                color = p.textTertiary,
                fontSize = 9.5.sp
            )
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(p.accent, p.accentLight)))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "知道了",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** 顶部导航条：左图标 + 标题 + 右侧动作 */
@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    backIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.ArrowBack,
    actions: @Composable (() -> Unit)? = null
) {
    val p = LocalFluent.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AcrylicIconButton(icon = backIcon, onClick = onBack, contentDescription = "返回")
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = p.textPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = p.textTertiary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (actions != null) actions()
    }
}
