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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.csis.social.app.AddPostActivity;
import com.csis.social.app.MainActivity;
import com.csis.social.app.R;
import com.csis.social.app.SettingsActivity;
import com.csis.social.app.TeacherQuizActivity;
import com.csis.social.app.Utilities;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

public class ChooseSubjectFragment extends Fragment {

    Spinner levelSpinner, semesterSpinner;
    View line;
    ListView listView;
    TextView noSubjects;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_subject, container, false);

        levelSpinner = view.findViewById(R.id.levelSpinner);
        semesterSpinner = view.findViewById(R.id.semesterSpinner);
        line = view.findViewById(R.id.line);
        listView = view.findViewById(R.id.list);
        noSubjects = view.findViewById(R.id.noSubjects);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Utilities.getAllLevels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);

        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext()
                        , android.R.layout.simple_spinner_item, Utilities.getAllSemesters());
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                semesterSpinner.setAdapter(adapter2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                databaseReference = firebaseDatabase.getReference().child("Courses").child(levelSpinner.getSelectedItem().toString())
                        .child(semesterSpinner.getSelectedItem().toString());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> list = Utilities.getAllSubjectsFromSemester(dataSnapshot);
                        fillListView(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getContext(), TeacherQuizActivity.class);
                intent.putExtra("subject", (String) adapterView.getAdapter().getItem(i));
                startActivity(intent);
            }
        });
        return view;
    }

    private void fillListView(ArrayList<String> list) {
        if (list.size() == 0)
            noSubjects.setVisibility(View.VISIBLE);
        else {
            if (noSubjects.getVisibility() == View.VISIBLE)
                noSubjects.setVisibility(View.GONE);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, list);
            listView.setAdapter(adapter);
        }
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

    public void clearSharedPreference(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        //editing into shared preference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userType", "no users");
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(getContext());
    }
}
