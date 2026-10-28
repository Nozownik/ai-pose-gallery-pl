package pl.nozownik.posegallery.data

object StylePrompts {
    /** Convert a normal photo into the gallery's clean pose-reference look. */
    fun nanoBanana(pose: Pose): String = buildString {
        append("Convert this photo into a clean AI pose-reference image. ")
        append("Full-body figure matching pose: ${pose.poseName}. ")
        append("Camera angle: ${pose.angle}. Category: ${pose.category}. ")
        append("Style: sharp silhouette, soft even studio lighting, plain light/white background, ")
        append("neutral clothing, clear limb articulation, no face detail focus, no text, no watermark. ")
        append("Keep the same body proportions and pose from the source photo; simplify into a pose board reference.")
    }

    fun chatGpt(pose: Pose): String = buildString {
        append("Using this photo as the pose reference, generate a photorealistic pose-reference sheet image. ")
        append("Reproduce the exact body pose: ${pose.poseName} (${pose.angle} view, ${pose.category}). ")
        append("Clean simple background (soft white or light gray), even lighting, full figure visible, ")
        append("neutral outfit, high clarity of joints and silhouette, product-photo quality, ")
        append("no logos, no captions, no extra people.")
    }

    fun grokImagine(pose: Pose): String = buildString {
        append("Remix this photo into a clean pose-reference still for AI artists. ")
        append("Pose: ${pose.poseName}. Angle: ${pose.angle}. Type: ${pose.category} / ${pose.system}. ")
        append("Plain studio backdrop, soft key light, full body in frame, clear silhouette, ")
        append("minimal clothing details, no text overlays, cinematic but neutral, photoreal.")
    }
}
