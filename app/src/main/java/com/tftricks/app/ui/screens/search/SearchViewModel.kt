package com.tftricks.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Trait
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchResults(
    val query: String,
    val comps: List<TeamComp> = emptyList(),
    val champions: List<Champion> = emptyList(),
    val traits: List<Trait> = emptyList(),
    val items: List<Item> = emptyList(),
    val augments: List<Augment> = emptyList()
) {
    val isEmpty: Boolean
        get() = comps.isEmpty() && champions.isEmpty() && traits.isEmpty() &&
            items.isEmpty() && augments.isEmpty()
}

private data class SearchIndex(
    val comps: List<TeamComp>,
    val champions: List<Champion>,
    val traits: List<Trait>,
    val items: List<Item>,
    val augments: List<Augment>
)

class SearchViewModel(private val container: AppContainer) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val index = MutableStateFlow<SearchIndex?>(null)

    val results: StateFlow<SearchResults> = combine(index, _query) { data, raw ->
        val q = raw.trim()
        if (data == null || q.length < 2) {
            SearchResults(query = q)
        } else {
            SearchResults(
                query = q,
                comps = data.comps.filter {
                    it.name.contains(q, true) || it.tags.any { tag -> tag.contains(q, true) }
                },
                champions = data.champions.filter {
                    it.name.contains(q, true) || it.traits.any { t -> t.contains(q, true) }
                },
                traits = data.traits.filter { it.name.contains(q, true) },
                items = data.items.filter { it.name.contains(q, true) },
                augments = data.augments.filter { it.name.contains(q, true) }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResults(query = ""))

    init {
        viewModelScope.launch {
            runCatching {
                SearchIndex(
                    comps = container.teamCompRepository.getTeamComps(),
                    champions = container.championRepository.getChampions(),
                    traits = container.traitRepository.getTraits(),
                    items = container.itemRepository.getItems(),
                    augments = container.augmentRepository.getAugments()
                )
            }.onSuccess { index.value = it }
            // On failure the index stays null and search shows no results;
            // asset data is bundled, so this effectively never happens.
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }
}
