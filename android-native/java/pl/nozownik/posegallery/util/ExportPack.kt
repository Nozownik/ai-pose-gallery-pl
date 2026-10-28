package pl.nozownik.posegallery.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.nozownik.posegallery.data.Pose
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportPack {
    suspend fun exportCustomPoses(context: Context, poses: List<Pose>): File = withContext(Dispatchers.IO) {
        val custom = poses.filter { it.isCustom }
        val outDir = File(context.cacheDir, "export").also { it.mkdirs() }
        val zip = File(outDir, "galeria-poz-custom-${System.currentTimeMillis()}.zip")
        val gson = Gson()
        ZipOutputStream(BufferedOutputStream(zip.outputStream())).use { zos ->
            zos.putNextEntry(ZipEntry("poses.json"))
            zos.write(gson.toJson(custom).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
            custom.forEach { pose ->
                val path = pose.localPath ?: return@forEach
                val src = File(path)
                if (!src.exists()) return@forEach
                zos.putNextEntry(ZipEntry("images/${pose.id}.jpg"))
                FileInputStream(src).use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        zip
    }

    fun shareZip(context: Context, zip: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zip)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export"))
    }
}
