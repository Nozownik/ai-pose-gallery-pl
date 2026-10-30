package pl.nozownik.posegallery.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import pl.nozownik.posegallery.data.AppLanguage
import pl.nozownik.posegallery.data.Costume
import pl.nozownik.posegallery.data.CostumePrompts

data class CS(
    val tab: String,
    val title: String,
    val count: String,
    val searchHint: String,
    val all: String,
    val favs: String,
    val noResults: String,
    val noFavs: String,
    val hint: String,
    val copyDesc: String,
    val copyDescHint: String,
    val copyImage: String,
    val saveOriginal: String,
    val openSite: String,
    val category: String,
    val id: String,
    val keywords: String,
    val prompts: String,
    val promptsHint: String,
    val withPose: String,
    val copyPrompt: String,
    val terms: String,
    val swipeHint: String,
    val saveCustom: String,
    val genPrompt: String,
    val customTerms: String,
    val offlineBadge: String,
    val addOutfit: String,
    val addOutfitTitle: String,
    val pickImage: String,
    val pickImageHint: String,
    val outfitName: String,
    val outfitDesc: String,
    val outfitDescHint: String,
    val saveOutfit: String,
    val saving: String,
    val needImage: String,
    val mineTerms: String,
    val noMine: String,
    val edit: String,
    val delete: String,
    val deleteConfirm: String,
    val cancel: String,
    val save: String,
    val saveMine: String,
    val added: String,
    val addFail: String,
    val updated: String,
    val deleted: String,
    val cats: Map<String, String>,
    val isPl: Boolean
)

