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

        sb.append("Mecidine Scan Details:\r\n");

        sb.append("\r\nMedicine: " + mNfcDatabaseDetails[0]);
        sb.append("\r\nRecommended: " + mNfcDatabaseDetails[4]);
        sb.append("\r\nContains: " + mNfcDatabaseDetails[2]);
        sb.append("\r\nBest Before: " + mNfcDatabaseDetails[1]);
        sb.append("\r\nBatch code: " + mNfcDatabaseDetails[3]);

        sb.append("\r\n\nScan Details\r\n");

        sb.append("\r\nTime: " + mNfcReadDetails[4]);
        sb.append("\r\nDay: " + mNfcReadDetails[1]);
        sb.append("\r\nDate: " + mNfcReadDetails[2] +" "+ mNfcReadDetails[3] +" "+ mNfcReadDetails[5]);
        sb.append("\r\nNFC Code: " + mNfcReadDetails[0]);

        mDisplayNfcDetails.setText(sb.toString());
    }
}
