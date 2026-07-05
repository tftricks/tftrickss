package com.tftricks.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A user-built team from the Team Builder, persisted locally.
 */
@Serializable
data class SavedTeam(
    val id: String,
    val name: String,
    val units: List<SavedUnit>,
    val createdAt: Long
)

@Serializable
data class SavedUnit(
    /** Hex index on the 4x7 board, 0..27 row-major from the back row. */
    val position: Int,
    val championId: String
)
