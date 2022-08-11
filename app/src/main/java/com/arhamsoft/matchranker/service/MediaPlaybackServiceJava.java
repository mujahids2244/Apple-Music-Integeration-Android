package com.arhamsoft.matchranker.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.apple.android.music.playback.controller.MediaPlayerController;
import com.apple.android.music.playback.controller.MediaPlayerControllerFactory;
import com.apple.android.music.playback.model.MediaPlayerException;
import com.apple.android.music.playback.model.PlaybackRepeatMode;
import com.apple.android.music.playback.model.PlaybackShuffleMode;
import com.apple.android.music.playback.model.PlaybackState;
import com.apple.android.music.playback.model.PlayerQueueItem;

import java.util.List;


public final class MediaPlaybackServiceJava extends MediaBrowserServiceCompat implements MediaPlayerController.Listener, Handler.Callback {

    private static final String TAG = "MediaPlaybackService";
    private static final int MESSAGE_START_COMMAND = 1;
    private static final int MESSAGE_TASK_REMOVED = 2;

    private HandlerThread serviceHandlerThread;
    private Handler serviceHandler;

    private MediaSessionCompat mediaSession;
    private PlaybackNotificationManagerJava playbackNotificationManager;
    //private LocalMediaProvider mediaProvider;
    public MediaPlayerController playerController;

    static {

        try {
            // Adding these two lines will prevent the OOM false alarm
            System.setProperty("org.bytedeco.javacpp.maxphysicalbytes", "0");
            System.setProperty("org.bytedeco.javacpp.maxbytes", "0");

            System.loadLibrary("c++_shared");
            System.loadLibrary("appleMusicSDK");
        } catch (final Exception e) {
            Log.e(TAG, "Could not load library due to: " + Log.getStackTraceString(e));
            throw e;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "service", Toast.LENGTH_LONG).show();
        serviceHandlerThread = new HandlerThread("MediaPlaybackService:Handler",
                Process.THREAD_PRIORITY_BACKGROUND);
        serviceHandlerThread.start();
        serviceHandler = new Handler(serviceHandlerThread.getLooper(), this);

        playerController = MediaPlayerControllerFactory.createLocalController(this,
                serviceHandler, new AppleMusicTokenProvider(this));
        playerController.addListener(this);

        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        mediaSession.setCallback(new MediaSessionManagerJava(this,
                serviceHandler, playerController, mediaSession), serviceHandler);
        setSessionToken(mediaSession.getSessionToken());
        playbackNotificationManager = new PlaybackNotificationManagerJava(this, serviceHandler);

        //mediaProvider = new LocalMediaProvider(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        playbackNotificationManager.stop(true);
        mediaSession.release();
        playerController.release();
        serviceHandlerThread.quit();
        Toast.makeText(this, "service destroyed", Toast.LENGTH_LONG).show();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceHandler.obtainMessage(MESSAGE_START_COMMAND, intent).sendToTarget();
        return START_STICKY;
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // TODO: This needs to make sure the client is allowed to browse
        return new BrowserRoot("MEDIA_ROOT", null);
    }


    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        //mediaProvider.loadMediaItems(parentId, result);
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_START_COMMAND:
                final Intent messageIntent = (Intent) msg.obj;
                final KeyEvent mediaButtonEvent = MediaButtonReceiver.handleIntent(mediaSession, messageIntent);
                if (mediaButtonEvent == null) {
                    handleIntent(messageIntent);
                }
                return true;
            case MESSAGE_TASK_REMOVED:
                stopSelf();
                return true;
        }
        return false;
    }


    @Override
    public void onPlayerStateRestored(@NonNull MediaPlayerController playerController) {

    }

    @Override
    public void onPlaybackStateChanged(@NonNull MediaPlayerController playerController, int previousState, int currentState) {
        switch (currentState) {
            case PlaybackState.PLAYING:
                playbackNotificationManager.start();
                break;
            case PlaybackState.PAUSED:
                playbackNotificationManager.stop(false);
                break;
            case PlaybackState.STOPPED:
                playbackNotificationManager.stop(true);
                break;
        }
    }

    @Override
    public void onPlaybackStateUpdated(@NonNull MediaPlayerController playerController) {

    }


    @Override
    public void onBufferingStateChanged(@NonNull MediaPlayerController playerController, boolean buffering) {
    }


    @Override
    public void onCurrentItemChanged(@NonNull MediaPlayerController playerController, @Nullable PlayerQueueItem previousItem, @Nullable PlayerQueueItem currentItem) {
    }

    @Override
    public void onItemEnded(@NonNull MediaPlayerController playerController, @NonNull PlayerQueueItem queueItem, long endPosition) {
    }

    @Override
    public void onMetadataUpdated(@NonNull MediaPlayerController playerController, @NonNull PlayerQueueItem currentItem) {
    }

    @Override
    public void onPlaybackQueueChanged(@NonNull MediaPlayerController playerController, @NonNull List<PlayerQueueItem> playbackQueueItems) {
    }

    @Override
    public void onPlaybackQueueItemsAdded(@NonNull MediaPlayerController playerController, int queueInsertionType, int containerType, int itemType) {

    }

    @Override
    public void onPlaybackError(@NonNull MediaPlayerController playerController, @NonNull MediaPlayerException error) {

    }

    @Override
    public void onPlaybackRepeatModeChanged(@NonNull MediaPlayerController playerController, @PlaybackRepeatMode int currentRepeatMode) {
    }

    @Override
    public void onPlaybackShuffleModeChanged(@NonNull MediaPlayerController playerController, @PlaybackShuffleMode int currentShuffleMode) {
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        serviceHandler.sendEmptyMessage(MESSAGE_TASK_REMOVED);
    }


    private void handleIntent(Intent intent) {
    }
}

