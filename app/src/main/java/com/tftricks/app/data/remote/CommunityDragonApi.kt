package com.tftricks.app.data.remote

import retrofit2.http.GET

interface CommunityDragonApi {
    /** "latest" always points at the current live patch — never a pinned version. */
    @GET("latest/cdragon/tft/en_us.json")
    suspend fun getTftData(): CDragonRoot
}
