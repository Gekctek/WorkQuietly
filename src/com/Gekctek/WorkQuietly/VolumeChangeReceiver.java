package com.Gekctek.WorkQuietly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VolumeChangeReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, VolFreeze.class));
	}
	
}