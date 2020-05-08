package com.csis.social.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChooseQuizFragment extends Fragment {

    ListView listView;
    TextView noQuizzes;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference subjectsReference, quizGradesReference, studentUIDQuizGradeRef;
    private ValueEventListener quizGradesValueEventListener, studentUIDQuizGradeValueEventListener;

    String current_student_uid;
    private ArrayList<Quiz> quizzes_list;
    boolean quizGradesReference_isListening,studentUIDQuizGradeRef_isListening;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_quiz, container, false);

        current_student_uid = Utilities.getCurrentUID();

        listView = view.findViewById(R.id.list);
        noQuizzes = view.findViewById(R.id.noQuizzes);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        subjectsReference = firebaseDatabase.getReference().child("Users").child(current_student_uid).child("follow");
        subjectsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final ArrayList<String> subjects_list = Utilities.getFollowedSubjects(dataSnapshot);

                quizzes_list = new ArrayList<>();

                if (subjects_list.size()>0) {
                    for (int i = 0; i < subjects_list.size(); i++) {
                        final int position = i;
                        final String subject = subjects_list.get(position);

                        firebaseDatabase.getReference().child("Quizzes").child(subject)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        ArrayList<Quiz> reversed_quizzes_list = Utilities.gettAllQuizzes(dataSnapshot, subject);
                                        quizzes_list.addAll(reverseList(reversed_quizzes_list));

                                        if (position == subjects_list.size() - 1)
                                            fillListView(quizzes_list);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }else if (subjects_list.size()==0)
                    fillListView(new ArrayList<Quiz>());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final Quiz quiz = quizzes_list.get(i);

                Date deadline_date = new Date(Long.parseLong(quiz.deadline));
                if (deadline_date.before(Calendar.getInstance().getTime()))
                    Toast.makeText(getContext(), "Deadline Passed", Toast.LENGTH_SHORT).show();

                else {
                    quizGradesReference_isListening=true;
                    studentUIDQuizGradeRef_isListening=true;

                    quizGradesReference = firebaseDatabase.getReference().child("Grades").child(quiz.subject).child(quiz.uid);

                    quizGradesValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            quizGradesReference.removeEventListener(quizGradesValueEventListener);

                            if (quizGradesReference_isListening) {
                                quizGradesReference_isListening=false;

                                boolean quizExist = Utilities.checkIfQuizExistInGrades(dataSnapshot);

                                if (quizExist) {
                                    studentUIDQuizGradeRef = quizGradesReference.child(current_student_uid);
                                    studentUIDQuizGradeValueEventListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        studentUIDQuizGradeRef.removeEventListener(studentUIDQuizGradeValueEventListener);

                                            if (studentUIDQuizGradeRef_isListening) {
                                                studentUIDQuizGradeRef_isListening=false;

                                                String grade = Utilities.checkIfStudentGradeExistInQuiz(dataSnapshot);
                                                if (grade != null) {
                                                    Toast.makeText(getContext(),
                                                            "You entered this quiz before and got " + grade+" %", Toast.LENGTH_LONG).show();
                                                } else {
                                                    startStudentQuizActivity(quiz);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    };
                                    studentUIDQuizGradeRef.addValueEventListener(studentUIDQuizGradeValueEventListener);
                                } else {
                                    startStudentQuizActivity(quiz);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    quizGradesReference.addValueEventListener(quizGradesValueEventListener);
                }
            }
        });
        return view;
    }

    private void startStudentQuizActivity(Quiz quiz) {

        stopCompetitionGradesReferenceListening();

        Map<String, Object> gradeHashMap = new HashMap<>();
        gradeHashMap.put("grade", 0);
        quizGradesReference.child(current_student_uid).setValue(gradeHashMap);

        Intent intent = new Intent(getContext(), StudentQuizActivity.class);
        intent.putExtra("subject", quiz.subject);
        intent.putExtra("quiz", quiz);
        intent.putParcelableArrayListExtra("questions_list", quiz.questions_list);
        intent.putParcelableArrayListExtra("bonusQuestions_list", quiz.bonusQuestions_list);

        startActivity(intent);
    }

    private ArrayList<Quiz> reverseList(ArrayList<Quiz> reversed_quizzes_list) {
        ArrayList<Quiz> reversed_list = new ArrayList<>();

        for (int i = reversed_quizzes_list.size() - 1; i >= 0; i--) {
            reversed_list.add(reversed_quizzes_list.get(i));
        }

        return reversed_list;
    }

    private void fillListView(ArrayList<Quiz> quizzes_list) {
        if (quizzes_list.size() == 0)
            noQuizzes.setVisibility(View.VISIBLE);
        else {
            if (noQuizzes.getVisibility() == View.VISIBLE)
                noQuizzes.setVisibility(View.GONE);

            QuizzesAdapter adapter = new QuizzesAdapter(getContext(), quizzes_list
                    , "StudentChooseQuiz");
            listView.setAdapter(adapter);
        }
    }

    private void stopCompetitionGradesReferenceListening() {
        if (quizGradesReference != null && quizGradesValueEventListener != null) {
            quizGradesReference.removeEventListener(quizGradesValueEventListener);
        }
        if (studentUIDQuizGradeRef != null && studentUIDQuizGradeValueEventListener != null) {
            studentUIDQuizGradeRef.removeEventListener(studentUIDQuizGradeValueEventListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopCompetitionGradesReferenceListening();
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
