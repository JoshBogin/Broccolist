import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

object BroccolistTheme {
    private val LightColors = lightColorScheme(
        primary = BroccolistColors.PrimaryGreen,
        primaryContainer = BroccolistColors.White,
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
    )

    // You can also define dark colors if you want to support dark mode
    private val DarkColors = darkColorScheme(
        primary = BroccolistColors.LightGreen,
        primaryContainer = BroccolistColors.Black,
        secondary = BroccolistColors.LightGreen,
        secondaryContainer = BroccolistColors.DarkGray,
        tertiary = BroccolistColors.SelectionGreenDark,
        background = BroccolistColors.Black,
        surface = BroccolistColors.Black,
        onPrimary = BroccolistColors.Black,
        onSecondary = BroccolistColors.White,
        onBackground = BroccolistColors.White,
        onSurface = BroccolistColors.White,
        surfaceContainer = BroccolistColors.ModalSheetDarkGray,
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
