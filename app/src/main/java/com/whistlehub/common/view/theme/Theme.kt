package com.whistlehub.common.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
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
private val CustomDarkColorScheme = darkColorScheme(
    primary = CustomColors().Grey500,
    secondary = CustomColors().Grey300,
    tertiary = CustomColors().Error500,
    error = CustomColors().Error700,
    background = Color(0xFF141218),
    onPrimary = CustomColors().Grey950,
    onSecondary = CustomColors().Grey50,
    onTertiary = CustomColors().Grey50,
    onBackground = CustomColors().Grey50,
    onError = CustomColors().Grey50,
)

@Composable
fun WhistleHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
    MaterialTheme(
//        colorScheme = colorScheme,
        colorScheme = CustomDarkColorScheme,
        typography = Typography,
        content = content
    )
}