package com.tftricks.app.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.ItemRepository
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
 * it onto our local champion/item ids by matching normalized names.
 *
 * Offline-first: whatever was cached from the last successful fetch loads immediately,
 * then a background refresh runs if the network is reachable and overwrites the cache.
 */
class DataDragonRepository(
    private val context: Context,
    private val championRepository: ChampionRepository,
    private val itemRepository: ItemRepository,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val cacheDir: File by lazy { File(context.filesDir, "ddragon").apply { mkdirs() } }

    private val _championIconUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val championIconUrls: StateFlow<Map<String, String>> = _championIconUrls.asStateFlow()

    private val _itemIconUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val itemIconUrls: StateFlow<Map<String, String>> = _itemIconUrls.asStateFlow()

    /** Loads the cached icon maps (if any), then refreshes from the network if online. */
    fun initialize() {
        scope.launch {
            loadFromDiskCache()
            if (isOnline()) refreshFromNetwork()
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
        }.onFailure {
            Log.w(TAG, "Failed to load cached Data Dragon data", it)
        }
    }

    private suspend fun refreshFromNetwork() {
        runCatching {
            val version = fetchLatestVersion()
            val championJson = fetchText(championUrl(version))
            val itemJson = fetchText(itemUrl(version))
            applyResponses(version, championJson, itemJson)
            writeToDiskCache(version, championJson, itemJson)
        }.onFailure {
            Log.w(TAG, "Data Dragon refresh failed, keeping cached/previous icon data", it)
        }
    }

    private suspend fun applyResponses(version: String, championJson: String, itemJson: String) {
        val championResponse = json.decodeFromString(DDragonChampionResponse.serializer(), championJson)
        val itemResponse = json.decodeFromString(DDragonItemResponse.serializer(), itemJson)

        val championByNormalizedName = championResponse.data.values.associateBy { normalize(it.name) }
        val itemByNormalizedName = itemResponse.data.values.associateBy { normalize(it.name) }

        val championIcons = mutableMapOf<String, String>()
        championRepository.getChampions().forEach { champion ->
            val match = championByNormalizedName[normalize(champion.name)]
            if (match != null) {
                championIcons[champion.id] = championIconUrl(version, match.image.full)
            } else {
                Log.w(TAG, "No Data Dragon match for champion '${champion.name}' (${champion.id})")
            }
        }

        val itemIcons = mutableMapOf<String, String>()
        itemRepository.getItems().forEach { item ->
            val match = itemByNormalizedName[normalize(item.name)]
            if (match != null) {
                itemIcons[item.id] = itemIconUrl(version, match.image.full)
            } else {
                Log.w(TAG, "No Data Dragon match for item '${item.name}' (${item.id})")
            }
        }

        _championIconUrls.value = championIcons
        _itemIconUrls.value = itemIcons
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

    /** Lowercase, strip spaces and apostrophes so e.g. "Guinsoo's Rageblade" matches "Guinsoo's Rageblade". */
    private fun normalize(name: String): String =
        name.lowercase().replace(" ", "").replace("'", "").replace("’", "")

    private companion object {
        const val TAG = "DataDragon"
        const val TIMEOUT_MS = 10_000
        const val CDN_BASE = "https://ddragon.leagueoflegends.com/cdn"
        const val VERSIONS_URL = "https://ddragon.leagueoflegends.com/api/versions.json"
        const val VERSION_FILE = "version.txt"
        const val CHAMPION_FILE = "tft-champion.json"
        const val ITEM_FILE = "tft-item.json"
    }
}
