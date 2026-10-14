package com.togglestudios.lumenpop.data

import kotlin.math.max
import kotlin.math.min

enum class LumenColor(
    val colorName: String,
    val closedAsset: String,
    val openedAsset: String,
    val hexColor: Long
) {
    SOLAR("solar", "solar_closed.png", "solar_opened.png", 0xFFFFD700),
    VERDANT("verdant", "verdant_closed.png", "verdant_opened.png", 0xFF00FF7F),
    TERRA("terra", "terra_closed.png", "terra_opened.png", 0xFFFF8C00),
    NOVA("nova", "nova_closed.png", "nova_opened.png", 0xFFFF1493),
    COSMIC("cosmic", "cosmic_closed.png", "cosmic_opened.png", 0xFF9932CC),
    AETHER("aether", "aether_closed.png", "aether_opened.png", 0xFF00FFFF),
    BLAZE("blaze", "blaze_closed.png", "blaze_opened.png", 0xFFFF4500);

    companion object {
        fun fromString(name: String): LumenColor =
            entries.find { it.colorName.equals(name, ignoreCase = true) } ?: SOLAR
    }
}

enum class SpecialType {
    NONE,
    BEAM_H,
    BEAM_V,
    NOVA,
    CROSS
}

enum class BoosterKind(val price: Int, val title: String) {
    SHUFFLE(100, "Shuffle"),
    BOMB(150, "Nova Bomb"),
    BURST(250, "Color Burst")
}

enum class Screen {
    LOADING,
    USERNAME,
    START,
    HOME,
    LEVEL_LOADING,
    GAME,
    SETTINGS
}

data class Tile(
    val id: Long,
    var color: LumenColor,
    var isFusion: Boolean = false,
    var special: SpecialType = SpecialType.NONE
)

data class LevelStats(
    val stars: Int,
    val bestScore: Int
)

data class SavedProgress(
    var highestUnlocked: Int = 1,
    val completed: MutableMap<Int, LevelStats> = mutableMapOf(),
    var coins: Int = 1000,
    var sound: Boolean = true,
    var music: Boolean = true,
    var haptics: Boolean = true,
    var highContrast: Boolean = false,
    var username: String = "Player"
)

data class LevelConfig(
    val level: Int,
    val targetScore: Int,
    val moves: Int,
    val world: String,
    val title: String,
    val lesson: String,
    val bgAsset: String
) {
    companion object {
        fun forLevel(level: Int): LevelConfig {
            val biomeIndex = ((level - 1) % 10) + 1
            val worldName = when {
                level < 11 -> "Starlight Meadows"
                level < 26 -> "Crystal Valley"
                else -> "Twilight Grove"
            }
            val titleName = when {
                level < 11 -> "First Glow $level"
                level < 26 -> "Crystal Drift $level"
                else -> "Moonlit Bloom $level"
            }
            val lessonText = when {
                level <= 2 -> "Make an easy 3-link to wake the meadow"
                level <= 5 -> "Longer chains charge brighter rewards"
                else -> "Find the clearest line through the glow"
            }
            val target = min(12000, 700 + (level - 1) * 180)
            val moveCount = max(20, min(30, 20 + (level - 1) / 2))
            return LevelConfig(
                level = level,
                targetScore = target,
                moves = moveCount,
                world = worldName,
                title = titleName,
                lesson = lessonText,
                bgAsset = "bg_level_$biomeIndex.png"
            )
        }
    }
}
