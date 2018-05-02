package com.rockfield.gmit.projectappfinal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class NfcDetailsActivity extends AppCompatActivity {

    private TextView mDisplayNfcDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_details);

        initViews();
    }

    private void initViews(){

        mDisplayNfcDetails = (TextView) findViewById(R.id.displayNfcDetails);

        final String [] mNfcReadDetails = getIntent().getStringArrayExtra("nfcReadDetails");
        final String [] mNfcDatabaseDetails = getIntent().getStringArrayExtra("nfcDatabaseDetails");

        StringBuilder sb = new StringBuilder();

        for (String mNfcReadDetail : mNfcReadDetails) {

            sb.append(mNfcReadDetail + "\r\n");
        }

        for (String mNfcDatabaseDetail : mNfcDatabaseDetails) {

            sb.append(mNfcDatabaseDetail + "\r\n");
        }

        mDisplayNfcDetails.setText(sb.toString());
    }
}
