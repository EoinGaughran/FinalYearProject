package com.example.pcworld.appuserui;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button testButton;
    private Button awsButton;
    private Button SqlButton;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "UserDatabase.db";

    private static final String TAG = "Main Activity";

    private TransferUtility transferUtility;

    //private File infoFile = new File(getFilesDir() + Constants.USER_INFO_FILE);
    //private File dataFile = new File(getFilesDir() + Constants.USER_DATA_FILE);

    //SQLiteDatabase mydatabase = openOrCreateDatabase("TestDatabase",MODE_PRIVATE,null);

    private FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(MainActivity.this);

    SQLiteDatabase db;

    //mydatabase.execSQL("CREATE TABLE IF NOT EXISTS TutorialsPoint(Username VARCHAR,Password VARCHAR);");
    //mydatabase.execSQL("INSERT INTO TutorialsPoint VALUES('admin','admin');");

    private static final int AWS_LIST_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //new FeedReaderDbHelper();
        //initData();
        transferUtility = Util.getTransferUtility(this);
        new testSQL().execute();
        initUI();
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SqlReaderClass.databaseInput.TABLE_NAME + " (" +
                    SqlReaderClass.databaseInput._ID + " INTEGER PRIMARY KEY," +
                    SqlReaderClass.databaseInput.COLUMN_NAME_TITLE + " TEXT," +
                    SqlReaderClass.databaseInput.COLUMN_NAME_SUBTITLE + " TEXT)";

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

    private class testSQL extends AsyncTask<Void, Void, Void> {

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
            db = mDbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(SqlReaderClass.databaseInput.COLUMN_NAME_TITLE, "World");
            values.put(SqlReaderClass.databaseInput.COLUMN_NAME_SUBTITLE, "Hello");

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

    private class testReadSQL extends AsyncTask<Void, Void, Void> {

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

            db = mDbHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    BaseColumns._ID,
                    SqlReaderClass.databaseInput.COLUMN_NAME_TITLE,
                    SqlReaderClass.databaseInput.COLUMN_NAME_SUBTITLE
            };

            // Filter results WHERE "title" = 'My Title'
            //String selection = SqlReaderClass.FeedEntry.COLUMN_NAME_TITLE;
            String selection = SqlReaderClass.databaseInput.COLUMN_NAME_TITLE + " = ?";
            String[] selectionArgs = { "World" };

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    SqlReaderClass.databaseInput.COLUMN_NAME_SUBTITLE + " DESC";

            Cursor cursor = db.query(
                    SqlReaderClass.databaseInput.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            List itemIds = new ArrayList<>();
            List titleNames = new ArrayList<String>();
            while(cursor.moveToNext()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(SqlReaderClass.databaseInput._ID));
                itemIds.add(itemId);
                titleNames.add(cursor.getString(cursor.getColumnIndexOrThrow(SqlReaderClass.databaseInput.COLUMN_NAME_TITLE)));
            }
            //String dsdsd[] = cursor.getColumnNames();
            cursor.close();
            Log.i("MainAcitvity", "Value: " + titleNames.size());
            for(int i = 0; i < titleNames.size(); i++)
                Log.i("MainAcitvity", "Value: " + titleNames.get(i));

            fileUpload(getDatabasePath(DATABASE_NAME));

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
                        uploadFile.getName(),
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

        testButton = (Button) findViewById(R.id.testButton);
        awsButton = (Button) findViewById(R.id.awsButton);
        SqlButton = (Button) findViewById(R.id.sqlButton);

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

        SqlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                new testReadSQL().execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == AWS_LIST_RESULT && data != null){
            if(resultCode == RESULT_OK){

                String dataString = data.getStringExtra("key");
                Log.i(TAG, "Context = " + dataString);
                Log.i(TAG, "Password = " + Util.getUserPassword());
                Log.i(TAG, "Username = " + Util.getClientUserName());
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