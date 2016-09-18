package stdmitry.playerpodcast.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import stdmitry.playerpodcast.R;
import stdmitry.playerpodcast.database.Podcast;
import stdmitry.playerpodcast.services.PlayPodcastService;

public class ItemActivity extends AppCompatActivity {

    public final static String BROADCAST_ACTION_ITEMACTIVITY = "ItemActivity";
    public final static String BROADCAST_TASK_PLAYER_PROGRESS_ITEMACTIVITY = "ItemActivityProgress";
    public final static String BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY = "MaxDuration";
    public final static String PROGRESS_BAR_SHOW = "ProgressBarAction";
    public final static String FINISH_PLAY = "Close";

    private Button btnPlay;
    private SeekBar seekBar;
    private TextView description, date, authors, title;
    private ImageView imageView;
    private ProgressBar progressBar;

    private boolean playPause = false;
    private BroadcastReceiver broadcastReceiver;
    private String url;
    private boolean bound = false;
    private int maxDuration;

    private ServiceConnection sConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("ALOHA", "onServiceConnected!");
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("ALOHA", "onServiceDisconnected!");
            bound = false;
        }
    };
    private Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ALOHA", "ON CREATE!");
        setContentView(R.layout.activity_item);
        getExtras(savedInstanceState);
        Podcast podcast = Podcast.selectByMP3(url);
        final Intent intentAction = new Intent(PlayPodcastService.BROADCAST_ACTION);

        progressBar = (ProgressBar) findViewById(R.id.progressBarItem);
        progressBar.setVisibility(View.VISIBLE);
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnPlay.setVisibility(View.GONE);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setProgress(0);
        description = (TextView) findViewById(R.id.description);
        authors = (TextView) findViewById(R.id.authors);
        date = (TextView) findViewById(R.id.date);
        title = (TextView) findViewById(R.id.title);
        imageView = (ImageView) findViewById(R.id.img_view);
        imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                int size = Math.min(imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
                imageView.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            }
        });

        description.setText("Description: " + podcast.getDescription());
        authors.setText("Authors: " + podcast.getAuthor());
        date.setText("Date: " + podcast.getPubdate());
        title.setText(podcast.getTitle());


        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("onClickSeekBar", "setOnTouchListener");
                if (!playPause) {
                    btnPlay.setBackgroundResource(R.mipmap.ic_pause);
                    playPause = true;
                }
                progressBar.setVisibility(View.VISIBLE);
                intentAction.putExtra(PlayPodcastService.BROADCAST_TASK_SEEK_BAR_PROGRESS, seekBar.getProgress());
                intentAction.putExtra(PlayPodcastService.BROADCAST_TASK_SEEK_BAR_MAX, seekBar.getMax());
                intentAction.putExtra(PlayPodcastService.BROADCAST_TASK, true);
                sendBroadcast(intentAction);
                return false;
            }
        });

        broadcastReceiver = new BroadcastReceiver() {


            public void onReceive(Context context, Intent intent) {
                int currentDuration = intent.getIntExtra(BROADCAST_TASK_PLAYER_PROGRESS_ITEMACTIVITY, 0);
                maxDuration = intent.getIntExtra(BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY, maxDuration);
                double result = (double) currentDuration * (double) seekBar.getMax() / (double) maxDuration;
                seekBar.setProgress((int) result);
                boolean flag = intent.getBooleanExtra(PROGRESS_BAR_SHOW, false);
                if (flag) {
                    progressBar.setVisibility(View.GONE);
                    btnPlay.setVisibility(View.VISIBLE);
                }
                if (intent.getBooleanExtra(FINISH_PLAY, false)) {
                    setPlayBtnResourse(true);
                }
            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION_ITEMACTIVITY);
        registerReceiver(broadcastReceiver, intFilt);

        intent = new Intent(this, PlayPodcastService.class);
        intent.putExtra(PlayPodcastService.URLKEY, url);
        bindService(intent, sConn, Context.BIND_AUTO_CREATE);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playPause) {
                    setPlayBtnResourse(false);
                    progressBar.setVisibility(View.VISIBLE);
                    Log.d(PlayPodcastService.LOGD, "Play");
                    intentAction.putExtra(PlayPodcastService.BROADCAST_TASK_SEEK_BAR_PROGRESS, seekBar.getProgress());
                    intentAction.putExtra(PlayPodcastService.BROADCAST_TASK_SEEK_BAR_MAX, seekBar.getMax());
                    intentAction.putExtra(PlayPodcastService.BROADCAST_TASK, true);
                    sendBroadcast(intentAction);
                } else {
                    setPlayBtnResourse(true);
                    Log.d(PlayPodcastService.LOGD, "Pause");
                    intentAction.putExtra(PlayPodcastService.BROADCAST_TASK, false);
                    sendBroadcast(intentAction);
                }
            }
        });
    }

    private void setPlayBtnResourse(boolean isPlay){
        if (!isPlay){
            btnPlay.setBackgroundResource(R.mipmap.ic_pause);
            playPause = true;
        } else {
            btnPlay.setBackgroundResource(R.mipmap.ic_play);
            playPause = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!bound) return;
        unbindService(sConn);
        bound = false;
        unregisterReceiver(broadcastReceiver);
    }

    private void getExtras(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                url = null;
            } else {
                url = extras.getString(PlayerActivity.EXTRAKEY);
            }
        } else {
            url = (String) savedInstanceState.getSerializable(PlayerActivity.EXTRAKEY);
        }
    }
}
