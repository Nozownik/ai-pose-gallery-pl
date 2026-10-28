package pl.nozownik.posegallery.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import pl.nozownik.posegallery.data.AppLanguage
import pl.nozownik.posegallery.data.AppThemeId
import pl.nozownik.posegallery.data.FreePoseLinks
import pl.nozownik.posegallery.data.Pose
import pl.nozownik.posegallery.data.PoseGroup
import pl.nozownik.posegallery.data.StylePrompts

@Composable
fun AppRoot(vm: PoseViewModel) {
    val state by vm.uiState.collectAsState()
    val s = strings(state.settings.language)
    val c = LocalPoseColors.current
    var selectedPoseId by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            val msg = when (ev) {
                "saved_ok" -> s.savedOk
                "saved_fail" -> s.savedFail
                "note_saved" -> s.noteSaved
                else -> null
            }
            if (msg != null) snack.showSnackbar(msg)
        }
    }

    if (showAdd) {
        BackHandler { showAdd = false }
        AddCustomScreen(
            vm = vm,
            strings = s,
            systems = state.systems,
            categories = state.categories.ifEmpty { listOf("Standing", "Sitting", "Lying", "Action", "Custom") },
            angles = state.angles.ifEmpty { listOf("Front", "Side", "Three-quarter", "High angle / top-down", "Low angle / looking up") },
            onBack = { showAdd = false },
            onSaved = { pose ->
                showAdd = false
                if (pose != null) selectedPoseId = pose.id
            }
        )
        return
    }

    if (selectedPoseId != null) {
        val group = vm.groupForId(selectedPoseId!!)
        val pose = vm.findPose(selectedPoseId!!)
        if (pose != null && group != null) {
            BackHandler { selectedPoseId = null }
            Box(Modifier.fillMaxSize()) {
                DetailScreen(
                    pose = pose,
                    group = group,
                    isFavorite = group.items.any { it.id in state.favorites },
                    note = state.notes[pose.id].orEmpty(),
                    strings = s,
                    onBack = { selectedPoseId = null },
                    onSelectPose = { selectedPoseId = it.id },
                    onToggleFav = { vm.toggleFavorite(pose.id) },
                    onCopy = { text ->
                        copyText(context, text)
                        scope.launch { snack.showSnackbar(s.copied) }
                    },
                    onCopyId = {
                        copyText(context, pose.id)
                        scope.launch { snack.showSnackbar(s.idCopied) }
                    },
                    onSaveImage = { vm.saveImage(pose) },
                    onSaveNote = { vm.setNote(pose.id, it) },
                    onDeleteCustom = {
                        vm.deleteCustom(pose.id)
                        selectedPoseId = null
                    },
                    onSwipeGroup = { dir ->
                        val list = state.visibleGroups
                        val idx = list.indexOfFirst { it.key == group.key }
                        val next = list.getOrNull(idx + dir) ?: return@DetailScreen
                        val keep = pose.angle
                        selectedPoseId = (next.byAngle[keep] ?: next.primary).id
                    }
                )
                SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
            }
            return
        }
    }

    Scaffold(
        containerColor = c.bg,
        snackbarHost = { SnackbarHost(snack) },
        floatingActionButton = {
            if (state.filters.tab == 0) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { showAdd = true },
                    containerColor = c.accent,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, s.addPose)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = c.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = c.accent,
                    selectedTextColor = c.accent,
                    indicatorColor = c.accentSoft,
                    unselectedIconColor = c.textMuted,
                    unselectedTextColor = c.textMuted
                )
                NavigationBarItem(
                    selected = state.filters.tab == 0,
                    onClick = { vm.setTab(0) },
                    icon = { Icon(Icons.Default.GridView, null) },
                    label = { Text(s.gallery) },
                    colors = colors
                )
                NavigationBarItem(
                    selected = state.filters.tab == 1,
                    onClick = { vm.setTab(1) },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text(s.favorites) },
                    colors = colors
                )
                NavigationBarItem(
                    selected = state.filters.tab == 2,
                    onClick = { vm.setTab(2) },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text(s.settings) },
                    colors = colors
                )
            }
        }
    ) { pad ->
        when (state.filters.tab) {
            2 -> SettingsScreen(state = state, vm = vm, strings = s, contentPadding = pad)
            else -> GalleryScreen(
                state = state,
                vm = vm,
                strings = s,
                contentPadding = pad,
                onOpen = { g ->
                    val ang = state.filters.angle
                    val pose = if (ang.isNotEmpty()) g.byAngle[ang] ?: g.primary else g.primary
                    selectedPoseId = pose.id
                }
            )
        }
    }
}

