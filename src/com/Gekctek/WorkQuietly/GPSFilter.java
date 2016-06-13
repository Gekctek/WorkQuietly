package com.Gekctek.WorkQuietly;

import android.database.Cursor;

public class GPSFilter {
	private String name;
	private float radius;
	private float latitude;
	private float longitude;
	private boolean enabled;
	private boolean silent;
	
	public GPSFilter(String name, float radius, float latitude, float longitude, boolean enabled, boolean silent){
		this.name = name;
		this.radius = radius;
		this.latitude = latitude;
		this.longitude = longitude;
		this.enabled = enabled;
		this.silent = silent;
	}
	
	
	public GPSFilter(Cursor filter) {
		this.name = filter.getString(filter.getColumnIndex("name"));
		this.radius = filter.getFloat(filter.getColumnIndex("radius"));
		this.latitude = filter.getFloat(filter.getColumnIndex("latitude"));
		this.longitude = filter.getFloat(filter.getColumnIndex("longitude"));
		this.enabled = filter.getInt(filter.getColumnIndex("enabled")) == 1;
		this.silent = filter.getInt(filter.getColumnIndex("silent")) == 1;
	}
	



	//Get Methods
	public String getName(){
		return name;
	}	
	
	public boolean getEnabled(){
		return enabled;
	}
	
	public boolean getSilent() {
		return silent;
	}
	
	public float getRadius(){
		return radius;
	}
	
	public float getLatitude(){
		return latitude;
	}
	
	public float getLongitude(){
		return longitude;
	}
}