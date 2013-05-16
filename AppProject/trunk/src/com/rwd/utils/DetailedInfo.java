package com.rwd.utils;

/**
 * Class containing detailed weather info for Today and Tomorrow
 * 
 * @author manuel.lopez
 *
 */

public class DetailedInfo {

	private Alarm today;			//Alarm for today
	private Alarm tomorrow;		//Alarm for tomorrow
	
	/**
	 * Constructor
	 * 	
	 * @param today
	 * @param tomorrow
	 */
	public DetailedInfo(Alarm today, Alarm tomorrow) {
		this.today = today;
		this.tomorrow = tomorrow;
	}
	
}
