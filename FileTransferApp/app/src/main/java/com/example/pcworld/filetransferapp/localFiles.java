package com.example.pcworld.filetransferapp;

import android.app.ListActivity;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

public class localFiles extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_files);
        init_ui();
    }

    public void init_ui(){

        TextView filesView1 = (TextView) findViewById(R.id.filesView);
        /*File dir = new File(getFilesDir().toString());
        File[] filelist = dir.listFiles();
        String[] theNamesOfFiles = new String[filelist.length];
        for (int i = 0; i < theNamesOfFiles.length; i++) {
            theNamesOfFiles[i] = filelist[i].getName();
        }

        new ArrayAdapter<String>(this, android.R.layout.activity_local_files, theNamesOfFiles);*/

        File dataDirectory = getFilesDir();
        File fileDir = new File(dataDirectory.toString());

        String[] listItems = fileDir.list();
        Arrays.sort(listItems);

        for(int i = 0; i < listItems.length; i ++){
            filesView1.append("\r\n" + listItems[i]);
        }

    }
}
