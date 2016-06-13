package com.Gekctek.WorkQuietly;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AddGPSActivity extends Activity {
	private Context mContext;
	private EditText gpsName;
	private TextView radiusLabel;
	private SeekBar radiusBar;
	private EditText radiusInput;
	private EditText latitudeInput;
	private EditText longitudeInput;
	private Button getCoordButton;
	private String radiusLabelText;
	private Switch enabled;
	private CheckBox vibrate;
	private final double M_TO_MILES = .000621371;
	private double latitude;
	private double longitude;
	private boolean edit;
	private String editName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_gps_activity);
		this.mContext = getBaseContext();

		Bundle b = getIntent().getExtras();
		
		edit = false;
		if(b != null){
			edit = getIntent().getExtras().getBoolean("Edit", false);
			editName = getIntent().getExtras().getString("Name");
		}
		
		radiusLabelText = mContext.getResources().getString (R.string.gps_radius_label);
		
		setXML();
		setListeners();
		
		if(edit){
			setFields();
		}
	}
	
	private void setXML(){
		int initial = mContext.getResources().getInteger(R.integer.gps_max_distance);
		gpsName = (EditText)findViewById(R.id.gps_name);
		radiusLabel = (TextView)findViewById(R.id.gps_radius_label);
		radiusLabel.setText(radiusLabelText+" "+initial+" meters  ("+Math.round(initial*M_TO_MILES*100.0)/100.0+" miles)");
		radiusBar = (SeekBar)findViewById(R.id.gps_radius_slider);
		radiusInput = (EditText)findViewById(R.id.gps_radius_input);
		latitudeInput = (EditText)findViewById(R.id.gps_latitude_input);
		longitudeInput = (EditText)findViewById(R.id.gps_longitude_input);
		getCoordButton = (Button)findViewById(R.id.gps_get_coordinates_button);
		enabled = (Switch)findViewById(R.id.gps_enabled_switch);
		vibrate = (CheckBox)findViewById(R.id.gps_vibrate_check);
		

		String progress = radiusBar.getProgress()+"";
		radiusInput.setText(progress);
	}
	
	private void setFields(){
		String[] args = {editName};
		DBHelper db = new DBHelper(mContext);
		db.open();
		Cursor filterQuery = db.rawQuery("SELECT * FROM GPS WHERE name=?", args);
		filterQuery.moveToFirst();	
		gpsName.setText(editName);
		latitudeInput.setText(filterQuery.getDouble(filterQuery.getColumnIndex("latitude"))+"");
		longitudeInput.setText(filterQuery.getDouble(filterQuery.getColumnIndex("longitude"))+"");
		enabled.setChecked(filterQuery.getInt(filterQuery.getColumnIndex("enabled")) == 1);
		vibrate.setChecked(filterQuery.getInt(filterQuery.getColumnIndex("silent")) != 1);
		int progress = filterQuery.getInt(filterQuery.getColumnIndex("radius"));
		if(progress > radiusBar.getMax())
			radiusBar.setMax(progress);
		
		radiusBar.setProgress(progress);
		radiusInput.setText(progress+"");
	}
	
	private void setListeners(){
		radiusBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				String progress = seekBar.getProgress()+"";
				radiusInput.setText(progress);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
								
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				radiusLabel.setText(radiusLabelText+" "+progress+" meters  ("+Math.round(progress*M_TO_MILES*100.0)/100.0+" miles)");
			}
		});
		
		getCoordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(QuietService.location != null){
					latitudeInput.setText(QuietService.location.getLatitude()+"");
					longitudeInput.setText(QuietService.location.getLongitude()+"");
				}else{
					Toast.makeText(mContext, "Location is unavailable", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		radiusInput.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String p = s.toString();
				if(!p.matches("")){
					int progress = Integer.parseInt(s.toString());
					if(progress > radiusBar.getMax())
						radiusBar.setMax(progress);
					
					radiusLabel.setText(radiusLabelText+" "+progress+" meters  ("+Math.round(progress*M_TO_MILES*100.0)/100.0+" miles)");	
					radiusBar.setProgress(progress);
				}
			}
		});
	}
	
	public boolean updateDB(){
		DBHelper db = new DBHelper(mContext);
		
		int result; 
		String name = gpsName.getText().toString().trim();
		
		if(edit){
			result = db.updateDB(editName, name, latitude, longitude, radiusBar.getProgress(), enabled.isChecked(), !vibrate.isChecked());
		}else{
			result = db.addToDB(name, latitude, longitude, radiusBar.getProgress(), enabled.isChecked(), !vibrate.isChecked());
		}
		return result != -1;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gps_menu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.gps_save:
        	try{latitude = Double.parseDouble(latitudeInput.getText().toString());}
        	catch ( Exception e ){ Toast.makeText(mContext, "Latitude input has the incorrect format", Toast.LENGTH_SHORT).show(); break;}
        	try{longitude = Double.parseDouble(longitudeInput.getText().toString());}
        	catch ( Exception e ){ Toast.makeText(mContext, "Longitude input has the incorrect format", Toast.LENGTH_SHORT).show(); break;}
        	if(!gpsName.getText().toString().trim().matches("") ){
				if(updateDB()){
					setResult(RESULT_OK);
					finish();
				}
    		}else{
    			Toast.makeText(getBaseContext(), "No name inputed", Toast.LENGTH_SHORT).show();
    		}
        	break;
    	default:
    		break;
        }
        return true;
	}
	
}
