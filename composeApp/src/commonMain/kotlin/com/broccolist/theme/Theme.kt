import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

object BroccolistTheme {
    private val LightColors = lightColorScheme(
        primary = BroccolistColors.PrimaryGreen,
        // primaryContainer is the light-green surface used by disabled
        // buttons, icon badges, and the top app bar.
        primaryContainer = BroccolistColors.SelectionGreen,
        secondary = BroccolistColors.CheckGreen,
        secondaryContainer = BroccolistColors.LightGray,
        tertiary = BroccolistColors.SelectionGreen,
        background = BroccolistColors.White,
        surface = BroccolistColors.White,
        onPrimary = BroccolistColors.White,
        onSecondary = BroccolistColors.Black,
        onBackground = BroccolistColors.Black,
        onSurface = BroccolistColors.Black,
        surfaceContainer = BroccolistColors.ModalSheetLightGray,
        tertiaryContainer = BroccolistColors.EditGray,
        onTertiaryContainer = BroccolistColors.Black,
    )

    private val DarkColors = darkColorScheme(
        // Bright-but-light green for the active accent
        primary = BroccolistColors.PrimaryGreenLight,
        // Muted darker green for disabled buttons and icon badges — has
        // enough chroma against black to read as "available but inactive".
        primaryContainer = BroccolistColors.PrimaryGreenDark,
        secondary = BroccolistColors.PrimaryGreenLight,
        secondaryContainer = BroccolistColors.DarkGray,
        tertiary = BroccolistColors.SelectionGreenDark,
        background = BroccolistColors.Black,
        surface = BroccolistColors.Black,
        onPrimary = BroccolistColors.Black,
        onSecondary = BroccolistColors.White,
        onBackground = BroccolistColors.White,
        onSurface = BroccolistColors.White,
        surfaceContainer = BroccolistColors.ModalSheetDarkGray,
        tertiaryContainer = BroccolistColors.EditGrayDark,
        onTertiaryContainer = BroccolistColors.White,
    )

    @Composable
    fun BroccolistTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
    ) {
        val colorScheme = if (darkTheme) {
            DarkColors
        } else {
            LightColors
        }

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
