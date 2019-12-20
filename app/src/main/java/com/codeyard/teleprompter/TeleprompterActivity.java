package com.codeyard.teleprompter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import static com.codeyard.teleprompter.MainActivity.TAG;

public class TeleprompterActivity extends Activity {
    private static final String htmlStart = "<html>\n" +
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
    private static final String htmlEnd = "</p>\n" +
            "</body>\n" +
            "</html>";
    @SuppressLint("StaticFieldLeak")
    private static WebView webView;
    @SuppressLint("StaticFieldLeak")
    private static TeleprompterActivity teleprompterActivity;
    private String message;
    private ServerClass serverClass;
    private BrightnessManager brightnessManager;
    private int oldBrightness;

    /**
     * Updates the WebView with the new content
     *
     * @param message The new content
     */
    static void updateContents(final String message) {
        teleprompterActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.contents = message;
                String htmlString = htmlStart + MainActivity.contents + htmlEnd;
                webView.loadData(htmlString, "text/html", "UTF-8");
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        teleprompterActivity = TeleprompterActivity.this;
        setContentView(R.layout.activity_teleprompter);
        webView = findViewById(R.id.scroll);

        //Defining the Html file here. Just add the content between them
        String htmlString = htmlStart + MainActivity.contents + htmlEnd;
        webView.loadData(htmlString, "text/html", "UTF-8");

        brightnessManager = new BrightnessManager();
        oldBrightness = brightnessManager.readBrightness(TeleprompterActivity.this);
        brightnessManager.setBrightness(TeleprompterActivity.this, 100);
        serverClass = new ServerClass();
        Thread mThread = new Thread(serverClass);
        mThread.start();
    }


    private void handleScrolling(final int numLines, final boolean next) {
        int currPosition = webView.getScrollY();
        int textSize = 50;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        brightnessManager.setBrightness(TeleprompterActivity.this, oldBrightness);
        serverClass.stopSocket();

    }

    private void handleMessages(String message) {
        final int numLine = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.NUM_LINES, "3"));
        if (message.contains("NEXT:")) {
            try {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleScrolling(numLine, true);
                    }
                });
                handleScrolling(numLine, true);
            } catch (NumberFormatException nFE) {
                nFE.printStackTrace();
                Log.e(TAG, "handleMessages: Number Format Exception", nFE);
                Toast.makeText(TeleprompterActivity.this, "Number Format Exception", Toast.LENGTH_SHORT).show();
            }
        }
        if (message.contains("BACK:")) {
            try {
                handleScrolling(numLine, false);
            } catch (NumberFormatException nFE) {
                nFE.printStackTrace();
                Log.e(TAG, "handleMessages: Number Format Exception", nFE);
                Toast.makeText(TeleprompterActivity.this, "Number Format Exception", Toast.LENGTH_SHORT).show();
            }
        }

    }

    class ServerClass implements Runnable, Serializable {
        ServerSocket serverSocket;
        Socket socket;
        DataInputStream dataInputStream;
        String receivedData;
        final Handler handler = new Handler();

        void stopSocket() {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8080);
                //noinspection InfiniteLoopStatement
                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    receivedData = dataInputStream.readUTF();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            message = receivedData;
                            handleMessages(message);

                        }
                    });

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}