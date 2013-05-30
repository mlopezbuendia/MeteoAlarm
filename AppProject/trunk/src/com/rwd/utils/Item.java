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
	 * Default constructor
	 */
	public Item(){
		this.title = null;
		this.link = null;
		this.description = null;
		this.pubDate = null;
		this.guid = null;
	}
	
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
	
	/**
	 * Indicates if there are alarms for today or tomorrow
	 * 
	 * @return true if there are alarms for a item = location
	 */
	public boolean haveAlarms(){
		
		boolean result = false;
		
		//There are alarms if the map contains more than 0 elements
		if((description.getToday().size() > 0) || (description.getTomorrow().size() > 0)){
			result = true;
		}
		
		return result;
		
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
	
	public String getLink(){
		return link;
	}
	
	public String getGuid(){
		return guid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setDescription(DetailedInfo description) {
		this.description = description;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
		
}
