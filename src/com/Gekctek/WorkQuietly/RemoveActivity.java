package com.Gekctek.WorkQuietly;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

public class RemoveActivity extends Activity implements OnItemClickListener, OnClickListener {
	private String[] activityList;
	private ListView activityLV;
	private List<String> removeList;
	private int amountSelected;
	private DBHelper db;
	private int AType;
    
	
	//Initializers
	
	//Pre:
	//Post:	Creates the RemoveActivity GUI
	//		Initialized Global variables
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_activity);
        setTitle("Delete Events");
        Bundle b = getIntent().getExtras();
        AType = b.getInt("ActivityType", WorkQuietlyActivity.RECURRING);

        db = new DBHelper(this);
        activityList = getActivities();
        
        removeList = new ArrayList<String>();
        		
        //Create lists		
        activityLV = (ListView)findViewById(R.id.activityList);
        activityLV.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, activityList));
        activityLV.setOnItemClickListener(this);
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.remove_menu, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.eventDelete:
			if(removeList.size() > 0){
				db.open();
				for(String name : removeList){
					switch(AType){
					case WorkQuietlyActivity.ONETIME:
						db.deleteO(name);
						break;
					case WorkQuietlyActivity.CALENDAR:
						db.deleteC(name);
						break;
					case WorkQuietlyActivity.GPS:
						db.deleteGPS(name);
						break;
					default:
						db.deleteR(name);
						break;
					}
					db.deleteR(name);
				}
				db.close();
			
				setResult(RESULT_OK);
				finish();
				return true;
			}
			

		default:
			return false;
		}
	}
	
	//Get Methods
	
	//Pre:
	//Post:	Returns a string array of all the quiet activities' names
	private String[] getActivities() {
		db.open();
		String q = "SELECT * from ";
		switch(AType){
		case WorkQuietlyActivity.ONETIME:
			q += "OneTime";
			break;
		case WorkQuietlyActivity.CALENDAR:
			q += "CalendarFilter";
			break;
		case WorkQuietlyActivity.GPS:
			q += "GPS";
			break;
		default:
			q += "Recurring";
			break;
		}
		Cursor activities = db.rawQuery(q, null);
		int a = activities.getCount();
		String[] activitiesList = new String[a];
		activities.moveToFirst();
		for(int i=0; i < a; i++){
			activitiesList[i] = activities.getString(1);
			activities.moveToNext();
		}
		activities.close();
		db.close();
		return activitiesList;
	}
	
	
	
	
	//Listeners

	//Pre:
	//Post:	Listener for a short activity list item click
	//		Marks/UnMarks the activity for removal
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		CheckedTextView c = (CheckedTextView) arg1;
		if(!removeList.contains(c.getText())){
			removeList.add((String)c.getText());
		}else{
			removeList.remove((String)c.getText());
		}	

		c.toggle();
	}

	//Pre:
	//Post:	Listener for clicking on the delete button
	//		Removes the marked activities from the database
	public void onClick(View v) {
		
	}
	
	
	
	

}