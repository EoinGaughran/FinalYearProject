package com.rockfield.gmit.projectappfinal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private TransferUtility transferUtility;

    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(LoginActivity.this);

    private boolean makeNewAccount = false;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        transferUtility = Util.getTransferUtility(this);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button mCreateAccountButton = (Button) findViewById(R.id.createAccount);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mCreateAccountButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SqlLibraries.userLoginDatabase.TABLE_NAME + " (" +
                    SqlLibraries.userLoginDatabase._ID + " INTEGER PRIMARY KEY," +
                    SqlLibraries.userLoginDatabase.COLUMN_USERNAME + " TEXT," +
                    SqlLibraries.userLoginDatabase.COLUMN_PASSWORD + " TEXT," +
                    SqlLibraries.userLoginDatabase.COLUMN_LASTLOGOUT + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SqlLibraries.userLoginDatabase.TABLE_NAME;



    public class FeedReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.


        public FeedReaderDbHelper(Context context) {
            super(context, Constants.USER_LOGIN_DATABASE, null, Constants.USER_LOGIN_DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);

            if (makeNewAccount){
                Log.i("AccountCreate", "Creating New One");
                //new newAccount(email, password).execute();
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        //return true;
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void createAccount(){

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        new createAccountTask(email, password).execute();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }



        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            File DATABASE_FILE = new File(getDatabasePath(Constants.USER_LOGIN_DATABASE).toString());
            Log.i("S3 FILE DOWNLOAD", "Current local database location" + getDatabasePath(Constants.USER_LOGIN_DATABASE));
            Log.i("S3 FILE DOWNLOAD", "Local Directory: "+ DATABASE_FILE.toString());
            TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, "UserData/UserAccounts.db", DATABASE_FILE);

            //Cursor cursor = sqlRead(mEmail);
            String[] projection = {
                    BaseColumns._ID,
                    SqlLibraries.userLoginDatabase.COLUMN_PASSWORD,
                    SqlLibraries.userLoginDatabase.COLUMN_LASTLOGOUT,
                    SqlLibraries.userLoginDatabase.COLUMN_USERNAME
            };

            // Filter results WHERE "title" = 'My Title'

            String selection = SqlLibraries.userLoginDatabase.COLUMN_USERNAME + " = ?";
            String[] selectionArgs = { mEmail };

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    SqlLibraries.userLoginDatabase.COLUMN_PASSWORD + " DESC";

            Cursor cursor = db.query(
                    SqlLibraries.userLoginDatabase.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );


            Log.i("AccountCheck", "Cursor Length: " + cursor.getCount());

            Log.i("AccountCheck", "APP Received Username:" + mEmail);
            Log.i("AccountCheck", "APP Received Username:" + mPassword);

            String receivedPassword = "NoPasswordReceived";
            String receivedUsername = "NoUsernameReceived";

            if(!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                Log.i("AccountCheck", "Account Doesnt Exist");

            }

            else {

                receivedPassword = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userLoginDatabase.COLUMN_PASSWORD));
                receivedUsername = cursor.getString(cursor.getColumnIndexOrThrow(SqlLibraries.userLoginDatabase.COLUMN_USERNAME));

                Log.i("AccountCheck", "SQL Received Username:" + receivedUsername);
                Log.i("AccountCheck", "SQL received Username:" + receivedPassword);

                if(receivedUsername.equals(mEmail)) {

                    Log.i("AccountCheck", "Username found");

                    if (!receivedPassword.equals(mPassword))
                        Log.i("AccountCheck", "Password incorrect");

                    else
                        Log.i("AccountCheck", "Password correct");
                }
            }
            cursor.close();

            /*try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }*/

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                SharedPreferences prefs = getSharedPreferences("LoginDetails", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged_in", true);
                editor.putString("username", mEmail);
                editor.putString("password", mPassword);
                editor.apply();

                Util.setClientUserName(mEmail);
                Util.setUserPassword(mPassword);

                //mDbHelper.close();
                //deleteDatabase(Constants.USER_LOGIN_DATABASE);

                Intent intent = new Intent();
                intent.putExtra("key", mEmail);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private Cursor sqlRead(String mEmail){

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                SqlLibraries.userLoginDatabase.COLUMN_PASSWORD,
                SqlLibraries.userLoginDatabase.COLUMN_LASTLOGOUT,
                SqlLibraries.userLoginDatabase.COLUMN_USERNAME
        };

        // Filter results WHERE "title" = 'My Title'

        String selection = SqlLibraries.userLoginDatabase.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { mEmail };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                SqlLibraries.userLoginDatabase.COLUMN_PASSWORD + " DESC";

        Cursor cursor = db.query(
                SqlLibraries.userLoginDatabase.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        return cursor;
    }

    private class createAccountTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog dialog;
        private String mEmail;
        private String mPassword;

        private File DATABASE_FILE = new File(getDatabasePath(Constants.USER_LOGIN_DATABASE).toString());


        createAccountTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginActivity.this,
                    "Creating",
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(String... keys) {

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(SqlLibraries.userLoginDatabase.COLUMN_USERNAME, mEmail);
            values.put(SqlLibraries.userLoginDatabase.COLUMN_PASSWORD, mPassword);

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(SqlLibraries.userLoginDatabase.TABLE_NAME, null, values);
            Log.i("SQLWRITE", "RowId: "+ newRowId);
            Log.i("DatabaseUpload", "File location:" + DATABASE_FILE.toString());
            /** UpLoad Database**/
            TransferObserver uploadObserver =
                    transferUtility.upload(Constants.BUCKET_NAME,
                            "UserData/UserAccounts.db",
                            DATABASE_FILE);

            uploadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        Log.i("UploadLoginDatabase", "Database Upload Successful");
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                    int percentDone = (int)percentDonef;

                    Log.d("MainActivity", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.i("UploadLoginDatabase", "Upload Failed");
                }

            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();

            Toast.makeText(LoginActivity.this, "Account Created",
                    Toast.LENGTH_LONG).show();
        }
    }
}