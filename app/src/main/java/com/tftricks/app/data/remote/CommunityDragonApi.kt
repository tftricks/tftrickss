package com.tftricks.app.data.remote

import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET

interface CommunityDragonApi {
    /** "latest" always points at the current live patch — never a pinned version.
     *  Decoded as a raw [JsonObject] rather than a typed graph so individual malformed
     *  champion/item/trait entries can be skipped instead of failing the whole parse. */
    @GET("latest/cdragon/tft/en_us.json")
    suspend fun getTftData(): JsonObject
}
