package com.tftricks.app.di

import android.content.Context
import com.tftricks.app.data.local.userDataStore
import com.tftricks.app.data.repository.DataStoreFavoritesRepository
import com.tftricks.app.data.repository.DataStoreSavedTeamsRepository
import com.tftricks.app.data.repository.JsonAugmentRepository
import com.tftricks.app.data.repository.JsonChampionRepository
import com.tftricks.app.data.repository.JsonItemRepository
import com.tftricks.app.data.repository.JsonPatchNoteRepository
import com.tftricks.app.data.repository.JsonTeamCompRepository
import com.tftricks.app.data.repository.JsonTraitRepository
import com.tftricks.app.data.source.AssetJsonDataSource
import com.tftricks.app.domain.repository.AugmentRepository
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.FavoritesRepository
import com.tftricks.app.domain.repository.ItemRepository
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

    val teamCompRepository: TeamCompRepository = JsonTeamCompRepository(dataSource)
    val championRepository: ChampionRepository = JsonChampionRepository(dataSource)
    val itemRepository: ItemRepository = JsonItemRepository(dataSource)
    val traitRepository: TraitRepository = JsonTraitRepository(dataSource)
    val augmentRepository: AugmentRepository = JsonAugmentRepository(dataSource)
    val patchNoteRepository: PatchNoteRepository = JsonPatchNoteRepository(dataSource)

    val favoritesRepository: FavoritesRepository =
        DataStoreFavoritesRepository(appContext.userDataStore)
    val savedTeamsRepository: SavedTeamsRepository =
        DataStoreSavedTeamsRepository(appContext.userDataStore, json)
}
