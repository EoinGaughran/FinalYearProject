package com.rockfield.gmit.projectappfinal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.*;
import android.app.PendingIntent;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity{

    private NfcAdapter mNfcAdapter;
    private ProgressBar nfcProgress;
    private static final int progr[]  = {30, 15, 20, 25, 20};

    private TransferUtility transferUtility;

    private Util.UserDataDbHelper mUserDataDbHelper = new Util.UserDataDbHelper(this);

    public static String nfcTagInfo = "noDataRead";

    private static final String TAG = "Main Activity";
    private static final int AWS_LIST_RESULT = 1;
    private static final int SIGN_IN_SUCCESSFUL = 2;
    private static final int NEW_ACCOUNT = 111;

    public static final String s3NfcDatabaseKey = "ServerData/NfcData.db";
    public static String s3UserDatabaseKey;
    //public static final String s3UserDatabaseKey = "UserData/" + Util.getClientUserName() + ".db";

    //public FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transferUtility = Util.getTransferUtility(this);

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

        s3UserDatabaseKey = "UserData/" + Util.getClientUserName() + ".db";
    }

    private boolean isUserLoggedIn() {

        SharedPreferences prefs = getSharedPreferences("LoginDetails", MODE_PRIVATE);

        if (prefs.getBoolean("logged_in", false)) { //user logged in before
            Util.setClientUserName(prefs.getString("username", "NoUsername"));
            //Util.setUserPassword(prefs.getString("password", "NoPassword"));
            return true;

        } else
            return false;
    }
    /*
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SqlLibraries.userLoginDatabase.TABLE_NAME + " (" +
                    SqlLibraries.userInfoDatabase._ID + " INTEGER PRIMARY KEY," +
                    SqlLibraries.userInfoDatabase.COLUMN_NFCDATA + " TEXT," +
                    SqlLibraries.userInfoDatabase.COLUMN_TIME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SqlLibraries.userLoginDatabase.TABLE_NAME;



    public static class FeedReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.


        public FeedReaderDbHelper(Context context) {
            super(context, Constants.USER_LOGIN_DATABASE, null, Constants.USER_LOGIN_DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
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

            if (requestCode == SIGN_IN_SUCCESSFUL) {

                //refresh views
                initViews();

                File NFC_DATABASE_FILE = new File(getDatabasePath(Constants.NFC_INFO_DATABASE).toString());
                Log.i("NFC DATA FILE DOWNLOAD", "Local Directory: "+ NFC_DATABASE_FILE);
                Log.i("NFC DATA FILE DOWNLOAD", "S3 Key: "+ s3NfcDatabaseKey);
                TransferObserver nfcDownloadObserver = transferUtility.download(Constants.BUCKET_NAME, s3NfcDatabaseKey, NFC_DATABASE_FILE);

                /*File USER_DATABASE_FILE = new File(getDatabasePath(Constants.USER_INFO_DATABASE).toString());
                Log.i("NFC DATA FILE DOWNLOAD", "Local Directory: "+ USER_DATABASE_FILE);
                Log.i("NFC DATA FILE DOWNLOAD", "S3 Key: "+ s3UserDatabaseKey);
                TransferObserver userDataDownloadObserver = transferUtility.download(Constants.BUCKET_NAME, s3UserDatabaseKey, USER_DATABASE_FILE);*/

                if (resultCode == RESULT_OK) {

                    //refresh
                    //initUI();

                    Log.i(TAG, "Login Succesful");
                    //setup new sql library
                    //FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(MainActivity.this);
                    //SQLiteDatabase db = mDbHelper.getWritableDatabase();

                    //retrieve username
                    String key = data.getStringExtra("key");

                    //download users existing data from the cloud if it exists
                    //new cloudDownload().execute(key+".db");

                    File USER_DATABASE_FILE = new File(getDatabasePath(Constants.USER_INFO_DATABASE).toString());
                    String s3FileKey = "UserData/"+key+".db";
                    Log.i("USER DATA FILE DOWNLOAD", "Local Directory: "+ USER_DATABASE_FILE);
                    Log.i("USER DATA FILE DOWNLOAD", "S3 Key: "+ s3FileKey);
                    TransferObserver userDownloadObserver = transferUtility.download(Constants.BUCKET_NAME, s3FileKey, USER_DATABASE_FILE);
                }
                else if(resultCode == NEW_ACCOUNT) {

                    //SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    Log.i(TAG, "New Accounted Logged In");
                }
            }
        }
        else
            Toast.makeText(this, "Login Info Failed", Toast.LENGTH_SHORT).show();
    }

    public void initViews() {

        /*mEtMessage = (EditText) findViewById(R.id.et_message);
        mBtWrite = (Button) findViewById(R.id.btn_write);
        mBtRead = (Button) findViewById(R.id.btn_read);

        mTvMessage = (TextView) view.findViewById(R.id.tv_message);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);*/

        String loginDisplay = "Username: " +Util.getClientUserName();

        TextView mLoginName = (TextView) findViewById(R.id.loginName);

        mLoginName.setText(loginDisplay);

        nfcProgress = (ProgressBar) findViewById(R.id.nfcProgressBar);
        nfcProgress.setVisibility(View.INVISIBLE);

        Button logoutButton = (Button) findViewById(R.id.logOutButton);
        Button menuButton = (Button) findViewById(R.id.menuButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                final File DATABASE_FILE = new File(getDatabasePath(Constants.USER_INFO_DATABASE).toString());

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:

                                if(isNetworkAvailable()){

                                    final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this,
                                            "Uploading",
                                            getString(R.string.please_wait));

                                    progressDialog.show();

                                    TransferObserver uploadObserver =
                                            transferUtility.upload(Constants.BUCKET_NAME,
                                                    s3UserDatabaseKey,
                                                    DATABASE_FILE);

                                    uploadObserver.setTransferListener(new TransferListener() {

                                        private static final String TAG = "logoutUploadDatabase";

                                        @Override
                                        public void onStateChanged(int id, TransferState state) {
                                            if (TransferState.COMPLETED == state) {

                                                Log.i(TAG, "Upload Successful");

                                                Toast.makeText(MainActivity.this, "Database Sync Successful",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                            float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                                            int percentDone = (int) percentDonef;

                                            Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
                                            if(percentDone == 100) {

                                                progressDialog.dismiss();
                                                logOut();
                                            }
                                        }

                                        @Override
                                        public void onError(int id, Exception ex) {
                                            Log.i(TAG, "Upload Failed");
                                        }

                                    });
                                }
                                else Toast.makeText(MainActivity.this, "Please enable your internet connection", Toast.LENGTH_SHORT).show();

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:

                                logOut();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Do you want to sync your database before logging out?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);

                //pass the directory of the userInfoDatabase to new intent
                intent.putExtra("path", getDatabasePath(Constants.USER_INFO_DATABASE).toString());
                intent.putExtra("nfcDatabasePath", getDatabasePath(Constants.NFC_INFO_DATABASE).toString());
                startActivity(intent);
                finish();
            }
        });
    }

    private void initNFC(){

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void logOut(){

        SharedPreferences prefs = getSharedPreferences("LoginDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("logged_in", false);
        editor.apply();

        mUserDataDbHelper.close();
        deleteDatabase(Constants.USER_INFO_DATABASE);

        checkLogin();
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

        //readFromNFC(ndef);

        new nfcReadTask(ndef).execute();
    }

    /*private void readFromNFC(Ndef ndef) {

        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            String message = new String(ndefMessage.getRecords()[0].getPayload());
            Log.d(TAG, "readFromNFC: "+message);

            SQLiteDatabase db = mUserDataDbHelper.getWritableDatabase();

            // Insert nfc data and time
            ContentValues values = new ContentValues();
            values.put(SqlLibraries.userInfoDatabase.COLUMN_NFCDATA, message);
            values.put(SqlLibraries.userInfoDatabase.COLUMN_TIME, Calendar.getInstance().getTime().toString());

            TextView nfcMessage = (TextView) findViewById(R.id.nfcResult);

            nfcMessage.setText(message);

            // Save the inserted data locally
            //long newRowId = db.insert(SqlLibraries.userLoginDatabase.TABLE_NAME, null, values);
            //Log.i("SQLWRITE", "RowId: "+ newRowId);

            //mTvMessage.setText(message);
            ndef.close();

        } catch (IOException | FormatException | NullPointerException e) {
            e.printStackTrace();

        }
    }*/

    private class nfcReadTask extends AsyncTask<Void, Integer, Void> {

        private ProgressDialog dialog;
        private Ndef mNdef;
        private int index;
        private String message = "NoNfcRead";

        nfcReadTask(Ndef ndef) {
            mNdef = ndef;
        }

        @Override
        protected void onPreExecute() {

            nfcProgress.setVisibility(View.VISIBLE);

            int max = 0;
            for (final int p : progr) {
                max += p;
            }
            nfcProgress.setMax(max);
            index = 0;

           // dialog = ProgressDialog.show(MainActivity.this,"Uploading",getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... keys) {

            try {

                publishProgress();

                mNdef.connect();

                publishProgress();

                NdefMessage ndefMessage = mNdef.getNdefMessage();

                String [] messageSplit = new String(ndefMessage.getRecords()[1].getPayload()).split("/");
                message = messageSplit[1];
                Log.d(TAG, "readFromNFC: "+message);

                publishProgress();

                SQLiteDatabase db = mUserDataDbHelper.getWritableDatabase();

                // Insert nfc data and time
                ContentValues values = new ContentValues();
                values.put(SqlLibraries.userInfoDatabase.COLUMN_NFCDATA, message);
                values.put(SqlLibraries.userInfoDatabase.COLUMN_TIME, Calendar.getInstance().getTime().toString());

                long newRowId = db.insert(SqlLibraries.userInfoDatabase.TABLE_NAME, null, values);

                Log.i("SQLWRITE", "RowId: "+ newRowId);

                publishProgress();

                mNdef.close();

                publishProgress();

            } catch (IOException | FormatException | NullPointerException e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            //textview.setText(titles[index]);
            nfcProgress.incrementProgressBy(progr[index]);
            ++index;
        }

        /*@Override
        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC",progress[0]);
            //Update Progress
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }*/

        @Override
        protected void onPostExecute(Void result) {

            TextView nfcMessage = (TextView) findViewById(R.id.nfcResult);

            String nfcDisplay = "NFC Read: " +message;

            nfcMessage.setText(nfcDisplay);

            nfcProgress.setVisibility(View.INVISIBLE);
            //parent.finish();
            //dialog.dismiss();
        }
    }
}