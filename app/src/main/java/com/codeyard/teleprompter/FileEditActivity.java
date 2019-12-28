package com.codeyard.teleprompter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
        final FileOperator fileOperator = new FileOperator(FileEditActivity.this);
        Button saveButton = findViewById(R.id.save_file_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = findViewById(R.id.edit_text_file);
                EditText editTextTitle = findViewById(R.id.edit_text_title);
                Toast.makeText(FileEditActivity.this, editText.getText().toString(), Toast.LENGTH_SHORT).show();
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = df.format(c);
                String data = formattedDate + ";" + editText.getText().toString();
                fileOperator.writeToFile(editTextTitle.getText().toString(), data);
                Log.d(TAG, "onClick: data = " + fileOperator.readFromFile(editTextTitle.getText().toString()));

            }
        });

    }

}
