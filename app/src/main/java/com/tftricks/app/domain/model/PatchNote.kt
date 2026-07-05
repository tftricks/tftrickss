package com.tftricks.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A summarized game patch.
 */
@Serializable
data class PatchNote(
    val id: String,
    val version: String,
    /** ISO-8601 date, e.g. "2026-07-02". */
    val date: String,
    val summary: String,
    val changes: List<PatchChange>
)

@Serializable
data class PatchChange(
    /** What was changed, e.g. a champion, trait, item or system name. */
    val target: String,
    val description: String
)
