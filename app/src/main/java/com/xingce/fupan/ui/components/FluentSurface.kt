package com.xingce.fupan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
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
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 页面背景：直接使用 miuix 的 background 色，保持与顶栏、卡片同一套底。
 * 不再叠加光晕——那会在四角留下色块。
 */
@Composable
fun FluentBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val p = LocalFluent.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(p.canvasTop)
    ) {
        content()
    }
}

/**
 * 卡片：直接使用 miuix 的 [MiuixCard]（连续圆角、按压反馈、标准内边距）。
 * 在 HyperOS 规范里卡片是白色容器，浮在灰色页面底上形成区域划分。
 */
@Composable
fun AcrylicCard(
    modifier: Modifier = Modifier,
    corner: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(18.dp),
    strong: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        MiuixCard(
            modifier = modifier,
            cornerRadius = corner,
            insideMargin = padding,
            onClick = onClick,
            content = content
        )
    } else {
        MiuixCard(
            modifier = modifier,
            cornerRadius = corner,
            insideMargin = padding,
            content = content
        )
    }
}

/** 分组标题：HyperOS 设置风格的小标签，位于卡片上方 */
@Composable
fun GroupLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    val cs = MiuixTheme.colorScheme
    Text(
        text = text,
        color = cs.onSurfaceVariantSummary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
        modifier = modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}

/** 卡片内行与行之间的分隔线（左侧留出与文字对齐的缩进） */
@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    val cs = MiuixTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 2.dp)
            .height(1.dp)
            .background(cs.dividerLine)
    )
}

/** 圆形图标按钮（顶栏右侧动作），保持轻量圆钮形态 */
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
            .border(1.dp, p.strokeBottom.copy(alpha = 0.6f), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.foundation.LocalIndication.current,
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

/** 主按钮：miuix [MiuixButton]（primary 配色 + 连续圆角） */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    hint: String? = null
) {
    val cs = MiuixTheme.colorScheme
    MiuixButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        cornerRadius = 22.dp,
        minWidth = 0.dp,
        insideMargin = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        colors = MiuixButtonDefaults.buttonColorsPrimary(),
        content = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = cs.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = cs.onPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (hint != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = hint,
                    color = cs.onPrimary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    )
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

/** 标签：可点击时用 miuix 按钮（连续圆角 + 标准按压反馈） */
@Composable
fun PillChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    tint: Color? = null
) {
    val p = LocalFluent.current
    val cs = MiuixTheme.colorScheme
    if (onClick != null) {
        MiuixButton(
            onClick = onClick,
            modifier = modifier.height(30.dp),
            cornerRadius = 50.dp,
            minWidth = 0.dp,
            insideMargin = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = if (selected) {
                MiuixButtonDefaults.buttonColorsPrimary()
            } else {
                MiuixButtonDefaults.buttonColors()
            },
            content = {
                Text(
                    text = text,
                    color = if (selected) cs.onPrimary else (tint ?: cs.onSecondaryVariant),
                    fontSize = 11.5.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(50))
                .background(if (selected) p.accent else p.track)
                .padding(horizontal = 13.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                color = if (selected) cs.onPrimary else (tint ?: p.textSecondary),
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
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
                .background(p.acrylicStrong)
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
