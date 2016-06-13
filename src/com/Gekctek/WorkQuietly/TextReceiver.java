package com.Gekctek.WorkQuietly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class TextReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		if(QuietService.isQuiet() && settings.getBoolean("autoResponse", false)){
			Object messages[] = (Object[]) bundle.get("pdus");
			for (Object msg : messages) {
			    SmsMessage sms = SmsMessage.createFromPdu((byte[]) msg);
				String number = sms.getDisplayOriginatingAddress();
				String responseMsg = settings.getString("autoResponseText", "I am busy at the moment, I'll get back to you");
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(number, null, responseMsg, null, null);
			}
		}
		
	}

}
