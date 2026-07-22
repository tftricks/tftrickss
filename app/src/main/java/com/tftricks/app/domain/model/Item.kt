package com.tftricks.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A craftable (or component) item. Sourced live from CommunityDragon; [components] comes
 * from real build-recipe data, but there is no curated AD/AP/tank categorization or
 * "best users"/"alternatives" content in that feed — best users are computed on the fly
 * from the bundled [TeamComp] itemization instead of stored here (see ItemDetailViewModel).
 */
@Serializable
data class Item(
    val id: String,
    val name: String,
    /** Component item names this is built from; empty for base components. */
    val components: List<String> = emptyList(),
    val effect: String,
    val category: ItemCategory
)

@Serializable
enum class ItemCategory {
    @SerialName("AD") AD,
    @SerialName("AP") AP,
    @SerialName("tank") TANK,
    @SerialName("utility") UTILITY,
    @SerialName("attackSpeed") ATTACK_SPEED,
    @SerialName("mana") MANA,
    /** CommunityDragon has no curated categorization; live items default here. */
    @SerialName("other") OTHER
}
