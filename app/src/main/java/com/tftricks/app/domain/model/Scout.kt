package com.tftricks.app.domain.model

/** A suggested comp to pivot into, with the overlap math that produced the ranking. */
data class PivotRecommendation(
    val comp: TeamComp,
    val overlapScore: Int,
    val reason: String
)

/**
 * Ranks our S-tier comps by how little their final-board roster overlaps with whatever
 * the scouted lobby is majority-playing, so a low score means fewer item/unit fights.
 *
 * The "majority" opponent comp is whichever assigned [assignments] id (or ids, on a tie)
 * has the highest count; unassigned slots (null = "Unknown") don't count toward it. Each
 * of our S-tier comps then scores `Σ sharedUnits(ourComp, majorityComp) * opponentCount`
 * across every majority comp (usually just one) — lower is better. Returns the top 3,
 * or an empty list once nobody's been scouted yet.
 */
fun recommendPivots(allComps: List<TeamComp>, assignments: List<String?>): List<PivotRecommendation> {
    val compsById = allComps.associateBy { it.id }
    val counts = assignments.filterNotNull().groupingBy { it }.eachCount()
    if (counts.isEmpty()) return emptyList()

    val maxCount = counts.values.max()
    val majorityComps = counts.filterValues { it == maxCount }.keys.mapNotNull { compsById[it] }
    if (majorityComps.isEmpty()) return emptyList()

    val majorityNames = majorityComps.joinToString(" & ") { it.name }
    val sTierComps = allComps.filter { it.tier == Tier.S }

    fun sharedUnitCount(a: TeamComp, b: TeamComp): Int {
        val aUnits = a.finalBoard.map { it.champion }.toSet()
        val bUnits = b.finalBoard.map { it.champion }.toSet()
        return aUnits.intersect(bUnits).size
    }

    return sTierComps
        .map { ourComp ->
            val overlap = majorityComps.sumOf { opponent ->
                sharedUnitCount(ourComp, opponent) * (counts[opponent.id] ?: 0)
            }
            val highlight = ourComp.strengths.firstOrNull()
            val overlapWord = if (overlap == 0) "No overlap" else "Low overlap"
            val reason = buildString {
                append("$overlapWord with $majorityNames")
                if (!highlight.isNullOrBlank()) append(", $highlight")
            }
            PivotRecommendation(ourComp, overlap, reason)
        }
        .sortedWith(
            compareBy<PivotRecommendation> { it.overlapScore }
                .thenByDescending { it.comp.top4Rate ?: Double.NEGATIVE_INFINITY }
        )
        .take(3)
}
