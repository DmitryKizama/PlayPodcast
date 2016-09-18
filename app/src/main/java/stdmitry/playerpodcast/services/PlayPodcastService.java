package stdmitry.playerpodcast.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
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
                    intialStage = true;
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    actionIntent.putExtra(ItemActivity.CLOSE_ACTIVITY, true);
                    sendBroadcast(actionIntent);
                }
            });
            mediaPlayer.prepare();
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

            actionIntent.putExtra(ItemActivity.BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY, mediaPlayer.getDuration());
            sendBroadcast(actionIntent);
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                    actionIntent.putExtra(ItemActivity.BROADCAST_TASK_PLAYER_PROGRESS_ITEMACTIVITY, mediaPlayer.getCurrentPosition());
                    actionIntent.putExtra(ItemActivity.BROADCAST_TASK_MAX_DURATION_ITEMACTIVITY, mediaPlayer.getDuration());
                    sendBroadcast(actionIntent);
                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    actionIntent.putExtra(ItemActivity.PROGRESS_BAR_SHOW, true);
                    sendBroadcast(actionIntent);
                }
            });

            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {

                }
            });

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
                    mediaPlayer.seekTo((int) result);
                    Log.d(PlayPodcastService.LOGD, "result = " + result);
                    mediaPlayer.start();
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
        unregisterReceiver(br);
    }

    @Override
    public void onLowMemory() {
        Log.d("ALOHA", "onLowMemory");
    }

    private class BackgroundLooperThread extends HandlerThread {

        private static final int DO_JOB = 1;
        private static final int CANCEL_ANIM_JOB = 2;

        private Handler mHandler;

        public BackgroundLooperThread() {
            super("BackgroundLooperThread");
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();

            mHandler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {

                    switch (msg.what) {
                        case DO_JOB:
                            if (!isAnimForceStopped){
                                launchAnimation();
                            }

                            break;
                        case CANCEL_ANIM_JOB:
                            stopAnimPrivate();
                            break;
                    }
                }
            };
        }

        public void launchAnimAsync() {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(DO_JOB);
            }
        }

        public void cancelAnimAsync() {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(CANCEL_ANIM_JOB);
            }
        }
    }
}
