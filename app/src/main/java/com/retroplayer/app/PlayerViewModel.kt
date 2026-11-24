package com.retroplayer.app

import android.app.Application
import android.content.ComponentName
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    var tracks by mutableStateOf<List<Track>>(emptyList())
        private set
    var current by mutableStateOf<Track?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var positionMs by mutableStateOf(0L)
        private set
    var durationMs by mutableStateOf(0L)
        private set
    var volume by mutableStateOf(0.8f)
        private set
    var skin by mutableStateOf(Skins.Silver)
        private set
    var loaded by mutableStateOf(false)
        private set

    private var controller: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())

    private val ticker = object : Runnable {
        override fun run() {
            controller?.let { c ->
                positionMs = c.currentPosition.coerceAtLeast(0L)
                durationMs = c.duration.coerceAtLeast(0L)
            }
            handler.postDelayed(this, 250)
        }
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@PlayerViewModel.isPlaying = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncCurrent()
        }
    }

    init {
        val context = getApplication<Application>()
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            val c = future.get()
            controller = c
            c.addListener(listener)
            c.setVolume(volume)
            isPlaying = c.isPlaying
            syncCurrent()
        }, ContextCompat.getMainExecutor(context))
        handler.post(ticker)
    }

    private fun syncCurrent() {
        val id = controller?.currentMediaItem?.mediaId
        current = tracks.firstOrNull { it.id.toString() == id }
    }

    fun loadTracks() {
        viewModelScope.launch {
            tracks = withContext(Dispatchers.IO) {
                try {
                    MusicRepository.load(getApplication())
                } catch (e: Exception) {
                    emptyList()
                }
            }
            loaded = true
            syncCurrent()
        }
    }

    fun play(track: Track) {
        val c = controller ?: return
        val index = tracks.indexOf(track)
        if (index < 0) return
        val items = tracks.map {
            MediaItem.Builder()
                .setMediaId(it.id.toString())
                .setUri(it.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle(it.title).setArtist(it.artist).build()
                )
                .build()
        }
        c.setMediaItems(items, index, 0L)
        c.prepare()
        c.play()
    }

    fun togglePlay() {
        val c = controller ?: return
        if (c.mediaItemCount == 0) {
            tracks.firstOrNull()?.let { play(it) }
            return
        }
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { controller?.seekToNextMediaItem() }

    fun previous() { controller?.seekToPreviousMediaItem() }

    fun seekTo(ms: Long) { controller?.seekTo(ms) }

    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
        controller?.setVolume(volume)
    }

    fun nextSkin() {
        val all = Skins.all
        skin = all[(all.indexOf(skin) + 1) % all.size]
    }

    override fun onCleared() {
        handler.removeCallbacks(ticker)
        controller?.removeListener(listener)
        controller?.release()
        super.onCleared()
    }
}
