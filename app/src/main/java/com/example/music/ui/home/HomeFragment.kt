package com.example.music.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.music.MainViewModel
import com.example.music.databinding.FragmentHomeBinding
import com.example.music.ui.adapters.AlbumAdapter
import com.example.music.ui.adapters.HomeGridAdapter
import com.example.music.ui.adapters.TrackAdapter
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mainViewModel: MainViewModel

    // Deezer
    private lateinit var deezerTrackAdapter: TrackAdapter
    private lateinit var deezerAlbumAdapter: AlbumAdapter

    // iTunes
    private lateinit var itunesTrackAdapter: TrackAdapter

    // Jamendo
    private lateinit var jamendoTrackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setGreeting()
        setupRecyclerViews()
        observeData()

        homeViewModel.fetchCharts()
    }

    private fun setupRecyclerViews() {
        // ── Quick-access grid (2 columns) ──
        binding.recyclerViewGrid.layoutManager = GridLayoutManager(context, 2)

        // ── Deezer: Trending Now (horizontal) ──
        binding.recyclerViewNewReleases.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        deezerTrackAdapter = TrackAdapter()
        binding.recyclerViewNewReleases.adapter = deezerTrackAdapter
        deezerTrackAdapter.setOnItemClickListener { track ->
            val playlist = homeViewModel.tracks.value ?: emptyList()
            mainViewModel.setPlaylist(playlist)
            mainViewModel.playTrack(track)
        }

        // ── iTunes: Top Picks (horizontal) ──
        binding.recyclerViewItunes.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        itunesTrackAdapter = TrackAdapter()
        binding.recyclerViewItunes.adapter = itunesTrackAdapter
        itunesTrackAdapter.setOnItemClickListener { track ->
            val playlist = homeViewModel.itunesTracks.value ?: emptyList()
            mainViewModel.setPlaylist(playlist)
            mainViewModel.playTrack(track)
        }

        // ── Jamendo: Free Full Songs (horizontal) ──
        binding.recyclerViewJamendo.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        jamendoTrackAdapter = TrackAdapter()
        binding.recyclerViewJamendo.adapter = jamendoTrackAdapter
        jamendoTrackAdapter.setOnItemClickListener { track ->
            val playlist = homeViewModel.jamendoTracks.value ?: emptyList()
            mainViewModel.setPlaylist(playlist)
            mainViewModel.playTrack(track)
        }

        // ── Deezer: Top Albums (horizontal) ──
        binding.recyclerViewTopMixes.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        deezerAlbumAdapter = AlbumAdapter()
        binding.recyclerViewTopMixes.adapter = deezerAlbumAdapter
    }

    private fun observeData() {

        // Deezer chart tracks → grid + trending row
        homeViewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            if (tracks.isNullOrEmpty()) return@observe

            mainViewModel.setPlaylist(tracks)
            deezerTrackAdapter.setTracks(tracks)

            val gridTracks = tracks.take(6)
            binding.recyclerViewGrid.adapter =
                HomeGridAdapter(gridTracks) { track ->
                    mainViewModel.setPlaylist(tracks)
                    mainViewModel.playTrack(track)
                }
        }

        // Deezer chart albums → top albums row
        homeViewModel.deezerAlbums.observe(viewLifecycleOwner) { deezerAlbums ->
            deezerAlbumAdapter.setAlbums(deezerAlbums.map { it.toAlbum() })
        }

        // iTunes top hits
        homeViewModel.itunesTracks.observe(viewLifecycleOwner) { tracks ->
            if (!tracks.isNullOrEmpty()) {
                itunesTrackAdapter.setTracks(tracks)
            }
        }

        // Jamendo free full songs
        homeViewModel.jamendoTracks.observe(viewLifecycleOwner) { tracks ->
            if (!tracks.isNullOrEmpty()) {
                jamendoTrackAdapter.setTracks(tracks)
            }
        }

        // Loading state — dim rows while fetching
        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            val alpha = if (loading) 0.4f else 1f
            binding.recyclerViewGrid.alpha = alpha
            binding.recyclerViewNewReleases.alpha = alpha
            binding.recyclerViewItunes.alpha = alpha
            binding.recyclerViewJamendo.alpha = alpha
        }

        // Non-fatal error toast
        homeViewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.textGreeting.text = when (hour) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            else      -> "Good evening"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
