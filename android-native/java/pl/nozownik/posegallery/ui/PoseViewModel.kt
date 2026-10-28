package pl.nozownik.posegallery.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.nozownik.posegallery.data.Pose
import pl.nozownik.posegallery.data.PoseGroup
import pl.nozownik.posegallery.data.PoseRepository

data class FilterState(
    val system: String = "",
    val category: String = "",
    val angle: String = "",
    val query: String = "",
    val tab: Int = 0 // 0 gallery, 1 favorites
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
    val favorites: Set<String> = emptySet()
)

class PoseViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PoseRepository(app)
    private val _base = MutableStateFlow(GalleryUiState())
    private val _filters = MutableStateFlow(FilterState())

    val uiState: StateFlow<GalleryUiState> = combine(_base, _filters, repo.favorites) { base, filters, favs ->
        val cats = base.poses
            .filter { filters.system.isEmpty() || it.system == filters.system }
            .map { it.category }.distinct().sorted()
        val q = filters.query.trim().lowercase()

        fun groupHit(g: PoseGroup): Boolean {
            if (filters.tab == 1 && g.items.none { it.id in favs }) return false
            if (filters.system.isNotEmpty() && g.system != filters.system) return false
            if (filters.category.isNotEmpty() && g.category != filters.category) return false
            val angleOk = filters.angle.isEmpty() || g.byAngle.containsKey(filters.angle)
            if (!angleOk) return false
            if (q.isNotEmpty()) {
                val hay = buildString {
                    append(g.name); append(' '); append(g.number); append(' ')
                    append(g.system); append(' '); append(g.category); append(' ')
                    g.items.forEach { append(it.id); append(' '); append(it.angle); append(' '); append(it.search); append(' ') }
                }.lowercase()
                if (!hay.contains(q)) return false
            }
            return true
        }

        val vis = base.groups.filter(::groupHit)
        base.copy(
            filters = filters,
            categories = cats,
            visibleGroups = vis,
            favorites = favs
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GalleryUiState())

    init {
        viewModelScope.launch {
            try {
                val poses = repo.loadPoses()
                val groups = repo.buildGroups(poses)
                val angleOrder = listOf(
                    "Front", "High angle / top-down", "Low angle / looking up", "Side", "Three-quarter"
                )
                _base.value = GalleryUiState(
                    loading = false,
                    poses = poses,
                    groups = groups,
                    systems = poses.map { it.system }.distinct(),
                    categories = poses.map { it.category }.distinct().sorted(),
                    angles = angleOrder.filter { a -> poses.any { it.angle == a } }
                        .ifEmpty { poses.map { it.angle }.distinct() }
                )
            } catch (e: Exception) {
                _base.value = GalleryUiState(loading = false, error = e.message ?: "Błąd ładowania")
            }
        }
    }

    fun setSystem(v: String) = _filters.update { it.copy(system = v, category = "") }
    fun setCategory(v: String) = _filters.update { it.copy(category = v) }
    fun setAngle(v: String) = _filters.update { it.copy(angle = v) }
    fun setQuery(v: String) = _filters.update { it.copy(query = v) }
    fun setTab(t: Int) = _filters.update { it.copy(tab = t) }
    fun resetFilters() = _filters.update { FilterState(tab = it.tab) }
    fun toggleFavorite(id: String) = repo.toggleFavorite(id)
    fun findPose(id: String): Pose? = _base.value.poses.find { it.id == id }
    fun groupForId(id: String): PoseGroup? = _base.value.groups.find { g -> g.items.any { it.id == id } }
}
