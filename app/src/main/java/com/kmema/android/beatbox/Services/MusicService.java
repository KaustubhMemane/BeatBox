package com.kmema.android.beatbox.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.kmema.android.beatbox.Database.SongDataModel;
import com.kmema.android.beatbox.MainActivity;
import com.kmema.android.beatbox.R;

import java.util.ArrayList;

/**
 * Created by kmema on 8/25/2017.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {


    private static final String CHANNEL_ID = "BeatBox";
    private MediaPlayer mPlayer;
    private Uri mSongUri;
    private ArrayList<SongDataModel> mListOfSongs;

    private int SONG_POS = 0;
    private final IBinder musicBind = new PlayerBinder();

    private final String ACTION_STOP = "com.kmema.android.beatbox.STOP";
    private final String ACTION_NEXT = "com.kmema.android.beatbox.NEXT";
    private final String ACTION_PREVIOUS = "com.kmema.android.beatbox.PREVIOUS";
    private final String ACTION_PAUSE = "com.kmema.android.beatbox.PAUSE";

    private static final int STATE_PAUSED = 1;
    private static final int STATE_PLAYING = 2;
    private int mState = 0;
    private static final int REQUEST_CODE_PAUSE = 101;
    private static final int REQUEST_CODE_PREVIOUS = 102;
    private static final int REQUEST_CODE_NEXT = 103;
    private static final int REQUEST_CODE_STOP = 104;
    public static int NOTIFICATION_ID = 11;


    private static final String CHANNEL_NAME = "BeatBox_Channel";
    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel = null;
    private NotificationCompat.Builder notificationCompatBuilder;
    private Notification mNotification;
    private NotificationCompat.Builder mNotificationBuilder;
    private TelephonyManager mgr;
    private PhoneStateListener phoneStateListener;
    private AudioManager mAudioManager;
    Bitmap bitmapImage = null;

    @Override
    public void onCreate() {
        super.onCreate();


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        // TODO: Done // creating a channel before creating a notification, needs channel to show notification in Android-O+ OS
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannel();
        }

        //initializing media player, to play the music using MediaPlayer Api/class we need it to play the music
        initMediaPlayer();

        notificationCompatBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        initNoisyReceiver();
    }


    public void initMediaPlayer()
    {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        }
    }


    public void updateNotification(String updatedSongName) {
        mNotification.contentView.setTextViewText(R.id.notify_song_name, updatedSongName);
        notificationManager.notify(NOTIFICATION_ID, mNotification);
    }


    /*private void checkAudioFocus() {

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    mPlayer.pause();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    mPlayer.start();
                    //Not in call: Play music
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    mPlayer.start();
                    //A call is dialing, active or on hold
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

    }*/

    public class PlayerBinder extends Binder {
        public MusicService getService() {
            Log.d("test", "getService()");
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("test", "onBind Called");
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        //stopSong();
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        //mAudioManager.abandonAudioFocus(this);

        notificationManager.cancel(NOTIFICATION_ID);
       /* mPlayer.pause();
        mPlayer.stop();
        mPlayer.release();
        System.exit(0);
        notificationManager.cancel(NOTIFICATION_ID);*/

        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String Action = intent.getAction();
            if (!TextUtils.isEmpty(Action)) {
                switch (Action) {
                    case ACTION_PAUSE:
                        playPauseSong();
                        break;
                    case ACTION_NEXT:
                        nextSong();
                        break;
                    case ACTION_PREVIOUS:
                        previousSong();
                        break;
                    case ACTION_STOP:
                        stopSong();
                        stopSelf();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void stopSong() {

        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        if (mPlayer != null) {
            if(mPlayer.isPlaying())
            {
                mPlayer.stop();
                mPlayer.release();
            } else if(mPlayer != null){
            mPlayer.release();
            }
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        System.exit(0);
    }

    private void previousSong() {
        try {
            if (SONG_POS == 0) {
                Toast.makeText(this, "No Previous Song", Toast.LENGTH_SHORT).show();
                return;
            }
            SONG_POS--;
            startSong(Uri.parse(mListOfSongs.get(SONG_POS).getSongUri()), mListOfSongs.get(SONG_POS).getSongName(), SONG_POS);
        } catch (Exception e) {

        }
    }

    private void nextSong() {

        if (SONG_POS > mListOfSongs.size()) {
            SONG_POS = 0;
        }
        try {
            SONG_POS++;
            startSong(Uri.parse(mListOfSongs.get(SONG_POS).getSongUri()), mListOfSongs.get(SONG_POS).getSongName(), SONG_POS
            );
        } catch (Exception e) {
            Toast.makeText(this, "No Next Song", Toast.LENGTH_SHORT).show();
            startSong(Uri.parse(mListOfSongs.get(SONG_POS).getSongUri()), mListOfSongs.get(SONG_POS).getSongName(), SONG_POS);
        }
    }




    private void startSong(Uri parseSongUri, String songName, int SONG_POS) {

//        checkAudioFocus();

        if(!successfullyRetrievedAudioFocus())
        {
            return;
        }
        mPlayer.reset();
        mState = STATE_PLAYING;
        mSongUri = parseSongUri;

        try {
            mPlayer.setDataSource(getApplicationContext(), mSongUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "ERROR SETTING DATA SOURCE", e);
        }
        mPlayer.prepareAsync();

        updateNotification(songName);
    }


 /*   private void updateNotification(String songName) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification.contentView.setTextViewText(R.id.notify_song_name, songName);
        notificationManager.notify(NOTIFICATION_ID,mNotification);
    }
*/

    private void playPauseSong() {


        if(mPlayer==null)
        {
            initMediaPlayer();
        }
        if (mState == STATE_PAUSED) {
                mState = STATE_PLAYING;
                mPlayer.start();

        } else {
            mState = STATE_PAUSED;
            mPlayer.pause();
        }
    }

    //starting media player when media player is ready
    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayer.reset();

        try {
            if (SONG_POS < mListOfSongs.size() - 1) {
                nextSong();
            } else {
                SONG_POS = 0;
                mPlayer.setDataSource(getApplicationContext(), Uri.parse(mListOfSongs.get(SONG_POS).getSongUri()));
            }
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if (mPlayer != null) {
                    mPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if (mPlayer != null) {
                    if (!mPlayer.isPlaying()) {
                        mPlayer.start();
                    }
                    mPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }


    private void setSongURI(Uri mSongUri) {
        this.mSongUri = mSongUri;
    }

    private void setBitmapImage(String albumArt) {
//        Drawable img = Drawable.createFromPath(albumArt);
        if(albumArt != "" && albumArt != null)
        {
            bitmapImage = BitmapFactory.decodeFile(albumArt);
        }
        else
        {
            bitmapImage = BitmapFactory.decodeResource(getResources(),R.drawable.beat_box_logo);
        }

    }

    public void setSelectedSong(int pos, int notification_id, Context context) {
        SONG_POS = pos;

        NOTIFICATION_ID = notification_id;

        setSongURI(Uri.parse(mListOfSongs.get(SONG_POS).getSongUri()));

        setBitmapImage(mListOfSongs.get(SONG_POS).getAlbumArt());

        ShowNotification();

        startSong(Uri.parse(mListOfSongs.get(SONG_POS).getSongUri()), mListOfSongs.get(SONG_POS).getSongName(), SONG_POS);
    }


    public void setListofSongs(ArrayList<SongDataModel> listofSongs) {
        mListOfSongs = listofSongs;
    }


    private void ShowNotification() {
        PendingIntent pendingIntent,pendingIntentStop;
        Intent intentStop, intentPause, intentPrevious, intentNext;

        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_mediacontroller);

        notificationView.setTextViewText(R.id.notify_song_name, mListOfSongs.get(SONG_POS).getSongName());

        intentStop = new Intent(ACTION_STOP);
        pendingIntentStop = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_STOP, intentStop, PendingIntent.FLAG_CANCEL_CURRENT);
//        notificationView.setOnClickPendingIntent(R.id.notify_btn_stop, pendingIntentStop);

        intentPause = new Intent(ACTION_PAUSE);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PAUSE, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_pause, pendingIntent);

        intentPrevious = new Intent(ACTION_PREVIOUS);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PREVIOUS, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_previous, pendingIntent);

        intentNext = new Intent(ACTION_NEXT);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_NEXT, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_next, pendingIntent);

        //this intent and pending intent using to open the app when user click on Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        mNotification = notificationCompatBuilder
                .setSmallIcon(R.drawable.play_logo)
                .setLargeIcon(bitmapImage)
                .setCustomContentView(notificationView)
                .setOngoing(true)
                .setColorized(true)
                .setColor(Color.RED)
                .setDeleteIntent(pendingIntentStop)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(notificationPendingIntent)
                .setContent(notificationView)
                .setDefaults(Notification.DEFAULT_ALL)
                .setColorized(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, mNotification);
    }


    private void initChannel() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("BeatBox Notification ");
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.pause();
            }
        }
    };

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mNoisyReceiver);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        notificationManager.cancel(NOTIFICATION_ID);
        if(mPlayer != null)
        {
            mPlayer.stop();
            mPlayer.release();
        }
    }
    protected PendingIntent getDeleteIntent()
    {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction("notification_cancelled");
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
