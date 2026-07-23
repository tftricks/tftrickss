package com.tftricks.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Leaf shapes decoded out of https://raw.communitydragon.org/latest/cdragon/tft/en_us.json.
 *
 * The response is walked manually as raw [kotlinx.serialization.json.JsonObject]/[kotlinx.serialization.json.JsonArray]
 * (see [CommunityDragonRepository]) rather than decoded as one big object graph, so a single
 * malformed champion/item/trait entry can be skipped and logged instead of failing the whole
 * fetch. Every field below is nullable/defaulted on purpose — this DTO layer was written from
 * CommunityDragon's publicly documented schema without a live fetch to verify against (this
 * environment's network policy blocks raw.communitydragon.org), and in practice CommunityDragon
 * sends explicit `null` (not just omission) for fields an entry has no data for, e.g. an item
 * with no recipe.
 */
@Serializable
data class CDragonChampion(
    val apiName: String? = null,
    val name: String? = null,
    val cost: Int? = null,
    val traits: List<String>? = null,
    val ability: CDragonAbility? = null,
    /** Candidate icon asset paths; whichever is present first gets used. */
    val squareIcon: String? = null,
    val icon: String? = null,
    val tileIcon: String? = null
)

@Serializable
data class CDragonAbility(
    val name: String? = null,
    val desc: String? = null
)

@Serializable
data class CDragonTrait(
    val apiName: String? = null,
    val name: String? = null,
    val desc: String? = null,
    val icon: String? = null,
    val effects: List<CDragonTraitEffect>? = null
)

@Serializable
data class CDragonTraitEffect(
    val minUnits: Int? = null,
    val maxUnits: Int? = null
)

@Serializable
data class CDragonItem(
    val apiName: String? = null,
    val name: String? = null,
    val desc: String? = null,
    val icon: String? = null,
    /** Component item apiNames this is built from; null/absent for base components. */
    val from: List<String>? = null,
    val composition: List<String>? = null
)

/**
 * A champion entry from tftchampions-teamplanner.json — verified against a live fetch
 * (unlike the DTOs above), via a throwaway CI diagnostic job, since this dev
 * environment's network policy also blocks that endpoint. Real field names confirmed:
 * `character_id` (e.g. "TFT17_Briar") and `display_name` (e.g. "Bel'Veth"). The response
 * also carries a `team_planner_code` int per champion that looks like Riot's own
 * persistent id (non-sequential per current set, e.g. Briar=14 but Tristana=735) — this
 * DTO intentionally does not decode it, since TFTricks builds its own alphabetical-index
 * codes per the product spec rather than relying on that field.
 */
@Serializable
data class CDragonTeamPlannerChampion(
    @SerialName("character_id") val characterId: String? = null,
    @SerialName("display_name") val displayName: String? = null
)

/** Observable outcome of the last CommunityDragon fetch attempt. */
sealed interface CommunityDragonStatus {
    data object Loading : CommunityDragonStatus

    data class Success(
        val setNumber: Int?,
        val championCount: Int,
        val itemCount: Int,
        val traitCount: Int
    ) : CommunityDragonStatus

    data class Error(val message: String) : CommunityDragonStatus
}

/** Human-readable one-liner for debug UI (banners, settings). */
fun CommunityDragonStatus.debugLabel(): String = when (this) {
    is CommunityDragonStatus.Loading -> "Loading..."
    is CommunityDragonStatus.Success ->
        "Loaded: Set ${setNumber ?: "?"}, $championCount champions, $itemCount items, $traitCount traits"
    is CommunityDragonStatus.Error -> "Error: $message"
}
