package mx.edu.utez.mirecetario.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
private val LightColorScheme = lightColorScheme(
    primary = AzulBotones,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    onPrimary = Color.White,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MiRecetarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    primaryColor: Color = AzulBotones,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.3f),
            onPrimaryContainer = Color.White,
            secondary = primaryColor,
            onSecondary = Color.White,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onBackground = Color(0xFFE2E2E2),
            onSurface = Color(0xFFE2E2E2)
        )
        else -> lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            secondary = primaryColor,
            onSecondary = Color.White,
            background = Color(0xFFFFFBFE),
            surface = Color(0xFFFFFBFE),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}