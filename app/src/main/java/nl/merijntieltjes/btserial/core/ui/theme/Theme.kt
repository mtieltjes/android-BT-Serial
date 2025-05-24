package nl.merijntieltjes.btserial.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightPalette = lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF001D36),

    secondary = Color(0xFF1E88E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E7FF),
    onSecondaryContainer = Color(0xFF002A4A),

    tertiary = Color(0xFF1EB980),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB3F6DE),
    onTertiaryContainer = Color(0xFF003927),

    error = Color(0xFFF44336),
    onError = Color.White,

    background = Color.White,
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFF6FAFD),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFE0E7F1),
    outline = Color(0xFF90A4AE),
)

private val DarkPalette = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF00325A),
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFBBDEFB),

    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF002A4A),
    secondaryContainer = Color(0xFF0B3D77),
    onSecondaryContainer = Color(0xFFD0E7FF),

    tertiary = Color(0xFF66E0C4),
    onTertiary = Color(0xFF003927),
    tertiaryContainer = Color(0xFF00513B),
    onTertiaryContainer = Color(0xFFB3F6DE),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1B1F23),
    onSurface = Color(0xFFC7C6CA),
    surfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF8A9099),
)

@Composable
fun BluetoothSerialSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color is available on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkPalette
        else -> LightPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}