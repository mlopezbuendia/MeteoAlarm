/**
 * 
 */
package com.rwd.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * DAO class form manipulating Awareness entity
 * 
 * @author manuel.lopez
 *
 */
public class DAOAwareness {

	//Database fields
	private SQLiteDatabase database;
	private WADatabase dbHelper;
	
	/**
	 * Constructor get a new WADatabase instance;
	 * 
	 * @param context
	 */
	public DAOAwareness(Context context){
		dbHelper = new WADatabase(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
	}
	
	public Awareness createAwareness(String description){
		
		Awareness result = null;
		ContentValues values = null;			//Pairs column-value to insert into the database
		long insertId = -1;						//Id 
		
		return result;
	}
}
