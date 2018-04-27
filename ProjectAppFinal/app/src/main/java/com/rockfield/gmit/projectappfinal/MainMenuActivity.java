package com.rockfield.gmit.projectappfinal;

import android.content.Context;
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
    private TestFragment testFragment;

    //private Util.UserDataDbHelper UserDatabase = new Util.UserDataDbHelper(this);
    //private SQLiteDatabase db = UserDatabase.getReadableDatabase();

    //private String databaseFilePath = getDatabasePath(Constants.USER_INFO_DATABASE).toString();

    private String username = Util.getClientUserName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_home:
                                selectedFragment = dataOptionsFragment.newInstance("test", username);
                                break;
                            case R.id.navigation_dashboard:
                                selectedFragment = HistoryFragment.newInstance("History", "Fragment");
                                break;
                            case R.id.navigation_notifications:
                                selectedFragment = TestFragment.newInstance("Hello", "World");
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
        transaction.replace(R.id.content, dataOptionsFragment.newInstance("share", "button sware"));
        transaction.commit();
    }
}
