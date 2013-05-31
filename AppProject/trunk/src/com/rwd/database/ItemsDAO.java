/**
 * 
 */
package com.rwd.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseIntArray;

import com.rwd.utils.DetailedInfo;
import com.rwd.utils.Item;

/**
 * Class for manipulating alarm's items from database to objects and vice-versa
 * 
 * @author manuel.lopez
 *
 */
public class ItemsDAO {
	
	//Database fields
	private SQLiteDatabase database;
	private WADatabase dbHelper;
	
	/**
	 * Constructor for accessing database info
	 * 
	 * @param context
	 */
	public ItemsDAO(Context context){
		dbHelper = new WADatabase(context);
	}
	
	/**
	 * Open database for writing
	 * 
	 * @throws SQLException
	 */
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}
	
	/**
	 * Close database access
	 * 
	 */
	public void close() {
		dbHelper.close();
	}
	
	/**
	 * Insert new item into database. One item have one or more alerts, so one item will represent one or more rows
	 * 
	 * @param Item to insert
	 * @return new item database id or -1 if insertion failed
	 */
	public long insertItem(Item item){
		
		long result = -1;
		ContentValues values = null;		//Values to be inserted into database and extracted from Item object
		int alarmsToInsert = -1;			//Number of alarms to be proccessed into the iteration
		SparseIntArray dayAlarms = null;	//Alarms for a day. Auxiliar to extract alarms info
		int type = -1;						//Unwrapping alarm
		int level = -1;						//Unwrapping alarm
		
		//Extract and populate content values for ITEM TABLE
		values = new ContentValues();
		values.put(DatabaseConstants.ITEM_COLUMN_TITLE, item.getTitle());
		values.put(DatabaseConstants.ITEM_COLUMN_LINK, item.getLink());
		values.put(DatabaseConstants.ITEM_COLUMN_PUBDATE, item.getPubDate());
		values.put(DatabaseConstants.ITEM_COLUMN_GUID, item.getGuid());
		
		//Insert data into database
		result = database.insert(DatabaseConstants.ITEM_TABLE_NAME, null, values);
		
		//If item data was successfully inserted:
		if(result != -1){
			
			//Starting with today...
			//Iterate through the number of today's alarms
			dayAlarms = item.getDescription().getToday();
			alarmsToInsert = dayAlarms.size();
			
			//Looking at result, stop if there is an error inserting data alarms
			for (int i=0; (i<alarmsToInsert) && (result != 1); i++){
				//Extract alarm's info and insert all elements into ALARM TABLE
				type = dayAlarms.keyAt(0);
				level = dayAlarms.valueAt(0);
				
				values = new ContentValues();
				values.put(DatabaseConstants.ALARM_COLUMN_DAY, DatabaseConstants.DAY_TODAY);
				values.put(DatabaseConstants.ALARM_COLUMN_AWARENESS, type);
				values.put(DatabaseConstants.ALARM_COLUMN_LEVEL, level);
				values.put(DatabaseConstants.ALARM_COLUMN_ITEM, item.getTitle());
				
				result = database.insert(DatabaseConstants.ALARM_TABLE_NAME, null, values);
			}
			
			//If there weren't errors, insert tomorrow's values
			if(result != -1){
				
				//Going on with tomorrow
				dayAlarms = item.getDescription().getTomorrow();
				alarmsToInsert = dayAlarms.size();
				
				//Looking at result, stop if there is an error inserting data alarms
				for (int i=0; (i<alarmsToInsert) && (result != 1); i++){
					//Extract alarm's info and insert all elements into ALARM TABLE
					type = dayAlarms.keyAt(0);
					level = dayAlarms.valueAt(0);
					
					values = new ContentValues();
					values.put(DatabaseConstants.ALARM_COLUMN_DAY, DatabaseConstants.DAY_TOMORROW);
					values.put(DatabaseConstants.ALARM_COLUMN_AWARENESS, type);
					values.put(DatabaseConstants.ALARM_COLUMN_LEVEL, level);
					values.put(DatabaseConstants.ALARM_COLUMN_ITEM, item.getTitle());
					
					result = database.insert(DatabaseConstants.ALARM_TABLE_NAME, null, values);
				}
			}

		}
		
		return result;
		
	}
	
	/**
	 * Drop the whole content stored in database because we are only interested in new data
	 * 
	 */
	public void dropDB(){
		
		//Version is not important for us
		dbHelper.onUpgrade(database, 1, 2);
		
	}
	
	/**
	 * Return formatted item with alarms info for the current province
	 * 
	 * @param province we are looking alarms for
	 * @return Item for alarms or null if there are no alarms for this province
	 */
	public Item getItem(String province){
		
		Item result = null;
		Cursor cursor = null;			//Query result
		
		//Declare empty item only with province
		result = new Item();
		result.setTitle(province);
		
		//Retrieve item general info from ITEM table
		cursor = database.query(DatabaseConstants.ITEM_TABLE_NAME,
								//select *
					   			DatabaseConstants.ITEM_ALL_COLUMNS,
					   			//where
					   			DatabaseConstants.ITEM_COLUMN_TITLE + " = " + province, 
					   			//AS
					   			null, 
					   			//groupBy
					   			null, 
					   			//having
					   			null, 
					   			//orderBy
					   			null);
		
		//We only have 1 row per province or none if there are no alarms. 
		if(cursor.moveToFirst()){

			//Populates info into item from Items table
			extractItemFromCursor(result, cursor);
			
			//Close cursor
			cursor.close();
			
			//Retrieve alarms related with item from ALARM table
			cursor = database.query(DatabaseConstants.ALARM_TABLE_NAME,
									//select *
									DatabaseConstants.ALARM_ALL_COLUMNS,
									//where
									DatabaseConstants.ALARM_COLUMN_ITEM + " = " + province,
									//AS
									null,
									//groupBy
									null,
									//having
									null,
									//orderBy
									null,
									//limit
									null);
			
			//If there are results
			if(cursor.moveToFirst()){
				//Populates info into item from Alarms table
				extractAlarmsFromCursor(result, cursor);	
			}
			//If there are no results, return null, something went wrong when inserting
			else{
				result = null;
			}
		
		}
		//If we can't move to first there are no results so result will be null
		
		//Close the cursor
		cursor.close();
		
		return result;
		
	}
	
	/**
	 * Get the number of alarms for the current country
	 * 
	 * @return number of alarms found in database
	 */
	public int numAlarms(){
		
		int result = -1;
		Cursor cursor = null;			//Cursor to store returned values
		
		//Select count (*)
		cursor = database.rawQuery(DatabaseConstants.SQL_ALARMS_COUNT, null);
		
		//Cursor always return at least 1 item
		if (cursor != null){
			cursor.moveToFirst();
			//Get count
			result = cursor.getInt(0);
		}
		
		//Close cursor
		cursor.close();
		
		return result;
		
	}

	/**
	 * Extract alarms from database and populate result item
	 * 
	 * @param result item to be populated
	 * @param cursor with alarms of current province
	 */
	private void extractAlarmsFromCursor(Item result, Cursor cursor) {
		
		DetailedInfo description = null;			//Description with alarms
		SparseIntArray today = null;
		SparseIntArray tomorrow = null;
		
		//Initialize today and tomorrow's alarms
		today = new SparseIntArray();
		tomorrow = new SparseIntArray();
		
		//Iterate through cursor results
		while(!cursor.isAfterLast()){
			
			//Look at alarm day
			if(cursor.getInt(0) == DatabaseConstants.DAY_TODAY){
				today.put(cursor.getInt(1), cursor.getInt(2));
			}
			else{
				tomorrow.put(cursor.getInt(1), cursor.getInt(2));
			}
			
			//Move to next row
			cursor.moveToNext();
		}
		
		//Build a new description attribute
		description = new DetailedInfo(today, tomorrow);
		
		//Set item's description
		result.setDescription(description);
		
	}

	/**
	 * Extract common info of Items
	 * 
	 * @param result to be populated
	 * @param cursor with 1 row info
	 */
	private void extractItemFromCursor(Item result, Cursor cursor) {
		
		//Set all item's attributes
		result.setGuid(cursor.getString(0));
		result.setPubDate(cursor.getString(1));
		result.setLink(cursor.getString(2));
	
	}

}
