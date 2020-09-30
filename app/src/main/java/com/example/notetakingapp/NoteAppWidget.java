package com.example.notetakingapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.widget.RemoteViews;

import androidx.annotation.NonNull;

/**
 * Implementation of App Widget functionality.
 */
public class NoteAppWidget extends AppWidgetProvider {

    private static final String onClickCreate = "onClickCreate";
    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_app_widget);
        views.setTextViewText(R.id.widget_edit_note, widgetText);
        views.setOnClickPendingIntent(R.id.widget_create_note,getPendingIntent(context,onClickCreate));
        setRemoteAdapter(context,views);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        super.onReceive(context,intent);
        if (onClickCreate.equals(intent.getAction())) {
            Intent intent1 = new Intent(context,NoteActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,getClass()));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_list_view);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int i =0;i<appWidgetIds.length;i++){
            Intent list_click_intent = new Intent(context,MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,
                    list_click_intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent intent = new Intent(context,NoteWidgetIntentService.class);
            RemoteViews rv = new RemoteViews(context.getPackageName(),R.layout.note_app_widget);
            rv.setRemoteAdapter(appWidgetIds[i],R.id.widget_list_view,intent);
            rv.setOnClickPendingIntent(R.id.widget_create_note,getPendingIntent(context,onClickCreate));
            rv.setPendingIntentTemplate(R.id.widget_list_view,pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i],rv);
        }
        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }
    protected PendingIntent getPendingIntent(Context context, String action){
        Intent intent = new Intent(context,getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context,0,intent,0);
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views){
        views.setRemoteAdapter(R.id.widget_list_view,new Intent(context,NoteWidgetIntentService.class));
    }
}

