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
    val search: String? = "",
    val isCustom: Boolean = false,
    val localPath: String? = null
) {
    private val resolvedLocal: String get() = localPath.orEmpty()

    fun thumbUrl(cdn: String = CDN): String =
        if (isCustom && resolvedLocal.isNotBlank()) resolvedLocal
        else cdn + thumb.removePrefix("./")

    fun fullUrl(cdn: String = CDN): String =
        if (isCustom && resolvedLocal.isNotBlank()) resolvedLocal
        else cdn + full.removePrefix("./")

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
    val byAngle: Map<String, Pose>,
    val isCustom: Boolean = false
) {
    val primary: Pose get() = items.first()
}
