package com.rwd.utils;

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
}
