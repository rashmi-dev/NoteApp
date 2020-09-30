package com.example.notetakingapp;
import android.app.Application;

import timber.log.BuildConfig;
import timber.log.Timber;


/**
 * Created by ras on 7/30/2017.
 */

public class NoteApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.i("NoteApp");
        }
    }
}

