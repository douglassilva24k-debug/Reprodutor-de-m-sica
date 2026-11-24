package com.retroplayer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) { RetroApp() }
        }
    }
}

fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

@Composable
fun RetroApp(vm: PlayerViewModel = viewModel()) {
    val ctx = LocalContext.current
    val perms = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.loadTracks() }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(ctx, perms[0]) == PackageManager.PERMISSION_GRANTED
        if (granted) vm.loadTracks() else launcher.launch(perms)
    }

    val skin = vm.skin
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(skin.bodyTop, skin.bodyBottom)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        Text(
            "RETRO PLAYER",
            color = skin.ink,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(8.dp))
        Display(vm, Modifier.weight(1f).fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        ControlPanel(vm)
    }
}

@Composable
fun Display(vm: PlayerViewModel, modifier: Modifier) {
    val skin = vm.skin
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black)
            .padding(8.dp)
    ) {
        val t = vm.current
        Text(
            if (t != null) "${t.title} - ${t.artist}" else "Escolha uma música",
            color = Color(0xFFEDEDED),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFE9E4D8))
        ) {
            if (vm.tracks.isEmpty()) {
                Text(
                    if (vm.loaded) "Nenhuma música encontrada" else "Carregando…",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF444444),
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(vm.tracks, key = { _, tr -> tr.id }) { i, tr ->
                        val selected = vm.current?.id == tr.id
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { vm.play(tr) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${i + 1}.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(36.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    tr.title,
                                    color = if (selected) Color(0xFFD32F2F) else Color(0xFF1B1B1B),
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    tr.artist,
                                    color = if (selected) Color(0xFFD32F2F) else Color(0xFF666666),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                fmt(tr.durationMs),
                                color = if (selected) Color(0xFFD32F2F) else Color(0xFF666666),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
        var dragging by remember { mutableStateOf<Float?>(null) }
        val frac = if (vm.durationMs > 0) vm.positionMs.toFloat() / vm.durationMs else 0f
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(fmt(vm.positionMs), color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Slider(
                value = dragging ?: frac,
                onValueChange = { dragging = it },
                onValueChangeFinished = {
                    dragging?.let { vm.seekTo((it * vm.durationMs).toLong()) }
                    dragging = null
                },
                modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                colors = SliderDefaults.colors(
                    thumbColor = skin.glow,
                    activeTrackColor = skin.glow,
                    inactiveTrackColor = Color(0xFF3A3A3A),
                ),
            )
            Text(fmt(vm.durationMs), color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun ControlPanel(vm: PlayerViewModel) {
    val skin = vm.skin
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(skin.metalLight, skin.metalMid, skin.metalDark)))
            .border(1.5.dp, Color.White.copy(alpha = 0.6f), shape)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MetalButton(skin, onClick = { vm.nextSkin() }, modifier = Modifier.size(width = 64.dp, height = 44.dp)) {
                GlyphIcon(Glyph.SKIN, skin.ink)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MetalKnob(
                    skin = skin,
                    value = vm.volume,
                    onValueChange = { vm.setVolume(it) },
                    modifier = Modifier.size(150.dp),
                    pulse = if (vm.isPlaying) 1f else 0f,
                )
                Text("VOLUME", color = skin.ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.size(width = 64.dp, height = 44.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MetalButton(skin, onClick = { vm.previous() }, round = true, modifier = Modifier.size(56.dp)) {
                GlyphIcon(Glyph.PREV, skin.ink)
            }
            MetalButton(skin, onClick = { vm.togglePlay() }, round = true, active = vm.isPlaying, modifier = Modifier.size(72.dp)) {
                GlyphIcon(if (vm.isPlaying) Glyph.PAUSE else Glyph.PLAY, skin.ink, Modifier.size(30.dp))
            }
            MetalButton(skin, onClick = { vm.next() }, round = true, modifier = Modifier.size(56.dp)) {
                GlyphIcon(Glyph.NEXT, skin.ink)
            }
        }
    }
}
