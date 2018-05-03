package com.rockfield.gmit.projectappfinal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.google.android.gms.plus.PlusOneButton;

import java.io.File;

public class dataOptionsFragment extends Fragment implements View.OnClickListener{

    private TransferUtility transferUtility;
    private static AmazonS3Client sS3Client;

    private static final String NFC_DATABASE_S3_KEY = "ServerData/NfcData.db";

    private static final String ARG_FILEPATH= "filePath";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_S3USERKEY = "s3UserKey";
    private static final String ARG_NFCDATABASEPATH = "nfcDatabasePath";

    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;

    private static final String TAG = "dataOptionsFragment";

    // The URL to +1.  Must be a valid URL.
    private final String PLUS_ONE_URL = "http://developer.android.com";

    private String mDataBaseFilePath;
    private String mUsername;
    private String mUserDatabaseS3Key;
    private String mNfcDatabasePath;

    private PlusOneButton mPlusOneButton;
    private Button mSyncDatabase;
    private Button mDeleteDatabase;
    private Button mUpdateNfcDatabase;

    public dataOptionsFragment() {
        // Required empty public constructor
    }

    public static dataOptionsFragment newInstance(String filePath, String username, String s3UserKey, String nfcDatabasePath) {
        dataOptionsFragment fragment = new dataOptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILEPATH, filePath);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_S3USERKEY, s3UserKey);
        args.putString(ARG_NFCDATABASEPATH, nfcDatabasePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDataBaseFilePath = getArguments().getString(ARG_FILEPATH);
            mUsername = getArguments().getString(ARG_USERNAME);
            mUserDatabaseS3Key = getArguments().getString(ARG_S3USERKEY);
            mNfcDatabasePath = getArguments().getString(ARG_NFCDATABASEPATH);
        }

        transferUtility = Util.getTransferUtility(getActivity());
        sS3Client = Util.getS3Client(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_options, container, false);

        mSyncDatabase = view.findViewById(R.id.uploadDatabase);
        mDeleteDatabase = view.findViewById(R.id.deleteOnlineData);
        mUpdateNfcDatabase = view.findViewById(R.id.updateNfcDatabase);

        mSyncDatabase.setOnClickListener(this);
        mDeleteDatabase.setOnClickListener(this);
        mUpdateNfcDatabase.setOnClickListener(this);

        mPlusOneButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the state of the +1 button each time the activity receives focus.
        mPlusOneButton.initialize(PLUS_ONE_URL, PLUS_ONE_REQUEST_CODE);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.uploadDatabase:

                if(!isNetworkAvailable()) {

                    Toast.makeText(getActivity(),
                            "Please enable your internet connection", Toast.LENGTH_SHORT).show();
                }
                else new uploadDatabaseTask().execute();

                Log.i(TAG, "onClick:" + view.getId());

                break;
            case R.id.deleteOnlineData:


                if(!isNetworkAvailable()) {

                    Toast.makeText(getActivity(),
                            "Please enable your internet connection", Toast.LENGTH_SHORT).show();
                }
                else deleteDatabase();

                Log.i(TAG, "onClick:" + view.getId());

                break;
            case R.id.updateNfcDatabase:

                if(!isNetworkAvailable()) {

                    Toast.makeText(getActivity(),
                            "Please enable your internet connection", Toast.LENGTH_SHORT).show();
                }
                else new updateNfcDatabaseTask().execute();

                Log.i(TAG, "onClick:" + view.getId());

                break;
        }
    }

    private void deleteDatabase(){

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        new deleteDatabaseTask().execute();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        //Do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private class uploadDatabaseTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;
        private File DATABASE_FILE = new File(mDataBaseFilePath);
        private String s3FileKey = "UserData/"+ mUsername + ".db";
        private boolean fileDoesntExist = true;

        @Override
        protected void onPreExecute() {

            dialog = ProgressDialog.show(getActivity(),
                    "Uploading",
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... keys) {

            if(!DATABASE_FILE.exists()){

                Log.i(TAG, "Error, no database");
                dialog.dismiss();
                fileDoesntExist = true;
            }
            else {
                fileDoesntExist = false;
                TransferObserver uploadObserver =
                        transferUtility.upload(Constants.BUCKET_NAME,
                                s3FileKey,
                                DATABASE_FILE);

                uploadObserver.setTransferListener(new TransferListener() {

                    private static final String TAG = "uploadDatabase";

                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (TransferState.COMPLETED == state) {

                            Log.i(TAG, "Upload Successful");

                            Toast.makeText(getActivity(), "Database Sync Successful",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                        int percentDone = (int) percentDonef;

                        if (percentDone == 100) dialog.dismiss();
                        Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.i(TAG, "Upload Failed");

                        Toast.makeText(getActivity(), "DOWNLOAD FAILED: Check your internet connection",
                                Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }

                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if(fileDoesntExist)
                Toast.makeText(getActivity(), "Your database is empty",
                        Toast.LENGTH_LONG).show();
        }
    }

    private class deleteDatabaseTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            dialog = ProgressDialog.show(getActivity(),
                    "Deleting",
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... keys) {

            sS3Client.deleteObject(new DeleteObjectRequest(Constants.BUCKET_NAME, mUserDatabaseS3Key));

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            dialog.dismiss();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class updateNfcDatabaseTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            dialog = ProgressDialog.show(getActivity(),
                    "Updating",
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... keys) {

            dialog.show();

            File DATABASE_FILE = new File(mNfcDatabasePath);
            Log.i("S3 DATA FILE DOWNLOAD", "Local Directory: " + DATABASE_FILE);
            Log.i("S3 DATA FILE DOWNLOAD", "S3 Key: " + NFC_DATABASE_S3_KEY);
            TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, NFC_DATABASE_S3_KEY, DATABASE_FILE);

            observer.setTransferListener(new TransferListener() {

                private static final String TAG = "DownloadNfcDatabase";

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {

                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int) percentDonef;

                    Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");

                    if(percentDone == 100) {

                        dialog.dismiss();
                        Toast.makeText(getActivity(),
                                "Update Successful", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.i(TAG, "Download Failed");
                    Toast.makeText(getActivity(), "DOWNLOAD FAILED: Check your internet connection",
                            Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }
}
