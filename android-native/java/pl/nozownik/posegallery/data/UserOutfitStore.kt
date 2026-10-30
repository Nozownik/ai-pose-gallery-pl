package pl.nozownik.posegallery.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** User's own outfits ("Moje"), stored only in app-private storage. */
class UserOutfitStore(private val context: Context) {
    private val gson = Gson()
    private val dir = File(context.filesDir, "user_outfits").also { it.mkdirs() }
    private val jsonFile = File(dir, "outfits.json")
    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<Costume>> = _items.asStateFlow()

    private fun load(): List<Costume> {
        if (!jsonFile.exists()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<Costume>>() {}.type
            gson.fromJson<List<Costume>>(jsonFile.readText(), type).orEmpty().map { it.copy(cat = Costume.MINE) }
        }.getOrDefault(emptyList())
    }

    private fun persist(list: List<Costume>) {
        jsonFile.writeText(gson.toJson(list))
        _items.value = list
    }

    suspend fun add(imageUri: Uri, name: String, keywords: String): Costume = withContext(Dispatchers.IO) {
        val id = "MOJE-" + UUID.randomUUID().toString().take(6).uppercase()
        val ext = when (context.contentResolver.getType(imageUri)) {
            "image/png" -> "png"; "image/webp" -> "webp"; "image/gif" -> "gif"; else -> "jpg"
        }
        val dest = File(dir, "$id.$ext")
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Cannot read image")
        val clean = name.trim().ifBlank { "My outfit" }
        val item = Costume(
            id = id, cat = Costume.MINE, kw = keywords.trim(),
            nameEn = clean, namePl = clean, localPath = dest.absolutePath
        )
        persist(listOf(item) + _items.value)
        item
    }

    suspend fun update(id: String, name: String, keywords: String) = withContext(Dispatchers.IO) {
        persist(_items.value.map {
            if (it.id == id) {
                val n = name.trim().ifBlank { it.nameEn ?: "My outfit" }
                it.copy(nameEn = n, namePl = n, kw = keywords.trim())
            } else it
        })
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        _items.value.find { it.id == id }?.localPath?.let { File(it).delete() }
        persist(_items.value.filterNot { it.id == id })
    }
}
