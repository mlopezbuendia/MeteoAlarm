package com.rwd.database;

public class DatabaseConstants {

	/* **************** DATABASE PROPERTIES *************************/
	public static final String DATABASE_NAME = "weather_alarms.db";
	public static final int DATABASE_VERSION = 1;
	/* **************************************************************/
	
	/* ******************* AWARANESSES TYPES ************************/
	public static final String AWARENESS_TABLE_NAME = "awaraness";
	private static final String AWARENESS_COLUMN_ID = "_id";
	private static final String AWARENESS_COLUMN_DESCRIPTION = "description";
	public static final String AWARENESS_TABLE_CREATE = "create table " + AWARENESS_TABLE_NAME + "(" +
														  AWARENESS_COLUMN_ID + " integer primary key autoincrement, " + 
														  AWARENESS_COLUMN_DESCRIPTION + " text not null);";
	/* **************************************************************/
	
	/* ******************** ALARM'S LEVELS **************************/
	public static final String LEVELS_TABLE_NAME = "levels";
	private static final String LEVELS_COLUMN_ID = "_id";
	private static final String LEVELS_COLUMN_DESCRIPTION = "description";
	public static final String LEVELS_TABLE_CREATE = "create table " + LEVELS_TABLE_NAME + "(" +
														LEVELS_COLUMN_ID + " integer primary key autoincrement, " +
														LEVELS_COLUMN_DESCRIPTION + " text not null);";
	/* **************************************************************/
	
	//Drop table sentence
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
														   
}
