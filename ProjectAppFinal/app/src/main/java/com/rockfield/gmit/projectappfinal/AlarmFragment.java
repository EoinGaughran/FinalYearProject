package com.rockfield.gmit.projectappfinal;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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


/**
 * A simple {@link //Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link //AlarmFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link// AlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button mSetAlarm;

    private static final String TAG = "AlarmActivity";

    private int hour, minute, day, month, year;
    private int alarmHour, alarmMinute, alarmDay, alarmMonth, alarmYear;

    //private OnFragmentInteractionListener mListener;

    public AlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlarmFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        mSetAlarm.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View view) {

        /*Intent intentNotif = new Intent(getActivity(), MainActivity.class);
        intentNotif.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 123, intentNotif, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_nfc)
                .setContentTitle("Testing Notification")
                .setContentText("Test test")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);*/

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        switch (view.getId()) {
            case R.id.setAlarm:

                //Intent intent = new Intent(getActivity(), SetAlarm.class);
                //startActivity(intent);
                Log.i(TAG, "onClick:" + view.getId());

                getDate();

                // Create an explicit intent for an Activity in your app

                // notificationId is a unique int for each notification that you must define
                //notificationManager.notify(123, mBuilder.build());
                break;
            /*case R.id.pastSevenDays:

                startActivity(intent);
                Log.i("HistoryFragment", "onClick:" + view.getId());
                break;*/
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
                    //myCalender.set(Calendar.YEAR, year);
                    //myCalender.set(Calendar.MONTH, month);
                    //myCalender.set(Calendar.DAY_OF_MONTH, day);
                    alarmYear = year;
                    alarmMonth = month;
                    alarmDay = day;
                    getTime();
                }
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), myDateListener, year, month, day);
        datePickerDialog.setTitle("Choose date:");
        //datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
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
                    //myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                   // myCalender.set(Calendar.MINUTE, minute);
                    alarmHour = hourOfDay;
                    alarmMinute = minute;
                    String alarm = alarmHour+":"+alarmMinute+" "+alarmDay+"/"+(alarmMonth+1)+"/"+alarmYear;
                    Toast.makeText(getActivity(), alarm, Toast.LENGTH_SHORT).show();

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
        /*Intent intentNotif = new Intent(getActivity(), MainActivity.class);
        intentNotif.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intentNotif, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_nfc)
                .setContentTitle("Testing Notification")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);*/

        Notification.Builder builder = new Notification.Builder(getActivity());
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_nfc);
        return builder.build();
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
