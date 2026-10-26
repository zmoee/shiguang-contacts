package com.zmoee.shiguang.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zmoee.shiguang.R

private val ShiguangLightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    inversePrimary = Color(0xFFD0BCFF),
    secondary = Color(0xFF635A75),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceTint = Color(0xFF6750A4),
    inverseSurface = Color(0xFF322F35),
    inverseOnSurface = Color(0xFFF5EFF7),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFEF7FF),
    surfaceDim = Color(0xFFDED8E1),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF3EDF7),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerHighest = Color(0xFFE6E0E9),
)

private val RobotoFamily = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold),
)

private fun TextStyle.withRoboto() = copy(fontFamily = RobotoFamily)

private val ShiguangTypography: Typography = run {
    val base = Typography()
    Typography(
        displayLarge = base.displayLarge.withRoboto(),
        displayMedium = base.displayMedium.withRoboto(),
        displaySmall = base.displaySmall.withRoboto(),
        headlineLarge = base.headlineLarge.withRoboto(),
        headlineMedium = base.headlineMedium.withRoboto(),
        headlineSmall = base.headlineSmall.withRoboto(),
        titleLarge = base.titleLarge.withRoboto(),
        titleMedium = base.titleMedium.withRoboto(),
        titleSmall = base.titleSmall.withRoboto(),
        bodyLarge = base.bodyLarge.withRoboto(),
        bodyMedium = base.bodyMedium.withRoboto(),
        bodySmall = base.bodySmall.withRoboto(),
        labelLarge = base.labelLarge.withRoboto(),
        labelMedium = base.labelMedium.withRoboto(),
        labelSmall = base.labelSmall.withRoboto(),
    )
}

private val ShiguangShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun ShiguangTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = ShiguangLightColors,
        motionScheme = MotionScheme.standard(),
        typography = ShiguangTypography,
        shapes = ShiguangShapes,
        content = content,
    )
}
