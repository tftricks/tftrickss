package com.tftricks.app.data.remote

import kotlinx.serialization.Serializable

/**
 * Raw shape of https://raw.communitydragon.org/latest/cdragon/tft/en_us.json.
 *
 * Every field here is nullable/defaulted on purpose: this DTO layer was written from
 * CommunityDragon's publicly documented schema without a live fetch to verify against
 * (this environment's network policy blocks raw.communitydragon.org), so a wrong field
 * name should degrade to a missing value instead of crashing the whole parse.
 */
@Serializable
data class CDragonRoot(
    val setData: List<CDragonSet> = emptyList(),
    val items: List<CDragonItem> = emptyList()
)

@Serializable
data class CDragonSet(
    /** Set number, e.g. 17. This is how the current set is detected — never hardcoded. */
    val number: Int? = null,
    val name: String? = null,
    val champions: List<CDragonChampion> = emptyList(),
    val traits: List<CDragonTrait> = emptyList()
)

@Serializable
data class CDragonChampion(
    val apiName: String? = null,
    val name: String? = null,
    val cost: Int? = null,
    val traits: List<String> = emptyList(),
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
    val effects: List<CDragonTraitEffect> = emptyList()
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
    /** Component item apiNames this is built from; field name is unconfirmed, so both
     *  common candidates are read and whichever is non-empty is used. */
    val from: List<String> = emptyList(),
    val composition: List<String> = emptyList()
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
