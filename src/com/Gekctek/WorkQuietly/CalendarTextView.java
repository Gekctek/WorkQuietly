package com.Gekctek.WorkQuietly;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

public class CalendarTextView extends CheckedTextView{
	private String name;
	private String filterText;
	private boolean enabled;

    public CalendarTextView(Context context) {
        this(context, null);
    }

    public CalendarTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CalendarTextView, defStyle, 0);

        String name = a.getString(R.styleable.CalendarTextView_calendar_name);
        setName(name);
        String filterText = a.getString(R.styleable.CalendarTextView_calendar_filter_text);
        setFilterText(filterText);
        boolean enabled = a.getBoolean(R.styleable.CalendarTextView_calendar_enabled, true);
        setEnabled(enabled);
    }
    
    public void setName(String name){
    	this.name = name;
    }
    
    public void setFilterText(String filterText){
    	this.filterText = filterText;
    }
    
    public void setEnabled(boolean enabled){
    	this.enabled = enabled;
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		int x = getMeasuredWidth();
		int y = getMeasuredHeight();

		
		float textSize = 30;
		float padding = 20;
		Paint textP = new Paint();
		textP.setTextSize(textSize);
		textP.setTextAlign(Align.LEFT);
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
		
		int maxLength = 15;
		int nameLength = name.length() > maxLength ? maxLength : name.length();
		canvas.drawText(name, 0, nameLength, padding, y/2 + padding, textP);
	}
	
	
}
