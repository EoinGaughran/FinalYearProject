package com.example.eoin.nfctest;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    }
}
