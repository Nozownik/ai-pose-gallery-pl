package pl.nozownik.posegallery.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import coil.request.ImageRequest
import pl.nozownik.posegallery.data.Pose
import pl.nozownik.posegallery.data.PoseGroup

@Composable
fun AppRoot(vm: PoseViewModel) {
    val state by vm.uiState.collectAsState()
    var selectedPoseId by remember { mutableStateOf<String?>(null) }

    if (selectedPoseId != null) {
        val group = vm.groupForId(selectedPoseId!!)
        val pose = vm.findPose(selectedPoseId!!)
        if (pose != null && group != null) {
            BackHandler { selectedPoseId = null }
            DetailScreen(
                pose = pose,
                group = group,
                isFavorite = group.items.any { it.id in state.favorites },
                onBack = { selectedPoseId = null },
                onSelectPose = { selectedPoseId = it.id },
                onToggleFav = { vm.toggleFavorite(pose.id) },
                onSwipeGroup = { dir ->
                    val list = state.visibleGroups
                    val idx = list.indexOfFirst { it.key == group.key }
                    val next = list.getOrNull(idx + dir) ?: return@DetailScreen
                    val keep = pose.angle
                    selectedPoseId = (next.byAngle[keep] ?: next.primary).id
                }
            )
            return
        }
    }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 0.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    indicatorColor = AccentSoft,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
                NavigationBarItem(
                    selected = state.filters.tab == 0,
                    onClick = { vm.setTab(0) },
                    icon = { Icon(Icons.Default.GridView, contentDescription = null) },
                    label = { Text("Galeria") },
                    colors = colors
                )
                NavigationBarItem(
                    selected = state.filters.tab == 1,
                    onClick = { vm.setTab(1) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Ulubione") },
                    colors = colors
                )
            }
        }
    ) { pad ->
        GalleryScreen(
            state = state,
            vm = vm,
            contentPadding = pad,
            onOpen = { g ->
                val ang = state.filters.angle
                val pose = if (ang.isNotEmpty()) g.byAngle[ang] ?: g.primary else g.primary
                selectedPoseId = pose.id
            }
        )
    }
}

@Composable
fun GalleryScreen(
    state: GalleryUiState,
    vm: PoseViewModel,
    contentPadding: PaddingValues,
    onOpen: (PoseGroup) -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = Surface2,
        labelColor = TextMuted,
        selectedContainerColor = AccentSoft,
        selectedLabelColor = Accent
    )

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
                Text("Galeria poz AI", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextMain)
                Text(
                    if (state.filters.tab == 1) "Ulubione · tagi EN do promptów"
                    else "${state.visibleGroups.size} grup · tagi EN",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtry", tint = if (showFilters) Accent else TextMuted)
            }
        }

        OutlinedTextField(
            value = state.filters.query,
            onValueChange = vm::setQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Szukaj nazwy EN, ID, kategorii…") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
            trailingIcon = {
                if (state.filters.query.isNotEmpty()) {
                    IconButton(onClick = { vm.setQuery("") }) {
                        Icon(Icons.Default.Close, null, tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = Line,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain,
                cursorColor = Accent
            )
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
                label = { Text("Wszystkie") },
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
            Text("Kategoria", Modifier.padding(horizontal = 16.dp), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = state.filters.category.isEmpty(), onClick = { vm.setCategory("") }, label = { Text("Wszystkie") }, colors = chipColors)
                state.categories.forEach { c ->
                    FilterChip(
                        selected = state.filters.category == c,
                        onClick = { vm.setCategory(if (state.filters.category == c) "" else c) },
                        label = { Text(c, maxLines = 1) },
                        colors = chipColors
                    )
                }
            }
            Text("Kąt", Modifier.padding(horizontal = 16.dp), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = state.filters.angle.isEmpty(), onClick = { vm.setAngle("") }, label = { Text("Wszystkie") }, colors = chipColors)
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
                Text("Reset filtrów", color = Accent)
            }
        }

        Spacer(Modifier.height(6.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "", color = TextMuted)
            }
            state.visibleGroups.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (state.filters.tab == 1) "Brak ulubionych" else "Brak wyników", color = TextMain, fontWeight = FontWeight.SemiBold)
                    Text("Zmień filtry albo dodaj serduszko", color = TextMuted, fontSize = 13.sp)
                    if (state.filters.tab == 0) {
                        TextButton(onClick = { vm.resetFilters() }) { Text("Wyczyść filtry", color = Accent) }
                    }
                }
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
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
    onClick: () -> Unit,
    onToggleFav: () -> Unit
) {
    Column(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
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
                    .background(Surface2)
            )
            Text(
                group.number,
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
                    contentDescription = "Ulubione",
                    tint = if (favorited) Color(0xFFFF5A7A) else Color.White
                )
            }
        }
        Column(Modifier.padding(10.dp)) {
            Text(
                group.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = TextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp
            )
            Text(group.category, color = Accent, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun DetailScreen(
    pose: Pose,
    group: PoseGroup,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onSelectPose: (Pose) -> Unit,
    onToggleFav: () -> Unit,
    onSwipeGroup: (Int) -> Unit
) {
    val context = LocalContext.current
    var dragAcc by remember { mutableFloatStateOf(0f) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć", tint = TextMain)
            }
            Column(Modifier.weight(1f)) {
                Text(pose.poseNumber, color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(pose.poseName, color = TextMain, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onToggleFav) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    "Ulubione",
                    tint = if (isFavorite) Color(0xFFFF5A7A) else TextMuted
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Surface)
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
                .background(Surface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Kąt", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            .background(if (selected) AccentSoft else Surface2)
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
                        Text(shortAngle(p.angle), color = if (selected) Accent else TextMuted, fontSize = 9.sp, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            MetaRow("System", pose.system)
            MetaRow("Kategoria", pose.category)
            MetaRow("Kąt", pose.angle)
            MetaRow("ID", pose.id)

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    copyText(context, pose.promptTags())
                    Toast.makeText(context, "Skopiowano tagi EN", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.White)
            ) {
                Icon(Icons.Default.ContentCopy, null)
                Spacer(Modifier.size(8.dp))
                Text("Kopiuj tagi EN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = {
                    copyText(context, pose.id)
                    Toast.makeText(context, "Skopiowano ID", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kopiuj ID", color = Accent)
            }
            Text(
                pose.promptTags(),
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(label, Modifier.weight(0.35f), color = TextMuted, fontSize = 12.sp)
        Text(value, Modifier.weight(0.65f), color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

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
