package pl.nozownik.posegallery.ui

import android.app.Application
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
import pl.nozownik.posegallery.data.CustomPoseStore
import pl.nozownik.posegallery.data.Pose
import pl.nozownik.posegallery.data.PoseGroup
import pl.nozownik.posegallery.data.PoseRepository
import pl.nozownik.posegallery.data.SettingsStore
import pl.nozownik.posegallery.util.ImageSaver

data class FilterState(
    val system: String = "",
    val category: String = "",
    val angle: String = "",
    val query: String = "",
    val tab: Int = 0 // 0 gallery, 1 favorites, 2 settings
)

data class GalleryUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val poses: List<Pose> = emptyList(),
    val groups: List<PoseGroup> = emptyList(),
    val systems: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val angles: List<String> = emptyList(),
    val filters: FilterState = FilterState(),
    val visibleGroups: List<PoseGroup> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val settings: AppSettings = AppSettings(),
    val notes: Map<String, String> = emptyMap(),
    val scrollToTopTick: Long = 0L
)

class PoseViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PoseRepository(app)
    private val settingsStore = SettingsStore(app)
    private val customStore = CustomPoseStore(app)
    private val annotationStore = AnnotationStore(app)

    private val _core = MutableStateFlow(GalleryUiState())
    private val _filters = MutableStateFlow(FilterState())
    private val _scrollTick = MutableStateFlow(0L)

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    private data class Left(
        val core: GalleryUiState,
        val filters: FilterState,
        val favs: Set<String>,
        val settings: AppSettings
    )

    private data class Right(
        val customs: List<Pose>,
        val notes: Map<String, String>,
        val tick: Long
    )

    val uiState: StateFlow<GalleryUiState> = combine(
        combine(_core, _filters, repo.favorites, settingsStore.settings) { core, filters, favs, settings ->
            Left(core, filters, favs, settings)
        },
        combine(customStore.poses, annotationStore.notes, _scrollTick) { customs, notes, tick ->
            Right(customs, notes, tick)
        }
    ) { left, right ->
        val allPoses = left.core.poses.filter { !it.isCustom } + right.customs
        val groups = repo.buildGroups(allPoses)
        val cats = allPoses
            .filter { left.filters.system.isEmpty() || it.system == left.filters.system }
            .map { it.category }.distinct().sorted()
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
            filters = left.filters,
            visibleGroups = groups.filter(::groupHit),
            favorites = left.favs,
            settings = left.settings,
            notes = right.notes,
            scrollToTopTick = right.tick
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GalleryUiState())

    init {
        viewModelScope.launch {
            try {
                val poses = repo.loadAssetPoses()
                val angleOrder = listOf(
                    "Front", "High angle / top-down", "Low angle / looking up", "Side", "Three-quarter"
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
        _filters.update { it.copy(system = v, category = "") }
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

    fun setQuery(v: String) = _filters.update { it.copy(query = v) }
    fun setTab(t: Int) = _filters.update { it.copy(tab = t) }
    fun resetFilters() {
        _filters.update { FilterState(tab = it.tab) }
        bumpScrollTop()
    }

    private fun bumpScrollTop() {
        _scrollTick.value = System.currentTimeMillis()
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

    fun addCustom(
        uri: Uri,
        poseName: String,
        category: String,
        system: String,
        angle: String,
        search: String,
        onDone: (Pose?) -> Unit
    ) {
        viewModelScope.launch {
            val pose = runCatching {
                customStore.add(uri, poseName, category, system, angle, search)
            }.getOrNull()
            onDone(pose)
        }
    }

    fun deleteCustom(id: String) {
        viewModelScope.launch { customStore.delete(id) }
    }

    fun saveImage(pose: Pose) {
        viewModelScope.launch {
            val ok = ImageSaver.savePoseImage(getApplication(), pose.fullUrl(), pose.id)
            _events.emit(if (ok) "saved_ok" else "saved_fail")
        }
    }
}
