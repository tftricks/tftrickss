package com.tftricks.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A recommended team composition with full play-guide information.
 */
@Serializable
data class TeamComp(
    val id: String,
    val name: String,
    val tier: Tier,
    val difficulty: Difficulty,
    val patchVersion: String,
    val finalBoard: List<BoardUnit>,
    val earlyGameBoard: List<BoardUnit> = emptyList(),
    val midGameBoard: List<BoardUnit> = emptyList(),
    val carryChampions: List<String>,
    val tankChampions: List<String>,
    val bestItems: List<String>,
    val alternativeItems: List<String> = emptyList(),
    val traitsActive: List<ActiveTrait>,
    val positioningNotes: String,
    val levelingGuide: String,
    val economyGuide: String,
    val rollTiming: String,
    val whenToPlay: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val tags: List<String> = emptyList()
)

/**
 * A single unit on a board plan.
 *
 * @param position hex index on the 4x7 board (0..27, row-major from the back row),
 *                 or null when positioning is flexible.
 * @param starTarget the star level the comp aims for on this unit (2 by default, 3 for reroll carries).
 */
@Serializable
data class BoardUnit(
    val champion: String,
    val position: Int? = null,
    val items: List<String> = emptyList(),
    val starTarget: Int = 2
)

/** A trait the comp activates, with the number of contributing units. */
@Serializable
data class ActiveTrait(
    val trait: String,
    val count: Int
)

/** Comp strength tier. */
@Serializable
enum class Tier {
    S, A, B, C
}

/** How hard the comp is to pilot. */
@Serializable
enum class Difficulty {
    @SerialName("Easy") EASY,
    @SerialName("Medium") MEDIUM,
    @SerialName("Hard") HARD
}

/** Well-known values used in [TeamComp.tags]. Tags stay plain strings so data can add new ones freely. */
object CompTags {
    const val REROLL = "reroll"
    const val FAST8 = "fast8"
    const val VERTICAL = "vertical"
    const val FLEX = "flex"
    const val AD = "AD"
    const val AP = "AP"
    const val BEGINNER = "beginner"
    const val ADVANCED = "advanced"
}
