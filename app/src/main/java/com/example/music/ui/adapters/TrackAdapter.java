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
import com.example.music.data.model.Track;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.DiffUtil;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {

    private List<Track> tracks = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Track track);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTracks(List<Track> newTracks) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return tracks.size();
            }

            @Override
            public int getNewListSize() {
                return newTracks.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return tracks.get(oldItemPosition).getId() == newTracks.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Track oldTrack = tracks.get(oldItemPosition);
                Track newTrack = newTracks.get(newItemPosition);
                return oldTrack.getTitle().equals(newTrack.getTitle()) &&
                        oldTrack.getArtist().getName().equals(newTrack.getArtist().getName());
            }
        });

        this.tracks = new ArrayList<>(newTracks);
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
        Track track = tracks.get(position);
        holder.title.setText(track.getTitle());
        holder.subtitle.setText(track.getArtist().getName());

        String coverUrl = null;
        if (track.getAlbum() != null) {
            coverUrl = track.getAlbum().getCoverMedium();
        }

        Glide.with(holder.itemView.getContext())
                .load(coverUrl)
                .placeholder(R.color.spotify_light_grey)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(track);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
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
