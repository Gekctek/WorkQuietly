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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class OneTimeEventsFragment extends Fragment implements OnItemLongClickListener, OnItemClickListener{
	private OneTimeQuietActivity[] activityList;
	private ListView activityLV;
	private ViewGroup view;
	private int requestCode;
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
	    
	    //Create lists	
		//activityLV = (ListView) getActivity().findViewById(R.id.activityList);
		activityLV = new ListView(getActivity());
	    activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "OneTime"));
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
	private OneTimeQuietActivity[] getActivities() {
		DBHelper db = new DBHelper(getActivity());
		db.open();
		Cursor activities = db.rawQuery("SELECT * from OneTime", null);
		OneTimeQuietActivity[] activitiesList = new OneTimeQuietActivity[activities.getCount()];
		activities.moveToFirst();
		for(int i=0; i < activities.getCount(); i++){
			activitiesList[i] = new OneTimeQuietActivity(activities);
			activities.moveToNext();
		}
		activities.close();
		db.close();
		return activitiesList;
	}

	//Pre:
	//Post:	Listener for long click on the activity list
	//		Starts EditActivity
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final String name = (String)((OneTimeQuietActivity)arg0.getAdapter().getItem(arg2)).getName();
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
					db.deleteO(name);
					db.close();
					mContext.startService(new Intent(mContext, QuietService.class).putExtra("Refresh", true));
					activityList = getActivities();
			        activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "OneTime"));
				}
			});
		dialog.create().show();
		return false;
	}

	//Pre:
	//Post:	Listener for short click on the add button
	//		Starts AddActivity
	public void onClick(View v) {
			Intent addActivity = new Intent(getActivity(), AddOneTimeActivity.class);
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
        activityLV.setAdapter(new ActivityArrayAdapter(getActivity(), android.R.layout.simple_list_item_checked, activityList, "OneTime"));
	}
	
	//Pre:
	//Post:	Listener for short click on the activity list
	//		Starts ViewActivity
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent editActivity = new Intent(getActivity(), AddOneTimeActivity.class);
		editActivity.putExtra("name", (String)((OneTimeQuietActivity)arg0.getAdapter().getItem(arg2)).getName());
		editActivity.putExtra("Edit", true);
		startActivityForResult(editActivity, requestCode);		
	}

}
