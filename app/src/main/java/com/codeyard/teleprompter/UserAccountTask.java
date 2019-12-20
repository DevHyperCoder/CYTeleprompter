package com.codeyard.teleprompter;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;


class UserAccountTask extends AsyncTask<Void, Void, FullAccount> {

    private final DbxClientV2 dbxClient;
    private final TaskDelegate delegate;
    private Exception error;

    UserAccountTask(DbxClientV2 dbxClient, TaskDelegate delegate) {
        this.dbxClient = dbxClient;
        this.delegate = delegate;
    }

    @Override
    protected FullAccount doInBackground(Void... params) {
        try {
            //get the user's FullAccount
            return dbxClient.users().getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);

        if (account != null && error == null) {
            //User Account received successfully
            delegate.onAccountReceived();
        } else {
            // Something went wrong
            delegate.onError(error);
        }
    }

    public interface TaskDelegate {
        void onAccountReceived();

        void onError(Exception error);
    }
}