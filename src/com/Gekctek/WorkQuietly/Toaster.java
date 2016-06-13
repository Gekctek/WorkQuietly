package com.Gekctek.WorkQuietly;

import android.content.Context;
import android.widget.Toast;

public class Toaster implements Runnable{
	private String toast;
	private Context mContext;
	
	public Toaster(Context context, String toast){
		this.toast = toast;
		this.mContext = context;
	}
	
	public void run(){
		Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	}

}
