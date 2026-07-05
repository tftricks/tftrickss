package com.tftricks.app.domain.repository

import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.PatchNote
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Trait

interface TeamCompRepository {
    suspend fun getTeamComps(): List<TeamComp>
    suspend fun getTeamComp(id: String): TeamComp?
}

interface ChampionRepository {
    suspend fun getChampions(): List<Champion>
    suspend fun getChampion(id: String): Champion?
}

interface ItemRepository {
    suspend fun getItems(): List<Item>
    suspend fun getItem(id: String): Item?
}

interface TraitRepository {
    suspend fun getTraits(): List<Trait>
    suspend fun getTrait(id: String): Trait?
}

interface AugmentRepository {
    suspend fun getAugments(): List<Augment>
    suspend fun getAugment(id: String): Augment?
}

interface PatchNoteRepository {
    suspend fun getPatchNotes(): List<PatchNote>
}
