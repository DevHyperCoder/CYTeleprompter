package com.codeyard.teleprompter;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.codeyard.teleprompter.MainActivity.TAG;

// first: ca-app-pub-1367393062330218/2119831843
//banner: ca-app-pub-1367393062330218/7265822099
public class UploaderActivity extends AppCompatActivity {
    FileOperator fileOperator;
    CustomAdapterTextViewAndCheckbox customAdapterTextViewAndCheckbox;

    List<UploadCheckboxModel> dataModelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploader);

        Toolbar toolbar = findViewById(R.id.upload_file_tool);
        setSupportActionBar(toolbar);

        fileOperator = new FileOperator(UploaderActivity.this);
        getInternalStorageFiles();
        ListView listView = findViewById(R.id.upload_list_view);

        customAdapterTextViewAndCheckbox = new CustomAdapterTextViewAndCheckbox(dataModelList, UploaderActivity.this);
        listView.setAdapter(customAdapterTextViewAndCheckbox);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //Ads:
        InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1367393062330218/2119831843");
        AdRequest adRequestInterstitial = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequestInterstitial);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void getInternalStorageFiles() {

        File f = UploaderActivity.this.getFilesDir();
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
                dataModelList.add(new UploadCheckboxModel(s.replace(".txt", ""), false));
            } catch (Exception e) {
                Log.d(TAG, "getInternalStorageFiles: some error", e);
            }
        }
    }

    private void upload(String title, String text) {
        Log.d(MainActivity.TAG, "upload: " + title);
        new UploadTask(DropboxClient.getClient(MainActivity.ACCESS_TOKEN), title, text, UploaderActivity.this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_uplaoder_activity, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.upload_to_dropbox) {
            List<String> titleStrings = customAdapterTextViewAndCheckbox.getSelectedString();
            for (String titleString : titleStrings) {
                String text = fileOperator.readFromFile(titleString);
                upload(titleString, text);
            }
            return true;
        }
        return false;
    }

}
