package com.Gekctek.WorkQuietly;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class VolChangeDialog extends Activity{
	private AudioManager aM;
	private static boolean isRunning;
	private SharedPreferences settings;
	private CountDownTimer cDT;
	private AlertDialog dialog;
	
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 aM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		 isRunning = true;
		 settings = PreferenceManager.getDefaultSharedPreferences(this);
		 int timeout = Integer.parseInt(settings.getString("timerSQ", "10"));
		 cDT = new CountDownTimer(timeout*1000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
				messageView.setText("Change the volume?  ("+millisUntilFinished/1000+")");
			}
			
			@Override
			public void onFinish() {
				aM.setRingerMode(QuietService.initialRingerMode);
				finish();				
			}
		};
	 }
	 
	 protected void onStart() {
		 super.onStart();
		 dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
		 		.setTitle("Volume Freeze")
		 		.setMessage("Change the volume?")
         		.setPositiveButton("Change",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	QuietService.initialRingerMode = aM.getRingerMode();
                        	finish();
                        }
                    }
                )
                .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
            				aM.setRingerMode(QuietService.initialRingerMode);
            				finish();
                        }
                    }
                ).create();
		 
		 dialog.show();
		 
		 cDT.start();
	 }
	 
	 public static boolean isRunning(){
		 return isRunning;
	 }
	 
	 @Override
	protected void onDestroy() {
		super.onDestroy();
		isRunning = false;
	}
	
}