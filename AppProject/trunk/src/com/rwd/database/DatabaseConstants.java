package com.rwd.database;

public class DatabaseConstants {

	/* **************** DATABASE PROPERTIES *************************/
	public static final String DATABASE_NAME = "weather_alarms.db";
	public static final int DATABASE_VERSION = 1;
	/* **************************************************************/
	
	/* ********************* ITEM TABLE **************************/
	//Table name
	public static final String ITEM_TABLE_NAME = "items";
	
	//Table columns
	public static final String ITEM_COLUMN_ID = "_id";
	public static final String ITEM_COLUMN_TITLE = "title";
	public static final String ITEM_COLUMN_LINK = "link";
	public static final String ITEM_COLUMN_PUBDATE = "pub_date";
	public static final String ITEM_COLUMN_GUID = "guid";
	
	//Create table sentence
	public static final String ITEM_TABLE_CREATE = "create table " + ITEM_TABLE_NAME + "(" +
														  ITEM_COLUMN_ID + " integer primary key autoincrement, " + 
														  ITEM_COLUMN_TITLE + " text not null, " +
														  ITEM_COLUMN_LINK + " text , " +
														  ITEM_COLUMN_PUBDATE + " text, " +
														  ITEM_COLUMN_GUID + " text );";
	
	//Columns we are interested in when reading
	public static final String[] ITEM_ALL_COLUMNS = {ITEM_COLUMN_GUID, ITEM_COLUMN_PUBDATE, ITEM_COLUMN_LINK};
	
	/* **************************************************************/
	
	/* ********************* ALARM TABLE **************************/
	//Table name
	public static final String ALARM_TABLE_NAME = "alarm";

	//Table columns
	public static final String ALARM_COLUMN_ID = "_id";
	public static final String ALARM_COLUMN_DAY = "day";
	public static final String ALARM_COLUMN_AWARENESS = "awareness";
	public static final String ALARM_COLUMN_LEVEL = "level";
	public static final String ALARM_COLUMN_ITEM = "item_id";
	
	//Create table sentence
	public static final String ALARM_TABLE_CREATE = "create table " + ALARM_TABLE_NAME + "(" +
														ALARM_COLUMN_ID + " integer primary key autoincrement, " +
														ALARM_COLUMN_DAY + " integer not null, " +
														ALARM_COLUMN_AWARENESS + " integer not null, " +
														ALARM_COLUMN_LEVEL + " integer not null, " +
														ALARM_COLUMN_ITEM + " integer, " +
														"foreign key(" + ALARM_COLUMN_ITEM + ") references " +
														ITEM_TABLE_NAME + "(" + ITEM_COLUMN_TITLE + ") );";
	
	//Columns we are interested in when reading
	public static final String[] ALARM_ALL_COLUMNS = {ALARM_COLUMN_DAY, ALARM_COLUMN_AWARENESS, ALARM_COLUMN_LEVEL};
	
	/* ************************************************************/
	
	//Drop table sentence
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
								
	//Day column values
	public static int DAY_TODAY = 0;
	public static int DAY_TOMORROW = 1;
	
	//SQL Exception Text
	public static final String SQL_INSERT_EXCEPTION = "SQL Exception. Insert operation failed!";
	
	//SQL Alarms count Sentence
	public static final String SQL_ALARMS_COUNT = "select count(*) from " + ALARM_TABLE_NAME + ";";
	
}
