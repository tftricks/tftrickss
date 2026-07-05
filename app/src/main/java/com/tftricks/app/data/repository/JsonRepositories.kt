package com.tftricks.app.data.repository

import com.tftricks.app.data.source.AssetJsonDataSource
import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.PatchNote
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Trait
import com.tftricks.app.domain.repository.AugmentRepository
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.ItemRepository
import com.tftricks.app.domain.repository.PatchNoteRepository
import com.tftricks.app.domain.repository.TeamCompRepository
import com.tftricks.app.domain.repository.TraitRepository
import kotlinx.serialization.builtins.ListSerializer

class JsonTeamCompRepository(dataSource: AssetJsonDataSource) :
    CachedAssetRepository<TeamComp>(dataSource, "team_comps.json", ListSerializer(TeamComp.serializer())),
    TeamCompRepository {

    override suspend fun getTeamComps(): List<TeamComp> = getAll()
    override suspend fun getTeamComp(id: String): TeamComp? = getAll().find { it.id == id }
}

class JsonChampionRepository(dataSource: AssetJsonDataSource) :
    CachedAssetRepository<Champion>(dataSource, "champions.json", ListSerializer(Champion.serializer())),
    ChampionRepository {

    override suspend fun getChampions(): List<Champion> = getAll()
    override suspend fun getChampion(id: String): Champion? = getAll().find { it.id == id }
}

class JsonItemRepository(dataSource: AssetJsonDataSource) :
    CachedAssetRepository<Item>(dataSource, "items.json", ListSerializer(Item.serializer())),
    ItemRepository {

    override suspend fun getItems(): List<Item> = getAll()
    override suspend fun getItem(id: String): Item? = getAll().find { it.id == id }
}

class JsonTraitRepository(dataSource: AssetJsonDataSource) :
    CachedAssetRepository<Trait>(dataSource, "traits.json", ListSerializer(Trait.serializer())),
    TraitRepository {

    override suspend fun getTraits(): List<Trait> = getAll()
    override suspend fun getTrait(id: String): Trait? = getAll().find { it.id == id }
}

class JsonAugmentRepository(dataSource: AssetJsonDataSource) :
    CachedAssetRepository<Augment>(dataSource, "augments.json", ListSerializer(Augment.serializer())),
    AugmentRepository {

    override suspend fun getAugments(): List<Augment> = getAll()
    override suspend fun getAugment(id: String): Augment? = getAll().find { it.id == id }
}

class JsonPatchNoteRepository(dataSource: AssetJsonDataSource) :
    CachedAssetRepository<PatchNote>(dataSource, "patch_notes.json", ListSerializer(PatchNote.serializer())),
    PatchNoteRepository {

    override suspend fun getPatchNotes(): List<PatchNote> = getAll()
}
