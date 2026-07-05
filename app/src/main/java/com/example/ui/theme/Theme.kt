package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = SleekPrimary,
    secondary = SleekSecondary,
    tertiary = SleekTertiary,
    background = SleekBg,
    surface = SleekSurface,
    onPrimary = Color.White,
    onSecondary = SleekOnBg,
    onBackground = SleekOnBg,
    onSurface = SleekOnSurface,
    surfaceVariant = SleekSurfaceVariant,
    outline = SleekOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekPrimary,
    secondary = Color(0xFFE2E8F0),
    tertiary = SleekTertiary,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color(0xFF0F1115),
    onBackground = Color(0xFF0F1115),
    onSurface = Color(0xFF0F1115),
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFCBD5E1)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
