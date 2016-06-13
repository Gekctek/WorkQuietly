package com.Gekctek.WorkQuietly;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

public class GPSTextView extends CheckedTextView{
	private String name;
	private float radius;
	private double latitude;
	private double longitude;
	private boolean enabled;

    public GPSTextView(Context context) {
        this(context, null);
    }

    public GPSTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GPSTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GPSTextView, defStyle, 0);

        String name = a.getString(R.styleable.GPSTextView_gps_name);
        setName(name);
        float radius = a.getFloat(R.styleable.GPSTextView_gps_radius, 1000);
        setRadius(radius);
        float latitude = a.getFloat(R.styleable.GPSTextView_gps_latitude, 0);
        setLatitude(latitude);
        float longitude = a.getFloat(R.styleable.GPSTextView_gps_longitude, 0);
        setLongitude(longitude);
        boolean enabled = a.getBoolean(R.styleable.GPSTextView_gps_enabled, true);
        setEnabled(enabled);
    }
    
    public void setName(String name){
    	this.name = name;
    }
    
    public void setRadius(float radius){
    	this.radius = radius;
    }
    
    public void setEnabled(boolean enabled){
    	this.enabled = enabled;
    }
    
    public void setLatitude(float latitude){
    	this.latitude = latitude;
    }
    
    public void setLongitude(float longitude){
    	this.longitude = longitude;
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		int x = getMeasuredWidth();
		int y = getMeasuredHeight();
		
		float lineOffset = 2*x/5;
		
		Paint linePaint = new Paint();
		if(enabled)
			linePaint.setColor(Color.WHITE);
		else
			linePaint.setColor(Color.GRAY);
		
		canvas.drawLine(lineOffset, 10, lineOffset, y-10, linePaint);
		
		float textSize = 30;
		float padding = 20;
		float timeOffset = lineOffset + padding*4;
		Paint textP = new Paint();
		if(enabled)
			textP.setColor(Color.WHITE);
		else{
			textP.setColor(Color.GRAY);
			Paint tempP = new Paint();
			tempP.setColor(Color.RED);
			tempP.setStrokeWidth(5);
			canvas.drawLine(x-60, y/2-20, x-20, y/2+20, tempP);
			canvas.drawLine(x-20, y/2-20, x-60, y/2+20, tempP);
		}
		
		
		textP.setTextSize(textSize);
		int maxLength = 15;
		int nameLength = name.length() > maxLength ? maxLength : name.length();
		canvas.drawText(name, 0, nameLength, padding, y/2 + padding, textP);
		canvas.drawText("Radius: "+radius, timeOffset, (y + padding)/2, textP);
	}
	
	
}
