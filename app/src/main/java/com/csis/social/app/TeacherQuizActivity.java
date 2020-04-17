package com.csis.social.app;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class TeacherQuizActivity extends AppCompatActivity {

    private TeacherQuizPageAdapter adapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_quiz);

        String subject = getIntent().getStringExtra("subject");

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);

        Bundle bundle = new Bundle();
        bundle.putString("subject", subject);
        //set Fragment Arguments

        AddQuizFragment addQuizFragment = new AddQuizFragment();
        addQuizFragment.setArguments(bundle);
        OldQuizzesFragment oldQuizzesFragment = new OldQuizzesFragment();
        oldQuizzesFragment.setArguments(bundle);

        adapter = new TeacherQuizPageAdapter(getSupportFragmentManager());
        adapter.addFragment(addQuizFragment, "Add Quiz");
        adapter.addFragment(oldQuizzesFragment, "Old Quizzes");
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }
}
