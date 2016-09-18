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
    private String urlPath;
    private int currentPosition = 0;
    private BroadcastReceiver br;
    private Intent actionIntent;

    @Override
    public IBinder onBind(final Intent intent) {
        Boolean prepared;
        urlPath = intent.getStringExtra(URLKEY);
        actionIntent = new Intent(ItemActivity.BROADCAST_ACTION_ITEMACTIVITY);
        Log.d(LOGD, "utl = " + urlPath);
        try {
            Log.d(LOGD, "TRY");
            Log.d(LOGD, "utl = " + urlPath);
            mediaPlayer.setDataSource(urlPath);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                    actionIntent.putExtra(ItemActivity.CLOSE_ACTIVITY, true);
                    sendBroadcast(actionIntent);
                }
            });
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mediaPlayer) {
                    actionIntent.putExtra(ItemActivity.BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY, mediaPlayer.getDuration());
                    actionIntent.putExtra(ItemActivity.PROGRESS_BAR_SHOW, true);
                    Log.d("ALLOHA", "------------------CALED-------------");
                    sendBroadcast(actionIntent);
                    mediaPlayer.start();
                    mediaPlayer.pause();
                    br = new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            boolean playOrpause = intent.getBooleanExtra(BROADCAST_TASK, false);
                            if (!playOrpause) {
                                mediaPlayer.pause();
                                return;
                            }
                            int progress = intent.getIntExtra(BROADCAST_TASK_SEEK_BAR_PROGRESS, 0);
                            int max = intent.getIntExtra(BROADCAST_TASK_SEEK_BAR_MAX, 1);
                            double result = (double) mediaPlayer.getDuration() * ((double) progress / (double) max);
                            mediaPlayer.pause();
                            mediaPlayer.seekTo((int) result);
                            Log.d(PlayPodcastService.LOGD, "result = " + result);
                            mediaPlayer.start();
                        }
                    };
                    IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
                    registerReceiver(br, intFilt);
                }
            });
            prepared = true;
        } catch (IllegalArgumentException e) {
            Log.d("IllegarArgument", e.getMessage());
            prepared = false;
            e.printStackTrace();
        } catch (SecurityException e) {
            prepared = false;
            e.printStackTrace();
        } catch (IllegalStateException e) {
            prepared = false;
            e.printStackTrace();
        } catch (IOException e) {
            prepared = false;
            e.printStackTrace();
        }
        if (prepared) {

            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                    actionIntent.putExtra(ItemActivity.BROADCAST_TASK_PLAYER_PROGRESS_ITEMACTIVITY, mediaPlayer.getCurrentPosition());
//                    actionIntent.putExtra(ItemActivity.BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY, mediaPlayer.getDuration());
                    sendBroadcast(actionIntent);
                }
            });


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
        if (br != null) {
            unregisterReceiver(br);
            br = null;
        }
    }

    @Override
    public void onLowMemory() {
        Log.d("ALOHA", "onLowMemory");
    }


}
