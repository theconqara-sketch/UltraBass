package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.model.Song

data class MaterialYouSongPalette(
    val dominantBg: Color,
    val secondaryBg: Color,
    val surfaceContainer: Color,
    val primaryAccent: Color,
    val onPrimaryAccent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val iconTint: Color
)

object PaletteUtils {

    private val presetPalettes = listOf(
        // Palette 1: Deep Indigo & Soft Violet
        MaterialYouSongPalette(
            dominantBg = Color(0xFF131524),
            secondaryBg = Color(0xFF1D2036),
            surfaceContainer = Color(0xFF272B48),
            primaryAccent = Color(0xFFB3C5FF),
            onPrimaryAccent = Color(0xFF1B2A5E),
            secondaryAccent = Color(0xFFE0C2FF),
            textPrimary = Color(0xFFEEF0FF),
            textSecondary = Color(0xFFACB2D4),
            iconTint = Color(0xFFD4E0FF)
        ),
        // Palette 2: Warm Amber & Muted Copper
        MaterialYouSongPalette(
            dominantBg = Color(0xFF1C1612),
            secondaryBg = Color(0xFF2B211A),
            surfaceContainer = Color(0xFF3B2E24),
            primaryAccent = Color(0xFFFFB878),
            onPrimaryAccent = Color(0xFF4A2800),
            secondaryAccent = Color(0xFFFFDBC3),
            textPrimary = Color(0xFFFFF0E6),
            textSecondary = Color(0xFFD4C1B4),
            iconTint = Color(0xFFFFD0A6)
        ),
        // Palette 3: Soft Emerald & Sage
        MaterialYouSongPalette(
            dominantBg = Color(0xFF111C18),
            secondaryBg = Color(0xFF1A2A24),
            surfaceContainer = Color(0xFF253B33),
            primaryAccent = Color(0xFF90F1CC),
            onPrimaryAccent = Color(0xFF003828),
            secondaryAccent = Color(0xFFBBECE0),
            textPrimary = Color(0xFFE6FAF3),
            textSecondary = Color(0xFFA3C9BC),
            iconTint = Color(0xFFB3F5DC)
        ),
        // Palette 4: Rose Quartz & Sunset Crimson
        MaterialYouSongPalette(
            dominantBg = Color(0xFF221319),
            secondaryBg = Color(0xFF331D26),
            surfaceContainer = Color(0xFF472835),
            primaryAccent = Color(0xFFFFB1C8),
            onPrimaryAccent = Color(0xFF5E112A),
            secondaryAccent = Color(0xFFFFD9E2),
            textPrimary = Color(0xFFFFF0F4),
            textSecondary = Color(0xFFD9B3BF),
            iconTint = Color(0xFFFFC2D4)
        ),
        // Palette 5: Muted Teal & Cyan
        MaterialYouSongPalette(
            dominantBg = Color(0xFF101C20),
            secondaryBg = Color(0xFF192A30),
            surfaceContainer = Color(0xFF233B44),
            primaryAccent = Color(0xFF81D5EE),
            onPrimaryAccent = Color(0xFF003644),
            secondaryAccent = Color(0xFFB0ECFF),
            textPrimary = Color(0xFFE6FAFF),
            textSecondary = Color(0xFFA1C7D4),
            iconTint = Color(0xFFAAE3F5)
        )
    )

    fun getPaletteForSong(song: Song?): MaterialYouSongPalette {
        if (song == null) return presetPalettes[0]
        val hash = (song.id.hashCode() + song.title.hashCode()).coerceAtLeast(0)
        val index = hash % presetPalettes.size
        return presetPalettes[index]
    }
}
