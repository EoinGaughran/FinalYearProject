package com.example.pcworld.appuserui;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Listener{

    private NFCReadFragment mNfcReadFragment;

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "UserDatabase.db";
    public static String nfcTagInfo = "noDataRead";
    public static String SQL_READ = "SQLREAD";

    private static final String TAG = "Main Activity";

    private boolean isDialogDisplayed = false;
    private boolean isUserLoggedIn = false;
    //private boolean isWrite = false;

    private NfcAdapter mNfcAdapter;

    private TransferUtility transferUtility;

    public FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(MainActivity.this);

    //private File infoFile = new File(getFilesDir() + Constants.USER_INFO_FILE);
    //private File dataFile = new File(getFilesDir() + Constants.USER_DATA_FILE);

    //SQLiteDatabase mydatabase = openOrCreateDatabase("TestDatabase",MODE_PRIVATE,null);

    //mydatabase.execSQL("CREATE TABLE IF NOT EXISTS TutorialsPoint(Username VARCHAR,Password VARCHAR);");
    //mydatabase.execSQL("INSERT INTO TutorialsPoint VALUES('admin','admin');");

    private static final int AWS_LIST_RESULT = 1;
    private static final int SIGN_IN_SUCCESSFUYL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup amazon s3 link
        transferUtility = Util.getTransferUtility(this);

        checkLogin();
        initNFC();
        initUI();
    }

    private void checkLogin() {

        if (!isUserLoggedIn()) {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, SIGN_IN_SUCCESSFUYL);

            //download the users database
        }

        /*GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        graph.addSeries(series);*/


        /*DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        new cloudDownload().execute(Util.getClientUserName() + ".db");
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to sync your data online?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();*/


        //Setup SQL Library


        //DATABASE_NAME = Util.getClientUserName()+ ".db";
        //Log.i(TAG, "DatabaseName: " + DATABASE_NAME);
    }

    private void initNFC(){

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SqlReaderClass.databaseInput.TABLE_NAME + " (" +
                    SqlReaderClass.databaseInput._ID + " INTEGER PRIMARY KEY," +
                    SqlReaderClass.databaseInput.COLUMN_TIME + " TEXT," +
                    SqlReaderClass.databaseInput.COLUMN_NFCDATA + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SqlReaderClass.databaseInput.TABLE_NAME;



    public class FeedReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.


        public FeedReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    }

    private class cloudDownload extends AsyncTask<String, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this,
                    "Deleting",
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(String... keys) {

            File DATABASE_FILE = new File(getDatabasePath(DATABASE_NAME).toString());
            String s3FileKey = keys[0];
            Log.i("S3 FILE DOWNLOAD", "Current local database location" + getDatabasePath(DATABASE_NAME));
            Log.i("S3 FILE DOWNLOAD", "Local Directory: "+ DATABASE_FILE.toString());
            Log.i("S3 FILE DOWNLOAD", "S3 Key: "+ s3FileKey);
            TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, s3FileKey, DATABASE_FILE);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();

            Toast.makeText(MainActivity.this, "File Downloaded",
                    Toast.LENGTH_LONG).show();
        }
    }

    private class writeSQL extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            Log.i("SQLWRITE", "EXECUTING");
            dialog = ProgressDialog.show(MainActivity.this,
                    getString(R.string.refreshing),
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            // Gets the data repository in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();


            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(SqlReaderClass.databaseInput.COLUMN_TIME, Calendar.getInstance().getTime().toString());
            values.put(SqlReaderClass.databaseInput.COLUMN_NFCDATA, nfcTagInfo);

            nfcTagInfo = "noDataRead";

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(SqlReaderClass.databaseInput.TABLE_NAME, null, values);
            Log.i("SQLWRITE", "RowId: "+ newRowId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }
    }

    private class readSQL extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            Log.i("SQLREAD", "EXECUTING");
            dialog = ProgressDialog.show(MainActivity.this,
                    "Reading",
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... inputs) {

            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            StringBuilder tempSQL = new StringBuilder();
            SQL_READ = "";

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    //BaseColumns._ID,
                    //SqlReaderClass.databaseInput.COLUMN_TIME,
                    SqlReaderClass.databaseInput.COLUMN_NFCDATA
            };

            // Filter results WHERE "title" = 'My Title'

            //String selection = SqlReaderClass.databaseInput.COLUMN_TIME + " = ?";
            //String[] selectionArgs = { "World" };

            String selection = SqlReaderClass.databaseInput.COLUMN_TIME + " = ?";
            String[] selectionArgs = { "World" };

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    SqlReaderClass.databaseInput.COLUMN_NFCDATA + " DESC";

            /*Cursor cursor1 = db.query(
                    SqlReaderClass.databaseInput.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );*/

            String table = SqlReaderClass.databaseInput.TABLE_NAME;
            String nfcColumn = SqlReaderClass.databaseInput.COLUMN_NFCDATA;
            String timeColumn = SqlReaderClass.databaseInput.COLUMN_TIME;
            String query = "SELECT "+nfcColumn+", "+timeColumn+" FROM " + table;
            Log.i(TAG, "SQL QUERY:" + query);

            Cursor cursor = db.rawQuery(query, null);

            List itemIds = new ArrayList<>();
            List timeList = new ArrayList<String>();
            List titleNames = new ArrayList<String>();
            while(cursor.moveToNext()) {
                //long itemId = cursor.getLong(
                        //cursor.getColumnIndexOrThrow(SqlReaderClass.databaseInput._ID));
                //itemIds.add(itemId);
                titleNames.add(cursor.getString(cursor.getColumnIndexOrThrow(SqlReaderClass.databaseInput.COLUMN_NFCDATA)));
                timeList.add(cursor.getString(cursor.getColumnIndexOrThrow(SqlReaderClass.databaseInput.COLUMN_TIME)));
            }
            //String dsdsd[] = cursor.getColumnNames();
            cursor.close();
            Log.i("MainAcitvity", "Value: " + titleNames.size());
            for(int i = 0; i < titleNames.size(); i++) {

                Log.i("MainAcitvity", "Time: " + timeList.get(i));
                Log.i("MainAcitvity", "Value: " + titleNames.get(i));
                tempSQL.append("NFCDATA: " + titleNames.get(i)+"\r\n");
                tempSQL.append("Time: " + timeList.get(i)+"\r\n");
            }

            SQL_READ = tempSQL.toString();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }
    }

    public void fileUpload(File uploadFile){
        //UPLOAD DATABASE

        TransferObserver uploadObserver =
                transferUtility.upload(Constants.BUCKET_NAME,
                        "UserData/"+Util.getClientUserName()+".db",
                        uploadFile);

        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    Toast.makeText(MainActivity.this, "Database Sync Complete",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("MainActivity", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Toast.makeText(MainActivity.this, "upload didnt work",
                        Toast.LENGTH_LONG).show();
            }

        });

        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }
    }

    public boolean isUserLoggedIn() {

        SharedPreferences prefs = getSharedPreferences("LoginDetails", MODE_PRIVATE);

        if (prefs.getBoolean("logged_in", true)) { //user logged in before
            Util.setClientUserName(prefs.getString("username", "NoUsername"));
            Util.setUserPassword(prefs.getString("password", "NoPassword"));
            return true;

        } else {
            return false;
        }
    }

    /*public void initData(){

        if(!infoFile.exists()) {
            firstTimeSetup();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        else {
            readFile(infoFile);
        }

        if(!dataFile.exists()){

            String dataFileFormat = "data";

            createFile(dataFile.toString(), dataFileFormat);
            try{
                String path = getFilesDir() + Constants.USER_DATA_FILE;
                //beginUpload(path);
            }
            catch(Exception e){
                Toast.makeText(MainActivity.this, "didnt work",
                        Toast.LENGTH_LONG).show();
            }
        }
    }*/

    public void initUI(){

        Button testButton = (Button) findViewById(R.id.testButton);
        Button awsButton = (Button) findViewById(R.id.awsButton);
        Button sqlButton = (Button) findViewById(R.id.sqlButton);
        Button NFCButton = (Button) findViewById(R.id.nfcButton);
        Button logoutButton = (Button) findViewById(R.id.logOutButton);
        Button sqlDisplayButton = (Button) findViewById(R.id.displaySql);
        Button uploadDatabase = (Button) findViewById(R.id.uploadDatabase);
        Button graphTest = (Button) findViewById(R.id.graphTest);
        TextView clientInfo = (TextView) findViewById(R.id.clientInfo);

        clientInfo.setText("User: "+ Util.getClientUserName());

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        awsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(MainActivity.this, AwsListActivity.class);
                startActivityForResult(intent, AWS_LIST_RESULT);
            }
        });

        sqlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                new writeSQL().execute();
                new readSQL().execute();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                SharedPreferences prefs = getSharedPreferences("LoginDetails", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged_in", false);
                editor.apply();

                mDbHelper.close();
                db.close();
                deleteDatabase(DATABASE_NAME);
                //getDatabasePath(DATABASE_NAME).delete();

                checkLogin();
            }
        });

        sqlDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                new readSQL().execute();
                Intent intent = new Intent(MainActivity.this, DisplaySqlData.class);
                startActivity(intent);
            }
        });

        uploadDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                fileUpload(getDatabasePath(DATABASE_NAME));
            }
        });

        graphTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(MainActivity.this, GraphTest.class);
                startActivity(intent);
            }
        });

        NFCButton.setOnClickListener(view -> showReadFragment());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == AWS_LIST_RESULT && data != null){
            if(resultCode == RESULT_OK){

                String dataString = data.getStringExtra("key");
                Log.i(TAG, "Context = " + dataString);
                Log.i(TAG, "Password = " + Util.getUserPassword());
                Log.i(TAG, "Username = " + Util.getClientUserName());

                String key = data.getStringExtra("key");
                new cloudDownload().execute(key);
            }
        }

        if(requestCode == SIGN_IN_SUCCESSFUYL && data != null){
            if(resultCode == RESULT_OK){

                //refresh
                initUI();

                //setup new sql library
                FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(MainActivity.this);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                //retrieve username
                String key = data.getStringExtra("key");

                //download users existing data from the cloud if it exists
                new cloudDownload().execute(key+".db");
            }
        }
    }

    private void showReadFragment() {

        mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment.newInstance();
        }
        mNfcReadFragment.show(getFragmentManager(),NFCReadFragment.TAG);

    }

    @Override
    public void onDialogDisplayed() {

        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {

        isDialogDisplayed = false;
        //isWrite = false;
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

            if (isDialogDisplayed) {

                /*if (isWrite) {

                    String messageToWrite = mEtMessage.getText().toString();
                    mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
                    mNfcWriteFragment.onNfcDetected(ndef,messageToWrite);

                } else {*/

                    mNfcReadFragment = (NFCReadFragment)getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                //}
            }
        }
    }

    /*@Override
    protected void onResume(){
        this.onResume();

        initUI();
    }*/

    private void firstTimeSetup(){

        String infoFileFormat = "**This file is for saving the users info\r\n" +
                "**Do not change anything in the file\r\n" +
                "**Username\r\n" +
                "\"usernameUndefined\"\r\n" +
                "**Password\r\n" +
                "\"passwordUndefined\"";

        //createFile(infoFile.toString(), infoFileFormat);
    }

    private void createFile(String filename, String fileContents){

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    MainActivity.this,
                    "didnt work",
                    Toast.LENGTH_SHORT).show();
        }

    }
    private String readFile(File fileToRead){

        String contents = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileToRead));
            String line;


            //fileView.setText("");

            /*while ((line = br.readLine()) != null) {
                //fileView.append(line);
                //fileView.append("\n");
            }*/


            br.close();
        } catch (FileNotFoundException ex) {
            Toast.makeText(
                    MainActivity.this,
                    "Unable to open file '" +
                            fileToRead.toString() + "'",
                    Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Toast.makeText(
                    MainActivity.this,
                    "Error reading file '"
                            + fileToRead.toString() + "'",
                    Toast.LENGTH_SHORT).show();
            // Or we could just do this:
            // ex.printStackTrace();
        }

        return contents;

        /*try {
            Files.readAllLines(Paths.get(infoFile.toString());
        }
        catch (IOException e){
            Log.e("Main", "readAllLines Error: " + e);
        }*/
    }
}