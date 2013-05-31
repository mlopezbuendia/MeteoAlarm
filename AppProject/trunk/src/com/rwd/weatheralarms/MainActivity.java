package com.rwd.weatheralarms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
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
import android.util.SparseIntArray;
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
import com.rwd.database.ItemsDAO;
import com.rwd.utils.AwarenessModel;
import com.rwd.utils.Constants;
import com.rwd.utils.Item;
import com.rwd.utils.LevelsModel;
import com.rwd.utils.LocationUtils;
import com.rwd.utils.Parser;

public class MainActivity extends FragmentActivity implements LocationListener, 
																  GooglePlayServicesClient.ConnectionCallbacks, 
																  GooglePlayServicesClient.OnConnectionFailedListener  {
	
    // General Preferences, Location Specific Preferences && Awareness and Levels used in alarms 
    private SharedPreferences sPref = null;
    private SharedPreferences locPref = null;
    private SharedPreferences awarenessPref = null;
    private SharedPreferences levelsPref = null;
    
    //Current Province or Sub-Admin Area
    private String currentProvince = null;
    
    //Task handlers
    private AsyncTask<String, Void, String> downloadXmlHandler = null;
    private AsyncTask<Location, Void, String> getAddressHandler = null;
    
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
    private TextView mInfoCountryAlarms;
    private Button mGoToCountryAlarms;
	
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
        awarenessPref = getSharedPreferences(Constants.AWARENESS_PREFERENCES, Context.MODE_PRIVATE);
        levelsPref = getSharedPreferences(Constants.LEVELS_PREFERENCES, Context.MODE_PRIVATE);
        
        //Check if initial values for awarenesses and levels are set. If not, set them
        if(!awarenessesAreSet()){
        	setDefaultAwarenesses();
        }
        
        if(!levelsAreSet()){
        	setDefaultLevels();
        }
        
        mobileConnected = true;
        
        //TODO: ???
        //Load current alerts on startup (like "Refresh")
        //loadInfo();

    }
    
    /**
     * Store default alarm levels into Shared Preferences
     */
    private void setDefaultLevels() {
    	
    	Editor editor = null;				//Levels SharedPreferences editor
    	
    	//Get the shared preferences editor
    	editor = levelsPref.edit();
    	
    	//Load all levels values from array defined in LevelsModel class
    	for (int i = 1; i <= LevelsModel.NUM_LEVELS; i++){
    		editor.putString(String.valueOf(i), LevelsModel.allLevels[i-1]);
    	}
    	
    	//Commit new values
    	editor.commit();
    	
	}

	/**
     * Inform if default values are stored for alarm levels
     * 
     * @return true if there are default values into Levels Shared Preferences
     */
    private boolean levelsAreSet() {

    	boolean result = false;
    	String check = null;			//getString returned value
    	
    	//Look for description linked to value 1. If it is not stored, getString will return the second parameter value
    	check = levelsPref.getString("1", LevelsModel.NO_LEVELS_SET);
    	if (!check.equals(LevelsModel.NO_LEVELS_SET)){
    		result = true;
    	}
    	
		return result;
    	
    }

	/**
     * Store default awareness types into Shared Preferences
     */
    private void setDefaultAwarenesses() {

    	Editor editor = null;				//Awareness SharedPreferences editor
    	
    	//Get the shared preferences editor
    	editor = awarenessPref.edit();
    	
    	//Load all awareness values from array defined in AwarenessModel class
    	for (int i = 1; i <= AwarenessModel.NUM_AWARENESS; i++){
    		editor.putString(String.valueOf(i), AwarenessModel.allAwareness[i-1]);
    	}
    	
    	//Commit new values
    	editor.commit();
	}

	/**
     * Inform if default values are stored for Awareness types
     * 
     * @return true if there are default values into Awareness Shared Preferences
     */
    private boolean awarenessesAreSet() {
		
    	boolean result = false;
    	String check = null;			//getString returned value
    	
    	//Look for description linked to value 1. If it is not stored, getString will return the second parameter value
    	check = awarenessPref.getString("1", AwarenessModel.NO_AWARENESS_SET);
    	if (!check.equals(AwarenessModel.NO_AWARENESS_SET)){
    		result = true;
    	}
    	
		return result;
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
    	/* END Xml date */
    	
    	//Province    	
    	mAddress = (TextView) findViewById(R.id.MAaddress);
    	
    	//WebView with alarms info
    	myWebView = (WebView) findViewById(R.id.MAmainWebView);
    	
    	/* Country Alarm's info */
    	
    	//Get TextView 
    	mInfoCountryAlarms = (TextView) findViewById(R.id.MAinfoCountry);
    	   	  	
    	//Get Button, set onclick and make it invisible
    	mGoToCountryAlarms = (Button) findViewById(R.id.MAgotoCountry);
    	mGoToCountryAlarms.setVisibility(LinearLayout.INVISIBLE);
    	mGoToCountryAlarms.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO: Go to second activity with alarms for current country
			}
		});
    	
    	/* END Country Alarm's info */
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
		
		//TODO: Debug mode, insert default awareness and levels
		setDefaultAwarenesses();
		setDefaultLevels();
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
	
	/**
	 * Update UI Elements after parsing Task has ended. If currentProvince is null, show error in screen (done in BuildWebViewContent)
	 * It also shows info for current country if there are some alarms for it.
	 * 
	 * @param result is the date of the last version info
	 */
	private void updateUIElements(String result){
		
		String htmlString = null;				//WebView's content
		int totalAlarms = -1;					//Number of alarms stored for the current country
		
		//Get Current Location Alarm's Info
		htmlString = buildWebViewContent();
		
		//Displays the HTML string for current location's alarms in the UI via a WebView
        myWebView.loadData(htmlString, "text/html", null);
        
        //Get the number of alarms stored in database
        totalAlarms = numCountryAlarms();
        
        //Show Country Alarms Text
        
        //If no alarms for the current country, inform...
        if(totalAlarms == 0){
        	mInfoCountryAlarms.setVisibility(LinearLayout.VISIBLE);
        	mInfoCountryAlarms.setText(R.string.UIE_country_no_alarms);
        }
        //If we got -1 something went wrong when reading from database
        else if (totalAlarms == -1){
        	mInfoC
        }
        //Inform if there are alarms in the current country and show a button for explore them
        else{
        	mInfoCountryAlarms.setVisibility(LinearLayout.VISIBLE);
        	mGoToCountryAlarms.setVisibility(LinearLayout.VISIBLE);
        }
        
        
        //Show publication date
        mLastUpdate.setText(result);
        
        //Hide Progress Bar
        mProgressBar.setVisibility(LinearLayout.INVISIBLE);
	}
	
	/**
	 * Check if there are alarms for the current country
	 * 
	 * @return number of alarms for the current country
	 */
	private int numCountryAlarms() {

		ItemsDAO datasource = null;			//Database reference
		int result = -1;
		
		//Connect to database
		datasource = new ItemsDAO(this);
		datasource.open();
		
		//Get the alarms from database
		result = datasource.numAlarms();
		
		//Close database
		datasource.close();
		
		return result;
	}

	/**
	 * Build WebView info to be shown with the data stored in database
	 * 
	 * @return
	 */
	private String buildWebViewContent(){
		
		Item item = null;					//All alarm items
		StringBuilder htmlString = null;			//Html with xml content
		
		//Construct html output
		htmlString = new StringBuilder();
		htmlString.append("<h3>" + getResources().getString(R.string.UIE_page_title) + "</h3>");
		
		//If we are able to know the current province...
		if(currentProvince != null){
		
			//Load items from bbdd
			item = getItemsFromBBDD(currentProvince);
			
			//If result is null, there are no alarms
			if (item == null){
				//Inform we are going to show today's info
				htmlString.append("<p>" + getString(R.string.ALI_alarm_today) + "</p>");
				htmlString.append("<p>" + getString(R.string.ALI_no_alarm) + "</p>");
				
				//Inform we are going to show tomorrow's info
				htmlString.append("<p>" + getString(R.string.ALI_alarm_tomorrow) + "</p>");
				htmlString.append("<p>" + getString(R.string.ALI_no_alarm) + "</p>");
			}
			else{
				//Inform we are going to show today's info
				htmlString.append("<p>" + getString(R.string.ALI_alarm_today) + "</p>");
				
				//If there are some alarms, extract and show them
				if(item.getDescription().noAlarms(Constants.today)){
					htmlString.append("<p>" + getString(R.string.ALI_no_alarm) + "</p>");
				}
				else{
					htmlString.append(formatAlarms(item.getDescription().getToday()));
				}
				
				//Inform we are going to show tomorrow's info
				htmlString.append("<p>" + getString(R.string.ALI_alarm_tomorrow) + "</p>");
				
				//If there are some alarms, extract and show them
				if(item.getDescription().noAlarms(Constants.tomorrow)){
					htmlString.append("<p>" + getString(R.string.ALI_no_alarm) + "</p>");
				}
				else{
					htmlString.append(formatAlarms(item.getDescription().getTomorrow()));
				}
				
			}
			
		//...we can't get current province
		}
		else{
			htmlString.append("<p>" + getString(R.string.LCI_no_address_found) + "</p>");
		}

		return htmlString.toString();
		
	}
	
	/**
	 * Get the alarms for a province
	 * 
	 * @param province
	 * @return a list with alarms or null if there are not alarms
	 */
	private Item getItemsFromBBDD(String province) {
		
		Item result = null;
		ItemsDAO datasource = null;				//Reference to database
		
		//Open datasource
		datasource = new ItemsDAO(this);
		datasource.open();
		
		//Retrieve item for current province
		result = datasource.getItem(province);
	
		//Close datasource
		datasource.close();
		
		return result;
	}

	/**
	 * Format alarm info in an attractive way to show in UI
	 * 
	 * @param alarm map with all alarms for a day
	 * @return string formatted
	 */
	private String formatAlarms(SparseIntArray alarms){
		
		String result = null;
		int alarmLevel = -1;			 		//Alarm level used in processing info
		int alarmType = -1;						//Alarm type used in processing info

		result = new String();
		
		//Iterate over all alarm elements
		for (int i=0; i < alarms.size(); i++){
			
			//Extract the current alarm info
			alarmLevel = alarms.valueAt(i);
			alarmType = alarms.keyAt(i);
			
			//Build the info
  			result = result +"<p>" 
						+ "Type: " 
						+ alarmType 
						+ " Level: "
						+ alarmLevel
						+ "</p>";    			
			
		}
		
		return result;
		
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

    	Context localContext = null;					//Stores the context when the Activity instantiates it
    	String onlineDate = null;						//Xml date retrieve from the Internet
    	
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
    	
    	/**
    	 * Check if new version file is available to determine if current database content is valid
    	 * 
    	 * @return Date of the last xml file version
    	 */
    	@Override
    	protected String doInBackground(String... urls) {
    		
    		//Check if new version file is available
    		if(onlineIsNewer(urls[0])){
    		
    			//Try to get the Xml formatted from web
    			try {
    				return loadXmlFromNetwork(urls[0]);
    			//If it doesn't succeed, it shows errors
    			} catch (IOException e) {
    				return getResources().getString(R.string.EEG_connection_error);
    			}
    		}
    		//...if there is no new version, current database content is valid
    		else{
    			return getDateBBDD();
    		}
    		
    	}

    	/**
    	 * Check if the date of the xml stored into shared preferences is the same as the 
    	 * 
    	 * @param url to retrieve xml
    	 * @return true if the online xml version is newer than the stored in bbdd
    	 */
    	private boolean onlineIsNewer(String url) {
			
    		boolean result = false;
    		String dateBBDD = null;				//Date of bbdd info
    		String dateOnline = null;			//Date of online info
    		
    		//Get xml date stored into bbdd
    		dateBBDD = getDateBBDD();
    		
    		//Get xml date from the Internet
    		dateOnline = getDateOnline(url);
    		
    		//If dates are different, there is a new online version
    		if((dateBBDD != null) && (dateOnline != null)){
				if(!dateBBDD.equals(dateOnline)){
					result = true;
				}
    		}
			
			return result;
    		
		}

    	/**
    	 * Get the date of the current online xml version file
    	 * 
    	 * @param url to retrieve xml
    	 * @return date from xml file
    	 */
    	private String getDateOnline(String url) {

    		String result = null;
    		InputStream stream = null;					//Represent the xml content
    		BufferedReader reader = null;				//Used for searching string into stream
    		boolean dateFound = false;					//Check if we found publication date into xml file
    		String line = null;							//Line returned by reader
    		int indexStartDate = -1;					//Used for searching publication date into a line 
    		int indexEndDate = -1;						//Used for extract pubDate
    		
    		try {
    			
    			//Get stream from url
				stream = downloadUrl(url);
				
				//Initialize reader from inputstream from stream
				reader = new BufferedReader(new InputStreamReader(stream));
				
				//Look for date into reader until the end of file or date was found
				while(!dateFound && reader.ready()){
					//Read line-by-line
					line = reader.readLine();
					
					//Check if line contains publication date
					indexStartDate = line.indexOf(Constants.startPubDate);
					
					//If was found, exit while...
					if (indexStartDate != -1){
						dateFound = true;
					}
				}
				
				//If we are here because we found date...
				if (indexStartDate != -1){
					//Extract only date between start and ending
					indexEndDate = line.indexOf(Constants.endPubDate);
					
					result = line.substring(indexStartDate, indexEndDate);
				}
				
			} catch (IOException e) {
				Log.d(Constants.APP_TAG_EXCEPTION, getString(R.string.EEG_IO_Exception));
				e.printStackTrace();
			}
    		
    		//Store date because it can be helpful later
    		onlineDate = result;
    		
			return result;
		}

		/**
    	 * Get the date of the stored information. This date is stored in General Shared Preferences
    	 * 
    	 * @return date from shared preferences
    	 */
		private String getDateBBDD() {
			
			String result = null;

			result = sPref.getString(Constants.BBDD_DATE, Constants.DEFAULT_BBDD_DATE);
			
			return result;
		}
		
		/**
		 * Set the date of the last data downloaded from the Internet
		 * This date was stored in a class variable when scanning last online file
		 * 
		 */
		private void setDateBBDD(){
			
			Editor editor = null;		//Editor used for update shared preferences
			
			editor = sPref.edit();
			
			//Store new date for xml data
			editor.putString(Constants.BBDD_DATE, onlineDate);
			editor.commit();
			
		}

		@Override
    	protected void onPostExecute(String result){
            
    		//Wait until get current province
    		currentProvince = getCurrentProvince();
    		
    		//Update UI Elements. result contains last version xml file date
    		updateUIElements(result);
    		                   
    	}

    	/**
    	 * Download XML and populate a list with alarm items. Inside the parser, it also loads data into bbdd
    	 * 
    	 * @param url where to download xml
    	 * @return
    	 * @throws IOException 
    	 */
    	private String loadXmlFromNetwork(String url) throws IOException{
    		
    		InputStream stream = null;					//Represent the xml content to parse
    		
    		//Get stream from url
    		stream = downloadUrl(url);
    		    
    		//Get items from stream and at the same time, store it into bbdd
    		getItemsFromStream(stream);
    		
    		//Return the new date after update database info
    		return getDateBBDD();

    	}
    	
    	/**
    	 * Parse stream and extract a list of items and store it into bbdd
    	 * 
    	 * @param stream to be parsed
    	 * 
    	 */
    	private void getItemsFromStream(InputStream stream) {
    		
    		Parser parser = null;			//Parser object to analyze xml
    		ItemsDAO datasource = null;		//Item's DAO to insert info into database
    		
    		//Initialize datasource
    		datasource = new ItemsDAO(localContext);
    		
    		//Open datasource
    		datasource.open();    		

    		//Drop current content in database
    		datasource.dropDB();
    		
    		//Initialize parser
    		parser = new Parser(datasource);
    		
    		//Get items from stream and store in database
    		try {
				parser.parse(stream);
				
				//Update current data date
				setDateBBDD();
				
			} catch (XmlPullParserException e) {
				//Nothing special
				Log.d(Constants.APP_TAG_EXCEPTION, getString(R.string.EEG_Xml_Parser_Exception));
				e.printStackTrace();
			} catch (IOException e) {
				//Nothing special
				Log.d(Constants.APP_TAG_EXCEPTION, getString(R.string.EEG_IO_Exception));
				e.printStackTrace();
			} catch (SQLException e){
				//This exception indicates that there was an error in inserting data into database
				//First of all invalidate all data in database
				datasource.dropDB();
				
				//Log the info
				Log.d(Constants.APP_TAG_EXCEPTION, e.getMessage());
				e.printStackTrace();
			}    		
    		finally{
    			//Taking care: stream must be always closed
    			if(stream != null){
    				try {
						stream.close();
					} catch (IOException e) {
						//Nothing special
						Log.d(Constants.APP_TAG_EXCEPTION, getString(R.string.EEG_IO_Exception));
						e.printStackTrace();
					}
    			}
    			
    			//Always close datasource connection
    			if(datasource != null){
    				datasource.close();
    			}
    		}
    		
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
    		
    		//If getAddressHandler is null it is because we don't have location ability so, return null
    		if(getAddressHandler != null){
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
        		
        		//If task is still not running running return current location, in other case, will return null
        		if((getAddressHandler.getStatus() != AsyncTask.Status.RUNNING)){
        			result = mAddress.getText().toString();
        		}
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
