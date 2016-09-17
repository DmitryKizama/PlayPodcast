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
import android.widget.SeekBar;
import android.widget.Toast;

import stdmitry.playerpodcast.R;
import stdmitry.playerpodcast.services.PlayPodcastService;

public class ItemActivity extends AppCompatActivity {

    public final static String BROADCAST_ACTION_ITEMACTIVITY = "ItemActivity";
    public final static String BROADCAST_TASK_PLAYER_PROGRESS_ITEMACTIVITY = "ItemActivityProgress";
    public final static String BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY = "MaxDuration";

    private Button btnPlay;
    private SeekBar seekBar;

    private boolean playPause = false;
    private BroadcastReceiver broadcastReceiver;
    private String url;
    private boolean bound = false;
    private ServiceConnection sConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    private Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        getExtras(savedInstanceState);

        final Intent intentAction = new Intent(PlayPodcastService.BROADCAST_ACTION);

        btnPlay = (Button) findViewById(R.id.btn_play);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setProgress(0);

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("onClickSeekBar","setOnTouchListener");
                intentAction.putExtra(PlayPodcastService.BROADCAST_TASK_SEEK_BAR_PROGRESS, seekBar.getProgress());
                sendBroadcast(intentAction);
                return false;
            }
        });



        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int currentDuration = intent.getIntExtra(BROADCAST_TASK_PLAYER_PROGRESS_ITEMACTIVITY, 0);
                int max = intent.getIntExtra(BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY, 1);
                double result = (double) currentDuration * (double) seekBar.getMax() / (double) max;
                seekBar.setProgress((int) result);
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
                Log.d(PlayPodcastService.LOGD, "onClick in Item");

                if (url == null) {
                    Toast.makeText(view.getContext(), "Podcast hasn`t mp3", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(PlayPodcastService.LOGD, "url = " + url);
                if (!playPause) {
                    Log.d(PlayPodcastService.LOGD, "Play");
//                    btnPlay.changePicture()
                    intentAction.putExtra(PlayPodcastService.BROADCAST_TASK_SEEK_BAR_PROGRESS, seekBar.getProgress());
                    intentAction.putExtra(PlayPodcastService.BROADCAST_TASK_SEEK_BAR_MAX, seekBar.getMax());
                    intentAction.putExtra(PlayPodcastService.BROADCAST_TASK, true);
                    sendBroadcast(intentAction);
                    playPause = true;
                } else {
                    Log.d(PlayPodcastService.LOGD, "Pause");
//                    btnPlay.changePicture()
                    intentAction.putExtra(PlayPodcastService.BROADCAST_TASK, false);
                    sendBroadcast(intentAction);
                    playPause = false;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!bound) return;
        unbindService(sConn);
        bound = false;
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
