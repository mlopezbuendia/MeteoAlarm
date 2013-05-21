package com.rwd.weatheralarms;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.rwd.utils.Constants;
import com.rwd.utils.DetailedInfo;
import com.rwd.utils.Item;
import com.rwd.utils.LocationUtils;
import com.rwd.utils.Parser;

public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, 
																  GooglePlayServicesClient.OnConnectionFailedListener,
																  LocationListener {
	
	// Whether the display should be refreshed.
    private static boolean refreshDisplay = true; 
    
    // General Preferences && Location Specific Preferences
    private static SharedPreferences sPref = null;
    private static SharedPreferences locPref = null;
    
    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false; 
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    
    //Buttons references
    private static Button parseButton = null;
    private static Button prefButton = null;
    
    //Location Client using for storing location in the activity
    private LocationRequest mLocationRequest = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
         * Setting location client
         */
        //Create a new location request, using the enclosing class to handle callbacks
        mLocationRequest = LocationRequest.create();
        //Set update interval
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        //Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Sets interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
                
        //Get initial Preferences
        sPref = getSharedPreferences(Constants.GENERAL_PREFERENCES, Context.MODE_PRIVATE);
        locPref = getSharedPreferences(LocationUtils.LOCATION_PREFERENCES, Context.MODE_PRIVATE);
        
        mobileConnected = true;
        
        //Set parse button action
        parseButton = (Button)findViewById(R.id.parseButton);
        parseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Launch xml download
				loadInfo();
			}
		});
        
        //Set prefs button action
        prefButton = (Button)findViewById(R.id.setPreference);
        prefButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Set Default Prefs
				setDefaultPreferences();
			}
		});
    }
    
    /**
     * Called when Activity becomes visible
     */
    @Override
	protected void onStart() {
		super.onStart();
		//Connect the location client
		mLocationClient.connect();
	}

    /**
     * Called when Activity is no longer visible
     */
	@Override
	protected void onStop() {
		//Disconnecting the client invalidates it
		mLocationClient.disconnect();
		super.onStop();
	}




	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		//Decide what to do based on the original request code
		switch(requestCode){
			
		//Call to Google Play
		case Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST:
			
			//If the result code of the activity called is ok, try to connect again
			switch(resultCode){
			
			case Activity.RESULT_OK:
				
				//TODO: try the request again
				break;
				
			}
		}
		
	}
    
    /**
     * Checks if Google Play Services are connected
     * 
     * @return
     */
    private boolean servicesConnected(){
    	
    	boolean result = false;
    	int resultCode = -1;							//For checking availability of google services
    	int errorCode = -1;								//For checking error
    	Dialog errorDialog = null;						//Error dialog from Google Services
    	ErrorDialogFragment errorFragment = null;		//Fragment to show error dialog
    	
    	resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	
    	//If Google Play service is available
    	if (ConnectionResult.SUCCESS == resultCode){
    		//In debug mode, log the status
    		Log.d("Location Updates", "Google Play is available");
    		result = true;
    	}
    	else{
    		//Get error code
    		errorCode = connectionResult.getErrorCode();
    		//Get the error dialog from Google Play Services
    		errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
    		
    		//If Google Play Services can provide an error dialog
    		if(errorDialog != null){
    			//Create a new DialogFragment for the error dialog
    			errorFragment = new ErrorDialogFragment();
    			//Set the dialog in the fragment
    			errorFragment.setDialog(errorDialog);
    			//Show the dialog in the fragment
    			errorFragment.show(getSupportFragmentManager(), "Location Updates");
    		}
    	}
    	
    	return result;
    	
    }


	/**
     * Uses AsyncTask to download the XML with the info
     * 
     */
    private void loadInfo(){
    	
    	Location loc = null;				//For managing current location
    	String prefCon = null;				//Network connection preferred by user
    	
    	//Shows the current location
    	loc = mLocationClient.getLastLocation();
    	Toast.makeText(this, loc.toString(), Toast.LENGTH_LONG).show();
    	
    	//Get network connection preferred
    	prefCon = sPref.getString(Constants.PREF_PREFERRED_CONNECTION, Constants.ANY);
    	
    	if(prefCon.equals(Constants.ANY) && (wifiConnected || mobileConnected)) {
    		new DownloadXmlTask().execute(Constants.URL);
    	}
    	else if(prefCon.equals(Constants.WIFI) && (wifiConnected)){
    		new DownloadXmlTask().execute(Constants.URL);
    	}
    	else{
    		//TODO: Show error in screen 
    		Log.d("ERROR", "Not able to download XML");
    	}
    	
    }
    
    /**
     * Class to implement the async download
     * 
     */
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {

    	@Override
    	protected String doInBackground(String... urls) {
    		
    		//Try to get the Xml formatted from web
    		try {
				return loadXmlFromNetwork(urls[0]);
			//If it doesn't succeed, it shows errors
    		}catch (XmlPullParserException e) {
	                return getResources().getString(R.string.xml_error);
    		} catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } 
    		
    	}

    	@Override
    	protected void onPostExecute(String result){
            setContentView(R.layout.activity_main);
            // Displays the HTML string in the UI via a WebView
            WebView myWebView = (WebView) findViewById(R.id.mainWebView);
            myWebView.loadData(result, "text/html", null);
    	}

    	/**
    	 * Download XML and populate a list with alarm items
    	 * 
    	 * @param url where to download xml
    	 * @return
    	 * @throws IOException
    	 * @throws XmlPullParserException 
    	 */
    	private String loadXmlFromNetwork(String url) throws IOException, XmlPullParserException{
    		
    		InputStream stream = null;					//Represent the xml content to parse
    		Parser parser = null;						//Parser
    		List<Item> items = null;					//All alarm items
    		String title = null;						//Item's title
    		String link = null;							//Item's link
    		DetailedInfo description = null;			//Item's description
    		String pubDate = null;						//Item's publication date
    		String guid = null;							//Item's guid
    		StringBuilder htmlString = null;			//Html with xml content
    		DateFormat formatter = null;				//Formatter for current date
    		Calendar rightNow = null;					//Current Date
    		
    		//Construct html output
    		htmlString = new StringBuilder();
    		htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
    		formatter = new SimpleDateFormat("MMM dd h:mmaa");
    		rightNow = Calendar.getInstance();
    		htmlString.append("<em>" + getResources().getString(R.string.updated) + formatter.format(rightNow.getTime()) + "</em>");
    		
    		//Get stream from url
    		//stream = downloadUrl(url);
    		
    		//Get it from file
    		AssetManager am = getAssets();
    		stream = am.open("es.rss");
    		
    		//Initialize parser
    		parser = new Parser();
    		
    		//Get items from stream
    		try {
				items = parser.parse(stream);
			} catch (XmlPullParserException e) {
				//Nothing special
				Log.d("ERROR", "XmlParserException");
				e.printStackTrace();
			} catch (IOException e) {
				//Nothing special
				Log.d("ERROR", "IO Exception");
				e.printStackTrace();
			}
    		//Taking care: stream must be always closed
    		finally{
    			if(stream != null){
    				stream.close();
    			}
    		}
    		
    		// Parser returns a List (called "items") of Item objects.
    	    // Each Item object represents a alarm for a place in the XML feed.
    	    // This section processes the items list to combine each item with HTML markup.
    	    for (Item item : items) {       
    	        htmlString.append("<p>");
    	        htmlString.append(item.getTitle() + "</p>");
    	    }
    	    return htmlString.toString();
    	}
    	
    	/**
    	 * Given a string representation of a URL, sets up a connection and gets an input stream.
    	 * 
    	 * @param urlString with the rss XML-formatted
    	 * @return
    	 * @throws IOException 
    	 */
    	private InputStream downloadUrl(String urlString) throws IOException{
    		
    		URL url = new URL(urlString);
    	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	    conn.setReadTimeout(10000 /* milliseconds */);
    	    conn.setConnectTimeout(15000 /* milliseconds */);
    	    conn.setRequestMethod("GET");
    	    conn.setDoInput(true);
    	    // Starts the query
    	    conn.connect();
    	    return conn.getInputStream();
    		
    	}

    }

    /**
     * Define a Dialog Fragment where display error dialogs
     * 
     * @author manuel.lopez
     *
     */
    public static class ErrorDialogFragment extends DialogFragment{
    	
    	private Dialog mDialog;			//Handle dialog field
    	
    	/**
    	 * Constructor
    	 */
    	public ErrorDialogFragment(){
    		super();
    		mDialog = null;
    	}
    	
    	/**
    	 * Set the dialog to display
    	 * 
    	 * @param dialog
    	 */
    	public void setDialog (Dialog dialog){
    		this.mDialog = dialog;
    	}
    	
    	/**
    	 * Return a dialog to the Dialog Fragment
    	 */
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState){
    		return mDialog;
    	}
    	
    }

    
    /**
     * Called by Location Services if the attempt to Location Services fails
     */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		/**
		 * Google Play Services can resolve some errors it detects. If the error has a resolution, try sending an Intent
		 * to start a Google Play services activity that can resolve error.
		 */
		if(result.hasResolution()){
			try{
				//Start an Activity that tries to resolve the error
				result.startResolutionForResult(this, Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original PendingIntent
				 */
			} catch(IntentSender.SendIntentException e){
				//Log the error
				e.printStackTrace();
			}
		}
		else{
			//If no resolution is available, display a dialog to the user with the error
			showErrorDialog(result.getErrorCode());
		}
	}

	/**
	 * Called by Location Services when the request to connect the client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		//Display the connection status
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called by Location Services if the connection to the location client drops because of an error.
	 * 
	 */
	@Override
	public void onDisconnected() {
		//Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect", Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Set default application preferences into Shared Preferences
	 * 
	 */
	private void setDefaultPreferences(){
		
		//Get the shared preferences' editor
		SharedPreferences.Editor editor = sPref.edit();
		editor.putString(Constants.PREF_PREFERRED_CONNECTION, Constants.ANY);
		editor.commit();
	}
    
}
