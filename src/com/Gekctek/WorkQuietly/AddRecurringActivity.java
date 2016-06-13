package com.Gekctek.WorkQuietly;

import java.util.Calendar;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddRecurringActivity extends Activity {
	protected EditText activityNameBox;
	protected Button startTimeButton;
	protected ChangeableAnalogClock startTimeClock;
	protected Button endTimeButton;
	protected ChangeableAnalogClock endTimeClock;
	protected Calendar calendar = Calendar.getInstance();
	protected RecurringQuietActivity qA;
	protected TimePickerDialog.OnTimeSetListener t;
	protected TimePickerDialog.OnTimeSetListener t2;
	protected CheckBox[] dOfW;
	protected CheckBox vibrateCheck;
	protected DBHelper db;	
	private Switch enabledSwitch;
	private String activityName;
	private boolean edit;
	private SharedPreferences settings;
	
	
	
	
	//Initializers
	
	//Pre:
	//Post:	Creates the GUI and sets all fields and listeners
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		
		edit = false;
		if(b != null)
			edit = getIntent().getExtras().getBoolean("Edit", false);
		
	    setContentView(R.layout.add_recurring_activity);
	    
			
		setXML();
		setListeners();
        db = new DBHelper(this);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int hourOfDayPlusDefActLen = hourOfDay+1;
        int minutes = 0;
        if(hourOfDayPlusDefActLen > 23){
        	hourOfDayPlusDefActLen = 23;
        	minutes = 59;
        }
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MILLISECOND, 0);
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, hourOfDayPlusDefActLen);
        endTime.set(Calendar.MINUTE, minutes);
		endTime.set(Calendar.SECOND, 0);
		endTime.set(Calendar.MILLISECOND, 0);
        qA = new RecurringQuietActivity("", startTime.getTimeInMillis(), endTime.getTimeInMillis(), false, true, new boolean[7], false);
        if(!edit){
			startTimeButton.setText(qA.dateString(0));
			endTimeButton.setText(qA.dateString(1));
			enabledSwitch.setChecked(true);
		}else{
			activityName = getIntent().getExtras().getString("name");
			setFields();
		}

		startTimeClock.setTime(qA.getHour(0), qA.getMinutes(0), false);
		endTimeClock.setTime(qA.getHour(1), qA.getMinutes(1) ,false);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	//Pre:
	//Post:	Sets the fields in the GUI to the current values of the activity
	private void setFields(){
		String[] args = new String[]{activityName};
		db.open();
		Cursor query = db.rawQuery("SELECT * FROM Recurring where name=?", args);
		query.moveToFirst();
		dOfW[0].setChecked(query.getInt(query.getColumnIndex("Monday")) != 0);
		dOfW[1].setChecked(query.getInt(query.getColumnIndex("Tuesday")) != 0);
		dOfW[2].setChecked(query.getInt(query.getColumnIndex("Wednesday")) != 0);
		dOfW[3].setChecked(query.getInt(query.getColumnIndex("Thursday")) != 0);
		dOfW[4].setChecked(query.getInt(query.getColumnIndex("Friday")) != 0);
		dOfW[5].setChecked(query.getInt(query.getColumnIndex("Saturday")) != 0);
		dOfW[6].setChecked(query.getInt(query.getColumnIndex("Sunday")) != 0);
		enabledSwitch.setChecked(query.getInt(query.getColumnIndex("enabled")) != 0);
		vibrateCheck.setChecked(query.getInt(query.getColumnIndex("silent")) != 1);
		qA.setTimeInMillis(0, query.getLong(query.getColumnIndex("startDate")));
		qA.setTimeInMillis(1, query.getLong(query.getColumnIndex("endDate")));
		startTimeButton.setText(qA.dateString(0));
		endTimeButton.setText(qA.dateString(1));

		activityNameBox.setText(activityName);
		query.close();
		db.close();
	}
	
	//Pre:
	//Post:	Sets classes variables to corresponding GUI/XML elements
	protected void setXML(){
		dOfW = new CheckBox[7];
		activityNameBox = 		(EditText) findViewById(R.id.activityName);
		startTimeButton = 		(Button)findViewById(R.id.startTimeButton);
		startTimeClock =		(ChangeableAnalogClock)findViewById(R.id.startTimeClock);
		endTimeButton = 		(Button)findViewById(R.id.endTimeButton);
		endTimeClock =			(ChangeableAnalogClock)findViewById(R.id.endTimeClock);
		dOfW[0] = 				(CheckBox)findViewById(R.id.monCheck);
		dOfW[1] = 				(CheckBox)findViewById(R.id.tuesCheck);
		dOfW[2] = 				(CheckBox)findViewById(R.id.wedCheck);
		dOfW[3] = 				(CheckBox)findViewById(R.id.thursCheck);
		dOfW[4] = 				(CheckBox)findViewById(R.id.friCheck);
		dOfW[5] = 				(CheckBox)findViewById(R.id.satCheck);
		dOfW[6] = 				(CheckBox)findViewById(R.id.sunCheck);
		vibrateCheck = 			(CheckBox)findViewById(R.id.vibrateCheck);
		enabledSwitch = 		(Switch)findViewById(R.id.setEnabled);
	}
	
	//Pre:	
	//Post: Sets all the buttons' listeners 
	protected void setListeners(){
		startTimeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new TimePickerDialog(AddRecurringActivity.this, t, qA.getHour(0, true, false), qA.getMinutes(0), settings.getBoolean("clockFormat", false)).show();				
			}
		});
		
		endTimeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new TimePickerDialog(AddRecurringActivity.this, t2, qA.getHour(1, true, false), qA.getMinutes(1), settings.getBoolean("clockFormat", false)).show();				
			}
		});
		
		t = new TimePickerDialog.OnTimeSetListener(){
			public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
				
				//Sets start time to chosen time if not 11:59pm
				if(!(hourOfDay == 23 && minutes == 59)){
					qA.setHour(0, hourOfDay, false);
					qA.setMinutes(0, minutes);
					startTimeButton.setText(qA.dateString(0));
					startTimeClock.setTime(hourOfDay, minutes, true);
				}else{
					Toast.makeText(AddRecurringActivity.this, "Start time can't be 11:59 PM", Toast.LENGTH_SHORT).show();
				}
				
				//Sets the end time 1 hour after the start time if the start time
				//	is after the current end time
				//If start time is past 11pm, end time equals 11:59pm
				if(!qA.isAfter(1, hourOfDay, minutes, false)){
					int otherHour;
					int otherMinutes = 0;
					if(hourOfDay == 23){
						otherHour = 23;
						otherMinutes = 59;
					}else{
						otherHour = qA.getHour(0, true, false)+1;
					}
					qA.setHour(1, otherHour);
					qA.setMinutes(1, otherMinutes);
					endTimeButton.setText(qA.dateString(1));
					endTimeClock.setTime(otherHour, minutes, true);
				}
			}
		};
		
		t2 = new TimePickerDialog.OnTimeSetListener(){
				public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
					if(qA.isBefore(QuietActivity.START, hourOfDay, minutes, false)){
						qA.setHour(1, hourOfDay, false);
						qA.setMinutes(1, minutes);
						endTimeButton.setText(qA.dateString(1));
						endTimeClock.setTime(hourOfDay, minutes, true);
					}else
						Toast.makeText(AddRecurringActivity.this, "Start Date must come before the End Date", Toast.LENGTH_SHORT).show();
				}
			};
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recurring_menu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.recurring_save:
        	if(!activityNameBox.getText().toString().trim().matches("")
					&& !startTimeButton.getText().toString().matches("Start Time") 
					&& !endTimeButton.getText().toString().matches("End Time")){
				if(updateDB() != -1){
					setResult(RESULT_OK);
					finish();
				}else 
					Toast.makeText(getBaseContext(), "Activity name already used", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getBaseContext(), "Not all fields are filled out!", Toast.LENGTH_SHORT).show();
			}
        	break;
    	default:
    		break;
        }
        return true;
	}
	
	
	
	
	
	
	
	
	//Database Methods

	//Pre:	
	//Post:	Updates the database with the new activity information
	//		Returns 1 if the query was successful
	protected int updateDB() {
		for(int i=0; i<7; i++)
			qA.setDay(i, dOfW[i].isChecked());
		qA.setSilent(!vibrateCheck.isChecked());
		qA.setName(activityNameBox.getText().toString());
		qA.setEnabled(enabledSwitch.isChecked());
		
		
		int result;

		db.open();
		if(!edit)
			result = db.addToDB(qA);
		else
			result = db.updateDB(activityName, qA);
		db.close();
		
		return result;
	}
		
	
}
