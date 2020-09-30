package com.example.notetakingapp;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ras on 8/7/2017.
 */
public class TimerPickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private Calendar cal;
    private int hour;
    private int minute;
    private ArrayList<PendingIntent> intentArrayList = new ArrayList<>();
    private final static HashMap alarm = new HashMap();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        int year = getArguments().getInt("year", 0);
        int month = getArguments().getInt("month", 0);
        int dayOfMonth = getArguments().getInt("dayOfMonth", 0);
        String notes = getArguments().getString("notes");
        int pos = getArguments().getInt("pos");
        cal.set(year, month, dayOfMonth, hourOfDay, minute);
        setAlarm(notes, getContext(), cal);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setAlarm(String reminder, Context context, android.icu.util.Calendar cal) {
        Calendar calendar = Calendar.getInstance();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationIntentService.class);
        intent.putExtra("item", reminder);
        int random = (int) System.currentTimeMillis();
        if (calendar.getTimeInMillis() <= cal.getTimeInMillis()) {
            PendingIntent pendingIntent = PendingIntent.getService(context, random, intent, 0);
            intentArrayList.add(pendingIntent);
            if (alarm.containsValue(reminder)) {
                Toast.makeText(context, R.string.alarm_already_set, Toast.LENGTH_SHORT).show();
            } else {
                alarm.put(cal.getTimeInMillis(), reminder);
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            }
        } else {
            Toast.makeText(context, R.string.invalid_alarm_time, Toast.LENGTH_SHORT).show();
        }
    }
}




