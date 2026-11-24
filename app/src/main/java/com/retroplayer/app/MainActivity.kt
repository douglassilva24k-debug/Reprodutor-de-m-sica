package com.retroplayer.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import com.retroplayer.app.plugin.PluginHubAndDevPortal
import com.retroplayer.app.plugin.PluginPermissionDialog
import com.retroplayer.app.plugin.RetroPlugin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: PlayerViewModel = viewModel()
            RetroPlayerRootApp(vm)
        }
    }
}

@Composable
fun RetroPlayerRootApp(vm: PlayerViewModel) {
    val skin = vm.currentSkin
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) vm.scanLibrary() }

    LaunchedEffect(Unit) { launcher.launch(permission) }

    var currentNavTab by remember { mutableIntStateOf(0) }
    var activePluginScreen by remember { mutableStateOf<RetroPlugin?>(null) }

    vm.pluginManager.pendingPermissionPlugin?.let { pending ->
        PluginPermissionDialog(pending, skin, onConfirm = { vm.pluginManager.confirmPermissionAndInstall(vm) }, onDismiss = { vm.pluginManager.denyPermission() })
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0D0F12)) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BrushedMetalPanel(skin = skin, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("RETROPLAYER // Y2K", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = skin.metalBorder)
                        MetalTactileButton(skin = skin, isActive = currentNavTab == 3, onClick = { activePluginScreen = null; currentNavTab = if (currentNavTab == 3) 0 else 3 }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Extension, contentDescription = null, tint = skin.metalBorder, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("PLUGINS (${vm.pluginManager.activePlugins.size})", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = skin.metalBorder)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(RetroSkins) { itemSkin ->
                            val isSelected = itemSkin.id == skin.id
                            Box(modifier = Modifier.size(26.dp).clip(CircleShape).background(itemSkin.metalMid).border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) Color.White else itemSkin.metalBorder, CircleShape).clickable { vm.currentSkin = itemSkin })
                        }
                    }
                }
            }

            BrushedMetalPanel(skin = skin, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(skin.lcdBg).border(2.dp, skin.metalBorder, RoundedCornerShape(8.dp)).padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (vm.isPlaying) "▶ TOCANDO" else "❚❚ PAUSADO", color = skin.accent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("SPD ${String.format("%.2f", vm.playbackSpeed)}x", color = skin.lcdText, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(vm.currentSong?.title ?: "Nenhuma música", color = skin.lcdText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(vm.currentSong?.artist ?: "Toque em uma faixa", color = skin.lcdText.copy(0.75f), fontFamily = FontFamily.Monospace, fontSize = 11.sp, maxLines = 1)
                        Spacer(Modifier.height(8.dp))
                        RetroSpectrumVisualizer(vm.isPlaying, skin, modifier = Modifier.fillMaxWidth().height(40.dp))
                        Spacer(Modifier.height(6.dp))
                        val progress = if (vm.durationMs > 0) (vm.currentPositionMs.toFloat() / vm.durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                        Slider(value = progress, onValueChange = { vm.seekTo((it * vm.durationMs).toLong()) }, colors = SliderDefaults.colors(thumbColor = skin.accent, activeTrackColor = skin.lcdText, inactiveTrackColor = skin.lcdDim), modifier = Modifier.height(20.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MetalTactileButton(skin = skin, isActive = vm.shuffleEnabled, onClick = { vm.toggleShuffle() }) { Icon(Icons.Default.Shuffle, contentDescription = null, tint = skin.metalBorder, modifier = Modifier.size(18.dp)) }
                            MetalTactileButton(skin = skin, onClick = { vm.previousTrack() }) { Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = skin.metalBorder, modifier = Modifier.size(20.dp)) }
                            MetalTactileButton(skin = skin, isActive = vm.isPlaying, onClick = { vm.togglePlayPause() }) { Icon(if (vm.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = skin.metalBorder, modifier = Modifier.size(24.dp)) }
                            MetalTactileButton(skin = skin, onClick = { vm.nextTrack() }) { Icon(Icons.Default.SkipNext, contentDescription = null, tint = skin.metalBorder, modifier = Modifier.size(20.dp)) }
                        }
                        RotaryMetalKnob(value = vm.volumeLevel, onValueChange = { vm.updateVolume(it) }, skin = skin, label = "VOL", size = 50.dp)
                    }
                }
            }

            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { MetalTactileButton(skin = skin, isActive = activePluginScreen == null && currentNavTab == 0, onClick = { activePluginScreen = null; currentNavTab = 0 }) { Text("MÚSICAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = skin.metalBorder) } }
                item { MetalTactileButton(skin = skin, isActive = activePluginScreen == null && currentNavTab == 1, onClick = { activePluginScreen = null; currentNavTab = 1 }) { Text("PLAYLISTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = skin.metalBorder) } }
                item { MetalTactileButton(skin = skin, isActive = activePluginScreen == null && currentNavTab == 2, onClick = { activePluginScreen = null; currentNavTab = 2 }) { Text("EQ & SPEED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = skin.metalBorder) } }
                items(vm.pluginManager.activePlugins) { plugin ->
                    MetalTactileButton(skin = skin, isActive = activePluginScreen?.id == plugin.id, onClick = { activePluginScreen = plugin }) { Text("⚡ ${plugin.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = skin.metalBorder) }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF15181C)).border(1.dp, skin.metalBorder, RoundedCornerShape(10.dp))) {
                val opened = activePluginScreen
                if (opened != null) {
                    opened.RenderPluginScreen(controller = vm, activeSkin = skin)
                } else {
                    when (currentNavTab) {
                        0 -> SongLibraryTab(vm, skin)
                        1 -> PlaylistsTab(vm, skin, onOpen = { currentNavTab = 0 })
                        2 -> EqualizerTab(vm, skin)
                        3 -> PluginHubAndDevPortal(vm.pluginManager, vm, skin, onOpenPluginTab = { activePluginScreen = it }, onClose = { currentNavTab = 0 })
                    }
                }
            }
        }
    }
}

@Composable
fun SongLibraryTab(vm: PlayerViewModel, skin: RetroSkin) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(value = vm.searchQuery, onValueChange = { vm.searchQuery = it }, placeholder = { Text("Buscar...", fontSize = 12.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        val displayed = vm.getDisplayedSongs()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(displayed) { song ->
                val isCurrent = vm.currentSong?.id == song.id
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(if (isCurrent) skin.lcdBg else Color(0xFF1D2127)).clickable { vm.playSong(song) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(song.title, color = if (isCurrent) skin.accent else Color.White, fontSize = 13.sp, maxLines = 1)
                        Text("${song.artist} • ${formatDuration(song.duration)}", color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistsTab(vm: PlayerViewModel, skin: RetroSkin, onOpen: () -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("PLAYLISTS", color = skin.accent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Nome...", fontSize = 12.sp) }, modifier = Modifier.weight(1f))
            Button(onClick = { vm.createPlaylist(name); name = "" }, colors = ButtonDefaults.buttonColors(containerColor = skin.accent)) { Text("Criar", color = Color.Black) }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(vm.playlists) { pl ->
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF1F242B)).clickable { vm.selectedPlaylist = pl; onOpen() }.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(pl.name, color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { vm.deletePlaylist(pl.id) }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) }
                }
            }
        }
    }
}

@Composable
fun EqualizerTab(vm: PlayerViewModel, skin: RetroSkin) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1C2026), RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text("VELOCIDADE", color = skin.accent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Slider(value = vm.playbackSpeed, onValueChange = { vm.setSpeed(it) }, valueRange = 0.5f..2.0f)
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val sec = (ms / 1000).coerceAtLeast(0)
    return String.format("%02d:%02d", sec / 60, sec % 60)
}
