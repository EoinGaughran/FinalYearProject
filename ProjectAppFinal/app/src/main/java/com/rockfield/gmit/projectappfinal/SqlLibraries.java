package com.rockfield.gmit.projectappfinal;

import android.provider.BaseColumns;

public class SqlLibraries {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SqlLibraries() {}

    /* Inner class that defines the table contents */
    public static class userLoginDatabase implements BaseColumns {
        public static final String TABLE_NAME = Constants.USER_LOGIN_DATABASE;
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_LASTLOGOUT = "logoutTime";
    }

    public static class userInfoDatabase implements BaseColumns {
        public static final String TABLE_NAME = Constants.USER_INFO_DATABASE;
        public static final String COLUMN_TIME = "Time";
        public static final String COLUMN_NFCDATA = "NFC_Data";
    }

    public static class nfcDatabase implements BaseColumns {
        public static final String TABLE_NAME = Constants.NFC_INFO_DATABASE;
        public static final String COLUMN_NFC_CODE = "nfc_code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_USEBY = "use_by";
        public static final String COLUMN_BATCH_CODE= "batch_code";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_RECOMMENDED_AMOUNT = "recommended";
    }
}
