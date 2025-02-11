package com.android.customization.model.color

import android.annotation.ColorInt
import android.app.WallpaperColors
import android.content.Context
import android.provider.Settings
import android.util.SparseIntArray

import dev.kdrag0n.colorkt.Color
import dev.kdrag0n.colorkt.cam.Zcam
import dev.kdrag0n.colorkt.conversion.ConversionGraph.convert
import dev.kdrag0n.colorkt.data.Illuminants
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.colorkt.tristimulus.CieXyzAbs.Companion.toAbs
import dev.kdrag0n.colorkt.ucs.lab.CieLab
import dev.kdrag0n.monet.theme.DynamicColorScheme
import dev.kdrag0n.monet.theme.MaterialYouTargets

class WallpaperColorResources {
    val colorOverlay = SparseIntArray()

    private val cond = Zcam.ViewingConditions(
        surroundFactor = Zcam.ViewingConditions.SURROUND_AVERAGE,
        // sRGB
        adaptingLuminance = 0.4 * SRGB_WHITE_LUMINANCE,
        // Gray world
        backgroundLuminance = CieLab(
            L = 50.0,
            a = 0.0,
            b = 0.0,
        ).toXyz().y * SRGB_WHITE_LUMINANCE,
        referenceWhite = Illuminants.D65.toAbs(SRGB_WHITE_LUMINANCE),
    )

    private val targets = MaterialYouTargets(
        chromaFactor = 1.0,
        useLinearLightness = false,
        cond = cond,
    )

    constructor(wallpaperColors: WallpaperColors, context: Context) {
        // Generate color scheme
        val colorType = Settings.Secure.getInt(context.contentResolver,
                "monet_engine_color_type", 0)
        val colorOverride = Settings.Secure.getInt(context.contentResolver,
                "monet_engine_color_override", -1)
        val colorAccent = Settings.Secure.getInt(context.contentResolver,
                "monet_engine_color_accent", -1)
        val tintSurface = Settings.Secure.getInt(context.contentResolver,
                "monet_engine_tint_surface", 1) == 1
        val seedColor = when (colorType) {
            1 -> colorOverride
            2 -> colorAccent
            else -> wallpaperColors.primaryColor.toArgb()
        }
        val colorScheme = DynamicColorScheme(
            targets = targets,
            seedColor = Srgb(seedColor),
            chromaFactor = 1.0,
            cond = cond,
            accurateShades = true,
        )
        addOverlayColor(colorScheme.neutral1, android.R.color.system_neutral1_10)
        addOverlayColor(colorScheme.neutral2, android.R.color.system_neutral2_10)
        addOverlayColor(colorScheme.accent1, android.R.color.system_accent1_10)
        addOverlayColor(colorScheme.accent2, android.R.color.system_accent2_10)
        addOverlayColor(colorScheme.accent3, android.R.color.system_accent3_10)
    }

    private fun addOverlayColor(swatch: Map<Int, Color>, @ColorInt color: Int) {
        var i = color
        SHADES.forEach { shade ->
            colorOverlay.append(i, swatch[shade]!!.convert<Srgb>().toRgb8() or (0xff shl 24))
            i++
        }
    }

    companion object {
        private const val SRGB_WHITE_LUMINANCE = 200.0 // cd/m^2
        private val SHADES = intArrayOf(10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)
    }
}
