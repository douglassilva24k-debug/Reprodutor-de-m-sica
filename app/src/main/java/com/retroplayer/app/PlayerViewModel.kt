package com.retroplayer.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.media.audiofx.Equalizer
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.retroplayer.app.plugin.PluginHostController
import com.retroplayer.app.plugin.PluginManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application), PluginHostController {
    private val repository = MusicRepository(application)
    val pluginManager = PluginManager(application)

    var currentSkin by mutableStateOf(RetroSkins.first())
    var searchQuery by mutableStateOf("")

    override val songs = mutableStateListOf<Song>()
    val playlists = mutableStateListOf<Playlist>()
    var selectedPlaylist by mutableStateOf<Playlist?>(null)

    override var currentSong by mutableStateOf<Song?>(null)
    override var isPlaying by mutableStateOf(false)
    override var playbackSpeed by mutableFloatStateOf(1.0f)
    var playbackPitch by mutableFloatStateOf(1.0f)
    var volumeLevel by mutableFloatStateOf(0.85f)
    override var currentPositionMs by mutableLongStateOf(0L)
    override var durationMs by mutableLongStateOf(0L)
    var shuffleEnabled by mutableStateOf(false)
    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_ALL)

    val eqBands = mutableStateListOf(0f, 0f, 0f, 0f, 0f)
    var activeEqPreset by mutableStateOf("Normal")
    private var hardwareEqualizer: Equalizer? = null
    private var mediaController: MediaController? = null

    init {
        loadSavedPlaylists()
        connectToPlaybackService()
        startProgressUpdater()
    }

    private fun connectToPlaybackService() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            mediaController = controller
            controller.repeatMode = repeatMode
            controller.volume = volumeLevel
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    currentSong = songs.find { it.id == mediaItem?.mediaId?.toLongOrNull() }
                    durationMs = controller.duration.coerceAtLeast(0L)
                }
            })
            if (songs.isNotEmpty() && controller.mediaItemCount == 0) {
                setQueue(songs, 0, false)
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startProgressUpdater() {
        viewModelScope.launch {
            while (true) {
                mediaController?.let {
                    currentPositionMs = it.currentPosition.coerceAtLeast(0L)
                    if (it.duration > 0) durationMs = it.duration
                }
                delay(350L)
            }
        }
    }

    fun scanLibrary() {
        viewModelScope.launch {
            val loaded = repository.loadAllSongs()
            songs.clear()
            songs.addAll(loaded)
            if (loaded.isNotEmpty() && currentSong == null) {
                currentSong = loaded.first()
                setQueue(loaded, 0, false)
            }
        }
    }

    private fun setQueue(list: List<Song>, startIndex: Int, autoPlay: Boolean) {
        val ctrl = mediaController ?: return
        val items = list.map { song ->
            MediaItem.Builder().setMediaId(song.id.toString()).setUri(song.uri)
                .setMediaMetadata(MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).build()).build()
        }
        ctrl.setMediaItems(items, startIndex, 0L)
        ctrl.prepare()
        if (autoPlay) ctrl.play()
    }

    override fun playSong(song: Song) {
        currentSong = song
        val list = getDisplayedSongs()
        setQueue(list, list.indexOfFirst { it.id == song.id }.coerceAtLeast(0), true)
    }

    override fun togglePlayPause() {
        mediaController?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    override fun nextTrack() { mediaController?.seekToNextMediaItem() }
    override fun previousTrack() { mediaController?.seekToPreviousMediaItem() }
    override fun seekTo(positionMs: Long) { mediaController?.seekTo(positionMs); currentPositionMs = positionMs }

    override fun setSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(0.5f, 2.0f)
        mediaController?.setPlaybackParameters(PlaybackParameters(playbackSpeed, playbackPitch))
    }

    override fun setPitch(pitch: Float) {
        playbackPitch = pitch.coerceIn(0.5f, 1.5f)
        mediaController?.setPlaybackParameters(PlaybackParameters(playbackSpeed, playbackPitch))
    }

    fun updateVolume(vol: Float) { volumeLevel = vol.coerceIn(0f, 1f); mediaController?.volume = volumeLevel }
    fun toggleShuffle() { shuffleEnabled = !shuffleEnabled; mediaController?.shuffleModeEnabled = shuffleEnabled }
    fun cycleRepeatMode() {
        repeatMode = when(repeatMode) { Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE; Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF; else -> Player.REPEAT_MODE_ALL }
        mediaController?.repeatMode = repeatMode
    }

    override fun setEqBand(bandIndex: Int, levelDb: Float) {
        if (bandIndex in eqBands.indices) {
            eqBands[bandIndex] = levelDb.coerceIn(-12f, 12f)
            activeEqPreset = "Personalizado"
        }
    }

    fun applyEqPreset(preset: String) {
        activeEqPreset = preset
        val vals = when(preset) {
            "Grave" -> listOf(8f, 5f, 0f, -1f, 2f)
            "Rock" -> listOf(6f, 3f, -2f, 4f, 7f)
            else -> listOf(0f, 0f, 0f, 0f, 0f)
        }
        vals.forEachIndexed { i, v -> eqBands[i] = v }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        playlists.add(Playlist(System.currentTimeMillis().toString(), name.trim(), emptyList()))
    }

    fun addSongToPlaylist(playlistId: String, songId: Long) {
        val idx = playlists.indexOfFirst { it.id == playlistId }
        if (idx >= 0) {
            val pl = playlists[idx]
            if (!pl.songIds.contains(songId)) playlists[idx] = pl.copy(songIds = pl.songIds + songId)
        }
    }

    fun deletePlaylist(id: String) { playlists.removeAll { it.id == id } }

    fun getDisplayedSongs(): List<Song> {
        val base = selectedPlaylist?.let { pl -> songs.filter { pl.songIds.contains(it.id) } } ?: songs
        return if (searchQuery.isBlank()) base else base.filter { it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true) }
    }

    private fun savePlaylists() {}
    private fun loadSavedPlaylists() {}

    override fun onCleared() {
        hardwareEqualizer?.release()
        mediaController?.release()
        super.onCleared()
    }
}
