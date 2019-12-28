package com.codeyard.teleprompter;

import android.annotation.SuppressLint;
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
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

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

            "\t\t\tfont-size: ";
    private static final String fontSizeBreakEnd = "px;\n";
    private static final String htmlEnd = "</p>\n" +
            "</body>\n" +
            "</html>";
    static final String MIRROR_ENABLED = "MIRROR_ENABLED";
    private static final String mirrorHTML = "\t\t\t transform: scale(-1, 1);\n";//This is the one that mirrors the text
    private static final String afterMirrorHTML = "}\n" +
            "</style>\n" +
            "<body>\n" +
            "<p>";
    private static boolean mirror;
    private static String font_size;
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
                Log.d(TAG, "run: " + generateHTML(mirror, font_size, message));
                webView.loadData(generateHTML(mirror, font_size, message), "text/html", "UTF-8");
            }
        });

    }

    private static String generateHTML(boolean mirrorMode, String fontSize, String contents) {
        if (mirrorMode) {
            return htmlStart + fontSize + fontSizeBreakEnd + mirrorHTML + afterMirrorHTML + contents + htmlEnd;
        }
        return htmlStart + fontSize + fontSizeBreakEnd + afterMirrorHTML + contents + htmlEnd;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        teleprompterActivity = TeleprompterActivity.this;
        setContentView(R.layout.activity_teleprompter);
        webView = findViewById(R.id.scroll);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TeleprompterActivity.this);

        mirror = sharedPreferences.getBoolean(MIRROR_ENABLED, true);
        font_size = sharedPreferences.getString(SettingsActivity.FONT_SIZE, "50");
        Log.d(TAG, "onCreate: " + font_size + " " + mirror);

        webView.loadData(generateHTML(mirror, font_size, MainActivity.contents), "text/html", "UTF-8");

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
        final int numLine = Integer.parseInt(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.NUM_LINES, "3")));
        if (message.contains("NEXT:")) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleScrolling(numLine, true);
                    }
                });
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
        final Handler handler = new Handler();
        ServerSocket serverSocket;
        Socket socket;
        DataInputStream dataInputStream;
        String receivedData;

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