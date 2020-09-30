package com.example.notetakingapp;

import android.content.Intent;
import android.widget.RemoteViewsService;

import timber.log.Timber;

/**
 * Created by ras on 8/3/2017.
 */

public class NoteWidgetIntentService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Timber.i("onGetViewFactory");
        return new NoteWidgetFactory(this,intent);
    }
}
