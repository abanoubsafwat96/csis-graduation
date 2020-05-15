package com.csis.social.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.csis.social.app.adapters.GradesAdapter;
import com.csis.social.app.models.Quiz;
import com.csis.social.app.models.QuizGrades;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GradesActivity extends AppCompatActivity {

    TextView noGrades;
    RecyclerView recyclerView;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private Quiz quiz;
    private ArrayList<String> usernames_list,grades_list;

    private GradesAdapter gradesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        quiz = getIntent().getParcelableExtra("quiz");

        noGrades = findViewById(R.id.noGrades);
        recyclerView = findViewById(R.id.recyclerView);

        firebaseDatabase = FirebaseDatabase.getInstance();

        if (quiz != null) {

            databaseReference = firebaseDatabase.getReference().child("Grades").child(quiz.subject).child(quiz.uid);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    QuizGrades quizGrades = Utilities.getStudentsGradesInQuiz(dataSnapshot);

                    usernames_list = new ArrayList<>();
                    grades_list= new ArrayList<>();

                    final Map<String, String> grades_map = quizGrades.grades_map;

                    Iterator it = grades_map.entrySet().iterator();
                    for (int i = 0; i < grades_map.size(); i++) {
                        final int position = i;

                        Map.Entry pair = (Map.Entry) it.next();
                        String key = pair.getKey() + "";

                        grades_list.add(pair.getValue().toString());

                        firebaseDatabase.getReference().child("Users").child(key)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String studentName = Utilities.getUserFullName(dataSnapshot);
                                        if (studentName != null && !TextUtils.isEmpty(studentName))
                                            usernames_list.add(studentName);
                                        else if (studentName != null && !TextUtils.isEmpty(studentName))
                                            usernames_list.add("User without name");
                                        else
                                            usernames_list.add("User without name");

                                        if (position == grades_map.size() - 1)
                                            fillRecyclerView(usernames_list, grades_list);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void fillRecyclerView( ArrayList<String> usernames_list, ArrayList<String> grades_list) {
        if (usernames_list.size() == 0)
            noGrades.setVisibility(View.VISIBLE);
        else {
            if (noGrades.getVisibility() == View.VISIBLE)
                noGrades.setVisibility(View.GONE);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
            recyclerView.setLayoutManager(layoutManager);

            gradesAdapter = new GradesAdapter(this, usernames_list, grades_list);
            recyclerView.setAdapter(gradesAdapter);
            recyclerView.setHasFixedSize(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grades, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_chart) {
            Intent intent = new Intent(GradesActivity.this, GradesGraphActivity.class);
            intent.putExtra("quiz", quiz);
            intent.putStringArrayListExtra("usernames_list",usernames_list);
            intent.putStringArrayListExtra("grades_list",grades_list);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
