package com.rwd.utils;

import java.util.Map;

import android.util.SparseIntArray;

/**
 * Class containing detailed weather info for Today and Tomorrow
 * 
 * @author manuel.lopez
 *
 */

public class DetailedInfo {

	private SparseIntArray today;			//Alarm's map for today (key = awareness type // value = alarm level)
	private SparseIntArray tomorrow;		//Alarm's map for tomorrow (key = awareness type // value = alarm level)
	
	public DetailedInfo(){
		this.today = new SparseIntArray();
		this.tomorrow = new SparseIntArray();
	}
	
	/**
	 * Constructor with parameters
	 * 	
	 * @param today is a map with today's alarms
	 * @param tomorrow is a map with tomorrow's alarms
	 */
	public DetailedInfo(SparseIntArray today, SparseIntArray tomorrow) {
		this.today = today;
		this.tomorrow = tomorrow;
	}
	
	/**
	 * Add a new alarm element in the corresponding day map
	 * 
	 * @param awareness is the type of the alarm
	 * @param level of the alarm
	 * @param day where to insert the new alarm element
	 */
	public void addAlarm(int awareness, int level, int day){
		
		//Decide where to insert the new alarm element
		//Use Awareness as key in the map
		switch(day){
			case(Constants.today):
			{
				today.put(awareness, level);
				break;
			}
		case(Constants.tomorrow):
			{
				tomorrow.put(awareness, level);
				break;
			}
		}
		
	}
	
	/**
	 * Inform is there are some alarms for the day passed by parameter
	 * 
	 * @param day we want to know if there are alarms for
	 * @return true if there are not alarms for that day
	 */
	public boolean noAlarms(int day){
		
		boolean result = false;
		SparseIntArray looked = null;				//Reference of the map where to look
		int level = -1;								//For extract level alarm when there is only one in the map
		
		//Select the day we are looking for
		switch (day){
			case (Constants.today):
			{
				looked = today;
				break;
			}
			case (Constants.tomorrow):{
				looked = tomorrow;
				break;
			}
		}
		
		//There won't be alarms if there is only 1 alarm element and its level is "Green Level" = 1
		//TODO: IF AT THE END USE SQLITE TABLE FOR THIS PARAMETER, CHANGE THE CONDITION
		if(looked.size() == 1){
			//Extract the first element (there is only one)
			level = looked.valueAt(0);
			
			//If the level is equals to 1, then this is not an alarm
			if (level == 1){
				result = true;
			}
		}
		
		return result;
		
	}
	
	/*
	 * *************************************************************
	 **************** GETTER METHODS NEEDED ************************
	 * *************************************************************
	 */
	public SparseIntArray getToday(){
		return today;
	}
	
	public SparseIntArray getTomorrow(){
		return tomorrow;
	}
	 
}
