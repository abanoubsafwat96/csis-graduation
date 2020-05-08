package com.csis.social.app;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

class SubjectsFollowAdapter extends RecyclerView.Adapter<SubjectsFollowAdapter.ViewHolder> {

    Context context;
    ArrayList<String> allSubjects_list,followedSubjects_list;

    public SubjectsFollowAdapter(Context context, ArrayList<String> allSubjects_list, ArrayList<String> followedSubjects_list) {
        this.context = context;
        this.allSubjects_list = allSubjects_list;
        this.followedSubjects_list=followedSubjects_list;
    }

    @NonNull
    @Override
    public SubjectsFollowAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final SubjectsFollowAdapter.ViewHolder holder, int position) {
        String subjectName=allSubjects_list.get(position);

        holder.subjectName.setText(subjectName);

        if (subjectIsfollowed(subjectName))
            holder.mark.setImageResource(R.drawable.truemark);
        else
            holder.mark.setImageResource(R.drawable.mark);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClicked(holder);
            }
        });

        holder.mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClicked(holder);
            }
        });
    }

    private void onItemClicked(ViewHolder holder) {
        if (((BitmapDrawable) holder.mark.getDrawable()).getBitmap()
                .sameAs(((BitmapDrawable) context.getResources().getDrawable(R.drawable.mark)).getBitmap())) {

            followedSubjects_list.add(holder.subjectName.getText().toString());
            holder.mark.setImageResource(R.drawable.truemark);

        } else if (((BitmapDrawable) holder.mark.getDrawable()).getBitmap()
                .sameAs(((BitmapDrawable) context.getResources().getDrawable(R.drawable.truemark)).getBitmap())) {

            boolean res = followedSubjects_list.remove(holder.subjectName.getText().toString());
            if (res)
                holder.mark.setImageResource(R.drawable.mark);
        }
    }

    private boolean subjectIsfollowed(String subjectName) {
        for (int i=0;i<followedSubjects_list.size();i++){
            if (followedSubjects_list.get(i).equals(subjectName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return allSubjects_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardView;
        TextView subjectName;
        ImageView mark;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            subjectName = itemView.findViewById(R.id.subjectName);
            mark = itemView.findViewById(R.id.mark);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
