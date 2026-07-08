package com.tftricks.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class DDragonImage(val full: String)

@Serializable
data class DDragonChampionEntry(
    val id: String,
    val name: String,
    val image: DDragonImage
)

@Serializable
data class DDragonChampionResponse(
    val data: Map<String, DDragonChampionEntry> = emptyMap()
)

@Serializable
data class DDragonItemEntry(
    val id: String,
    val name: String,
    val image: DDragonImage
)

@Serializable
data class DDragonItemResponse(
    val data: Map<String, DDragonItemEntry> = emptyMap()
)

/** Observable outcome of the last Data Dragon load attempt (cache read or network refresh). */
sealed interface DataDragonStatus {
    data object Loading : DataDragonStatus

    data class Success(
        val setNumber: Int?,
        val championCount: Int,
        val itemCount: Int
    ) : DataDragonStatus

    data class Error(val message: String) : DataDragonStatus
}

/** Human-readable one-liner for debug UI (banners, settings). */
fun DataDragonStatus.debugLabel(): String = when (this) {
    is DataDragonStatus.Loading -> "Loading..."
    is DataDragonStatus.Success ->
        "Loaded: Set ${setNumber ?: "?"}, $championCount champions, $itemCount items"
    is DataDragonStatus.Error -> "Error: $message"
}
