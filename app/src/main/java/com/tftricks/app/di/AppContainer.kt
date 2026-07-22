package com.tftricks.app.di

import android.content.Context
import com.tftricks.app.ads.AdsManager
import com.tftricks.app.data.local.userDataStore
import com.tftricks.app.data.repository.DataStoreFavoritesRepository
import com.tftricks.app.data.repository.DataStoreOverlayPrefsRepository
import com.tftricks.app.data.repository.DataStoreSavedTeamsRepository
import com.tftricks.app.data.repository.JsonAugmentRepository
import com.tftricks.app.data.repository.JsonPatchNoteRepository
import com.tftricks.app.data.repository.JsonTeamCompRepository
import com.tftricks.app.data.remote.CommunityDragonRepository
import com.tftricks.app.data.source.AssetJsonDataSource
import com.tftricks.app.domain.repository.AugmentRepository
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.FavoritesRepository
import com.tftricks.app.domain.repository.ItemRepository
import com.tftricks.app.domain.repository.OverlayPrefsRepository
import com.tftricks.app.domain.repository.PatchNoteRepository
import com.tftricks.app.domain.repository.SavedTeamsRepository
import com.tftricks.app.domain.repository.TeamCompRepository
import com.tftricks.app.domain.repository.TraitRepository
import kotlinx.serialization.json.Json

/**
 * Hand-rolled dependency container. Small enough that a DI framework
 * would be overkill for this phase.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val dataSource = AssetJsonDataSource(appContext, json)

    // Team comps (and their curated guides) stay bundled locally; champions/items/traits
    // are fetched live from CommunityDragon by the same repository instance below.
    val teamCompRepository: TeamCompRepository = JsonTeamCompRepository(dataSource)
    val augmentRepository: AugmentRepository = JsonAugmentRepository(dataSource)
    val patchNoteRepository: PatchNoteRepository = JsonPatchNoteRepository(dataSource)

    val communityDragonRepository: CommunityDragonRepository =
        CommunityDragonRepository(appContext, teamCompRepository, json)
    val championRepository: ChampionRepository = communityDragonRepository
    val itemRepository: ItemRepository = communityDragonRepository
    val traitRepository: TraitRepository = communityDragonRepository

    val favoritesRepository: FavoritesRepository =
        DataStoreFavoritesRepository(appContext.userDataStore)
    val savedTeamsRepository: SavedTeamsRepository =
        DataStoreSavedTeamsRepository(appContext.userDataStore, json)
    val overlayPrefsRepository: OverlayPrefsRepository =
        DataStoreOverlayPrefsRepository(appContext.userDataStore)

    val adsManager: AdsManager = AdsManager(appContext)
}
