package com.Gekctek.WorkQuietly;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;


public class QuietActivity{
	public static final int START = 0;
	public static final int END = 1;
	private String name;
	protected Calendar[] time;
	private boolean silent;
	private boolean enabled;
	private boolean isOneTime;
	public boolean isGPS;
	
	//Constructors
	
	//Pre:
	//Post: Creates an Activity with all inputed parameters
	public QuietActivity(String name, long startTime, long endTime, boolean silent, boolean enabled, boolean GMT, boolean isOneTime){
		this.name = name;
		this.time =  new Calendar[2];
		
		this.time[START] = Calendar.getInstance();
		this.time[END] = Calendar.getInstance();
		if(GMT){
			this.time[START].setTimeZone(TimeZone.getTimeZone("GMT"));
			this.time[END].setTimeZone(TimeZone.getTimeZone("GMT"));
		}
			
		this.time[START].setTimeInMillis(startTime);
		this.time[END].setTimeInMillis(endTime);
		this.silent = silent;
		this.enabled = enabled;
		this.isOneTime = isOneTime;
		this.isGPS = false;
	}
	
	public QuietActivity(Cursor query){
		this(query.getString(query.getColumnIndex("name")),
				query.getLong(query.getColumnIndex("startDate")),
				query.getLong(query.getColumnIndex("endDate")),
				query.getInt(query.getColumnIndex("silent")) == 1,
				query.getInt(query.getColumnIndex("enabled")) == 1,
				false,
				query.getInt(query.getColumnIndex("isOneTime")) == 1);
	}
	
	
	
	
	
	
	
