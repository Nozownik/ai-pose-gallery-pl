package pl.nozownik.posegallery.data

object StylePrompts {
    fun nanoBanana(poseName: String, angle: String, category: String, system: String = ""): String = buildString {
        append("Convert this photo into a clean AI pose-reference image. ")
        if (poseName.isNotBlank()) append("Full-body figure matching pose: $poseName. ")
        if (angle.isNotBlank()) append("Camera angle: $angle. ")
        if (category.isNotBlank()) append("Category: $category. ")
        append("Style: sharp silhouette, soft even studio lighting, plain light/white background, ")
        append("neutral clothing, clear limb articulation, no face detail focus, no text, no watermark. ")
        append("Keep the same body proportions and pose from the source photo; simplify into a pose board reference.")
    }

    fun chatGpt(poseName: String, angle: String, category: String, system: String = ""): String = buildString {
        append("Using this photo as the pose reference, generate a photorealistic pose-reference sheet image. ")
        append("Reproduce the exact body pose")
        if (poseName.isNotBlank()) append(": $poseName")
        if (angle.isNotBlank() || category.isNotBlank()) {
            append(" (")
            append(listOf(angle.takeIf { it.isNotBlank() }?.let { "$it view" }, category.takeIf { it.isNotBlank() }).filterNotNull().joinToString(", "))
            append(")")
        }
        append(". ")
        append("Clean simple background (soft white or light gray), even lighting, full figure visible, ")
        append("neutral outfit, high clarity of joints and silhouette, product-photo quality, ")
        append("no logos, no captions, no extra people.")
    }

    fun grokImagine(poseName: String, angle: String, category: String, system: String = ""): String = buildString {
        append("Remix this photo into a clean pose-reference still for AI artists. ")
        if (poseName.isNotBlank()) append("Pose: $poseName. ")
        if (angle.isNotBlank()) append("Angle: $angle. ")
        if (category.isNotBlank() || system.isNotBlank()) {
            append("Type: ${listOf(category, system).filter { it.isNotBlank() }.joinToString(" / ")}. ")
        }
        append("Plain studio backdrop, soft key light, full body in frame, clear silhouette, ")
        append("minimal clothing details, no text overlays, cinematic but neutral, photoreal.")
    }

    fun nanoBanana(pose: Pose) = nanoBanana(pose.poseName, pose.angle, pose.category, pose.system)
    fun chatGpt(pose: Pose) = chatGpt(pose.poseName, pose.angle, pose.category, pose.system)
    fun grokImagine(pose: Pose) = grokImagine(pose.poseName, pose.angle, pose.category, pose.system)
}
