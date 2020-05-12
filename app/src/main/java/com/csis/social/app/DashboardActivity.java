package com.csis.social.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.csis.social.app.fragments.ChatListFragment;
import com.csis.social.app.fragments.ChooseQuizFragment;
import com.csis.social.app.fragments.ChooseSubjectFragment;
import com.csis.social.app.fragments.HomeFragment;
import com.csis.social.app.fragments.ProfileFragment;
import com.csis.social.app.fragments.UsersFragment;
import com.csis.social.app.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
        //reading from shared preference
        userType = sharedPreferences.getString("userType", "not found");

        if (userType.equals("Student"))
            setContentView(R.layout.activity_student_dashboard);
        else if (userType.equals("Admin"))
            setContentView(R.layout.activity_admin_dashboard);
        else
            checkUserStatus();

        //Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //bottom navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        if (navigationView!=null)
            navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment transaction (default, on star)
        actionBar.setTitle("Home");//change actionbar title
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        checkUserStatus();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //handle item clicks
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            //home fragment transaction
                            actionBar.setTitle("Home");//change actionbar title
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_Quizzes:
                            FragmentTransaction ft = null;
                            actionBar.setTitle("Quizzes");//change actionbar title
                            if (userType.equals("Admin")) {
                                ChooseSubjectFragment chooseSubjectFragment = new ChooseSubjectFragment();
                                ft = getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.content, chooseSubjectFragment, "");

                            } else if (userType.equals("Student")) {
                                ChooseQuizFragment chooseQuizFragment = new ChooseQuizFragment();
                                ft = getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.content, chooseQuizFragment, "");
                            }
                            ft.commit();
                            return true;
                        case R.id.nav_profile:
                            //profile fragment transaction
                            actionBar.setTitle("Profile");//change actionbar title
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            //users fragment transaction
                            actionBar.setTitle("Users");//change actionbar title
                            UsersFragment fragment3 = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;
                        case R.id.nav_chat:
                            //users fragment transaction
                            actionBar.setTitle("Chats");//change actionbar title
                            ChatListFragment fragment4 = new ChatListFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4, "");
                            ft4.commit();
                            return true;
//                        case R.id.nav_notification:
//                            //users fragment transaction
//                            actionBar.setTitle("Notifications");//change actionbar title
//                            NotificationsFragment fragment5 = new NotificationsFragment();
//                            FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
//                            ft5.replace(R.id.content, fragment5, "");
//                            ft5.commit();
//                            return true;
                    }

                    return false;
                }
            };

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            mUID = user.getUid();

            //save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            //update token
            updateToken(FirebaseInstanceId.getInstance().getToken());

        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
            return;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        moveTaskToBack(true); //exit app
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
    }
}
