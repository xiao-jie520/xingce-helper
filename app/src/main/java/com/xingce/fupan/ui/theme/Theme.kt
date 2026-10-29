package com.xingce.fupan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * Fluent 质感的语义色板：亚克力表面 + 高光描边 + 多层柔影。
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
    val track: Color
)

/** 素白 · 松绿 · 赭金 */
val LightPalette = FluentPalette(
    dark = false,
    canvasTop = Color(0xFFFDFDFC),
    canvasBottom = Color(0xFFEDEEE9),
    glowPrimary = Color(0x0E1F6B5B),
    glowSecondary = Color(0x08B98433),
    acrylic = Color(0xFFFFFFFF).copy(alpha = 0.92f),
    acrylicStrong = Color(0xFFFFFFFF).copy(alpha = 0.98f),
    acrylicSoft = Color(0xFFFFFFFF).copy(alpha = 0.70f),
    strokeTop = Color.White.copy(alpha = 0.95f),
    strokeBottom = Color(0xFFC4C9BE).copy(alpha = 0.58f),
    divider = Color(0xFF1E1B16).copy(alpha = 0.07f),
    textPrimary = Color(0xFF1E1B16),
    textSecondary = Color(0xFF5C5548),
    textTertiary = Color(0xFF948A78),
    accent = Color(0xFF1F6B5B),
    accentLight = Color(0xFF3D907C),
    accentAlt = Color(0xFFB98433),
    success = Color(0xFF3F7D39),
    warning = Color(0xFF9E6C12),
    danger = Color(0xFFBE3A2B),
    shadow = Color(0xFF2A322D),
    track = Color(0xFF1E1B16).copy(alpha = 0.06f)
)

/** 松烟墨底色 */
val DarkPalette = FluentPalette(
    dark = true,
    canvasTop = Color(0xFF0E1312),
    canvasBottom = Color(0xFF131A18),
    glowPrimary = Color(0x334FBF9F),
    glowSecondary = Color(0x26D2A45C),
    acrylic = Color.White.copy(alpha = 0.07f),
    acrylicStrong = Color.White.copy(alpha = 0.10f),
    acrylicSoft = Color.White.copy(alpha = 0.045f),
    strokeTop = Color.White.copy(alpha = 0.16f),
    strokeBottom = Color.White.copy(alpha = 0.045f),
    divider = Color.White.copy(alpha = 0.08f),
    textPrimary = Color(0xFFF2EFE6),
    textSecondary = Color(0xFFA8A395),
    textTertiary = Color(0xFF767162),
    accent = Color(0xFF55C4A6),
    accentLight = Color(0xFF7FDCC2),
    accentAlt = Color(0xFFD9AB62),
    success = Color(0xFF5FBF7E),
    warning = Color(0xFFE2B45E),
    danger = Color(0xFFE4675A),
    shadow = Color(0xFF000000),
    track = Color.White.copy(alpha = 0.08f)
)

val LocalFluent = staticCompositionLocalOf { LightPalette }

private val FluentTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 46.sp, letterSpacing = (-1.2).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = (-0.8).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 11.5.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 10.5.sp, letterSpacing = 0.3.sp)
)

@Composable
fun XingceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            secondary = palette.accentAlt,
            background = palette.canvasTop,
            onBackground = palette.textPrimary,
            surface = Color(0xFF141A24),
            onSurface = palette.textPrimary,
            surfaceVariant = Color(0xFF1B222E),
            onSurfaceVariant = palette.textSecondary,
            outline = palette.strokeBottom,
            error = palette.danger
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            secondary = palette.accentAlt,
            background = palette.canvasTop,
            onBackground = palette.textPrimary,
            surface = Color.White,
            onSurface = palette.textPrimary,
            surfaceVariant = Color(0xFFF0F3FA),
            onSurfaceVariant = palette.textSecondary,
            outline = palette.strokeBottom,
            error = palette.danger
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalFluent provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = FluentTypography,
            content = content
        )
    }
}

/** 便捷扩展：主题色板 */
val fluent: FluentPalette
    @Composable get() = LocalFluent.current
