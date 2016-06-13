package com.Gekctek.WorkQuietly;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class RecurringEventsFragment extends Fragment implements OnItemClickListener, OnClickListener, OnItemLongClickListener{
	private QuietActivity[] activityList;
	private ListView activityLV;
	private int requestCode;
	private ViewGroup view;
	private Context mContext;
	private SharedPreferences settings;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		activityList = getActivities();    
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
	    view = new LinearLayout(getActivity());
		activityLV = new ListView(getActivity());
	    activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "Recurring"));
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
	
	//Pre:
	//Post:	Returns a string array representing all the activity names
	private QuietActivity[] getActivities() {
		DBHelper db = new DBHelper(getActivity());
		db.open();
		Cursor activities = db.rawQuery("SELECT * from Recurring", null);
		Cursor advanced = db.rawQuery("SELECT * from RecurringAdvanced", null);
		QuietActivity[] activitiesList = new QuietActivity[activities.getCount()+advanced.getCount()];
		activities.moveToFirst();
		for(int i=0; i < activities.getCount(); i++){
			activitiesList[i] = new RecurringQuietActivity(activities);
			activities.moveToNext();
		}
		advanced.moveToFirst();
		for(int i = activities.getCount(); i < activitiesList.length; i++){
			activitiesList[i] = new RecurringAdvancedQuietActivity(advanced);
			advanced.moveToNext();
		}
		advanced.close();
		activities.close();
		db.close();
		return activitiesList;
	}


	
	//Pre:
		//Post:	Listener for short click on the activity list
		//		Starts ViewActivity
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			QuietActivity activity = (QuietActivity)arg0.getAdapter().getItem(arg2);
			Intent editActivity;
			if(activity instanceof RecurringQuietActivity)
				editActivity = new Intent(getActivity(), AddRecurringActivity.class);
			else
				editActivity = new Intent(getActivity(), AddRecurringAdvancedActivity.class);
			
			editActivity.putExtra("name", (String)activity.getName());
			editActivity.putExtra("Edit", true);
			startActivityForResult(editActivity, requestCode);		
		}

		//Pre:
		//Post:	Listener for long click on the activity list
		//		Starts EditActivity
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			final QuietActivity activity = (QuietActivity)arg0.getAdapter().getItem(arg2);
			final String name = (String)activity.getName();
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
						if(activity instanceof RecurringAdvancedQuietActivity)
							db.deleteRA(name);
						else
							db.deleteR(name);
						db.close();
						mContext.startService(new Intent(mContext, QuietService.class).putExtra("Refresh", true));
						activityList = getActivities();
				        activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "Recurring"));
					}
				});
			dialog.create().show();
			return false;
		}

		//Pre:
		//Post:	Listener for short click on the add button
		//		Starts AddActivity
		public void onClick(View v) {
				Intent addActivity = new Intent(getActivity(), AddRecurringActivity.class);
				addActivity.putExtra("Edit", false);
				startActivityForResult(addActivity, requestCode);
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
			activityList = getActivities();
	        activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "Recurring"));
		}
		
		
}