fun costumeStrings(lang: AppLanguage): CS = if (lang == AppLanguage.EN) CS(
    tab = "Outfits",
    title = "Outfits",
    count = "%d outfits",
    searchHint = "Search: ID or word, e.g. denim, black",
    all = "All",
    favs = "♥ Favorites",
    noResults = "No outfits found",
    noFavs = "No favorite outfits yet",
    hint = "Change the category or search words.",
    copyDesc = "Copy outfit description (EN)",
    copyDescHint = "Prompt text stays in English",
    copyImage = "Copy image",
    saveOriginal = "Save original (PNG)",
    openSite = "Open on source site",
    category = "Category",
    id = "ID",
    keywords = "Keywords",
    prompts = "Outfit prompts",
    promptsHint = "Attach the outfit image as a reference in the AI tool and paste the prompt.",
    withPose = "Outfit + pose (paste pose tags after)",
    copyPrompt = "Copy prompt",
    terms = "Garment-only AI images by ギア (@giaaibusi). Free to use as a reference, one image at a time. Redistribution, resale and bulk downloading of the images are prohibited. Images load from the original site.",
    swipeHint = "Swipe left/right: next / previous outfit",
    saveCustom = "Save image (WEBP)",
    genPrompt = "Generation prompt (EN)",
    customTerms = "Custom: own AI-generated garment images (no people) made for Galeria poz AI. Bundled offline in the app.",
    offlineBadge = "OFFLINE",
    addOutfit = "Add your outfit",
    addOutfitTitle = "Add your outfit",
    pickImage = "Tap to pick an outfit photo",
    pickImageHint = "System photo picker - the app gets only the photo you choose (no extra permissions).",
    outfitName = "Outfit name",
    outfitDesc = "Keywords / short description (EN works best)",
    outfitDescHint = "e.g. black satin slip dress, lace trim, strappy heels",
    saveOutfit = "Save outfit",
    saving = "Saving…",
    needImage = "Pick a photo first",
    mineTerms = "Your own outfit - saved only on this phone (app storage). Attach this photo as the reference image in the AI tool and paste a prompt below.",
    noMine = "No own outfits yet - tap + to add one.",
    edit = "Edit",
    delete = "Delete",
    deleteConfirm = "Delete this outfit from the app?",
    cancel = "Cancel",
    save = "Save",
    saveMine = "Save image to gallery",
    added = "Outfit added",
    addFail = "Couldn't add the outfit",
    updated = "Outfit updated",
    deleted = "Outfit deleted",
    cats = mapOf(
        "mine" to "Mine", "custom" to "Custom", "spring" to "Spring", "summer" to "Summer", "autumn" to "Autumn", "winter" to "Winter",
        "lingerie" to "Lingerie", "party" to "Party", "swimwear" to "Swimwear", "sexy" to "Sexy"
    ),
    isPl = false
) else CS(
    tab = "Ubrania",
    title = "Ubrania",
    count = "%d strojów",
    searchHint = "Szukaj: ID lub słowo, np. denim, czarny",
    all = "Wszystkie",
    favs = "♥ Ulubione",
    noResults = "Brak strojów",
    noFavs = "Nie masz jeszcze ulubionych strojów",
    hint = "Zmień kategorię albo słowa wyszukiwania.",
    copyDesc = "Kopiuj opis stroju (EN)",
    copyDescHint = "Tekst promptu zostaje po angielsku",
    copyImage = "Kopiuj obraz",
    saveOriginal = "Zapisz oryginał (PNG)",
    openSite = "Otwórz na stronie źródłowej",
    category = "Kategoria",
    id = "ID",
    keywords = "Słowa kluczowe",
    prompts = "Prompty stroju",
    promptsHint = "Dodaj obraz stroju jako referencję w narzędziu AI i wklej prompt.",
    withPose = "Strój + poza (dopisz tagi pozy na końcu)",
    copyPrompt = "Kopiuj prompt",
    terms = "Obrazy samych ubrań (bez ludzi), wygenerowane AI przez ギア (@giaaibusi). Darmowe jako referencja, pojedynczo. Zakaz redystrybucji, odsprzedaży i masowego pobierania obrazów. Obrazy ładują się z oryginalnej strony.",
    swipeHint = "Przesuń w lewo/prawo: następny / poprzedni strój",
    saveCustom = "Zapisz obraz (WEBP)",
    genPrompt = "Prompt generowania (EN)",
    customTerms = "Custom: własne obrazy ubrań wygenerowane AI (bez ludzi) dla Galerii poz AI. Wbudowane offline w aplikację.",
    offlineBadge = "OFFLINE",
    addOutfit = "Dodaj swój ciuch",
    addOutfitTitle = "Dodaj swój ciuch",
    pickImage = "Dotknij, aby wybrać zdjęcie stroju",
    pickImageHint = "Systemowy wybór zdjęć - aplikacja dostaje tylko wybrane zdjęcie (bez dodatkowych uprawnień).",
    outfitName = "Nazwa stroju",
    outfitDesc = "Słowa kluczowe / krótki opis (najlepiej po angielsku)",
    outfitDescHint = "np. black satin slip dress, lace trim, strappy heels",
    saveOutfit = "Zapisz strój",
    saving = "Zapisywanie…",
    needImage = "Najpierw wybierz zdjęcie",
    mineTerms = "Twój własny strój - zapisany tylko na tym telefonie (pamięć aplikacji). Dodaj to zdjęcie jako referencję w narzędziu AI i wklej prompt poniżej.",
    noMine = "Nie masz jeszcze własnych strojów - dodaj przyciskiem +.",
    edit = "Edytuj",
    delete = "Usuń",
    deleteConfirm = "Usunąć ten strój z aplikacji?",
    cancel = "Anuluj",
    save = "Zapisz",
    saveMine = "Zapisz obraz w galerii",
    added = "Dodano strój",
    addFail = "Nie udało się dodać stroju",
    updated = "Zapisano zmiany",
    deleted = "Usunięto strój",
    cats = mapOf(
        "mine" to "Moje", "custom" to "Custom", "spring" to "Wiosna", "summer" to "Lato", "autumn" to "Jesień", "winter" to "Zima",
        "lingerie" to "Bielizna", "party" to "Impreza", "swimwear" to "Stroje kąpielowe", "sexy" to "Seksowne"
    ),
    isPl = true
)

