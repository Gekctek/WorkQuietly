package com.Gekctek.WorkQuietly;


import java.util.Calendar;
import java.util.Date;
import android.database.Cursor;


public class OneTimeQuietActivity extends QuietActivity{
	private Calendar startDate;
	private Calendar endDate;
	
	
	public OneTimeQuietActivity(String name, long startTime, long endTime, boolean silent, boolean enabled, Date date, boolean GMT){
		super(name, startTime, endTime, silent, enabled, GMT, true);
		startDate = Calendar.getInstance();
		startDate.setTimeInMillis(startTime);
		endDate = Calendar.getInstance();
		endDate.setTimeInMillis(endTime);
	}
	
	public OneTimeQuietActivity(Cursor query){
		super(query);
		long sD = query.getLong(query.getColumnIndex("startDate"));
		long eD = query.getLong(query.getColumnIndex("endDate"));
		startDate = Calendar.getInstance();
		startDate.setTimeInMillis(sD);
		endDate = Calendar.getInstance();
		endDate.setTimeInMillis(eD);
	}
	
	
	public Date getDate(int index){
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(startDate.getTimeInMillis());
		start.set(Calendar.HOUR_OF_DAY, getHour(0, true, false));
		start.set(Calendar.MINUTE, getMinutes(0));
		
		Calendar end = Calendar.getInstance();
		end.setTimeInMillis(endDate.getTimeInMillis());
		end.set(Calendar.HOUR_OF_DAY, getHour(1, true, false));
		end.set(Calendar.MINUTE, getMinutes(1));
		
		if(index == 0)
			return start.getTime();
		else if(index == 1)
			return end.getTime();
		else 
			throw new IllegalArgumentException("Index needs to be 1 or 0 not "+index);
	}
	
	public void setStartDate(long date){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		
		startDate.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
		startDate.set(Calendar.MONTH, c.get(Calendar.MONTH));
		startDate.set(Calendar.YEAR, c.get(Calendar.YEAR));
	}
	
	public void setEndDate(long date){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		
		endDate.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
		endDate.set(Calendar.MONTH, c.get(Calendar.MONTH));
		endDate.set(Calendar.YEAR, c.get(Calendar.YEAR));
	}
	
	public long getTimeInMillis(int index){
		if(!(index == 0 || index == 1))
			throw new IllegalArgumentException("Index needs to be 1 or 0 not "+index);

		return getDate(index).getTime();
	}
}
