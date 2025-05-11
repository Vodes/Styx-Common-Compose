package moe.styx.common.compose.components

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import moe.styx.styx_common_compose.generated.resources.*
import org.jetbrains.compose.resources.Font

private val defaultTypo = Typography()

@Suppress("ComposableNaming")
object AppFont {

    @Composable
    fun OpenSans(): FontFamily {
        return FontFamily(
            Font(Res.font.OpenSans_Variable, FontWeight.Light, FontStyle.Normal),
            Font(Res.font.OpenSans_Italic_Variable, FontWeight.Light, FontStyle.Italic),
            Font(Res.font.OpenSans_Variable, FontWeight.Normal, FontStyle.Normal),
            Font(Res.font.OpenSans_Italic_Variable, FontWeight.Normal, FontStyle.Italic),
            Font(Res.font.OpenSans_Variable, FontWeight.Medium, FontStyle.Normal),
            Font(Res.font.OpenSans_Italic_Variable, FontWeight.Medium, FontStyle.Italic),
            Font(Res.font.OpenSans_Variable, FontWeight.Bold, FontStyle.Normal),
            Font(Res.font.OpenSans_Italic_Variable, FontWeight.Bold, FontStyle.Italic),
        )
    }

    @Composable
    fun JetbrainsMono(): FontFamily {
        return FontFamily(
            Font(Res.font.JetBrainsMono_Variable, FontWeight.Light, FontStyle.Normal),
            Font(Res.font.JetBrainsMono_Italic_Variable, FontWeight.Light, FontStyle.Italic),
            Font(Res.font.JetBrainsMono_Variable, FontWeight.Normal, FontStyle.Normal),
            Font(Res.font.JetBrainsMono_Italic_Variable, FontWeight.Normal, FontStyle.Italic),
            Font(Res.font.JetBrainsMono_Variable, FontWeight.Medium, FontStyle.Normal),
            Font(Res.font.JetBrainsMono_Italic_Variable, FontWeight.Medium, FontStyle.Italic),
            Font(Res.font.JetBrainsMono_Variable, FontWeight.Bold, FontStyle.Normal),
            Font(Res.font.JetBrainsMono_Italic_Variable, FontWeight.Bold, FontStyle.Italic),
        )
    }
}

val FontFamily.Typography: androidx.compose.material3.Typography
    get() = Typography(
        displayLarge = defaultTypo.displayLarge.copy(fontFamily = this),
        displayMedium = defaultTypo.displayMedium.copy(fontFamily = this),
        displaySmall = defaultTypo.displaySmall.copy(fontFamily = this),

        headlineLarge = defaultTypo.headlineLarge.copy(fontFamily = this),
        headlineMedium = defaultTypo.headlineMedium.copy(fontFamily = this),
        headlineSmall = defaultTypo.headlineSmall.copy(fontFamily = this),

        titleLarge = defaultTypo.titleLarge.copy(fontFamily = this),
        titleMedium = defaultTypo.titleMedium.copy(fontFamily = this),
        titleSmall = defaultTypo.titleSmall.copy(fontFamily = this),

        bodyLarge = defaultTypo.bodyLarge.copy(fontFamily = this),
        bodyMedium = defaultTypo.bodyMedium.copy(fontFamily = this),
        bodySmall = defaultTypo.bodySmall.copy(fontFamily = this),

        labelLarge = defaultTypo.labelLarge.copy(fontFamily = this),
        labelMedium = defaultTypo.labelMedium.copy(fontFamily = this),
        labelSmall = defaultTypo.labelSmall.copy(fontFamily = this)
    )
