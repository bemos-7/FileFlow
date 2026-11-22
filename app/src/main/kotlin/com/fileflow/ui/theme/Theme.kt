package com.fileflow.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DarkColors = darkColorScheme(
    primary = Color(0xFF3978F6),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF3978F6),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF111214),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF191A1D),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF222328),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF303136),
    outlineVariant = Color(0xFF2A2B30),
    error = Color(0xFFEF4444)
)

val LightColors = lightColorScheme(
    primary = Color(0xFF3978F6),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF777B85),
    onSecondary = Color(0xFF17181C),
    background = Color(0xFFF5F6F8),
    onBackground = Color(0xFF17181C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17181C),
    surfaceVariant = Color(0xFFF0F1F4),
    onSurfaceVariant = Color(0xFF777B85),
    outline = Color(0xFFE2E4E9),
    outlineVariant = Color(0xFFE2E4E9),
    error = Color(0xFFE5484D)
)

val FileFlowShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(14.dp)
)

val FileFlowTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge = TextStyle(fontSize = 14.sp),
    bodyMedium = TextStyle(fontSize = 13.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)
)

@Composable
fun FileFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = FileFlowTypography,
        shapes = FileFlowShapes,
        content = content
    )
}