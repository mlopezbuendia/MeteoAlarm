/**
 * 
 */
package com.rwd.database;

import com.rwd.utils.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Class defining database structure used in the application
 * 
 * @author manuel.lopez
 *
 */
public class WADatabase extends SQLiteOpenHelper {
	
	/**
	 * Constructor calls constructor's parent with current context and the name and version of the database
	 * 
	 * @param context
	 */
	public WADatabase (Context context){
		super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
	}

	/**
	 * Called by the system if the schema does not exist
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		//Create items table
		db.execSQL(DatabaseConstants.ITEM_TABLE_CREATE);
		
		//Create alarms table
		db.execSQL(DatabaseConstants.ALARM_TABLE_CREATE);

	}

	/**
	 * Call by the system when it is needed to make changes in the schema
	 * Simply drop all existing data and re-create the tables
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		//Show a log
		Log.w(Constants.APP_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion +
			  ", which will destroy all old data");
		
		//Drop tables
		db.execSQL(DatabaseConstants.DROP_TABLE + DatabaseConstants.ITEM_TABLE_NAME);
		db.execSQL(DatabaseConstants.DROP_TABLE + DatabaseConstants.ALARM_TABLE_NAME);
		
		//Create tables
		onCreate(db);
		
	}

}
