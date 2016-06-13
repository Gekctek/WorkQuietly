package com.Gekctek.WorkQuietly;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.database.Cursor;
import android.preference.PreferenceManager;

public class RecurringAdvancedQuietActivity extends QuietActivity {
	private int startDay;
	private int endDay;
	public static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	public static final String[] REPEAT_LIST = {"Every Week", "Every Month - 1st Week", "Every Month - 2nd Week", "Every Month - 3rd Week", "Every Month - 4th Week", "Every Month - 5th Week"};
	private int repeatIndex;
	
	public RecurringAdvancedQuietActivity(Cursor query){
		super(query);
		this.startDay = query.getInt(query.getColumnIndex("startDay"));
		this.endDay = query.getInt(query.getColumnIndex("endDay"));
		this.repeatIndex = query.getInt(query.getColumnIndex("repeatIndex"));
	}
	
	public RecurringAdvancedQuietActivity(String name, long startTime, long endTime, boolean silent, boolean enabled, boolean[] days, boolean GMT, int startDay, int endDay, int repeatIndex) {
		super(name, startTime, endTime, silent, enabled, GMT, false);
		
		if(startDay < 0 || startDay > 6 || endDay < 0 || endDay > 6)
			throw new IllegalArgumentException("Start day and end day must be between 0 and 6");
		
		if(repeatIndex < 0 || repeatIndex > 5)
			throw new IllegalArgumentException("Repeat index must be between 0 and 5");
		
		this.startDay = startDay;
		this.endDay = endDay;
		this.repeatIndex = repeatIndex;
	}
	
	public int getStartDayIndex(){
		return startDay;
	}
	
	public String getStartDay(){
		return DAYS[startDay];
	}
	
	public int getEndDayIndex(){
		return endDay;
	}
	
	public String getEndDay(){
		return DAYS[endDay];
	}
	
	public int getRepeatIndex(){
		return repeatIndex;
	}
	
	public String getRepeat(){
		return REPEAT_LIST[repeatIndex];
	}
	
	public void setStartDayIndex(int index){
		if(index < 0 || index > 6)
			throw new IllegalArgumentException("Start day must be between 0 and 6");
		
		startDay = index;
	}
	
	public void setEndDayIndex(int index){
		if(index < 0 || index > 6)
			throw new IllegalArgumentException("End day must be between 0 and 6");
		
		endDay = index;
	}
	
	public void setRepeatIndex(int index){
		if(index < 0 || index > 5)
			throw new IllegalArgumentException("Repeat index must be between 0 and 5");
		
		repeatIndex = index;
	}
	
	public String timeString(int index){
		if(WorkQuietlyActivity.context != null && PreferenceManager.getDefaultSharedPreferences(WorkQuietlyActivity.context).getBoolean("clockFormat", false))
			return super.dateString(index, "H:mm", false);
		else
			return super.dateString(index, "h:mm a", false);
	}
	
	@Override
	public String dateString(int index){		
		return dateString(index, false, false);
	}

	@Override
	public String dateString(int index, String format, boolean GMT){
		indexExceptionCheck(index);
		SimpleDateFormat dayString = new SimpleDateFormat("E");
		SimpleDateFormat timeString = new SimpleDateFormat(format);
		
		if(GMT)
			this.time[index].setTimeZone(TimeZone.getTimeZone("GMT"));
		else
			this.time[index].setTimeZone(TimeZone.getDefault());
		
		Calendar c = Calendar.getInstance();
		if(index == 0){
			c.set(Calendar.DAY_OF_WEEK, startDay+1);
		}else{
			c.set(Calendar.DAY_OF_WEEK, endDay+1);
		}
		String day = dayString.format(c.getTime()).toString();
		String time = timeString.format(this.time[index].getTime()).toString();
		return day + " " + time;
	}

}
