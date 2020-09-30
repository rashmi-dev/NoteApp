package com.example.notetakingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by ras on 7/24/2017.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>{
    private List<NoteItem> list;
    private Context context;
    private FragmentManager fragmentManager;
    @BindView(R.id.delete_button)ImageButton delete_button;

    private FirebaseDatabase mFirebaseDatabase;// entry point for app to access the data in db
    private DatabaseReference mMessagesDatabaseReference;//class which references a specific part of the db
    public NoteAdapter(List<NoteItem> list, Context context,FragmentManager fm){
        this.list = list;
        this.context = context;
        this.fragmentManager = fm;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_main,parent,false);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final NoteItem noteItem = list.get(position);
        holder.textView.setText(noteItem.getNote());
        final int adapterPosition = holder.getAdapterPosition();

        holder.list_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,NoteActivity.class);
                intent.putExtra("position",adapterPosition);
                intent.putExtra("note",noteItem.getNote());
                intent.putExtra("edit",true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("notes");
                Query applesQuery = mMessagesDatabaseReference.orderByChild("note").equalTo(noteItem.getNote());
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                            list.remove(appleSnapshot.getRef().removeValue());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });
        holder.edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,NoteActivity.class);
                intent.putExtra("position",adapterPosition);
                intent.putExtra("note",noteItem.getNote());
                intent.putExtra("edit",true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, noteItem.getNote());
                shareIntent.setType("text/plain");
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(shareIntent);
            }
        });
        holder.alarm_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle args = new Bundle();
                args.putInt("position",adapterPosition);
                args.putString("note",noteItem.getNote());
                newFragment.setArguments(args);
                newFragment.show(fragmentManager, "DatePicker");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filterList(ArrayList<NoteItem> filterdNames) {
        this.list = filterdNames;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.edit_note)
        TextView textView;
        @BindView(R.id.delete_button)
        ImageButton delete_button;
        @BindView(R.id.edit_button)
        ImageButton edit_button;
        @BindView(R.id.share_button)
        ImageButton share_button;
        @BindView(R.id.alarm_button)
        ImageButton alarm_button;
        @BindView(R.id.relative_layout)
        RelativeLayout list_layout;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            textView = (TextView) itemView.findViewById(R.id.edit_note);
        }
    }
}


