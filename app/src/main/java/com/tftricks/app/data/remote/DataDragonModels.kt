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
