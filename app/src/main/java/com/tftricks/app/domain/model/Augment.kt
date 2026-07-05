package com.tftricks.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A selectable augment.
 */
@Serializable
data class Augment(
    val id: String,
    val name: String,
    val tier: AugmentTier,
    val effect: String,
    /** Ids of comps ([TeamComp.id]) this augment pairs well with. */
    val bestComps: List<String> = emptyList(),
    val notes: String = "",
    /** Pick priority from 1 (skip) to 5 (always take). */
    val priorityRating: Int
)

@Serializable
enum class AugmentTier {
    @SerialName("Silver") SILVER,
    @SerialName("Gold") GOLD,
    @SerialName("Prismatic") PRISMATIC
}
