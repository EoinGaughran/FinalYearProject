package com.rockfield.gmit.projectappfinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.*;
import android.app.ListActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GraphViewActivity extends ListActivity {

    private Util.NfcDataDbHelper nfcDataDbHelper = new Util.NfcDataDbHelper(GraphViewActivity.this);
    private static final String TAG = "GraphViewActivity";

    private SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, String>>[] nfcReadDetails;

    private TextView mListDetails;
    private int listChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_view);

        mListDetails = (TextView) findViewById(R.id.listDetails);

        initUi();
    }

    public void initUi() {

        final int daysTotal = getIntent().getIntExtra("amount", 7);

        //setup new sql library
        Util.UserDataDbHelper mUserDataDbHelper = new Util.UserDataDbHelper(this);

        SQLiteDatabase db = mUserDataDbHelper.getReadableDatabase();

        String table = SqlLibraries.userInfoDatabase.TABLE_NAME;
        String nfcColumn = SqlLibraries.userInfoDatabase.COLUMN_NFCDATA;
        String timeColumn = SqlLibraries.userInfoDatabase.COLUMN_TIME;

        String query = "SELECT " + nfcColumn + ", " + timeColumn + " FROM " + table;
        Log.i(TAG, "SQL QUERY:" + query);

        Cursor cursor = db.rawQuery(query, null);

        String[] getTime = Calendar.getInstance().getTime().toString().split(" ");
        String today = getTime[0] + getTime[1] + getTime[2];
        String currentDay = today;
        String day = currentDay;
        String[] timeRead;

        int [] inputCount = new int[daysTotal + 1];
        int dayCount = 0;
        int calculateBiggest = 0;
        int biggestNumber = 0;

        nfcReadDetails  = (ArrayList<HashMap<String, String>>[])new ArrayList[daysTotal+1];

        nfcReadDetails[0] = new ArrayList<>();

        if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
            Log.i("GraphSQL", "No data to read");
            Toast.makeText(this, "No data to read", Toast.LENGTH_SHORT).show();
            finish();
        }
        else

        {
            Log.i("GraphSQL", "Total SQL reads: " +cursor.getCount());

            cursor.moveToLast();

            timeRead = (cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userInfoDatabase.COLUMN_TIME))).split(" ");

            while (!cursor.isClosed()) {

                if (dayCount == daysTotal) {

                    cursor.close();
                    day = null;
                }
                else if (day.equals(currentDay)) {

                    HashMap<String, String> map = new HashMap<>();
                    map.put("details", cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userInfoDatabase.COLUMN_NFCDATA))+
                            "/"+ timeRead[0] +"/"+ timeRead[1] +"/"+ timeRead[2] +"/"+ timeRead[3] +"/"+ timeRead[5]);
                    nfcReadDetails[dayCount].add(map);

                    inputCount[dayCount]++;
                    calculateBiggest++;
                    cursor.moveToPrevious();

                    if(!cursor.isBeforeFirst()) {
                        timeRead = (cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userInfoDatabase.COLUMN_TIME))).split(" ");
                        day = timeRead[0] + timeRead[1] + timeRead[2];
                    }
                }

                else {

                    currentDay = day;
                    Log.i(TAG, "Day:" +dayCount + " Reads: " + inputCount[dayCount]);
                    dayCount++;
                    nfcReadDetails[dayCount] = new ArrayList<>();
                    if(calculateBiggest > biggestNumber)
                        biggestNumber = calculateBiggest;

                    calculateBiggest = 0;
                }

                if(cursor.isBeforeFirst()) {

                    Log.i("GraphCursor", "reached end of read before dayCount max");
                    cursor.close();

                    if(dayCount == 0)
                        biggestNumber = calculateBiggest;
                }
            }

            GraphView graph = (GraphView) findViewById(R.id.graph);

            DataPoint [] points = new DataPoint[dayCount+1];
            String [] labels = new String[dayCount+1];

            Log.i(TAG, "Number of days: " + (dayCount+1));

            for( int i = 0; i < dayCount+1; i++){

                Log.i(TAG, "Day: " +(i+1));
                Log.i(TAG, "Number of points: " + inputCount[i]);
                points[i] = new DataPoint(i+1, inputCount[i]);
            }

            for( int i = 0; i < dayCount; i++){

                String read[] = nfcReadDetails[i].get(0).get("details").split("/");
                labels[i] = read[1] + " " + read[2];
            }

            BarGraphSeries<DataPoint> series = new BarGraphSeries<>(points);

            // set manual X bounds
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);

            int twentyPercentGap = (int)(biggestNumber *.2);
            if (twentyPercentGap == 0) twentyPercentGap = 1;

            graph.getViewport().setMaxY(biggestNumber + twentyPercentGap);

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(5);

            // enable scaling and scrolling
            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(false);


            try{
                StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
                staticLabelsFormatter.setHorizontalLabels(labels);
                graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
            }
            catch (RuntimeException e){

                Log.e(TAG, "Error: "+e);
            }
            graph.addSeries(series);

            series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                @Override
                public int get(DataPoint data) {
                    return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
                }
            });

            series.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {

                    int position = (int)dataPoint.getX()-1;
                    String read[] = nfcReadDetails[position].get(0).get("details").split("/");
                    String displayDay = read[1] + " " + read[2] + " " + read[3];
                    mListDetails.setText(displayDay);
                    initList(position);

                }
            });

            series.setSpacing(50);

            // draw values on top
            series.setDrawValuesOnTop(true);
            series.setValuesOnTopColor(Color.RED);
        }
    }

    public void initList(int choice){

        listChoice = choice;

        simpleAdapter = new SimpleAdapter(this, nfcReadDetails[choice],
                R.layout.list_items, new String[] {
                "details"
        },
                new int[] {
                        R.id.details
                });
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                switch (view.getId()) {
                    case R.id.details:
                        TextView fileName = (TextView) view;

                        String [] nfcCode = data.toString().split("/");

                        SQLiteDatabase db = nfcDataDbHelper.getReadableDatabase();

                        String[] projection = {
                                BaseColumns._ID,
                                SqlLibraries.nfcDatabase.COLUMN_NFC_CODE,
                                SqlLibraries.nfcDatabase.COLUMN_NAME
                        };

                        // Filter results WHERE "title" = 'My Title'

                        String selection = SqlLibraries.nfcDatabase.COLUMN_NFC_CODE + " = ?";

                        //Filter the nfcCode column with the nfc code we read from the choice in the list
                        String[] selectionArgs = { nfcCode[0]};

                        // How you want the results sorted in the resulting Cursor
                        String sortOrder =
                                SqlLibraries.nfcDatabase.COLUMN_NFC_CODE + " DESC";

                        Cursor cursor = db.query(
                                SqlLibraries.nfcDatabase.TABLE_NAME,   // The table to query
                                projection,             // The array of columns to return (pass null to get all)
                                selection,              // The columns for the WHERE clause
                                selectionArgs,          // The values for the WHERE clause
                                null,                   // don't group the rows
                                null,                   // don't filter by row groups
                                sortOrder               // The sort order
                        );

                        if(!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                            Log.i(TAG, "Error: Nfc code \""+nfcCode[0]+"\" isnt in the NfcDatabase");
                            fileName.setText(nfcCode[0] +" at "+ nfcCode[4]);
                        }
                        else {

                            String passDataToList = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.nfcDatabase.COLUMN_NAME
                            ))+" at "+ nfcCode[4];

                            fileName.setText(passDataToList);
                        }

                        cursor.close();

                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);

        // When an item is selected, finish the activity and pass back the S3
        // key associated with the object selected
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

                String nfcDetails[] = nfcReadDetails[listChoice].get(pos).get("details").split("/");

                SQLiteDatabase db = nfcDataDbHelper.getReadableDatabase();

                String[] projection = {
                        BaseColumns._ID,
                        SqlLibraries.nfcDatabase.COLUMN_NFC_CODE,
                        SqlLibraries.nfcDatabase.COLUMN_NAME,
                        SqlLibraries.nfcDatabase.COLUMN_USEBY,
                        SqlLibraries.nfcDatabase.COLUMN_DETAILS,
                        SqlLibraries.nfcDatabase.COLUMN_BATCH_CODE,
                        SqlLibraries.nfcDatabase.COLUMN_RECOMMENDED_AMOUNT
                };

                // Filter results WHERE "title" = 'My Title'

                String selection = SqlLibraries.nfcDatabase.COLUMN_NFC_CODE + " = ?";

                //Filter the nfcCode column with the nfc code we read from the choice in the list
                String[] selectionArgs = { nfcDetails[0] };

                // How you want the results sorted in the resulting Cursor
                String sortOrder =
                        SqlLibraries.nfcDatabase.COLUMN_NFC_CODE + " DESC";

                Cursor cursor = db.query(
                        SqlLibraries.nfcDatabase.TABLE_NAME,   // The table to query
                        projection,             // The array of columns to return (pass null to get all)
                        selection,              // The columns for the WHERE clause
                        selectionArgs,          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder               // The sort order
                );

                if(!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                    Log.i(TAG, "Error: Nfc code \""+nfcDetails[0]+"\" isnt in the NfcDatabase");
                    Toast.makeText(GraphViewActivity.this, "Please update your Medicine Library", Toast.LENGTH_SHORT).show();

                }
                else {

                    String[] passDataToFragment = new String[6];

                    passDataToFragment[0] = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.nfcDatabase.COLUMN_NAME));
                    passDataToFragment[1] = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.nfcDatabase.COLUMN_USEBY));
                    passDataToFragment[2] = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.nfcDatabase.COLUMN_DETAILS));
                    passDataToFragment[3] = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.nfcDatabase.COLUMN_BATCH_CODE));
                    passDataToFragment[4] = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.nfcDatabase.COLUMN_RECOMMENDED_AMOUNT));
                    passDataToFragment[5] = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.nfcDatabase.COLUMN_NFC_CODE));

                    Log.i(TAG, "Position" + pos);

                    cursor.close();

                    Intent intent = new Intent(GraphViewActivity.this, NfcDetailsActivity.class);
                    intent.putExtra("nfcReadDetails", nfcDetails);
                    intent.putExtra("nfcDatabaseDetails", passDataToFragment);
                    startActivity(intent);
                }


            }
        });
    }

    @Override
    public void onBackPressed() {

        finish();
    }
}