package com.csis.social.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.csis.social.app.adapters.SubjectsFollowAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SubjectsFollowActivity extends AppCompatActivity {

    TextView noSubjects;
    RecyclerView recyclerView;

    SubjectsFollowAdapter subjectsFollowAdapter;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, followReference;
    ValueEventListener followValueEventListener;
    ArrayList<String> allSubjects_list;
    private String userType;
    private String startedFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects_follow);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //reading from shared preference
        userType = sharedPreferences.getString("userType", "not found");

        startedFrom = getIntent().getExtras().getString("startedFrom");

        noSubjects = findViewById(R.id.noSubjects);
        recyclerView = findViewById(R.id.recyclerView);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Courses");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allSubjects_list = Utilities.getAllSubjectsAllLevelsAndSemesters(dataSnapshot);

                if (userType.equals("Student")) {
                    followReference = firebaseDatabase.getReference().child("Users").child(Utilities.getCurrentUID())
                            .child("follow");
                } else if (userType.equals("Admin")) {
                    followReference = firebaseDatabase.getReference().child("Admins").child(Utilities.getCurrentUID())
                            .child("follow");
                }
                followValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> followedSubjects_list = Utilities.getFollowedSubjects(dataSnapshot);

                        fillRecyclerView(allSubjects_list, followedSubjects_list);
                        followReference.removeEventListener(followValueEventListener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                followReference.addValueEventListener(followValueEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fillRecyclerView(ArrayList<String> allSubjects_list, ArrayList<String> followedSubjects_list) {
        if (allSubjects_list.size() == 0)
            noSubjects.setVisibility(View.VISIBLE);
        else {
            if (noSubjects.getVisibility() == View.VISIBLE)
                noSubjects.setVisibility(View.GONE);

            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(layoutManager);

            subjectsFollowAdapter = new SubjectsFollowAdapter(this, allSubjects_list, followedSubjects_list);
            recyclerView.setAdapter(subjectsFollowAdapter);
            recyclerView.setHasFixedSize(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subjects_follow, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.finish_btn) {
            if (isNetworkAvailable()) {
                ArrayList<String> followedSubjects_list = subjectsFollowAdapter.followedSubjects_list;

                    DatabaseReference followReference = null;

                    if (userType.equals("Student")) {
                        followReference = firebaseDatabase.getReference().child("Users").child(Utilities.getCurrentUID())
                                .child("follow");
                    } else if (userType.equals("Admin")) {
                        followReference = firebaseDatabase.getReference().child("Admins").child(Utilities.getCurrentUID())
                                .child("follow");
                    }
                    followReference.setValue(null);

                if (followedSubjects_list.size() > 0) {

                    for (int i = 0; i < followedSubjects_list.size(); i++) {
                        Map<String, String> map = new HashMap<>();
                        map.put("subject", followedSubjects_list.get(i));

                        String pushID = followReference.push().getKey();
                        followReference.child(pushID).setValue(map);

                        Toast.makeText(SubjectsFollowActivity.this, "Changes saved successfully", Toast.LENGTH_LONG).show();
                        if (startedFrom != null && startedFrom.equals("Registration"))
                            startActivity(new Intent(SubjectsFollowActivity.this, DashboardActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(SubjectsFollowActivity.this, "You aren't follow any subject", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else
                Toast.makeText(this, "Check your network connectivity", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        if (startedFrom != null && startedFrom.equals("Registration")) {
            Toast.makeText(this, "Subjects' follow not saved", Toast.LENGTH_SHORT).show();
            finish();
            moveTaskToBack(true); //Exit app
        } else
            super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this);
    }
}