package com.rockfield.gmit.projectappfinal;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.*;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.IOException;

public class MainActivity extends AppCompatActivity{

    private NfcAdapter mNfcAdapter;

    public static String nfcTagInfo = "noDataRead";

    private static final String TAG = "Main Activity";
    private static final int AWS_LIST_RESULT = 1;
    private static final int SIGN_IN_SUCCESSFUL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLogin();
        initNFC();
        initViews();
    }

    private void checkLogin() {

        if (!isUserLoggedIn()) {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, SIGN_IN_SUCCESSFUL);

            //download the users database
        }
    }

    private boolean isUserLoggedIn() {

        SharedPreferences prefs = getSharedPreferences("LoginDetails", MODE_PRIVATE);

        if (prefs.getBoolean("logged_in", false)) { //user logged in before
            Util.setClientUserName(prefs.getString("username", "NoUsername"));
            Util.setUserPassword(prefs.getString("password", "NoPassword"));
            return true;

        } else
            return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        /*if(requestCode == AWS_LIST_RESULT && data != null){
            if(resultCode == RESULT_OK){

                String dataString = data.getStringExtra("key");
                Log.i(TAG, "Context = " + dataString);
                Log.i(TAG, "Password = " + Util.getUserPassword());
                Log.i(TAG, "Username = " + Util.getClientUserName());

                String key = data.getStringExtra("key");
                new cloudDownload().execute(key);
            }
        }*/

        if(requestCode == SIGN_IN_SUCCESSFUL && data != null){
            if(resultCode == RESULT_OK){

                //refresh
                //initUI();

                Log.i(TAG, "Login Succesful");
                //setup new sql library
                //FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(MainActivity.this);
                //SQLiteDatabase db = mDbHelper.getWritableDatabase();

                //retrieve username
                //String key = data.getStringExtra("key");

                //download users existing data from the cloud if it exists
                //new cloudDownload().execute(key+".db");
            }
        }
    }

    private void initViews() {

        /*mEtMessage = (EditText) findViewById(R.id.et_message);
        mBtWrite = (Button) findViewById(R.id.btn_write);
        mBtRead = (Button) findViewById(R.id.btn_read);

        mTvMessage = (TextView) view.findViewById(R.id.tv_message);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);*/

        Button logoutButton = (Button) findViewById(R.id.logOutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                SharedPreferences prefs = getSharedPreferences("LoginDetails", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged_in", false);
                editor.apply();

                checkLogin();
            }
        });
    }

    private void initNFC(){

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!= null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: "+intent.getAction());

        if(tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            onNfcDetected(ndef);
        }
    }

    public void onNfcDetected(Ndef ndef){

        readFromNFC(ndef);
    }

    private void readFromNFC(Ndef ndef) {

        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            String message = new String(ndefMessage.getRecords()[0].getPayload());
            Log.d(TAG, "readFromNFC: "+message);
            //mTvMessage.setText(message);
            ndef.close();

        } catch (IOException | FormatException | NullPointerException e) {
            e.printStackTrace();

        }
    }
}
