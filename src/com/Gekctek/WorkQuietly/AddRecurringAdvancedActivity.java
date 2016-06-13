package com.Gekctek.WorkQuietly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;


public class AddRecurringAdvancedActivity extends Activity {
	private EditText nameText;
	private Switch enabledSwitch;
	private CheckBox vibrateCheck;
	private Spinner startDaySpinner;
	private Spinner endDaySpinner;
	private Button startTimeButton;
	private Button endTimeButton;
	private ChangeableAnalogClock startClock;
	private ChangeableAnalogClock endClock;
	private Spinner repeatSpinner;
	private RecurringAdvancedQuietActivity activity;
	private DBHelper db;
	private boolean edit;
	private TimePickerDialog.OnTimeSetListener t;
	private TimePickerDialog.OnTimeSetListener t2;
	private SharedPreferences settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		setContentView(R.layout.add_recurring_advanced);
		
		edit = false;
		if(b != null)
			edit = getIntent().getExtras().getBoolean("Edit", false);

		setXML();
		populateLists();
		
        db = new DBHelper(this);
        if(edit){
			String activityName = getIntent().getExtras().getString("name");
        	String[] args = new String[]{activityName};
    		db.open();
    		
    		Cursor query = db.rawQuery("SELECT * from RecurringAdvanced where name=?", args);
    		query.moveToFirst();
        	activity = new RecurringAdvancedQuietActivity(query);
        	db.close();
        	query.close();
        	nameText.setText(activity.getName());
        	enabledSwitch.setChecked(activity.getEnabled());
        	vibrateCheck.setChecked(!activity.getSilent());
        	repeatSpinner.setSelection(activity.getRepeatIndex());
        	startDaySpinner.setSelection(activity.getStartDayIndex());
        	endDaySpinner.setSelection(activity.getEndDayIndex());
        }else{	
    		Calendar start = Calendar.getInstance();
    		start.set(Calendar.MINUTE, 0);
    		Calendar end = (Calendar) start.clone();
    		end.add(Calendar.HOUR_OF_DAY, 1);
        	activity = new RecurringAdvancedQuietActivity("", start.getTimeInMillis(), end.getTimeInMillis(), false, true, new boolean[7], false, 0, 0, 0);
        }
		setListeners();
		
		startTimeButton.setText(activity.timeString(QuietActivity.START));
		endTimeButton.setText(activity.timeString(QuietActivity.END));
		startClock.setTime(activity.getTimeInMillis(QuietActivity.START));
		endClock.setTime(activity.getTimeInMillis(QuietActivity.END));
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
	
	}
	
	private void setXML(){
		nameText = (EditText) findViewById(R.id.name_text);
		enabledSwitch = (Switch) findViewById(R.id.enabled_switch);
		vibrateCheck = (CheckBox) findViewById(R.id.vibrate_check);
		startDaySpinner = (Spinner) findViewById(R.id.start_days_spinner);
		endDaySpinner = (Spinner) findViewById(R.id.end_days_spinner);
		startTimeButton = (Button) findViewById(R.id.start_time_button);
		endTimeButton = (Button) findViewById(R.id.end_time_button);
		startClock = (ChangeableAnalogClock) findViewById(R.id.start_clock);
		endClock = (ChangeableAnalogClock) findViewById(R.id.end_clock);
		repeatSpinner = (Spinner) findViewById(R.id.repeat_spinner);
	}
	
	private void populateLists(){
		List<String> days = new ArrayList<String>(Arrays.asList(RecurringAdvancedQuietActivity.DAYS));
		List<String> repeatList = new ArrayList<String>(Arrays.asList(RecurringAdvancedQuietActivity.REPEAT_LIST));
		startDaySpinner.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, days));
		endDaySpinner.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, days));
		repeatSpinner.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, repeatList));
	}
	
	private void setListeners(){
		startTimeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(AddRecurringAdvancedActivity.this, t, activity.getHour(QuietActivity.START, true, false), activity.getMinutes(QuietActivity.START), settings.getBoolean("clockFormat", false)).show();		
			}
		});
		endTimeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new TimePickerDialog(AddRecurringAdvancedActivity.this, t2, activity.getHour(QuietActivity.END, true, false), activity.getMinutes(QuietActivity.END), settings.getBoolean("clockFormat", false)).show();		
			}
		});
		
		t = new TimePickerDialog.OnTimeSetListener(){
			public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {

				activity.setHour(QuietActivity.START, hourOfDay, false);
				activity.setMinutes(QuietActivity.START, minutes);
				startTimeButton.setText(activity.timeString(QuietActivity.START));
				startClock.setTime(hourOfDay, minutes, false);
			}
		};
		
		t2 = new TimePickerDialog.OnTimeSetListener(){
				public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {					
					
					activity.setHour(QuietActivity.END, hourOfDay, false);
					activity.setMinutes(QuietActivity.END, minutes);
					endTimeButton.setText(activity.timeString(QuietActivity.END));
					endClock.setTime(hourOfDay, minutes, false);
				}
			};
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recurring_advanced_menu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.recurring_advanced_save:
        	if(!nameText.getText().toString().trim().matches("") 
					&& !startTimeButton.getText().toString().matches("Start Time") 
					&& !endTimeButton.getText().toString().matches("End Time")){
				if(updateDB() != -1){
					setResult(RESULT_OK);
					finish();
				}else 
					Toast.makeText(getBaseContext(), "Activity name already used", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getBaseContext(), "No name has been inputted!", Toast.LENGTH_SHORT).show();
			}
        	break;
    	default:
    		break;
        }
        return true;
	}
	
	protected int updateDB() {
		activity.setSilent(!vibrateCheck.isChecked());
		activity.setEnabled(enabledSwitch.isChecked());
		activity.setStartDayIndex(startDaySpinner.getSelectedItemPosition());
		activity.setEndDayIndex(endDaySpinner.getSelectedItemPosition());
		activity.setRepeatIndex(repeatSpinner.getSelectedItemPosition());
		activity.setName(nameText.getText().toString());
		
				
		int result;

		db.open();
		if(!edit)
			result = db.addToDB(activity);
		else
			result = db.updateDB(activity.getName(), activity);
		db.close();
		
		return result;
	}	
}