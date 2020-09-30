package com.example.notetakingapp;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import android.widget.DatePicker;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

/**
 * Created by ras on 8/9/2017.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    private Calendar calendar;
    private  int year;
    private int month;
    private int day;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String notes = getArguments().getString("note");
        int pos = getArguments().getInt("position");
        DialogFragment datePickerFragment = new TimerPickerFragment();
        Bundle setYear = new Bundle();
        setYear.putInt("year",year);
        setYear.putInt("month",month);
        setYear.putInt("dayOfMonth",dayOfMonth);
        setYear.putString("notes",notes);
        setYear.putInt("pos",pos);
        datePickerFragment.setArguments(setYear);
        datePickerFragment.show(getFragmentManager(),"TimePicker");
    }
}
