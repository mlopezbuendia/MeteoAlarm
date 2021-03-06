package com.rwd.utils;

/**
 * Constants used in app
 * 
 * @author manuel.lopez
 *
 */

public final class Constants {
	
	//App tag
	public static final String APP_TAG = "WeatherAlarms";
	
	//Exception tag
	public static final String APP_TAG_EXCEPTION = "WeatherAlarmsException";
	
	//Error tag
	public static final String APP_TAG_ERROR = "WeatherAlarmsError";

	//Namespace used in parser
	public static final String ns = null;
	
	//Tag looked into the xml doc
	public static final String lookedTag = "item";
	//Start tag in doc
	public static final String startTag = "rss";
	//Channel tag (we need to ignore it)
	public static final String channelTag = "channel";
	
	//Elements inside item
	public static final String title = "title";
	public static final String link = "link";
	public static final String description = "description";
	public static final String pubDate = "pubDate";
	public static final String guid = "guid";
	
	//Days in alarm
	public static final int today = 0;
	public static final int tomorrow = 1;
	
	//Constants for manipulate alarm info in cData
	public static final String daySplit = "Tomorrow";
	public static final String startInfo = "awt:";
	public static final String startLevel = "level:";
	
	//URL where we extract the info from
	public static final String URL = "http://www.meteoalarm.eu/documents/rss/es.rss";
	
	//Constants for settings and type of data connection
	public static final String ANY = "Any";
	public static final String WIFI = "Wi-Fi";
	
	//Connection failure in Google Play request
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	//Preferences Names
	public static final String GENERAL_PREFERENCES = "WeatherAlarmPrefs";
	public static final String PREF_PREFERRED_CONNECTION = "ConnectionPreferred";
	public static final String AWARENESS_PREFERENCES = "AwarenessPrefs";
	public static final String LEVELS_PREFERENCES = "LevelsPrefs";
	
	//Date format
	public static final String FORMATTER = "dd MMM h:mmaa";
	public static final String LOCALE_LANGUAGE = "es";
	public static final String LOCALE_COUNTRY = "ES";
	
	//Time to be waiting for current location
	public static final int LOC_MAX_CYCLES_WAIT = 5;
	public static final int LOC_CYCLE_TIME_MILLIS = 1000;
	
}
