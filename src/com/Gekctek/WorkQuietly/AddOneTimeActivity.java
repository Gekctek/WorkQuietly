package com.Gekctek.WorkQuietly;

import java.util.Calendar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddOneTimeActivity extends Activity {
	private String activityName;
	private boolean edit;
	private Button startTimeButton;
	private Button endTimeButton;
	
	private EditText activityNameBox;
	
	private ChangeableAnalogClock startTimeClock;
	private ChangeableAnalogClock endTimeClock;
	
	private CalendarView startCalendarView;
	private LinearLayout startCalendarViewWrapper;
	private CalendarView endCalendarView;
	private LinearLayout endCalendarViewWrapper;
	
	private TimePickerDialog.OnTimeSetListener t;
	private TimePickerDialog.OnTimeSetListener t2;
	private DatePickerDialog.OnDateSetListener d1;
	private DatePickerDialog.OnDateSetListener d2;
	
	
	protected Calendar startTimeCalendar = Calendar.getInstance();
	protected Calendar endTimeCalendar = Calendar.getInstance();
	
	private OneTimeQuietActivity qA;
	
	private CheckBox vibrateCheck;
	
	private DBHelper db;
	
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
		
	    setContentView(R.layout.add_one_time_activity);
	    
	    setXML();
	    setListeners();

        db = new DBHelper(this);
        if(!edit){
			startTimeCalendar.set(Calendar.MINUTE, 0);
			startTimeCalendar.set(Calendar.SECOND, 0);
			startTimeCalendar.set(Calendar.MILLISECOND, 0);
			endTimeCalendar = (Calendar)startTimeCalendar.clone();
			endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }else{
			activityName = getIntent().getExtras().getString("name");
        	String[] args = new String[]{activityName};
    		db.open();
    		
    		Cursor query = db.rawQuery("SELECT * from OneTime where name=?", args);
    		query.moveToFirst();
        	startTimeCalendar.setTimeInMillis(query.getLong(query.getColumnIndex("startDate")));
        	endTimeCalendar.setTimeInMillis(query.getLong(query.getColumnIndex("endDate")));
        	db.close();
        	query.close();
        }
		
        qA = new OneTimeQuietActivity("", startTimeCalendar.getTimeInMillis(), endTimeCalendar.getTimeInMillis(), false, true, Calendar.getInstance().getTime(), false);
        if(!edit){
			startTimeButton.setText(qA.dateString(0));
			endTimeButton.setText(qA.dateString(1));
		}else{
			setFields();
		}
		startTimeClock.setTime(qA.getHour(0), qA.getMinutes(0), false);
		endTimeClock.setTime(qA.getHour(1), qA.getMinutes(1) ,false);
		
		startCalendarView.setEnabled(false);
		endCalendarView.setEnabled(false);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	//Pre:
	//Post:	Sets the fields in the GUI to the current values of the activity
	private void setFields(){
		String[] args = new String[]{activityName};
		db.open();
		
		Cursor query = db.rawQuery("SELECT * from OneTime where name=?", args);
		query.moveToFirst();
		
		vibrateCheck.setChecked(query.getInt(query.getColumnIndex("silent")) != 1);
		qA.setTimeInMillis(0, query.getLong(query.getColumnIndex("startDate")));
		qA.setTimeInMillis(1, query.getLong(query.getColumnIndex("endDate")));
		startTimeButton.setText(qA.dateString(0));
		endTimeButton.setText(qA.dateString(1));
		activityNameBox.setText(activityName);
		long a = qA.getTimeInMillis(0);
		long b = qA.getTimeInMillis(1);
		startCalendarView.setDate(a);
		endCalendarView.setDate(b);
		
		query.close();
		db.close();
	}
	
	//Pre:
	//Post:	Sets classes variables to corresponding GUI/XML elements
	protected void setXML(){
		activityNameBox = (EditText)findViewById(R.id.one_time_activity_name);
		startTimeButton = (Button)findViewById(R.id.one_time_start_button);
		startTimeClock = (ChangeableAnalogClock)findViewById(R.id.one_time_start_clock);
		endTimeButton = (Button)findViewById(R.id.one_time_end_button);
		endTimeClock = (ChangeableAnalogClock)findViewById(R.id.one_time_end_clock);
		vibrateCheck = (CheckBox)findViewById(R.id.one_time_vibrate_check);
		startCalendarView = (CalendarView)findViewById(R.id.one_time_start_calendar);
		startCalendarViewWrapper = (LinearLayout)findViewById(R.id.one_time_start_calendar_wrapper);
		endCalendarView = (CalendarView)findViewById(R.id.one_time_end_calendar);
		endCalendarViewWrapper = (LinearLayout)findViewById(R.id.one_time_end_calendar_wrapper);
	}
	
	//Pre:	
	//Post: Sets all the buttons' listeners 
	protected void setListeners(){
		startTimeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new TimePickerDialog(AddOneTimeActivity.this, t, qA.getHour(0, true, false), qA.getMinutes(0), settings.getBoolean("clockFormat", false)).show();				
			}
		});
		
		endTimeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new TimePickerDialog(AddOneTimeActivity.this, t2, qA.getHour(1, true, false), qA.getMinutes(1), settings.getBoolean("clockFormat", false)).show();				
			}
		});
		
		startCalendarViewWrapper.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new DatePickerDialog(AddOneTimeActivity.this, d1, startTimeCalendar.get(Calendar.YEAR), startTimeCalendar.get(Calendar.MONTH), startTimeCalendar.get(Calendar.DAY_OF_MONTH)).show();				
			}
		});
		
		endCalendarViewWrapper.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new DatePickerDialog(AddOneTimeActivity.this, d2, endTimeCalendar.get(Calendar.YEAR), endTimeCalendar.get(Calendar.MONTH), endTimeCalendar.get(Calendar.DAY_OF_MONTH)).show();				
			}
		});
		
		t = new TimePickerDialog.OnTimeSetListener(){
			public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
				Calendar start = Calendar.getInstance();
				start.setTimeInMillis(startCalendarView.getDate());
				Calendar end = Calendar.getInstance();
				end.setTimeInMillis(endCalendarView.getDate());
				start.set(Calendar.MILLISECOND, 0);
				start.set(Calendar.SECOND, 0);
				start.set(Calendar.MINUTE, 0);
				start.set(Calendar.HOUR_OF_DAY, 0);
				end.set(Calendar.MILLISECOND, 0);
				end.set(Calendar.SECOND, 0);
				end.set(Calendar.MINUTE, 0);
				end.set(Calendar.HOUR_OF_DAY, 0);
				
				if((start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR) && !qA.isBefore(QuietActivity.END, hourOfDay, minutes, false))){
					qA.setHour(1, hourOfDay+1, false);
					qA.setMinutes(1, minutes);
					endTimeButton.setText(qA.dateString(1));
					endTimeClock.setTime(hourOfDay+1, 0, true);
				}
				qA.setHour(0, hourOfDay, false);
				qA.setMinutes(0, minutes);
				startTimeButton.setText(qA.dateString(0));
				startTimeClock.setTime(hourOfDay, minutes, false);
			}
		};
		
		t2 = new TimePickerDialog.OnTimeSetListener(){
				public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
					Calendar start = Calendar.getInstance();
					start.setTimeInMillis(startCalendarView.getDate());
					Calendar end = Calendar.getInstance();
					end.setTimeInMillis(endCalendarView.getDate());
					start.set(Calendar.MILLISECOND, 0);
					start.set(Calendar.SECOND, 0);
					start.set(Calendar.MINUTE, 0);
					start.set(Calendar.HOUR_OF_DAY, 0);
					end.set(Calendar.MILLISECOND, 0);
					end.set(Calendar.SECOND, 0);
					end.set(Calendar.MINUTE, 0);
					end.set(Calendar.HOUR_OF_DAY, 0);
					
					boolean a = qA.isBefore(QuietActivity.START, hourOfDay, minutes, false);
					boolean b = start.before(end);
					if(a || b){
						qA.setHour(1, hourOfDay, false);
						qA.setMinutes(1, minutes);
						endTimeButton.setText(qA.dateString(1));
						endTimeClock.setTime(hourOfDay, minutes, true);
					}else
						Toast.makeText(AddOneTimeActivity.this, "Start Date must come before the End Date", Toast.LENGTH_SHORT).show();
				}
			};
			
		d1 = new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar start = Calendar.getInstance();
				start.set(year, monthOfYear, dayOfMonth);
				start.set(Calendar.MILLISECOND, 0);
				start.set(Calendar.SECOND, 0);
				start.set(Calendar.MINUTE, 0);
				start.set(Calendar.HOUR_OF_DAY, 0);
				Calendar end = Calendar.getInstance();
				end.setTimeInMillis(endCalendarView.getDate());
				end.set(Calendar.MILLISECOND, 0);
				end.set(Calendar.SECOND, 0);
				end.set(Calendar.MINUTE, 0);
				end.set(Calendar.HOUR_OF_DAY, 0);
				
				if(start.after(end)){
					end.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR));
					endCalendarView.setDate(end.getTimeInMillis());
				}
				int hourOfDay = qA.getHour(QuietActivity.START, true, false);
				int minutes = qA.getMinutes(QuietActivity.START);
				if(!qA.isBefore(QuietActivity.END, hourOfDay, minutes, false)){
					qA.setHour(1, hourOfDay+1, false);
					qA.setMinutes(1, minutes);
					endTimeButton.setText(qA.dateString(1));
					endTimeClock.setTime(hourOfDay+1, 0, true);
				}
				startCalendarView.setDate(start.getTimeInMillis());					
			}
		};
		
		d2 = new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar end = Calendar.getInstance();
				end.set(year, monthOfYear, dayOfMonth);
				end.set(Calendar.MILLISECOND, 0);
				end.set(Calendar.SECOND, 0);
				end.set(Calendar.MINUTE, 0);
				end.set(Calendar.HOUR_OF_DAY, 0);
				Calendar start = Calendar.getInstance();
				start.setTimeInMillis(endCalendarView.getDate());
				start.set(Calendar.MILLISECOND, 0);
				start.set(Calendar.SECOND, 0);
				start.set(Calendar.MINUTE, 0);
				start.set(Calendar.HOUR_OF_DAY, 0);
				
				if(!start.after(end)){
					endCalendarView.setDate(end.getTimeInMillis());
				}else{
					Toast.makeText(AddOneTimeActivity.this, "Start Date must come before the End Date", Toast.LENGTH_SHORT).show();
				}
			}
		};
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.one_time_menu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.one_time_save:
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
		qA.setSilent(!vibrateCheck.isChecked());
		qA.setName(activityNameBox.getText().toString());
		qA.setStartDate(startCalendarView.getDate());
		qA.setEndDate(endCalendarView.getDate());
		
				
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
