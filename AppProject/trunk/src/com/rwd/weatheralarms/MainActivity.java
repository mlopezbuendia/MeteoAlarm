package com.rwd.weatheralarms;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.rwd.utils.Constants;
import com.rwd.utils.Item;
import com.rwd.utils.LocationUtils;
import com.rwd.utils.Parser;

public class MainActivity extends FragmentActivity implements LocationListener, 
																  GooglePlayServicesClient.ConnectionCallbacks, 
																  GooglePlayServicesClient.OnConnectionFailedListener  {
	
    // General Preferences && Location Specific Preferences
    private SharedPreferences sPref = null;
    private SharedPreferences locPref = null;
    
    //Current Province or Sub-Admin Area
    private String currentProvince = null;
    
    //Task handlers
    private AsyncTask downloadXmlHandler = null;
    private AsyncTask getAddressHandler = null;
    
    // Whether there is a Wi-Fi connection.
    private boolean wifiConnected = false; 
    // Whether there is a mobile connection.
    private boolean mobileConnected = false;
    
    //Location Parameters and Client using for storing location in the activity
    private LocationRequest mLocationRequest = null;
    private LocationClient mLocationClient = null;
    
    //UI Handlers
    private ProgressBar mProgressBar;
    private TextView mLastUpdate;
    private TextView mAddress;
    private WebView myWebView;    
    private Button parseButton = null;
    private Button prefButton = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Getting UI elements' references 
        getUIElements();
        
        //Setting location client and parameters
        iniLocation();
                
        //Get initial Preferences
        sPref = getSharedPreferences(Constants.GENERAL_PREFERENCES, Context.MODE_PRIVATE);
        locPref = getSharedPreferences(LocationUtils.LOCATION_PREFERENCES, Context.MODE_PRIVATE);
        
        mobileConnected = true;
        
        //Load current alerts on startup (like "Refresh")
        //loadInfo();

    }
    
    /**
     * Loads the references for each UI Element of the application
     */
    private void getUIElements() {
		
    	/* Buttons */
    	parseButton = (Button)findViewById(R.id.MAparseButton);
    	//Set parse button action
    	parseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Launch xml download
				loadInfo();
			}
		});
        
        
        prefButton = (Button)findViewById(R.id.MAsetPreference);
        //Set prefs button action
        prefButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Set Default Prefs
				setDefaultPreferences();
			}
		});
        /* END Buttons */ 
    	
    	//Spinning progress bar
    	mProgressBar = (ProgressBar) findViewById(R.id.MAinitialLoading);
    	
    	/* Xml date */
    	mLastUpdate = (TextView) findViewById(R.id.MAxmlDate);
    	//Set initial text
    	mLastUpdate.setText(getString(R.string.UIE_xml_date));
    	/* END Xml date */
    	
    	//Province    	
    	mAddress = (TextView) findViewById(R.id.MAaddress);
    	//Set initial text
    	mAddress.setText(getString(R.string.LCI_address_output_string));
    	
    	//WebView with alarms info
    	myWebView = (WebView) findViewById(R.id.MAmainWebView);
	}

	/**
     * Set location parameters and return a location client into the global variables
     * 
     * @return location client
     */
    private void iniLocation() {
    	
    	//Create a new location parameters, using the enclosing class to handle callbacks
        mLocationRequest = LocationRequest.create();
        //Set update interval
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        //Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Sets interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        
        //Create a new location client, using the enclosing class to handle callbacks
        mLocationClient = new LocationClient(this, this, this);
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
    
	/**
	 * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
	 */
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		//Decide what to do based on the original request code
		switch(requestCode){
			
			//Call to Google Play in the method onConnectionFailed
			case Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST:
			
				switch(resultCode){
			
				//If the result code of the activity called is ok, try to connect again
				case Activity.RESULT_OK:
				
					//Log the result
					Log.d(Constants.APP_TAG, getString(R.string.GPC_resolved));
						
					break;
				
				//If any other result was returned by Google Play Services...
				default:
					
					//Log the result
					Log.d(Constants.APP_TAG, getString(R.string.GPC_no_resolution));
					
					//Display the result in Toast
					Toast.makeText(this, getString(R.string.GPC_no_resolution), Toast.LENGTH_SHORT).show();
				
					break;
			}
				
			//If any other request code was received
			default:
				
				//Report that this Activity received an unknown requestCode
				Log.d(Constants.APP_TAG_ERROR, getString(R.string.EEG_unknown_activity_request_code, requestCode));
				
				break;
		}
		
	}
    
    /**
     * Checks if Google Play Services is connected
     * 
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected(){
    	
    	boolean result = false;
    	int resultCode = -1;							//For checking availability of google services
   	
    	resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	
    	//If Google Play service is available
    	if (ConnectionResult.SUCCESS == resultCode){
    		//In debug mode, log the status
    		Log.d(Constants.APP_TAG, getString(R.string.GPC_play_services_available));
    		
    		//Continue
    		result = true;
    	}
    	//Google Play Services is not available for some reason
    	else{
    		//Get the error dialog from Google Play Services and show the error
    		showErrorDialog(resultCode);
    		
    		result = false;
    	}
    	
    	return result;
    	
    }


	/**
     * Uses AsyncTask to download the XML with the info
     * 
     */
    private void loadInfo(){
    	
    	String prefCon = null;				//Network connection preferred by user
    	
    	//Show we are in
    	Log.d(Constants.APP_TAG_ERROR, "SHOW");
    	
    	//Show progress bar
    	mProgressBar.setVisibility(LinearLayout.VISIBLE);
    	
    	//Show current city
    	getAddress();
    	
    	//Get network connection preferred
    	prefCon = sPref.getString(Constants.PREF_PREFERRED_CONNECTION, Constants.ANY);
    	
    	//TODO: CONDITIONAL OPTIONS
    	if(prefCon.equals(Constants.ANY) && (wifiConnected || mobileConnected)) {
    		downloadXmlHandler = (new DownloadXmlTask(this)).execute(Constants.URL);
    	}
    	else if(prefCon.equals(Constants.WIFI) && (wifiConnected)){
    		downloadXmlHandler = (new DownloadXmlTask(this)).execute(Constants.URL);
    	}
    	else{ 
    		Log.d(Constants.APP_TAG_ERROR, getString(R.string.EEG_Xml_Not_Downloaded));
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
	 * Report location updates to the UI
	 * 
	 * @param location The updated location
	 */
	@Override
	public void onLocationChanged(Location location) {

		//Report to the UI that the location was updated
		Toast.makeText(this, R.string.LCI_location_updated, Toast.LENGTH_SHORT).show();
		
		//Show the new location
		//TODO: CALL TO REFRESH DATA AND UI
	}

	/**
	 * Called by Location Services when the request to connect the client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		//Display the connection status
		//TODO: START PERIODIC UPDATES??
	}

	/**
	 * Called by Location Services if the connection to the location client drops because of an error.
	 * 
	 */
	@Override
	public void onDisconnected() {
		//Display the connection status
		Toast.makeText(this, getString(R.string.LCI_location_disconnected), Toast.LENGTH_SHORT).show();
		//TODO: REFRESH DATA / RETRY CONNECTION ???
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
	
	/**
	 * Show a dialog returned by Google Play Services for the connection error code
	 * 
	 * @param errorCode An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode){
		
		Dialog errorDialog = null;						//Dialog where to show the error
		ErrorDialogFragment errorFragment = null;		//Fragment where to show the error dialog
		
		//Get the error dialog from Google Play Services
		errorDialog = GooglePlayServicesUtil.getErrorDialog(
				errorCode,
				this,
				Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
				
		//If Google Play services can provide an error dialog
		if(errorDialog != null){
			
			//Create a new DialogFragment in which to show the error dialog
			errorFragment = new ErrorDialogFragment();
			
			//Set the dialog in the Dialog Fragment
			errorFragment.setDialog(errorDialog);
			
			//Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(), Constants.APP_TAG);
		}
	}
	
	/**
	 * Get the address of the current location, using reverse geocoding. This only works if a geocoding service is available
	 *  
	 */
	//For Eclipse with ADT, suppress warnings about Geocoder.isPresent()
	@SuppressLint("NewApi")
	private void getAddress(){
		
		Location loc = null;				//Current location
		
		//In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available
		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) && !(Geocoder.isPresent())){
			//No geocoder is present. Issue an error message
			Toast.makeText(this, R.string.LCI_no_geocoder_available, Toast.LENGTH_LONG).show();
			return;
		}
		
    	//Shows the current location if Google Play Services is available
    	if(servicesConnected()){
    		
    		//Get the current location
    		loc = mLocationClient.getLastLocation();
    		
    		//Start the background task to retrieve the current city
    		getAddressHandler = (new MainActivity.GetAddressTask(this)).execute(loc);
    	}
		
	}
	
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * **********************************************************************
	 * ********************** SUBCLASSES ************************************
	 * **********************************************************************
	 */
	
    /**
     * Class to implement the async download
     * 
     */
    protected class DownloadXmlTask extends AsyncTask<String, Void, String> {

    	private String lastUpdate = "";				//Publication Date info for current province
    	Context localContext = null;							//Stores the context when the Activity instantiates it
    	
    	/**
    	 * Constructor
    	 * 
    	 * @param context
    	 */
    	public DownloadXmlTask(Context context){
    		//Required by the semantics of AsyncTask
			super();
			
			//Set a context for the background task
			localContext = context;
    	}
    	
    	@Override
    	protected String doInBackground(String... urls) {
    		
    		//Try to get the Xml formatted from web
    		try {
				return loadXmlFromNetwork(urls[0]);
			//If it doesn't succeed, it shows errors
    		}catch (XmlPullParserException e) {
	                return getResources().getString(R.string.EEG_xml_error);
    		} catch (IOException e) {
                return getResources().getString(R.string.EEG_connection_error);
            } 
    		
    	}

    	@Override
    	protected void onPostExecute(String result){
            
            // Displays the HTML string in the UI via a WebView
            myWebView.loadData(result, "text/html", null);
            
            //Show publication date
            mLastUpdate.setText(lastUpdate);
            
            //Hide Progress Bar
            mProgressBar.setVisibility(LinearLayout.INVISIBLE);
                   
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
    		StringBuilder htmlString = null;			//Html with xml content
    		DateFormat formatter = null;				//Formatter for current date
    		Calendar rightNow = null;					//Current Date
    		
    		//Construct html output
    		htmlString = new StringBuilder();
    		htmlString.append("<h3>" + getResources().getString(R.string.UIE_page_title) + "</h3>");
    		formatter = new SimpleDateFormat(Constants.FORMATTER, new Locale(Constants.LOCALE_LANGUAGE, Constants.LOCALE_COUNTRY));
    		rightNow = Calendar.getInstance();
    		htmlString.append("<em>" + getResources().getString(R.string.UIE_updated) + formatter.format(rightNow.getTime()) + "</em>");
    		
    		//Get stream from url
    		stream = downloadUrl(url);
    		    		
    		//Initialize parser
    		parser = new Parser();
    		
    		//Get items from stream
    		try {
				items = parser.parse(stream);
			} catch (XmlPullParserException e) {
				//Nothing special
				Log.d(Constants.APP_TAG_EXCEPTION, getString(R.string.EEG_Xml_Parser_Exception));
				e.printStackTrace();
			} catch (IOException e) {
				//Nothing special
				Log.d(Constants.APP_TAG_EXCEPTION, getString(R.string.EEG_IO_Exception));
				e.printStackTrace();
			}
    		//Taking care: stream must be always closed
    		finally{
    			if(stream != null){
    				stream.close();
    			}
    		}
    		
    		//Get current province from getAddressTask
    		currentProvince = getCurrentProvince();
   		
    		//If we got the current province without errors
    		if (currentProvince != null){
        		/* Parser returns a List (called "items") of Item objects.
        	    * Each Item object represents a alarm for a place in the XML feed.
        	    * This section processes selects the item for current province
        	    */
        	    for (Item item : items) {
        	    	//We are only interested in current province
        	    	if(item.getTitle().equals(currentProvince)){
        	    		//Get province alert info
        	    		htmlString.append("<p>");
        	    		htmlString.append(item.getTitle() + "</p>");
        	    		
        	    		//Get publication date and show it in last update 
        	    		lastUpdate = item.getPubDate();
        	    	}
        	    }
    		}
    		//...if there was some errors retrieving current location, show an error in html view
    		else{
    			htmlString.append("<p>Unknown location</p>");
    		}
	    	   		
    	    return htmlString.toString();
    	}
    	
    	/**
    	 * This methods waits for getAddressTask to be completed if it is necessary and after that returns the current
    	 * province from the result of the task
    	 * It waits a maximum of 5 cycles of 1 second each one
    	 * 
    	 * @return null it there was an error with getAddressTask or the name of the current province based on current location
    	 */
    	private String getCurrentProvince(){
    		
    		int cycle = -1;					//Number of cycles to be waiting the current location
    		String result = null;
    		
    		//Set max number of cycles
    		cycle = Constants.LOC_MAX_CYCLES_WAIT;
    		
    		//Checks if getAddressTask is finished
    		while ((cycle > 0) && (getAddressHandler.getStatus() == AsyncTask.Status.RUNNING)){
	    		try {
	    			//If it is running, wait for 1 seconds
					Thread.sleep(Constants.LOC_CYCLE_TIME_MILLIS);
					cycle--;
				} catch (InterruptedException e) {
					//Register log and print stacktrace
					Log.e(Constants.APP_TAG_EXCEPTION, getString(R.string.EEG_interrupted_exception));
					e.printStackTrace();
				}
    		}    		
    		
    		//If task is still running
    		if((getAddressHandler.getStatus() == AsyncTask.Status.RUNNING)){
    			//TODO: que hacer cuando no hay manera de sacar la localización.....
    		}
    		//If we already have current location it is stored in mAddress text view
    		else{

    			result = mAddress.getText().toString();

    		}
    		
    		return result;
	    	
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
	 * An AsyncTask that calls getFromLocation() in the background.
	 * The class uses the following generic types:
	 * Location - A {@link android.location.Location} object containing the current location,
	 * 			  passed as the input parameter to doInBackground()
	 * Void		- indicates that progress units are not used by this subclass
	 * String	- An address passed to onPostExecute()
	 * 
	 * @author manuel.lopez
	 *
	 */
	protected class GetAddressTask extends AsyncTask<Location, Void, String>{

		//Store the context passed to the AsynTask when the system instantiates it
		Context localContext;
		
		/**
		 * Constructor called by the system to instantiate the task 
		 * 
		 * @param context
		 */
		public GetAddressTask(Context context) {
			
			//Required by the semantics of AsyncTask
			super();
			
			//Set a context for the background task
			localContext = context;
		}


		/**
		 * Get a geocoding service instance, pass latitude and longitude to it, format the returned address, and return
		 * the address to the UI thread
		 * 
		 */
		@Override
		protected String doInBackground(Location... params) {
			
			Geocoder geocoder = null;					//Geocoder for get address from location
			Location location = null;					//Location passed by parameters
			List<Address> addresses = null;				//List to contain the result address
			Address address = null;						//Each one of the addresses' item
			String result = null;
			
			//Get a new geocoding service instance, set for localized addresses.  
			geocoder = new Geocoder(localContext, Locale.getDefault());
			
			//Get the current location from the input parameter list
			location = params[0];
			
			//Try to get an address for the current location. Catch IO or network problems
			try{
				
				/*
				 * Call the synchronous getFromLocation() method with the latitude and longitude of the current location.
				 * Return at most 1 address
				 */
				addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			
			//Catch network or other I/O problems
			}catch (IOException exception1){
				
				//Log an error and return an error message
				Log.e(Constants.APP_TAG_EXCEPTION, getString(R.string.EEG_IO_Exception));
				
				//Print the stack trace
				exception1.printStackTrace();
				
				//Return an error message
				result = getString(R.string.EEG_IO_Exception);
			
			//Catch incorrect latitude or longitude values
			}catch (IllegalArgumentException exception2){
				
				//Construct a message containing the invalid arguments
				result = getString(R.string.EEG_illegal_argument_exception) + 
						           String.valueOf(location.getLatitude()) + 
						           String.valueOf(location.getLongitude());
				
				//Log the error and print the stack trace
				Log.e(Constants.APP_TAG_EXCEPTION, result);
				exception2.printStackTrace();
			}
			
			//If the reverse geocode returned an address
			if((addresses != null) && (addresses.size() > 0)){
				
				//Get the first address (we forced only 1 result)
				address = addresses.get(0);
				
				//We are only interested in the "subAdminArea" or "Provincia"
				result = address.getSubAdminArea();
							  				
				//Debugger info for the selected address
				Log.d(Constants.APP_TAG, result);
			}
			//If there aren't any address, post a message
			else{
				result = getString(R.string.LCI_no_address_found);
			}
			
			return result;
		}


		/**
		 * A method that's called once doInBackground() completes. Set the text of the UI element that displays the address.
		 * This method runs on the UI thread.
		 */
		@Override
		protected void onPostExecute(String result) {

			//Set the address in the UI
			mAddress.setText(result);
			
		}	
		
	}
    
}
