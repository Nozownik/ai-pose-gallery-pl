package pl.nozownik.posegallery.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageClipboard {
    private const val ASSET_PREFIX = "file:///android_asset/"
    /**
     * Puts the actual image on the system clipboard (not a URL string),
     * so keyboards / chat apps can paste it.
     */
    suspend fun copyImage(context: Context, source: String, nameHint: String = "pose"): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val bytes = loadBytes(context, source)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: return@runCatching false
                val jpeg = ByteArrayOutputStream().use { bos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, bos)
                    bos.toByteArray()
                }
                if (!bitmap.isRecycled) bitmap.recycle()

                val uri = insertCacheViaFileProvider(context, jpeg, nameHint)
                    ?: insertViaMediaStore(context, jpeg, nameHint)
                    ?: return@runCatching false

                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newUri(context.contentResolver, "image/jpeg", uri)
                cm.setPrimaryClip(clip)
                true
            }.getOrDefault(false)
        }

    private fun insertCacheViaFileProvider(context: Context, jpeg: ByteArray, nameHint: String): Uri? =
        runCatching {
            val dir = File(context.cacheDir, "clipboard").also { it.mkdirs() }
            val file = File(dir, "${sanitize(nameHint)}.jpg")
            file.writeBytes(jpeg)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull()

    private fun insertViaMediaStore(context: Context, jpeg: ByteArray, nameHint: String): Uri? =
        runCatching {
            val name = "${sanitize(nameHint)}_${System.currentTimeMillis()}.jpg"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/GaleriaPozAI/clipboard"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@runCatching null
            resolver.openOutputStream(uri)?.use { it.write(jpeg) }
                ?: return@runCatching null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            uri
        }.getOrNull()

    private fun loadBytes(context: Context, source: String): ByteArray = when {
        source.startsWith(ASSET_PREFIX) -> context.assets.open(source.removePrefix(ASSET_PREFIX)).use { it.readBytes() }
        source.startsWith("http://") || source.startsWith("https://") -> download(source)
        source.startsWith("content://") || source.startsWith("file://") ->
            context.contentResolver.openInputStream(Uri.parse(source))!!.use { it.readBytes() }
        else -> FileInputStream(File(source)).use { it.readBytes() }
    }

    private fun download(url: String): ByteArray {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20000
            readTimeout = 30000
            instanceFollowRedirects = true
        }
        conn.inputStream.use { return it.readBytes() }
    }

    private fun sanitize(s: String) = s.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(40).ifBlank { "pose" }
}
