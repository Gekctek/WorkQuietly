package com.Gekctek.WorkQuietly;

import android.database.Cursor;

public class CalendarFilter {
	private String name;
	private String filters;
	private boolean enabled;
	private boolean silent;
	
	public CalendarFilter(String name, boolean enabled, boolean silent){
		this.name = name;
		this.filters = "";
		this.enabled = enabled;
		this.silent = silent;
	}
	
	
	public CalendarFilter(Cursor filter) {
		this.name = filter.getString(filter.getColumnIndex("name"));
		this.filters = filter.getString(filter.getColumnIndex("filters"));
		this.enabled = filter.getInt(filter.getColumnIndex("enabled")) == 1;
		this.silent = filter.getInt(filter.getColumnIndex("silent")) == 1;
	}
	
	public void add(String filter){
		filters += filter;
	}
	
	public void add(FilterView v){
		if(!v.getFilter().equalsIgnoreCase("None Available"))
			add(v.getFilter());
	}



	//Get Methods
	public String getName(){
		return name;
	}
	
	public String getFilters(){
		return filters;
	}
	
	public boolean getEnabled(){
		return enabled;
	}
	
	public boolean getSilent() {
		return silent;
	}
	
	
	public boolean check(Cursor event){
		return check(event, filters);
	}
	
	public boolean check(Cursor event, String filter){
		String[] s = filter.split("~.~", 2);
		String[] f = s[0].split(":");
		
		if(f.length < 2)
			return false;
		
		if(f[0].equalsIgnoreCase("Keyword")){
			String a = event.getString(event.getColumnIndex("Title"));
			if(f.length == 3 && f[2].equalsIgnoreCase("and")){
				return a.toLowerCase().contains(f[1].toLowerCase()) && check(event, s[1]);
			}else if(f.length == 3 && f[2].equalsIgnoreCase("or")){
				return a.toLowerCase().contains(f[1].toLowerCase()) || check(event, s[1]);
			}else{
				return a.toLowerCase().contains(f[1].toLowerCase());
			}
		}else{
		
			if(f[0].equalsIgnoreCase("Location"))
				f[0] = "Event_Location";
			else if(f[0].equalsIgnoreCase("Recurrence"))
				f[0] = "RRULE";
			
			f[0] = f[0].toLowerCase();
			
			if(event.isBeforeFirst())
				event.moveToFirst();
			
			String a = event.getString(event.getColumnIndex(f[0]));
			if(f[1].equalsIgnoreCase("availability_busy")){
				f[1] = "0";
			}else if(f[1].equalsIgnoreCase("availability_free")){
				f[1] = "1";
			}else if(f[1].equalsIgnoreCase("availability_tentative")){
				f[1] = "2";
			}
			
			if(f.length == 3 && f[2].equalsIgnoreCase("and")){
				return a.equalsIgnoreCase(f[1]) && check(event, s[1]);
			}else if(f.length == 3 && f[2].equalsIgnoreCase("or")){
				return a.equalsIgnoreCase(f[1]) || check(event, s[1]);
			}else{
				return a.equalsIgnoreCase(f[1]);
			}
		}
	}



}