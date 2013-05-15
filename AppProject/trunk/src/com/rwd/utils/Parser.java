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
		
		Item result;
		String currentTag = null;			//Name of curren tag
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
			
			//Reading item content
			if(currentTag.equals(Constants.title)){
				title = readText(parser, Constants.title);
			}
			else if(currentTag.equals(Constants.link)){
				link = readText(parser, Constants.link);
			}
			else if(currentTag.equals(Constants.description)){
				description = readDescription(parser);
			}
		}
		
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
	
	private DetailedInfo readDescription(XmlPullParser parser){
		
		DetailedInfo result = null;
		Alarm today = null;					//Today's alarm info
		Alarm tomorrow = null;				//Tomorrow's alarm info
		String cData = null;				//Detailed info text raw
				
		
	}
	
}
