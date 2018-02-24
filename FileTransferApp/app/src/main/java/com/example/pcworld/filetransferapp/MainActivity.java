package com.example.pcworld.filetransferapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    private Button fileSync;
    private Button newUser;
    private Button deleteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }
    private void initUI() {
        fileSync= (Button) findViewById(R.id.fileSync);
        newUser = (Button) findViewById(R.id.newFile);
        deleteUser = (Button) findViewById(R.id.deleteUser);

        fileSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, FileSync.class);
                startActivity(intent);
            }
        });

        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, deleteUser.class);
                startActivity(intent);
            }
        });

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                String filename = "AWSTEST";
                String fileContents = "Hello world!";
                FileOutputStream outputStream;

                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    Toast.makeText(
                            MainActivity.this,
                            "woah it worked",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(
                            MainActivity.this,
                            "didnt work",
                            Toast.LENGTH_SHORT).show();
                }




                /*try {
                    PrintWriter writer = new PrintWriter("test.txt", "UTF-8");
                    writer.println("The first line");
                    writer.println("The second line");
                    writer.close();
                    Toast.makeText(
                            MainActivity.this,
                            "Created new user",
                            Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    Toast.makeText(
                            MainActivity.this,
                            "Error creating new user",
                            Toast.LENGTH_SHORT).show();
                }*/
            }
        });
    }
}
