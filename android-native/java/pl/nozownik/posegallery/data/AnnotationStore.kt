package pl.nozownik.posegallery.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnnotationStore(context: Context) {
    private val prefs = context.getSharedPreferences("pose_notes", Context.MODE_PRIVATE)
    private val _notes = MutableStateFlow(loadAll())
    val notes: StateFlow<Map<String, String>> = _notes.asStateFlow()

    private fun loadAll(): Map<String, String> =
        prefs.all.mapNotNull { (k, v) -> (v as? String)?.let { k to it } }.toMap()

    fun get(id: String): String = _notes.value[id].orEmpty()

    fun set(id: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            prefs.edit().remove(id).apply()
            _notes.value = _notes.value - id
        } else {
            prefs.edit().putString(id, trimmed).apply()
            _notes.value = _notes.value + (id to trimmed)
        }
    }
}
