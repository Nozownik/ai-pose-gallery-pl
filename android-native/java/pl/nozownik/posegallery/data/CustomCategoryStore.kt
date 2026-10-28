package pl.nozownik.posegallery.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

data class CustomCategory(
    val id: String,
    val name: String
)

class CustomCategoryStore(context: Context) {
    private val gson = Gson()
    private val file = File(context.filesDir, "custom_categories.json")
    private val _categories = MutableStateFlow(load())
    val categories: StateFlow<List<CustomCategory>> = _categories.asStateFlow()

    private fun load(): List<CustomCategory> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<CustomCategory>>() {}.type
            gson.fromJson<List<CustomCategory>>(file.readText(), type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun persist(list: List<CustomCategory>) {
        file.writeText(gson.toJson(list))
        _categories.value = list
    }

    fun add(name: String): CustomCategory {
        val cat = CustomCategory(UUID.randomUUID().toString().take(8), name.trim())
        persist(_categories.value + cat)
        return cat
    }

    fun rename(id: String, name: String) {
        persist(_categories.value.map { if (it.id == id) it.copy(name = name.trim()) else it })
    }

    fun delete(id: String) {
        persist(_categories.value.filterNot { it.id == id })
    }
}
