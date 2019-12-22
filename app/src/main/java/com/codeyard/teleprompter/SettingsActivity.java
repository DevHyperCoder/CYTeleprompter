package com.codeyard.teleprompter;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    static final String NUM_LINES = "NUM_LINES";
    private static final String FONT_SIZE = "FONT-SIZE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final EditText fontSizeEditText = findViewById(R.id.editTextFontSize);
        final EditText numScrollLineEditText = findViewById(R.id.num_lines_edit_text);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        fontSizeEditText.setText(sharedPreferences.getString(FONT_SIZE, "50"));
        numScrollLineEditText.setText(sharedPreferences.getString(NUM_LINES, "3"));
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fontSizeEditText.getText().toString().equals("")) {//if it s empty display in red color
                    fontSizeEditText.setError(getString(R.string.valid_font_size_prompt));

                }
                if (numScrollLineEditText.getText().toString().equals("")) {
                    numScrollLineEditText.setError(getString(R.string.valid_number));
                }
                try {
                    int fontSize = Integer.parseInt(fontSizeEditText.getText().toString());
                    int numLinesToScroll = Integer.parseInt(numScrollLineEditText.getText().toString());
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(FONT_SIZE, String.valueOf(fontSize));
                    editor.putString(NUM_LINES, String.valueOf(numLinesToScroll));
                    editor.apply();
                    Toast.makeText(SettingsActivity.this, "Settings Saved Successfully", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException numberFormatException) {
                    Log.e(MainActivity.TAG, "onClick: NumberFormatException", numberFormatException);
                    fontSizeEditText.setText(R.string.valid_font_size_prompt);
                    fontSizeEditText.setTextColor(Color.RED);
                    numScrollLineEditText.setText(R.string.valid_number);
                    numScrollLineEditText.setTextColor(Color.RED);
                    Toast.makeText(SettingsActivity.this, "Couldn't save the settings.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