@Composable
fun GalleryScreen(
    state: GalleryUiState,
    vm: PoseViewModel,
    strings: S,
    contentPadding: PaddingValues,
    onOpen: (PoseGroup) -> Unit
) {
    val c = LocalPoseColors.current
    var showFilters by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = c.surface2,
        labelColor = c.textMuted,
        selectedContainerColor = c.accentSoft,
        selectedLabelColor = c.accent
    )

    LaunchedEffect(state.scrollToTopTick) {
        if (state.scrollToTopTick > 0L) {
            gridState.animateScrollToItem(0)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(strings.appTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = c.textMain)
                Text(
                    if (state.filters.tab == 1) strings.favoritesSubtitle
                    else strings.groupsCount.format(state.visibleGroups.size),
                    color = c.textMuted,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Default.FilterList, strings.filters, tint = if (showFilters) c.accent else c.textMuted)
            }
        }

        OutlinedTextField(
            value = state.filters.query,
            onValueChange = vm::setQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(strings.searchHint) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = c.textMuted) },
            trailingIcon = {
                if (state.filters.query.isNotEmpty()) {
                    IconButton(onClick = { vm.setQuery("") }) {
                        Icon(Icons.Default.Close, null, tint = c.textMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(c)
        )

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.filters.system.isEmpty(),
                onClick = { vm.setSystem("") },
                label = { Text(strings.all) },
                colors = chipColors
            )
            state.systems.forEach { sys ->
                FilterChip(
                    selected = state.filters.system == sys,
                    onClick = { vm.setSystem(if (state.filters.system == sys) "" else sys) },
                    label = { Text(sys.substringAfterLast('·').trim().ifBlank { sys }, maxLines = 1) },
                    colors = chipColors
                )
            }
        }

        if (showFilters) {
            Spacer(Modifier.height(6.dp))
            Text(strings.category, Modifier.padding(horizontal = 16.dp), color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = state.filters.category.isEmpty(), onClick = { vm.setCategory("") }, label = { Text(strings.all) }, colors = chipColors)
                state.categories.forEach { cat ->
                    FilterChip(
                        selected = state.filters.category == cat,
                        onClick = { vm.setCategory(if (state.filters.category == cat) "" else cat) },
                        label = { Text(cat, maxLines = 1) },
                        colors = chipColors
                    )
                }
            }
            Text(strings.angle, Modifier.padding(horizontal = 16.dp), color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = state.filters.angle.isEmpty(), onClick = { vm.setAngle("") }, label = { Text(strings.all) }, colors = chipColors)
                state.angles.forEach { a ->
                    FilterChip(
                        selected = state.filters.angle == a,
                        onClick = { vm.setAngle(if (state.filters.angle == a) "" else a) },
                        label = { Text(shortAngle(a), maxLines = 1) },
                        colors = chipColors
                    )
                }
            }
            TextButton(onClick = { vm.resetFilters(); showFilters = false }, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(strings.resetFilters, color = c.accent)
            }
        }

        Spacer(Modifier.height(6.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: strings.loadingError, color = c.textMuted)
            }
            state.visibleGroups.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (state.filters.tab == 1) strings.noFavorites else strings.noResults,
                        color = c.textMain,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(strings.noResultsHint, color = c.textMuted, fontSize = 13.sp)
                    if (state.filters.tab == 0) {
                        TextButton(onClick = { vm.resetFilters() }) { Text(strings.clearFilters, color = c.accent) }
                    }
                }
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.visibleGroups, key = { it.key }) { group ->
                    val display = state.filters.angle.takeIf { it.isNotEmpty() }?.let { group.byAngle[it] } ?: group.primary
                    PoseCard(
                        group = group,
                        pose = display,
                        favorited = group.items.any { it.id in state.favorites },
                        customLabel = strings.customBadge,
                        onClick = { onOpen(group) },
                        onToggleFav = { vm.toggleFavorite(display.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PoseCard(
    group: PoseGroup,
    pose: Pose,
    favorited: Boolean,
    customLabel: String,
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
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pose.thumbUrl())
                    .crossfade(true)
                    .build(),
                contentDescription = group.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .background(c.surface2)
            )
            Text(
                if (group.isCustom) customLabel else group.number,
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
            IconButton(
                onClick = onToggleFav,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
            ) {
                Icon(
                    if (favorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (favorited) Color(0xFFFF5A7A) else Color.White
                )
            }
        }
        Column(Modifier.padding(10.dp)) {
            Text(
                group.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = c.textMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp
            )
            Text(group.category, color = c.accent, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    pose: Pose,
    group: PoseGroup,
    isFavorite: Boolean,
    note: String,
    strings: S,
    onBack: () -> Unit,
    onSelectPose: (Pose) -> Unit,
    onToggleFav: () -> Unit,
    onCopy: (String) -> Unit,
    onCopyId: () -> Unit,
    onSaveImage: () -> Unit,
    onSaveNote: (String) -> Unit,
    onDeleteCustom: () -> Unit,
    onSwipeGroup: (Int) -> Unit
) {
    val c = LocalPoseColors.current
    val context = LocalContext.current
    var dragAcc by remember { mutableFloatStateOf(0f) }
    var noteText by remember(pose.id, note) { mutableStateOf(note) }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.back, tint = c.textMain)
            }
            Column(Modifier.weight(1f)) {
                Text(pose.poseNumber, color = c.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(pose.poseName, color = c.textMain, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onSaveImage) {
                Icon(Icons.Default.Download, strings.saveImage, tint = c.textMuted)
            }
            IconButton(onClick = onToggleFav) {
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
                .weight(1f)
                .background(c.surface)
                .pointerInput(pose.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragAcc < -80f -> onSwipeGroup(1)
                                dragAcc > 80f -> onSwipeGroup(-1)
                            }
                            dragAcc = 0f
                        },
                        onHorizontalDrag = { _, dx -> dragAcc += dx }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(pose.fullUrl())
                    .crossfade(true)
                    .build(),
                contentDescription = pose.poseName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(c.surface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (group.items.size > 1) {
                Text(strings.angle, color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.items.forEach { p ->
                        val selected = p.id == pose.id
                        Column(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) c.accentSoft else c.surface2)
                                .clickable { onSelectPose(p) }
                                .padding(6.dp)
                                .size(width = 72.dp, height = 96.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = p.thumbUrl(),
                                contentDescription = p.angle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Text(shortAngle(p.angle), color = if (selected) c.accent else c.textMuted, fontSize = 9.sp, maxLines = 1)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            MetaRow(strings.system, pose.system)
            MetaRow(strings.category, pose.category)
            MetaRow(strings.angle, pose.angle)
            MetaRow(strings.id, pose.id)

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = { onCopy(pose.promptTags()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = Color.White)
            ) {
                Icon(Icons.Default.ContentCopy, null)
                Spacer(Modifier.size(8.dp))
                Text(strings.copy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(
                strings.copyIdLongPress,
                color = c.accent,
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .combinedClickable(onClick = {}, onLongClick = onCopyId)
            )
            Text(pose.promptTags(), color = c.textMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onSaveImage,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Download, null, tint = c.accent)
                Spacer(Modifier.size(8.dp))
                Text(strings.saveImage, color = c.accent, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(18.dp))
            Text(strings.annotation, color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(strings.annotationHint) },
                minLines = 2,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(c)
            )
            TextButton(onClick = { onSaveNote(noteText) }) {
                Text(strings.saveNote, color = c.accent)
            }

            Spacer(Modifier.height(12.dp))
            Text(strings.stylePrompts, color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(strings.styleHint, color = c.textMuted, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            PromptCard(strings.nanoBanana, StylePrompts.nanoBanana(pose), strings.copyPrompt, c) { onCopy(it) }
            PromptCard(strings.chatGpt, StylePrompts.chatGpt(pose), strings.copyPrompt, c) { onCopy(it) }
            PromptCard(strings.grokImagine, StylePrompts.grokImagine(pose), strings.copyPrompt, c) { onCopy(it) }

            if (pose.isCustom) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDeleteCustom,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5A7A))
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.size(8.dp))
                    Text(strings.deleteCustom)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PromptCard(title: String, prompt: String, copyLabel: String, c: PoseColors, onCopy: (String) -> Unit) {
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
fun SettingsScreen(
    state: GalleryUiState,
    vm: PoseViewModel,
    strings: S,
    contentPadding: PaddingValues
) {
    val c = LocalPoseColors.current
    val context = LocalContext.current
    var linksTab by remember { mutableStateOf(false) }
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = c.surface2,
        labelColor = c.textMuted,
        selectedContainerColor = c.accentSoft,
        selectedLabelColor = c.accent
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(strings.settings, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = c.textMain)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !linksTab, onClick = { linksTab = false }, label = { Text(strings.settings) }, colors = chipColors)
            FilterChip(selected = linksTab, onClick = { linksTab = true }, label = { Text(strings.links) }, colors = chipColors)
        }

        Spacer(Modifier.height(16.dp))

        if (!linksTab) {
            Text(strings.language, color = c.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.settings.language == AppLanguage.PL,
                    onClick = { vm.setLanguage(AppLanguage.PL) },
                    label = { Text("Polski") },
                    colors = chipColors
                )
                FilterChip(
                    selected = state.settings.language == AppLanguage.EN,
                    onClick = { vm.setLanguage(AppLanguage.EN) },
                    label = { Text("English") },
                    colors = chipColors
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(strings.theme, color = c.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val themes = listOf(
                AppThemeId.DARK to strings.themeDark,
                AppThemeId.AMOLED to strings.themeAmoled,
                AppThemeId.LIGHT to strings.themeLight,
                AppThemeId.PURPLE to strings.themePurple,
                AppThemeId.CYAN to strings.themeCyan
            )
            themes.chunked(2).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (id, label) ->
                        FilterChip(
                            selected = state.settings.theme == id,
                            onClick = { vm.setTheme(id) },
                            label = { Text(label) },
                            colors = chipColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        } else {
            Text(strings.linksHint, color = c.textMuted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            FreePoseLinks.all.forEach { link ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(c.surface)
                        .border(1.dp, c.line, RoundedCornerShape(12.dp))
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, null, tint = c.accent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(link.title, color = c.textMain, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        if (state.settings.language == AppLanguage.PL) link.notePl else link.noteEn,
                        color = c.textMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun AddCustomScreen(
    vm: PoseViewModel,
    strings: S,
    systems: List<String>,
    categories: List<String>,
    angles: List<String>,
    onBack: () -> Unit,
    onSaved: (Pose?) -> Unit
) {
    val c = LocalPoseColors.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var poseName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Custom") }
    var system by remember { mutableStateOf(systems.firstOrNull() ?: "Custom") }
    var angle by remember { mutableStateOf(angles.firstOrNull() ?: "Front") }
    var keywords by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.back, tint = c.textMain)
            }
            Text(strings.addPoseTitle, color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    .clickable { picker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(strings.pickImage, color = c.textMuted)
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = poseName,
                onValueChange = { poseName = it },
                label = { Text(strings.poseName) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors(c),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            ChipPicker(strings.category, categories, category, c) { category = it }
            ChipPicker(strings.system, (listOf("Custom") + systems).distinct(), system, c) { system = it }
            ChipPicker(strings.angle, angles, angle, c) { angle = it }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = keywords,
                onValueChange = { keywords = it },
                label = { Text(strings.keywords) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(c),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val uri = imageUri ?: return@Button
                    saving = true
                    vm.addCustom(uri, poseName, category, system, angle, keywords) {
                        saving = false
                        onSaved(it)
                    }
                },
                enabled = imageUri != null && poseName.isNotBlank() && !saving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.accent)
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(strings.savePose, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChipPicker(label: String, options: List<String>, selected: String, c: PoseColors, onSelect: (String) -> Unit) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = c.surface2,
        labelColor = c.textMuted,
        selectedContainerColor = c.accentSoft,
        selectedLabelColor = c.accent
    )
    Text(label, color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.take(20).forEach { opt ->
            FilterChip(
                selected = selected == opt,
                onClick = { onSelect(opt) },
                label = { Text(opt.substringAfterLast('·').trim().ifBlank { opt }.take(24), maxLines = 1) },
                colors = chipColors
            )
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    val c = LocalPoseColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, Modifier.weight(0.35f), color = c.textMuted, fontSize = 12.sp)
        Text(value, Modifier.weight(0.65f), color = c.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun fieldColors(c: PoseColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = c.accent,
    unfocusedBorderColor = c.line,
    focusedContainerColor = c.surface,
    unfocusedContainerColor = c.surface,
    focusedTextColor = c.textMain,
    unfocusedTextColor = c.textMain,
    cursorColor = c.accent,
    focusedLabelColor = c.accent,
    unfocusedLabelColor = c.textMuted,
    focusedPlaceholderColor = c.textMuted,
    unfocusedPlaceholderColor = c.textMuted
)

private fun shortAngle(a: String): String = when {
    a.startsWith("Front") -> "Front"
    a.startsWith("High") -> "Top"
    a.startsWith("Low") -> "Low"
    a.startsWith("Side") -> "Side"
    a.startsWith("Three") -> "3/4"
    else -> a.take(10)
}

private fun copyText(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("pose", text))
}
