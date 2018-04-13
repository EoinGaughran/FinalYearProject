package com.example.pcworld.appuserui;

import android.provider.BaseColumns;

public final class SqlReaderClass {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SqlReaderClass() {}

    /* Inner class that defines the table contents */
    public static class databaseInput implements BaseColumns {
        public static final String TABLE_NAME = "userData";
        public static final String COLUMN_TIME = "Time";
        public static final String COLUMN_NFCDATA = "NFC_Data";
    }
}


