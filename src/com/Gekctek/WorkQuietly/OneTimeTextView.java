package com.Gekctek.WorkQuietly;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CheckedTextView;



public class OneTimeTextView extends CheckedTextView{
	private String startTime;
	private String endTime;
	private String startDate;
	private String endDate;
	private String name;
	private boolean enabled;

    public OneTimeTextView(Context context) {
        this(context, null);
    }

    public OneTimeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OneTimeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.OneTimeTextView, defStyle, 0);

        String sT = a.getString(R.styleable.OneTimeTextView_one_time_start_time);
        setStartTime(sT);
        String eT = a.getString(R.styleable.OneTimeTextView_one_time_end_time);
        setEndTime(eT);
        String name = a.getString(R.styleable.OneTimeTextView_one_time_name);
        setName(name);
        boolean enabled = a.getBoolean(R.styleable.OneTimeTextView_one_time_enabled, true);
        setEnabled(enabled);
    }
    
    public void setStartTime(String time){
    	startTime = time;
    }
    
    public void setEndTime(String time){
    	endTime = time;
    }
    
    public void setStartDate(String date){
    	this.startDate = date;
    }
    
    public void setEndDate(String date){
    	this.endDate = date;
    }
    
    public void setName(String name){
    	this.name = name;
    }
    
    public void setEnabled(boolean enabled){
    	this.enabled = enabled;
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
		float timeOffset = lineOffset + padding;
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
		canvas.drawText(startDate + " at " + startTime, timeOffset, textSize + padding, textP);
		canvas.drawText(endDate + " at " + endTime, timeOffset, y - padding, textP);
	}
	
	
}