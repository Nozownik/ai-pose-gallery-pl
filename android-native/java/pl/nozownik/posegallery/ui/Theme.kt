package pl.nozownik.posegallery.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pl.nozownik.posegallery.data.AppThemeId

data class PoseColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val accent: Color,
    val accentSoft: Color,
    val textMain: Color,
    val textMuted: Color,
    val line: Color,
    val scheme: ColorScheme
)

val LocalPoseColors = staticCompositionLocalOf { darkPalette() }

private fun darkPalette(
    bg: Color = Color(0xFF0B0D12),
    surface: Color = Color(0xFF151922),
    surface2: Color = Color(0xFF1C2230),
    accent: Color = Color(0xFF7B7BFF),
    accentSoft: Color = Color(0xFF2A2A55),
    textMain: Color = Color(0xFFE8EAF0),
    textMuted: Color = Color(0xFF9AA3B5),
    line: Color = Color(0xFF2A3142)
) = PoseColors(
    bg, surface, surface2, accent, accentSoft, textMain, textMuted, line,
    darkColorScheme(
        primary = accent,
        onPrimary = Color.White,
        secondary = accent,
        background = bg,
        onBackground = textMain,
        surface = surface,
        onSurface = textMain,
        surfaceVariant = surface2,
        onSurfaceVariant = textMuted,
        outline = line
    )
)

fun paletteFor(theme: AppThemeId): PoseColors = when (theme) {
    AppThemeId.DARK -> darkPalette()
    AppThemeId.AMOLED -> darkPalette(
        bg = Color(0xFF000000),
        surface = Color(0xFF0A0A0A),
        surface2 = Color(0xFF141414),
        accent = Color(0xFF8B8BFF),
        accentSoft = Color(0xFF1A1A33),
        line = Color(0xFF222222)
    )
    AppThemeId.LIGHT -> PoseColors(
        bg = Color(0xFFF4F5F8),
        surface = Color(0xFFFFFFFF),
        surface2 = Color(0xFFE8EBF2),
        accent = Color(0xFF5B5BFF),
        accentSoft = Color(0xFFE0E0FF),
        textMain = Color(0xFF12141A),
        textMuted = Color(0xFF5C6578),
        line = Color(0xFFD0D5E0),
        scheme = lightColorScheme(
            primary = Color(0xFF5B5BFF),
            onPrimary = Color.White,
            secondary = Color(0xFF5B5BFF),
            background = Color(0xFFF4F5F8),
            onBackground = Color(0xFF12141A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF12141A),
            surfaceVariant = Color(0xFFE8EBF2),
            onSurfaceVariant = Color(0xFF5C6578),
            outline = Color(0xFFD0D5E0)
        )
    )
    AppThemeId.PURPLE -> darkPalette(
        bg = Color(0xFF120B1A),
        surface = Color(0xFF1C1228),
        surface2 = Color(0xFF281A38),
        accent = Color(0xFFC084FC),
        accentSoft = Color(0xFF3B1F5C),
        line = Color(0xFF3A2A4E)
    )
    AppThemeId.CYAN -> darkPalette(
        bg = Color(0xFF071115),
        surface = Color(0xFF0E1A20),
        surface2 = Color(0xFF152830),
        accent = Color(0xFF22D3EE),
        accentSoft = Color(0xFF0E3A44),
        line = Color(0xFF1E3A44)
    )
}

private val typography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodyMedium = TextStyle(fontSize = 13.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontSize = 11.sp)
)

@Composable
fun PoseTheme(themeId: AppThemeId = AppThemeId.DARK, content: @Composable () -> Unit) {
    val colors = paletteFor(themeId)
    CompositionLocalProvider(LocalPoseColors provides colors) {
        MaterialTheme(colorScheme = colors.scheme, typography = typography, content = content)
    }
}

// Back-compat aliases used by older call sites during migration
val Bg get() = Color(0xFF0B0D12)
val Surface get() = Color(0xFF151922)
val Surface2 get() = Color(0xFF1C2230)
val Accent get() = Color(0xFF7B7BFF)
val AccentSoft get() = Color(0xFF2A2A55)
val TextMain get() = Color(0xFFE8EAF0)
val TextMuted get() = Color(0xFF9AA3B5)
val Line get() = Color(0xFF2A3142)
