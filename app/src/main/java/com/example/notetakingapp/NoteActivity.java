package com.example.notetakingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class NoteActivity extends AppCompatActivity {
    @BindView(R.id.save_fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.notepad)
    EditText editText;
    private int listPosition;
    private String getNote;
    private HashMap<String, Boolean> noteExists = new HashMap<String, Boolean>();
    public static final int DEFAULT_NOTE_LENGTH_LIMIT =800;
    private FirebaseDatabase mFirebaseDatabase;// entry point for app to access the data in db
    private DatabaseReference mMessagesDatabaseReference;//class which references a specific part of the db
    private Boolean editPage;
    private Intent noteIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        Timber.tag("NoteActivity");
        mFirebaseDatabase = FirebaseDatabase.getInstance();//get instance of the firebase db class.(rootnote access)
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("notes");//used to get reference of a part of the db(specific node access)

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteIntent = getIntent();
        editPage=noteIntent.getBooleanExtra("edit", false);
        listPosition = noteIntent.getIntExtra("position", 0);
        getNote = noteIntent.getStringExtra("note");
        editText.setText(getNote);

        if (editPage) {
            listPosition = noteIntent.getIntExtra("position", 0);
            getNote = noteIntent.getStringExtra("note");
            editText.setText(getNote);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().length() > 0 || s.toString().length()>getNote.length()) {
                        fab.setEnabled(true);
                    } else {
                        fab.setEnabled(false);
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_NOTE_LENGTH_LIMIT)});
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, R.string.saving, Snackbar.LENGTH_LONG)
                            .setAction(R.string.save, null).show();
                    final Map<String, Object> hopperUpdates = new HashMap<String, Object>();
                    hopperUpdates.put("note", editText.getText().toString());
                    mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("notes");
                    Query editQuery = mMessagesDatabaseReference.orderByChild("note").equalTo(getNote);
                    editQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot editSnapshot : dataSnapshot.getChildren()) {
                                editSnapshot.getRef().updateChildren(hopperUpdates, null);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                    Intent intent =new Intent(NoteActivity.this,MainActivity.class);
                    intent.putExtra("position",listPosition);
                    startActivity(intent);
                }
            });
        }
        else if(!editPage) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().length() > 0) {
                        fab.setEnabled(true);
                    } else {
                        fab.setEnabled(false);
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_NOTE_LENGTH_LIMIT)});
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(editText.getText().length()>0 ) {
                  AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                  builder.setTitle(R.string.confirm);
                  builder.setMessage(R.string.are_you_sure);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                      saveData(editText.getText().toString());
                      //editText.setText("");
                      Toast.makeText(getApplicationContext(), ""+editText.getText().toString(), Toast.LENGTH_SHORT).show();
                      Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                      startActivity(intent);
                      dialog.dismiss();
                      }
                  });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          dialog.dismiss();
                      }
                  });

                  AlertDialog saveAlert = builder.create();
                  saveAlert.show();
                }
               else{
                    Toast.makeText(getApplicationContext(), R.string.note_is_empty, Toast.LENGTH_SHORT).show();
                }
                }
            });
        }
    }

    @Override
    public void onBackPressed(){
           backPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    public void backPressed(){
        if(!editPage){
            if(editText.getText().length()>0 ) {
                if(noteExists==null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                    builder.setTitle(R.string.confirm);
                    builder.setMessage(R.string.save_data_dialog);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveData(editText.getText().toString());
                            Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                    AlertDialog saveAlert = builder.create();
                    saveAlert.show();
                }

                else if(noteExists!=null) {
                    if ((noteExists.containsKey(editText.getText().toString()))) {
                        Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                        builder.setTitle(R.string.confirm);
                        builder.setMessage(R.string.save_data_dialog);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveData(editText.getText().toString());
                                Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                        AlertDialog saveAlert = builder.create();
                        saveAlert.show();
                    }
                }
                else{
                    Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
            else if(editText.getText().length()<=0){
                Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
        else if(editPage){
            if(editText.getText().toString().equals(getNote)) {
                Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                startActivity(intent);
            }
            else if(editText.getText().length()>0){
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setTitle(R.string.confirm);
                builder.setMessage(R.string.save_data_dialog);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Map<String, Object> back_pressed = new HashMap<String, Object>();
                        back_pressed.put("note", editText.getText().toString());
                        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("notes");
                        Query editQuery = mMessagesDatabaseReference.orderByChild("note").equalTo(getNote);
                        editQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot editSnapshot : dataSnapshot.getChildren()) {
                                    editSnapshot.getRef().updateChildren(back_pressed, null);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        Intent intent =new Intent(NoteActivity.this,MainActivity.class);
                        intent.putExtra("position",listPosition);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                AlertDialog saveAlert = builder.create();
                saveAlert.show();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        if(id==android.R.id.home){
            backPressed();
            return true;
        }
        if (id == R.id.share_icon) {
            share_note();
            return true;
        }
        if (id == R.id.delete_icon) {
            delete_note();
            return true;
        }
        if (id == R.id.alarm_icon) {
            set_alarm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void set_alarm(){
        if(editText.getText().length()>0) {
            if (!noteIntent.getBooleanExtra("edit", false)) {
                String to_save_text = editText.getText().toString();
                if((noteExists == null) || noteExists.isEmpty() || !noteExists.get(to_save_text)) {
                    saveData(editText.getText().toString());
                }
                DialogFragment newFragment = new DatePickerFragment();
                Bundle args = new Bundle();
                args.putString("note", to_save_text);
                newFragment.setArguments(args);
                newFragment.show(getSupportFragmentManager(), getString(R.string.datepicker));
            } else if (noteIntent.getBooleanExtra("edit", false)) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle args = new Bundle();
                args.putInt("position", listPosition);
                args.putString("note", getNote);
                newFragment.setArguments(args);
                newFragment.show(getSupportFragmentManager(), getString(R.string.datepicker));
            }
        }
      else{
            Toast.makeText(this,R.string.note_is_empty, Toast.LENGTH_SHORT).show();
        }
    }
    public void share_note(){
        if(editText.getText().length()>0) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());
            shareIntent.setType("text/plain");
            startActivity(shareIntent);
        }
        else{
            Toast.makeText(this,R.string.note_is_empty, Toast.LENGTH_SHORT).show();
        }
    }

    public void delete_note(){
        if(noteIntent.getBooleanExtra("edit", false)) {
            Toast.makeText(getApplicationContext(),"Inside Deleting note2", Toast.LENGTH_SHORT).show();
            mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("notes");
            Query applesQuery = mMessagesDatabaseReference.orderByChild("note").equalTo(getNote);
            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                        appleSnapshot.getRef().removeValue();
                        editText.clearComposingText();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        Toast.makeText(getApplicationContext(),"Deleting note1", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else{
            editText.clearComposingText();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            Toast.makeText(getApplicationContext(),"Deleting note2", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
    }
    public void saveData(String save){
            NoteItem note = new NoteItem(save);
            mMessagesDatabaseReference.push().setValue(note);
            noteExists.put(save,true);
            Timber.i("NoteActivity", noteExists.get("notes"));
            Toast.makeText(getApplicationContext(), R.string.note_saved, Toast.LENGTH_SHORT).show();
    }
}
