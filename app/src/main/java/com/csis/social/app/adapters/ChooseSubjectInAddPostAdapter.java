package com.csis.social.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.csis.social.app.R;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ChooseSubjectInAddPostAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> subjects_list;

    public ChooseSubjectInAddPostAdapter(Context context, ArrayList<String> subjects_list) {
        this.context = context;
        this.subjects_list = subjects_list;
    }

    @Override
    public int getCount() {
        return subjects_list.size();
    }

    @Override
    public Object getItem(int position) {
        return subjects_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.choose_subject_list_item, null);
        }

        ((TextView) view.findViewById(R.id.name)).setText(subjects_list.get(position));

        return view;
    }
}
