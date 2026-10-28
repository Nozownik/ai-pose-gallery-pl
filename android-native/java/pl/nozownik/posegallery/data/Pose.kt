package pl.nozownik.posegallery.data

data class Pose(
    val id: String,
    val system: String,
    val poseNumber: String,
    val category: String,
    val poseName: String,
    val angle: String,
    val thumb: String,
    val full: String,
    val search: String = ""
) {
    fun thumbUrl(cdn: String = CDN): String = cdn + thumb.removePrefix("./")
    fun fullUrl(cdn: String = CDN): String = cdn + full.removePrefix("./")

    /** English prompt tags: pose name + angle + category */
    fun promptTags(): String = listOf(poseName, angle, category, system)
        .filter { it.isNotBlank() }
        .joinToString(", ")

    companion object {
        const val CDN = "https://ai-pose-gallery.pages.dev/"
    }
}

data class PoseGroup(
    val key: String,
    val system: String,
    val number: String,
    val name: String,
    val category: String,
    val items: List<Pose>,
    val byAngle: Map<String, Pose>
) {
    val primary: Pose get() = items.first()
}
