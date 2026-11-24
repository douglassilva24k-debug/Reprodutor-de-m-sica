package com.retroplayer.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Tokens de estética do app. Qualquer tela (inclusive plugins, na Parte 2)
 * recebe estas cores e desenha seus botões a partir delas.
 */
data class Skin(
    val id: String,
    val name: String,
    val metalLight: Color,
    val metalMid: Color,
    val metalDark: Color,
    val glow: Color,
    val bodyTop: Color,
    val bodyBottom: Color,
    val ink: Color,
) {
    val sweep: List<Color>
        get() = listOf(metalLight, metalDark, metalLight, metalMid, metalDark, metalLight)

    /** Variáveis CSS que os plugins poderão usar para herdar o visual do app. */
    fun cssVars(): String {
        fun hex(c: Color) = String.format("#%06X", 0xFFFFFF and c.toArgb())
        return ":root{" +
            "--mp-metal-light:${hex(metalLight)};" +
            "--mp-metal-mid:${hex(metalMid)};" +
            "--mp-metal-dark:${hex(metalDark)};" +
            "--mp-glow:${hex(glow)};" +
            "--mp-body-top:${hex(bodyTop)};" +
            "--mp-body-bottom:${hex(bodyBottom)};" +
            "--mp-ink:${hex(ink)};" +
            "}"
    }
}

object Skins {
    val Silver = Skin(
        id = "silver", name = "Prata",
        metalLight = Color(0xFFF4F4F4), metalMid = Color(0xFFB8B8B8), metalDark = Color(0xFF6E6E6E),
        glow = Color(0xFFFF2A2A),
        bodyTop = Color(0xFFD9D9D9), bodyBottom = Color(0xFF8E8E8E),
        ink = Color(0xFF222222),
    )
    val Gold = Skin(
        id = "gold", name = "Dourado",
        metalLight = Color(0xFFFFE9A0), metalMid = Color(0xFFD4A62A), metalDark = Color(0xFF8A6410),
        glow = Color(0xFFFFB300),
        bodyTop = Color(0xFFF0C95A), bodyBottom = Color(0xFF9C7314),
        ink = Color(0xFF3A2600),
    )
    val Copper = Skin(
        id = "copper", name = "Cobre",
        metalLight = Color(0xFFFFD2B0), metalMid = Color(0xFFC8794A), metalDark = Color(0xFF7A3E1E),
        glow = Color(0xFF40E0FF),
        bodyTop = Color(0xFFD99063), bodyBottom = Color(0xFF804220),
        ink = Color(0xFF2E1308),
    )
    val all = listOf(Silver, Gold, Copper)
}
