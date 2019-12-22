package com.codeyard.teleprompter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    static final String TAG = "TELEPROMPTER";
    static String contents;
    private List<DataModel> dataModelList;
    private DbxClientV2 client;
    private String ACCESS_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableStrictMode();
        com.google.android.material.appbar.AppBarLayout toolbar = findViewById(R.id.tool);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        dataModelList = new ArrayList<>();
        //Check if a token exists and send to login activity if not
        if (!tokenExists()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        //get the dropbox account
        ACCESS_TOKEN = retrieveAccessToken();
        getUserAccount();
        //noinspection deprecation
        DbxRequestConfig config = new DbxRequestConfig("Teleprompter Client", "en_US");

        client = new DbxClientV2(config, ACCESS_TOKEN);
        // Get files and folder metadata from Dropbox directory
        ListFolderResult result;
        try {
            result = client.files().listFolder("/Apps/Parrot Teleprompter");
            Log.e(TAG, result.toString());

            //result is in json form
            JSONObject jsonObject1 = new JSONObject(result.toString());
            JSONArray entries = jsonObject1.getJSONArray("entries");
            for (int i = 0; i < entries.length(); i++) {
                JSONObject jsonObject = entries.getJSONObject(i);
                String filename = jsonObject.getString("name");
                String date = jsonObject.getString("client_modified");
                try {
                    if (filename.contains(".txt")) { //check if the file is a txt file
                        String finalDate = date.substring(0, date.indexOf('T'));
                        String finalFileName = filename.substring(0, filename.indexOf(".txt"));
                        dataModelList.add(new DataModel(finalFileName, finalDate));
                    }
                } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundException) {
                    Log.e(TAG, "StringIndexOutOfBoundsException");
                    Log.e(TAG, stringIndexOutOfBoundException.toString());
                }

            }
        } catch (DbxException | JSONException e) {
            e.printStackTrace();
        }
        //parse the dates into ascending order
        Collections.sort(dataModelList, Collections.<DataModel>reverseOrder());

//add the data to the listView and add a onItemClick Listener
        ListView listView = findViewById(R.id.listViewForFiles);
        CustomAdapterTwoTextViews customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
        listView.setAdapter(customAdapterTwoTextViews);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                DataModel currDataModel = dataModelList.get(position);
                downloadFile(currDataModel.getName());
                Log.e(TAG, "Selected File is " + currDataModel.getName());
                Intent i = new Intent(MainActivity.this, TeleprompterActivity.class);
                startActivity(i);
            }
        });

    }


    /***
     * Enables Strict Mode
     */
    private void enableStrictMode() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    /***
     *Gets the user account
     */
    private void getUserAccount() {
        if (ACCESS_TOKEN == null) return;
        new UserAccountTask(DropboxClient.getClient(ACCESS_TOKEN), new UserAccountTask.TaskDelegate() {
            @Override
            public void onAccountReceived() {
                Log.d(TAG, "Account details received successfully");
            }

            //Display if any error messages
            @Override
            public void onError(Exception error) {
                Log.d("User data", "Error receiving account details.");
                Log.e(TAG, error.getMessage());
                Toast.makeText(MainActivity.this, "Something went wrong while trying to access the dropbox account", Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    /***
     * Downloads the specified file from dropbox
     */
    private void downloadFile(String file) {
        String dropboxPath = "/Apps/Parrot Teleprompter/";
        String txtExtension = ".txt";
        file = dropboxPath + file + txtExtension;
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading");
        dialog.show();

        new DownloadFileTask(client, new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();
                if (result != null) {
                    Log.e(TAG, result.toString());
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to download file.", e);
                Toast.makeText(MainActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    /**
     * Checks if the access token exists on the device
     */
    private boolean tokenExists() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.sharedPreferenceName), Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }

    /**
     * Retrieves the access token on the device
     *
     * @return Returns the access token. Returns null if no token was found
     */
    private String retrieveAccessToken() {
        //check if ACCESS_TOKEN is previously stored on previous app launches
        SharedPreferences prefs = getSharedPreferences("com.example.valdio.dropboxintegration", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            Log.d("AccessToken Status", "No token found");
            return null;
        } else {
            //accessToken already exists
            Log.d("AccessToken Status", "Token exists");
            return accessToken;
        }
    }

    /*
     * Add menu system
     * Add item select listeners
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
