package com.csis.social.app;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;

import com.csis.social.app.models.Quiz;
import com.csis.social.app.models.QuizGrades;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class GradesGraphActivity extends AppCompatActivity {

    BarChart barChart;
    private ArrayList<String> xValues;
    private ArrayList<BarEntry> yValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades_graph);

        barChart = findViewById(R.id.barChart);
        Description description = new Description();


        final Quiz quiz = getIntent().getParcelableExtra("quiz");
        xValues = getIntent().getStringArrayListExtra("usernames_list");
        ArrayList<String> grades_list = getIntent().getStringArrayListExtra("grades_list");

        if (quiz != null) {
            description.setText(quiz.title);
        }

        barChart.setDescription(description);

        Legend l = barChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(true);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        yValues = new ArrayList<>();

        for (int i = 0; i < grades_list.size(); i++) {
            yValues.add(new BarEntry(i, Integer.parseInt(grades_list.get(i))));
        }

        setXAxis(xValues);
    }

    private void setXAxis(final ArrayList<String> xValues) {

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setGranularity(1f); //prevent duplicating xAxis values
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value < xValues.size())
                    return xValues.get((int) value); // xVal is a string array
                else
                    return "loading";
            }
        });

        setYAxis();
        barChart.setFitBars(true); //center barchart with mounth line
    }

    private void setYAxis() {

        BarDataSet set = new BarDataSet(yValues, "Grades");
        set.setColor(Color.rgb(60, 220, 78));
        set.setDrawValues(true);

        BarData data = new BarData(set);

        barChart.setData(data);
        barChart.invalidate();
        barChart.animateY(500);
    }
}
