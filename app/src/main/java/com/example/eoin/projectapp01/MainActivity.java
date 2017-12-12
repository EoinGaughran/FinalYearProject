package com.example.eoin.projectapp01;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.service.autofill.Dataset;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button upload;
    //private Button btnUpload;

    //TextView tv = (TextView) findViewById(R.id.textVew);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start();
    }

    private void start() {

        upload=(Button)findViewById(R.id.upload);


        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "eu-west-1:0e854cdc-c925-462a-a214-d58d9f16cdc3", // Identity pool ID
                Regions.EU_WEST_1 // Region
        );

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

        final TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        // Initialize the Cognito Sync client
        /*CognitoSyncManager syncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.EU_WEST_1, // Region
                credentialsProvider);*/

        // Create an S3 client

        //btnUpload = (Button) findViewById(R.id.buttonUploadMain);

        upload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
               Random r = new Random();
                int i1 = r.nextInt(999999 - 1) + 1;


                    File file = new File("/storage/emulated/0/hello.docx");

                    transferUtility.upload(
                            userInfo.BUCKET_NAME,     /* The bucket to upload to */
                            "hello.docx",    /* The key for the uploaded object */
                            file /* The file where the data to upload exists */
                    );



                    //tv.setText("Fle Uploaded");

            }
        });


        // Create a record in a dataset and synchronize with the server
        /*Dataset dataset = syncClient.openOrCreateDataset("myDataset");
        dataset.put("myKey", "myValue");
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List newRecords) {
                //Your handler code here
            }
        });*/
        }

}
