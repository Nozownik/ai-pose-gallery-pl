package pl.nozownik.posegallery.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Costume / outfit reference (garment-only images, no people) from
 * COSTUME-DB-01 by ギア (@giaaibusi): https://ai-costume-gallery.pages.dev/
 *
 * Only metadata (id, category, English keywords) is bundled. Images are NOT
 * bundled or bulk-prefetched: the author forbids redistribution and bulk
 * downloading, so they load on demand one by one from the original site.
 */
data class Costume(
    val id: String,
    val cat: String,
    val kw: String,
    /** Own offline items (category "custom") only: */
    val nameEn: String? = null,
    val namePl: String? = null,
    val kwPl: String? = null,
    val prompt: String? = null,
    /** User-added outfits (category "mine") only: absolute path in app storage. */
    val localPath: String? = null
) {
    val isCustom: Boolean get() = cat == CUSTOM
    val isMine: Boolean get() = cat == MINE
    val isOnline: Boolean get() = !isCustom && !isMine

    fun thumbUrl(): String = when {
        isMine -> localPath.orEmpty()
        isCustom -> "${ASSET}custom/thumbs/$id.webp"
        else -> "${CDN}thumbs/$id.webp"
    }
    fun imageUrl(): String = when {
        isMine -> localPath.orEmpty()
        isCustom -> "${ASSET}custom/img/$id.webp"
        else -> "${CDN}img/$id.webp"
    }
    fun originalUrl(): String = if (isOnline) "${CDN}png/$id.png" else imageUrl()

    /** Text describing the outfit for prompts. */
    fun describe(): String = if (isMine) {
        listOf(nameEn.orEmpty().trim(), kw.trim()).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "the outfit" }
    } else kw

    fun tokens(): List<String> = kw.split(' ').filter { it.isNotBlank() }
    fun tokensPl(): List<String> = kwPl.orEmpty().split(' ').filter { it.isNotBlank() }
    fun searchHay(): String = listOf(id, kw, kwPl.orEmpty(), nameEn.orEmpty(), namePl.orEmpty()).joinToString(" ").lowercase()

    /** English outfit description to paste into a prompt. */
    fun promptText(): String = "outfit: ${describe()}"

    companion object {
        const val CDN = "https://ai-costume-gallery.pages.dev/"
        const val SITE = CDN
        const val ASSET = "file:///android_asset/"
        const val CUSTOM = "custom"
        const val MINE = "mine"

        /** Display order (same as the source site). */
        val CATEGORY_ORDER = listOf(MINE, CUSTOM, "spring", "summer", "autumn", "winter", "lingerie", "party", "swimwear", "sexy")
    }
}

class CostumeRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("costume_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _favorites = MutableStateFlow(prefs.getStringSet("ids", emptySet())?.toSet() ?: emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    suspend fun load(): List<Costume> = withContext(Dispatchers.IO) {
        val type = object : TypeToken<List<Costume>>() {}.type
        val custom: List<Costume> = runCatching {
            context.assets.open("custom/custom.json").bufferedReader().use { r ->
                gson.fromJson<List<Costume>>(r, type).map { it.copy(cat = Costume.CUSTOM) }
            }
        }.getOrDefault(emptyList())
        val online: List<Costume> = context.assets.open("data/costumes.json").bufferedReader().use { r ->
            gson.fromJson<List<Costume>>(r, type)
        }
        custom + online
    }

    fun toggleFavorite(id: String) {
        val next = _favorites.value.toMutableSet()
        if (!next.add(id)) next.remove(id)
        prefs.edit().putStringSet("ids", next).apply()
        _favorites.value = next
    }
}

object CostumePrompts {
    fun nanoBanana(c: Costume) =
        "Use the attached image as the exact clothing reference. Generate a full-body fashion photo of an adult model " +
            "wearing exactly this outfit: ${c.describe()}. Match colors, fabrics, cut, layering and accessories precisely, " +
            "with a natural fit and realistic fabric folds. Neutral standing pose, plain light studio background, " +
            "soft even lighting, photorealistic, no text, no watermark."

    fun chatGpt(c: Costume) =
        "Create a photorealistic full-body lookbook photo of an adult model wearing the outfit shown in the attached " +
            "reference image (${c.describe()}). Reproduce every garment and accessory faithfully: colors, materials, " +
            "silhouette and details. Clean light-gray background, even studio lighting, full figure in frame, " +
            "no logos, no captions, no extra people."

    fun grokImagine(c: Costume) =
        "Photoreal full-body editorial shot of an adult model dressed in this outfit: ${c.describe()}. " +
            "Treat the attached image as the exact wardrobe reference and copy it 1:1. Minimal studio backdrop, " +
            "soft key light, relaxed natural pose, sharp fabric detail, no text overlays."

    /** Combine with a pose from the main gallery: paste pose tags after this. */
    fun withPose(c: Costume) =
        "Adult character wearing: ${c.describe()}. Use the attached outfit image as the clothing reference and the attached " +
            "pose image as the pose reference. Pose: "
}
