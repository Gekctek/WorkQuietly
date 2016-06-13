package com.Gekctek.WorkQuietly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class BootUpReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent();
		serviceIntent.putExtra("Boot", true);
		serviceIntent.setAction("com.Gekctek.WorkQuietly.QuietService");
		context.startService(serviceIntent);

        CallReceiver phoneListener = new CallReceiver(context);
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


	}

}
