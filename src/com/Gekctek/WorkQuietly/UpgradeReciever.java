package com.Gekctek.WorkQuietly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpgradeReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		intent = new Intent(context, QuietService.class).putExtra("Refresh", true);
		context.startService(intent);
	}

}
