package com.retroplayer.app

import androidx.compose.ui.graphics.Color

data class RetroSkin(
    val id: String,
    val name: String,
    val metalLight: Color,
    val metalMid: Color,
    val metalDark: Color,
    val metalBorder: Color,
    val lcdBg: Color,
    val lcdText: Color,
    val lcdDim: Color,
    val accent: Color
)

val RetroSkins = listOf(
    RetroSkin(
        id = "titanium",
        name = "Titânio Clássico",
        metalLight = Color(0xFFD8DEE4),
        metalMid = Color(0xFF9AA3AD),
        metalDark = Color(0xFF4E5660),
        metalBorder = Color(0xFF2B3036),
        lcdBg = Color(0xFF0E1D14),
        lcdText = Color(0xFF4AF676),
        lcdDim = Color(0xFF1D442B),
        accent = Color(0xFF00E676)
    ),
    RetroSkin(
        id = "cyber_blue",
        name = "Cobalto Y2K",
        metalLight = Color(0xFFB8D4F5),
        metalMid = Color(0xFF5D8EC9),
        metalDark = Color(0xFF23416B),
        metalBorder = Color(0xFF12233B),
        lcdBg = Color(0xFF071526),
        lcdText = Color(0xFF38E8FF),
        lcdDim = Color(0xFF133A5C),
        accent = Color(0xFF00B0FF)
    ),
    RetroSkin(
        id = "gold_edition",
        name = "Ouro Metálico",
        metalLight = Color(0xFFF7E5A8),
        metalMid = Color(0xFFC9A449),
        metalDark = Color(0xFF6E5319),
        metalBorder = Color(0xFF3B2B0A),
        lcdBg = Color(0xFF1C1407),
        lcdText = Color(0xFFFFD54F),
        lcdDim = Color(0xFF4A3712),
        accent = Color(0xFFFFB300)
    ),
    RetroSkin(
        id = "crimson_steel",
        name = "Aço Rubi",
        metalLight = Color(0xFFF2B8C1),
        metalMid = Color(0xFFC25969),
        metalDark = Color(0xFF66222D),
        metalBorder = Color(0xFF380F16),
        lcdBg = Color(0xFF1F080C),
        lcdText = Color(0xFFFF5252),
        lcdDim = Color(0xFF4F161F),
        accent = Color(0xFFFF1744)
    ),
    RetroSkin(
        id = "emerald_chrome",
        name = "Cromo Esmeralda",
        metalLight = Color(0xFFB5E8CE),
        metalMid = Color(0xFF52A87D),
        metalDark = Color(0xFF1E573B),
        metalBorder = Color(0xFF0E2E1E),
        lcdBg = Color(0xFF071C12),
        lcdText = Color(0xFF69F0AE),
        lcdDim = Color(0xFF174A31),
        accent = Color(0xFF00E676)
    ),
    RetroSkin(
        id = "purple_haze",
        name = "Níquel Violeta",
        metalLight = Color(0xFFDCC2F2),
        metalMid = Color(0xFF8E63B5),
        metalDark = Color(0xFF452763),
        metalBorder = Color(0xFF231136),
        lcdBg = Color(0xFF140821),
        lcdText = Color(0xFFE040FB),
        lcdDim = Color(0xFF3B185C),
        accent = Color(0xFFD500F9)
    ),
    RetroSkin(
        id = "stealth_black",
        name = "Grafite Escuro",
        metalLight = Color(0xFF757B82),
        metalMid = Color(0xFF42464B),
        metalDark = Color(0xFF1E2023),
        metalBorder = Color(0xFF0D0E10),
        lcdBg = Color(0xFF090C10),
        lcdText = Color(0xFF00E5FF),
        lcdDim = Color(0xFF162B36),
        accent = Color(0xFF00B8D4)
    )
)
