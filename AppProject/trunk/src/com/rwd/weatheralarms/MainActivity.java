package com.rwd.weatheralarms;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import com.rwd.utils.Constants;
import com.rwd.utils.DetailedInfo;
import com.rwd.utils.Item;
import com.rwd.utils.Parser;

public class MainActivity extends Activity {
	
	// Whether the display should be refreshed.
    private static boolean refreshDisplay = true; 
    private static String sPref = null;
    
    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false; 
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    
    //Parse button
    private static Button parseButton = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Set initial prefs
        sPref = Constants.ANY;
        mobileConnected = true;
        
        //Set button action
        parseButton = (Button)findViewById(R.id.parseButton);
        parseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Launch xml download
				loadInfo();
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * Uses AsyncTask to download the XML with the info
     * 
     */
    private void loadInfo(){
    	
    	if(sPref.equals(Constants.ANY) && (wifiConnected || mobileConnected)) {
    		new DownloadXmlTask().execute(Constants.URL);
    	}
    	else if(sPref.equals(Constants.WIFI) && (wifiConnected)){
    		new DownloadXmlTask().execute(Constants.URL);
    	}
    	else{
    		//TODO: Show error in screen 
    		Log.d("ERROR", "Not able to download XML");
    	}
    	
    }
    
    /**
     * Class to implement the async download
     * 
     */
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {

    	@Override
    	protected String doInBackground(String... urls) {
    		
    		//Try to get the Xml formatted from web
    		try {
				return loadXmlFromNetwork(urls[0]);
			//If it doesn't succeed, it shows errors
    		}catch (XmlPullParserException e) {
	                return getResources().getString(R.string.xml_error);
    		} catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } 
    		
    	}

    	@Override
    	protected void onPostExecute(String result){
            setContentView(R.layout.activity_main);
            // Displays the HTML string in the UI via a WebView
            WebView myWebView = (WebView) findViewById(R.id.mainWebView);
            myWebView.loadData(result, "text/html", null);
    	}

    	/**
    	 * Download XML and populate a list with alarm items
    	 * 
    	 * @param url where to download xml
    	 * @return
    	 * @throws IOException
    	 * @throws XmlPullParserException 
    	 */
    	private String loadXmlFromNetwork(String url) throws IOException, XmlPullParserException{
    		
    		InputStream stream = null;					//Represent the xml content to parse
    		Parser parser = null;						//Parser
    		List<Item> items = null;					//All alarm items
    		String title = null;						//Item's title
    		String link = null;							//Item's link
    		DetailedInfo description = null;			//Item's description
    		String pubDate = null;						//Item's publication date
    		String guid = null;							//Item's guid
    		StringBuilder htmlString = null;			//Html with xml content
    		DateFormat formatter = null;				//Formatter for current date
    		Calendar rightNow = null;					//Current Date
    		
    		//Construct html output
    		htmlString = new StringBuilder();
    		htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
    		formatter = new SimpleDateFormat("MMM dd h:mmaa");
    		rightNow = Calendar.getInstance();
    		htmlString.append("<em>" + getResources().getString(R.string.updated) + formatter.format(rightNow.getTime()) + "</em>");
    		
    		//Get stream from url
    		stream = downloadUrl(url);
    		//Get items from stream
    		try {
				items = parser.parse(stream);
			} catch (XmlPullParserException e) {
				//Nothing special
				Log.d("ERROR", "XmlParserException");
				e.printStackTrace();
			} catch (IOException e) {
				//Nothing special
				Log.d("ERROR", "IO Exception");
				e.printStackTrace();
			}
    		//Taking care: stream must be always closed
    		finally{
    			if(stream != null){
    				stream.close();
    			}
    		}
    		
    		// Parser returns a List (called "items") of Item objects.
    	    // Each Item object represents a alarm for a place in the XML feed.
    	    // This section processes the items list to combine each item with HTML markup.
    	    for (Item item : items) {       
    	        htmlString.append("<p>");
    	        htmlString.append(item.getTitle() + "</p>");
    	    }
    	    return htmlString.toString();
    	}
    	
    	/**
    	 * Given a string representation of a URL, sets up a connection and gets an input stream.
    	 * 
    	 * @param urlString with the rss XML-formatted
    	 * @return
    	 * @throws IOException 
    	 */
    	private InputStream downloadUrl(String urlString) throws IOException{
    		
    		URL url = new URL(urlString);
    	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	    conn.setReadTimeout(10000 /* milliseconds */);
    	    conn.setConnectTimeout(15000 /* milliseconds */);
    	    conn.setRequestMethod("GET");
    	    conn.setDoInput(true);
    	    // Starts the query
    	    conn.connect();
    	    return conn.getInputStream();
    		
    	}

    }

    
}
