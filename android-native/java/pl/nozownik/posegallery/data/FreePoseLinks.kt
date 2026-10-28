package pl.nozownik.posegallery.data

data class FreePoseLink(
    val title: String,
    val url: String,
    val notePl: String,
    val noteEn: String
)

object FreePoseLinks {
    val all = listOf(
        FreePoseLink(
            "Line of Action",
            "https://line-of-action.com/",
            "Darmowe sesje poz i gestów (timed practice).",
            "Free timed pose & gesture practice sessions."
        ),
        FreePoseLink(
            "Quickposes",
            "https://quickposes.com/",
            "Szybkie ćwiczenia poz, darmowe.",
            "Fast pose drills, free to use."
        ),
        FreePoseLink(
            "PoseMy.Art",
            "https://posemy.art/",
            "3D manekin poz — darmowy tier (bez konta płatnego).",
            "3D pose mannequin — free tier available."
        ),
        FreePoseLink(
            "AdorkaStock (free)",
            "https://www.adorkastock.com/",
            "Dużo darmowych stocków poz (sprawdź licencję).",
            "Many free pose stock photos (check license)."
        ),
        FreePoseLink(
            "SenshiStock (DA free)",
            "https://www.deviantart.com/senshistock",
            "Darmowe referencje poz na DeviantArt.",
            "Free pose references on DeviantArt."
        ),
        FreePoseLink(
            "SketchDaily Reference",
            "https://reference.sketchdaily.net/",
            "Losowe referencje do rysowania, free.",
            "Random drawing references, free."
        ),
        FreePoseLink(
            "Character Designs — Poses",
            "https://www.characterdesigns.com/poses",
            "Galerie poz (publiczne podglądy).",
            "Pose galleries (public previews)."
        ),
        FreePoseLink(
            "Croquis Cafe (archive info)",
            "https://www.youtube.com/@CroquisCafe",
            "Darmowe timed figure videos (YouTube).",
            "Free timed figure drawing videos (YouTube)."
        )
    )
}
