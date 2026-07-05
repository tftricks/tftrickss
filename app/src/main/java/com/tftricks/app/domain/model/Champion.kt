package com.tftricks.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A playable champion.
 */
@Serializable
data class Champion(
    val id: String,
    val name: String,
    /** Shop cost, 1..5. */
    val cost: Int,
    val traits: List<String>,
    val ability: Ability,
    val recommendedItems: List<String>,
    /** e.g. "AD Carry", "AP Carry", "Tank", "Utility". */
    val role: String,
    val positioningNotes: String,
    /** Ids of comps ([TeamComp.id]) this champion shines in. */
    val bestComps: List<String> = emptyList()
)

@Serializable
data class Ability(
    val name: String,
    val description: String,
    val manaStart: Int = 0,
    val manaMax: Int
)
