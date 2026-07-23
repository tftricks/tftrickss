package com.tftricks.app.domain.model

/**
 * The current TFT set's champion code dictionary for building Team Planner paste codes.
 * Built once from a live fetch and cached — see [com.tftricks.app.domain.repository.TeamPlannerRepository].
 */
data class TeamPlannerEncoding(
    /** e.g. "TFTSet17". */
    val setId: String,
    /** 2-digit hex code keyed by normalized (see [normalizeChampionName]) display name. */
    val codesByNormalizedName: Map<String, String>
)

/** Lowercase, strip spaces/apostrophes/periods — matches e.g. "Bel'Veth" to "Belveth". */
fun normalizeChampionName(name: String): String =
    name.lowercase()
        .replace(" ", "")
        .replace("'", "")
        .replace("’", "")
        .replace(".", "")

/**
 * Encodes this comp's final board as a TFT Team Planner paste code: a "01" format
 * marker, then 10 two-digit hex champion slot codes in [TeamComp.finalBoard] order
 * ("00" for an empty slot beyond the roster, or one whose champion has no known code),
 * then the raw set id — e.g. "01010203000000000000TFTSet17" for a 3-unit comp.
 *
 * A champion with no match in [encoding] doesn't fail the whole code — that slot just
 * becomes "00" and [onUnmatched] is called with the unmatched name so the caller can log it.
 */
fun TeamComp.toTeamPlannerCode(
    encoding: TeamPlannerEncoding,
    onUnmatched: (String) -> Unit = {}
): String {
    val slots = (0 until 10).joinToString("") { index ->
        val unit = finalBoard.getOrNull(index)
        when {
            unit == null -> "00"
            else -> encoding.codesByNormalizedName[normalizeChampionName(unit.champion)]
                ?: "00".also { onUnmatched(unit.champion) }
        }
    }
    return "01" + slots + encoding.setId
}
