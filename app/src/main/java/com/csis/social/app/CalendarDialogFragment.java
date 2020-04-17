package com.csis.social.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.fragment.app.DialogFragment;

/**
 * Created by Abanoub on 2018-04-27.
 */

public class CalendarDialogFragment extends DialogFragment {

    TextView monthYear_textView;
    CompactCalendarView compactCalendarView;
    private SimpleDateFormat dateFormatForMonth;

    private static Communicator communicator;

    /**
     * Create a new instance of MyDialogFragment, to pass "newUser" as an argument to this dialog.
     * <p>
     * 1. newInstance to make instance of your DialogFragment
     * 2. setter to initialize your object
     * 3. and add setRetainInstance(true); in onCreate
     */
    static CalendarDialogFragment newInstance(Communicator targetCommunicator) {

        CalendarDialogFragment f = new CalendarDialogFragment();
        communicator = targetCommunicator;
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Create your dialog here

        setRetainInstance(true);

        Dialog dialog = new Dialog(getContext(), R.style.DialogTheme);
        dialog.setContentView(R.layout.calendar_dialog);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar_dialog, container, false);

//        communicator= (Communicator) getTargetFragment();

        monthYear_textView = view.findViewById(R.id.monthyear);
        compactCalendarView = view.findViewById(R.id.compactcalendar_view);

        dateFormatForMonth = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYear_textView.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        // Set first day of week to Monday, defaults to Monday so calling setFirstDayOfWeek is not necessary
        // Use constants provided by Java Calendar class
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        compactCalendarView.setUseThreeLetterAbbreviation(true);

        // define a listener to receive callbacks when certain events happen.
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {

            @Override
            public void onDayClick(Date dateClicked) {
                if (dateClicked.after(Calendar.getInstance().getTime())) {
                    communicator.onDeadlineChoosed(dateClicked);
                    dismiss();
                } else
                    Toast.makeText(getContext(), "Choose right date", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                monthYear_textView.setText(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });

        return view;
    }
}
