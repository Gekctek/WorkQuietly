package com.Gekctek.WorkQuietly;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class QuickQuietDialogFragment extends DialogFragment implements OnSeekBarChangeListener, OnClickListener{
	private View view;
	private int qQHours = 1;
	private int qQMinutes = 0;
	private boolean silent = false;
	private Context mContext;
	
	public static QuickQuietDialogFragment newInstance(){
		QuickQuietDialogFragment f = new QuickQuietDialogFragment();
		Bundle args = new Bundle();
		f.setArguments(args);
		
		return f;
	}
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		mContext = inflater.getContext();
		getDialog().setTitle("Quick Quiet");
		view = inflater.inflate(R.layout.quick_quiet_dialog, container, false);

    	((SeekBar)view.findViewById(R.id.qqHourBar)).setOnSeekBarChangeListener(this);
		((SeekBar)view.findViewById(R.id.qqMinBar)).setOnSeekBarChangeListener(this);
		((Button)view.findViewById(R.id.qqStart)).setOnClickListener(this);
		return view;
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(seekBar.getId() == R.id.qqHourBar){
			TextView hours = (TextView) view.findViewById(R.id.qqHour);
			qQHours = progress;
			hours.setText(""+progress);
		}else{
			TextView minutes = (TextView) view.findViewById(R.id.qqMin);
			minutes.setText(""+progress);
			qQMinutes = progress;
		}
	}



	public void onStartTrackingTouch(SeekBar seekBar) {
				 
	}



	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}

	public void onClick(View v) {
		mContext.startService(new Intent(mContext, QuietService.class).putExtra("QQ", true).putExtra("qQHours", this.qQHours).putExtra("qQMinutes", this.qQMinutes).putExtra("qQSilent", this.silent));
		dismiss();
	}
	
	
	
}
