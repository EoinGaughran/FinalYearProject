package com.rockfield.gmit.projectappfinal;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link //dataOptionsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link// dataOptionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class dataOptionsFragment extends Fragment implements View.OnClickListener{

    private TransferUtility transferUtility;
    private static AmazonS3Client sS3Client;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_FILEPATH= "filePath";
    private static final String ARG_USERNAME = "username";
    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;

    private static final String TAG = "dataOptionsFragment";
    // The URL to +1.  Must be a valid URL.
    private final String PLUS_ONE_URL = "http://developer.android.com";
    // TODO: Rename and change types of parameters
    private String mDataBaseFilePath;
    private String mUsername;
    private PlusOneButton mPlusOneButton;
    private Button mSyncDatabase;
    private Button mDeleteDatabase;

    //private OnFragmentInteractionListener mListener;

    public dataOptionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param filePath Parameter 1.
     * @param username Parameter 2.
     * @return A new instance of fragment dataOptionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static dataOptionsFragment newInstance(String filePath, String username) {
        dataOptionsFragment fragment = new dataOptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILEPATH, filePath);
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDataBaseFilePath = getArguments().getString(ARG_FILEPATH);
            mUsername = getArguments().getString(ARG_USERNAME);
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

        mSyncDatabase.setOnClickListener(this);
        mDeleteDatabase.setOnClickListener(this);

        //Find the +1 button
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

                new uploadDatabaseTask().execute();
                Log.i(TAG, "onClick:" + view.getId());

                break;
            case R.id.deleteOnlineData:


                Log.i(TAG, "onClick:" + view.getId());
                break;
        }
    }

    private class uploadDatabaseTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;
        private File DATABASE_FILE = new File(mDataBaseFilePath);
        private String s3FileKey = "UserData/"+ mUsername + ".db";


        uploadDatabaseTask() {
            //mEmail = email;
            //mPassword = password;
        }

        @Override
        protected void onPreExecute() {

            dialog = ProgressDialog.show(getActivity(),
                    "Uploading",
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... keys) {

            //sS3Client.deleteObject(new DeleteObjectRequest(Constants.BUCKET_NAME, s3FileKey));


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

                    Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.i(TAG, "Upload Failed");
                }

            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            dialog.dismiss();
        }
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
    }
    *;
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
