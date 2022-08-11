package com.arhamsoft.matchranker.service;

import android.content.Context;

import com.apple.android.sdk.authentication.TokenProvider;
import com.arhamsoft.matchranker.R;
import com.arhamsoft.matchranker.room.UserDatabase;
import com.arhamsoft.matchranker.util.AppPreferences;

/**
 * Copyright (C) 2018 Apple, Inc. All rights reserved.
 */
public class AppleMusicTokenProvider implements TokenProvider {

    private final Context context;
    private final AppPreferences appPreferences;



    public AppleMusicTokenProvider(Context context) {
        this.context = context.getApplicationContext();
        appPreferences = AppPreferences.Companion.getInstance(context).with(this.context);

    }

    @Override
    public String getDeveloperToken() {
        return context.getString(R.string.developer_token);
    }

    @Override
    public String getUserToken() {

        return appPreferences.getAppleMusicUserToken();

    }

}
