package pl.nozownik.posegallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pl.nozownik.posegallery.data.ClipboardEntryType
import pl.nozownik.posegallery.data.Pose

@Composable
fun CompareScreen(
    poseA: Pose?,
    poseB: Pose?,
    strings: S,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    val c = LocalPoseColors.current
    var overlay by remember { mutableStateOf(false) }
    var alpha by remember { mutableFloatStateOf(0.5f) }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .padding(top = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.back, tint = c.textMain)
            }
            Text(strings.compare, Modifier.weight(1f), color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = onClear) { Text(strings.compareClear, color = c.accent) }
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !overlay,
                onClick = { overlay = false },
                label = { Text(strings.compareSplit) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = c.accentSoft, selectedLabelColor = c.accent,
                    containerColor = c.surface2, labelColor = c.textMuted
                )
            )
            FilterChip(
                selected = overlay,
                onClick = { overlay = true },
                label = { Text(strings.compareOverlay) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = c.accentSoft, selectedLabelColor = c.accent,
                    containerColor = c.surface2, labelColor = c.textMuted
                )
            )
        }
        Spacer(Modifier.height(8.dp))
        if (poseA == null || poseB == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.comparePick, color = c.textMuted)
            }
            return
        }
        if (!overlay) {
            Row(Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ComparePane(poseA, Modifier.weight(1f))
                ComparePane(poseB, Modifier.weight(1f))
            }
        } else {
            Box(Modifier.fillMaxSize().padding(8.dp)) {
                AsyncImage(poseA.fullUrl(), poseA.poseName, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                AsyncImage(
                    poseB.fullUrl(), poseB.poseName, contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().alpha(alpha)
                )
            }
            Slider(value = alpha, onValueChange = { alpha = it }, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun ComparePane(pose: Pose, modifier: Modifier) {
    val c = LocalPoseColors.current
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(c.surface).padding(6.dp)) {
        AsyncImage(
            pose.fullUrl(), pose.poseName, contentScale = ContentScale.Fit,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        Text(pose.poseName, color = c.textMain, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(pose.angle, color = c.accent, fontSize = 10.sp)
    }
}

@Composable
fun ClipboardHistoryPanel(state: GalleryUiState, vm: PoseViewModel, strings: S) {
    val c = LocalPoseColors.current
    Text(strings.clipboardHistory, color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(Modifier.height(8.dp))
    if (state.clipboardHistory.isEmpty()) {
        Text(strings.clipboardEmpty, color = c.textMuted, fontSize = 12.sp)
    } else {
        state.clipboardHistory.take(20).forEach { e ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.surface)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (e.type == ClipboardEntryType.TAGS) "TAGS" else "IMAGE",
                        color = c.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                    Text(e.label, color = c.textMain, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (e.type == ClipboardEntryType.TAGS) {
                        Text(e.payload, color = c.textMuted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                IconButton(onClick = { vm.reCopyHistory(e) }) {
                    Icon(Icons.Default.ContentCopy, strings.recopy, tint = c.accent)
                }
            }
        }
        TextButton(onClick = { vm.clearClipboardHistory() }) {
            Text(strings.clipboardClear, color = c.accent)
        }
    }
}

@Composable
fun CustomCategoriesPanel(state: GalleryUiState, vm: PoseViewModel, strings: S) {
    val c = LocalPoseColors.current
    var name by remember { mutableStateOf("") }
    Text(strings.customCategories, color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.weight(1f),
            label = { Text(strings.categoryName) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.size(8.dp))
        Button(
            onClick = { vm.addCustomCategory(name); name = "" },
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = c.accent)
        ) { Text(strings.addCategory) }
    }
    Spacer(Modifier.height(8.dp))
    state.customCategories.forEach { cat ->
        var editing by remember(cat.id) { mutableStateOf(false) }
        var editName by remember(cat.id) { mutableStateOf(cat.name) }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(10.dp)).background(c.surface).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (editing) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                TextButton(onClick = {
                    vm.renameCustomCategory(cat.id, editName)
                    editing = false
                }) { Text(strings.renameCategory, color = c.accent) }
            } else {
                Text(cat.name, Modifier.weight(1f).clickable { editing = true }, color = c.textMain)
                IconButton(onClick = { vm.deleteCustomCategory(cat.id) }) {
                    Icon(Icons.Default.Delete, strings.deleteCategory, tint = androidx.compose.ui.graphics.Color(0xFFFF5A7A))
                }
            }
        }
    }
}

@Composable
fun ToolsPanel(state: GalleryUiState, vm: PoseViewModel, strings: S) {
    val c = LocalPoseColors.current
    Text(strings.tools, color = c.textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = { vm.openRandomPose() }, modifier = Modifier.fillMaxWidth()) {
        Text(strings.randomPose, color = c.accent)
    }
    Spacer(Modifier.height(6.dp))
    OutlinedButton(onClick = { vm.pinRandomShortcut() }, modifier = Modifier.fillMaxWidth()) {
        Text(strings.pinShortcut, color = c.accent)
    }
    Spacer(Modifier.height(6.dp))
    OutlinedButton(onClick = { vm.exportCustomPack() }, modifier = Modifier.fillMaxWidth()) {
        Text(strings.exportPack, color = c.accent)
    }
    Spacer(Modifier.height(6.dp))
    OutlinedButton(onClick = { vm.cacheCurrentCategory() }, modifier = Modifier.fillMaxWidth()) {
        Text(strings.cacheCategory, color = c.accent)
    }
    Text(strings.offlineCached.format(state.offlineCached), color = c.textMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
}
