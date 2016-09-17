package stdmitry.playerpodcast.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import stdmitry.playerpodcast.R;

public class PlayListViewHolder extends RecyclerView.ViewHolder {

    public TextView title, time;
    private ImageView jpg;

    public PlayListViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.name);
        jpg = (ImageView) itemView.findViewById(R.id.jpg);
        time = (TextView) itemView.findViewById(R.id.time);
    }
}