@Composable
fun CostumeGalleryScreen(
    state: CostumeUiState,
    vm: CostumeViewModel,
    cs: CS,
    contentPadding: PaddingValues,
    onOpen: (Costume) -> Unit
) {
    val c = LocalPoseColors.current
    val gridState = rememberLazyGridState()
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = c.surface2,
        labelColor = c.textMuted,
        selectedContainerColor = c.accentSoft,
        selectedLabelColor = c.accent
    )
    LaunchedEffect(state.scrollTick) {
        if (state.scrollTick > 0L) gridState.scrollToItem(0)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .statusBarsPadding()
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(cs.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = c.textMain)
            Text(cs.count.format(state.visible.size), color = c.textMuted, fontSize = 12.sp)
        }
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::setQuery,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text(cs.searchHint) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = c.textMuted) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { vm.setQuery("") }) { Icon(Icons.Default.Close, null, tint = c.textMuted) }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = costumeFieldColors(c)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.category.isEmpty(),
                onClick = { vm.setCategory("") },
                label = { Text("${cs.all} ${state.all.size}") },
                colors = chipColors
            )
            FilterChip(
                selected = state.category == "fav",
                onClick = { vm.setCategory("fav") },
                label = { Text("${cs.favs} ${state.favorites.size}") },
                colors = chipColors
            )
            Costume.CATEGORY_ORDER.filter { it == Costume.MINE || (state.counts[it] ?: 0) > 0 }.forEach { key ->
                FilterChip(
                    selected = state.category == key,
                    onClick = { vm.setCategory(key) },
                    label = { Text("${cs.cats[key] ?: key} ${state.counts[key] ?: 0}", maxLines = 1) },
                    colors = chipColors
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error, color = c.textMuted)
            }
            state.visible.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(when (state.category) { "fav" -> cs.noFavs; Costume.MINE -> cs.noMine; else -> cs.noResults }, color = c.textMain, fontWeight = FontWeight.SemiBold)
                    Text(cs.hint, color = c.textMuted, fontSize = 13.sp)
                }
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.visible, key = { it.id }) { item ->
                    CostumeCard(
                        item = item,
                        catLabel = cs.cats[item.cat] ?: item.cat,
                        isPl = cs.isPl,
                        favorited = item.id in state.favorites,
                        onClick = { onOpen(item) },
                        onToggleFav = { vm.toggleFavorite(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CostumeCard(
    item: Costume,
    catLabel: String,
    isPl: Boolean,
    favorited: Boolean,
    onClick: () -> Unit,
    onToggleFav: () -> Unit
) {
    val c = LocalPoseColors.current
    Column(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.thumbUrl()).crossfade(true).build(),
                contentDescription = item.id,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f).background(c.surface2)
            )
            Text(
                item.id,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC0B0D12))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onToggleFav, modifier = Modifier.align(Alignment.TopEnd).size(40.dp)) {
                Icon(
                    if (favorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null,
                    tint = if (favorited) Color(0xFFFF5A7A) else Color.White
                )
            }
        }
        Column(Modifier.padding(10.dp)) {
            Text(
                if (item.isCustom || item.isMine) customName(item, isPl, catLabel) else item.tokens().joinToString(" · "),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = c.textMain,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Text(catLabel, color = c.accent, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
fun CostumeDetailScreen(
    item: Costume,
    isFavorite: Boolean,
    cs: CS,
    vm: CostumeViewModel,
    onBack: () -> Unit,
    onSwipe: (Int) -> Unit
) {
    val c = LocalPoseColors.current
    val context = LocalContext.current
    var dragAcc by remember { mutableFloatStateOf(0f) }
    var editing by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    if (editing && item.isMine) {
        EditOutfitDialog(item, cs, onDismiss = { editing = false }) { n, k ->
            vm.updateMine(item.id, n, k); editing = false
        }
    }
    if (confirmDelete && item.isMine) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            text = { Text(cs.deleteConfirm) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.deleteMine(item.id); onBack() }) { Text(cs.delete, color = Color(0xFFFF5A7A)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(cs.cancel) } }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textMain) }
            Column(Modifier.weight(1f)) {
                Text(item.id, color = c.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (item.isCustom || item.isMine) customName(item, cs.isPl, cs.title) else (cs.cats[item.cat] ?: item.cat),
                    color = c.textMain, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            if (item.isMine) {
                IconButton(onClick = { editing = true }) { Icon(Icons.Default.Edit, cs.edit, tint = c.textMuted) }
                IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, cs.delete, tint = c.textMuted) }
            }
            IconButton(onClick = { vm.saveImage(item) }) { Icon(Icons.Default.Download, cs.saveOriginal, tint = c.textMuted) }
            IconButton(onClick = { vm.toggleFavorite(item.id) }) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null,
                    tint = if (isFavorite) Color(0xFFFF5A7A) else c.textMuted
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1.35f)
                .background(c.surface)
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragAcc < -80f -> onSwipe(1)
                                dragAcc > 80f -> onSwipe(-1)
                            }
                            dragAcc = 0f
                        },
                        onHorizontalDrag = { _, dx -> dragAcc += dx }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context).data(item.imageUrl()).crossfade(true).build(),
                contentDescription = item.id,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(4.dp),
                loading = {
                    // show the small thumb while the larger image loads
                    AsyncImage(model = item.thumbUrl(), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                }
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 340.dp)
                .background(c.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(cs.swipeHint, color = c.textMuted, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            CMetaRow(cs.category, cs.cats[item.cat] ?: item.cat)
            CMetaRow(cs.id, item.id)
            if (item.isMine) CMetaRow(cs.outfitName, item.nameEn.orEmpty())
            if (!item.isMine || item.kw.isNotBlank()) CMetaRow(cs.keywords, if (item.isMine) item.kw else item.tokens().joinToString(", "))
            if (item.isCustom && !item.kwPl.isNullOrBlank()) CMetaRow("PL", item.tokensPl().joinToString(", "))
            if (item.isCustom) CMetaRow("", cs.offlineBadge)
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { vm.copyText(item.promptText()) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = Color.White)
            ) {
                Icon(Icons.Default.ContentCopy, null)
                Spacer(Modifier.size(8.dp))
                Text(cs.copyDesc, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text(item.promptText(), color = c.textMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { vm.copyImage(item) }, modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.ContentCopy, null, tint = c.accent)
                Spacer(Modifier.size(8.dp))
                Text(cs.copyImage, color = c.accent, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = { vm.saveImage(item) }, modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Download, null, tint = c.accent)
                Spacer(Modifier.size(8.dp))
                Text(when { item.isMine -> cs.saveMine; item.isCustom -> cs.saveCustom; else -> cs.saveOriginal }, color = c.accent, fontWeight = FontWeight.SemiBold)
            }
            if (item.isOnline) {
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Costume.SITE))) },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Link, null, tint = c.accent)
                    Spacer(Modifier.size(8.dp))
                    Text(cs.openSite, color = c.accent, fontWeight = FontWeight.SemiBold)
                }
            }

            // Outfit prompt cards: only for the user's own outfits ("Moje").
            if (item.isMine) {
                Spacer(Modifier.height(16.dp))
                OutfitPromptCards(item, cs) { vm.copyText(it) }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                when { item.isMine -> cs.mineTerms; item.isCustom -> cs.customTerms; else -> cs.terms },
                color = c.textMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, c.line, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun OutfitPromptCards(item: Costume, cs: CS, onCopy: (String) -> Unit) {
    val c = LocalPoseColors.current
    Text(cs.prompts, color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Text(cs.promptsHint, color = c.textMuted, fontSize = 11.sp)
    Spacer(Modifier.height(6.dp))
    CPromptCard("Nano Banana (Gemini)", CostumePrompts.nanoBanana(item), cs.copyPrompt, onCopy)
    CPromptCard("ChatGPT", CostumePrompts.chatGpt(item), cs.copyPrompt, onCopy)
    CPromptCard("Grok Imagine", CostumePrompts.grokImagine(item), cs.copyPrompt, onCopy)
    CPromptCard(cs.withPose, CostumePrompts.withPose(item), cs.copyPrompt, onCopy)
}

@Composable
private fun EditOutfitDialog(item: Costume, cs: CS, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    val c = LocalPoseColors.current
    var name by remember(item.id) { mutableStateOf(item.nameEn.orEmpty()) }
    var kw by remember(item.id) { mutableStateOf(item.kw) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(cs.edit) },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text(cs.outfitName) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = costumeFieldColors(c))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(kw, { kw = it }, label = { Text(cs.outfitDesc) }, minLines = 2,
                    modifier = Modifier.fillMaxWidth(), colors = costumeFieldColors(c))
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, kw) }) { Text(cs.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(cs.cancel) } }
    )
}

