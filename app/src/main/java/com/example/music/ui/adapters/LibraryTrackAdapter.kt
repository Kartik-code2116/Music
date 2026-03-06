package com.example.music.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.data.model.Track
import com.example.music.databinding.ItemLibraryRowBinding

class LibraryTrackAdapter : RecyclerView.Adapter<LibraryTrackAdapter.ViewHolder>() {

    private var tracks: List<Track> = emptyList()
    private var onItemClickListener: ((Track) -> Unit)? = null
    private var onMoreClickListener: ((Track) -> Unit)? = null

    fun setTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    fun getCurrentTracks(): List<Track> = tracks

    fun setOnItemClickListener(listener: (Track) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnMoreClickListener(listener: (Track) -> Unit) {
        onMoreClickListener = listener
    }

    inner class ViewHolder(private val binding: ItemLibraryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {
            binding.libraryItemTitle.text = track.title
            binding.libraryItemSubtitle.text = track.artist.name

            Glide.with(binding.root.context)
                .load(track.album.coverMedium)
                .placeholder(R.color.spotify_light_grey)
                .error(R.color.spotify_light_grey)
                .centerCrop()
                .into(binding.libraryItemImage)

            binding.root.setOnClickListener {
                onItemClickListener?.invoke(track)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLibraryRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size
}
