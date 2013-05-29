package com.rwd.utils;

/**
 * XML Parser
 * 
 * @author manuel.lopez
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.SparseIntArray;
import android.util.Xml;

public class Parser {
	
	//Parser stores info in BBDD
	public void parse(InputStream in) throws XmlPullParserException, IOException{
		
		try{
			//Defining the parser
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			//Calling to process
			readFeed(parser);
		} 
		finally{
			in.close();
		}
		
	}
	
	//Processing the feed
	private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException{
		
		//Name of current tag
		String currentTag = null;
		
		//Parser parameters
		parser.require(XmlPullParser.START_TAG, Constants.ns, Constants.startTag);
		
		//Go through the document looking all tags
		while(parser.next() != XmlPullParser.END_TAG){
			//Looking for start tag
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			
			currentTag = parser.getName();
			
			//Looking for "item" tag
			if(currentTag.equals(Constants.lookedTag)){
				readItem(parser);
			}
			//If we receive channel tag, skip only this line
			else if(currentTag.equals(Constants.channelTag)){
				continue;
			}
			else{
				skip(parser);
			}
		}
		
	}
	
	/**
	 * Reads the content of an item and populates one instance. After that insert that instance into bbdd
	 * 
	 * @param parser doc
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private void readItem(XmlPullParser parser) throws XmlPullParserException, IOException{
		
		Item result = null;
		String currentTag = null;			//Name of current tag
		String title = null;				//Item's title
		String link = null;					//Item's link
		DetailedInfo description = null;	//Item's description
		String pubDate = null;				//Item's publication date
		String guid = null;					//Item's guid
		
		//Parser parameters for "item"
		parser.require(XmlPullParser.START_TAG, Constants.ns, Constants.lookedTag);
		
		while(parser.nextTag() != XmlPullParser.END_TAG){
			
			//Looking for next "item"
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			
			currentTag = parser.getName();
			
			//Reading item content...
			//1. Title
			if(currentTag.equals(Constants.title)){
				title = readInfo(parser, Constants.title);
			}
			//2. Link
			else if(currentTag.equals(Constants.link)){
				link = readInfo(parser, Constants.link);
			}
			//3. Description
			else if(currentTag.equals(Constants.description)){
				description = readDescription(parser);				
			}
			//4. Pub Date
			else if(currentTag.equals(Constants.pubDate)){
				pubDate = readInfo(parser, Constants.pubDate);
			}
			//5. Guid
			else if(currentTag.equals(Constants.guid)){
				guid = readInfo(parser, Constants.guid);
			}
			//...else skip
			else{
				skip(parser);
			}
		}
		
		//Building new Item instance
		result = new Item(title, link, description, pubDate, guid);
		
		//Store Item instance
		insertItem(result);
		
	}
	
	/**
	 * Read the content between the current start-end tag
	 * 
	 * @param parser to read
	 * @param tag to use
	 * @return content between start-end tag
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private String readInfo(XmlPullParser parser, String tag) throws XmlPullParserException, IOException{
		
		String result = null;
		int indexStr = -1;				//Used in string publication date formatting
		
		parser.require(XmlPullParser.START_TAG, Constants.ns, tag);
		result = readText(parser);
		parser.require(XmlPullParser.END_TAG, Constants.ns, tag);
		
		//If tag is pubDate, we need to format it in an friendly way
		if(tag.equals(Constants.pubDate)){
			//We drop content until comma and the following blank space
			indexStr = result.indexOf(",");
			result = result.substring(indexStr+1);
			
			//Drop seconds after second colon
			indexStr = result.indexOf(":", result.indexOf(":") + 1);
			result = result.substring(0, indexStr);
		}
		
		return result;
		
	}
	
	/**
	 * Read the string that contains the detailed alarm info
	 * 
	 * @param parser to read
	 * @return new DetailedInfo object
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private DetailedInfo readDescription(XmlPullParser parser) throws XmlPullParserException, IOException{
		
		DetailedInfo result = null;
		SparseIntArray today = null;				//Today's alarms
		SparseIntArray tomorrow = null;				//Tomorrow's alarms
		String cData = null;						//Detailed alarm info text raw
				
		//Position check
		parser.require(XmlPullParser.START_TAG, Constants.ns, Constants.description);
		
		//Go to the next token CDATA from description
		if(parser.nextToken() == XmlPullParser.CDSECT){
			//If is CDATA extracts text...
			cData = parser.getText();
			parser.nextTag();
		}
		
		//Position check
		parser.require(XmlPullParser.END_TAG, Constants.ns, Constants.description);
		
		//If the position is ok no exception is thrown so we can go on...
			
		//Get alarms
		today = extractAlarm(cData, Constants.today);
		tomorrow = extractAlarm(cData, Constants.tomorrow);
		
		//Build DetailedInfo instance
		result = new DetailedInfo(today, tomorrow);
		
		return result;
	}
	
	/**
	 * Ignore tags we are not interested in
	 * 
	 * @param parser
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private void skip (XmlPullParser parser) throws XmlPullParserException, IOException{
		
		if(parser.getEventType() != XmlPullParser.START_TAG){
			throw new IllegalStateException();
		}
		
		int depth = 1;
		
		//Skips all start and end tags after the current call point deep into the document and come out
		while(depth != 0){
			switch(parser.next()){
			case XmlPullParser.END_TAG: depth--;
										 break;
			case XmlPullParser.START_TAG: depth++;
										   break;
			}
		}
		
	}
	
	/**
	 * Return the next alarm extracted from cData
	 * 
	 * @param cData input with today and tomorrow's alarms
	 * @param day to extract (0 = today, 1 =  tomorrow)
	 * @return Alarms Map with info from cData. The structure is a SparseIntArray that is equivalent to a Map<Integer, Integer>
	 * with a better performance
	 */
	private SparseIntArray extractAlarm(String cData, int day){
		
		SparseIntArray result = null;
		int index = -1;						//Index where starts alarm info
		String alarmInfo = null;			//Substring starting from alarm info
		int type = -1;						//Alarm type
		int level = -1;						//Alarm level
		String todayInfo = null;			//To contain string info from the beginning to the string "Tomorrow"
		String tomorrowInfo = null;			//To contain string info from the string "Tomorrow" till the end
		
		//Split the cData string into today and tomorrow
		index = cData.indexOf(Constants.daySplit);
		todayInfo = cData.substring(0, index);
		tomorrowInfo = cData.substring(index);
		
		//Select the day we are interested in
		switch(day){
			case Constants.today:
			{
				alarmInfo = todayInfo;
				break;
			}
			case Constants.tomorrow:
			{
				alarmInfo = tomorrowInfo;
				break;
			}
		}
		
		//Create a new SparseIntArray object
		result = new SparseIntArray();
		
		//Looks for the beginning of alarm info
		index = alarmInfo.indexOf(Constants.startInfo);
		
		//Iterate through the string looking for alarm info
		while(index != -1){
			
			//Now we have in alarmInfo the info for the desired day
			alarmInfo = alarmInfo.substring(index);
			
			//Populate Alarm from alarm info string starting with "awt:"
			//...get the awt type code, it can be one or 2 digits, so we get 2 characters. If it's one digit long, we dismiss the 
			//blank space after that digit..
			type = Integer.parseInt(alarmInfo.substring(4, 6).trim());
			
			//...go on looking for "level:"
			index = alarmInfo.indexOf(Constants.startLevel);
			
			//If was found...
			if(index != -1){
				//Follow to level...
				alarmInfo = alarmInfo.substring(index);
				//level is always 1 digit long (1,2,3,4)
				level = Integer.parseInt(alarmInfo.substring(6, 7));
			}
			
			//Add the new alarm for the processed day
			result.put(type, level);
			
			//Advance to the next alarm of the current day
			index = alarmInfo.indexOf(Constants.startInfo);
		}

		//Return the result;		
		return result;		
	}
	
	/**
	 * Read text content 
	 * 
	 * @param parser
	 * @return text content 
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private String readText(XmlPullParser parser) throws XmlPullParserException, IOException{
		
		String result = null;
		
		if(parser.next() == XmlPullParser.TEXT){
			result = parser.getText();
			parser.nextTag();
		}
		
		return result;
		
	}
		
}
