package com.csis.social.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.csis.social.app.adapters.QuizzesAdapter;
import com.csis.social.app.models.Quiz;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class OldQuizzesFragment extends Fragment {

    private String subject;

    ListView listView;
    TextView noQuizzes;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private ArrayList<Quiz> quizzes_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_old_quizzes, container, false);

        subject = getArguments().getString("subject");

        listView = view.findViewById(R.id.listView);
        noQuizzes = view.findViewById(R.id.noQuizzes);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Quizzes").child(subject);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Quiz> reversed_quizzes_list = Utilities.gettAllQuizzes(dataSnapshot,subject);
                quizzes_list = reverseList(reversed_quizzes_list);
                fillListView(quizzes_list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), OldQuizzes2Activity.class);
                intent.putExtra("quiz", quizzes_list.get(i));
                intent.putParcelableArrayListExtra("questions_list", quizzes_list.get(i).questions_list);
                intent.putParcelableArrayListExtra("bonusQuestions_list", quizzes_list.get(i).bonusQuestions_list);
                startActivity(intent);
            }
        });

        return view;
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

            QuizzesAdapter adapter = new QuizzesAdapter(getContext(), quizzes_list, "OldQuizzes");
            listView.setAdapter(adapter);
        }
    }
}