	//Get Methods
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns a string representing the time in 12 hour,
	//		current timezone time
	public String dateString(int index){		
		return dateString(index, false, false);
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns a string representing the time
	public String dateString(int index, boolean military, boolean GMT){
		if(military 
			|| (WorkQuietlyActivity.context != null 
			&& PreferenceManager.getDefaultSharedPreferences(WorkQuietlyActivity.context).getBoolean("clockFormat", false)))
			return dateString(index, "H:mm", GMT);
		else
			return dateString(index, "h:mm a", GMT);
	}
	
	public String dateString(int index, String format, boolean GMT){
		indexExceptionCheck(index);
		SimpleDateFormat dateString = new SimpleDateFormat(format);
		
		if(GMT)
			this.time[index].setTimeZone(TimeZone.getTimeZone("GMT"));
		else
			this.time[index].setTimeZone(TimeZone.getDefault());
		
		return dateString.format(this.time[index].getTime()).toString();
	}
	
	//Pre:
	//Post: Returns a float of the time
	public long getTimeInMillis(int index){
		indexExceptionCheck(index);
		
		return time[index].getTimeInMillis();
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns an int representing 12 hour, current timezone 
	//		activity hour
	public int getHour(int index){
		indexExceptionCheck(index);
		return getHour(index, false, false);
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns an int representing the activity hour
	public int getHour(int index, boolean military, boolean GMT){
		indexExceptionCheck(index);

		if(GMT)
			this.time[index].setTimeZone(TimeZone.getTimeZone("GMT"));
		else
			this.time[index].setTimeZone(TimeZone.getDefault());
		
		if(military)
			return time[index].get(Calendar.HOUR_OF_DAY);
		else
			return time[index].get(Calendar.HOUR);
	}

	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns T if the time is in the PM	
	public boolean isPM(int index){
		indexExceptionCheck(index);
		return time[index].get(Calendar.PM) == 1;
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns an int representing the activity minutes
	public int getMinutes(int index){
		indexExceptionCheck(index);
		return time[index].get(Calendar.MINUTE);
	}
	
	//Pre:
	//Post:	Return T if the activity is silent
	public boolean getSilent(){
		return this.silent;
	}
	
	//Pre:
	//Post:	Returns the name of the activity
	public String getName(){
		return this.name;
	}
	
	//Pre:
	//Post:	Returns T if the activity is enabled
	public boolean getEnabled(){
		return this.enabled;
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post:	Returns the an ActivityTime in the Activity
	public Date getTime(int index){
		indexExceptionCheck(index);
		return time[index].getTime();
	}
	
	
	
	
	
	//Set Methods
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//		Hour must be between 0 and 23
	//Post:	Sets the activity hour to the passed timezone hour
	public void setHour(int index, int hour){
		setHour(index, hour, false);
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//		Hour must be between 0 and 23
	//Post:	Sets the activity hour to the passed hour
	public void setHour(int index, int hour, boolean GMT){
		indexExceptionCheck(index);
		hourExceptionCheck(hour, GMT);
		
		if(GMT)
			this.time[index].setTimeZone(TimeZone.getTimeZone("GMT"));
		else
			this.time[index].setTimeZone(TimeZone.getDefault());
		
		time[index].set(Calendar.HOUR_OF_DAY, hour);
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post:	Sets the activity minutes to the passed minutes
	public void setMinutes(int index, int minutes){
		indexExceptionCheck(index);
		
		time[index].set(Calendar.MINUTE, minutes);
	}
	
	//Pre:
	//Post:	Sets if the activity is silent to the passed boolean
	public void setSilent(boolean silent){
		this.silent = silent;
	}
	
	//Pre:	Name must have characters, 
	//		will throw IllegalArgumentException
	//Post:	Sets the activity name to the passed string
	public void setName(String name){
		if(name == "")
			throw new IllegalArgumentException("Name must have characters");
		
		this.name = name;
	}
	
	//Pre:	
	//Post:	Sets the activity to enabled or disabled	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	//Pre:	Time string must be formatted correctly,
	//		will throw IllegalArgumentException
	//Post:	Sets the specific activity time to the passed 12 hour, timezone time
	public void setTime(int index, String time){		
		setTime(index,time,false,false);
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//		Time string must be formatted correctly,
	//		will throw IllegalArgumentException
	//Post:	Sets the specific activity time to the passed time
	public void setTime(int index, String time, boolean military, boolean GMT){
		indexExceptionCheck(index);
		
		SimpleDateFormat dateF = new SimpleDateFormat();
		try{
			Date d = dateF.parse(time);
			this.time[index].setTime(d);
		}catch(Exception e){
			throw new IllegalArgumentException("Time string format is incorrect");
		}
		
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post:	Sets the specified activity time to the passed activity time
	public void setTime(int index, Date time){
		indexExceptionCheck(index);
		this.time[index].setTime(time);
	}
	
	public void setTimeInMillis(int index, long time){
		indexExceptionCheck(index);
		this.time[index].setTimeInMillis(time);
	}
	

	
	
	//Comparison Methods
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns T if this time is after the other time
	public boolean isAfter(int index, QuietActivity other){
		if(other == null)
			return true;
		Calendar otherC = Calendar.getInstance();
		otherC.setTime(other.getTime(index));
		return isAfter(index, otherC);//TODO ? fix index to 0?
	}
	
	public boolean isAfter(int index, Calendar other){
		indexExceptionCheck(index);
		return time[index].after(other);
	}
	
	public boolean isAfter(int index, int hour, int minutes, boolean GMT){
		indexExceptionCheck(index);
		hourExceptionCheck(hour, GMT);
		minutesExceptionCheck(minutes);
		
		return hour < getHour(index, true, false) || (hour == getHour(index, true, false) && minutes < getMinutes(index));
	}
	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns T if this time is before the other time
	public boolean isBefore(int index, QuietActivity other){
		if(other == null)
			return true;
		Calendar otherC = Calendar.getInstance();
		otherC.setTime(other.getTime(index));
		return isBefore(index, otherC);//TODO ? fix index to 0?
	}
	
	public boolean isBefore(int index, Calendar other){
		indexExceptionCheck(index);
		return time[index].before(other);
	}
	
	public boolean isBefore(int index, int hour, int minutes, boolean GMT){
		indexExceptionCheck(index);
		hourExceptionCheck(hour, GMT);
		minutesExceptionCheck(minutes);
		
		boolean a = hour > getHour(index, true, false);
		boolean b = (hour == getHour(index, true, false) && minutes > getMinutes(index));
		return a || b;
	}

	
	//Pre:	Index must be between 0 and 1, 
	//		will throw IllegalArgumentException
	//Post: Returns T if this time is equal to the other time
	public boolean isEqualTo(int index, QuietActivity other){
		indexExceptionCheck(index);		
		return time[index].equals(other.getTime(index));
	}
		
	
	
	
	
	
	
	//Exception Checks
	
	//Pre:
	//Post:	Throws IllegalArgumentException if index is not between
	//		0 and 1
	public void indexExceptionCheck(int index){
		if(index < 0 || index > 1)
			throw new IllegalArgumentException("Index must be between 0 and 1");
	}
	
	//Pre:
	//Post: Throws an IllegalArgumentException if hour is not between 0 and 23
	public void hourExceptionCheck(int hour, boolean GMT){

		if(GMT)
			hour = convertToTZ(hour);
		
		if(hour < 0 || hour > 23)
			throw new IllegalArgumentException("Hour must be between 0 and 23");
	}
	
	//Pre:
	//Post: Throws an IllegalArgumentException if minutes is not between 0 and 59
	public void minutesExceptionCheck(int minutes){
		if(minutes < 0 || minutes > 59)
			throw new IllegalArgumentException("Minutes must be between 0 and 59");
	}
	
	
	
	
	
	
	
	
	
	////////DELETE??????????????????
	//Pre:
	//Post:	Throws IllegalArguemntException if the string is not formatted correctly
	public void timeFormatCheck(String time, boolean military){
		if(!time.matches("(\\d)(\\d):(\\d)(\\d)|(\\d)(\\d):(\\d)(\\d)(\\s)(A|P)M"))
			throw new IllegalArgumentException("Time string format is incorrect");
	}
	
	
	//Conversion Methods
	
	//Pre:	
	//Post: Returns an int representation of the hour in GMT time
	public int convertToGMT(int hour){
		int timeOffset = TimeZone.getDefault().getRawOffset()/3600000;
		hour -= timeOffset;
		return hour;
	}
	
	//Pre:	
	//Post:	Returns an int representation of the hour in current timezone time
	public int convertToTZ(int hour){
		int timeOffset = TimeZone.getDefault().getRawOffset()/3600000;
		hour += timeOffset;
		return hour;
	}
	
	public boolean isOneTime(){
		return isOneTime;
	}
	
}

