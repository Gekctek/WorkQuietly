package com.Gekctek.WorkQuietly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper {
	private static final String DATABASE_NAME = "Quiet Activities";
	private static final String CREATE_RECURRING_TABLE = "CREATE TABLE Recurring (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, startDate REAL, endDate REAL, enabled INTEGER, silent INTEGER, Monday INTEGER, Tuesday INTEGER, Wednesday INTEGER, Thursday INTEGER, Friday INTEGER, Saturday INTEGER, Sunday INTEGER, isOneTime INTEGER);";
	private static final String CREATE_ONE_TIME_TABLE = "CREATE TABLE OneTime (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, startDate REAL, endDate REAL, silent INTEGER, enabled INTEGER, isOneTime INTEGER);";
	private static final String CREATE_FILTERS_TABLE = "CREATE TABLE CalendarFilter (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, filters TEXT, enabled INTEGER, silent INTEGER);";
	private static final String CREATE_GPS_TABLE = "CREATE TABLE GPS (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, latitude REAL, longitude REAL, radius INTEGER, enabled INTEGER, silent INTEGER);";
	private static final String CREATE_WHITELIST_TABLE = "CREATE TABLE WHITELIST (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, number TEXT Unique, contactID INTEGER, numberType TEXT, displayNumber TEXT);";
	private static final String CREATE_RECURRING_ADVANCED_TABLE = "CREATE TABLE RecurringAdvanced (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, startDate REAL, endDate REAL, enabled INTEGER, silent INTEGER, isOneTime INTEGER, startDay INTEGER, endDay INTEGER, repeatIndex INTEGER);";
	private static final String RECURRING_ACTIVITY = "Recurring";	
	private static final String RECURRING_ADVANCED = "RecurringAdvanced";	
	private static final String ONE_TIME_ACTIVITY = "OneTime";
	private static final String CALENDAR_FILTER = "CalendarFilter";
	private static final String GPS = "GPS";
	private static final String WHITELIST = "WHITELIST";
	private static final int VERSION = 8;
	private SQLiteDatabase db;
    private final Context context; 
    private DatabaseHelper DataBHelper;
 
    public DBHelper(Context ctx) {
        this.context = ctx;
        DataBHelper = new DatabaseHelper(context);
    }
 
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_RECURRING_TABLE);
            db.execSQL(CREATE_ONE_TIME_TABLE);
            db.execSQL(CREATE_FILTERS_TABLE);
            db.execSQL(CREATE_GPS_TABLE);
            db.execSQL(CREATE_WHITELIST_TABLE);
            db.execSQL(CREATE_RECURRING_ADVANCED_TABLE);
            Log.w("Database", "New database created, old data destroyed");
        }
 
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	try{
        		boolean upgraded = false;
        		switch(oldVersion){
        		case 6:
        			Log.i("Database", "Upgrading database from version 6 to 7");
        			db.execSQL(CREATE_WHITELIST_TABLE);
        			Log.i("Database", "Database upgraded to version 7 successfully");
        		case 7:
        			Log.i("Database", "Upgrading database from version 7 to 8");
        			db.execSQL(CREATE_RECURRING_ADVANCED_TABLE);
        			Log.i("Database", "Database upgraded to version 8 successfully");
        			upgraded = true;
        			break;
        		}
        		
        		
        		if(!upgraded)
        			throw new Exception("Database is not the correct version, but wasn't upgraded");
        	}catch(Exception e){
        		Log.e("Database", e.getMessage());
	            Log.w("Database", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
	            db.execSQL("DROP TABLE IF EXISTS "+RECURRING_ACTIVITY);
	            db.execSQL("DROP TABLE IF EXISTS "+ONE_TIME_ACTIVITY);
	            db.execSQL("DROP TABLE IF EXISTS "+CALENDAR_FILTER);
	            db.execSQL("DROP TABLE IF EXISTS "+GPS);
	            db.execSQL("DROP TABLE IF EXISTS "+WHITELIST);
	            db.execSQL("DROP TABLE IF EXISTS "+RECURRING_ADVANCED);
	            onCreate(db);
        	}
        	Log.i("Database", "Database upgraded");
        }
    }    
 
    //---opens the database---
    public DBHelper open() throws SQLException {
        db = DataBHelper.getWritableDatabase();
        return this;
    }
 
    //---closes the database---    
    public void close() {
        DataBHelper.close();
    }
 
    
  	//Database altering methods
    
	//Pre:	
	//Post: Returns a cursor holding the queried database values
	public Cursor query(String table, String[] columns, String selection,String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}
	
	//Pre:	
	//Post: Returns a cursor holding the queried database values
	public Cursor rawQuery(String query, String[] args) {
		return db.rawQuery(query, args);
	}

	
	//Pre:	
	//Post:	Adds the QuietActivity's values to the database
	public int addToDB(RecurringQuietActivity activity) {
		ContentValues values = putRecurringValues(activity);

		open();
		int result = (int)db.insert(RECURRING_ACTIVITY, null, values);
		close();
		return result;
	}
	
	//Pre:
	//Post:	Updates the passed QuietActivity's values to the other QuietActivity's
	public int updateDB(String name, RecurringQuietActivity activity){
		ContentValues values = putRecurringValues(activity);
		
		String table = RECURRING_ACTIVITY;
		String whereClause = "name=?";
		String[] whereArgs = new String[] { name };
		
		open();
		int result = db.update(table, values, whereClause, whereArgs);
		close();
		return result;
	}
	
	private ContentValues putRecurringValues(RecurringQuietActivity activity){
		ContentValues values = new ContentValues();
		values.put("name", 		activity.getName());
		values.put("startDate", activity.getTimeInMillis(0));
		values.put("endDate", 	activity.getTimeInMillis(1));
		values.put("enabled", 	activity.getEnabled() ? 1 : 0);
		values.put("silent", 	activity.getSilent() ? 1 : 0);
		values.put("isOneTime",	0);
		
		values.put("Monday", 	activity.getDay(0) ? 1 : 0);
		values.put("Tuesday", 	activity.getDay(1) ? 1 : 0);
		values.put("Wednesday", activity.getDay(2) ? 1 : 0);
		values.put("Thursday", 	activity.getDay(3) ? 1 : 0);
		values.put("Friday", 	activity.getDay(4) ? 1 : 0);
		values.put("Saturday",	activity.getDay(5) ? 1 : 0);
		values.put("Sunday", 	activity.getDay(6) ? 1 : 0);
		
		return values;
	}

	//Pre:	
	//Post:	Adds the QuietActivity's values to the database
	public int addToDB(OneTimeQuietActivity activity) {
		ContentValues values = putOneTimeValues(activity);
		
		open();
		int result = (int)db.insert(ONE_TIME_ACTIVITY, null, values);
		close();
		return result;
	}
	
	
	//Pre:
	//Post:	Updates the passed QuietActivity's values to the other QuietActivity's
	public int updateDB(String oldName, OneTimeQuietActivity activity){
		
		ContentValues values = putOneTimeValues(activity);

		String table = ONE_TIME_ACTIVITY;
		String whereClause = "name=?";
		String[] whereArgs = new String[] { oldName };
		
		open();
		int result = db.update(table, values, whereClause, whereArgs);
		close();
		return result;
	}	
	
	public int addToDB(CalendarFilter cFilter){
		ContentValues values = putCalendarFilterValues(cFilter);
		
		open();
		int result = (int)db.insert(CALENDAR_FILTER, null, values);
		close();
		
		return result;
	}
	
	public int updateDB(String oldName, CalendarFilter cFilter){
		ContentValues values = putCalendarFilterValues(cFilter);
		
		String table = CALENDAR_FILTER;
		String whereClause = "name=?";
		String[] whereArgs = new String[] { oldName };
		
		open();
		int result = db.update(table, values, whereClause, whereArgs);
		close();
		
		return result;
	}
	
	private ContentValues putCalendarFilterValues(CalendarFilter cFilter){
		ContentValues values = new ContentValues();
		values.put("name", cFilter.getName());
		values.put("filters", cFilter.getFilters());
		values.put("enabled", cFilter.getEnabled() ? 1 : 0);
		values.put("silent", cFilter.getSilent() ? 1 : 0);
		
		return values;
	}
	
	
	private ContentValues putOneTimeValues(OneTimeQuietActivity activity){
		ContentValues values = new ContentValues();
		values.put("name", 		activity.getName());
		values.put("startDate", activity.getTimeInMillis(0));
		values.put("endDate", 	activity.getTimeInMillis(1));
		values.put("silent", 	activity.getSilent() ? 1 : 0);
		values.put("enabled", 	activity.getEnabled() ? 1 : 0);
		values.put("isOneTime",	1);

		return values;
	}
	
	//Pre:	
	//Post:	Adds the QuietActivity's values to the database
	public int addToDB(RecurringAdvancedQuietActivity activity) {
		ContentValues values = putRecurringValues(activity);

		open();
		int result = (int)db.insert(RECURRING_ADVANCED, null, values);
		close();
		return result;
	}
	
	//Pre:
	//Post:	Updates the passed QuietActivity's values to the other QuietActivity's
	public int updateDB(String name, RecurringAdvancedQuietActivity activity){
		ContentValues values = putRecurringValues(activity);
		
		String table = RECURRING_ADVANCED;
		String whereClause = "name=?";
		String[] whereArgs = new String[] { name };
		
		open();
		int result = db.update(table, values, whereClause, whereArgs);
		close();
		return result;
	}
	
	private ContentValues putRecurringValues(RecurringAdvancedQuietActivity activity){
		ContentValues values = new ContentValues();
		values.put("name", 		activity.getName());
		values.put("startDate", activity.getTimeInMillis(0));
		values.put("endDate", 	activity.getTimeInMillis(1));
		values.put("enabled", 	activity.getEnabled() ? 1 : 0);
		values.put("silent", 	activity.getSilent() ? 1 : 0);
		values.put("isOneTime",	0);
		values.put("startDay", activity.getStartDayIndex());
		values.put("endDay", activity.getEndDayIndex());
		values.put("repeatIndex", activity.getRepeatIndex());
		return values;
	}
	
	//Pre:
	//Post: Deletes the specified QuietActivty from the database
	//		Returns 1 if the specified QuietActivity was deleted
	public int deleteR(String name){
		String whereClause = "name=?";
		String[] whereArgs = new String[]{name};
		return db.delete(RECURRING_ACTIVITY, whereClause, whereArgs);
	}	
	
	public int deleteRA(String name){
		String whereClause = "name=?";
		String[] whereArgs = new String[]{name};
		return db.delete(RECURRING_ADVANCED, whereClause, whereArgs);
	}	
	
	public int deleteO(String name){
		String whereClause = "name=?";
		String[] whereArgs = new String[]{name};
		return db.delete(ONE_TIME_ACTIVITY, whereClause, whereArgs);
	}	
	
	public int deleteC(String name){
		String whereClause = "name=?";
		String[] whereArgs = new String[]{name};
		return db.delete(CALENDAR_FILTER, whereClause, whereArgs);
	}	
	
	public int deleteGPS(String name){
		String whereClause = "name=?";
		String[] whereArgs = new String[]{name};
		return db.delete(GPS, whereClause, whereArgs);
	}

	public int updateDB(String oldName, String newName, double latitude, double longitude, int radius, boolean enabled, boolean silent){
		ContentValues values = putGPSValues(newName, latitude, longitude, radius, enabled, silent);
		
		String[] args = {oldName};
		
		open();
		int result  = (int)db.update(GPS, values, "name=?", args);
		close();
		
		return result;
	}	
	
	public int addToDB(String gpsName, double latitude, double longitude, int radius, boolean enabled, boolean silent) {
		ContentValues values = putGPSValues(gpsName, latitude, longitude, radius, enabled, silent);

		open();
		int result  = (int)db.insert(GPS, null, values);
		close();
		
		return result;
	}	
	
	public ContentValues putGPSValues(String gpsName, double latitude, double longitude, int radius, boolean enabled, boolean silent){
		ContentValues values = new ContentValues();
		values.put("name", gpsName);
		values.put("latitude", latitude);
		values.put("longitude", longitude);
		values.put("radius", radius);
		values.put("enabled", enabled ? 1 : 0);
		values.put("silent", silent ? 1 : 0);
		
		return values;
	}
	
	public int addToDB(String contactDisplayNumber, String contactName, String contactId, String numberType){
		
		ContentValues values = new ContentValues();
		values.put("displayNumber", contactDisplayNumber);
		String contactNumber = contactDisplayNumber.replaceAll("\\D", "");
		values.put("number", contactNumber);
		values.put("name", contactName);
		values.put("contactId", contactId);
		values.put("numberType", numberType);
		open();
		int result = (int)db.insert(WHITELIST, null, values);
		close();
		
		return result;
	}

	public int deleteWhitelistNumber(String number) {
		number = number.replaceAll("\\D", "");
		open();
		int result = (int)db.delete(WHITELIST, "number=?", new String[]{number});
		close();
		return result;		
	}

}
