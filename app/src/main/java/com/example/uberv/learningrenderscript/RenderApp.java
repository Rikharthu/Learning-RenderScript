package com.example.uberv.learningrenderscript;

import android.app.Application;

import timber.log.Timber;

public class RenderApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
