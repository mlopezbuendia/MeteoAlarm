/**
 * 
 */
package com.rwd.utils;

/**
 * Constants used to populate default awareness values
 * 
 * @author manuel.lopez
 *
 */
public class AwarenessModel {
	
	//No defaults set 
	public static String NO_AWARENESS_SET = "NoAwareness";
	
	/* ********************** AWARENESS DESCRIPTION **********************/
	public static String VALUE1 = "Wind";
	public static String VALUE2 = "Snow/Ice";
	public static String VALUE3 = "Thunderstorms";
	public static String VALUE4 = "Fog";
	public static String VALUE5 = "Extreme high temperature";
	public static String VALUE6 = "Extreme low temperature";
	public static String VALUE7 = "Coastal Event";
	public static String VALUE8 = "Forestfire";
	public static String VALUE9 = "Avalanches";
	public static String VALUE10 = "Rain";
	public static String VALUE11 = "";
	public static String VALUE12 = "Flood";
	public static String VALUE13 = "Rain-Flood";
	
	//Different types of awareness available
	public static int NUM_AWARENESS = 13;
	
	//Array with all awareness values
	public static String[] allAwareness = {VALUE1, VALUE2, VALUE3, VALUE4, VALUE5, VALUE6, VALUE7, VALUE8, VALUE9,
											VALUE10, VALUE11, VALUE12, VALUE13};
	
}
