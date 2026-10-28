package pl.nozownik.posegallery.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.nozownik.posegallery.data.AnnotationStore
import pl.nozownik.posegallery.data.AppLanguage
import pl.nozownik.posegallery.data.AppSettings
import pl.nozownik.posegallery.data.AppThemeId
import pl.nozownik.posegallery.data.ClipboardEntry
import pl.nozownik.posegallery.data.ClipboardEntryType
import pl.nozownik.posegallery.data.ClipboardHistoryStore
import pl.nozownik.posegallery.data.CustomCategory
import pl.nozownik.posegallery.data.CustomCategoryStore
import pl.nozownik.posegallery.data.CustomPoseStore
import pl.nozownik.posegallery.data.Pose
import pl.nozownik.posegallery.data.PoseGroup
import pl.nozownik.posegallery.data.PoseRepository
import pl.nozownik.posegallery.data.SettingsStore
import pl.nozownik.posegallery.util.ExportPack
import pl.nozownik.posegallery.util.ImageClipboard
import pl.nozownik.posegallery.util.ImageSaver
import pl.nozownik.posegallery.util.OfflineCache
import pl.nozownik.posegallery.util.ShortcutHelper

data class FilterState(
    val system: String = "",
    val category: String = "",
    val angle: String = "",
    val query: String = "",
    val tab: Int = 0
)

data class GalleryUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val poses: List<Pose> = emptyList(),
    val groups: List<PoseGroup> = emptyList(),
    val systems: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val customCategories: List<CustomCategory> = emptyList(),
    val angles: List<String> = emptyList(),
    val filters: FilterState = FilterState(),
    val visibleGroups: List<PoseGroup> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val settings: AppSettings = AppSettings(),
    val notes: Map<String, String> = emptyMap(),
    val scrollToTopTick: Long = 0L,
    val clipboardHistory: List<ClipboardEntry> = emptyList(),
    val batchMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val offlineCached: Int = 0,
    val compareA: String? = null,
    val compareB: String? = null,
    val openPoseId: String? = null
)

class PoseViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PoseRepository(app)
    private val settingsStore = SettingsStore(app)
    private val customStore = CustomPoseStore(app)
    private val annotationStore = AnnotationStore(app)
    private val categoryStore = CustomCategoryStore(app)
    private val clipboardStore = ClipboardHistoryStore(app)
    private val offlineCache = OfflineCache(app)

    private val _core = MutableStateFlow(GalleryUiState())
    private val _filters = MutableStateFlow(FilterState())
    private val _scrollTick = MutableStateFlow(0L)
    private val _batchMode = MutableStateFlow(false)
    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    private val _compareA = MutableStateFlow<String?>(null)
    private val _compareB = MutableStateFlow<String?>(null)
    private val _openPoseId = MutableStateFlow<String?>(null)
    private val _offlineCount = MutableStateFlow(offlineCache.cachedCount())

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    private data class Left(val core: GalleryUiState, val filters: FilterState, val favs: Set<String>, val settings: AppSettings)
    private data class Mid(val customs: List<Pose>, val notes: Map<String, String>, val tick: Long, val customCats: List<CustomCategory>)
    private data class ExtraA(val history: List<ClipboardEntry>, val batch: Boolean, val selected: Set<String>, val offline: Int)
    private data class ExtraB(val compareA: String?, val compareB: String?, val openId: String?)

    val uiState: StateFlow<GalleryUiState> = combine(
        combine(_core, _filters, repo.favorites, settingsStore.settings) { c, f, fav, s -> Left(c, f, fav, s) },
        combine(customStore.poses, annotationStore.notes, _scrollTick, categoryStore.categories) { p, n, t, cc -> Mid(p, n, t, cc) },
        combine(clipboardStore.entries, _batchMode, _selected, _offlineCount) { h, b, s, o -> ExtraA(h, b, s, o) },
        combine(_compareA, _compareB, _openPoseId) { a, b, o -> ExtraB(a, b, o) }
    ) { left, mid, ea, eb ->
        val allPoses = left.core.poses.filter { !it.isCustom } + mid.customs
        val groups = repo.buildGroups(allPoses)
        val builtInCats = allPoses
            .filter { left.filters.system.isEmpty() || it.system == left.filters.system }
            .map { it.category }
        val cats = (builtInCats + mid.customCats.map { it.name }).distinct().sorted()
        val systems = allPoses.map { it.system }.distinct()
        val q = left.filters.query.trim().lowercase()

        fun groupHit(g: PoseGroup): Boolean {
            if (left.filters.tab == 1 && g.items.none { it.id in left.favs }) return false
            if (left.filters.system.isNotEmpty() && g.system != left.filters.system) return false
            if (left.filters.category.isNotEmpty() && g.category != left.filters.category) return false
            if (left.filters.angle.isNotEmpty() && !g.byAngle.containsKey(left.filters.angle)) return false
            if (q.isNotEmpty()) {
                val hay = buildString {
                    append(g.name); append(' '); append(g.number); append(' ')
                    append(g.system); append(' '); append(g.category); append(' ')
                    g.items.forEach {
                        append(it.id); append(' '); append(it.angle); append(' ')
                        append(it.search.orEmpty()); append(' ')
                    }
                }.lowercase()
                if (!hay.contains(q)) return false
            }
            return true
        }

        left.core.copy(
            poses = allPoses,
            groups = groups,
            systems = systems,
            categories = cats,
            customCategories = mid.customCats,
            filters = left.filters,
            visibleGroups = groups.filter(::groupHit),
            favorites = left.favs,
            settings = left.settings,
            notes = mid.notes,
            scrollToTopTick = mid.tick,
            clipboardHistory = ea.history,
            batchMode = ea.batch,
            selectedIds = ea.selected,
            offlineCached = ea.offline,
            compareA = eb.compareA,
            compareB = eb.compareB,
            openPoseId = eb.openId
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GalleryUiState())

    init {
        ShortcutHelper.ensureDynamicShortcuts(app)
        viewModelScope.launch {
            try {
                val poses = repo.loadAssetPoses()
                val angleOrder = listOf(
                    "Front", "Side", "Three-quarter", "High angle / top-down", "Low angle / looking up", "Back"
                )
                _core.value = GalleryUiState(
                    loading = false,
                    poses = poses,
                    systems = poses.map { it.system }.distinct(),
                    categories = poses.map { it.category }.distinct().sorted(),
                    angles = angleOrder.filter { a -> poses.any { it.angle == a } }
                        .ifEmpty { poses.map { it.angle }.distinct() }
                )
            } catch (e: Exception) {
                _core.value = GalleryUiState(loading = false, error = e.message ?: "error")
            }
        }
    }

    fun setSystem(v: String) {
        _filters.update { cur -> if (cur.system == v) cur else cur.copy(system = v, category = "") }
        bumpScrollTop()
    }

    fun setCategory(v: String) {
        _filters.update { it.copy(category = v) }
        bumpScrollTop()
    }

    fun setAngle(v: String) {
        _filters.update { it.copy(angle = v) }
        bumpScrollTop()
    }

    fun scrollToTop() = bumpScrollTop()
    fun setQuery(v: String) = _filters.update { it.copy(query = v) }
    fun setTab(t: Int) = _filters.update { it.copy(tab = t) }
    fun resetFilters() {
        _filters.update { FilterState(tab = it.tab) }
        bumpScrollTop()
    }

    private fun bumpScrollTop() { _scrollTick.value = System.currentTimeMillis() }

    fun shiftCategory(dir: Int) {
        val cats = listOf("") + uiState.value.categories
        val idx = cats.indexOf(uiState.value.filters.category).coerceAtLeast(0)
        val next = cats.getOrNull(idx + dir) ?: return
        setCategory(next)
    }

    fun toggleFavorite(id: String) = repo.toggleFavorite(id)
    fun findPose(id: String): Pose? = uiState.value.poses.find { it.id == id }
    fun groupForId(id: String): PoseGroup? =
        uiState.value.groups.find { g -> g.items.any { it.id == id } }

    fun setLanguage(lang: AppLanguage) = settingsStore.setLanguage(lang)
    fun setTheme(theme: AppThemeId) = settingsStore.setTheme(theme)

    fun setNote(id: String, text: String) {
        annotationStore.set(id, text)
        viewModelScope.launch { _events.emit("note_saved") }
    }

    fun addCustom(uri: Uri, poseName: String, category: String, system: String, angle: String, search: String, onDone: (Pose?) -> Unit) {
        viewModelScope.launch {
            onDone(runCatching { customStore.add(uri, poseName, category, system, angle, search) }.getOrNull())
        }
    }

    fun deleteCustom(id: String) { viewModelScope.launch { customStore.delete(id) } }

    fun updateCustomCategory(poseId: String, category: String) {
        viewModelScope.launch { customStore.updateCategory(poseId, category) }
    }

    fun addCustomCategory(name: String) { if (name.isNotBlank()) categoryStore.add(name) }

    fun renameCustomCategory(id: String, name: String) {
        val old = categoryStore.categories.value.find { it.id == id }?.name ?: return
        categoryStore.rename(id, name)
        viewModelScope.launch { customStore.remapCategory(old, name.trim()) }
    }

    fun deleteCustomCategory(id: String) = categoryStore.delete(id)

    fun saveImage(pose: Pose) {
        viewModelScope.launch {
            _events.emit(if (ImageSaver.savePoseImage(getApplication(), pose.fullUrl(), pose.id)) "saved_ok" else "saved_fail")
        }
    }

    fun copyTags(pose: Pose) {
        clipboardStore.pushTags(pose.poseName, pose.promptTags())
        val cm = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("pose", pose.promptTags()))
        viewModelScope.launch { _events.emit("copied_tags") }
    }

    fun copyImage(pose: Pose) {
        viewModelScope.launch {
            val ok = ImageClipboard.copyImage(getApplication(), pose.fullUrl(), pose.id)
            if (ok) clipboardStore.pushImage(pose.poseName, pose.fullUrl())
            _events.emit(if (ok) "image_copied" else "image_copy_fail")
        }
    }

    fun reCopyHistory(entry: ClipboardEntry) {
        viewModelScope.launch {
            when (entry.type) {
                ClipboardEntryType.TAGS -> {
                    val cm = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("pose", entry.payload))
                    _events.emit("copied_tags")
                }
                ClipboardEntryType.IMAGE -> {
                    _events.emit(if (ImageClipboard.copyImage(getApplication(), entry.payload, entry.id)) "image_copied" else "image_copy_fail")
                }
            }
        }
    }

    fun clearClipboardHistory() = clipboardStore.clear()

    fun setBatchMode(on: Boolean) {
        _batchMode.value = on
        if (!on) _selected.value = emptySet()
    }

    fun toggleSelect(id: String) {
        _selected.update { if (id in it) it - id else it + id }
    }

    fun copySelectedTags() {
        val poses = uiState.value.selectedIds.mapNotNull { findPose(it) }
        if (poses.isEmpty()) return
        val text = poses.joinToString("\n") { it.promptTags() }
        clipboardStore.pushTags("batch ${poses.size}", text)
        val cm = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("poses", text))
        viewModelScope.launch { _events.emit("batch_copied") }
    }

    fun setCompareSlot(poseId: String) {
        when {
            _compareA.value == null -> _compareA.value = poseId
            _compareB.value == null && poseId != _compareA.value -> _compareB.value = poseId
            else -> { _compareA.value = poseId; _compareB.value = null }
        }
    }

    fun clearCompare() { _compareA.value = null; _compareB.value = null }

    fun exportCustomPack() {
        viewModelScope.launch {
            val customs = uiState.value.poses.filter { it.isCustom }
            if (customs.isEmpty()) { _events.emit("export_empty"); return@launch }
            val zip = ExportPack.exportCustomPoses(getApplication(), customs)
            ExportPack.shareZip(getApplication(), zip)
            _events.emit("export_ok")
        }
    }

    fun cacheCurrentCategory() {
        viewModelScope.launch {
            val thumbs = uiState.value.visibleGroups.map { it.primary }
            val n = offlineCache.cacheCategory(thumbs)
            _offlineCount.value = offlineCache.cachedCount()
            _events.emit("cache_ok:$n")
        }
    }

    fun resolveThumb(pose: Pose): String = offlineCache.resolveThumb(pose)

    fun openRandomPose() {
        val pool = uiState.value.visibleGroups.ifEmpty { uiState.value.groups }
        _openPoseId.value = pool.randomOrNull()?.primary?.id
    }

    fun consumeOpenPose() { _openPoseId.value = null }

    fun pinRandomShortcut() {
        val ok = ShortcutHelper.requestPinRandom(getApplication())
        viewModelScope.launch { _events.emit(if (ok) "shortcut_ok" else "shortcut_fail") }
    }

    fun adjacentAngleId(current: Pose, group: PoseGroup, dir: Int): String? {
        val idx = group.items.indexOfFirst { it.id == current.id }
        if (idx < 0) return null
        return group.items.getOrNull(idx + dir)?.id
    }
}
