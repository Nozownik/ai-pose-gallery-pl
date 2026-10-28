package pl.nozownik.posegallery.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.nozownik.posegallery.data.Pose
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class OfflineCache(private val context: Context) {
    private val root = File(context.filesDir, "offline_thumbs").also { it.mkdirs() }

    fun localThumb(pose: Pose): File = File(root, "${pose.id}.jpg")

    fun has(pose: Pose): Boolean = localThumb(pose).exists()

    /** Prefer local file URI if cached. */
    fun resolveThumb(pose: Pose): String {
        val f = localThumb(pose)
        return if (f.exists()) f.absolutePath else pose.thumbUrl()
    }

    suspend fun cacheCategory(poses: List<Pose>, onProgress: (Int, Int) -> Unit = { _, _ -> }): Int =
        withContext(Dispatchers.IO) {
            var ok = 0
            poses.forEachIndexed { i, pose ->
                val dest = localThumb(pose)
                if (!dest.exists()) {
                    runCatching {
                        val url = pose.thumbUrl()
                        if (url.startsWith("http")) {
                            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                                connectTimeout = 15000
                                readTimeout = 20000
                            }
                            conn.inputStream.use { input -> dest.outputStream().use { input.copyTo(it) } }
                        }
                    }
                }
                if (dest.exists()) ok++
                onProgress(i + 1, poses.size)
            }
            ok
        }

    fun cachedCount(): Int = root.listFiles()?.count { it.isFile } ?: 0
}
