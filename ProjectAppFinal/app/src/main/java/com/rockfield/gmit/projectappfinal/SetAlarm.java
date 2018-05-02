package com.rockfield.gmit.projectappfinal;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class SetAlarm extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private DatePickerDialog mPickDate;
    private TimePickerDialog mPickTime;
    private Button mSetAlarmTime;
    private int hour, minute, day, month, year;
    //private

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        mSetAlarmTime = (Button) findViewById(R.id.createAlarm);

        //mPickDate = findViewById(R.id.datePicker);

        initUI();
    }

    public void initUI(){
        /*Date now = new Date();
        DateFormat df1 = DateFormat.getDateInstance(DateFormat.SHORT);
        String s1[] = df1.format(now).split("/");

        month = Integer.parseInt(s1[0]);
        day = Integer.parseInt(s1[1]);
        year = Integer.parseInt("20"+s1[2]);*/

        mSetAlarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate();
            }
        });
    }

    private void getDate(){

        final Calendar myCalender = Calendar.getInstance();
        day = myCalender.get(Calendar.DAY_OF_MONTH);
        month = myCalender.get(Calendar.MONTH);
        year = myCalender.get(Calendar.YEAR);

        final DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, this, year, month, day);

        datePickerDialog.show();

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Handle dismiss
                Toast.makeText(SetAlarm.this, "dismiss", Toast.LENGTH_SHORT).show();
                getTime();
            }
        });

        datePickerDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // Handle clicks
                Toast.makeText(SetAlarm.this, "onkey", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void getTime(){

        final Calendar myCalender = Calendar.getInstance();
        hour = myCalender.get(Calendar.HOUR_OF_DAY);
        minute = myCalender.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {
                    myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    myCalender.set(Calendar.MINUTE, minute);

                }
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, hour, minute, true);
        timePickerDialog.setTitle("Choose hour:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, Alarm.class);
        notificationIntent.putExtra(Alarm.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(Alarm.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        Intent intentNotif = new Intent(this, MainActivity.class);
        intentNotif.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 123, intentNotif, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_nfc)
                .setContentTitle("Testing Notification")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return mBuilder.build();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

    }

    /*Alarm alarm = new Alarm();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.setAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        alarm.setAlarm(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }*/
}
