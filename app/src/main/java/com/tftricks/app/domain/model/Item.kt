package com.tftricks.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A craftable (or component) item.
 */
@Serializable
data class Item(
    val id: String,
    val name: String,
    /** Component item names this is built from; empty for base components. */
    val components: List<String> = emptyList(),
    val effect: String,
    /** Champion names that use this item best. */
    val bestUsers: List<String> = emptyList(),
    /** Item names that work as substitutes. */
    val goodAlternatives: List<String> = emptyList(),
    val category: ItemCategory
)

@Serializable
enum class ItemCategory {
    @SerialName("AD") AD,
    @SerialName("AP") AP,
    @SerialName("tank") TANK,
    @SerialName("utility") UTILITY,
    @SerialName("attackSpeed") ATTACK_SPEED,
    @SerialName("mana") MANA
}
