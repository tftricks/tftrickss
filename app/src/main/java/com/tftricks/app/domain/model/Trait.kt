package com.tftricks.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A trait (origin/class) shared by champions.
 */
@Serializable
data class Trait(
    val id: String,
    val name: String,
    val description: String,
    val breakpoints: List<TraitBreakpoint>,
    /** Champion names that carry this trait. */
    val champions: List<String>
)

@Serializable
data class TraitBreakpoint(
    /** Number of unique units needed to activate this level. */
    val count: Int,
    val effect: String
)
