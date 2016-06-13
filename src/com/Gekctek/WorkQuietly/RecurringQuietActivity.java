package com.Gekctek.WorkQuietly;


import android.database.Cursor;


public class RecurringQuietActivity extends QuietActivity{
	private boolean[] days;
	//Constructors
	
	//Pre:
	//Post: Creates an Activity with all inputed parameters

	
	public RecurringQuietActivity(Cursor query){
		super(query);
		days = new boolean[7];
		days[0] = query.getInt(query.getColumnIndex("Monday")) == 1;
		days[1] = query.getInt(query.getColumnIndex("Tuesday")) == 1;
		days[2] = query.getInt(query.getColumnIndex("Wednesday")) == 1;
		days[3] = query.getInt(query.getColumnIndex("Thursday")) == 1;
		days[4] = query.getInt(query.getColumnIndex("Friday")) == 1;
		days[5] = query.getInt(query.getColumnIndex("Saturday")) == 1;
		days[6] = query.getInt(query.getColumnIndex("Sunday")) == 1;
	}
	
	public RecurringQuietActivity(String name, long startTime, long endTime, boolean silent, boolean enabled, boolean[] days, boolean GMT){
		super(name, startTime, endTime, silent, enabled, GMT, false);
		this.days = days;
	}
	
	//Pre:
	//Post: Returns a boolean array of which days are enabled
	public boolean[] getDays(){
		return days;
	}
	
	//Pre:	Index must be between 0 and 6,
	// 		will throw IllegalArgumentException
	//Post:	Return T if the chosen day is enabled
	public boolean getDay(int index){
		if(index < 0 || index > 6)
			throw new IllegalArgumentException("Index must be between 0 and 6");
		
		return days[index];
	}
	
	//Pre:	Passed array must have exactly 7 indices,
	//		will throw IllegalArgumentException
	//Post: Sets the activity days array to the passed array
	public void setDays(boolean[] days){
		if(days.length != 7)
			throw new IllegalArgumentException("Array must have exactly 7 indices");
		
		this.days = days;
	}
	
	//Pre:	Index must be between 0 and 6, 
	//		will throw IllegalArgumentException
	//Post: Sets the specific day to enabled or disabled
	public void setDay(int index, boolean enabled){
		if(index < 0 || index > 6)
			throw new IllegalArgumentException("Index must be between 0 and 6");
		
		this.days[index] = enabled;
	}
}