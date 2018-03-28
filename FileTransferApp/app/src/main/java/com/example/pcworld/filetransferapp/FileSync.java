package com.example.pcworld.filetransferapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.widget.*;
import com.amazonaws.mobileconnectors.s3.transferutility.*;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileSync extends AppCompatActivity {

    private Button createSync;
    private Button clear;

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    /**
     * This map is used to provide data to the SimpleAdapter above. See the
     * fillMap() function for how it relates observers to rows in the displayed
     * activity.
     */

    // Which row in the UI is currently checked (if any)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sync);

        // Initializes TransferUtility, always do this before using it.
        transferUtility = Util.getTransferUtility(this);
        initUI();
    }

    /**
     * Gets all relevant transfers from the Transfer Service for populating the
     * UI
     */

    private void initUI() {

        createSync= (Button) findViewById(R.id.createAndSync);
        clear = (Button) findViewById(R.id.textClear);

        final EditText message =  (EditText) findViewById(R.id.fileText);
        final EditText fileName = (EditText) findViewById(R.id.fileName);

        createSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                String filename = fileName.getText().toString();
                String fileContents = message.getText().toString();
                FileOutputStream outputStream;

                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(
                            FileSync.this,
                            "didnt work",
                            Toast.LENGTH_SHORT).show();
                }
                try{
                    String path = getFilesDir() + "/" + fileName.getText().toString();
                    beginUpload(path);
                    //uploadData(path);
                }
                catch(Exception e){
                    Toast.makeText(FileSync.this, "didnt work",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                message.setText("");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            //Uri uri = data.getData();

            String path = ("test.txt");
            //beginUpload(path);
            uploadData(path);
            //Toast.makeText(this,
             //       "Unable to get the file from the given URI.  See error log for details",
            //        Toast.LENGTH_LONG).show();
            //Log.e(TAG, "Unable to upload file from the given uri", e);

        }
    }

    /*
     * Begins to upload the file specified by the file path.
     *
     */

    public void uploadData(String path) {

        // Initialize AWSMobileClient if not initialized upon the app startup.
        // AWSMobileClient.getInstance().initialize(this).execute();

        File file = new File(getFilesDir()+ "/AWSTEST1");

        /*TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();*/

        TransferObserver uploadObserver =
                transferUtility.upload(
                        file.getName(),
                        file);

        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    Toast.makeText(FileSync.this, "shits done",
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
                Toast.makeText(FileSync.this, "upload didnt work",
                        Toast.LENGTH_LONG).show();
            }

        });

        // If your upload does not trigger the onStateChanged method inside your
        // TransferListener, you can directly check the transfer state as shown here.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }
    }
    private void beginUpload(String filePath) {
        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        //File file = new File(filePath);
        File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(com.example.pcworld.filetransferapp.Constants.BUCKET_NAME, "UserData/" + file.getName(),
                file);
        Toast.makeText(this, filePath + " Uploaded",
                Toast.LENGTH_LONG).show();
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new UploadListener());
    }

    /*
     * Gets the file path of the given Uri.
     */


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */

}
