package pl.nozownik.posegallery.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import pl.nozownik.posegallery.data.Costume
import pl.nozownik.posegallery.data.CostumeRepository
import pl.nozownik.posegallery.util.ImageClipboard
import pl.nozownik.posegallery.util.ImageSaver

data class CostumeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val all: List<Costume> = emptyList(),
    val counts: Map<String, Int> = emptyMap(),
    /** "" = all, "fav" = favourites, otherwise category key */
    val category: String = "",
    val query: String = "",
    val visible: List<Costume> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val scrollTick: Long = 0L
)

class CostumeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CostumeRepository(app)
    private val _data = MutableStateFlow(CostumeUiState())
    private val _cat = MutableStateFlow("")
    private val _q = MutableStateFlow("")
    private val _tick = MutableStateFlow(0L)

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<CostumeUiState> = combine(_data, _cat, _q, repo.favorites, _tick) { d, cat, q, fav, tick ->
        val words = q.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val visible = d.all.filter { c ->
            when (cat) {
                "" -> true
                "fav" -> c.id in fav
                else -> c.cat == cat
            } && (words.isEmpty() || words.all { w -> matches(c, w) })
        }
        d.copy(category = cat, query = q, visible = visible, favorites = fav, scrollTick = tick)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CostumeUiState())

    init {
        viewModelScope.launch {
            try {
                val all = repo.load()
                _data.value = CostumeUiState(
                    loading = false,
                    all = all,
                    counts = all.groupingBy { it.cat }.eachCount()
                )
            } catch (e: Exception) {
                _data.value = CostumeUiState(loading = false, error = e.message ?: "error")
            }
        }
    }

    private fun matches(c: Costume, w: String): Boolean {
        val e = CostumeSearch.toEnglish(w)
        if (c.id.lowercase().contains(w)) return true
        if (c.cat.startsWith(e) || CostumeSearch.categoryLabelMatches(c.cat, w)) return true
        if (c.tokens().any { it.startsWith(e) }) return true
        return c.isCustom && c.searchHay().contains(w)
    }

    fun setCategory(cat: String) {
        if (_cat.value == cat) { bump(); return }
        _cat.value = cat
        bump()
    }

    fun setQuery(q: String) { _q.value = q }
    private fun bump() { _tick.value = System.currentTimeMillis() }

    fun find(id: String): Costume? = _data.value.all.find { it.id == id }
    fun toggleFavorite(id: String) = repo.toggleFavorite(id)

    fun adjacent(id: String, dir: Int): String? {
        val list = uiState.value.visible.ifEmpty { _data.value.all }
        val i = list.indexOfFirst { it.id == id }
        if (i < 0) return null
        return list.getOrNull(i + dir)?.id
    }

    fun copyText(text: String, event: String = "copied_tags") {
        val cm = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("outfit", text))
        viewModelScope.launch { _events.emit(event) }
    }

    fun copyImage(c: Costume) {
        viewModelScope.launch {
            val ok = ImageClipboard.copyImage(getApplication(), c.imageUrl(), c.id)
            _events.emit(if (ok) "image_copied" else "image_copy_fail")
        }
    }

    /** Saves one original PNG (single image, on explicit user action). */
    fun saveImage(c: Costume) {
        viewModelScope.launch {
            val ok = if (c.isCustom) ImageSaver.savePoseImage(getApplication(), c.originalUrl(), c.id, "image/webp", "webp")
            else ImageSaver.savePoseImage(getApplication(), c.originalUrl(), c.id, "image/png", "png")
            _events.emit(if (ok) "saved_ok" else "saved_fail")
        }
    }
}

/** Polish (and a few English) search words → English keyword stems used by the dataset. */
object CostumeSearch {
    private val stems: List<Pair<String, String>> = listOf(
        "czarn" to "black", "biał" to "white", "białe" to "white", "biel" to "white", "czerwon" to "red",
        "różow" to "pink", "rozow" to "pink", "niebiesk" to "blue", "granat" to "navy", "brąz" to "brown",
        "braz" to "brown", "beż" to "beige", "bez" to "beige", "szar" to "grey", "zielon" to "green",
        "żółt" to "yellow", "zolt" to "yellow", "pomarańcz" to "orange", "pomarancz" to "orange",
        "fiolet" to "purple", "lawend" to "lavender", "miętow" to "mint", "mietow" to "mint",
        "kremow" to "cream", "złot" to "gold", "zlot" to "gold", "srebr" to "silver", "khaki" to "khaki",
        "bordo" to "burgundy", "oliwk" to "olive",
        "sukien" to "dress", "spódnic" to "skirt", "spodnic" to "skirt", "kurtk" to "jacket",
        "marynark" to "jacket", "płaszcz" to "coat", "plaszcz" to "coat", "swet" to "sweater",
        "kardigan" to "cardigan", "bluzk" to "blouse", "koszul" to "shirt", "spodn" to "pants",
        "dżins" to "denim", "dzins" to "denim", "jeans" to "denim", "szort" to "shorts", "kozak" to "boots",
        "botk" to "boots", "trampk" to "sneakers", "sneaker" to "sneakers", "sandał" to "sandals",
        "sandal" to "sandals", "szpilk" to "heels", "obcas" to "heels", "mokasyn" to "loafers",
        "czółen" to "pumps", "czolen" to "pumps", "skarpet" to "socks", "rajstop" to "tights",
        "pończoch" to "stockings", "ponczoch" to "stockings", "toreb" to "bag", "torb" to "bag",
        "kapelusz" to "hat", "czapk" to "beanie", "beret" to "beret", "szalik" to "scarf", "chust" to "scarf",
        "rękawicz" to "gloves", "rekawicz" to "gloves", "pasek" to "belt", "naszyjnik" to "necklace",
        "choker" to "choker", "stanik" to "bra", "biustonosz" to "bra", "majt" to "briefs", "figi" to "briefs",
        "kostium" to "swimsuit", "strój" to "swimsuit", "bikini" to "bikini", "bluz" to "hoodie",
        "kamizel" to "vest", "gorset" to "corset", "szlafrok" to "robe", "golf" to "turtleneck",
        "okular" to "sunglasses", "opask" to "headband", "fartuch" to "apron",
        "koronk" to "lace", "satyn" to "satin", "skór" to "leather", "skor" to "leather",
        "dzianin" to "knit", "zamsz" to "suede", "futr" to "fur", "aksamit" to "velvet", "weł" to "wool",
        "wel" to "wool", "szyfon" to "chiffon", "tiul" to "tulle", "siatk" to "mesh", "sztruks" to "corduroy",
        "kwiat" to "floral", "grosz" to "polka-dot", "kokard" to "ribbon", "falban" to "frill",
        "plis" to "pleated", "mini" to "mini", "midi" to "midi", "maxi" to "maxi"
    )

    fun toEnglish(word: String): String {
        val w = word.lowercase()
        return stems.firstOrNull { (pl, _) -> w.startsWith(pl) }?.second ?: w
    }

    private val labelsPl = mapOf(
        "spring" to "wiosna", "summer" to "lato", "autumn" to "jesień jesien", "winter" to "zima",
        "lingerie" to "bielizna", "party" to "impreza", "swimwear" to "stroje kąpielowe kapielowe",
        "sexy" to "seksowne"
    )

    fun categoryLabelMatches(cat: String, w: String): Boolean =
        labelsPl[cat]?.split(' ')?.any { it.startsWith(w) } == true
}
