package stdmitry.playerpodcast.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import stdmitry.playerpodcast.activity.ItemActivity;

public class PlayPodcastService extends Service {

    public static final String URLKEY = "urlkey";
    public static final String LOGD = "DebugPlayPodcastService";
    public final static String BROADCAST_ACTION = "PlayPodcastBroadcast";
    public final static String BROADCAST_TASK = "PlayOrPause";
    public final static String BROADCAST_TASK_SEEK_BAR_PROGRESS = "ProgressSeekBar";
    public final static String BROADCAST_TASK_SEEK_BAR_MAX = "MaxSeekBar";

    private MediaPlayer mediaPlayer;
    private boolean intialStage = true;
    private String urlPath;
    private int currentPosition = 0;
    private BroadcastReceiver br;

    @Override
    public IBinder onBind(Intent intent) {
        Boolean prepared;
        urlPath = intent.getStringExtra(URLKEY);

        try {
            Log.d(LOGD, "TRY");
            mediaPlayer.setDataSource(urlPath);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    intialStage = true;
//                    btn.setBackgroundResource(R.drawable.button_play);
//                    mediaPlayer.stop();
//                    mediaPlayer.reset();
                }
            });
            mediaPlayer.prepare();
            prepared = true;
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            Log.d("IllegarArgument", e.getMessage());
            prepared = false;
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            prepared = false;
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            prepared = false;
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            prepared = false;
            e.printStackTrace();
        }
        Log.d(LOGD, "onBind");
        Log.d(LOGD, "onBind url = " + urlPath);
        if (prepared) {
            final Intent actionIntent = new Intent(ItemActivity.BROADCAST_ACTION_ITEMACTIVITY);
            actionIntent.putExtra(ItemActivity.BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY, mediaPlayer.getDuration());
            sendBroadcast(actionIntent);
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                    actionIntent.putExtra(ItemActivity.BROADCAST_TASK_PLAYER_PROGRESS_ITEMACTIVITY, mediaPlayer.getCurrentPosition());
                    sendBroadcast(actionIntent);
                }
            });

            br = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    boolean status = intent.getBooleanExtra(BROADCAST_TASK, false);
                    int progress = intent.getIntExtra(BROADCAST_TASK_SEEK_BAR_PROGRESS, 0);
                    int max = intent.getIntExtra(BROADCAST_TASK_SEEK_BAR_MAX, 1);
                    Log.d(PlayPodcastService.LOGD, "current location = " + progress);
                    Log.d(PlayPodcastService.LOGD, "max duration = " + mediaPlayer.getDuration());
                    Log.d(PlayPodcastService.LOGD, "seek bar max = " + max);
                    if (status) {
                        double result = (double) mediaPlayer.getDuration() * ((double) progress / (double) max);
                        mediaPlayer.seekTo((int) result);
                        Log.d(PlayPodcastService.LOGD, "result = " + result);
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.pause();
                    }

                }
            };
            IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
            registerReceiver(br, intFilt);
            Log.d(LOGD, "onCreate");

        }
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        mediaPlayer.seekTo(currentPosition);
        Log.d(LOGD, "onStartCommand");
        return 1;
    }

    public void onStart(Intent intent, int startId) {
        Log.d(LOGD, "onStart");
        // TO DO
    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public void onStop() {
//        mediaPlayer.stop();
        mediaPlayer.start();
    }

    public void onPause() {
        mediaPlayer.pause();
    }

    @Override
    public void onDestroy() {
        Log.d(LOGD, "onDestroy");
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
    }

    @Override
    public void onLowMemory() {

    }
}
