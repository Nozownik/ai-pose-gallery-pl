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

class CustomPoseStore(private val context: Context) {
    private val gson = Gson()
    private val dir = File(context.filesDir, "custom_poses").also { it.mkdirs() }
    private val jsonFile = File(dir, "custom.json")
    private val _poses = MutableStateFlow(load())
    val poses: StateFlow<List<Pose>> = _poses.asStateFlow()

    private fun load(): List<Pose> {
        if (!jsonFile.exists()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<Pose>>() {}.type
            gson.fromJson<List<Pose>>(jsonFile.readText(), type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun persist(list: List<Pose>) {
        jsonFile.writeText(gson.toJson(list))
        _poses.value = list
    }

    suspend fun add(
        imageUri: Uri,
        poseName: String,
        category: String,
        system: String,
        angle: String,
        search: String
    ): Pose = withContext(Dispatchers.IO) {
        val id = "custom_${UUID.randomUUID().toString().take(8)}"
        val dest = File(dir, "$id.jpg")
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Cannot read image")
        val path = dest.absolutePath
        val pose = Pose(
            id = id,
            system = system.ifBlank { "Custom" },
            poseNumber = "CUSTOM",
            category = category.ifBlank { "Custom" },
            poseName = poseName.ifBlank { "Custom pose" },
            angle = angle.ifBlank { "Front" },
            thumb = path,
            full = path,
            search = search,
            isCustom = true,
            localPath = path
        )
        persist(_poses.value + pose)
        pose
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val pose = _poses.value.find { it.id == id } ?: return@withContext
        pose.localPath?.takeIf { it.isNotBlank() }?.let { File(it).delete() }
        persist(_poses.value.filterNot { it.id == id })
    }
}
