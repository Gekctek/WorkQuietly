package com.Gekctek.WorkQuietly;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class QuickQuietWidget extends View{
	private int progress;
	
	//constructor 1 required for in-code creation
	public QuickQuietWidget(Context context){
		super(context);
		progress = 0;
	}

	//constructor 2 required for inflation from resource file
	public QuickQuietWidget(Context context, AttributeSet attr){
		super(context,attr);
		progress = 0;
	}

	//constructor 3 required for inflation from resource file
	public QuickQuietWidget(Context context, AttributeSet attr, int defaultStyles){
		super(context, attr, defaultStyles);
		progress = 0;
	}
	
	
	@Override
	protected void onMeasure(int widthSpec, int heightSpec){
		int measuredWidth = MeasureSpec.getSize(widthSpec);
		int measuredHeight = MeasureSpec.getSize(heightSpec);
	
		/*measuredWidth and measured height are your view boundaries. You need to change these values based on your requirement E.g.
	
		if you want to draw a circle which fills the entire view, you need to select the Min(measuredWidth,measureHeight) as the radius.
	
		Now the boundary of your view is the radius itself i.e. height = width = radius. */
	
		/* After obtaining the height, width of your view and performing some changes you need to set the processed value as your view dimension by using the method setMeasuredDimension */
	
		setMeasuredDimension( measuredWidth, measuredHeight);
	
		/* If you consider drawing circle as an example, you need to select the minimum of height and width and set that value as your screen dimensions
	
		int d=Math.min(measuredWidth, measuredHeight);
	
		setMeasuredDimension(d,d); */
	}
	
	@Override

	protected void onDraw(Canvas canvas){

		//get the size of your control based on last call to onMeasure
	
		int height = getMeasuredHeight();
	
		int width = getMeasuredWidth();
	
		// Now create a paint brush to draw your widget
	
		Paint mTextPaint=new Paint();
	
		mTextPaint.setStrokeWidth(10);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setARGB(255,85,85,85);
		
		RectF oval = new RectF(0,0,width,height);
		canvas.drawArc(oval, -45, 270, true, mTextPaint);
		
		mTextPaint.setARGB(255,51,181,229);
		
		RectF oval2 = new RectF(0,0,width,height);
		float degrees = 50.0f/100.0f*270.0f;
		canvas.drawArc(oval2, -45, degrees, true, mTextPaint);
		
		mTextPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		RectF oval3 = new RectF(5,5,width-5,height-5);
		canvas.drawArc(oval3, -45, 270, false, mTextPaint);
		
		//ImageView thumb = (ImageView)findViewById(R.drawable.seek_thumb_normal);
		//thumb.draw(canvas);


		

	}
	
	public int getProgress(){
		return progress;
	}
	
	public void setProgress(int p){
		if(p <= 100 && p >= 0)
			progress = p;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
	    switch (action) {
	    case MotionEvent.ACTION_DOWN:
	    	Toast.makeText(getContext(), "DErP", Toast.LENGTH_SHORT).show();
	    	break;
	    case MotionEvent.ACTION_UP:
	    	Toast.makeText(getContext(), "Slerp", Toast.LENGTH_SHORT).show();
	    	break;
	    }
	    return true;
	}
	
}