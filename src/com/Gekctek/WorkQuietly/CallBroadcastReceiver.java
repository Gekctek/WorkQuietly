package com.Gekctek.WorkQuietly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallBroadcastReceiver extends BroadcastReceiver {
    TelephonyManager telephony;

    public void onReceive(Context context, Intent intent) {
        CallReceiver phoneListener = new CallReceiver(context);
        telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void onDestroy() {
        //telephony.listen(null, PhoneStateListener.LISTEN_NONE);
    }

}