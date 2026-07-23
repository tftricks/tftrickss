package com.tftricks.app.data.repository

import com.tftricks.app.data.source.AssetJsonDataSource
import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.model.PatchNote
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.repository.AugmentRepository
import com.tftricks.app.domain.repository.PatchNoteRepository
import com.tftricks.app.domain.repository.TeamCompRepository
import kotlinx.serialization.builtins.ListSerializer

class JsonTeamCompRepository(dataSource: AssetJsonDataSource) :
    CachedAssetRepository<TeamComp>(dataSource, "team_comps.json", ListSerializer(TeamComp.serializer())),
    TeamCompRepository {

    override suspend fun getTeamComps(): List<TeamComp> = getAll()
    override suspend fun getTeamComp(id: String): TeamComp? = getAll().find { it.id == id }
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
