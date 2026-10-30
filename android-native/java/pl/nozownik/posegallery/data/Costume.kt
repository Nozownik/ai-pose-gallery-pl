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
    val kw: String
) {
    fun thumbUrl(): String = "${CDN}thumbs/$id.webp"
    fun imageUrl(): String = "${CDN}img/$id.webp"
    fun originalUrl(): String = "${CDN}png/$id.png"

    fun tokens(): List<String> = kw.split(' ').filter { it.isNotBlank() }

    /** English outfit description to paste into a prompt. */
    fun promptText(): String = "outfit: $kw"

    companion object {
        const val CDN = "https://ai-costume-gallery.pages.dev/"
        const val SITE = CDN

        /** Display order (same as the source site). */
        val CATEGORY_ORDER = listOf("spring", "summer", "autumn", "winter", "lingerie", "party", "swimwear", "sexy")
    }
}

class CostumeRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("costume_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _favorites = MutableStateFlow(prefs.getStringSet("ids", emptySet())?.toSet() ?: emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    suspend fun load(): List<Costume> = withContext(Dispatchers.IO) {
        context.assets.open("data/costumes.json").bufferedReader().use { r ->
            val type = object : TypeToken<List<Costume>>() {}.type
            gson.fromJson<List<Costume>>(r, type)
        }
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
            "wearing exactly this outfit: ${c.kw}. Match colors, fabrics, cut, layering and accessories precisely, " +
            "with a natural fit and realistic fabric folds. Neutral standing pose, plain light studio background, " +
            "soft even lighting, photorealistic, no text, no watermark."

    fun chatGpt(c: Costume) =
        "Create a photorealistic full-body lookbook photo of an adult model wearing the outfit shown in the attached " +
            "reference image (${c.kw}). Reproduce every garment and accessory faithfully: colors, materials, " +
            "silhouette and details. Clean light-gray background, even studio lighting, full figure in frame, " +
            "no logos, no captions, no extra people."

    fun grokImagine(c: Costume) =
        "Photoreal full-body editorial shot of an adult model dressed in this outfit: ${c.kw}. " +
            "Treat the attached image as the exact wardrobe reference and copy it 1:1. Minimal studio backdrop, " +
            "soft key light, relaxed natural pose, sharp fabric detail, no text overlays."

    /** Combine with a pose from the main gallery: paste pose tags after this. */
    fun withPose(c: Costume) =
        "Adult character wearing: ${c.kw}. Use the attached outfit image as the clothing reference and the attached " +
            "pose image as the pose reference. Pose: "
}
