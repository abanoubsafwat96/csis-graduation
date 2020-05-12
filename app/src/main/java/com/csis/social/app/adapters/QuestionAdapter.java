package com.csis.social.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.csis.social.app.models.Question;
import com.csis.social.app.R;

import java.util.ArrayList;

public class QuestionAdapter extends BaseAdapter {
    Context context;
    public ArrayList<Question> questions_list;
    String adapterType;
    boolean needCheckbox;
    boolean[] choosed_questions_arr;

    public QuestionAdapter(Context context, ArrayList<Question> questions_list, String adapterType, boolean needCheckbox) {
        this.context = context;
        this.questions_list = questions_list;
        this.adapterType = adapterType;
        this.needCheckbox=needCheckbox;
        choosed_questions_arr=new boolean[questions_list.size()];
    }

    @Override
    public int getCount() {
        return questions_list.size();
    }

    @Override
    public Object getItem(int i) {
        return questions_list.get(i);
    }

    public boolean[] getChoosed_questions_arr() {
        return choosed_questions_arr;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.questions_list_item, null);
        }

        Question question = (Question) getItem(i);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        TextView questionTitle = (TextView) view.findViewById(R.id.questionTitle);
        TextView questionTV = (TextView) view.findViewById(R.id.question);
        TextView ch1 = (TextView) view.findViewById(R.id.choice1);
        TextView ch2 = (TextView) view.findViewById(R.id.choice2);
        TextView ch3 = (TextView) view.findViewById(R.id.choice3);
        TextView ch4 = (TextView) view.findViewById(R.id.choice4);

        int questionNum = i + 1;
        if (adapterType.equals("questions_list"))
            questionTitle.setText("Question " + questionNum);
        else if (adapterType.equals("bonusQuestions_list"))
            questionTitle.setText("Bonus Question " + questionNum);

        questionTV.setText(question.question);
        ch1.setText(question.choice1);
        ch2.setText(question.choice2);
        ch3.setText(question.choice3);
        ch4.setText(question.choice4);

        if (question.choice1.equals(question.answer))
            ch1.setBackgroundColor(context.getResources().getColor(R.color.caldroid_light_red));
        else if (question.choice2.equals(question.answer))
            ch2.setBackgroundColor(context.getResources().getColor(R.color.caldroid_light_red));
        else if (question.choice3.equals(question.answer))
            ch3.setBackgroundColor(context.getResources().getColor(R.color.caldroid_light_red));
        else if (question.choice4.equals(question.answer))
            ch4.setBackgroundColor(context.getResources().getColor(R.color.caldroid_light_red));

        if (needCheckbox)
            checkBox.setVisibility(View.VISIBLE);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                choosed_questions_arr[i] = isChecked;
            }
        });

        return view;
    }
}
