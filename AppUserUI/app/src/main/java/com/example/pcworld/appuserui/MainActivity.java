package com.example.pcworld.appuserui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button testButton;
    private Button awsButton;

    private static final String TAG = "Main Activity";

    public File infoFile = new File(getFilesDir() + Constants.USER_INFO_FILE);
    public File dataFile = new File(getFilesDir() + Constants.USER_DATA_FILE);

    private static final int AWS_LIST_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initUI();
    }

    public void initData(){

        if(!infoFile.exists()) {
            firstTimeSetup();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        else {
            readFile(infoFile);
        }

        if(!dataFile.exists()){

            String dataFileFormat = "data";

            createFile(dataFile.toString(), dataFileFormat);
            try{
                String path = getFilesDir() + Constants.USER_DATA_FILE;
                //beginUpload(path);
            }
            catch(Exception e){
                Toast.makeText(MainActivity.this, "didnt work",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void initUI(){

        testButton = (Button) findViewById(R.id.testButton);
        awsButton = (Button) findViewById(R.id.awsButton);

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        awsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(MainActivity.this, AwsListActivity.class);
                startActivityForResult(intent, AWS_LIST_RESULT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == AWS_LIST_RESULT && data != null){
            if(resultCode == RESULT_OK){

                String dataString = data.getStringExtra("key");
                Log.i(TAG, "Context = " + dataString);
                Log.i(TAG, "Password = " + Util.getUserPassword());
                Log.i(TAG, "Username = " + Util.getClientUserName());
            }
        }
    }

    @Override
    protected void onResume(){
        this.onResume();

        initUI();
    }

    private void firstTimeSetup(){

        String infoFileFormat = "**This file is for saving the users info\r\n" +
                "**Do not change anything in the file\r\n" +
                "**Username\r\n" +
                "\"usernameUndefined\"\r\n" +
                "**Password\r\n" +
                "\"passwordUndefined\"";

        createFile(infoFile.toString(), infoFileFormat);
    }

    private void createFile(String filename, String fileContents){

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    MainActivity.this,
                    "didnt work",
                    Toast.LENGTH_SHORT).show();
        }

    }
    private String readFile(File fileToRead){

        String contents = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileToRead));
            String line;


            //fileView.setText("");

            /*while ((line = br.readLine()) != null) {
                //fileView.append(line);
                //fileView.append("\n");
            }*/


            br.close();
        } catch (FileNotFoundException ex) {
            Toast.makeText(
                    MainActivity.this,
                    "Unable to open file '" +
                            fileToRead.toString() + "'",
                    Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Toast.makeText(
                    MainActivity.this,
                    "Error reading file '"
                            + fileToRead.toString() + "'",
                    Toast.LENGTH_SHORT).show();
            // Or we could just do this:
            // ex.printStackTrace();
        }

        return contents;

        /*try {
            Files.readAllLines(Paths.get(infoFile.toString());
        }
        catch (IOException e){
            Log.e("Main", "readAllLines Error: " + e);
        }*/
    }
}