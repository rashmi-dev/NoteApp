package com.example.notetakingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by ras on 8/1/2017.
 */

public class NoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mDbReference;
    private Context context;
    private static final String onClickCreate="onClickCreateButton";
    private List<NoteItem> noteItems =new ArrayList<>();
    private long childrenCount;

    private static final String onClickListView = "onClickListView";

    public NoteWidgetFactory(Context con, Intent intent){
        this.context = con;
    }
    @Override
    public void onCreate() {
        Timber.tag("NoteWidgetFactory");
    }

    @Override
    public void onDataSetChanged() {
        Timber.i("onDataSetChanged");
        mFirebaseDb = FirebaseDatabase.getInstance();
        mDbReference = mFirebaseDb.getReference().child("notes");
        mDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noteItems.clear();
                childrenCount = dataSnapshot.getChildrenCount();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    noteItems.add(noteSnapshot.getValue(NoteItem.class));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onDestroy() {noteItems.clear();}

    @Override
    public int getCount() {
       Timber.i("getCount"+(int) childrenCount);
        return (int) childrenCount;}

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.note_item_list);
        if(noteItems.size()>0) {
            remoteViews.setTextViewText(R.id.widget_edit_note, noteItems.get(position).getNote());
        }
        else{
            remoteViews.setTextViewText(R.id.widget_edit_note,context.getString(R.string.loading_list));
        }
        Bundle bundle = new Bundle();
        Intent fillInIntent = new Intent();
        fillInIntent.setAction(onClickListView);
        fillInIntent.putExtras(bundle);
        remoteViews.setOnClickFillInIntent(R.id.widget_list_layout,fillInIntent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