/** Add-your-own-outfit flow: photo picker → name/description → live prompt cards → save. */
@Composable
fun AddOutfitScreen(cs: CS, vm: CostumeViewModel, onBack: () -> Unit, onSaved: (String?) -> Unit) {
    val c = LocalPoseColors.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var kw by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var showNeed by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { imageUri = uri; showNeed = false }
    }
    val launch = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    val preview = Costume(id = "MOJE-NEW", cat = Costume.MINE, kw = kw.trim(), nameEn = name.trim(), namePl = name.trim())

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textMain) }
            Text(cs.addOutfitTitle, color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surface)
                    .border(1.dp, if (showNeed) Color(0xFFFF5A7A) else c.line, RoundedCornerShape(14.dp))
                    .clickable { launch() },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = c.accent, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(cs.pickImage, color = c.textMain, fontWeight = FontWeight.SemiBold)
                        Text(cs.pickImageHint, color = c.textMuted, fontSize = 11.sp)
                    }
                }
            }
            if (showNeed) Text(cs.needImage, color = Color(0xFFFF5A7A), fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(cs.outfitName) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = costumeFieldColors(c)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = kw, onValueChange = { kw = it },
                label = { Text(cs.outfitDesc) }, placeholder = { Text(cs.outfitDescHint) }, minLines = 2,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = costumeFieldColors(c)
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    val uri = imageUri
                    if (uri == null) { showNeed = true; return@Button }
                    saving = true
                    vm.addMine(uri, name, kw) { id -> saving = false; onSaved(id) }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = Color.White)
            ) { Text(if (saving) cs.saving else cs.saveOutfit, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(18.dp))
            OutfitPromptCards(preview, cs) { vm.copyText(it) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CPromptCard(title: String, prompt: String, copyLabel: String, onCopy: (String) -> Unit) {
    val c = LocalPoseColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface2)
            .padding(12.dp)
    ) {
        Text(title, color = c.accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(prompt, color = c.textMuted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp), maxLines = 6, overflow = TextOverflow.Ellipsis)
        TextButton(onClick = { onCopy(prompt) }, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.Default.ContentCopy, null, tint = c.accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(6.dp))
            Text(copyLabel, color = c.accent, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CMetaRow(label: String, value: String) {
    val c = LocalPoseColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, Modifier.weight(0.35f), color = c.textMuted, fontSize = 12.sp)
        Text(value, Modifier.weight(0.65f), color = c.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun costumeFieldColors(c: PoseColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = c.accent,
    unfocusedBorderColor = c.line,
    focusedContainerColor = c.surface,
    unfocusedContainerColor = c.surface,
    focusedTextColor = c.textMain,
    unfocusedTextColor = c.textMain,
    cursorColor = c.accent,
    focusedPlaceholderColor = c.textMuted,
    unfocusedPlaceholderColor = c.textMuted
)

private fun customName(item: Costume, pl: Boolean, fallback: String): String =
    (if (pl) item.namePl else item.nameEn) ?: item.nameEn ?: item.namePl ?: fallback
