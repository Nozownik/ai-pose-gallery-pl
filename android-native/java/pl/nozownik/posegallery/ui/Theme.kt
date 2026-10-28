package pl.nozownik.posegallery.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF0B0D12)
val Surface = Color(0xFF151922)
val Surface2 = Color(0xFF1C2230)
val Accent = Color(0xFF7B7BFF)
val AccentSoft = Color(0xFF2A2A55)
val TextMain = Color(0xFFE8EAF0)
val TextMuted = Color(0xFF9AA3B5)
val Line = Color(0xFF2A3142)

private val colors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Accent,
    background = Bg,
    onBackground = TextMain,
    surface = Surface,
    onSurface = TextMain,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMuted,
    outline = Line
)

private val typography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextMain),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextMain),
    bodyLarge = TextStyle(fontSize = 15.sp, color = TextMain),
    bodyMedium = TextStyle(fontSize = 13.sp, color = TextMuted),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextMain),
    labelMedium = TextStyle(fontSize = 11.sp, color = TextMuted)
)

@Composable
fun PoseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
