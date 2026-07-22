package com.tftricks.app.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.tftricks.app.domain.model.Ability
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.ItemCategory
import com.tftricks.app.domain.model.Trait
import com.tftricks.app.domain.model.TraitBreakpoint
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.ItemRepository
import com.tftricks.app.domain.repository.TeamCompRepository
import com.tftricks.app.domain.repository.TraitRepository
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Champions, traits, and items are fetched live from Riot's public CommunityDragon CDN
 * (`raw.communitydragon.org`) on app launch and kept in memory for the process's lifetime —
 * there is no bundled/local copy and no disk cache, so a failed fetch means no data until a
 * retry succeeds. Team comps stay bundled locally (see JsonTeamCompRepository); this
 * repository only supplies live champion/item/trait data and matches its icon art onto
 * whatever champion/item names the bundled comps reference.
 *
 * The current TFT set is detected dynamically from the highest `number` found in the
 * response's `setData` array — never a hardcoded set id — so this keeps working across
 * mid-set patches and the next full set alike.
 *
 * Caveat: this DTO layer (see [CommunityDragonModels]) was written from CommunityDragon's
 * publicly documented schema without a live fetch to verify field names against, because
 * this development environment's network policy blocks raw.communitydragon.org. Every
 * field is nullable/defaulted so a wrong guess degrades a value to blank/missing instead
 * of crashing; the [status] flow and the champion-match debug data below surface exactly
 * what did/didn't come through, for verification on a real device.
 */
