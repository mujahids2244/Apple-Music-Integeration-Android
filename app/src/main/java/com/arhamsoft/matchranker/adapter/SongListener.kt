package com.arhamsoft.matchranker.adapter

import android.support.v4.media.MediaBrowserCompat
import android.view.View

interface SongListener {


    fun onMediaItemClicked(mediaItem: MediaBrowserCompat.MediaItem)

}