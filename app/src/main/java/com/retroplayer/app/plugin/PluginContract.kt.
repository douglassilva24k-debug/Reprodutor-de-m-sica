package com.retroplayer.app.plugin

import android.content.Context
import androidx.compose.runtime.Composable
import com.retroplayer.app.RetroSkin
import com.retroplayer.app.Song

interface RetroPlugin {
    val id: String
    val name: String
    val author: String
    val description: String
    val version: String
    val requestedPermissions: List<PluginPermission>

    fun onInitialize(context: Context, controller: PluginHostController)
    @Composable
    fun RenderPluginScreen(controller: PluginHostController, activeSkin: RetroSkin)
    fun onDestroy()
}

enum class PluginPermission(val label: String, val detail: String) {
    PLAYBACK_CONTROL("Controle de Reprodução", "Permite tocar, pausar, pular faixas e alterar velocidade."),
    LIBRARY_READ("Leitura da Biblioteca", "Permite listar músicas e playlists do aparelho."),
    AUDIO_FX("Efeitos e Equalização", "Permite alterar bandas do equalizador e ganho de graves."),
    FULL_SCREEN_TAB("Aba Dedicada", "Permite abrir uma tela/ferramenta completa dentro do app.")
}

interface PluginHostController {
    val songs: List<Song>
    val currentSong: Song?
    val isPlaying: Boolean
    val playbackSpeed: Float
    val currentPositionMs: Long
    val durationMs: Long

    fun playSong(song: Song)
    fun togglePlayPause()
    fun nextTrack()
    fun previousTrack()
    fun seekTo(positionMs: Long)
    fun setSpeed(speed: Float)
    fun setPitch(pitch: Float)
    fun setEqBand(bandIndex: Int, levelDb: Float)
}
