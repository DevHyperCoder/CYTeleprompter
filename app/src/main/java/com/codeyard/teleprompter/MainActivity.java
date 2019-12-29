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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class MainActivity extends AppCompatActivity {
    static final String TAG = "TELEPROMPTER";
    static final String THEME = "THEME";
    static final int LIGHT_MODE = 0;
    static final int DARK_MODE = 1;
    //static final int USER_THEME_MODE = 2;
    static String contents;
    static String ACCESS_TOKEN;
    private CustomAdapterTwoTextViews customAdapterTwoTextViews;
    private FileOperator fileOperator;
    private List<DataModel> dataModelList;
    private DbxClientV2 client;

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a network
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED;

    }

    private void loadAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

//    void setTheme() {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//        int userTheme = sharedPreferences.getInt(THEME, DARK_MODE);
//        switch (userTheme) {
//            case DARK_MODE:
//                //dark mode
//                setTheme(R.style.AppTheme);
//                break;
//
//            case LIGHT_MODE:
//                setTheme(R.style.AppTheme_Light);
//
//        }
//    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        enableStrictMode();
        loadAds();
        fileOperator = new FileOperator(MainActivity.this);
        dataModelList = new ArrayList<>();
        Toolbar toolbar = findViewById(R.id.tool);
        setSupportActionBar(toolbar);


        //check if internet connection
        if (isConnected()) {
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
        //parse the dates into ascending order
        Collections.sort(dataModelList, Collections.<DataModel>reverseOrder());
        //add the data to the listView and add a onItemClick Listener
        final ListView listView;
        listView = findViewById(R.id.listViewForFiles);
        if (dataModelList.size() > 0) {
            CustomAdapterTwoTextViews customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
            listView.setAdapter(customAdapterTwoTextViews);
        } else {

            dataModelList.add(new DataModel("Sorry! No contents found", "", DataModel.LOCATION_DROPBOX));
            customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
            listView.setAdapter(customAdapterTwoTextViews);
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
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                if (dataModelList.get(position).getLocation() == DataModel.LOCATION_INTERNAL) {
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("Are you sure??")
                            .setMessage("Are you sure that you want to delete the file \"" + dataModelList.get(position).getName() + "\"?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File f = MainActivity.this.getFilesDir();
                                    Log.d(TAG, "onClick: " + Arrays.toString(f.list()));//getting the list of files in string array
                                    fileOperator.delete(dataModelList.get(position).getName());
                                    dataModelList.remove(position);
                                    if (dataModelList.size() > 0) {
                                        CustomAdapterTwoTextViews customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
                                        listView.setAdapter(customAdapterTwoTextViews);
                                    } else {

                                        dataModelList.add(new DataModel("Sorry! No contents found", "", DataModel.LOCATION_DROPBOX));
                                        customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
                                        listView.setAdapter(customAdapterTwoTextViews);
                                    }
                                    Log.d(TAG, "onClick: " + Arrays.toString(f.list()));//getting the list of files in string array

                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("Sorry!")
                            .setMessage("Sorry this file can not be deleted as it is located in dropbox. Deleting files from dropbox feature will be added soon")
                            .setPositiveButton("Ok", null)
                            .show();
                }
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
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FileEditActivity.class));
            }
        });
    }


    /**
     * @param name name of the file
     */
    private void readFile(String name) {
        //reading text from file
        String file = fileOperator.readFromFile(name);
        int indexOfSemicolon = file.indexOf(";");
        file = file.substring(indexOfSemicolon + 1);
        MainActivity.contents = file;
        //    TeleprompterActivity.updateContents(file);
    }

    /***
     * Gets the contents from internal storage
     */
    private void getInternalStorageFiles() {
        File f = MainActivity.this.getFilesDir();
        String[] values = f.list();//getting the list of files in string array
        String date;
        for (String s : values) {
            try {
                String data = fileOperator.readFromFile(s.replace(".txt", ""));
                int indexOfSemicolon = data.indexOf(";");
                if (indexOfSemicolon > 0) {
                    date = data.substring(0, indexOfSemicolon);
                } else {
                    date = "";
                }
                Log.d(TAG, "getInternalStorageFiles: date " + date);
                dataModelList.add(new DataModel(s.replace(".txt", ""), date, DataModel.LOCATION_INTERNAL));
            } catch (Exception e) {
                Log.d(TAG, "getInternalStorageFiles: some error", e);
            }
        }
    }

    /***
     * Updates the contents and notifies the adapter
     */
    private void updateContents() {
        dataModelList.clear();
        getContents();
        getInternalStorageFiles();
        Collections.sort(dataModelList, Collections.<DataModel>reverseOrder());
        if (dataModelList.size() > 0) {
            CustomAdapterTwoTextViews customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
            ListView lio = findViewById(R.id.listViewForFiles);
            lio.setAdapter(customAdapterTwoTextViews);
        } else {

            dataModelList.add(new DataModel("Sorry! No contents found", "", DataModel.LOCATION_DROPBOX));
            customAdapterTwoTextViews = new CustomAdapterTwoTextViews(dataModelList, MainActivity.this);
            ListView lio = findViewById(R.id.listViewForFiles);
            lio.setAdapter(customAdapterTwoTextViews);
        }
    }

    /***
     * Gets the contents from dropbox
     */
    private void getContents() {
        //Check if a token exists and send to login activity if not
        if (isConnected()) {
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
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
//            case R.id.about:
//                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
//                startActivity(intent);
//                return true;
        }
        if (item.getItemId() == R.id.upload) {
            startActivity(new Intent(MainActivity.this, UploaderActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
