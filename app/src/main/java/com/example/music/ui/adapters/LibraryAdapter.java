package com.example.music.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.music.R;
import com.example.music.data.model.Track;
import java.util.ArrayList;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private List<Track> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Track track);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Track> newItems) {
        if (newItems == null)
            return;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return items.get(oldPos).getId() == newItems.get(newPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Track o = items.get(oldPos);
                Track n = newItems.get(newPos);
                return o.getTitle().equals(n.getTitle()) &&
                        o.getArtist().getName().equals(n.getArtist().getName());
            }
        });

        this.items = new ArrayList<>(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.subtitle.setText("Playlist • " + item.getArtist().getName());

        Glide.with(holder.itemView.getContext())
                .load(item.getAlbum().getCoverMedium())
                .placeholder(R.color.spotify_light_grey)
                .error(R.color.spotify_dark_grey)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.library_item_image);
            title = itemView.findViewById(R.id.library_item_title);
            subtitle = itemView.findViewById(R.id.library_item_subtitle);
        }
    }
}
