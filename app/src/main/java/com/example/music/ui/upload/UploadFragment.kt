package com.example.music.ui.upload

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.MainViewModel
import com.example.music.data.api.RetrofitClient
import com.example.music.data.repository.MusicRepository
import com.example.music.databinding.FragmentUploadBinding
import com.example.music.ui.adapters.LibraryTrackAdapter

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var communityAdapter: LibraryTrackAdapter
    private lateinit var musicRepository: MusicRepository

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                selectedFileName = getFileName(uri)
                // Show selected file card
                binding.selectedFileCard.visibility = View.VISIBLE
                binding.textSelectedFile.text = selectedFileName ?: "audio_file.mp3"
                // Update drop zone appearance
                binding.dropZone.strokeColor =
                    requireContext().getColor(com.example.music.R.color.spotify_green)
                // Auto-fill title from filename
                if (binding.editSongTitle.text.isNullOrBlank()) {
                    val nameWithoutExt = selectedFileName
                        ?.substringBeforeLast(".")
                        ?.replace("_", " ")
                        ?.replace("-", " ")
                    binding.editSongTitle.setText(nameWithoutExt)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        musicRepository = MusicRepository(RetrofitClient.api)

        setupRecycler()
        setupListeners()
        loadCommunityUploads()
    }

    private fun setupRecycler() {
        communityAdapter = LibraryTrackAdapter()
        binding.recyclerCommunityUploads.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCommunityUploads.adapter = communityAdapter
        binding.recyclerCommunityUploads.isNestedScrollingEnabled = false

        communityAdapter.setOnItemClickListener { track ->
            mainViewModel.playTrack(track)
        }
    }

    private fun setupListeners() {
        // Tapping drop zone or Browse button both open file picker
        val openPicker = View.OnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            filePickerLauncher.launch(Intent.createChooser(intent, "Select Audio File"))
        }
        binding.dropZone.setOnClickListener(openPicker)
        binding.btnSelectFile.setOnClickListener(openPicker)

        binding.btnUpload.setOnClickListener {
            val title = binding.editSongTitle.text?.toString()?.trim()
            val artist = binding.editArtistName.text?.toString()?.trim()
            val uri = selectedFileUri
            val fileName = selectedFileName

            when {
                title.isNullOrBlank() ->
                    Toast.makeText(context, "Please enter a song title", Toast.LENGTH_SHORT).show()
                artist.isNullOrBlank() ->
                    Toast.makeText(context, "Please enter an artist name", Toast.LENGTH_SHORT).show()
                uri == null || fileName == null ->
                    Toast.makeText(context, "Please select an audio file", Toast.LENGTH_SHORT).show()
                else -> {
                    val fileBytes = readFileBytes(uri) ?: run {
                        Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    mainViewModel.uploadTrack(title, artist, fileBytes, fileName)
                    Toast.makeText(context, "Uploading \"$title\"... ☁️", Toast.LENGTH_SHORT).show()
                    clearForm()
                }
            }
        }

        mainViewModel.savingInProgress.observe(viewLifecycleOwner) { inProgress ->
            binding.progressUpload.visibility = if (inProgress) View.VISIBLE else View.GONE
            binding.btnUpload.isEnabled = !inProgress
            binding.btnUpload.text = if (inProgress) "Uploading..." else "Upload"
            binding.btnSelectFile.isEnabled = !inProgress
        }

        mainViewModel.supabaseMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (message.contains("Successfully", ignoreCase = true)) {
                    loadCommunityUploads()
                }
                mainViewModel.clearSupabaseMessage()
            }
        }
    }

    private fun loadCommunityUploads() {
        musicRepository.getCloudTracks().observe(viewLifecycleOwner) { tracks ->
            if (!tracks.isNullOrEmpty()) {
                communityAdapter.setTracks(tracks)
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name ?: uri.lastPathSegment
    }

    private fun readFileBytes(uri: Uri): ByteArray? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }

    private fun clearForm() {
        binding.editSongTitle.setText("")
        binding.editArtistName.setText("")
        binding.selectedFileCard.visibility = View.GONE
        binding.textSelectedFile.text = "audio_file.mp3"
        binding.dropZone.strokeColor =
            requireContext().getColor(com.example.music.R.color.stroke_subtle)
        selectedFileUri = null
        selectedFileName = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
