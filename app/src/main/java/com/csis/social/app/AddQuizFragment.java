package com.csis.social.app;

import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class AddQuizFragment extends Fragment implements Communicator {

    TextView deadline, noQuestions, noBonusQuestions;
    EditText title, description, timerMinutes, timerSeconds, questionED, choice1, choice2, choice3, choice4, bonusQuestionED, bonusChoice1, bonusChoice2, bonusChoice3, bonusChoice4;
    ImageView mark1, mark2, mark3, mark4, addQuestionArrow, bonusMark1, bonusMark2, bonusMark3, bonusMark4, addBonusQuestionArrow;
    RelativeLayout addQuestionRelative, addBonusQuestionRelative;
    LinearLayout addQuestionLinear, addBonusQuestionLinear;
    ListView listView, bonusListView;
    Button addQuestion_btn, addBonusQuestion_btn, finish_btn;
    private String deadlineDate;
    QuestionAdapter adapter, bonusAdapter;

    private String subject;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference quizReference, quizGradesReference;
    ValueEventListener quizGradesValueEventListener;
    private SimpleDateFormat dateFormatForDay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_quiz, container, false);

        subject = getArguments().getString("subject");

        deadline = view.findViewById(R.id.deadline2);
        title = view.findViewById(R.id.title2);
        description = view.findViewById(R.id.description2);
        timerMinutes = view.findViewById(R.id.minutes);
        timerSeconds = view.findViewById(R.id.seconds);
        //Add Question
        questionED = view.findViewById(R.id.question);
        choice1 = view.findViewById(R.id.choice1);
        choice2 = view.findViewById(R.id.choice2);
        choice3 = view.findViewById(R.id.choice3);
        choice4 = view.findViewById(R.id.choice4);
        mark1 = view.findViewById(R.id.mark1);
        mark2 = view.findViewById(R.id.mark2);
        mark3 = view.findViewById(R.id.mark3);
        mark4 = view.findViewById(R.id.mark4);
        addQuestionRelative = view.findViewById(R.id.addQuestionRelative);
        addQuestionArrow = view.findViewById(R.id.addQuestionArrow);
        addQuestionLinear = view.findViewById(R.id.addQuestionLinear);
        addQuestion_btn = view.findViewById(R.id.addQuestion_btn);
        listView = view.findViewById(R.id.listView);
        noQuestions = view.findViewById(R.id.noQuestions);
        //Add Bonus Question
        bonusQuestionED = view.findViewById(R.id.bonusQuestion);
        bonusChoice1 = view.findViewById(R.id.bonusChoice1);
        bonusChoice2 = view.findViewById(R.id.bonusChoice2);
        bonusChoice3 = view.findViewById(R.id.bonusChoice3);
        bonusChoice4 = view.findViewById(R.id.bonusChoice4);
        bonusMark1 = view.findViewById(R.id.bonusMark1);
        bonusMark2 = view.findViewById(R.id.bonusMark2);
        bonusMark3 = view.findViewById(R.id.bonusMark3);
        bonusMark4 = view.findViewById(R.id.bonusMark4);
        addBonusQuestionRelative = view.findViewById(R.id.addBonusQuestionRelative);
        addBonusQuestionArrow = view.findViewById(R.id.addBonusQuestionArrow);
        addBonusQuestionLinear = view.findViewById(R.id.addBonusQuestionLinear);
        addBonusQuestion_btn = view.findViewById(R.id.addBonusQuestion_btn);
        bonusListView = view.findViewById(R.id.bonusListView);
        noBonusQuestions = view.findViewById(R.id.noBonusQuestions);

        finish_btn = view.findViewById(R.id.finish_btn);

        dateFormatForDay = new SimpleDateFormat("EEEE ',' dd MMMM yyyy", Locale.getDefault());

        adapter = new QuestionAdapter(getContext(), new ArrayList<Question>(), "questions_list", false);
        listView.setAdapter(adapter);

        bonusAdapter = new QuestionAdapter(getContext(), new ArrayList<Question>(), "bonusQuestions_list", false);
        bonusListView.setAdapter(bonusAdapter);

        deadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = CalendarDialogFragment.newInstance(AddQuizFragment.this);
                dialog.setTargetFragment(AddQuizFragment.this, 0);
                dialog.show(getFragmentManager(), "tag");
            }
        });

        addQuestionRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOrCloseQuestionPart();
            }
        });

        mark1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mark1.setImageResource(R.drawable.truemark);
                mark2.setImageResource(R.drawable.mark);
                mark3.setImageResource(R.drawable.mark);
                mark4.setImageResource(R.drawable.mark);
            }
        });

        mark2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mark2.setImageResource(R.drawable.truemark);
                mark1.setImageResource(R.drawable.mark);
                mark3.setImageResource(R.drawable.mark);
                mark4.setImageResource(R.drawable.mark);
            }
        });

        mark3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mark3.setImageResource(R.drawable.truemark);
                mark1.setImageResource(R.drawable.mark);
                mark2.setImageResource(R.drawable.mark);
                mark4.setImageResource(R.drawable.mark);
            }
        });

        mark4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mark4.setImageResource(R.drawable.truemark);
                mark1.setImageResource(R.drawable.mark);
                mark2.setImageResource(R.drawable.mark);
                mark3.setImageResource(R.drawable.mark);
            }
        });

        addQuestion_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quest = questionED.getText().toString();
                String ch1 = choice1.getText().toString();
                String ch2 = choice2.getText().toString();
                String ch3 = choice3.getText().toString();
                String ch4 = choice4.getText().toString();
                String answer = null;

                if (((BitmapDrawable) mark1.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.truemark)).getBitmap()))
                    answer = ch1;
                else if (((BitmapDrawable) mark2.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.truemark)).getBitmap()))
                    answer = ch2;
                else if (((BitmapDrawable) mark3.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.truemark)).getBitmap()))
                    answer = ch3;
                else if (((BitmapDrawable) mark4.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.truemark)).getBitmap()))
                    answer = ch4;

                if (!TextUtils.isEmpty(quest) && !TextUtils.isEmpty(ch1) && !TextUtils.isEmpty(ch2)
                        && !TextUtils.isEmpty(ch3) && !TextUtils.isEmpty(ch4)) {

                    if (answer != null) {
                        Question question = new Question(quest, answer, ch1, ch2, ch3, ch4);
                        adapter.questions_list.add(question);

                        refreshListView(adapter, listView, noQuestions);
                        clearAddQuestionFields();
                    } else
                        Toast.makeText(getContext(), "Choose the answer", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getContext(), "Fill question data", Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(getContext());
                alertDialogBuilder2.setMessage("Are you sure that you want to delete this question ?");
                alertDialogBuilder2.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                adapter.questions_list.remove(i);
                                refreshListView(adapter, listView, noQuestions);
                            }
                        });
                alertDialogBuilder2.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertDialogBuilder2.create().show();

                return true;
            }
        });

        addBonusQuestionRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOrCloseBonusQuestionPart();
            }
        });

        bonusMark1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bonusMark1.setImageResource(R.drawable.truemark);
                bonusMark2.setImageResource(R.drawable.mark);
                bonusMark3.setImageResource(R.drawable.mark);
                bonusMark4.setImageResource(R.drawable.mark);
            }
        });

        bonusMark2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bonusMark2.setImageResource(R.drawable.truemark);
                bonusMark1.setImageResource(R.drawable.mark);
                bonusMark3.setImageResource(R.drawable.mark);
                bonusMark4.setImageResource(R.drawable.mark);
            }
        });

        bonusMark3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bonusMark3.setImageResource(R.drawable.truemark);
                bonusMark1.setImageResource(R.drawable.mark);
                bonusMark2.setImageResource(R.drawable.mark);
                bonusMark4.setImageResource(R.drawable.mark);
            }
        });

        bonusMark4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bonusMark4.setImageResource(R.drawable.truemark);
                bonusMark1.setImageResource(R.drawable.mark);
                bonusMark2.setImageResource(R.drawable.mark);
                bonusMark3.setImageResource(R.drawable.mark);
            }
        });

        addBonusQuestion_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quest = bonusQuestionED.getText().toString();
                String ch1 = bonusChoice1.getText().toString();
                String ch2 = bonusChoice2.getText().toString();
                String ch3 = bonusChoice3.getText().toString();
                String ch4 = bonusChoice4.getText().toString();
                String answer = null;

                if (((BitmapDrawable) bonusMark1.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.truemark)).getBitmap()))
                    answer = ch1;
                else if (((BitmapDrawable) bonusMark2.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.truemark)).getBitmap()))
                    answer = ch2;
                else if (((BitmapDrawable) bonusMark3.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.truemark)).getBitmap()))
                    answer = ch3;
                else if (((BitmapDrawable) bonusMark4.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) getResources().getDrawable(R.drawable.truemark)).getBitmap()))
                    answer = ch4;

                if (!TextUtils.isEmpty(quest) && !TextUtils.isEmpty(ch1) && !TextUtils.isEmpty(ch2)
                        && !TextUtils.isEmpty(ch3) && !TextUtils.isEmpty(ch4)) {

                    if (answer != null) {
                        Question question = new Question(quest, answer, ch1, ch2, ch3, ch4);
                        bonusAdapter.questions_list.add(question);

                        refreshListView(bonusAdapter, bonusListView, noBonusQuestions);
                        clearAddBonusQuestionFields();
                    } else
                        Toast.makeText(getContext(), "Choose the answer", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getContext(), "Fill question data", Toast.LENGTH_SHORT).show();
            }
        });

        bonusListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(getContext());
                alertDialogBuilder2.setMessage("Are you sure that you want to delete this question ?");
                alertDialogBuilder2.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                bonusAdapter.questions_list.remove(i);
                                refreshListView(bonusAdapter, bonusListView, noBonusQuestions);
                            }
                        });
                alertDialogBuilder2.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertDialogBuilder2.create().show();

                return true;
            }
        });

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deadlineDate != null) {
                    if (adapter.getCount() > 0) {
                        if (!TextUtils.isEmpty(title.getText()) && !TextUtils.isEmpty(description.getText())) {
                            if (!TextUtils.isEmpty(timerMinutes.getText()) && !TextUtils.isEmpty(timerSeconds.getText())) {
                                firebaseDatabase = FirebaseDatabase.getInstance();
                                quizReference = firebaseDatabase.getReference().child("Quizzes").child(subject);

                                final String pushID = quizReference.push().getKey();
                                final Quiz quiz;
                                String timerInMilliSec = ((Long.parseLong(timerMinutes.getText().toString()) * 60)
                                        + (Long.parseLong(timerSeconds.getText().toString()))) * 1000 + "";

                                if (bonusAdapter.getCount() > 0) {
                                    quiz = new Quiz(title.getText().toString(), description.getText().toString(), timerInMilliSec
                                            , adapter.questions_list, bonusAdapter.questions_list, deadlineDate);
                                } else {
                                    quiz = new Quiz(title.getText().toString(), description.getText().toString(),timerInMilliSec
                                            , adapter.questions_list, deadlineDate);
                                }

                                quizReference.child(pushID).setValue(quiz);

                                //Adding quiz to grades branch
                                quizGradesReference = firebaseDatabase.getReference().child("Grades").child(subject);
                                quizGradesValueEventListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        boolean quizExist = Utilities.checkIfQuizExistInGrades(dataSnapshot);

                                        if (!quizExist) {
                                            Map<String, Object> titleHashMap = new HashMap<>();
                                            titleHashMap.put("title", quiz.title);
                                            quizGradesReference.child(pushID).setValue(titleHashMap);
                                            stopQuizGradesReferenceListening();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                };
                                quizGradesReference.addValueEventListener(quizGradesValueEventListener);

                                deadline.setText("Select a date...");
                                title.setText("");
                                description.setText("");
                                adapter.questions_list.clear();
                                bonusAdapter.questions_list.clear();

                                refreshListView(adapter, listView, noQuestions);
                                refreshListView(bonusAdapter, bonusListView, noBonusQuestions);

                                clearAddQuestionFields();
                                clearAddBonusQuestionFields();

                                openOrCloseQuestionPart();
                                openOrCloseBonusQuestionPart();

                                Toast.makeText(getContext(), "Quiz Added Successfully", Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(getContext(), "Enter countdown timer minutes and seconds", Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(getContext(), "Enter title and description", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getContext(), "Add one main question at least", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getContext(), "Choose a deadline", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void openOrCloseQuestionPart() {
        if (addQuestionLinear.getVisibility() == View.GONE) {
            addQuestionLinear.setVisibility(View.VISIBLE);
            addQuestionArrow.setImageResource(R.drawable.ic_arrow_drop_up_trquaz_24dp);
        } else {
            addQuestionLinear.setVisibility(View.GONE);
            addQuestionArrow.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
        }
    }

    private void openOrCloseBonusQuestionPart() {
        if (addBonusQuestionLinear.getVisibility() == View.GONE) {
            addBonusQuestionLinear.setVisibility(View.VISIBLE);
            addBonusQuestionArrow.setImageResource(R.drawable.ic_arrow_drop_up_trquaz_24dp);
        } else {
            addBonusQuestionLinear.setVisibility(View.GONE);
            addBonusQuestionArrow.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
        }
    }

    private void refreshListView(QuestionAdapter adapter1, ListView listView1, TextView noData) {
        if (adapter1.getCount() == 0)
            noData.setVisibility(View.VISIBLE);
        else {
            if (noData.getVisibility() == View.VISIBLE)
                noData.setVisibility(View.GONE);
        }
        adapter1.notifyDataSetChanged();
        Utilities.getTotalHeightofListView(listView1);
    }

    private void clearAddQuestionFields() {
        questionED.setText("");
        choice1.setText("");
        choice2.setText("");
        choice3.setText("");
        choice4.setText("");
        mark1.setImageResource(R.drawable.mark);
        mark2.setImageResource(R.drawable.mark);
        mark3.setImageResource(R.drawable.mark);
        mark4.setImageResource(R.drawable.mark);
    }

    private void clearAddBonusQuestionFields() {
        bonusQuestionED.setText("");
        bonusChoice1.setText("");
        bonusChoice2.setText("");
        bonusChoice3.setText("");
        bonusChoice4.setText("");
        bonusMark1.setImageResource(R.drawable.mark);
        bonusMark2.setImageResource(R.drawable.mark);
        bonusMark3.setImageResource(R.drawable.mark);
        bonusMark4.setImageResource(R.drawable.mark);
    }

    @Override
    public void onDeadlineChoosed(Date dateClicked) {
        this.deadlineDate = dateClicked.getTime() + "";
        Log.e("onDeadlineChoosed: ", deadlineDate);
        deadline.setText(dateFormatForDay.format(dateClicked));
    }

    private void stopQuizGradesReferenceListening() {
        if (quizGradesReference != null && quizGradesValueEventListener != null) {
            quizGradesReference.removeEventListener(quizGradesValueEventListener);
        }
    }
}
