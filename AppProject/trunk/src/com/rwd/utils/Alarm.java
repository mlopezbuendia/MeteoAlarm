package com.rwd.utils;

/**
 * Weather alarm
 * 
 * @author manuel.lopez
 *
 */

public class Alarm {
	
	private int type;			//Alarm type
	private int level;			//Alarm level i.e. : green, yellow, red
	
	/**
	 * Constructor
	 * 
	 * @param type 
	 * @param level
	 */
	public Alarm(int type, int level) {
		this.type = type;
		this.level = level;
	}

}
