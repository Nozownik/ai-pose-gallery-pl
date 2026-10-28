package pl.nozownik.posegallery.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class PoseRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _favorites = MutableStateFlow(loadFavIds())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    suspend fun loadPoses(): List<Pose> = withContext(Dispatchers.IO) {
        context.assets.open("data/pose-en.json").bufferedReader().use { reader ->
            val type = object : TypeToken<List<Pose>>() {}.type
            gson.fromJson(reader, type)
        }
    }

    fun buildGroups(poses: List<Pose>): List<PoseGroup> {
        val order = linkedMapOf<String, MutableList<Pose>>()
        poses.forEach { pose ->
            val key = "${pose.system}|${pose.poseNumber.ifBlank { pose.id }}"
            order.getOrPut(key) { mutableListOf() }.add(pose)
        }
        return order.map { (key, items) ->
            val first = items.first()
            PoseGroup(
                key = key,
                system = first.system,
                number = first.poseNumber,
                name = first.poseName,
                category = first.category,
                items = items,
                byAngle = items.associateBy { it.angle }
            )
        }
    }

    private fun loadFavIds(): Set<String> =
        prefs.getStringSet("ids", emptySet())?.toSet() ?: emptySet()

    fun toggleFavorite(id: String) {
        val next = _favorites.value.toMutableSet()
        if (!next.add(id)) next.remove(id)
        prefs.edit().putStringSet("ids", next).apply()
        _favorites.value = next
    }

    fun isFavorite(id: String): Boolean = id in _favorites.value
}
