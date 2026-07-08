package com.tftricks.app.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.ItemRepository
import com.tftricks.app.domain.repository.TeamCompRepository
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Fetches champion and item icon metadata from Riot's public Data Dragon CDN and maps
 * it onto every local champion name we know about — both the champions.json roster and
 * every champion name referenced by imported team comps, since those can include
 * champions outside that roster.
 *
 * Data Dragon's champion/item files can mix in stale entries from older TFT sets (and
 * `/api/versions.json`'s latest patch doesn't always have the newest set fully live), so
 * entries are filtered down to whichever set-number prefix is highest in the fetched
 * champion data before any matching happens.
 *
 * Offline-first: whatever was cached from the last successful fetch loads immediately,
 * then a background refresh runs if the network is reachable and overwrites the cache.
 */
class DataDragonRepository(
    private val context: Context,
    private val championRepository: ChampionRepository,
    private val itemRepository: ItemRepository,
    private val teamCompRepository: TeamCompRepository,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val cacheDir: File by lazy { File(context.filesDir, "ddragon").apply { mkdirs() } }

    /** Champion icon URLs keyed by exact local champion display name (e.g. "Cho'Gath"). */
    private val _championIconUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val championIconUrls: StateFlow<Map<String, String>> = _championIconUrls.asStateFlow()

    /** Item icon URLs keyed by our local item id. */
    private val _itemIconUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val itemIconUrls: StateFlow<Map<String, String>> = _itemIconUrls.asStateFlow()

    /** Outcome of the last load attempt (cache read or network refresh) — for debug UI. */
    private val _status = MutableStateFlow<DataDragonStatus>(DataDragonStatus.Loading)
    val status: StateFlow<DataDragonStatus> = _status.asStateFlow()

    /** Loads the cached icon maps (if any), then refreshes from the network if online. */
    fun initialize() {
        scope.launch {
            loadFromDiskCache()
            if (isOnline()) {
                refreshFromNetwork()
            } else if (_status.value is DataDragonStatus.Loading) {
                _status.value = DataDragonStatus.Error("No internet connection and no cached data")
            }
        }
    }

    /** Manual retry for debug UI: wipes the disk cache and forces a fresh network fetch. */
    fun forceRefresh() {
        scope.launch {
            _status.value = DataDragonStatus.Loading
            clearDiskCache()
            refreshFromNetwork()
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun loadFromDiskCache() {
        val versionFile = File(cacheDir, VERSION_FILE)
        val championFile = File(cacheDir, CHAMPION_FILE)
        val itemFile = File(cacheDir, ITEM_FILE)
        if (!versionFile.exists() || !championFile.exists() || !itemFile.exists()) return

        runCatching {
            val version = versionFile.readText().trim()
            val championJson = championFile.readText()
            val itemJson = itemFile.readText()
            applyResponses(version, championJson, itemJson)
        }.onSuccess { result ->
            _status.value = DataDragonStatus.Success(result.setNumber, result.championCount, result.itemCount)
        }.onFailure {
            // Leave status as Loading — a network refresh right after this may still succeed.
            Log.w(TAG, "Failed to load cached Data Dragon data", it)
        }
    }

    private suspend fun refreshFromNetwork() {
        runCatching {
            val version = fetchLatestVersion()
            val championJson = fetchText(championUrl(version))
            val itemJson = fetchText(itemUrl(version))
            val result = applyResponses(version, championJson, itemJson)
            writeToDiskCache(version, championJson, itemJson)
            result
        }.onSuccess { result ->
            _status.value = DataDragonStatus.Success(result.setNumber, result.championCount, result.itemCount)
        }.onFailure { e ->
            Log.w(TAG, "Data Dragon refresh failed, keeping cached/previous icon data", e)
            _status.value = DataDragonStatus.Error(e.message ?: e.javaClass.simpleName ?: "Unknown error")
        }
    }

    private fun clearDiskCache() {
        File(cacheDir, VERSION_FILE).delete()
        File(cacheDir, CHAMPION_FILE).delete()
        File(cacheDir, ITEM_FILE).delete()
    }

    /** Result of a successful parse: what we detected, for the [status] flow. */
    private data class ApplyResult(val setNumber: Int?, val championCount: Int, val itemCount: Int)

    private suspend fun applyResponses(version: String, championJson: String, itemJson: String): ApplyResult {
        val championResponse = json.decodeFromString(DDragonChampionResponse.serializer(), championJson)
        val itemResponse = json.decodeFromString(DDragonItemResponse.serializer(), itemJson)

        // versions.json's latest patch doesn't always have the newest set's TFT data live,
        // and the champion/item files can carry stale entries from older sets — keep only
        // whichever set-number prefix is highest among the champion ids we actually got.
        val currentSet = detectCurrentSet(championResponse.data.values.map { it.id })
        if (currentSet != null) {
            Log.i(TAG, "Detected current TFT set: $currentSet (Data Dragon patch $version)")
        } else {
            Log.w(TAG, "Could not detect a TFT set number from champion ids (patch $version); using all entries")
        }

        val currentSetChampions = championResponse.data.values.filter {
            currentSet == null || setNumber(it.id) == currentSet
        }
        // Items without a set number are set-agnostic (basic components, universal completed
        // items); only entries carrying a *different* set's number get dropped.
        val currentSetItems = itemResponse.data.values.filter {
            val n = setNumber(it.id)
            currentSet == null || n == null || n == currentSet
        }

        val championCandidates = mutableMapOf<String, DDragonChampionEntry>()
        currentSetChampions.forEach { entry ->
            championCandidates.putIfAbsent(normalize(entry.name), entry)
            championCandidates.putIfAbsent(normalize(stripIdPrefix(entry.id)), entry)
        }
        val itemCandidates = mutableMapOf<String, DDragonItemEntry>()
        currentSetItems.forEach { entry ->
            itemCandidates.putIfAbsent(normalize(entry.name), entry)
        }

        // Match every champion name we reference anywhere locally — not just the small
        // champions.json roster — so imported team comps get icons for champions outside it.
        val localChampionNames = collectLocalChampionNames()
        val championIcons = mutableMapOf<String, String>()
        val unmatchedChampions = mutableListOf<String>()
        localChampionNames.forEach { name ->
            val match = championCandidates[normalize(name)]
            if (match != null) {
                championIcons[name] = championIconUrl(version, match.image.full)
            } else {
                unmatchedChampions += name
            }
        }
        if (unmatchedChampions.isNotEmpty()) {
            val candidateNames = currentSetChampions.map { it.name }
            unmatchedChampions.sorted().forEach { name ->
                val closest = closestCandidates(name, candidateNames)
                Log.w(TAG, "No Data Dragon match for champion '$name'. Closest candidates: $closest")
            }
        }

        val itemIcons = mutableMapOf<String, String>()
        itemRepository.getItems().forEach { item ->
            val match = itemCandidates[normalize(item.name)]
            if (match != null) {
                itemIcons[item.id] = itemIconUrl(version, match.image.full)
            } else {
                Log.w(TAG, "No Data Dragon match for item '${item.name}' (${item.id})")
            }
        }

        _championIconUrls.value = championIcons
        _itemIconUrls.value = itemIcons

        return ApplyResult(
            setNumber = currentSet,
            championCount = currentSetChampions.size,
            itemCount = currentSetItems.size
        )
    }

    /** Every champion name appearing anywhere in our local data: the roster plus every team comp. */
    private suspend fun collectLocalChampionNames(): Set<String> {
        val names = mutableSetOf<String>()
        championRepository.getChampions().forEach { names += it.name }
        teamCompRepository.getTeamComps().forEach { comp ->
            comp.finalBoard.forEach { names += it.champion }
            comp.earlyGameBoard.forEach { names += it.champion }
            comp.midGameBoard.forEach { names += it.champion }
            names += comp.carryChampions
            names += comp.tankChampions
            names += comp.earlyGameChampions
            names += comp.godOfferingPriority
            comp.levelAlternatives.values.forEach { names += it }
        }
        return names
    }

    private fun setNumber(id: String): Int? =
        SET_NUMBER_REGEX.find(id)?.groupValues?.get(1)?.toIntOrNull()

    private fun stripIdPrefix(id: String): String =
        id.replaceFirst(ID_PREFIX_REGEX, "")

    private fun detectCurrentSet(championIds: Collection<String>): Int? =
        championIds.mapNotNull { setNumber(it) }.maxOrNull()

    /** Closest Data Dragon champion names by edit distance, for unmatched-name log lines. */
    private fun closestCandidates(name: String, candidates: List<String>, limit: Int = 3): List<String> {
        val target = normalize(name)
        return candidates
            .distinct()
            .sortedBy { levenshtein(target, normalize(it)) }
            .take(limit)
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }

    private fun writeToDiskCache(version: String, championJson: String, itemJson: String) {
        File(cacheDir, VERSION_FILE).writeText(version)
        File(cacheDir, CHAMPION_FILE).writeText(championJson)
        File(cacheDir, ITEM_FILE).writeText(itemJson)
    }

    private suspend fun fetchLatestVersion(): String {
        val text = fetchText(VERSIONS_URL)
        val versions = json.decodeFromString(ListSerializer(String.serializer()), text)
        return versions.first()
    }

    private suspend fun fetchText(urlString: String): String = withContext(ioDispatcher) {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.requestMethod = "GET"
        try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun championUrl(version: String) = "$CDN_BASE/$version/data/en_US/tft-champion.json"
    private fun itemUrl(version: String) = "$CDN_BASE/$version/data/en_US/tft-item.json"
    private fun championIconUrl(version: String, fileName: String) =
        "$CDN_BASE/$version/img/tft-champion/$fileName"
    private fun itemIconUrl(version: String, fileName: String) =
        "$CDN_BASE/$version/img/tft-item/$fileName"

    /** Lowercase, strip spaces/apostrophes/periods so e.g. "Cho'Gath" matches "Chogath". */
    private fun normalize(name: String): String =
        name.lowercase()
            .replace(" ", "")
            .replace("'", "")
            .replace("’", "")
            .replace(".", "")

    private companion object {
        const val TAG = "DataDragon"
        const val TIMEOUT_MS = 10_000
        const val CDN_BASE = "https://ddragon.leagueoflegends.com/cdn"
        const val VERSIONS_URL = "https://ddragon.leagueoflegends.com/api/versions.json"
        const val VERSION_FILE = "version.txt"
        const val CHAMPION_FILE = "tft-champion.json"
        const val ITEM_FILE = "tft-item.json"
        val SET_NUMBER_REGEX = Regex("^TFT(\\d+)")
        val ID_PREFIX_REGEX = Regex("^TFT\\d*_")
    }
}
