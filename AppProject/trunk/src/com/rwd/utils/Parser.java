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
	public List parse(InputStream in) throws XmlPullParserException, IOException{
		
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
		}
		
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
	
	private void skip (XmlPullParser parser){
		
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
		
		return result;		
	}
	
}
