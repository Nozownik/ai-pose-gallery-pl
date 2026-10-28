package pl.nozownik.posegallery.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class ClipboardEntryType { TAGS, IMAGE }

data class ClipboardEntry(
    val id: String,
    val type: ClipboardEntryType,
    val label: String,
    /** For TAGS: the text. For IMAGE: local file path or content URI string. */
    val payload: String,
    val ts: Long = System.currentTimeMillis()
)

class ClipboardHistoryStore(context: Context) {
    private val gson = Gson()
    private val file = File(context.filesDir, "clipboard_history.json")
    private val _entries = MutableStateFlow(load())
    val entries: StateFlow<List<ClipboardEntry>> = _entries.asStateFlow()

    private fun load(): List<ClipboardEntry> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<ClipboardEntry>>() {}.type
            gson.fromJson<List<ClipboardEntry>>(file.readText(), type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun persist(list: List<ClipboardEntry>) {
        file.writeText(gson.toJson(list.take(40)))
        _entries.value = list.take(40)
    }

    fun pushTags(label: String, text: String) {
        if (text.isBlank()) return
        val e = ClipboardEntry(
            id = "t_${System.currentTimeMillis()}",
            type = ClipboardEntryType.TAGS,
            label = label.take(80),
            payload = text
        )
        persist(listOf(e) + _entries.value.filterNot { it.type == ClipboardEntryType.TAGS && it.payload == text })
    }

    fun pushImage(label: String, pathOrUri: String) {
        if (pathOrUri.isBlank()) return
        val e = ClipboardEntry(
            id = "i_${System.currentTimeMillis()}",
            type = ClipboardEntryType.IMAGE,
            label = label.take(80),
            payload = pathOrUri
        )
        persist(listOf(e) + _entries.value.filterNot { it.type == ClipboardEntryType.IMAGE && it.payload == pathOrUri })
    }

    fun clear() = persist(emptyList())
}
