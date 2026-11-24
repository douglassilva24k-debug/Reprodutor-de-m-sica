// Exemplo de como o ViewModel ou Service deve expor os métodos para o PluginHostController
class PlayerViewModel : ViewModel(), PluginHostController {
    override var songs: List<Song> by mutableStateOf(emptyList())
    override var currentSong: Song? by mutableStateOf(null)
    override var isPlaying: Boolean by mutableStateOf(false)
    override var playbackSpeed: Float by mutableFloatStateOf(1.0f)
    override var currentPositionMs: Long by mutableLongStateOf(0L)
    override var durationMs: Long by mutableLongStateOf(0L)

    override fun playSong(song: Song) {
        // Sua lógica para tocar a música
        currentSong = song
        isPlaying = true
    }

    override fun togglePlayPause() {
        isPlaying = !isPlaying
        // Chame aqui o método de pause/play real do seu player (ex: mediaController.pause())
    }

    override fun nextTrack() {
        // Mapeie para a sua função de próxima faixa (ex: skipNext())
    }

    override fun previousTrack() {
        // Mapeie para a sua função de faixa anterior
    }

    override fun seekTo(positionMs: Long) {
        // Lógica de seek
    }

    override fun setSpeed(speed: Float) {
        playbackSpeed = speed
        // Ajustar velocidade no player
    }

    override fun setPitch(pitch: Float) {
        // Ajustar pitch
    }

    override fun setEqBand(bandIndex: Int, levelDb: Float) {
        // Ajustar equalizador
    }
}
