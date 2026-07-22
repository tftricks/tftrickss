package com.tftricks.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A playable champion. Sourced live from CommunityDragon — id/name/cost/traits/ability
 * are real game data; there is no local curated role/positioning/recommended-items
 * content since that doesn't exist in CommunityDragon's feed. "Best comps" for a
 * champion are computed on the fly from the bundled [TeamComp] data instead of stored
 * here (see ChampionDetailViewModel).
 */
@Serializable
data class Champion(
    val id: String,
    val name: String,
    /** Shop cost, 1..5. */
    val cost: Int,
    val traits: List<String>,
    val ability: Ability
)

@Serializable
data class Ability(
    val name: String,
    val description: String,
    val manaStart: Int = 0,
    val manaMax: Int = 0
)
