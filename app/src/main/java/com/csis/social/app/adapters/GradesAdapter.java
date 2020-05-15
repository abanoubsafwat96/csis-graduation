package com.csis.social.app.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.csis.social.app.R;

import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class GradesAdapter extends RecyclerView.Adapter<GradesAdapter.ViewHolder> {

    Context context;
    ArrayList<String>  usernames_list,grades_list;

    public GradesAdapter(Context context, ArrayList<String> usernames_list,ArrayList<String> grades_list) {
        this.context = context;
        this.usernames_list = usernames_list;
        this.grades_list = grades_list;
    }

    @NonNull
    @Override
    public GradesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grades_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final GradesAdapter.ViewHolder holder, int position) {

        holder.username.setText(usernames_list.get(position));
        holder.grade.setText(grades_list.get(position));
    }

    @Override
    public int getItemCount() {
        return usernames_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView username,grade;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            grade = itemView.findViewById(R.id.grade);

        }
    }
}
