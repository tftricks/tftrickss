package com.tftricks.app.data.remote

import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET

interface CommunityDragonApi {
    /** "latest" always points at the current live patch — never a pinned version.
     *  Decoded as a raw [JsonObject] rather than a typed graph so individual malformed
     *  champion/item/trait entries can be skipped instead of failing the whole parse. */
    @GET("latest/cdragon/tft/en_us.json")
    suspend fun getTftData(): JsonObject

    /** Keyed by set id (e.g. "TFTSet17"); each value is that set's champion roster,
     *  used to build the TFT Team Planner paste-code champion dictionary. */
    @GET("latest/plugins/rcp-be-lol-game-data/global/default/v1/tftchampions-teamplanner.json")
    suspend fun getTeamPlannerData(): JsonObject
}