class CommunityDragonRepository(
    private val context: Context,
    private val teamCompRepository: TeamCompRepository,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ChampionRepository, ItemRepository, TraitRepository {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val mutex = Mutex()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val api: CommunityDragonApi = Retrofit.Builder()
        .baseUrl("https://raw.communitydragon.org/")
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(CommunityDragonApi::class.java)

    @Volatile private var loaded = false
    @Volatile private var cachedChampions: List<Champion> = emptyList()
    @Volatile private var cachedItems: List<Item> = emptyList()
    @Volatile private var cachedTraits: List<Trait> = emptyList()

    private val _status = MutableStateFlow<CommunityDragonStatus>(CommunityDragonStatus.Loading)
    val status: StateFlow<CommunityDragonStatus> = _status.asStateFlow()

    /** Champion icon URLs keyed by exact local champion display name (e.g. "Cho'Gath"). */
    private val _championIconUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val championIconUrls: StateFlow<Map<String, String>> = _championIconUrls.asStateFlow()

    /** Item icon URLs keyed by the live item id (CommunityDragon apiName). */
    private val _itemIconUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val itemIconUrls: StateFlow<Map<String, String>> = _itemIconUrls.asStateFlow()

    /** Debug-only trace: local champion name -> matched CommunityDragon apiName. */
    private val _championMatchDebug = MutableStateFlow<Map<String, String>>(emptyMap())
    val championMatchDebug: StateFlow<Map<String, String>> = _championMatchDebug.asStateFlow()

    /** Debug-only: every champion id loaded for the current set, sorted. */
    private val _loadedChampionIds = MutableStateFlow<List<String>>(emptyList())
    val loadedChampionIds: StateFlow<List<String>> = _loadedChampionIds.asStateFlow()

    /** Kicks off the initial fetch. Safe to call once at app start. */
    fun initialize() {
        scope.launch { runCatching { ensureLoaded() } }
    }

    /** Manual retry for debug/settings UI: forces a fresh fetch regardless of cache state. */
    fun forceRefresh() {
        scope.launch {
            _status.value = CommunityDragonStatus.Loading
            mutex.withLock {
                loaded = false
                doFetch()
            }
        }
    }

    override suspend fun getChampions(): List<Champion> {
        ensureLoaded()
        return cachedChampions
    }

    override suspend fun getChampion(id: String): Champion? = getChampions().find { it.id == id }

    override suspend fun getItems(): List<Item> {
        ensureLoaded()
        return cachedItems
    }

    override suspend fun getItem(id: String): Item? = getItems().find { it.id == id }

    override suspend fun getTraits(): List<Trait> {
        ensureLoaded()
        return cachedTraits
    }

    override suspend fun getTrait(id: String): Trait? = getTraits().find { it.id == id }

    /** Loads once and caches for the session; concurrent callers share one in-flight fetch. */
    private suspend fun ensureLoaded() {
        if (loaded) return
        mutex.withLock {
            if (loaded) return
            doFetch()
        }
        if (!loaded) {
            val message = (_status.value as? CommunityDragonStatus.Error)?.message
                ?: "Failed to load CommunityDragon data"
            throw IOException(message)
        }
    }

    private suspend fun doFetch() {
        runCatching {
            if (!isOnline()) error("No internet connection")
            val root = api.getTftData()
            applyRoot(root)
        }.onSuccess { result ->
            loaded = true
            _status.value = CommunityDragonStatus.Success(
                setNumber = result.setNumber,
                championCount = result.championCount,
                itemCount = result.itemCount,
                traitCount = result.traitCount
            )
        }.onFailure { e ->
            Log.w(TAG, "CommunityDragon fetch failed", e)
            _status.value = CommunityDragonStatus.Error(e.message ?: e.javaClass.simpleName ?: "Unknown error")
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private data class ApplyResult(
        val setNumber: Int?,
        val championCount: Int,
        val itemCount: Int,
        val traitCount: Int
    )

    private suspend fun applyRoot(root: CDragonRoot): ApplyResult = withContext(ioDispatcher) {
        val set = root.setData.maxByOrNull { it.number ?: Int.MIN_VALUE }
            ?: throw IOException("CommunityDragon response had no set data")
        val currentSet = set.number
        if (currentSet != null) {
            Log.i(TAG, "Detected current TFT set: $currentSet")
        } else {
            Log.w(TAG, "Could not detect a TFT set number; using the only set entry available")
        }

        val traitNameByApi: Map<String, String> = set.traits
            .mapNotNull { t -> t.apiName?.let { it to (t.name ?: it) } }
            .toMap()

        val champions = set.champions.mapNotNull { c ->
            val apiName = c.apiName ?: return@mapNotNull null
            val name = c.name ?: apiName
            Champion(
                id = apiName,
                name = name,
                cost = c.cost ?: 1,
                traits = c.traits.map { raw -> traitNameByApi[raw] ?: raw },
                ability = Ability(
                    name = c.ability?.name ?: "",
                    description = c.ability?.desc ?: "",
                    manaStart = 0,
                    manaMax = 0
                )
            )
        }

        val itemNameByApi: Map<String, String> = root.items
            .mapNotNull { i -> i.apiName?.let { it to (i.name ?: it) } }
            .toMap()

        val items = root.items.mapNotNull { i ->
            val apiName = i.apiName ?: return@mapNotNull null
            val rawComponents = i.from.ifEmpty { i.composition }
            Item(
                id = apiName,
                name = i.name ?: apiName,
                components = rawComponents.map { itemNameByApi[it] ?: it },
                effect = i.desc ?: "",
                category = ItemCategory.OTHER
            )
        }

        val traits = set.traits.mapNotNull { t ->
            val apiName = t.apiName ?: return@mapNotNull null
            val name = t.name ?: apiName
            val breakpoints = t.effects
                .mapNotNull { effect -> effect.minUnits?.let { TraitBreakpoint(count = it, effect = "") } }
                .distinctBy { it.count }
                .sortedBy { it.count }
            val champsWithTrait = champions.filter { name in it.traits }.map { it.name }
            Trait(id = apiName, name = name, description = t.desc ?: "", breakpoints = breakpoints, champions = champsWithTrait)
        }

        applyIconMatching(set, root, champions, items)

        cachedChampions = champions
        cachedItems = items
        cachedTraits = traits

        ApplyResult(
            setNumber = currentSet,
            championCount = champions.size,
            itemCount = items.size,
            traitCount = traits.size
        )
    }

    private suspend fun applyIconMatching(
        set: CDragonSet,
        root: CDragonRoot,
        champions: List<Champion>,
        items: List<Item>
    ) {
        val rawChampionByNormalizedName: Map<String, CDragonChampion> = set.champions
            .mapNotNull { c -> (c.name ?: c.apiName)?.let { normalize(it) to c } }
            .toMap()

        // Match every champion name we reference anywhere locally (the live roster plus
        // every bundled team comp, including variants) — not just an exact string match —
        // since our comp text and the live display name can differ in spacing/punctuation.
        val localChampionNames = collectLocalChampionNames(champions)
        val championIcons = mutableMapOf<String, String>()
        val championMatchDebug = mutableMapOf<String, String>()
        val unmatchedChampions = mutableListOf<String>()
        localChampionNames.forEach { name ->
            val match = rawChampionByNormalizedName[normalize(name)]
            val iconPath = match?.squareIcon ?: match?.icon ?: match?.tileIcon
            if (match != null && iconPath != null) {
                championIcons[name] = assetUrl(iconPath)
                championMatchDebug[name] = match.apiName ?: match.name ?: name
            } else {
                unmatchedChampions += name
            }
        }
        if (unmatchedChampions.isNotEmpty()) {
            val candidateNames = set.champions.mapNotNull { it.name }
            unmatchedChampions.sorted().forEach { name ->
                val closest = closestCandidates(name, candidateNames)
                Log.w(TAG, "No CommunityDragon match for champion '$name'. Closest candidates: $closest")
            }
        }

        val itemIcons = mutableMapOf<String, String>()
        root.items.forEach { i ->
            val apiName = i.apiName ?: return@forEach
            val icon = i.icon ?: return@forEach
            itemIcons[apiName] = assetUrl(icon)
        }

        _championIconUrls.value = championIcons
        _itemIconUrls.value = itemIcons
        _championMatchDebug.value = championMatchDebug
        _loadedChampionIds.value = champions.map { it.id }.sorted()
    }

    /** Every champion name appearing anywhere in our local data: the live roster plus every
     *  bundled team comp field, including nested variants. */
    private suspend fun collectLocalChampionNames(liveChampions: List<Champion>): Set<String> {
        val names = mutableSetOf<String>()
        liveChampions.forEach { names += it.name }
        teamCompRepository.getTeamComps().forEach { comp ->
            comp.finalBoard.forEach { names += it.champion }
            comp.earlyGameBoard.forEach { names += it.champion }
            comp.midGameBoard.forEach { names += it.champion }
            names += comp.carryChampions
            names += comp.tankChampions
            names += comp.earlyGameChampions
            names += comp.godOfferingPriority
            comp.levelAlternatives.values.forEach { names += it }
            comp.variants.forEach { variant ->
                variant.finalBoard.forEach { names += it.champion }
                names += variant.carryChampions
                names += variant.tankChampions
            }
        }
        return names
    }

    /** Closest CommunityDragon champion names by edit distance, for unmatched-name log lines. */
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

    /**
     * CommunityDragon's raw game asset paths (e.g.
     * "ASSETS/Characters/TFT14_Chogath/HUD/TFT14_Chogath_Square.TFT_Set14.tex") are served
     * as lowercased, `.png`-extensioned files under `<base>/latest/game/<path>`.
     */
    private fun assetUrl(rawPath: String): String {
        val lower = rawPath.lowercase()
        val withPng = if (lower.endsWith(".tex") || lower.endsWith(".dds")) {
            lower.substringBeforeLast('.') + ".png"
        } else {
            lower
        }
        return "$GAME_ASSET_BASE/$withPng"
    }

    /** Lowercase, strip spaces/apostrophes/periods so e.g. "Cho'Gath" matches "Chogath". */
    private fun normalize(name: String): String =
        name.lowercase()
            .replace(" ", "")
            .replace("'", "")
            .replace("’", "")
            .replace(".", "")

    private companion object {
        const val TAG = "CommunityDragon"
        const val GAME_ASSET_BASE = "https://raw.communitydragon.org/latest/game"
    }
}
