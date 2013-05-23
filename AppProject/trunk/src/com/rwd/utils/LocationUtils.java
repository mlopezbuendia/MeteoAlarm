package com.rwd.utils;

import com.rwd.weatheralarms.R;

import android.content.Context;
import android.location.Location;

public final class LocationUtils {
	
	//Update interval in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	
	public static final int MILLIS_IN_SECOND = 1000;
	
	//Update interval in milliseconds
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_SECONDS * MILLIS_IN_SECOND;
	
	//Update ceiling in seconds
	public static final int FAST_INTERVAL_CEILING_IN_SECONDS = 1;
	
	//Update ceiling in milliseconds
	public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = FAST_INTERVAL_CEILING_IN_SECONDS * MILLIS_IN_SECOND;
	
	//Location Preferences
	public static final String LOCATION_PREFERENCES = "LocationPreferences";
	
	//Create an empty String for initializing strings
	public static final String EMPTY_STRING = new String();
	
	/**
	 * Get the latitude and longitude from the Location object returned by Location Services
	 * 
	 * @param context
	 * @param currentLocation A location object containing the current location
	 * @return The latitude and longitude of the current location, or null if no location is available
	 */
	public static String getLatLng(Context context, Location currentLocation){
		
		String result = null;
		
		//If the location is valid
		if(currentLocation != null){
			
			//Return the latitude and longitude as strings
			result = R.string.LCI_latitude_longitude + 
					 String.valueOf(currentLocation.getLatitude()) +
					 String.valueOf(currentLocation.getLongitude());
		}
		else{
			
			//Otherwise, return the empty string;
			result = EMPTY_STRING;
		}
		
		return result;
		
	}
}
