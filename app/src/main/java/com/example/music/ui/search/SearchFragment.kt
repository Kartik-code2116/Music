package com.example.music.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.MainViewModel
import com.example.music.data.model.Category
import com.example.music.data.repository.AudiusRepository
import com.example.music.data.repository.DeezerRepository
import com.example.music.data.repository.ItunesRepository
import com.example.music.data.repository.JamendoRepository
import com.example.music.databinding.FragmentSearchBinding
import com.example.music.ui.adapters.CategoryAdapter
import com.example.music.ui.adapters.LibraryTrackAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: LibraryTrackAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var mainViewModel: MainViewModel

    private val deezerRepository  = DeezerRepository()
    private val itunesRepository   = ItunesRepository()
    private val jamendoRepository  = JamendoRepository()
    private val audiusRepository   = AudiusRepository()

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupRecyclerViews()
        setupCategories()
        setupSearchBar()
    }

    // ── RecyclerViews ────────────────────────────────────────────────────────

    private fun setupRecyclerViews() {
        searchAdapter = LibraryTrackAdapter()
        binding.recyclerSearchResults.layoutManager = LinearLayoutManager(context)
        binding.recyclerSearchResults.adapter = searchAdapter

        searchAdapter.setOnItemClickListener { track ->
            mainViewModel.playTracks(searchAdapter.getCurrentTracks(), track)
        }
    }

    // ── Categories ───────────────────────────────────────────────────────────

    private fun setupCategories() {
        val categories = listOf(
            Category("Pop",         android.graphics.Color.parseColor("#E13300")),
            Category("Hindi",       android.graphics.Color.parseColor("#1DB954")),
            Category("English",     android.graphics.Color.parseColor("#FC3C44")),
            Category("Rock",        android.graphics.Color.parseColor("#E91429")),
            Category("Hip-Hop",     android.graphics.Color.parseColor("#BC5900")),
            Category("Classical",   android.graphics.Color.parseColor("#7D4B32")),
            Category("Jazz",        android.graphics.Color.parseColor("#7744FF")),
            Category("Indie",       android.graphics.Color.parseColor("#608108")),
            Category("R&B",         android.graphics.Color.parseColor("#DC148C")),
            Category("K-Pop",       android.graphics.Color.parseColor("#148A08")),
            Category("Workout",     android.graphics.Color.parseColor("#477D95")),
            Category("Sleep",       android.graphics.Color.parseColor("#1E3264")),
            Category("Party",       android.graphics.Color.parseColor("#AF2896")),
            Category("Focus",       android.graphics.Color.parseColor("#503750")),
            Category("Metal",       android.graphics.Color.parseColor("#5C1A1A")),
            Category("Latin",       android.graphics.Color.parseColor("#C47E04")),
            Category("Electronic",  android.graphics.Color.parseColor("#0D6E6E")),
            Category("Country",     android.graphics.Color.parseColor("#8B5E3C"))
        )

        categoryAdapter = CategoryAdapter(categories)
        binding.recyclerCategories.adapter = categoryAdapter

        // Tapping a category triggers a multi-source search for that genre
        categoryAdapter.setOnItemClickListener { category ->
            binding.searchBar.setText(category.name)
            binding.searchBar.setSelection(binding.searchBar.text?.length ?: 0)
            showResults()
            performMultiSearch(category.name)
        }
    }

    // ── Search Bar ───────────────────────────────────────────────────────────

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    showResults()
                    if (query.length >= 2) {
                        searchJob?.cancel()
                        searchJob = viewLifecycleOwner.lifecycleScope.launch {
                            delay(400) // debounce — wait for user to pause typing
                            performMultiSearch(query)
                        }
                    }
                } else {
                    searchJob?.cancel()
                    searchAdapter.setTracks(emptyList())
                    showCategories()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Clear button (right-side drawable tap)
        binding.searchBar.setOnTouchListener { _, event ->
            val drawableEnd = 2
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawable = binding.searchBar.compoundDrawables[drawableEnd]
                if (drawable != null &&
                    event.rawX >= (binding.searchBar.right - drawable.bounds.width())
                ) {
                    binding.searchBar.setText("")
                    searchAdapter.setTracks(emptyList())
                    showCategories()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    // ── Multi-source Search ──────────────────────────────────────────────────

    /**
     * Fires search requests to Deezer, iTunes and Jamendo simultaneously,
     * then merges the results (de-duplicated by title+artist) and displays them.
     */
    private fun performMultiSearch(query: String) {
        if (query.isBlank()) return

        // Dim list to signal loading
        binding.recyclerSearchResults.alpha = 0.5f

        viewLifecycleOwner.lifecycleScope.launch {
            val deezerDeferred  = async { deezerRepository.searchTracks(query,  limit = 20) }
            val itunesDeferred  = async { itunesRepository.searchTracks(query,  limit = 15) }
            val jamendoDeferred = async { jamendoRepository.searchTracks(query, limit = 15) }
            val audiusDeferred  = async { audiusRepository.searchTracks(query,  limit = 25) }

            val deezerTracks  = deezerDeferred.await().getOrElse { emptyList() }
            val itunesTracks  = itunesDeferred.await().getOrElse { emptyList() }
            val jamendoTracks = jamendoDeferred.await().getOrElse { emptyList() }
            val audiusTracks  = audiusDeferred.await().getOrElse { emptyList() }

            // Interleave results: D, I, J, D, I, J … so every source is visible
            val combined = buildList {
                val d = deezerTracks.iterator()
                val i = itunesTracks.iterator()
                val j = jamendoTracks.iterator()
                val a = audiusTracks.iterator()
                while (d.hasNext() || i.hasNext() || j.hasNext() || a.hasNext()) {
                    if (d.hasNext()) add(d.next())
                    if (i.hasNext()) add(i.next())
                    if (j.hasNext()) add(j.next())
                    if (a.hasNext()) add(a.next())
                }
            }

            // De-duplicate by normalised "title · artist" key
            val seen = mutableSetOf<String>()
            val deduped = combined.filter { track ->
                val key = "${track.title.lowercase().trim()}|${track.artist.name.lowercase().trim()}"
                seen.add(key)
            }

            binding.recyclerSearchResults.alpha = 1f
            searchAdapter.setTracks(deduped)

            if (deduped.isEmpty()) {
                Toast.makeText(
                    context,
                    "No results found for \"$query\"",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ── Visibility Helpers ───────────────────────────────────────────────────

    private fun showResults() {
        binding.browseAllHeader.visibility    = View.GONE
        binding.recyclerCategories.visibility = View.GONE
        binding.recyclerSearchResults.visibility = View.VISIBLE
    }

    private fun showCategories() {
        binding.browseAllHeader.visibility    = View.VISIBLE
        binding.recyclerCategories.visibility = View.VISIBLE
        binding.recyclerSearchResults.visibility = View.GONE
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
