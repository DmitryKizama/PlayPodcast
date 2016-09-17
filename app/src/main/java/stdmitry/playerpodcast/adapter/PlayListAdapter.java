package stdmitry.playerpodcast.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import stdmitry.playerpodcast.R;
import stdmitry.playerpodcast.database.Podcast;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListViewHolder> {

    public interface PlayListAdapterListener {
        void onClickItem(String url);
    }

    private PlayListAdapterListener listener;
    private List<Podcast> list;

    public PlayListAdapter(Context context, PlayListAdapterListener lis) {
        list = Podcast.getAll();
        this.listener = lis;
        Log.d("Adapter", "size = " + list.size());
    }

    @Override
    public PlayListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.podcust_item, parent, false);
        return new PlayListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PlayListViewHolder holder, int position) {
        final Podcast p = list.get(holder.getAdapterPosition());
//        Log.d("Adapter", "On bind");
//        Log.d("Adapter", "title = " + p.getTitle());
        holder.title.setText(p.getTitle());

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickItem(p.getMp3());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
