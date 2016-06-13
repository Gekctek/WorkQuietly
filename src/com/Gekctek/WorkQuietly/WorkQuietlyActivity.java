package com.Gekctek.WorkQuietly;

import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class WorkQuietlyActivity extends FragmentActivity implements LocationListener{
	private int resultCode;
	private SharedPreferences settings;
	public static final int RECURRING = 0;
	public static final int ONETIME = 1;
	public static final int CALENDAR = 2;
	public static final int GPS = 3;
	private ActionBar actionBar;
	private LocationManager locManager;
	private static boolean isGettingGPS;
	protected static LocationListener gpsLocationListener;
	private final int GPS_MIN_CHECK_TIME_DEFAULT = 10;
	private final int GPS_MIN_CHECK_DISTANCE_DEFAULT = 10;	
	private int GPS_MIN_CHECK_TIME; //milliseconds
	private float GPS_MIN_CHECK_DISTANCE; //meters
	private ViewPager mViewPager;
	private boolean isGPSEnabled;
	private boolean isNetworkEnabled;
	public static Context context;
    
	
	//Initializers
	
	//Pre:
	//Post:	Creates the initial page GUI 
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        CallReceiver phoneListener = new CallReceiver(getBaseContext());
        TelephonyManager telephony = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
                
        context = this;
        //Loads default preferences
        PreferenceManager.setDefaultValues(this, R.xml.general_prefs, false);
        
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        GPS_MIN_CHECK_TIME = settings.getInt("gps_min_check_time", GPS_MIN_CHECK_TIME_DEFAULT) * 1000;
        GPS_MIN_CHECK_DISTANCE = settings.getFloat("gps_min_check_distance", GPS_MIN_CHECK_DISTANCE_DEFAULT);
        
        

        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);
        
        isGettingGPS = false;
        gpsLocationListener = this;
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    
        isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);    
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new WorkQuietlyPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });
        
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				
			}
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				
			}};
        
        Tab recurringTab = actionBar.newTab();
        recurringTab.setText("Recurring")
        			.setContentDescription("Recurring Events")
        			.setTabListener(tabListener);
        actionBar.addTab(recurringTab);
        
        Tab oneTimeTab = actionBar.newTab();
        oneTimeTab.setText("One Time")
        			.setContentDescription("One Time Events")
        			.setTabListener(tabListener);
        actionBar.addTab(oneTimeTab);
        
        Tab calendarTab = actionBar.newTab();
        calendarTab.setText("Calendar")
        			.setContentDescription("Calendar Events")
        			.setTabListener(tabListener);
        actionBar.addTab(calendarTab);
                
        Tab gpsTab = actionBar.newTab();
        gpsTab.setText("GPS (Beta)")
        			.setContentDescription("GPS Events")
        			.setTabListener(tabListener);
        actionBar.addTab(gpsTab);
        
        PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	        int versionCode = pInfo.versionCode;
	        if(settings.getInt("version", 0) < versionCode){
		    	DialogFragment changeLogDialog = ChangeLogDialogFragment.newInstance();
		    	changeLogDialog.show(getFragmentManager(), "ChangeLog");
		    	settings.edit().putInt("version", versionCode).commit();
	        }
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	//Menu methods

	//Pre:
	//Post:	Creates a contextual menu based on an XML layout R.menu.menu
	//		Returns T in any situation
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
	
	//Pre:
	//Post:	Gives each contextual menu option a task such as starting an activity
	//		Returns T if a contextual option was selected
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prefs:     
            	Intent preferences = new Intent(this, EditPreferences.class);
            	startActivity(preferences);
                break;
            case R.id.deleteActivities: 
            	Intent removeActivity = new Intent(this, RemoveActivity.class);
            	removeActivity.putExtra("ActivityType", actionBar.getSelectedNavigationIndex());
            	startActivityForResult(removeActivity, resultCode);
            	break;
            case R.id.quickQuiet:
            	DialogFragment newFragment = QuickQuietDialogFragment.newInstance();
            	newFragment.show(getFragmentManager(), "QQ");
            	break;
            case R.id.add:
            	Intent addActivity;
            	switch(actionBar.getSelectedNavigationIndex()){
            	case 0:
            		createRecurringDialog().show();
            		break;
            	case 1:
            		addActivity = new Intent(this, AddOneTimeActivity.class);
            		startActivityForResult(addActivity, resultCode);
            		break;
            	case 2:
            		addActivity = new Intent(this, AddCalendarFilter.class);
            		startActivityForResult(addActivity, resultCode);
            		break;
            	case 3:
            		addActivity = new Intent(this, AddGPSActivity.class);
            		startActivityForResult(addActivity, resultCode);
            		break;
            	default:
            		break;
            	}
            	break;
            case R.id.menu_change_log:
            	DialogFragment changeLogDialog = ChangeLogDialogFragment.newInstance();
            	changeLogDialog.show(getFragmentManager(), "ChangeLog");
            	break;
            case R.id.whitelist_activity:
            	startActivity(new Intent(this, WhitelistActivity.class));
            	break;
            default:
            	return false;

        }
        return true;
    }

	
	private AlertDialog createRecurringDialog() {
		return new AlertDialog.Builder(this)
		.setTitle("Choose a recurring type")
		.setItems(R.array.recurring_types, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent addActivity;
				if(which == 0){
					addActivity = new Intent(getBaseContext(), AddRecurringActivity.class);
				}else{
					addActivity = new Intent(getBaseContext(), AddRecurringAdvancedActivity.class);
				}
				startActivityForResult(addActivity, resultCode);
			}
		} ).create();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(settings.getBoolean("enableWQ", true) && QuietService.isQuiet())
				refreshService();
		}
	}
	
	private void refreshService(){
		Intent intent = new Intent(this, QuietService.class).putExtra("Refresh", true);
		startService(intent);
	}

	//Activity Methods
	
	
	@Override
	protected void onResume() {
		super.onResume();
		DBHelper db = new DBHelper(getBaseContext());
		db.open();
		boolean gpsEventsExist = db.rawQuery("SELECT * FROM GPS", null).getCount() > 0;
		db.close();
		
		if(settings.getBoolean("enableWQ", true) && !QuietService.isQuiet()){
			refreshService();
		}else if(!settings.getBoolean("enableWQ", true)){
			QuietService.disable();
		}	
		
		
		if(!settings.getBoolean("enableGPS", false)){
			locManager.removeUpdates(this);
			refreshService();
			isGettingGPS = false;
		}else if(settings.getBoolean("enableGPS", false)){
			if(gpsEventsExist){
				refreshUpdates();
			}else{
				Criteria c = new Criteria();
		        c.setAccuracy(Criteria.ACCURACY_FINE);
		        String bestProvider = locManager.getBestProvider(c, true);
		        if(bestProvider != null){
		        	QuietService.location = locManager.getLastKnownLocation(bestProvider);
		        }
			}
		}
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		QuietService.location = location;
		startService(new Intent(getBaseContext(), QuietService.class).putExtra("GPS", true));		
	}

	@Override
	public void onProviderDisabled(String provider) {
		refreshUpdates();
	}

	@Override
	public void onProviderEnabled(String provider) {
		refreshUpdates();
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		refreshUpdates();		
	}
	
	private void refreshUpdates(){
		locManager.removeUpdates(this);
		isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);    

        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        int maxPower = Integer.parseInt(settings.getString("gpsPower", Criteria.POWER_HIGH+""));
        c.setPowerRequirement(maxPower);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setVerticalAccuracy(Criteria.ACCURACY_LOW);
        String bestProvider = locManager.getBestProvider(c, true);
        //locManager.requestLocationUpdates(GPS_MIN_CHECK_TIME, GPS_MIN_CHECK_DISTANCE, c, this, null);
        QuietService.location = locManager.getLastKnownLocation(bestProvider);
        locManager.requestLocationUpdates(bestProvider, GPS_MIN_CHECK_TIME, GPS_MIN_CHECK_DISTANCE, this);
		isGettingGPS = true;
	}

}