package com.csis.social.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.csis.social.app.R;
import com.csis.social.app.adapters.AdapterUsers;
import com.csis.social.app.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostLikedByActivity extends AppCompatActivity {

    String postId;

    private RecyclerView recyclerView;

    private List<ModelUser> userList;
    private AdapterUsers adapterUsers;

    private FirebaseAuth firebaseAuth;

    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(PostLikedByActivity.this);
        userType = mPrefs.getString("userType", "");

        //actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Liked By");
        //add back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail());

        recyclerView = findViewById(R.id.recyclerView);

        //get the post id
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        userList = new ArrayList<>();

        //get the list of UIDs of users who liked the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String hisUid = ""+ ds.getRef().getKey();
                    String hisNode = ""+ ds.getValue().toString();

                    //get user info from each id
                    getUsers(hisUid,hisNode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsers(String hisUid, final String hisNode) {
        // get information of each user using uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(hisNode);
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelUser modelUser = ds.getValue(ModelUser.class);
                            modelUser.setNode(hisNode);
                            userList.add(modelUser);
                        }

                        //setup adapter
                        adapterUsers = new AdapterUsers(PostLikedByActivity.this, userList,userType);
                        //set adapter to recyclerview
                        recyclerView.setAdapter(adapterUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(PostLikedByActivity.this);
    }
}
