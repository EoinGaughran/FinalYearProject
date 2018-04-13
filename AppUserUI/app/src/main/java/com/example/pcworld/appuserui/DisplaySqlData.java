package com.example.pcworld.appuserui;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DisplaySqlData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sql_data);

        initUI();
    }

    public void initUI(){

        TextView sqlDisplay= (TextView) findViewById(R.id.sqlView);

        sqlDisplay.setText(MainActivity.SQL_READ);
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
