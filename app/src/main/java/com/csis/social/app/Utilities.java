package com.csis.social.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.csis.social.app.models.Quiz;
import com.csis.social.app.models.QuizGrades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class Utilities {

    public static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public static FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public static String getCurrentEmail() {
        return getCurrentUser().getEmail();
    }

    public static String getCurrentUID() {
        return getCurrentUser().getUid();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean checkIfUserExist(DataSnapshot dataSnapshot, String currentUID) {

        ArrayList<String> list = new ArrayList<>();

        //iterate through each Msg, ignoring their UID
        if (dataSnapshot.getValue() != null) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {

                list.add(child.getKey());
            }
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(currentUID))
                return true;
        }
        return false;
    }

    public static ArrayList<String> getUIDs(DataSnapshot dataSnapshot) {

        ArrayList<String> list = new ArrayList<>();

        if (dataSnapshot.getValue() != null) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                Object uid = child.child("uid").getValue();
//                if (uid !=null)
                list.add(uid.toString());

//                String studentUID=child.getValue(Student.class).uid;
//                list.add(studentUID);
            }
        }
        return list;
    }

    public static String getUserFullName(DataSnapshot dataSnapshot) {

        if (dataSnapshot.getValue() != null) {
            Object fullname = dataSnapshot.child("name").getValue();
            if (fullname != null)
                return fullname.toString();
        }
        return null;
    }

    public static ArrayList<String> getAllSubjectsFromSemester(DataSnapshot dataSnapshot) {
        ArrayList<String> list = new ArrayList<>();

        if (dataSnapshot.getValue() != null) {

            Object subject1 = dataSnapshot.child("subject1").getValue();
            if (subject1 != null)
                list.add(subject1.toString());
            Object subject2 = dataSnapshot.child("subject2").getValue();
            if (subject2 != null)
                list.add(subject2.toString());
            Object subject3 = dataSnapshot.child("subject3").getValue();
            if (subject3 != null)
                list.add(subject3.toString());
            Object subject4 = dataSnapshot.child("subject4").getValue();
            if (subject4 != null)
                list.add(subject4.toString());
            Object subject5 = dataSnapshot.child("subject5").getValue();
            if (subject5 != null)
                list.add(subject5.toString());
            Object subject6 = dataSnapshot.child("subject6").getValue();
            if (subject6 != null)
                list.add(subject6.toString());
        }
        return list;
    }

    public static ArrayList<String> getAllSubjectsAllLevelsAndSemesters(DataSnapshot dataSnapshot) {
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> allLevels=getAllLevels();
        ArrayList<String> allSemesters=getAllSemesters();

        if (dataSnapshot.getValue() != null) {

            for (int i=0;i<allLevels.size();i++){
                String level=allLevels.get(i);

                for (int j=0;j<allSemesters.size();j++){
                    String semester=allSemesters.get(j);

                    Object subject1 = dataSnapshot.child(level).child(semester).child("subject1").getValue();
                    if (subject1 != null)
                        list.add(subject1.toString());
                    Object subject2 = dataSnapshot.child(level).child(semester).child("subject2").getValue();
                    if (subject2 != null)
                        list.add(subject2.toString());
                    Object subject3 = dataSnapshot.child(level).child(semester).child("subject3").getValue();
                    if (subject3 != null)
                        list.add(subject3.toString());
                    Object subject4 = dataSnapshot.child(level).child(semester).child("subject4").getValue();
                    if (subject4 != null)
                        list.add(subject4.toString());
                    Object subject5 = dataSnapshot.child(level).child(semester).child("subject5").getValue();
                    if (subject5 != null)
                        list.add(subject5.toString());
                    Object subject6 = dataSnapshot.child(level).child(semester).child("subject6").getValue();
                    if (subject6 != null)
                        list.add(subject6.toString());
                }
            }
        }
        return list;
    }

    public static QuizGrades getStudentsGradesInQuiz(DataSnapshot dataSnapshot) {

        QuizGrades quizGrades=new QuizGrades();

        if (dataSnapshot.getValue() != null) {
            if (dataSnapshot.child("title").getValue() != null)
                quizGrades.title = dataSnapshot.child("title").getValue().toString();

            quizGrades.grades_map = new HashMap<>();

            for (DataSnapshot child : dataSnapshot.getChildren()) {
                String uid = child.getKey();
                if (!uid.equals("title")) {
                    Object grade = child.child("grade").getValue();
                    if (grade == null)
                        quizGrades.grades_map.put(uid, "0");
                    else
                        quizGrades.grades_map.put(uid, grade.toString());
                }
            }
        }
        return quizGrades;
    }

    public static boolean checkIfQuizExistInGrades(DataSnapshot dataSnapshot) {

        if (dataSnapshot.getValue() != null) {

            if (dataSnapshot.child("title").getValue() != null)
                return true;
            else
                return false;
        }
        return false;
    }

    public static ArrayList<Quiz> gettAllQuizzes(DataSnapshot dataSnapshot, String subject) {
        ArrayList<Quiz> list = new ArrayList<>();

        if (dataSnapshot.getValue() != null) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {

                Quiz quiz = child.getValue(Quiz.class);
                quiz.uid = child.getKey();
                quiz.subject = subject;

                list.add(quiz);
            }
        }
        return list;
    }

    public static String checkIfStudentGradeExistInQuiz(DataSnapshot dataSnapshot) {

        if (dataSnapshot.getValue() != null) {
            Object grade = dataSnapshot.child("grade").getValue();
            if (grade != null)
                return grade.toString();
            else
                return null;
        }
        return null;
    }

    //method to calculate the height of listview inside scrollview to show full list without need scrolling
    public static void getTotalHeightofListView(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);

            if (listItem != null) {
                // This next line is needed before you call measure or else you won't get measured height at all. The listitem needs to be drawn first to know the height.
                listItem.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += listItem.getMeasuredHeight();
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    //method to calculate the height of listview inside scrollview to show full list without need scrolling
    public static void getTotalHeightofListView2(ListView listView) {

        ListAdapter mAdapter = listView.getAdapter();

        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);

            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            totalHeight += mView.getMeasuredHeight();

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static ArrayList<String> getFollowedSubjects(DataSnapshot dataSnapshot) {
        ArrayList<String> list = new ArrayList<>();

        if (dataSnapshot.getValue() != null) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {

                Object subject = child.child("subject").getValue();
                if (subject != null)
                    list.add(subject.toString());
            }
        }
        return list;
    }

    public static ArrayList<String> getAllLevels() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Level 1");
        list.add("Level 2");
        list.add("Level 3");
        list.add("Level 4");
        return list;
    }

    public static ArrayList<String> getAllSemesters() {
        ArrayList<String> list = new ArrayList<>();
        list.add("First Semester");
        list.add("Second Semester");
        return list;
    }
}