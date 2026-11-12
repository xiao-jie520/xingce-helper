package com.xingce.fupan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * 自绘组件（趋势图、环形图、进度条等）使用的语义色。
 * 全部由 [MiuixTheme] 的 miuix 配色派生，保证与组件库同一套视觉语言。
 */
@Immutable
data class FluentPalette(
    val dark: Boolean,
    val canvasTop: Color,
    val canvasBottom: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val acrylic: Color,
    val acrylicStrong: Color,
    val acrylicSoft: Color,
    val strokeTop: Color,
    val strokeBottom: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentLight: Color,
    val accentAlt: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val shadow: Color,
    val track: Color,
    val glassTop: Color,
    val glassBottom: Color
)

/** 脱离 MiuixTheme 时的兜底配色（预览等场景） */
val LightPalette = FluentPalette(
    dark = false,
    canvasTop = Color(0xFFF7F7F7),
    canvasBottom = Color(0xFFEFEFEF),
    glowPrimary = Color(0x0D3482FF),
    glowSecondary = Color(0x08FF9500),
    acrylic = Color.White.copy(alpha = 0.92f),
    acrylicStrong = Color.White,
    acrylicSoft = Color(0xFFF2F2F7),
    strokeTop = Color.White.copy(alpha = 0.95f),
    strokeBottom = Color(0xFFE3E3E8).copy(alpha = 0.7f),
    divider = Color(0xFFE5E5EA),
    textPrimary = Color(0xFF171717),
    textSecondary = Color(0xFF5C5C5C),
    textTertiary = Color(0xFF9A9A9A),
    accent = Color(0xFF3482FF),
    accentLight = Color(0xFF5A9AFF),
    accentAlt = Color(0xFFFF9500),
    success = Color(0xFF2FA84F),
    warning = Color(0xFFE08600),
    danger = Color(0xFFE8453C),
    shadow = Color(0xFF1B1B1B),
    track = Color(0xFFF0F0F0),
    glassTop = Color(0xFFF7F7F7),
    glassBottom = Color(0xFFE9E9EE)
)

val DarkPalette = FluentPalette(
    dark = true,
    canvasTop = Color(0xFF0A0A0A),
    canvasBottom = Color(0xFF111111),
    glowPrimary = Color(0x2A3482FF),
    glowSecondary = Color(0x1EFF9500),
    acrylic = Color.White.copy(alpha = 0.08f),
    acrylicStrong = Color.White.copy(alpha = 0.11f),
    acrylicSoft = Color.White.copy(alpha = 0.05f),
    strokeTop = Color.White.copy(alpha = 0.14f),
    strokeBottom = Color.White.copy(alpha = 0.05f),
    divider = Color.White.copy(alpha = 0.10f),
    textPrimary = Color(0xFFF2F2F2),
    textSecondary = Color(0xFFADADAD),
    textTertiary = Color(0xFF7A7A7A),
    accent = Color(0xFF6AA1FF),
    accentLight = Color(0xFF8FBBFF),
    accentAlt = Color(0xFFFFB340),
    success = Color(0xFF30D158),
    warning = Color(0xFFFF9F0A),
    danger = Color(0xFFFF6961),
    shadow = Color.Black,
    track = Color.White.copy(alpha = 0.09f),
    glassTop = Color(0xFF232323),
    glassBottom = Color(0xFF161616)
)

val LocalFluent = staticCompositionLocalOf { LightPalette }

/**
 * 应用主题：以 miuix 的 [MiuixTheme] 为准（配色、文字、组件样式），
 * 同时给 Material3 组件（下拉菜单等）映射一套等价色板。
 */
@Composable
fun XingceTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val controller = remember { ThemeController(ColorSchemeMode.System) }

    MiuixTheme(controller = controller) {
        val cs = MiuixTheme.colorScheme
        val p = FluentPalette(
            dark = dark,
            // HyperOS 规范：页面底 = surface（灰），卡片 = background/surfaceContainer（白）
            canvasTop = cs.surface,
            canvasBottom = cs.surface,
            glowPrimary = Color.Transparent,
            glowSecondary = Color.Transparent,
            acrylic = cs.background,
            acrylicStrong = cs.background,
            acrylicSoft = cs.surfaceContainerHigh,
            strokeTop = Color.Transparent,
            strokeBottom = cs.dividerLine,
            divider = cs.dividerLine,
            textPrimary = cs.onSurface,
            textSecondary = cs.onSurfaceSecondary,
            textTertiary = cs.onSurfaceVariantSummary,
            accent = cs.primary,
            accentLight = cs.primaryVariant,
            accentAlt = cs.tertiaryContainer,
            success = if (dark) Color(0xFF30D158) else Color(0xFF2FA84F),
            warning = if (dark) Color(0xFFFF9F0A) else Color(0xFFE08600),
            danger = cs.error,
            shadow = Color.Black,
            track = cs.secondaryContainer,
            glassTop = cs.surfaceContainerHigh,
            glassBottom = cs.surfaceContainerHigh
        )

        val m3 = if (dark) {
            darkColorScheme(
                primary = cs.primary,
                onPrimary = cs.onPrimary,
                secondary = cs.secondary,
                background = cs.background,
                onBackground = cs.onBackground,
                surface = cs.surface,
                onSurface = cs.onSurface,
                error = cs.error
            )
        } else {
            lightColorScheme(
                primary = cs.primary,
                onPrimary = cs.onPrimary,
                secondary = cs.secondary,
                background = cs.background,
                onBackground = cs.onBackground,
                surface = cs.surface,
                onSurface = cs.onSurface,
                error = cs.error
            )
        }

        CompositionLocalProvider(LocalFluent provides p) {
            MaterialTheme(colorScheme = m3, content = content)
        }
    }
}
