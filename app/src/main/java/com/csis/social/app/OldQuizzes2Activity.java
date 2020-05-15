package com.csis.social.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.csis.social.app.adapters.QuestionAdapter;
import com.csis.social.app.models.Quiz;

import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class OldQuizzes2Activity extends AppCompatActivity {

    TextView title, deadline, description, noBonusQuestions;
    LinearLayout showGrades_link;
    EditText timerMinutes,timerSeconds;
    ListView listView, bonusListView;
    private SimpleDateFormat dateFormatForDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_quizzes2);

        final Quiz quiz = getIntent().getParcelableExtra("quiz");
        quiz.questions_list = getIntent().getParcelableArrayListExtra("questions_list");
        quiz.bonusQuestions_list = getIntent().getParcelableArrayListExtra("bonusQuestions_list");

        title = findViewById(R.id.title2);
        deadline = findViewById(R.id.deadline2);
        description = findViewById(R.id.description2);
        timerMinutes = findViewById(R.id.minutes);
        timerSeconds = findViewById(R.id.seconds);
        showGrades_link=findViewById(R.id.showGrades_link);
        listView = findViewById(R.id.listView);
        bonusListView = findViewById(R.id.bonusListView);
        noBonusQuestions = findViewById(R.id.noBonusQuestions);

        dateFormatForDay = new SimpleDateFormat("EEEE ',' dd MMMM yyyy", Locale.getDefault());

        title.setText(quiz.title);
        deadline.setText(dateFormatForDay.format(Long.parseLong(quiz.deadline)));
        description.setText(quiz.description);

        long timerInSec=Long.parseLong(quiz.timerInMilliSec)/1000;
        long minutes=timerInSec/60;
        long seconds=timerInSec-(minutes*60);
        timerMinutes.setText(minutes+" min");
        timerSeconds.setText(seconds+" sec");

        showGrades_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OldQuizzes2Activity.this, GradesActivity.class);
                intent.putExtra("quiz", quiz);
                startActivity(intent);
            }
        });

        if (quiz.questions_list != null) {
            QuestionAdapter adapter = new QuestionAdapter(this, quiz.questions_list
                    , "questions_list", false);
            listView.setAdapter(adapter);
            Utilities.getTotalHeightofListView2(listView);
        }
        if (quiz.bonusQuestions_list != null) {
            if (quiz.bonusQuestions_list.size() > 0) {
                QuestionAdapter adapter2 = new QuestionAdapter(this, quiz.bonusQuestions_list
                        , "bonusQuestions_list", false);
                bonusListView.setAdapter(adapter2);
                Utilities.getTotalHeightofListView2(bonusListView);
                noBonusQuestions.setVisibility(View.GONE);
            } else
                noBonusQuestions.setVisibility(View.VISIBLE);
        } else
            noBonusQuestions.setVisibility(View.VISIBLE);
    }
}
