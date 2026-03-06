package com.example.music.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.MainViewModel
import com.example.music.databinding.FragmentLibraryBinding
import com.example.music.ui.adapters.LibraryTrackAdapter

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var libraryAdapter: LibraryTrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupRecyclerView()
        setupButtons()
        observeData()

        // Auto-load saved tracks on open
        mainViewModel.refreshCloudLibrary()
    }

    private fun setupRecyclerView() {
        libraryAdapter = LibraryTrackAdapter()
        binding.recyclerLibrary.layoutManager = LinearLayoutManager(context)
        binding.recyclerLibrary.adapter = libraryAdapter

        libraryAdapter.setOnItemClickListener { track ->
            mainViewModel.setPlaylist(mainViewModel.savedTracks.value ?: emptyList())
            mainViewModel.playTrack(track)
        }
    }

    private fun setupButtons() {
        binding.btnConnectTest.setOnClickListener {
            mainViewModel.testConnection()
        }

        binding.btnSaveToSupabase.setOnClickListener {
            val current = mainViewModel.currentTrack.value
            if (current != null) {
                mainViewModel.saveCurrentTrackToSupabase()
            } else {
                Toast.makeText(context, "Play a track first to save it ☁️", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLoadSaved.setOnClickListener {
            mainViewModel.refreshCloudLibrary()
        }
    }

    private fun observeData() {
        mainViewModel.savedTracks.observe(viewLifecycleOwner) { tracks ->
            libraryAdapter.setTracks(tracks)
        }

        mainViewModel.savingInProgress.observe(viewLifecycleOwner) { loading ->
            binding.progressSupabase.visibility = if (loading) View.VISIBLE else View.GONE
        }

        mainViewModel.supabaseMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                mainViewModel.clearSupabaseMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
