package com.Gekctek.WorkQuietly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.IntentService;

public class VolFreeze extends IntentService{
	private AudioManager aM;
	private SharedPreferences settings;
	private Context mContext;
	private Handler mHandler;
	
	public VolFreeze(){
		this("VolFreeze");
	}
	
	public VolFreeze(String name){
		super(name);
	}
	
	public void onCreate() {
		super.onCreate();
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		aM = (AudioManager)getSystemService(Context.AUDIO_SERVICE);	
		mContext = getBaseContext();
		mHandler = new Handler();
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		if(!QuietService.goingQuiet && !QuietService.refreshing && !QuietService.whitelist){
			if(QuietService.isQuiet()){
				aM.setRingerMode(QuietService.quietRingerMode);
				mHandler.post(new Toaster(mContext, "Volume is frozen in Quiet Mode"));   
			}else if(aM.getRingerMode() != QuietService.initialRingerMode && settings.getBoolean("enableSQ", false)){
				if(!VolChangeDialog.isRunning())
					startActivity(new Intent(getApplicationContext(), VolChangeDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}
		}
		if(QuietService.goingQuiet){
			QuietService.goingQuiet = false;
		}
		if(QuietService.refreshing){
			QuietService.refreshing = false;
		}
		if(QuietService.whitelist){
			QuietService.whitelist = false;
		}
		stopSelf();
	}
}
