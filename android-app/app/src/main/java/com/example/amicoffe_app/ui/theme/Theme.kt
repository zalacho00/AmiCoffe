package com.example.amicoffe_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AmiCoffeLightColorScheme = lightColorScheme(
    primary = AmiGreenDark,
    onPrimary = AmiCardBg,
    secondary = AmiAccentOrange,
    background = AmiScreenBg,
    surface = AmiCardBg,
    onBackground = AmiTextPrimary,
    onSurface = AmiTextPrimary,
)

@Composable
fun AmiCoffeappTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AmiCoffeLightColorScheme,
        typography = Typography,
        content = content
    )
}