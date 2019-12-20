package com.codeyard.teleprompter;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */
class DownloadFileTask extends AsyncTask<String, Void, File> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    DownloadFileTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    //TODO change this
    @Override
    protected File doInBackground(String... params) {
        String metadata = params[0];
        try {
            //Download the file. Get the stream data
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100000)) {
                mDbxClient.files().download(metadata.toLowerCase())
                        .download(outputStream);
                MainActivity.contents = outputStream.toString();
                TeleprompterActivity.updateContents(outputStream.toString());
            }

            return null;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }

    public interface Callback {
        void onDownloadComplete(File result);

        void onError(Exception e);
    }
}