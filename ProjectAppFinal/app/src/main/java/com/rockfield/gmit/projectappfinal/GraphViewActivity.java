package com.rockfield.gmit.projectappfinal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GraphViewActivity extends AppCompatActivity {

    private static final String userDatabaseKey = "UserData/" + Util.getClientUserName() + ".db";
    private static final String TAG = "GraphViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_view);

        initUi();
    }

    public void initUi() {

        //setup new sql library
        Util.UserDataDbHelper mUserDataDbHelper = new Util.UserDataDbHelper(this);

        SQLiteDatabase db = mUserDataDbHelper.getReadableDatabase();

        //StringBuilder tempSQL = new StringBuilder();

        String table = SqlLibraries.userInfoDatabase.TABLE_NAME;
        String nfcColumn = SqlLibraries.userInfoDatabase.COLUMN_NFCDATA;
        String timeColumn = SqlLibraries.userInfoDatabase.COLUMN_TIME;

        String query = "SELECT " + nfcColumn + ", " + timeColumn + " FROM " + table;
        Log.i(TAG, "SQL QUERY:" + query);

        Cursor cursor = db.rawQuery(query, null);

        String[] getTime = Calendar.getInstance().getTime().toString().split(" ");
        String today = getTime[0] + getTime[1] + getTime[2];
        String currentDay = today;
        String day = today;
        String[] timeRead;

        int [] inputCount = new int[7];
        int input = 0;
        int dayCount = 0;
        int calculateBiggest = 0;
        int biggestNumber = 0;

        List itemIds = new ArrayList<>();
        List timeList = new ArrayList<String>();
        List titleNames = new ArrayList<String>();
        //HashMap<String, Object> map = new HashMap<String, Object>();

        List results = new ArrayList<HashMap<String, String>>();

        if (!(cursor.moveToFirst()) || cursor.getCount() == 0)
            Log.i("GraphSQL", "No data to read");

        else

        {
            Log.i("GraphSQL", "Total SQL reads: " +cursor.getCount());

            cursor.moveToFirst();

            while (!cursor.isClosed()) {

                timeRead = (cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userInfoDatabase.COLUMN_TIME))).split(" ");
                day = timeRead[0] + timeRead[1] + timeRead[2];

                if (dayCount == 7) {

                    cursor.close();
                    day = null;
                }
                else if (day.equals(currentDay)) {

                    //HashMap<String, String> map = new HashMap<>();
                    //map.put(day, cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userInfoDatabase.COLUMN_NFCDATA)));
                    //results.add(map);
                    inputCount[input]++;
                    calculateBiggest++;
                    //itemIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(SqlLibraries.userInfoDatabase._ID)));
                    //titleNames.add(cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userInfoDatabase.COLUMN_NFCDATA)));
                    //timeList.add(cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userInfoDatabase.COLUMN_TIME)));
                    cursor.moveToNext();
                }

                else {

                    currentDay = day;
                    dayCount++;
                    Log.i(TAG, "Next Day:" +dayCount);
                    if(calculateBiggest > biggestNumber)
                        biggestNumber = calculateBiggest;

                    calculateBiggest = 0;
                }

                if(cursor.isAfterLast()) {

                    Log.i("GraphCursor", "reached end of read before dayCount max");
                    cursor.close();

                    if(dayCount == 0)
                        biggestNumber = calculateBiggest;
                }
            }
            
            //for(int i = 0; i < timeList.le)

            GraphView graph = (GraphView) findViewById(R.id.graph);

            DataPoint[] points = new DataPoint[dayCount+1];

            Log.i(TAG, "Number of days: " + (dayCount+1));

            for( int i = 0; i < dayCount+1; i++){

                Log.i(TAG, "Day: " +(i+1));
                Log.i(TAG, "Number of points: " + inputCount[i]);
                points[i] = new DataPoint(i+1, inputCount[i]);
            }

            BarGraphSeries<DataPoint> series = new BarGraphSeries<>(points);
            //cursor.close();

        /*DataPoint[] points = new DataPoint[100];
        for (int i = 0; i < points.length; i++) {
            points[i] = new DataPoint(i, Math.sin(i * 0.5) * 20 * (Math.random() * 10 + 1));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);*/

            // set manual X bounds
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(biggestNumber + 1);

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(10);

            // enable scaling and scrolling
            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(false);

            graph.addSeries(series);

        /*GraphView graph1 = (GraphView) findViewById(R.id.graph1);
        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        graph1.addSeries(series1);*/
        }
    }

    @Override
    public void onBackPressed() {

        finish();
    }
}