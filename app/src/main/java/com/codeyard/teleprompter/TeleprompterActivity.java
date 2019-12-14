package com.codeyard.teleprompter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.codeyard.teleprompter.MainActivity.TAG;

public class TeleprompterActivity extends Activity {
    WebView webView;
    String message;
    //Defining the Html file here. Just add the content between them
    String textSize = "";
    String htmlStart = "<html>\n" +
            "<style type=\"text/css\">\n" +
            "body{\n" +
            "background-color:#000000\n" +
            "}\n" +
            "p{\n" +
            "color: #FFFFFF;\n" +
            "font-family: verdana;\n" +
            "\t\t\tfont-size: 50px;\n" +
            "\t\t\t transform: scale(-1, 1);\n" +//This is the one that mirrors the text
            "}\n" +
            "</style>\n" +
            "<body>\n" +
            "<p>";
    String htmlEnd = "</p>\n" +
            "</body>\n" +
            "</html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleprompter);
        webView = findViewById(R.id.scroll);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TeleprompterActivity.this);
        textSize = sharedPreferences.getString(SettingsActivity.FONT_SIZE, "50");
        String htmlString = htmlStart + MainActivity.contents + htmlEnd;
        webView.loadData(htmlString, "text/html", "UTF-8");
        Thread mThread = new Thread(new ServerClass());
        mThread.start();
    }

//    String generateHtml() {
//        return htmlStart + textSize + htmlAfterTextSize + MainActivity.contents + htmlEnd;
//    }

    void handleScrolling(final int numLines, final boolean next) {
        int currPosition = webView.getScrollY();
        int textSize = 36;
        if (next) {
            int newPos = currPosition + (textSize * numLines);
            webView.scrollTo(0, newPos);
        }
        if (!next) {
            int newPos = currPosition - (textSize * numLines);
            if (newPos < 0) {
                newPos = 0;
            }
            webView.scrollTo(0, newPos);
        }


    }

    void handleMessage(String message) {
        Log.e(TAG, "handleMessage: " + message);
        if (message.contains("NEXT:")) {
            try {
                final int numLine = Integer.parseInt(message.substring(5));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleScrolling(numLine, true);
                    }
                });
                handleScrolling(numLine, true);
            } catch (NumberFormatException nFE) {
                nFE.printStackTrace();
                Toast.makeText(TeleprompterActivity.this, "Number Format Exception", Toast.LENGTH_SHORT).show();
            }
        }
        if (message.contains("BACK:")) {
            try {
                int numLine = Integer.parseInt(message.substring(5));
                handleScrolling(numLine, false);
            } catch (NumberFormatException nFE) {
                nFE.printStackTrace();
                Toast.makeText(TeleprompterActivity.this, "Number Format Exception", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public class ServerClass implements Runnable {
        ServerSocket serverSocket;
        Socket socket;
        DataInputStream dataInputStream;
        String receivedData;
        Handler handler = new Handler();

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8080);
                Log.e("TELE", "WAITIN FOR CLIENT");
                //noinspection InfiniteLoopStatement
                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    receivedData = dataInputStream.readUTF();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            message = receivedData;
                            handleMessage(message);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}