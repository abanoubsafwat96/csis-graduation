package com.csis.social.app;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class StudentQuizActivity extends AppCompatActivity {

    LinearLayout question_linear, prize_linear;
    TextView prize_textView, timer;
    ImageView prize_imageView;
    RelativeLayout next_finish_relative;
    TextView question_number, question_textView, choice1, choice2, choice3, choice4;
    Button finish_btn, next_btn;

    String subject, student_uid;
    Quiz quiz;

    int question_index = -1, bonusQuestion_index = -1;
    float questionsResult = 0, bonusQuestionsResult = 0;
    boolean nextQuestion_isAvailable = true, nextBonusQuestion_isAvailable = false, thisQuestion_isAvailable = true, checkIfGotStar = false, checkIfGotCrown = false;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference addGradeReference;
    private ValueEventListener valueEventListener;

    private CountDownTimer countDownTimer;
    private long TIME_LEFT_IN_MILLISECONDS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_quiz);

        subject = getIntent().getStringExtra("subject");
        quiz = getIntent().getParcelableExtra("quiz");
        quiz.questions_list = getIntent().getParcelableArrayListExtra("questions_list");
        quiz.bonusQuestions_list = getIntent().getParcelableArrayListExtra("bonusQuestions_list");
        student_uid = Utilities.getCurrentUID();

        firebaseDatabase = FirebaseDatabase.getInstance();

        question_linear = findViewById(R.id.question_linear);
        timer = findViewById(R.id.timer);
        prize_linear = findViewById(R.id.prize_linear);
        prize_textView = findViewById(R.id.prize_textView);
        prize_imageView = findViewById(R.id.prize_imageView);
        next_finish_relative = findViewById(R.id.next_finish_relative);
        question_number = findViewById(R.id.question_number);
        question_textView = findViewById(R.id.question);
        choice1 = findViewById(R.id.choice1);
        choice2 = findViewById(R.id.choice2);
        choice3 = findViewById(R.id.choice3);
        choice4 = findViewById(R.id.choice4);
        next_btn = findViewById(R.id.next_btn);
        finish_btn = findViewById(R.id.finish_btn);

        setNewQuestion();
        TIME_LEFT_IN_MILLISECONDS=Long.parseLong(quiz.timerInMilliSec);
        countDownStart();

        choice1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice1.setBackgroundColor(getResources().getColor(R.color.caldroid_light_red));
                choice2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        choice2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice2.setBackgroundColor(getResources().getColor(R.color.caldroid_light_red));
                choice3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        choice3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice3.setBackgroundColor(getResources().getColor(R.color.caldroid_light_red));
                choice4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        choice4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                choice4.setBackgroundColor(getResources().getColor(R.color.caldroid_light_red));
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (question_linear.getVisibility() == View.VISIBLE) {
                    int color1 = ((ColorDrawable) choice1.getBackground()).getColor();
                    int color2 = ((ColorDrawable) choice2.getBackground()).getColor();
                    int color3 = ((ColorDrawable) choice3.getBackground()).getColor();
                    int color4 = ((ColorDrawable) choice4.getBackground()).getColor();

                    String studentAnswer = null;

                    if (color1 == getResources().getColor(R.color.caldroid_light_red)) {
                        studentAnswer = choice1.getText().toString();
                    } else if (color2 == getResources().getColor(R.color.caldroid_light_red)) {
                        studentAnswer = choice2.getText().toString();
                    } else if (color3 == getResources().getColor(R.color.caldroid_light_red)) {
                        studentAnswer = choice3.getText().toString();
                    } else if (color4 == getResources().getColor(R.color.caldroid_light_red)) {
                        studentAnswer = choice4.getText().toString();
                    }

                    if (studentAnswer != null) {

                        int questionsCount = quiz.questions_list.size();

                        if (thisQuestion_isAvailable && question_index < questionsCount) {
                            Question question = quiz.questions_list.get(question_index);

                            if (studentAnswer.equals(question.answer)) {
                                questionsResult++;
                                Toast.makeText(StudentQuizActivity.this, "True", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(StudentQuizActivity.this, "False", Toast.LENGTH_SHORT).show();

                            if (question_index + 1 == questionsCount) //last question
                                thisQuestion_isAvailable = false; //stop executing this condition to start execute bonus questions condition

                        } else if (quiz.bonusQuestions_list != null) {

                            int bonusQuestionsCount = quiz.bonusQuestions_list.size();

                            if (bonusQuestion_index < bonusQuestionsCount) {
                                Question question = quiz.bonusQuestions_list.get(bonusQuestion_index);

                                if (studentAnswer.equals(question.answer)) {
                                    bonusQuestionsResult++;
                                    Toast.makeText(StudentQuizActivity.this, "True", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(StudentQuizActivity.this, "False", Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (nextQuestion_isAvailable)
                            setNewQuestion();

                        else if (checkIfGotStar) {
                            checkIfGotStar_func();

                        } else if (nextBonusQuestion_isAvailable) //if executed or any other down condition executed, it mean that bonusQuestion_list != null and user got 85% and will solve bonus questions.
                            setNewBonusQuestion();

                        else if (checkIfGotCrown)
                            checkIfGotCrown_func();

                        unMarkChoices();

                    } else
                        Toast.makeText(StudentQuizActivity.this, "Choose answer", Toast.LENGTH_SHORT).show();

                } else if (prize_linear.getVisibility() == View.VISIBLE) { //if executed, it mean that bonusQuestion_list != null and user got 85% and will solve bonus questions. so hide prize_linear and show question_linear
                    finish_btn.setVisibility(View.INVISIBLE);
                    prize_linear.setVisibility(View.GONE);
                    question_linear.setVisibility(View.VISIBLE);

                    setNewBonusQuestion();
                }
            }
        });

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onFinishClicked();
            }
        });
    }

    private void onFinishClicked() {
        addGradeReference = firebaseDatabase.getReference().child("Grades").child(subject).child(quiz.uid);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean quizExist = Utilities.checkIfQuizExistInGrades(dataSnapshot);

                if (!quizExist) {
                    Map<String, Object> titleHashMap = new HashMap<>();
                    titleHashMap.put("title", quiz.title);
                    addGradeReference.setValue(titleHashMap);
                }

                int final_grade;
                if (quiz.bonusQuestions_list == null)
                    final_grade = (int) ((questionsResult / quiz.questions_list.size()) * 100);
                else
                    final_grade = (int) (((questionsResult + bonusQuestionsResult)
                            / (quiz.questions_list.size() + quiz.bonusQuestions_list.size())) * 100);

                Map<String, Object> gradeHashMap = new HashMap<>();
                gradeHashMap.put("grade", final_grade);
                addGradeReference.child(student_uid).setValue(gradeHashMap);

                stopAddGradesReferenceListening();

                Toast.makeText(StudentQuizActivity.this, "You have got " + final_grade + " %", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        addGradeReference.addValueEventListener(valueEventListener);
    }

    private void checkIfGotStar_func() {

        int questionsCount = quiz.questions_list.size();
        float result = questionsResult / questionsCount;

        if (result >= 0.50) { //got star
            prize_imageView.setImageResource(R.drawable.star2);

            if (result >= 0.85) {

                if (quiz.bonusQuestions_list == null) {
                    next_btn.setVisibility(View.INVISIBLE);
                    prize_textView.setText("Congratulations, You got up to 85%");
                } else
                    prize_textView.setText("Congratulations, You got up to 85% so, you can solve bonus questions");
            } else {
                prize_textView.setText("You didn't get up to 85% so, you can't solve bonus questions");
                next_btn.setVisibility(View.INVISIBLE);
            }
        } else {
            next_btn.setVisibility(View.INVISIBLE);

            if (quiz.bonusQuestions_list == null)
                prize_textView.setText("You get less than 50%");
            else
                prize_textView.setText("You get less than 50% so, you can't solve bonus questions");
            prize_imageView.setImageResource(R.drawable.sadface);
        }
        finish_btn.setVisibility(View.VISIBLE);
        question_linear.setVisibility(View.GONE);
        prize_linear.setVisibility(View.VISIBLE);
        checkIfGotStar = false;
    }

    private void checkIfGotCrown_func() {

        prize_imageView.setImageResource(0);
        float result = bonusQuestionsResult / quiz.bonusQuestions_list.size();

        if (result >= 0.85) {
            prize_textView.setText("Wohoooo Congratulations, You got up to 85% also in bonus questions");
            prize_imageView.setImageResource(R.drawable.crown);
        } else {
            prize_textView.setText("Great, but You didn't get up to 85% in bonus questions");
            prize_imageView.setImageResource(R.drawable.star2);
        }

        next_btn.setVisibility(View.INVISIBLE);
        finish_btn.setVisibility(View.VISIBLE);
        question_linear.setVisibility(View.GONE);
        prize_linear.setVisibility(View.VISIBLE);
        checkIfGotCrown = false;
    }

    private void setNewQuestion() {

        int questionsCount = quiz.questions_list.size();
        if (question_index + 1 < questionsCount) {

            question_index++;

            question_number.setText("Question " + (question_index + 1));

            Question question = quiz.questions_list.get(question_index);
            fillQuestionFields(question);

            if (question_index + 1 == questionsCount) { //last question

                nextQuestion_isAvailable = false;
                checkIfGotStar = true;

                if (quiz.bonusQuestions_list != null)
                    nextBonusQuestion_isAvailable = true;
            }
        }
    }

    private void setNewBonusQuestion() {

        int bonusQuestionsCount = quiz.bonusQuestions_list.size();
        if (bonusQuestion_index + 1 < bonusQuestionsCount) {

            bonusQuestion_index++;

            question_number.setText("Bonus Question " + (bonusQuestion_index + 1));

            Question question = quiz.bonusQuestions_list.get(bonusQuestion_index);
            fillQuestionFields(question);

            if (bonusQuestion_index + 1 == bonusQuestionsCount) { //last bonus question

                nextBonusQuestion_isAvailable = false;
                checkIfGotCrown = true;
            }
        }
    }

    private void fillQuestionFields(Question question) {
        question_textView.setText(question.question);
        choice1.setText(question.choice1);
        choice2.setText(question.choice2);
        choice3.setText(question.choice3);
        choice4.setText(question.choice4);
    }

    private void unMarkChoices() {
        choice1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        choice2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        choice3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        choice4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "You must finish quiz first", Toast.LENGTH_SHORT).show();
    }

    private void stopAddGradesReferenceListening() {
        if (addGradeReference != null && valueEventListener != null) {
            addGradeReference.removeEventListener(valueEventListener);
        }
    }

    private void countDownStart() {
        countDownTimer = new CountDownTimer(TIME_LEFT_IN_MILLISECONDS, 1000) {

            public void onTick(long millisUntilFinished) {
                TIME_LEFT_IN_MILLISECONDS = millisUntilFinished;

                long timerInSec=millisUntilFinished/1000;
                long minutes=timerInSec/60;
                long seconds=timerInSec-(minutes*60);
                timer.setText(minutes + ":"+seconds);
            }

            public void onFinish() {
                timer.setText("Time is over !");
                onFinishClicked();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
