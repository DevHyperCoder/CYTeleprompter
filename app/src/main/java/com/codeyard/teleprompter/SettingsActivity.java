package com.codeyard.teleprompter;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    static final String NUM_LINES = "NUM_LINES";
    static final String FONT_SIZE = "FONT-SIZE";
    boolean mirror;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        final TextView textView = findViewById(R.id.textPreview);
        final SeekBar fontSizeEditText = findViewById(R.id.seekBar);
        final EditText numScrollLineEditText = findViewById(R.id.num_lines_edit_text);
        final SwitchMaterial mirrorModeToggleButton = findViewById(R.id.mirror_mode_switch);

        fontSizeEditText.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                textView.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        fontSizeEditText.setMax(150);
        fontSizeEditText.setProgress(Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString(FONT_SIZE, "50"))));
        numScrollLineEditText.setText(sharedPreferences.getString(NUM_LINES, "3"));
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (numScrollLineEditText.getText().toString().equals("")) {
                    numScrollLineEditText.setError(getString(R.string.valid_number));
                }
                try {
                    int fontSize = fontSizeEditText.getProgress();
                    int numLinesToScroll = Integer.parseInt(numScrollLineEditText.getText().toString());

                    mirror = mirrorModeToggleButton.isChecked();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(FONT_SIZE, String.valueOf(fontSize));
                    editor.putString(NUM_LINES, String.valueOf(numLinesToScroll));
                    editor.putBoolean(TeleprompterActivity.MIRROR_ENABLED, mirror);
                    editor.apply();
                    Toast.makeText(SettingsActivity.this, "Settings Saved Successfully" + mirror, Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException numberFormatException) {
                    Log.e(MainActivity.TAG, "onClick: NumberFormatException", numberFormatException);

                    numScrollLineEditText.setText(R.string.valid_number);
                    numScrollLineEditText.setTextColor(Color.RED);
                    Toast.makeText(SettingsActivity.this, "Couldn't save the settings.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
