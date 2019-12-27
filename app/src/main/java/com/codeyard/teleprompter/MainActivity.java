package com.codeyard.teleprompter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class MainActivity extends AppCompatActivity {
    static final String TAG = "TELEPROMPTER";
    static String contents;
    FloatingActionButton fab;
    CustomAdapterTwoTextViews customAdapterTwoTextViews;
    private List<DataModel> dataModelList;
    private DbxClientV2 client;
    private String ACCESS_TOKEN;

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a network
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        enableStrictMode();
        dataModelList = new ArrayList<>();
        Toolbar toolbar = findViewById(R.id.tool);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        //check if internet connection
        if (!isConnected()) {
            //show the dialog
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Not Connected to Network")
                    .setMessage("Internet access is required to retrieve the files form dropbox. Please connect to WiFi or Mobile network")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    })
                    .setNegativeButton("Proceed without internet access", null)
                    .show();

        }
        getContents();
//read the list of files form the internal storage
        getInternalStorageFiles();
        //add the data to the listView and add a onItemClick Listener
        ListView listView = findViewById(R.id.listViewForFiles);
        if (dataModelList.size() > 0) {
            CustomAdapterTwoTextViews customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
            listView.setAdapter(customAdapterTwoTextViews);
        } else {

            dataModelList.add(new DataModel("Sorry! No contetns found", "", DataModel.LOCATION_DROPBOX));

            customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
            listView.setAdapter(customAdapterTwoTextViews);
            listView.setClickable(false);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                DataModel currDataModel = dataModelList.get(position);
                if (currDataModel.getLocation() == DataModel.LOCATION_DROPBOX) {
                    downloadFile(currDataModel.getName());
                } else if (currDataModel.getLocation() == DataModel.LOCATION_INTERNAL) {
                    Log.d(TAG, "onItemClick: date = " + currDataModel.getDate());
                    readFile(currDataModel.getName());
                }
                Log.e(TAG, "Selected File is " + currDataModel.getName());
                Intent i = new Intent(MainActivity.this, TeleprompterActivity.class);
                startActivity(i);

            }
        });
        //todo add te long click to remove right now only applicable for internal storage
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Are you sure??")
                        .setMessage("Are you sure that you want to delete the file \"" + dataModelList.get(position).getName() + "\"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dataModelList.remove(position);
//delete the file
                                if (customAdapterTwoTextViews != null) {
                                    customAdapterTwoTextViews.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();


                return true;
            }
        });
        final SwipeRefreshLayout mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateContents();
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

        });
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FileEditActivity.class));
            }
        });
    }

    void readFile(String name) {
        //reading text from file
        try {
            Log.d(TAG, "readFile: filename" + name + ".txt");
            FileInputStream fileIn = openFileInput(name + ".txt");
            InputStreamReader InputRead = new InputStreamReader(fileIn);

            int READ_BLOCK_SIZE = 1000;
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            StringBuilder s = new StringBuilder();
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                s.append(String.copyValueOf(inputBuffer, 0, charRead));
            }
            InputRead.close();
            String file = s.toString();
            int indexOfSemicolon = file.indexOf(";");
            String date = file.substring(0, indexOfSemicolon);
            file = file.substring(indexOfSemicolon + 1);
            MainActivity.contents = file;
            TeleprompterActivity.updateContents(file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "readFile: Some error", e);
        }
    }

    /***
     * Gets the contents from internal storage
     */
    //todo fix the bu where the date doesnt show
    void getInternalStorageFiles() {
        File f = MainActivity.this.getFilesDir();
        String[] values = f.list();//getting the list of files in string array
        Log.d(TAG, "getInternalStorageFiles: " + Arrays.toString(values));
        String date = "";
        for (String s : values) {
            try {
                Log.d(TAG, "getInternalStorageFiles: filename" + s);
                s = s.trim();
                s = s.replace(" ", "");
                FileInputStream fileIn = new FileInputStream(s);
                InputStreamReader InputRead = new InputStreamReader(fileIn);

                int READ_BLOCK_SIZE = 1000;
                char[] inputBuffer = new char[READ_BLOCK_SIZE];
                StringBuilder stringBuilder = new StringBuilder();
                int charRead;

                while ((charRead = InputRead.read(inputBuffer)) > 0) {
                    // char to string conversion
                    stringBuilder.append(String.copyValueOf(inputBuffer, 0, charRead));
                }
                InputRead.close();
                String data = stringBuilder.toString();
                int indexOfSemicolon = data.indexOf(";");
                date = data.substring(0, indexOfSemicolon);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "readFile: Some error", e);
            }
            Log.d(TAG, "getInternalStorageFiles: date " + date);
            dataModelList.add(new DataModel(s.replace(".txt", ""), date, DataModel.LOCATION_INTERNAL));
        }
    }

    /***
     * Updates the contents and notifies the adapter
     */
    private void updateContents() {
        dataModelList.clear();
        getContents();
        getInternalStorageFiles();
        if (customAdapterTwoTextViews != null) {
            customAdapterTwoTextViews.notifyDataSetChanged();
        }
    }

    /***
     * Gets the contents from dropbox
     */
    public void getContents() {
        //Check if a token exists and send to login activity if not
        if (!isConnected()) {
            return;
        }
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
            result = client.files().listFolder("/Apps/CYTeleprompter");
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
                        dataModelList.add(new DataModel(finalFileName, finalDate, DataModel.LOCATION_DROPBOX));
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
        String dropboxPath = "/Apps/CYTeleprompter/";
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
