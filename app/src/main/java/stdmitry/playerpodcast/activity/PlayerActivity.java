package stdmitry.playerpodcast.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import stdmitry.playerpodcast.R;
import stdmitry.playerpodcast.adapter.PlayListAdapter;
import stdmitry.playerpodcast.services.DownloadPodcustService;


public class PlayerActivity extends AppCompatActivity implements PlayListAdapter.PlayListAdapterListener {

    private RecyclerView recyclerView;
    private PlayListAdapter adapter;
    private ProgressBar progressBar;

    public static final String EXTRAKEY = "url";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Log.d("onReceice", "HERE");
            if (bundle != null) {
                Log.d("onReceice", "HERE in bundle");
                String string = bundle.getString(DownloadPodcustService.FILEPATH);
                int resultCode = bundle.getInt(DownloadPodcustService.RESULT);
                if (resultCode == RESULT_OK) {
                    adapter = new PlayListAdapter(context, PlayerActivity.this);
                    recyclerView.setAdapter(adapter);

                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(mLayoutManager);
                    Toast.makeText(PlayerActivity.this, "CONNECT!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PlayerActivity.this, "Download failed", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        Intent intent = new Intent(this, DownloadPodcustService.class);
        intent.putExtra(DownloadPodcustService.FILENAME, "radio-t.html");
        intent.putExtra(DownloadPodcustService.URLPATH, "http://feeds.rucast.net/radio-t.html");
        startService(intent);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClickItem(String url) {
        Intent intent = new Intent(PlayerActivity.this, ItemActivity.class);
        intent.putExtra(EXTRAKEY, url);
        if (url == null) {
            Toast.makeText(this, "Podcast doesn`t has mp3", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                DownloadPodcustService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


}
