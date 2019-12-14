package com.temboo.example;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

/**
 Copyright 2012, Temboo Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 This is an example Android application distributed with the Temboo Android SDK, intended
 to demonstrate how to use the Temboo SDK to create apps that interact with 3rd party APIs
 and services.

 Visit the "getting started" section of our website at www.temboo.com for the tutorial
 that goes along with this example.

 A simple view class, used to display a video thumbnail, title, and link
 from a YouTube query result.
 */
public class YouTubeResultView extends LinearLayout {

	// The sub-views that will be used to show the thumbnail
	// and link respectively
	TextView linkView;
	ImageView imageView;
	
	public YouTubeResultView(Context context, String title, String url, String thumbnailURL) {
		super(context);
		super.setOrientation(HORIZONTAL);
		
		// Create the link text view, and make the link clickable
		linkView = new TextView(getContext());
		linkView.setMovementMethod(LinkMovementMethod.getInstance());
		
		// Set the link text
		linkView.append(Html.fromHtml("<a href='" + url + "'>" + title + "</a>"));

		// Populate the image view
		try {
			imageView = new ImageView(getContext());
			imageView.setPadding(4, 4, 4, 0);
			fetchDrawableOnThread(thumbnailURL, imageView);
			super.addView(imageView);
		} catch(Exception e) {
			Toast.makeText(getContext(), "An error occurred retrieving the video thumbnail", Toast.LENGTH_LONG).show();	
		}
		super.addView(linkView);
	}

	/**
	 * A utility method to retrieve the thumbnail image on a separate thread, and populate
	 * the image view with that thumbnail
	 * @param urlString - the URL of the thumbnail to retrieve
	 * @param imageView - the view to populate with the thumbnail
	 */
    private void fetchDrawableOnThread(final String urlString, final ImageView imageView) {

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                imageView.setImageDrawable((Drawable) message.obj);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
            	try {
	                DefaultHttpClient httpClient = new DefaultHttpClient();
	                HttpGet request = new HttpGet(urlString);
	                HttpResponse response = httpClient.execute(request);
	                InputStream is = response.getEntity().getContent();	
	                Drawable drawable = Drawable.createFromStream(is, "src");
	                
	                Message message = handler.obtainMessage(1, drawable);
	                handler.sendMessage(message);
            	} catch(Exception e) {
        			Toast.makeText(getContext(), "An error occurred retrieving the video thumbnail", Toast.LENGTH_LONG).show();	
            	}
            }
        };
        thread.start();
    }
}
