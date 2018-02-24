package com.example.pcworld.filetransferapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
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
import android.widget.*;
import com.amazonaws.mobileconnectors.s3.transferutility.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileSync extends AppCompatActivity {

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
        transferUtility = com.example.eoin.testawsapp.Util.getTransferUtility(this);
        initUI();
    }

    /**
     * Gets all relevant transfers from the Transfer Service for populating the
     * UI
     */

    private void initUI() {
        try{
        String path = getFilesDir() + "/AWSTEST";
        beginUpload(path);
        }
        catch(Exception e){
            Toast.makeText(this, "didnt work",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            //Uri uri = data.getData();

            String path = ("test.txt");
            beginUpload(path);
            //Toast.makeText(this,
             //       "Unable to get the file from the given URI.  See error log for details",
            //        Toast.LENGTH_LONG).show();
            //Log.e(TAG, "Unable to upload file from the given uri", e);

        }
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private void beginUpload(String filePath) {
        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        //File file = new File(filePath);
        File file = new File(getFilesDir()+ "/AWSTEST");
        TransferObserver observer = transferUtility.upload(com.example.eoin.testawsapp.Constants.BUCKET_NAME, file.getName(),
                file);
        Toast.makeText(this, getFilesDir()+ "/AWSTEST",
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
