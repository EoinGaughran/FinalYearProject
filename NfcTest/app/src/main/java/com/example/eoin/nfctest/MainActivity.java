package com.example.eoin.nfctest;

import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        TextView messageView1 = (TextView) findViewById(R.id.textView);

        setIntent(intent);

        try {
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                    || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent
                    .getAction())) {
                messageView1.append("Found NFC tag: ");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        IntentFilter[] mFilters = new IntentFilter[]{
                ndef,
        };


        // Setup a tech list for all NfcF tags
        String[][] mTechLists = new String[][]{new String[]{MifareClassic.class.getName()}};


        Intent intent = getIntent();

        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        byte[] data;

        try {       //  5.1) Connect to card
            mfc.connect();
            boolean auth = false;
            String cardData = null;
            // 5.2) and get the number of sectors this card has..and loop thru these sectors
            int secCount = mfc.getSectorCount();
            int bCount = 0;
            int bIndex = 0;
            for(int j = 0; j < secCount; j++){
                // 6.1) authenticate the sector
                auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
                if(auth){
                    // 6.2) In each sector - get the block count
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = 0;
                    for(int i = 0; i < bCount; i++){
                        bIndex = mfc.sectorToBlock(j);
                        // 6.3) Read the block
                        data = mfc.readBlock(bIndex);
                        // 7) Convert the data into a string from Hex format.
                        Log.i(TAG, getHexString(data, data.length));
                        bIndex++;
                    }
                }else{ // Authentication failed - Handle it

                }
            }
        }catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            showAlert(3);
        }
		
		try {       //  5.1) Connect to card
            mfc.connect();
            boolean auth = false;
            String cardData = null;
            // 5.2) and get the number of sectors this card has..and loop thru these sectors
            int secCount = mfc.getSectorCount();
            int bCount = 0;
            int bIndex = 0;
            for(int j = 0; j < secCount; j++){
                // 6.1) authenticate the sector
                auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
                if(auth){
                    // 6.2) In each sector - get the block count
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = 0;
                    for(int i = 0; i < bCount; i++){
                        bIndex = mfc.sectorToBlock(j);
                        // 6.3) Read the block
                        data = mfc.readBlock(bIndex);
                        // 7) Convert the data into a string from Hex format.
                        Log.i(TAG, getHexString(data, data.length));
                        bIndex++;
                    }
                }else{ // Authentication failed - Handle it

                }
            }
        }catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            showAlert(3);
        }
    }// End of d

}
