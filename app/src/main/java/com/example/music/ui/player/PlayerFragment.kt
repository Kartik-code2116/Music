package com.example.music.ui.player

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.music.MainViewModel
import com.example.music.R
import com.example.music.databinding.FragmentPlayerBinding
import java.util.Locale
import kotlin.math.max

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel

    private var isUserSeeking = false
    private var sleepTimer: CountDownTimer? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupSeekBar()
        setupObservers()
        setupClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelSleepTimer()
        _binding = null
    }

    // ── SeekBar Setup ─────────────────────────────────────────────────────────

    private fun setupSeekBar() {
        binding.playerSeekbar.apply {

            // User starts scrubbing — stop auto-updating the bar
            onStartTracking = {
                isUserSeeking = true
            }

            // Live scrub — update the current-time label as the user drags
            onProgressChanged = { progress, fromUser ->
                if (fromUser) {
                    val duration = mainViewModel.duration.value ?: 0
                    val positionMs = (progress * duration).toInt()
                    binding.tvCurrentTime.text = formatTime(positionMs)
                }
            }

            // User lifts finger — seek to the chosen position
            onStopTracking = { progress ->
                isUserSeeking = false
                val duration = mainViewModel.duration.value ?: 0
                val positionMs = (progress * duration).toInt()
                mainViewModel.seekTo(positionMs)
            }
        }
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun setupObservers() {

        // Track changed — load artwork and update labels
        mainViewModel.currentTrack.observe(viewLifecycleOwner) { track ->
            track ?: return@observe
            binding.playerSongTitle.text  = track.title
            binding.playerArtistName.text = track.artist.name

            Glide.with(this)
                .asBitmap()
                .load(track.album.coverBig ?: track.album.coverMedium)
                .placeholder(R.color.spotify_light_grey)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        binding.playerAlbumArt.setImageBitmap(resource)
                        updateBackground(resource)
                    }
                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                })
        }

        // Play / pause icon
        mainViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            binding.btnPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
            )
        }

        // Duration — update total-time label only (seekbar uses 0..1 internally)
        mainViewModel.duration.observe(viewLifecycleOwner) { duration ->
            binding.tvTotalTime.text = formatTime(duration)
        }

        // Playback position — push to seekbar only when the user isn't scrubbing
        mainViewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            if (!isUserSeeking) {
                val duration = max(1, mainViewModel.duration.value ?: 1)
                binding.playerSeekbar.setProgress(position.toFloat() / duration)
                binding.tvCurrentTime.text = formatTime(position)
            }
        }

        // Accent colour extracted from the album art — applied to seekbar glow
        mainViewModel.accentColor.observe(viewLifecycleOwner) { color ->
            binding.playerSeekbar.accentColor = color
        }

        // Shuffle state
        mainViewModel.isShuffle.observe(viewLifecycleOwner) { isShuffle ->
            binding.btnShuffle.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    if (isShuffle) R.color.spotify_green else R.color.spotify_grey
                )
            )
        }

        // Repeat state
        mainViewModel.isRepeat.observe(viewLifecycleOwner) { isRepeat ->
            binding.btnRepeat.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    if (isRepeat) R.color.spotify_green else R.color.spotify_grey
                )
            )
        }

        // Favourite state
        mainViewModel.favoriteIds.observe(viewLifecycleOwner) { favoriteIds ->
            mainViewModel.currentTrack.value?.let { track ->
                val isFav = favoriteIds.contains(track.id)
                binding.btnLike.setImageResource(
                    if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                )
                binding.btnLike.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isFav) R.color.spotify_green else R.color.white
                    )
                )
            }
        }

        // Supabase save messages
        mainViewModel.supabaseMessage.observe(viewLifecycleOwner) { message ->
            message ?: return@observe
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (message.contains("saved", ignoreCase = true)) {
                binding.btnSaveCloud.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.spotify_green)
                )
            }
            mainViewModel.clearSupabaseMessage()
        }
    }

    // ── Click Listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {

        binding.btnClose.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPlayPause.setOnClickListener {
            val playing = mainViewModel.isPlaying.value ?: false
            mainViewModel.setPlaying(!playing)
        }

        binding.btnNext.setOnClickListener     { mainViewModel.shiftTrack(true)  }
        binding.btnPrevious.setOnClickListener { mainViewModel.shiftTrack(false) }

        binding.btnShuffle.setOnClickListener  { mainViewModel.toggleShuffle() }
        binding.btnRepeat.setOnClickListener   { mainViewModel.toggleRepeat()  }

        binding.btnLike.setOnClickListener {
            mainViewModel.currentTrack.value?.let { mainViewModel.toggleFavorite(it.id) }
        }

        binding.btnShare.setOnClickListener {
            mainViewModel.currentTrack.value?.let { track ->
                val text = "🎵 ${track.title} by ${track.artist.name}\nListening on Music App"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(Intent.createChooser(intent, "Share via"))
            }
        }

        binding.btnQueue.setOnClickListener { showQueueBottomSheet() }

        // Long-press = sleep timer, tap = playback speed
        binding.btnDevices.setOnLongClickListener {
            showSleepTimerDialog()
            true
        }
        binding.btnDevices.setOnClickListener { showSpeedControlDialog() }

        binding.btnSaveCloud.setOnClickListener {
            mainViewModel.saveCurrentTrackToSupabase()
        }
    }

    // ── Background / Palette ─────────────────────────────────────────────────

    private fun updateBackground(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            palette ?: return@generate
            val dominant = palette.getDominantColor(
                ContextCompat.getColor(requireContext(), R.color.spotify_black)
            )
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(dominant, ContextCompat.getColor(requireContext(), R.color.spotify_black))
            )
            binding.dynamicBackground.background = gradient
            mainViewModel.setAccentColor(dominant)
        }
    }

    // ── Sleep Timer ───────────────────────────────────────────────────────────

    private fun showSleepTimerDialog() {
        val labels   = arrayOf("5 minutes", "15 minutes", "30 minutes", "1 hour", "Cancel timer")
        val minuteMs = longArrayOf(5, 15, 30, 60, -1)

        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Sleep Timer")
            .setItems(labels) { _, which ->
                if (minuteMs[which] == -1L) cancelSleepTimer()
                else startSleepTimer(minuteMs[which])
            }
            .show()
    }

    private fun startSleepTimer(minutes: Long) {
        cancelSleepTimer()
        sleepTimer = object : CountDownTimer(minutes * 60_000, 60_000) {
            override fun onTick(ms: Long) {
                val left = (ms / 60_000).toInt() + 1
                Toast.makeText(context, "Sleep timer: $left min left", Toast.LENGTH_SHORT).show()
            }
            override fun onFinish() {
                mainViewModel.setPlaying(false)
                Toast.makeText(context, "Sleep timer ended. Good night! 🌙", Toast.LENGTH_LONG).show()
            }
        }.start()
        Toast.makeText(context, "Sleep timer set for $minutes minutes", Toast.LENGTH_SHORT).show()
    }

    private fun cancelSleepTimer() {
        sleepTimer?.cancel()
        sleepTimer = null
    }

    // ── Playback Speed ────────────────────────────────────────────────────────

    private fun showSpeedControlDialog() {
        val labels = arrayOf("0.5×", "0.75×", "1× (Normal)", "1.25×", "1.5×", "2×")
        val values = floatArrayOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Playback Speed")
            .setItems(labels) { _, which ->
                mainViewModel.setPlaybackSpeed(values[which])
                Toast.makeText(context, "Speed: ${labels[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // ── Queue ─────────────────────────────────────────────────────────────────

    private fun showQueueBottomSheet() {
        val tracks = mainViewModel.playlist.value ?: emptyList()
        if (tracks.isEmpty()) {
            Toast.makeText(context, "Queue is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val currentId = mainViewModel.currentTrack.value?.id
        val names = tracks.mapIndexed { i, t ->
            val marker = if (t.id == currentId) "▶ " else "   "
            "$marker${i + 1}. ${t.title} · ${t.artist.name}"
        }.toTypedArray()

        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Up Next")
            .setItems(names) { _, which -> mainViewModel.playTrack(tracks[which]) }
            .setNegativeButton("Close", null)
            .show()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun formatTime(ms: Int): String {
        val totalSec = ms / 1000
        val min      = totalSec / 60
        val sec      = totalSec % 60
        return String.format(Locale.getDefault(), "%d:%02d", min, sec)
    }
}
