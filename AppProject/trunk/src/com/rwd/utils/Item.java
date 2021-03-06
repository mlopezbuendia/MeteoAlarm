package com.rwd.utils;

/**
 * Each interesting element in XML with location's alarms
 * 
 * @author manuel.lopez
 *
 */

public class Item {
	
	private String title;					//Location name
	private String link;					//Link for detailed info
	private DetailedInfo description;	    //Detailed info
	private String pubDate;				//Publishing Date
	private String guid;					//Guid of rss doc in the link
	
	/**
	 * Constructor
	 * 
	 * @param title
	 * @param link
	 * @param description
	 * @param pubDate
	 * @param guid
	 */
	public Item(String title, String link, DetailedInfo description, String pubDate, String guid) {
		
		this.title = title;
		this.link = link;
		this.description = description;
		this.pubDate = pubDate;
		this.guid = guid;
		
	}

	/*
	 *****************************************************************************************************
	 ********************************** GETTER METHODS NEEDED ********************************************	 
	 *****************************************************************************************************
	 */
	
	public String getTitle() {
		return title;
	}

	public String getPubDate() {
		return pubDate;
	}

	public DetailedInfo getDescription(){
		return description;
	}
	
}
