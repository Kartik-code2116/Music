package com.example.music.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.data.model.Track
import com.example.music.databinding.ItemRecentMiniBinding

class HomeGridAdapter(
    private val tracks: List<Track>,
    private val onClick: (Track) -> Unit
) : RecyclerView.Adapter<HomeGridAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRecentMiniBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {
            binding.textMiniTitle.text = track.title
            Glide.with(binding.root.context)
                .load(track.album.coverMedium)
                .placeholder(R.color.spotify_light_grey)
                .centerCrop()
                .into(binding.imageMini)
            binding.root.setOnClickListener { onClick(track) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentMiniBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(tracks[position])

    override fun getItemCount() = tracks.size
}
