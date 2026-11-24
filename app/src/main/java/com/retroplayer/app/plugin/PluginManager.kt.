package com.retroplayer.app.plugin

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retroplayer.app.RetroSkin
import com.retroplayer.app.Song
import dalvik.system.DexClassLoader
import java.io.File

class PluginManager(private val context: Context) {
    val activePlugins = mutableStateListOf<RetroPlugin>()
    val availableCatalog = mutableStateListOf<RetroPlugin>()
    var pendingPermissionPlugin by mutableStateOf<RetroPlugin?>(null)

    init {
        availableCatalog.add(BuiltInDjDeckPlugin())
    }

    fun requestPluginLink(plugin: RetroPlugin) {
        if (activePlugins.any { it.id == plugin.id }) return
        pendingPermissionPlugin = plugin
    }

    fun confirmPermissionAndInstall(controller: PluginHostController) {
        val plugin = pendingPermissionPlugin ?: return
        plugin.onInitialize(context, controller)
        if (activePlugins.none { it.id == plugin.id }) {
            activePlugins.add(plugin)
        }
        pendingPermissionPlugin = null
    }

    fun denyPermission() {
        pendingPermissionPlugin = null
    }

    fun removePlugin(pluginId: String) {
        val target = activePlugins.find { it.id == pluginId }
        target?.onDestroy()
        activePlugins.removeAll { it.id == pluginId }
    }

    fun loadExternalPluginPackage(apkOrJarPath: String, entryClassName: String): Result<RetroPlugin> {
        return try {
            val file = File(apkOrJarPath)
            if (!file.exists()) return Result.failure(IllegalArgumentException("Arquivo não encontrado."))
            val optimizedDir = context.getDir("dex_plugins", Context.MODE_PRIVATE)
            val loader = DexClassLoader(file.absolutePath, optimizedDir.absolutePath, null, context.classLoader)
            val loadedClass = loader.loadClass(entryClassName)
            val instance = loadedClass.getDeclaredConstructor().newInstance() as RetroPlugin
            if (availableCatalog.none { it.id == instance.id }) {
                availableCatalog.add(instance)
            }
            requestPluginLink(instance)
            Result.success(instance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class BuiltInDjDeckPlugin : RetroPlugin {
    override val id = "com.retroplayer.plugin.djdeck"
    override val name = "Mesa DJ Pocket Y2K"
    override val author = "RetroPlayer Open API"
    override val description = "Controle de crossfader, pitch fino, pads de efeito e controle duplo de deck."
    override val version = "1.0.0"
    override val requestedPermissions = listOf(
        PluginPermission.PLAYBACK_CONTROL,
        PluginPermission.LIBRARY_READ,
        PluginPermission.FULL_SCREEN_TAB
    )

    override fun onInitialize(context: Context, controller: PluginHostController) {}

    @Composable
    override fun RenderPluginScreen(controller: PluginHostController, activeSkin: RetroSkin) {
        var crossfader by remember { mutableFloatStateOf(0.5f) }
        var finePitch by remember { mutableFloatStateOf(controller.playbackSpeed) }
        var deckBSong by remember { mutableStateOf<Song?>(controller.songs.getOrNull(1)) }

        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF121417)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("DJ MIXER STUDIO // PLUGIN ATIVO", color = activeSkin.accent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E2229)).border(1.dp, activeSkin.metalMid, RoundedCornerShape(8.dp)).padding(10.dp)) {
                    Text("DECK A", color = activeSkin.lcdText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    Text(text = controller.currentSong?.title ?: "Nenhuma faixa", color = Color.White, fontSize = 12.sp, maxLines = 1)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { controller.togglePlayPause() }, colors = ButtonDefaults.buttonColors(containerColor = activeSkin.accent), modifier = Modifier.fillMaxWidth()) {
                        Text(if (controller.isPlaying) "PAUSE" else "PLAY", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E2229)).border(1.dp, activeSkin.metalMid, RoundedCornerShape(8.dp)).padding(10.dp)) {
                    Text("DECK B", color = activeSkin.lcdText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    Text(text = deckBSong?.title ?: "Selecionar", color = Color.White, fontSize = 12.sp, maxLines = 1)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { deckBSong?.let { controller.playSong(it) } }, colors = ButtonDefaults.buttonColors(containerColor = activeSkin.metalLight), modifier = Modifier.fillMaxWidth()) {
                        Text("DISPARAR", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF1A1D22)).padding(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PITCH / TEMPO", color = Color.LightGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text("${String.format("%.2f", finePitch)}x", color = activeSkin.accent, fontFamily = FontFamily.Monospace)
                }
                Slider(value = finePitch, onValueChange = { finePitch = it; controller.setSpeed(it) }, valueRange = 0.5f..2.0f)
            }
        }
    }
    override fun onDestroy() {}
}

@Composable
fun PluginPermissionDialog(plugin: RetroPlugin, skin: RetroSkin, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1F24),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = skin.accent)
                Spacer(Modifier.width(8.dp))
                Text("Permissão de Plugin", color = Color.White, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("O plugin \"${plugin.name}\" deseja se vincular ao seu reprodutor.", color = Color.LightGray, fontSize = 14.sp)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = skin.accent)) {
                Text("Permitir e Vincular", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Recusar", color = Color.LightGray) }
        }
    )
}

@Composable
fun PluginHubAndDevPortal(pluginManager: PluginManager, hostController: PluginHostController, skin: RetroSkin, onOpenPluginTab: (RetroPlugin) -> Unit, onClose: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121417)).padding(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("PLUGINS & API ABERTA", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White) }
        }
        TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFF1E2229), contentColor = skin.accent) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Loja", fontSize = 12.sp) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("API Grátis", fontSize = 12.sp) })
        }
        Spacer(Modifier.height(12.dp))
        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pluginManager.availableCatalog) { plugin ->
                    val isInstalled = pluginManager.activePlugins.any { it.id == plugin.id }
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2229)), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(plugin.name, color = skin.accent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(plugin.description, color = Color.LightGray, fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            if (!isInstalled) {
                                Button(onClick = { pluginManager.requestPluginLink(plugin) }, colors = ButtonDefaults.buttonColors(containerColor = skin.accent)) {
                                    Text("Vincular", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(onClick = { onOpenPluginTab(plugin) }, colors = ButtonDefaults.buttonColors(containerColor = skin.lcdText)) {
                                    Text("Abrir Tela", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("SDK & API 100% Gratuita para Criadores", color = skin.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Implemente a interface RetroPlugin para criar qualquer ferramenta no RetroPlayer sem pagar nada.", color = Color.LightGray, fontSize = 13.sp)
                }
            }
        }
    }
}
