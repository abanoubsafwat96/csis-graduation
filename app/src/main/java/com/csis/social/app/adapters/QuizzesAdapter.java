package com.csis.social.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.csis.social.app.R;
import com.csis.social.app.models.Quiz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class QuizzesAdapter extends BaseAdapter {

    Context context;
    ArrayList<Quiz> quizzes_list;
    String activityType;

    private SimpleDateFormat dateFormatForDay;
    boolean[] choosed_quizzes_arr;

    public QuizzesAdapter(Context context, ArrayList<Quiz> quizzes_list, String activityType) {
        this.context = context;
        this.quizzes_list = quizzes_list;
        this.activityType = activityType;

        dateFormatForDay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        choosed_quizzes_arr = new boolean[quizzes_list.size()];
    }

    @Override
    public int getCount() {
        return quizzes_list.size();
    }

    @Override
    public Object getItem(int i) {
        return quizzes_list.get(i);
    }

    public boolean[] getChoosed_quizzes_arr() {
        return choosed_quizzes_arr;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.quiz_list_item, null);
        }

        Quiz quiz = (Quiz) getItem(i);

        TextView dealine = (TextView) view.findViewById(R.id.deadline);
        Date deadline_date = new Date(Long.parseLong(quiz.deadline));
        dealine.setText(dateFormatForDay.format(deadline_date));

        if (activityType.equals("StudentChooseQuiz")) {
            if (deadline_date.before(Calendar.getInstance().getTime()))
                (view.findViewById(R.id.layout)).setBackgroundColor(
                        context.getResources().getColor(R.color.disabled_button));
            else
                (view.findViewById(R.id.layout)).setBackgroundColor(
                        context.getResources().getColor(R.color.white));
        }

        ((TextView) view.findViewById(R.id.title)).setText(quiz.title);
        ((TextView) view.findViewById(R.id.description)).setText(quiz.description);

        if (quiz.bonusQuestions_list == null)
            ((TextView) view.findViewById(R.id.questions_count))
                    .setText(quiz.questions_list.size() + " Questions and No Bonus Questions");
        else
            ((TextView) view.findViewById(R.id.questions_count))
                    .setText(quiz.questions_list.size() + " Questions and "
                            + quiz.bonusQuestions_list.size() + " Bonus Questions");
        return view;
    }
}
