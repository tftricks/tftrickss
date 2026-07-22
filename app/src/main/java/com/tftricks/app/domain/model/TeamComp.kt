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
    val tags: List<String> = emptyList(),
    /** Level to slow-roll at for a reroll comp, or null for fast-leveling comps. */
    val slowRollLevel: Int? = null,
    /** Low-cost champions from this comp's roster worth playing before the comp comes online. */
    val earlyGameChampions: List<String> = emptyList(),
    /** Suggested roster swaps keyed by level (as a string, e.g. "7"). */
    val levelAlternatives: Map<String, List<String>> = emptyMap(),
    /** Champion names in priority order for god-offering carousel picks. */
    val godOfferingPriority: List<String> = emptyList(),
    /** Alternative setups of this same comp (different roster/items, same core idea). */
    val variants: List<CompVariant> = emptyList(),
    /** Staged leveling breakdown, e.g. "Lv6 @ 3-2 (slow-roll here)", in play order. */
    val levelingStages: List<String> = emptyList(),
    /** Freeform early-game notes: opener, itemization priority, trait counts. */
    val earlyGameNotes: List<String> = emptyList(),
    /** Augment picks by tier. Null when this comp has no curated augment data yet. */
    val augmentGuide: AugmentGuide? = null,
    /** Component items to prioritize off carousels, in priority order. */
    val carouselItemPriority: List<String> = emptyList(),
    /** Misc guide notes: roster alternatives, execution tips. */
    val tips: List<String> = emptyList()
)

/** Augment recommendations for a [TeamComp], grouped by the round they're offered. */
@Serializable
data class AugmentGuide(
    val tier1: List<String> = emptyList(),
    val tier2: List<String> = emptyList(),
    val tier3: List<String> = emptyList()
)

/** A base component item required to build a comp's full itemization, with how many copies. */
data class ComponentRequirement(val componentName: String, val count: Int)

/**
 * Tallies the base components needed to build every completed item on [TeamComp.finalBoard],
 * computed from live [Item.components] recipe data rather than curated per-comp.
 */
fun TeamComp.requiredComponents(itemsByName: Map<String, Item>): List<ComponentRequirement> {
    val counts = LinkedHashMap<String, Int>()
    finalBoard.flatMap { it.items }.forEach { itemName ->
        itemsByName[itemName]?.components?.forEach { component ->
            counts[component] = (counts[component] ?: 0) + 1
        }
    }
    return counts.entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .map { ComponentRequirement(it.key, it.value) }
}

/**
 * An alternative setup of a [TeamComp] — a different roster/itemization on the same
 * core idea, rather than a fully separate comp entry.
 */
@Serializable
data class CompVariant(
    val name: String,
    val tier: Tier,
    val finalBoard: List<BoardUnit>,
    val carryChampions: List<String> = emptyList(),
    val tankChampions: List<String> = emptyList(),
    val notes: String = "",
    /** Recorded match stats as free text, e.g. "Avg 4.04 • Win 14.8% • Top-4 58.5%". */
    val statsNote: String = ""
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
