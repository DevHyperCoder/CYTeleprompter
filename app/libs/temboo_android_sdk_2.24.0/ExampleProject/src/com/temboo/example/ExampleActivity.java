package com.temboo.example;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

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
*/
public class ExampleActivity extends Activity {
	
	private TextView nytResultsView;
	private EditText movieNameInput;
	private LinearLayout youTubeResultsList;
	
    public static ErrorHandler errorHandler;

    /**
     * The onCreate method is automatically called when this activity is first created.
     * Obtain references to relevant UI objects, and attach a click-handler to the UI button.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Obtain references to the View objects that we'll use to display results
        // from YouTube and the New York Times.
        nytResultsView = (TextView) findViewById(R.id.nytResultsView);
        youTubeResultsList = (LinearLayout) findViewById(R.id.youtubeResultsList);

        // Specify that links in the NYT results text view should be clickable
        nytResultsView.setMovementMethod(LinkMovementMethod.getInstance());
        
        // Obtain a reference to the "movie name" input field
        movieNameInput = (EditText) findViewById(R.id.movieName);
        
        // Create a handler, that will be used to pass messages from task
        // threads back to the UI thread.
        errorHandler = new ErrorHandler();
        
        try {

        	// Obtain a reference to the "go" button, and attach a click listener
        	Button goButton = (Button)this.findViewById(R.id.goButton);
	      	goButton.setOnClickListener(new OnClickListener() {

	      		// Define the action to perform when the button is clicked
	      		@Override
	      	    public void onClick(View v) {
	      	    	
	      	    	// Validate that a reasonable movie name has been submitted; if the 
	      			// "movie name" input is empty, show an error message
	      	    	if(movieNameInput.getText().toString().length() < 1) {
	    	    	    Message msg = ExampleActivity.errorHandler.obtainMessage();
	    	    	    msg.obj = "Please enter a valid movie title!";
	    	    	    ExampleActivity.errorHandler.sendMessage(msg);	
	      	    		return;
	      	    	}
	      	    	
	      	    	// If the input passes validation, clear the Views that will be used to
	      	    	// display New York Times and YouTube results.
	      	    	nytResultsView.setText("Waiting for results...");
	      	    	youTubeResultsList.removeAllViews();
	      	    	
	      	    	// Launch the task threads that will actually fetch and display data
	      	    	// from the New York Times and Youtube.
	      	    	new NYTReviewFetcherTask().execute(null, null, null);
	      	    	new YouTubeFetcherTask().execute(null, null, null);
	      	    }
	      	}); 
        } catch(Exception e) {
        	// if an exception occurred, show an error message
    	    Message msg = ExampleActivity.errorHandler.obtainMessage();
    	    msg.obj = e.getMessage();
    	    ExampleActivity.errorHandler.sendMessage(msg);	
        }
    }
    
    
    /**
     * An AsyncTask that will be used to retrieve and display movie review data
     * from the New York Times.
     */
    private class NYTReviewFetcherTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
	    	try {
	    		
	    		// Add New York Times Choreo code here!
	    		
	    	} catch(Exception e) {
	        	// if an exception occurred, show an error message
	    	    Message msg = ExampleActivity.errorHandler.obtainMessage();
	    	    msg.obj = e.getMessage();
	    	    ExampleActivity.errorHandler.sendMessage(msg);
	    	}
			return null;
		}
		
		protected void onPostExecute(String result) {
			try {

				// Add New York Times parsing/display code here!
				
			} catch(Exception e) {
	        	// if an exception occurred, show an error message
	    	    Message msg = ExampleActivity.errorHandler.obtainMessage();
	    	    msg.obj = e.getMessage();
	    	    ExampleActivity.errorHandler.sendMessage(msg);			}
		}	
    }
    
       
    /**
     * An AsyncTask that will be used to retrieve and display video query
     * results from Youtube.
     */
    private class YouTubeFetcherTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			try {

				// Add YouTube Choreo code here!

			} catch(Exception e) {
	        	// if an exception occurred, show an error message
	    	    Message msg = ExampleActivity.errorHandler.obtainMessage();
	    	    msg.obj = e.getMessage();
	    	    ExampleActivity.errorHandler.sendMessage(msg);	
	    	}
			return null;
		}
		
		protected void onPostExecute(String result) {

			try {
				
				// Add YouTube parsing/display code here!

			} catch(Exception e) {
	        	// if an exception occurred, show an error message
	    	    Message msg = ExampleActivity.errorHandler.obtainMessage();
	    	    msg.obj = e.getMessage();
	    	    ExampleActivity.errorHandler.sendMessage(msg);			}
		} 	
    }
 
    
    /**
     * A simple utility Handler to display an error message as a Toast popup
     * @param errorMessage
     */
    
    private class ErrorHandler extends Handler {
        @Override
        public void handleMessage(Message msg)
        {
    		Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();	        	
        }
    }
}