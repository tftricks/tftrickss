package com.tftricks.app.ui.navigation

/**
 * Parameterized routes for detail screens (not part of the [Destination] enum,
 * which only covers top-level screens).
 */
object DetailRoutes {
    const val COMP_ARG = "compId"
    const val CHAMPION_ARG = "championId"
    const val ITEM_ARG = "itemId"
    const val PATCH_ARG = "patchId"

    const val COMP_PATTERN = "comp_detail/{$COMP_ARG}"
    const val CHAMPION_PATTERN = "champion_detail/{$CHAMPION_ARG}"
    const val ITEM_PATTERN = "item_detail/{$ITEM_ARG}"
    const val PATCH_PATTERN = "patch_detail/{$PATCH_ARG}"

    fun comp(id: String) = "comp_detail/$id"
    fun champion(id: String) = "champion_detail/$id"
    fun item(id: String) = "item_detail/$id"
    fun patch(id: String) = "patch_detail/$id"

    /** Top-bar title for a detail route pattern, or null if [route] isn't a detail route. */
    fun titleFor(route: String?): String? = when (route) {
        COMP_PATTERN -> "Comp Details"
        CHAMPION_PATTERN -> "Champion"
        ITEM_PATTERN -> "Item"
        PATCH_PATTERN -> "Patch Notes"
        else -> null
    }
}
