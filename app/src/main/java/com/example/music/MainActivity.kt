package com.example.music

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.music.data.model.Track
import com.example.music.service.PlaybackService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private lateinit var miniPlayerContainer: View
    private lateinit var miniPlayerImage: ImageView
    private lateinit var miniPlayerTitle: TextView
    private lateinit var miniPlayerArtist: TextView
    private lateinit var miniPlayerPlayPause: ImageView
    private lateinit var miniPlayerProgress: ProgressBar
    private lateinit var navController: NavController
    private var activeQueueSignature: String = ""
    private var syncingFromController = false

    // ── Progress polling ──────────────────────────────────────────────────────
    // 500 ms instead of 1000 ms → smoother time display and seekbar movement
    private val progressHandler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            mediaController?.let { ctrl ->
                if (ctrl.isPlaying) {
                    val pos = ctrl.currentPosition.toInt()
                    mainViewModel.setCurrentPosition(pos)
                    miniPlayerProgress.progress = pos
                }
            }
            progressHandler.postDelayed(this, 500)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        miniPlayerContainer = findViewById(R.id.mini_player_container)
        miniPlayerImage     = findViewById(R.id.mini_player_image)
        miniPlayerTitle     = findViewById(R.id.mini_player_title)
        miniPlayerArtist    = findViewById(R.id.mini_player_artist)
        miniPlayerPlayPause = findViewById(R.id.mini_player_play_pause)
        miniPlayerProgress  = findViewById(R.id.mini_player_progress)

        val navView: BottomNavigationView = findViewById(R.id.nav_view) ?: return
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment ?: return

        navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initializeController()
        setupObservers()
        setupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(updateProgressRunnable)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    // ── Controller setup ──────────────────────────────────────────────────────

    private fun initializeController() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupControllerListeners()
            } catch (e: ExecutionException) { e.printStackTrace() }
              catch (e: InterruptedException) { e.printStackTrace() }
        }, MoreExecutors.directExecutor())
    }

    private fun setupControllerListeners() {
        mediaController?.addListener(object : Player.Listener {

            // ── Play / pause ──────────────────────────────────────────────────
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                mainViewModel.setPlaying(isPlaying)
                if (isPlaying) progressHandler.post(updateProgressRunnable)
                else           progressHandler.removeCallbacks(updateProgressRunnable)
            }

            // ── Duration: only reliable once STATE_READY ──────────────────────
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    applyDurationFromController()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncUiWithController(mediaItem)
            }

            // ── Catches timeline/metadata updates (e.g. live streams) ─────────
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.containsAny(
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_TIMELINE_CHANGED,
                        Player.EVENT_MEDIA_METADATA_CHANGED
                    )
                ) {
                    val dur = player.duration
                    if (dur > 0L) applyDuration(dur)
                }
            }
        })
    }

    /** Pull the real duration from the controller and push it into the ViewModel. */
    private fun applyDurationFromController() {
        val dur = mediaController?.duration ?: return
        if (dur > 0L) applyDuration(dur)
    }

    private fun applyDuration(durationMs: Long) {
        val d = durationMs.toInt()
        mainViewModel.setDuration(d)
        miniPlayerProgress.max = d
    }

    // ── ViewModel observers ───────────────────────────────────────────────────

    private fun setupObservers() {

        // New track selected → load into ExoPlayer, reset position
        mainViewModel.currentTrack.observe(this) { track ->
            if (!syncingFromController) {
                track?.let { prepareQueue(it) }
            }
        }

        // Play / pause from UI
        mainViewModel.isPlaying.observe(this) { isPlaying ->
            mediaController?.let { ctrl ->
                when {
                    isPlaying && !ctrl.isPlaying -> {
                        ctrl.play()
                        progressHandler.post(updateProgressRunnable)
                    }
                    !isPlaying && ctrl.isPlaying -> {
                        ctrl.pause()
                        progressHandler.removeCallbacks(updateProgressRunnable)
                    }
                    else -> {}
                }
            }
            miniPlayerPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
            )
        }

        // Explicit seek from the SeekBar (pendingSeek is only set by ViewModel.seekTo())
        // We always honour this — no minimum-delta guard.
        mainViewModel.pendingSeek.observe(this) { positionMs ->
            positionMs ?: return@observe
            mediaController?.seekTo(positionMs.toLong())
            mainViewModel.clearPendingSeek()
        }

        // Mini-player progress bar accent colour
        mainViewModel.accentColor.observe(this) { color ->
            miniPlayerProgress.progressDrawable
                ?.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        }

        // Playback speed
        mainViewModel.playbackSpeed.observe(this) { speed ->
            mediaController?.playbackParameters = PlaybackParameters(speed)
        }
    }

    // ── UI listeners ──────────────────────────────────────────────────────────

    private fun setupListeners() {
        miniPlayerPlayPause.setOnClickListener {
            mainViewModel.setPlaying(!(mainViewModel.isPlaying.value ?: false))
        }

        miniPlayerContainer.setOnClickListener {
            navController.navigate(R.id.navigation_player)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isPlayer = destination.id == R.id.navigation_player
            findViewById<View>(R.id.nav_view).visibility =
                if (isPlayer) View.GONE else View.VISIBLE
            if (isPlayer) {
                miniPlayerContainer.visibility = View.GONE
            } else if (mainViewModel.currentTrack.value != null) {
                if (miniPlayerContainer.visibility != View.VISIBLE) {
                    miniPlayerContainer.visibility = View.VISIBLE
                    miniPlayerContainer.alpha = 0f
                    miniPlayerContainer.animate().alpha(1f).setDuration(300).start()
                }
            }
        }
    }

    // ── Track loading ─────────────────────────────────────────────────────────

    private fun prepareQueue(track: Track) {
        val ctrl = mediaController ?: return
        val queue = buildPlayableQueue(track)
        if (queue.isEmpty()) return
        val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        // Update mini-player UI immediately
        updateMiniPlayer(track)

        // Reset position and duration — real duration arrives via STATE_READY callback
        mainViewModel.setCurrentPosition(0)
        mainViewModel.setDuration(0)
        miniPlayerProgress.progress = 0
        miniPlayerProgress.max      = 0

        val queueSignature = queue.joinToString("|") { "${it.id}:${it.preview}" }
        if (queueSignature != activeQueueSignature) {
            ctrl.setMediaItems(queue.map { it.toMediaItem() }, startIndex, 0L)
            activeQueueSignature = queueSignature
        } else if (ctrl.currentMediaItem?.mediaId != track.id.toString()) {
            ctrl.seekToDefaultPosition(startIndex)
        }
        ctrl.prepare()   // asynchronous — duration arrives in onPlaybackStateChanged
        ctrl.play()

        mainViewModel.setPlaying(true)
        progressHandler.post(updateProgressRunnable)
    }

    private fun buildPlayableQueue(track: Track): List<Track> {
        val playable = mainViewModel.playlist.value.orEmpty()
            .filter { it.preview.isNotBlank() }
        return when {
            playable.any { it.id == track.id } -> playable
            track.preview.isNotBlank() -> listOf(track)
            else -> emptyList()
        }
    }

    private fun Track.toMediaItem(): MediaItem {
        val artworkUrl = album.coverBig ?: album.coverMedium ?: album.coverSmall
        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(preview)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist.name)
                    .setAlbumTitle(album.title)
                    .setArtworkUri(artworkUrl?.let(android.net.Uri::parse))
                    .build()
            )
            .build()
    }

    private fun syncUiWithController(mediaItem: MediaItem?) {
        val trackId = mediaItem?.mediaId?.toLongOrNull() ?: return
        val track = mainViewModel.playlist.value
            ?.firstOrNull { it.id == trackId }
            ?: return
        updateMiniPlayer(track)
        syncingFromController = true
        mainViewModel.syncControllerTrack(trackId)
        syncingFromController = false
        mainViewModel.setCurrentPosition(0)
    }

    private fun updateMiniPlayer(track: Track) {
        miniPlayerContainer.visibility = View.VISIBLE
        miniPlayerTitle.text = track.title
        miniPlayerArtist.text = track.artist.name
        Glide.with(this)
            .load(track.album.coverMedium ?: track.album.coverSmall ?: track.album.coverBig)
            .placeholder(R.color.spotify_light_grey)
            .error(R.color.spotify_light_grey)
            .into(miniPlayerImage)
    }
}
