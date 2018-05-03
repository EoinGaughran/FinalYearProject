package com.rockfield.gmit.projectappfinal;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Calendar;

public class AlarmFragment extends Fragment implements View.OnClickListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Button mSetAlarm;
    private Button mNotificationExample;

    private static final String TAG = "AlarmActivity";

    private int hour, minute, day, month, year;
    private int alarmHour, alarmMinute, alarmDay, alarmMonth, alarmYear;

    public AlarmFragment() {
        // Required empty public constructor
    }

    public static AlarmFragment newInstance(String param1, String param2) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        mSetAlarm = view.findViewById(R.id.setAlarm);
        mNotificationExample = view.findViewById(R.id.notificationExample);

        mSetAlarm.setOnClickListener(this);
        mNotificationExample.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View view) {

        Intent intentNotif = new Intent(getActivity(), MainActivity.class);
        intentNotif.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 123, intentNotif, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.medicineiconwhite)
                .setContentTitle("Medication Alert")
                .setContentText("It is time for you to take your medicine.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        switch (view.getId()) {
            case R.id.setAlarm:

                getDate();
                Log.i(TAG, "onClick:" + view.getId());
                break;

            case R.id.notificationExample:

                notificationManager.notify(123, mBuilder.build());
                Log.i(TAG, "onClick:" + view.getId());
                break;
        }
    }

    private void getDate(){

        final Calendar myCalender = Calendar.getInstance();
        year = myCalender.get(Calendar.YEAR);
        month = myCalender.get(Calendar.MONTH);
        day = myCalender.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                if (view.isShown()) {

                    alarmYear = year;
                    alarmMonth = month;
                    alarmDay = day;
                    getTime();
                }
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), myDateListener, year, month, day);
        datePickerDialog.setTitle("Choose date:");
        datePickerDialog.show();
    }

    private void getTime(){

        final Calendar myCalender = Calendar.getInstance();
        hour = myCalender.get(Calendar.HOUR_OF_DAY);
        minute = myCalender.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {

                    alarmHour = hourOfDay;
                    alarmMinute = minute;
                    String alarm = alarmHour+":"+alarmMinute+" "+alarmDay+"/"+(alarmMonth+1)+"/"+alarmYear;
                    Toast.makeText(getActivity(), "Alarm set for "+alarm, Toast.LENGTH_SHORT).show();

                    scheduleNotification(getNotification(alarm), 10000);
                }
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, hour, minute, true);
        timePickerDialog.setTitle("Choose hour:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(getActivity(), Alarm.class);
        notificationIntent.putExtra(Alarm.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(Alarm.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        Intent intentNotif = new Intent(getActivity(), MainActivity.class);
        intentNotif.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intentNotif, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_nfc)
                .setContentTitle("Testing Notification")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        return mBuilder.build();
    }
}
