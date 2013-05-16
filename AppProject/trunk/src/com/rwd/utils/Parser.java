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

import android.util.Log;
import android.util.Xml;

public class Parser {
	
	//Parser
	public List<Item> parse(InputStream in) throws XmlPullParserException, IOException{
		
		try{
			//Defining the parser
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			//Calling to process
			return readFeed(parser);
		} 
		finally{
			in.close();
		}
		
	}
	
	//Processing the feed
	private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException{
		
		//Result
		List items = new ArrayList();
		
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
				items.add(readItem(parser));
			}
			else{
				skip(parser);
			}
		}
		
		return items;
		
	}
	
	/**
	 * Reads the content of an item and populates one instance
	 * 
	 * @param parser doc
	 * @return one item with alarms info for a location
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private Item readItem(XmlPullParser parser) throws XmlPullParserException, IOException{
		
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
				title = readText(parser, Constants.title);
			}
			//2. Link
			else if(currentTag.equals(Constants.link)){
				link = readText(parser, Constants.link);
			}
			//3. Description
			else if(currentTag.equals(Constants.description)){
				description = readDescription(parser);
			}
			//4. Pub Date
			else if(currentTag.equals(Constants.pubDate)){
				pubDate = readText(parser, Constants.pubDate);
			}
			//5. Guid
			else if(currentTag.equals(Constants.guid)){
				guid = readText(parser, Constants.guid);
			}
			//...else skip
			else{
				skip(parser);
			}
		}
		
		//Building new Item instance
		result = new Item(title, link, description, pubDate, guid);
		
		return result;
		
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
	private String readText(XmlPullParser parser, String tag) throws XmlPullParserException, IOException{
		
		String result = null;
		
		parser.require(XmlPullParser.START_TAG, Constants.ns, tag);
		result = parser.getText();
		parser.require(XmlPullParser.END_TAG, Constants.ns, tag);
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
		Alarm today = null;					//Today's alarm info
		Alarm tomorrow = null;				//Tomorrow's alarm info
		String cData = null;				//Detailed info text raw
				
		//Position check
		parser.require(XmlPullParser.START_TAG, Constants.ns, Constants.description);
		
		//Go to the next token CDATA from description
		if(parser.nextToken() == XmlPullParser.CDSECT){
			//If is CDATA extracts text...
			cData = parser.getText();
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
	 * @return Alarm with info from cData
	 */
	private Alarm extractAlarm(String cData, int day){
		
		Alarm result = null;
		int index = -1;				//Index where starts alarm info
		String alarmInfo = null;	//Substring starting from alarm info
		int type = -1;			//Alarm type
		int level = -1;			//Alarm level
		
		//Looks for start of alarm info
		index = cData.indexOf(Constants.startInfo);
		
		//If was found...
		if(index != -1){
			//We get the substring with the info
			alarmInfo = cData.substring(index);
			
			//If day = tomorrow we want the second info, so we look for start alarm info again
			if (day == Constants.tomorrow){
				index = alarmInfo.indexOf(Constants.startInfo);
				
				//If was found...
				if(index != -1){
					//We get the substring with tomorrow's alarm info
					alarmInfo = alarmInfo.substring(index);
				}
				else{
					alarmInfo = "";
				}
			}
		}
		else{
			alarmInfo = "";
		}
		
		//Now we have in alarmInfo the info for the desired day or "" if something went wrong
		if(!alarmInfo.equals("")){
			//Populate Alarm from alarm info string starting with "awt:"
			//...get the awt type code, it can be one or 2 digits, so we get 2 characters. If it's one digit long, we dismiss the 
			//blank space after that digit...
			type = Integer.parseInt(alarmInfo.substring(4, 6).trim());
			//...go on looking for "level:"
			index = alarmInfo.indexOf(Constants.startLevel);
			//If was found...
			if(index != -1){
				//Follow to level...
				alarmInfo = alarmInfo.substring(index);
				//level is always 1 digit long (1,2,3,4)
				level = Integer.parseInt(alarmInfo.substring(0,1));
			}
			
			result = new Alarm(type, level);
		}
		
		return result;		
	}
	
}
