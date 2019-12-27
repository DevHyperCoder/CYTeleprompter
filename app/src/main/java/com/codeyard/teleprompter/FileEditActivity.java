package com.codeyard.teleprompter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.codeyard.teleprompter.MainActivity.TAG;

public class FileEditActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_edit);
        Toolbar toolbar = findViewById(R.id.file_edit_tool);
        setSupportActionBar(toolbar);
        Button saveButton = findViewById(R.id.save_file_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = findViewById(R.id.edit_text_file);
                EditText editTextTitle = findViewById(R.id.edit_text_title);
                Toast.makeText(FileEditActivity.this, editText.getText().toString(), Toast.LENGTH_SHORT).show();
                try {
                    FileOutputStream fileOut = openFileOutput(editTextTitle.getText().toString() + ".txt", MODE_PRIVATE);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(fileOut);
                    Date c = Calendar.getInstance().getTime();
                    Log.d(MainActivity.TAG, "onClick: date" + c);
                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String formattedDate = df.format(c);
                    String data = formattedDate + ";" + editText.getText().toString();
                    outputWriter.write(data);
                    outputWriter.close();
                    Log.d(TAG, "onClick: data" + data);
                    //display file saved message
                    Toast.makeText(getBaseContext(), "File saved successfully!",
                            Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                editText.setText("");
                Toast.makeText(FileEditActivity.this, editTextTitle.getText().toString() +
                        ".txt saved to Internal Storage...", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
