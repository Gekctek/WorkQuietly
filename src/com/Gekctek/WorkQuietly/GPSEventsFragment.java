package com.Gekctek.WorkQuietly;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class GPSEventsFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener{
	private GPSFilter[] activityList;
	private ListView activityLV;
	private int requestCode;
	private ViewGroup view;
	private Context mContext;
	private SharedPreferences settings;

	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    activityList = getGPSEvents();  
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	

	    view = new LinearLayout(getActivity());
		//Create lists	
		activityLV = new ListView(getActivity());
	    activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "GPS"));
	    activityLV.setOnItemClickListener(this);
	    activityLV.setOnItemLongClickListener(this);
	    
	    view.addView(activityLV);
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity.getBaseContext();
	}

	private GPSFilter[] getGPSEvents(){
		DBHelper db = new DBHelper(getActivity());
		db.open();
		Cursor activities = db.rawQuery("SELECT * FROM GPS", null);
		int a = activities.getCount();
		GPSFilter[] activitiesList = new GPSFilter[a];
		activities.moveToFirst();
		for(int i=0; i < a; i++){
			activitiesList[i] = new GPSFilter(activities);
			activities.moveToNext();
		}
		activities.close();
		db.close();
		return activitiesList;
		
	}
	
	//Pre:
		//Post:	Listener for short click on the activity list
		//		Starts ViewActivity
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent editActivity = new Intent(getActivity(), AddGPSActivity.class);
			editActivity.putExtra("Name", ((GPSFilter)arg0.getAdapter().getItem(arg2)).getName());
			editActivity.putExtra("Edit", true);
			startActivityForResult(editActivity, requestCode);		
		}

		//Pre:
		//Post:	Listener for long click on the activity list
		//		Starts EditActivity
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			final String name = ((GPSFilter)arg0.getAdapter().getItem(arg2)).getName();
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
				.setTitle("Delete")
				.setMessage("Do you want to delete this Quiet Activity?")
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						DBHelper db = new DBHelper(getActivity());
						db.open();
						db.deleteGPS(name);
						db.close();
						mContext.startService(new Intent(mContext, QuietService.class).putExtra("Refresh", true));
						activityList = getGPSEvents();
					    activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "GPS"));
				        if(activityList.length == 0){
				        	 ((LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE)).removeUpdates(WorkQuietlyActivity.gpsLocationListener);
				        }
					}
				});
			dialog.create().show();
			return false;
		}

		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        Log.d("Refresh","onActivityResult and resultCode = "+resultCode);
	        super.onActivityResult(requestCode, resultCode, data);
	        if(resultCode==Activity.RESULT_OK){
				if(settings.getBoolean("enableWQ", true) && QuietService.isQuiet())
					mContext.startService(new Intent(mContext, QuietService.class).putExtra("Refresh", true));
	        }
		}
		
		//Pre:
		//Post:	Refreshes the GUI activity list when returning to this activity
		public void onResume(){
			super.onResume();
			if(!settings.getBoolean("enableGPS", false))
				Toast.makeText(getActivity(), "GPS events are disabled!", Toast.LENGTH_SHORT).show();
			
			activityList = getGPSEvents();
	        activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "GPS"));
		}
}
