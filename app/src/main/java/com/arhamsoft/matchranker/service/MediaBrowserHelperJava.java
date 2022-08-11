package com.arhamsoft.matchranker.service;

import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Copyright (C) 2017 Apple, Inc. All rights reserved.
 */

public final class MediaBrowserHelperJava extends MediaBrowserCompat.ConnectionCallback {

    public interface Listener {

        void onMediaBrowserConnected(@NonNull MediaBrowserCompat mediaBrowser);

    }

    private final Listener listener;
    private final MediaBrowserCompat mediaBrowser;


    public MediaBrowserHelperJava(@NonNull Context context, @NonNull Listener listener) {
        this.listener = listener;
        mediaBrowser = new MediaBrowserCompat(context, new ComponentName(context, MediaPlaybackServiceJava.class), this, null);
    }


    public void connect() {
        mediaBrowser.connect();
    }


    public void disconnect() {
        mediaBrowser.disconnect();
    }


    @Override
    public void onConnected() {
        listener.onMediaBrowserConnected(mediaBrowser);
    }


    @Override
    public void onConnectionSuspended() {
        Log.d("Error", "");

    }


    @Override
    public void onConnectionFailed() {
        Log.d("Error", "");

    }

}
