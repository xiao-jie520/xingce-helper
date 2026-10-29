package com.xingce.fupan.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingce.fupan.ui.theme.LocalFluent

/** 页面背景：竖向渐变 + 两团低频光斑，模拟 Fluent 的亚克力底层 */
@Composable
fun FluentBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val p = LocalFluent.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(p.canvasTop, p.canvasBottom)))
                val c1 = Offset(size.width * 1.02f, size.height * 0.01f)
                val r1 = size.width * 1.30f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(p.glowPrimary, Color.Transparent),
                        center = c1,
                        radius = r1
                    ),
                    radius = r1,
                    center = c1
                )
                val c2 = Offset(-size.width * 0.08f, size.height * 0.74f)
                val r2 = size.width * 1.10f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(p.glowSecondary, Color.Transparent),
                        center = c2,
                        radius = r2
                    ),
                    radius = r2,
                    center = c2
                )
            }
    ) {
        content()
    }
}

@Composable
fun AcrylicCard(
    modifier: Modifier = Modifier,
    corner: Dp = 26.dp,
    padding: PaddingValues = PaddingValues(18.dp),
    strong: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val p = LocalFluent.current
    val shape = RoundedCornerShape(corner)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scaleAnim by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.978f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "acrylicScale"
    )

    var base = modifier
    base = if (p.dark) {
        base.shadow(0.dp, shape, false, Color.Transparent, Color.Transparent)
    } else {
        base.shadow(
            elevation = 16.dp,
            shape = shape,
            clip = false,
            ambientColor = p.shadow.copy(alpha = 0.10f),
            spotColor = p.shadow.copy(alpha = 0.18f)
        )
    }

    Column(
        modifier = base
            .scale(scaleAnim)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (strong) p.acrylicStrong else p.acrylic,
                        if (strong) p.acrylic else p.acrylicSoft
                    )
                )
            )
            .border(
                BorderStroke(1.dp, Brush.verticalGradient(listOf(p.strokeTop, p.strokeBottom))),
                shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(padding),
        content = content
    )
}

@Composable
fun AcrylicIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    tint: Color? = null,
    contentDescription: String? = null
) {
    val p = LocalFluent.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scaleAnim by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "iconScale"
    )
    Box(
        modifier = modifier
            .size(size)
            .scale(scaleAnim)
            .clip(CircleShape)
            .background(p.acrylicStrong)
            .border(1.dp, Brush.verticalGradient(listOf(p.strokeTop, p.strokeBottom)), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.44f),
            tint = tint ?: p.textPrimary
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    hint: String? = null
) {
    val p = LocalFluent.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scaleAnim by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.962f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "btnScale"
    )
    val shape = RoundedCornerShape(20.dp)
    val bg = if (enabled) {
        Brush.horizontalGradient(listOf(p.accent, p.accentLight))
    } else {
        Brush.horizontalGradient(listOf(p.track, p.track))
    }
    Box(
        modifier = modifier
            .scale(scaleAnim)
            .then(
                if (enabled && !p.dark) Modifier.shadow(
                    elevation = 14.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = p.accent.copy(alpha = 0.35f),
                    spotColor = p.accent.copy(alpha = 0.45f)
                ) else Modifier
            )
            .height(58.dp)
            .clip(shape)
            .background(bg)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else p.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) Color.White else p.textTertiary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
            if (hint != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = hint,
                    color = if (enabled) Color.White.copy(alpha = 0.68f) else p.textTertiary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val p = LocalFluent.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = p.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.1.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = p.textTertiary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun PillChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    tint: Color? = null
) {
    val p = LocalFluent.current
    val bg = if (selected) {
        Brush.horizontalGradient(listOf(p.accent, p.accentLight))
    } else {
        Brush.horizontalGradient(listOf(p.acrylicStrong, p.acrylicSoft))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(
                1.dp,
                if (selected) Color.Transparent else p.strokeBottom.copy(alpha = 0.5f),
                RoundedCornerShape(50)
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else (tint ?: p.textSecondary),
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    BoxWithConstraints(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(p.track)
    ) {
        val pad = 4.dp
        val itemWidth = (maxWidth - pad * 2) / options.size
        val x by animateDpAsState(
            targetValue = pad + itemWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium),
            label = "segmentX"
        )
        Box(
            modifier = Modifier
                .offset(x = x, y = pad)
                .width(itemWidth)
                .height(42.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Brush.verticalGradient(listOf(p.acrylicStrong, p.acrylic)))
                .border(1.dp, p.strokeTop.copy(alpha = 0.7f), RoundedCornerShape(11.dp))
        )
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (index == selectedIndex) p.accent else p.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    accent: Color? = null
) {
    val p = LocalFluent.current
    AcrylicCard(modifier = modifier, corner = 20.dp, padding = PaddingValues(14.dp)) {
        Text(text = label, color = p.textTertiary, fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            color = accent ?: p.textPrimary,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )
        if (caption != null) {
            Text(
                text = caption,
                color = p.textTertiary,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun accentGradient(): Brush {
    val p = LocalFluent.current
    return Brush.linearGradient(listOf(p.accent, p.accentLight))
}

@Composable
fun DotBadge(color: Color, size: Dp = 7.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun VerticalRule(modifier: Modifier = Modifier, height: Dp = 26.dp) {
    val p = LocalFluent.current
    Box(
        modifier = modifier
            .width(1.dp)
            .height(height)
            .background(p.divider)
    )
}
