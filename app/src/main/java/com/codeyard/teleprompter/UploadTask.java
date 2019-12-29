package com.codeyard.teleprompter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.codeyard.teleprompter.MainActivity.TAG;

/**
 * Uploads the file to dropbox
 */
class UploadTask extends AsyncTask<String, String, String> {

    final private DbxClientV2 dbxClient;
    final private String title;
    final private String file;
    @SuppressLint("StaticFieldLeak")
    final private
    Context context;

    UploadTask(DbxClientV2 dbxClient, String title, String file, Context context) {
        this.dbxClient = dbxClient;
        this.title = title;
        this.file = file;
        this.context = context;
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            // Upload to Dropbox
            Log.d(TAG, "doInBackground: starting");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.file.getBytes());
            dbxClient.files().uploadBuilder("/Apps/CYTeleprompter/" + title + ".txt") //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(byteArrayInputStream);
            Log.d(TAG, "Success");
        } catch (DbxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String o) {
        super.onPostExecute(o);
        Toast.makeText(context, "File uploaded successfully", Toast.LENGTH_SHORT).show();
    }
}
