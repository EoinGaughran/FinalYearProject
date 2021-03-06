package com.rockfield.gmit.projectappfinal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.*;
import java.util.Map;
import java.util.UUID;

/*
 * Handles basic helper functions used throughout the app.
 */
public class Util {

    // We only need one instance of the clients and credentials provider
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;

    private static String clientUserName;
    private static String userPassword;

    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */
    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    Constants.COGNITO_POOL_ID,
                    Regions.fromName(Constants.COGNITO_POOL_REGION));
        }
        return sCredProvider;
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
            sS3Client.setRegion(Region.getRegion(Regions.fromName(Constants.BUCKET_REGION)));
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }

    /**
     * Converts number of bytes into proper scale.
     *
     * @param bytes number of bytes to be converted.
     * @return A string that represents the bytes in a proper scale.
     */
    public static String getBytesString(long bytes) {
        String[] quantifiers = new String[] {
                "KB", "MB", "GB", "TB"
        };
        double speedNum = bytes;
        for (int i = 0;; i++) {
            if (i >= quantifiers.length) {
                return "";
            }
            speedNum /= 1024;
            if (speedNum < 512) {
                return String.format("%.2f", speedNum) + " " + quantifiers[i];
            }
        }
    }

    public static void setClientUserName(String userName){
        clientUserName = userName;
    }

    public static String getClientUserName() {
        return clientUserName;
    }


    private static final String SQL_CREATE_ENTRIES_INFO =
            "CREATE TABLE " + SqlLibraries.userInfoDatabase.TABLE_NAME + " (" +
                    SqlLibraries.userInfoDatabase._ID + " INTEGER PRIMARY KEY," +
                    SqlLibraries.userInfoDatabase.COLUMN_NFCDATA + " TEXT," +
                    SqlLibraries.userInfoDatabase.COLUMN_TIME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES_INFO =
            "DROP TABLE IF EXISTS " + SqlLibraries.userInfoDatabase.TABLE_NAME;



    public static class UserDataDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.


        public UserDataDbHelper(Context context) {
            super(context, Constants.USER_INFO_DATABASE, null, Constants.USER_INFO_DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_INFO);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES_INFO);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static final String SQL_CREATE_ENTRIES_NFC =
            "CREATE TABLE " + SqlLibraries.nfcDatabase.TABLE_NAME + " (" +
                    SqlLibraries.nfcDatabase._ID + " INTEGER PRIMARY KEY," +
                    SqlLibraries.nfcDatabase.COLUMN_NFC_CODE+ " TEXT," +
                    SqlLibraries.nfcDatabase.COLUMN_NAME+ " TEXT," +
                    SqlLibraries.nfcDatabase.COLUMN_USEBY+ " TEXT," +
                    SqlLibraries.nfcDatabase.COLUMN_DETAILS+ " TEXT," +
                    SqlLibraries.nfcDatabase.COLUMN_BATCH_CODE+ " TEXT," +
                    SqlLibraries.nfcDatabase.COLUMN_RECOMMENDED_AMOUNT+ " TEXT)";

    private static final String SQL_DELETE_ENTRIES_NFC =
            "DROP TABLE IF EXISTS " + SqlLibraries.nfcDatabase.TABLE_NAME;



    public static class NfcDataDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.


        public NfcDataDbHelper(Context context) {
            super(context, Constants.NFC_INFO_DATABASE, null, Constants.NFC_INFO_DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_NFC);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES_NFC);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static final String SQL_CREATE_LOGIN_ENTRIES =
            "CREATE TABLE " + SqlLibraries.userLoginDatabase.TABLE_NAME + " (" +
                    SqlLibraries.userLoginDatabase._ID + " INTEGER PRIMARY KEY," +
                    SqlLibraries.userLoginDatabase.COLUMN_USERNAME + " TEXT," +
                    SqlLibraries.userLoginDatabase.COLUMN_PASSWORD + " TEXT," +
                    SqlLibraries.userLoginDatabase.COLUMN_LASTLOGOUT + " TEXT)";

    private static final String SQL_DELETE_LOGIN_ENTRIES =
            "DROP TABLE IF EXISTS " + SqlLibraries.userLoginDatabase.TABLE_NAME;



    public static class loginDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.


        public loginDbHelper(Context context) {
            super(context, Constants.USER_LOGIN_DATABASE, null, Constants.USER_LOGIN_DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_LOGIN_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_LOGIN_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}