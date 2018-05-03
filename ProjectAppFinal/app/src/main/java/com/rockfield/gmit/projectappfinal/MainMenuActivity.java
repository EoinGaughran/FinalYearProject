package com.rockfield.gmit.projectappfinal;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;

public class MainMenuActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private String username = Util.getClientUserName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        final String userDatabasePath = getIntent().getStringExtra("path");
        final String nfcDatabasePath = getIntent().getStringExtra("nfcDatabasePath");
        final String s3UserDatabaseKey = "UserData/"+username+".db";

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_home:
                                selectedFragment = dataOptionsFragment.newInstance(userDatabasePath, username, s3UserDatabaseKey, nfcDatabasePath);
                                break;
                            case R.id.navigation_dashboard:
                                selectedFragment = HistoryFragment.newInstance("History", "Fragment");
                                break;
                            case R.id.navigation_notifications:
                                selectedFragment = AlarmFragment.newInstance("Alarm", "Fragment");
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.content, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, dataOptionsFragment.newInstance(
                userDatabasePath, username, s3UserDatabaseKey, nfcDatabasePath));
        transaction.commit();
    }

    @Override
    public void onBackPressed(){

        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}