package com.csis.social.app.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.csis.social.app.AddPostActivity;
import com.csis.social.app.MainActivity;
import com.csis.social.app.R;
import com.csis.social.app.SettingsActivity;
import com.csis.social.app.Utilities;
import com.csis.social.app.adapters.AdapterPosts;
import com.csis.social.app.models.ModelPost;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    private TextView noPosts;
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    String userType;
    private DatabaseReference subjectsReference;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        userType = mPrefs.getString("userType", "");

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //recycler view and its properties
        noPosts = view.findViewById(R.id.noPosts);
        recyclerView = view.findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postList = new ArrayList<>();

        loadPosts();

        return view;
    }

    private void loadPosts() {
        String current_student_uid = Utilities.getCurrentUID();
        if (userType.equals("Student"))
            subjectsReference = firebaseDatabase.getReference().child("Users").child(current_student_uid).child("follow");
        else if (userType.equals("Admin"))
            subjectsReference = firebaseDatabase.getReference().child("Admins").child(current_student_uid).child("follow");

        subjectsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final ArrayList<String> subjects_list = Utilities.getFollowedSubjects(dataSnapshot);

                //path of all posts
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                //get all data from this ref
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        postList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelPost modelPost = ds.getValue(ModelPost.class);

                            for (int i = 0; i < subjects_list.size(); i++) {
                                if (modelPost != null && modelPost.getSubject() != null && modelPost.getSubject().equals(subjects_list.get(i))) {
                                    postList.add(modelPost);
                                    break;
                                }
                            }
                            if (postList.size() > 0) {
                                noPosts.setVisibility(View.GONE);

                                //adapter
                                adapterPosts = new AdapterPosts(getActivity(), postList, userType);
                                //set adapter to recyclerview
                                recyclerView.setAdapter(adapterPosts);
                            }else {
                                noPosts.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //in case of error
                        Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchPosts(final String searchQuery) {

        String current_student_uid = Utilities.getCurrentUID();
        if (userType.equals("Student"))
            subjectsReference = firebaseDatabase.getReference().child("Users").child(current_student_uid).child("follow");
        else if (userType.equals("Admin"))
            subjectsReference = firebaseDatabase.getReference().child("Admins").child(current_student_uid).child("follow");

        subjectsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final ArrayList<String> subjects_list = Utilities.getFollowedSubjects(dataSnapshot);

                //path of all posts
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                //get all data from this ref
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        postList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelPost modelPost = ds.getValue(ModelPost.class);

                            if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                    modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                                for (int i = 0; i < subjects_list.size(); i++) {
                                    if (modelPost != null && modelPost.getSubject() != null && modelPost.getSubject().equals(subjects_list.get(i))) {
                                        postList.add(modelPost);
                                        break;
                                    }
                                }
                            }

                            //adapter
                            adapterPosts = new AdapterPosts(getActivity(), postList,userType);
                            //set adapter to recyclerview
                            recyclerView.setAdapter(adapterPosts);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //in case of error
                        Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    /*inflate options menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide some options
        menu.findItem(R.id.action_create_group).setVisible(false);

        //searchview to search posts by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);
                } else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);
                } else {
                    loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    /*handle menu item clicks*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            clearSharedPreference();
            checkUserStatus();
        } else if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        } else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void clearSharedPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        //editing into shared preference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userType", "no users");
        editor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (adapterPosts!=null)
            adapterPosts.pausePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(getContext());

        if (adapterPosts!=null)
            adapterPosts.releaseExoPlayer();
    }
}
