package pl.nozownik.posegallery.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.io.FileInputStream

object ImageSaver {
    suspend fun savePoseImage(
        context: Context,
        source: String,
        displayName: String,
        mimeType: String = "image/jpeg",
        extension: String = "jpg"
    ): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val bytes = when {
                    source.startsWith("http://") || source.startsWith("https://") -> download(source)
                    else -> FileInputStream(File(source)).use { it.readBytes() }
                }
                val name = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_") + "." + extension
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GaleriaPozAI")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@runCatching false
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: return@runCatching false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                true
            }.getOrDefault(false)
        }

    private fun download(url: String): ByteArray {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20000
            readTimeout = 30000
            instanceFollowRedirects = true
        }
        conn.inputStream.use { return it.readBytes() }
    }
}
