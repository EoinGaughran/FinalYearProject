package com.example.pcworld.appuserui;

import java.io.File;

public class Constants {

    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "eu-west-1:0e854cdc-c925-462a-a214-d58d9f16cdc3";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "eu-west-1";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "finalyearprojecteoin";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "eu-west-1";

    public static final String USER_INFO_FILE = "/UserInfo";
    public static final String USER_DATA_FILE = "/UserData";
}
