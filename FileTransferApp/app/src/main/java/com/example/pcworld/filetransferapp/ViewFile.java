package com.example.pcworld.filetransferapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.*;

public class ViewFile extends AppCompatActivity {

    private Button fileSearch;
    private Button fileDisplay;
    private TextView fileView;
    private String fileName;

    private static final String TAG = "ViewFile";

    private TransferUtility transferUtility;

    private static final int DOWNLOAD_SELECTION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);
        transferUtility = Util.getTransferUtility(this);
        initUI();
    }

    public void initUI(){

        fileView= (TextView) findViewById(R.id.fileView);
        fileSearch = (Button) findViewById(R.id.viewFileButton);
        fileDisplay = (Button) findViewById(R.id.downloadFile);

        fileSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ViewFile.this, deleteUser.class);
                startActivityForResult(intent, DOWNLOAD_SELECTION_REQUEST_CODE);
            }
        });

        fileDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(fileName != null) {

                    File file = new File(getFilesDir() + "/" + fileName);

                    if (file.exists())
                    {
                        Log.i(TAG, "index=ItsThere!!");
                    }
                    else
                    {
                        Log.i(TAG, "index=NotThere!!");
                    }

                    //Log.i(TAG, "index=" + file.toString());

                    // This will reference one line at a time
                    // String line;

                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;

                        while ((line = br.readLine()) != null) {
                            fileView.append(line);
                            fileView.append("\n");
                        }
                        br.close();
                    } catch (FileNotFoundException ex) {
                        Toast.makeText(
                                ViewFile.this,
                                "Unable to open file '" +
                                        file.toString() + "'",
                                Toast.LENGTH_SHORT).show();
                    } catch (IOException ex) {
                        Toast.makeText(
                                ViewFile.this,
                                "Error reading file '"
                                        + file.toString() + "'",
                                Toast.LENGTH_SHORT).show();
                        // Or we could just do this:
                        // ex.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == DOWNLOAD_SELECTION_REQUEST_CODE) {
            if(resultCode == RESULT_OK){

                String key = data.getStringExtra("key");
                beginDownload(key);
                fileName = key;
            }
        }
    }

    public void beginDownload(String key){
        File file = new File(getFilesDir() + "/" + key);
        Log.i(TAG, "index=" + file.toString());

        TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, key, file);


    }

    @Override
    protected void onResume(){
        super.onResume();

        initData();
    }

    public void initData() {


    }
}
