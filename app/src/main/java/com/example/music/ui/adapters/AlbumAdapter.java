package com.example.music.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.music.R;
import com.example.music.data.model.Album;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.DiffUtil;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private List<Album> albums = new ArrayList<>();

    public void setAlbums(List<Album> newAlbums) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return albums.size();
            }

            @Override
            public int getNewListSize() {
                return newAlbums.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return albums.get(oldItemPosition).getId() == newAlbums.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return albums.get(oldItemPosition).getTitle().equals(newAlbums.get(newItemPosition).getTitle());
            }
        });

        this.albums = new ArrayList<>(newAlbums);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track_square, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.title.setText(album.getTitle());
        holder.subtitle.setText("Album");

        Glide.with(holder.itemView.getContext())
                .load(album.getCoverMedium())
                .placeholder(R.color.spotify_light_grey)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.text_title);
            subtitle = itemView.findViewById(R.id.text_subtitle);
        }
    }
}
