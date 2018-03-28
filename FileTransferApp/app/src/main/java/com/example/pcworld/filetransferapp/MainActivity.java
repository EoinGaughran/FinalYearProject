package com.example.pcworld.filetransferapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;


import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button fileSync;
    private Button newUser;
    private Button deleteUser;
    private Button viewFiles;
    private Button localFiles;

    private static final int DOWNLOAD_SELECTION_REQUEST_CODE = 1;

    private AmazonS3Client s3;
    //private TransferUtility transferUtility

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {
        fileSync= (Button) findViewById(R.id.fileSync);
        newUser = (Button) findViewById(R.id.newFile);
        deleteUser = (Button) findViewById(R.id.deleteUser);
        viewFiles = (Button) findViewById(R.id.FileViewer);
        localFiles = (Button) findViewById(R.id.listFiles);

        //transferUtility = Util.getTransferUtility(this);
        s3 = com.example.pcworld.filetransferapp.Util.getS3Client(MainActivity.this);

        fileSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, FileSync.class);
                startActivity(intent);
            }
        });

        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, deleteUser.class);
                startActivityForResult(intent, DOWNLOAD_SELECTION_REQUEST_CODE);

                //DeleteObjectRequest(Constants.BUCKET_NAME, java.lang.String key)
                //TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, key, file);


            }
        });

        localFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, localFiles.class);
                startActivity(intent);
            }
        });

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                String filename = "User1";
                String fileContents = "4352354355";
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

                /*try {
                    PrintWriter writer = new PrintWriter("test.txt", "UTF-8");
                    writer.println("The first line");
                    writer.println("The second line");
                    writer.close();
                    Toast.makeText(
                            MainActivity.this,
                            "Created new user",
                            Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    Toast.makeText(
                            MainActivity.this,
                            "Error creating new user",
                            Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        viewFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                /*File skm = new File(getFilesDir() + "/User1");
                if (skm.exists())
                {
                    Toast.makeText(
                            MainActivity.this,
                            "its there",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(
                            MainActivity.this,
                            "not there",
                            Toast.LENGTH_SHORT).show();
                }*/
                Intent intent = new Intent(MainActivity.this, ViewFile.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == DOWNLOAD_SELECTION_REQUEST_CODE && data != null) {
            if(resultCode == RESULT_OK ) {

                String key = data.getStringExtra("key");
                new deleteObject().execute(key);
            }
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        initData();
    }

    private class deleteObject extends AsyncTask<String, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this,
                    "Deleting",
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(String... key) {
            // Queries files in the bucket from S3.
            s3.deleteObject(new DeleteObjectRequest(Constants.BUCKET_NAME, key[0]));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }
    }

    public void initData() {
    }
}
