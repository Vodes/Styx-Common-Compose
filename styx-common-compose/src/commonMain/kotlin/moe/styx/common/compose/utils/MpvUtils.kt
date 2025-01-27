package moe.styx.common.compose.utils

import com.russhwolf.settings.get
import kotlinx.serialization.Serializable
import moe.styx.common.Platform
import moe.styx.common.compose.settings
import moe.styx.common.extension.capitalize
import moe.styx.common.json

val videoOutputDriverChoices = listOf("gpu-next", "gpu")
val gpuApiChoices =
    when (Platform.current) {
        Platform.ANDROID, Platform.IOS -> listOf("opengl", "vulkan")
        else -> listOf("auto", "vulkan", "d3d11", "opengl")
    }

val profileChoices =
    when (Platform.current) {
        Platform.ANDROID, Platform.IOS -> listOf("high", "normal", "fast")
        else -> listOf("high", "normal", "light", "fast")
    }

val debandIterationsChoices = if (Platform.current == Platform.JVM) listOf("4", "3", "2") else listOf("3", "2", "1")


object MpvDesc {
    val profileDescription = """
        Sorted from best to worst (and slowest to fastest).
        This mostly affects scaling and if you have a relatively modern machine, it should have no issues with "high".
    """.trimIndent()

    val hwDecoding = """
        Use your GPU to decode the video.
        This is both faster and more efficient.
        Leave on if at all possible and not causing issues.
    """.trimIndent()

    val deband = if (Platform.current == Platform.JVM) """
        Removes colorbanding from the video.
        Keep this on if you don't have any performance issues.
        Can also be toggled with h in the player.
    """.trimIndent()
    else """
        Removes colorbanding from the video.
        This might be quite heavy for mobile devices but worth it for WEB releases if your device is strong enough.
    """.trimIndent()

    val oversample = """
        Interpolates by showing every frame 2.5 times (on 60hz).
        This can make the video feel way smoother but cause issues with advanced subtitles.
        G-Sync makes this redundant.
    """.trimIndent()

    val gpuAPI = """
        The rendering backend used.
        Keep at auto if you don't know what you're doing.
        Auto is basically d3d11 on windows and vulkan everywhere else.
    """.trimIndent()

    val outputDriver = """
        Keep at gpu-next if you don't know what you're doing and if not causing issues.
        This is in theory faster and has higher quality.
    """.trimIndent()

    val downmix = """
        This forces a custom downmix for surround sound audio.
        May be useful if you think that surround audio sounds like crap on your headphones.
        Or if you just don't have a 5.1+ setup.
    """.trimIndent()

    val dither10bit = """
        Forces dithering to 10bit because MPV's auto detection is broken.
        Only use if you know that your display is 10bit.
    """.trimIndent()

    val blendSubs = """
        Render subtitles in the same colorspace as your video.
        This makes the colored stuff in subtitles more accurate but may be a little taxing on the performance.
        Keep this on if you don't have any performance issues.
    """.trimIndent()
}

@Serializable
data class MpvPreferences(
    val gpuAPI: String = gpuApiChoices[0],
    val videoOutputDriver: String = videoOutputDriverChoices[0],
    val profile: String = if (Platform.current == Platform.JVM) profileChoices[0] else profileChoices[2],
    val deband: Boolean = Platform.current == Platform.JVM,
    val debandIterations: String = if (Platform.current == Platform.JVM) debandIterationsChoices[0] else debandIterationsChoices[2],
    val hwDecoding: Boolean = true,
    val alternativeHwDecode: Boolean = false,
    val oversampleInterpol: Boolean = false,
    val dither10bit: Boolean = false,
    val customDownmix: Boolean = false,
    val blendSubs: Boolean = true,
    val preferGerman: Boolean = false,
    val preferEnDub: Boolean = false,
    val preferDeDub: Boolean = false,
) {
    fun getSlangArg(): String {
        return if (preferGerman)
            "de,ger,en,eng"
        else
            "en,eng,de,ger"
    }

    fun getAlangArg(): String {
        return if (preferEnDub)
            "en,eng,jp,jpn,de,ger"
        else if (preferDeDub)
            "de,ger,jp,jpn,en,eng"
        else
            "jp,jpn,en,eng,de,ger"
    }

    fun getPlatformProfile(): String {
        return when (Platform.current) {
            Platform.JVM -> "styx${profile.capitalize()}"
            else -> when (profile) {
                "high" -> "high-quality"
                "normal" -> "default"
                else -> "fast"
            }
        }
    }

    companion object {
        fun getOrDefault(): MpvPreferences {
            return runCatching { json.decodeFromString<MpvPreferences>(settings["mpv-preferences", ""]) }.getOrNull() ?: MpvPreferences()
        }
    }
}