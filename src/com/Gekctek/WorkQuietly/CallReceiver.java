package com.Gekctek.WorkQuietly;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallReceiver extends PhoneStateListener {
	SharedPreferences settings;

	private Context context;
	public CallReceiver(Context context){
		this.context = context;
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);	
		if(state == TelephonyManager.CALL_STATE_RINGING){
			boolean alwaysUseWhitelist = settings.getBoolean("alwaysUseWhitelist", false);
			if((QuietService.isQuiet() || alwaysUseWhitelist) && isOnWhitelist(incomingNumber)){
				QuietService.whitelist = true;
				QuietService.whitelistStarted = true;
				QuietService.whitelistRinger = audioManager.getRingerMode();
				if(!settings.getBoolean("whitelistIRM", false) || alwaysUseWhitelist)
					audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				else
					audioManager.setRingerMode(settings.getInt("IRM", AudioManager.RINGER_MODE_NORMAL));
			}
		}
		else if(state == TelephonyManager.CALL_STATE_IDLE && QuietService.whitelistStarted){
			QuietService.whitelist = true;
			QuietService.whitelistStarted = false;
			audioManager.setRingerMode(QuietService.whitelistRinger);
		}
		super.onCallStateChanged(state, incomingNumber);	
	}
	
	private boolean isOnWhitelist(String incomingNumber){
		DBHelper db = new DBHelper(context);
		db.open();
		Cursor c = db.rawQuery("SELECT * FROM WHITELIST WHERE number=?", new String[]{incomingNumber});
		return c.getCount() > 0;
	}
}
