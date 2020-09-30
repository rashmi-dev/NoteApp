package com.example.notetakingapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static com.example.notetakingapp.R.id.search_icon;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @Nullable
    @BindView(R.id.alarm_button)
    ImageButton alarm_button;
    @Nullable
    @BindView(R.id.delete_button)
    ImageButton delete_button;
    @Nullable
    @BindView(R.id.share_button)
    ImageButton share_button;
    public static final String ACTION_UPDATED = "com.example.android.portfolio.noteapp.ACTION_UPDATED";
    private NoteAdapter noteAdapter;
    private AdView mAdView;
    private FirebaseDatabase mFirebaseDatabase;// entry point for app to access the data in db
    private DatabaseReference mMessagesDatabaseReference;//class which references a specific part of the db
    private ChildEventListener mChildEventListener;//to know if the data changes
    private List<NoteItem> noteItemList = new ArrayList<>();
    NoteItem noteItem = new NoteItem();
    //private FirebaseAuth mAuth;
    public static final int RC_SIGN_IN = 1;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private Intent signInIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        //Initiating Timber
        Timber.tag("MainActivity");
        Timber.i("OnCreate");

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null){
                    Timber.i("User is signed in");
                    onSignedInInitialize();
                }
                else {
                    Timber.i("user is signed out");
                    onSignedOutCleanUp();
                }
            }
        };

        FirebaseApp.initializeApp(MainActivity.this);
        mAuth = FirebaseAuth.getInstance();

        //Initiating Firebase Database
        mFirebaseDatabase = FirebaseDatabase.getInstance();//get instance of the firebase db class.(rootnote access)
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("notes");//used to get reference of a part of the db(specific node access)

        //Adding AdMob into the project
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        Timber.i("note items" + noteItem.getNote());
        noteAdapter = new NoteAdapter(noteItemList, getApplicationContext(), getSupportFragmentManager());
        recyclerView.setAdapter(noteAdapter);

        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.create_new_note, Snackbar.LENGTH_LONG)
                        .setAction(R.string.create, null).show();
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager manager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(search_icon).getActionView();

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                return false;
            }
        });
        searchView.setSearchableInfo(manager.getSearchableInfo(this.getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                NoteItem noteItem = new NoteItem(newText);
                filter(noteItem);
                return false;
            }
        });
        return true;
    }

    private void filter(NoteItem results) {
        //new array list that will hold the filtered data
        ArrayList<NoteItem> filterdSearch = new ArrayList<>();
        //looping through existing elements
        for (NoteItem s : noteItemList) {
            //if the existing elements contains the search input
            if (s.getNote().toLowerCase().contains(results.getNote().toLowerCase())) {
                //adding the element to filtered list
                filterdSearch.add(s);
            }
        }
        //calling a method of the adapter class and passing the filtered list
        noteAdapter.filterList(filterdSearch);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out:
                AuthUI.getInstance().signOut(this);
                Intent intent = new Intent (this,SignInActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode ==RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.sign_in, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.signed_out, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void onSignedInInitialize(){
        Timber.i("onSignedInInitialize");
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener(){
        Timber.i("attachDatabaseReadListener");
        Intent getIntent = getIntent();
        final int position = getIntent.getIntExtra("position", 0);
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //when new notes are added
                    //Datasnapshot contains the message that has already been added at this point in time.
                    NoteItem noteItemNew = dataSnapshot.getValue(NoteItem.class);
                    Timber.i("onChildAdded",noteItemNew.getNote());
                    noteItemList.add(noteItemNew);
                    noteAdapter = new NoteAdapter(noteItemList, getApplicationContext(), getSupportFragmentManager());
                    recyclerView.setAdapter(noteAdapter);
                    Intent intent = new Intent();
                    intent.setAction(ACTION_UPDATED);
                    sendBroadcast(intent);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    //contents of existing child is changed
                    Timber.i("onChildChanged");
                    NoteItem noteItemNew = dataSnapshot.getValue(NoteItem.class);
                    noteAdapter.notifyItemChanged(position);
                    noteAdapter.notifyItemChanged(position, noteItemNew.getNote());
                    recyclerView.setAdapter(noteAdapter);
                    Intent intent = new Intent();
                    intent.setAction(ACTION_UPDATED);
                    sendBroadcast(intent);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.i("onChildRemoved");
                    NoteItem noteItemNew = dataSnapshot.getValue(NoteItem.class);
                    for (int i = 0; i < noteItemList.size(); i++) {
                        if (noteItemList.get(i).getNote() == noteItemNew.getNote()) {
                            noteItemList.remove(i);
                        }
                    }
                    noteAdapter = new NoteAdapter(noteItemList, getApplicationContext(), getSupportFragmentManager());
                    recyclerView.setAdapter(noteAdapter);
                    Intent intent = new Intent();
                    intent.setAction(ACTION_UPDATED);
                    sendBroadcast(intent);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void onSignedOutCleanUp(){
        Timber.i("onSignedOutCleanUp");
        noteItemList.clear();
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener(){
        if(mChildEventListener!=null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener =null;
        }
    }
}