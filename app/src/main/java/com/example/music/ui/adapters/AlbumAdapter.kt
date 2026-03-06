package com.example.music.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.data.model.Album
import com.example.music.databinding.ItemTrackSquareBinding

class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    private var albums: List<Album> = emptyList()
    private var onItemClickListener: ((Album) -> Unit)? = null

    fun setAlbums(newAlbums: List<Album>) {
        albums = newAlbums
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Album) -> Unit) {
        onItemClickListener = listener
    }

    inner class ViewHolder(private val binding: ItemTrackSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.textTitle.text = album.title
            binding.textSubtitle.text = "Album"
            Glide.with(binding.root)
                .load(album.coverMedium)
                .placeholder(R.color.spotify_light_grey)
                .into(binding.imageView)
            binding.root.setOnClickListener { onItemClickListener?.invoke(album) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrackSquareBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(albums[position])

    override fun getItemCount() = albums.size
}
