package com.Gekctek.WorkQuietly;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.util.Log;
import android.app.IntentService;
import android.app.Notification;
import android.os.Vibrator;

public class QuietService extends IntentService {
	private static AudioManager audioM;
	private static NotificationManager nM;
	private AlarmManager aM;
	private Vibrator vibrator;
	
	public static int initialRingerMode;
	public static int quietRingerMode;
	private static QuietActivity currentQA;
	private DBHelper db;
	
		
	private PendingIntent killPI;
	private PendingIntent startPI;
	private PendingIntent deletePI;
	
	private final static int KILL = 1;
	private final static int START = 2;
	private final static int PAUSE = 3;
	private final static int QUIET = 4;
	private final static int REFRESH = 5;
	
	private static SharedPreferences settings;
	
	
	private static boolean refreshScheduled;
	public static boolean goingQuiet;
	public static boolean refreshing;
	public static boolean whitelist = false;
	public static boolean whitelistStarted = false;
	public static int whitelistRinger = 2;
	
	private Context mContext;
	private Notification.Builder quietMode;
	private Notification.Builder normalMode;
	private boolean silent;
	private String name;
	private long endTime;
	private long[] vibratePattern = {0,100,25,100,25,100,25,100};
	private Handler mHandler;
	private static boolean gpsInRange = false;
	
	public static Location location;
	
	private final String[] modes = {"S", "V", "N", "NULL"};
	
	//Initializers
	
	public QuietService(){
		this("QuietService");
	}
	
	public QuietService(String name){
		super(name);
	}
	
	//Pre:
	//Post: Creates the QuietService service
	@Override
	public void onCreate(){
		super.onCreate();
		db = new DBHelper(this);
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		audioM = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		nM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		aM = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		settings = (SharedPreferences)PreferenceManager.getDefaultSharedPreferences(this);
		vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		Log.i("QuietService", "Serivce created");
		mContext = getBaseContext();
		mHandler = new Handler();
    	scheduleRefresh();
	}
	
	@Override
	public void onHandleIntent(Intent intent){
	    if(!settings.getBoolean("enableWQ", true)){
	    	Log.i("LocalService", "Service cancled, WorkQuietly is disabled in settings");
	    }else{
	    
			Bundle bundle = intent.getExtras();
	    	if(bundle == null)
				bundle = new Bundle();
			
			boolean kill = bundle.getBoolean("Kill", false);
			boolean boot = bundle.getBoolean("Boot", false);
			boolean quickQuiet = bundle.getBoolean("QQ", false);
	 		boolean refresh = bundle.getBoolean("Refresh", false);
	 		boolean unQuiet = bundle.getBoolean("unQuiet", false);
	 		boolean goQuiet = bundle.getBoolean("goQuiet", false);
	 		boolean GPS = bundle.getBoolean("GPS", false);
	 		int qQHours = bundle.getInt("qQHours");
	 		int qQMinutes = bundle.getInt("qQMinutes");
	 		boolean qQSilent = bundle.getBoolean("qQSilent", false);
	 		boolean killOnClick = bundle.getBoolean("killOnClick", false);
	 		boolean autoDelete = bundle.getBoolean("AutoDelete", true);
	 		boolean advanced = bundle.getBoolean("Advanced", false); 
	 		int startDay = bundle.getInt("startDay");
	 		int endDay = bundle.getInt("endDay");
	 		int repeatIndex = bundle.getInt("repeatIndex");
	 		long startTime = bundle.getInt("StartTime");

			this.name = bundle.getString("Name");
			this.endTime = bundle.getLong("EndTime");
			this.silent = bundle.getBoolean("Silent");
			setIRM(bundle.getString("IRM"));
			
			if(advanced)
				currentQA = new RecurringAdvancedQuietActivity(name, startTime, endTime, silent, true, null, false, startDay, endDay, repeatIndex);
			else
				currentQA = new QuietActivity(name, 0, endTime, silent, true, false, false);
			
			

			setUpNotificationBar();

	    	if(kill){
	    		killQuiet(autoDelete);
				scheduleNextEvent();
			}else if(boot){
	    		scheduleNextEvent();
			}else if(quickQuiet){
				if(qQHours == 0 && qQMinutes == 0){
		    		killQuiet();
		    		refresh();
				}else{
					quickQuiet(qQHours, qQMinutes, qQSilent);
				}
			}else if(refresh){
				refreshScheduled = false;
	    		refresh();
			}else if(GPS){
				String name = checkGPS();
				if(unQuiet){
					unQuiet(this.name, -1, true);
				}else if(goQuiet || (name != null && !gpsInRange)){
					goQuiet(name, null, false, killOnClick);
				}else if(name == null && gpsInRange){
					gpsInRange = false;
					killQuiet();
				}
			}else if(goQuiet){
				goQuiet(killOnClick);
			}else if(unQuiet){
				unQuiet();					
			}
		
	    	if(!refreshScheduled)
	    		scheduleRefresh();
	    }
	}
	  
	
	
	private static void setIRM(String mode){
		if(mode == null)
			initialRingerMode = 3;
		else if(mode.toLowerCase().equals("s"))
			initialRingerMode = 0;
		else if(mode.toLowerCase().equals("v"))
			initialRingerMode = 1;
		else if(mode.toLowerCase().equals("n"))
			initialRingerMode = 2;
		else
			initialRingerMode = 3;
	}

	private void scheduleNextEvent(){
		if(isQuietTime()){
			goQuiet();
			if(currentQA.getTimeInMillis(QuietActivity.END) != 0)
				scheduleKill(currentQA);
		}else{
			scheduleQuietTime();
			Log.i("QuietService", "Serivce scheduled next event");
		}
			
	}
	
	private void scheduleKill(QuietActivity qA){
		Intent intent = new Intent(this, QuietService.class).putExtra("Kill", true).putExtra("Name", qA.getName()).putExtra("EndTime", qA.getTimeInMillis(1)).putExtra("IRM", modes[initialRingerMode]).putExtra("Silent", qA.getSilent());
		Calendar c = Calendar.getInstance();
		
		if(qA.isOneTime()){
			if(settings.getBoolean("enableAutoDelete", true))
				intent.putExtra("AutoDelete", true);
			
			OneTimeQuietActivity otqA = (OneTimeQuietActivity)qA;
			c.setTime(otqA.getDate(QuietActivity.END));
		}else if(qA instanceof RecurringAdvancedQuietActivity){
			RecurringAdvancedQuietActivity r = (RecurringAdvancedQuietActivity)qA;
			Calendar start = calculateDate(r.getHour(QuietActivity.START, true, false), r.getMinutes(QuietActivity.START), r.getStartDayIndex(), r.getRepeatIndex());
			Calendar end = calculateDate(r.getHour(QuietActivity.END, true, false), r.getMinutes(QuietActivity.END), r.getEndDayIndex(), r.getRepeatIndex(), start);
			c = end;
		}else{
			long m = qA.getTimeInMillis(QuietActivity.END);
			Calendar a = Calendar.getInstance();
			a.setTimeInMillis(m);
			c.set(Calendar.HOUR_OF_DAY, a.get(Calendar.HOUR_OF_DAY));
			c.set(Calendar.MINUTE, a.get(Calendar.MINUTE));
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
		}
		
		killPI  = PendingIntent.getService(getBaseContext(), KILL, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		
		aM.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), killPI); 
	}
	
	private void scheduleQuietTime(){
		QuietActivity qA = getNextEvent();
		if(qA == null){
			mHandler.post(new Toaster(mContext, "There are no events scheduled for today"));   
		}else{
			mHandler.post(new Toaster(mContext, qA.getName()+" is scheduled for later"));
			long m = qA.getTimeInMillis(QuietActivity.START);
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(m);
			Calendar today = Calendar.getInstance();
			
			today.set(Calendar.SECOND, 0);
			today.set(Calendar.MILLISECOND, 0);
			today.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
			today.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
							
			Intent intent = new Intent(this, QuietService.class).putExtra("goQuiet", true).putExtra("Name", qA.getName()).putExtra("Silent", qA.getSilent()).putExtra("IRM", modes[initialRingerMode]);
			if(qA instanceof RecurringAdvancedQuietActivity){
				Calendar start = calculateDate(qA.getHour(QuietActivity.START, true, false), qA.getMinutes(QuietActivity.START), ((RecurringAdvancedQuietActivity) qA).getStartDayIndex(), ((RecurringAdvancedQuietActivity) qA).getRepeatIndex());
				Calendar end = calculateDate(qA.getHour(QuietActivity.END, true, false), qA.getMinutes(QuietActivity.END), ((RecurringAdvancedQuietActivity) qA).getStartDayIndex(), ((RecurringAdvancedQuietActivity) qA).getRepeatIndex());
				intent.putExtra("StartTime", start.getTimeInMillis());
				intent.putExtra("EndTime", end.getTimeInMillis());
				intent.putExtra("startDay", ((RecurringAdvancedQuietActivity) qA).getStartDay());
				intent.putExtra("endDay", ((RecurringAdvancedQuietActivity) qA).getEndDay());
				intent.putExtra("repeatIndex", ((RecurringAdvancedQuietActivity) qA).getRepeatIndex());
				intent.putExtra("Advanced", true);
			}else{
				intent.putExtra("EndTime", qA.getTimeInMillis(QuietActivity.END));
			}
				
			startPI = PendingIntent.getService(getBaseContext(), START, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			aM.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(), startPI);
			currentQA = qA;
			
			scheduleKill(qA);
		}
	}
	
	public void scheduleRefresh(){
		if(!refreshScheduled && settings.getBoolean("enableWQ", true)){
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.add(Calendar.DAY_OF_YEAR, 1);
			
			Intent intent = new Intent("com.Gekctek.WorkQuietly.QuietService").putExtra("Refresh", true);
			PendingIntent pI = PendingIntent.getService(getBaseContext(), REFRESH, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			aM.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pI); 
			refreshScheduled = true;
			Log.i("QuietService", "Refresh scheduled for " + c.getTime().toString());
		}
	}
	
	public void refresh(){
		if(settings.getBoolean("enableWQ", true)){
			if(killPI != null)
				aM.cancel(killPI);
			if(startPI != null)
				aM.cancel(startPI);
			if(deletePI != null)
				aM.cancel(deletePI);
			killQuiet();
			scheduleNextEvent();
			scheduleRefresh();
		}
	}
	

	private void setUpNotificationBar(){

		//Quiet Mode 
		CharSequence tickerText = "Going quiet...";
		quietMode = new Notification.Builder(mContext);
		quietMode.setSmallIcon(R.drawable.wq_clock)
			.setTicker(tickerText)
			.setWhen(System.currentTimeMillis())
			.setOngoing(true);


		//Leaving Quiet Mode
		tickerText = "Leaving quiet mode...";
		normalMode = new Notification.Builder(mContext);
		normalMode.setSmallIcon(R.drawable.wq_clock)
			.setTicker(tickerText)
			.setWhen(System.currentTimeMillis())
			.setOngoing(true);

	}

	private void goQuiet(){
		goQuiet(false);
	}

	private void goQuiet(boolean killOnClick){
		String name = currentQA.getName();
		String endTime;
		if(currentQA.isOneTime())
			if(settings.getBoolean("clockFormat", false))
				endTime = currentQA.dateString(QuietActivity.END, "M/d H:mm", false);
			else
				endTime = currentQA.dateString(QuietActivity.END, "M/d h:mm a", false);
		else if(currentQA.isGPS)
			endTime = null;
		else
			endTime = currentQA.dateString(QuietActivity.END);
		
		boolean silent = currentQA.getSilent();
		
		goQuiet(name, endTime, silent, killOnClick);
	}

	private void goQuiet(String name, String end, boolean silent, boolean killOnClick){
		initialRingerMode = audioM.getRingerMode();
		SharedPreferences.Editor s = settings.edit();
		s.putInt("IRM", initialRingerMode);
		s.commit();

		QuietService.setQuiet(true);
		QuietService.goingQuiet = true;

		Intent intent;
		String contentText;
		if(end == null){
			gpsInRange = true;
			contentText = "In range of "+name+" GPS coordinates";
			intent = new Intent(mContext, QuietService.class).putExtra("unQuiet", true).putExtra("GPS", true).putExtra("Name", name).putExtra("EndTime", endTime).putExtra("IRM", modes[initialRingerMode]).putExtra("Silent", silent);
		}else if(currentQA instanceof RecurringAdvancedQuietActivity){
			contentText = name+" ends on "+end;
			intent = new Intent(mContext, QuietService.class).putExtra("unQuiet", true).putExtra("Name", name).putExtra("EndTime", endTime).putExtra("IRM", modes[initialRingerMode]).putExtra("Silent", silent);
		}else{
			contentText = name+" ends at "+end;
			intent = new Intent(mContext, QuietService.class).putExtra("unQuiet", true).putExtra("Name", name).putExtra("EndTime", endTime).putExtra("IRM", modes[initialRingerMode]).putExtra("Silent", silent);
		}

		if(killOnClick)
			intent.putExtra("QQ", true);
		
		PendingIntent pI  = PendingIntent.getService(mContext, QUIET, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		quietMode.setContentTitle("Work Quietly")
			.setContentText(contentText)
			.setContentIntent(pI)
			.setWhen(System.currentTimeMillis());

		if(!settings.getBoolean("notifPersist", false))
			quietMode.setOngoing(false);

		nM.notify(1, quietMode.getNotification());


		if(!silent && initialRingerMode != AudioManager.RINGER_MODE_SILENT){
			quietRingerMode = AudioManager.RINGER_MODE_VIBRATE;
		}else{
			quietRingerMode = AudioManager.RINGER_MODE_SILENT;
		}
		
		setRingerMode(quietRingerMode);
		
		if(settings.getBoolean("notifVib", false) && !silent){
			vibrator.vibrate(vibratePattern, -1);
		}
		if(settings.getBoolean("notifSound", false) && !silent){
			//play sound
		}


	}
	private void unQuiet(){
		unQuiet(name, endTime, false);
	}
	//Pre:
	//Post:	Reverts to the phone's pre-quiet volume levels
	//		Keeps notification till killed
	private void unQuiet(String name, long endTime, boolean GPS){
		
		QuietService.setQuiet(false);
		Intent intent;
		if(GPS){
			intent = new Intent(mContext, QuietService.class).putExtra("GPS", true).putExtra("goQuiet", true).putExtra("IRM", modes[initialRingerMode]).putExtra("Silent", currentQA.getSilent());
		}else{
			intent = new Intent(mContext, QuietService.class).putExtra("goQuiet", true).putExtra("Name", name).putExtra("EndTime", endTime).putExtra("IRM", modes[initialRingerMode]).putExtra("Silent", currentQA.getSilent());
		}
		PendingIntent pI  = PendingIntent.getService(mContext, PAUSE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		normalMode.setContentTitle("Work Quietly")
			.setContentText(name+" is disabled. Press this to enable.")
			.setContentIntent(pI)
			.setWhen(System.currentTimeMillis());

		nM.notify(1, normalMode.getNotification());
		setRingerMode(initialRingerMode);

	}
	
	private static void setRingerMode(int initialRingerMode2) {
		if(initialRingerMode2 != -1)
			audioM.setRingerMode(initialRingerMode2);		
	}

	//Action Methods
	
	
	
	//Pre:
	//Post:	Reverts to the phone's pre-quiet volume levels
	//		Takes down persistent notification
	public void killQuiet(){
		killQuiet(false);
	}
	
	private void killQuiet(boolean autoDelete){
		if(autoDelete && this.name != null){
			db.open();
			db.deleteO(currentQA.getName());
			db.close();
		}
		if(isQuiet()){
			QuietService.refreshing = true;
			setQuiet(false);
			setRingerMode(settings.getInt("IRM", -1));
		}
		nM.cancel(KILL);
		currentQA = null;
	}
	
	public static void disable(){
		if(nM != null){
			setQuiet(false);
			nM.cancel(KILL);
			setRingerMode(initialRingerMode);
		}
		currentQA = null;
	}
	
	
	
	
	//Get Methods
	
	//Pre:
	//Post:	Returns an ActivityTime if there 
	private boolean isQuietTime(){
		if(settings.getBoolean("enableGPS", false) && inGPS()){
			return true;
		}else if(inOneTime()){
			return true;
		}else if(settings.getBoolean("enableCal", false) && inCalendar()){
			return true;
		}else if(inRecurring()){
			return true;
		}else{
			return false;	
		}
	}
	
	private boolean inGPS(){
		String name = checkGPS();
		long now = Calendar.getInstance().getTimeInMillis();
		currentQA = new QuietActivity(name, now, 0, silent, true, false, false);
		currentQA.isGPS = true;
		return name != null;
	}
	
	private boolean inOneTime(){
		boolean isQuiet = false;
		db.open();
		
		Cursor query = db.rawQuery("SELECT * FROM OneTime ORDER BY startDate", null);
		query.moveToFirst();
		
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		
		for(int i=0; i<query.getCount(); i++){
			start.setTimeInMillis(query.getLong(query.getColumnIndex("startDate")));
			end.setTimeInMillis(query.getLong(query.getColumnIndex("endDate")));
			if(now.after(start) && now.before(end)){
				isQuiet = true;
				break;
			}
			query.moveToNext();
		}
		
		if(isQuiet){
			currentQA = new OneTimeQuietActivity(query);
			mHandler.post(new Toaster(mContext, query.getString(1)));
			return true;
		}
		db.close();
		return false;
	}
	
	private boolean inCalendar(){
		Calendar c = Calendar.getInstance();

		db.open();
		Cursor query = db.rawQuery("SELECT * FROM CalendarFilter", null);

		query.moveToFirst();
		db.close();
		
		if(query.getCount() > 0){
		
	
			long now = c.getTimeInMillis();
			Calendar d = (Calendar) c.clone();
			d.set(Calendar.MINUTE, 0);
			d.set(Calendar.SECOND, 0);
			d.set(Calendar.MILLISECOND, 0);
			d.set(Calendar.HOUR_OF_DAY, 0);
			d.add(Calendar.DAY_OF_YEAR, 1);
			Calendar today = (Calendar)d.clone();
			today.add(Calendar.DAY_OF_YEAR, -1);
		    String[] projection = {
		    		            Instances._ID, 
		    		            Instances.BEGIN, 
		    		            Instances.END, 
		    		            Instances.EVENT_ID,
		    		            Instances.AVAILABILITY,
		    		            Instances.TITLE,
		    		            Instances.EVENT_LOCATION,
		    		            Instances.ORGANIZER,
		    		            Instances.ALL_DAY};
		    
		    //All Day Events
		    long t = today.getTimeInMillis() + today.getTimeZone().getRawOffset();
		    Calendar tomorrow = (Calendar) today.clone();
		    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		    Cursor cursor2 = Instances.query(getContentResolver(), projection, t+1, t+1);
		    
		    
		    if(cursor2.getCount() != 0){
				cursor2.moveToFirst();				
				do{
					if(query.getInt(query.getColumnIndex("enabled")) != 1)
						continue;
					do{
						if(cursor2.getInt(cursor2.getColumnIndex(Instances.ALL_DAY)) != 1)
							continue;
						CalendarFilter cFilter = new CalendarFilter(query);
						if(cFilter.check(cursor2)){
							long start = today.getTimeInMillis();
							long end = tomorrow.getTimeInMillis();
							String name = cursor2.getString(cursor2.getColumnIndexOrThrow(Instances.TITLE));
							
							boolean silent = false;
							boolean enabled = true;
							cursor2.close();
							query.close();
							setQuiet(true);
							currentQA = new OneTimeQuietActivity(name, start, end, silent, enabled, new Date(start), true);
							return true;
						}
					}while(cursor2.moveToNext());
					
					
				}while(query.moveToNext());
			}
			
		    //Non-All Day events
		    Cursor cursor = Instances.query(getContentResolver(), projection, now, now);
		    query.moveToFirst();
			cursor.moveToFirst();
			if(cursor.getCount() == 0){
				return false;
			}
			do{
				if(query.getInt(query.getColumnIndex("enabled")) != 1)
					continue;
				do{
					
					CalendarFilter cFilter = new CalendarFilter(query);
					if(cursor.getInt(cursor.getColumnIndex(Instances.ALL_DAY)) != 1 && cFilter.check(cursor)){
						long start = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.BEGIN));
						long end = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.END));
						String name = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE));
						
						boolean silent = false;
						boolean enabled = true;
						cursor.close();
						query.close();
						setQuiet(true);
						currentQA = new OneTimeQuietActivity(name, start, end, silent, enabled, new Date(start), true);
						return true;
					}
				}while(cursor.moveToNext());
				cursor.moveToFirst();
			}while(query.moveToNext());

			cursor.close();
		}
		query.close();
		return false;
	}

	private boolean inRecurring(){
		boolean isQuiet = false;
		db.open();
		
		Cursor query = db.rawQuery("SELECT * FROM Recurring ORDER BY startDate", null);
		query.moveToFirst();
		
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		
		for(int i=0; i<query.getCount(); i++){
			start.setTimeInMillis(query.getLong(query.getColumnIndex("startDate")));
			end.setTimeInMillis(query.getLong(query.getColumnIndex("endDate")));
			boolean enabled = query.getInt(query.getColumnIndex("enabled")) == 1;
			if(enabled && isBetween(query, start, end)){
				isQuiet = true;
				break;
			}
			query.moveToNext();
		}
		if(isQuiet){
			currentQA = new RecurringQuietActivity(query);
			mHandler.post(new Toaster(mContext, query.getString(1)));
			query.close();
			db.close();
			return true;
		}
		
		isQuiet = false;
		query = db.rawQuery("SELECT * FROM RecurringAdvanced ORDER BY startDay", null);
		query.moveToFirst();
		
		for(int i = 0; i<query.getCount(); i++){
			int startDay = query.getInt(query.getColumnIndex("startDay"));
			int endDay = query.getInt(query.getColumnIndex("endDay"));
			int repeatIndex = query.getInt(query.getColumnIndex("repeatIndex"));
			boolean enabled = query.getInt(query.getColumnIndex("enabled")) == 1;
			start = calculateDate(query, startDay, repeatIndex);
			if(start == null)
				continue;
			end = calculateDate(query, endDay, repeatIndex, start);
			if(end == null)
				continue;
			
			if(enabled && start.before(now) && end.after(now)){
				isQuiet = true;
				break;
			}
			query.moveToNext();
		}
		if(isQuiet){
			currentQA = new RecurringAdvancedQuietActivity(query);
			mHandler.post(new Toaster(mContext, query.getString(QuietActivity.END)));
			query.close();
			db.close();
			return true;
		}
		
		
		

		query.close();
		db.close();
		return false;
	}
	
	private boolean isBetween(Cursor query, Calendar start, Calendar end){
		Calendar now = Calendar.getInstance();
		Calendar start1 = start;
		Calendar end1 = end;
		start1.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		end1.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		
		if(start1.after(now) || end1.before(now))
			return false;
		
		boolean result = activeToday(query);
		return result;
			
	}
	
	private boolean activeToday(Cursor query){
		Cursor dOfW = db.rawQuery("SELECT strftime('%w', 'now', 'localtime')", null);
		dOfW.moveToFirst();
		int dayOfW = dOfW.getInt(0);
		String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		
		return dayOfW >= 0 && dayOfW <= 6 && query.getInt(query.getColumnIndex(days[dayOfW])) == 1;
	}
	
	
	public void quickQuiet(int hours, int minutes, boolean silent){
		killQuiet();
		Calendar c = Calendar.getInstance();
		Calendar c1 = Calendar.getInstance();
		if(c.get(Calendar.HOUR_OF_DAY) + hours < 24){
			c1.add(Calendar.HOUR_OF_DAY, hours);
			c1.add(Calendar.MINUTE, minutes);
		}else{
			c1.set(Calendar.HOUR_OF_DAY, 23);
			c1.set(Calendar.MINUTE, 59);
			mHandler.post(new Toaster(mContext, "Quick Quiet will end at 11:59PM, can't schedule past it"));
		}
		QuietActivity qA = new QuietActivity("Quick Quiet", c.getTimeInMillis(), c1.getTimeInMillis(), silent, true, false, false);
		currentQA = qA;
		SimpleDateFormat date;
		if(settings.getBoolean("clockFormat", false))
			date = new SimpleDateFormat("H:mm", Locale.getDefault());
		else
			date = new SimpleDateFormat("h:mm a", Locale.getDefault());
		String dateString = date.format(c1.getTime());
		goQuiet("Quick Quiet", dateString, silent, true);
		scheduleKill(qA);
	}
	
	private QuietActivity getNextEvent(){		
		QuietActivity nextRecurring = getNextRecurring();
		OneTimeQuietActivity nextOneTime = getNextOneTime();
		OneTimeQuietActivity nextCalendar = null;
		if(settings.getBoolean("enableCal", false))
			nextCalendar = getNextCalendar();
		
		
		if(nextCalendar != null && nextCalendar.isBefore(0, nextOneTime) && nextCalendar.isBefore(0, nextRecurring))
			return nextCalendar;
		else if(nextOneTime  != null && nextOneTime.isBefore(0, nextCalendar) && nextOneTime.isBefore(0, nextRecurring))
			return nextOneTime;
		else if(nextRecurring != null && nextRecurring.isBefore(0, nextCalendar) && nextRecurring.isBefore(0, nextOneTime))
			return nextRecurring;
		else 
			return null;
	}
	
	private QuietActivity getNextRecurring(){
		Calendar c = Calendar.getInstance();
		String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		String today = days[c.get(Calendar.DAY_OF_WEEK)-1];
		db.open();
		Cursor query = db.rawQuery("SELECT * FROM Recurring WHERE "+today+" = 1 ORDER BY startDate", null);
		query.moveToFirst();		
		Calendar t = Calendar.getInstance();

		int a = query.getCount();
		
		for(int i = 0; i < a; i++){
			query.moveToPosition(i);
			RecurringQuietActivity  qA = new RecurringQuietActivity(query);
			if(qA.getEnabled() && (qA.getHour(0, true, false) > t.get(Calendar.HOUR_OF_DAY) || (qA.getHour(0, true, false) == t.get(Calendar.HOUR_OF_DAY) && qA.getMinutes(0) > t.get(Calendar.MINUTE)))){
				query.close();
				db.close();
				return qA;
			}
		}
		
		query = db.rawQuery("SELECT * FROM RecurringAdvanced ORDER BY startDay", null);
		a = query.getCount();
		for(int i = 0; i< a; i++){
			query.moveToPosition(i);
			RecurringAdvancedQuietActivity qA = new RecurringAdvancedQuietActivity(query);
			Calendar start = calculateDate(query, qA.getStartDayIndex(), qA.getRepeatIndex());
			if(qA.getEnabled() && t.get(Calendar.DAY_OF_YEAR) == start.get(Calendar.DAY_OF_YEAR) && t.before(start)){
				query.close();
				db.close();
				return qA;
			}			
		}
		query.close();
		db.close();
		return null;
	}
	
	private OneTimeQuietActivity getNextOneTime(){
		db.open();
		Calendar n = Calendar.getInstance();
		long now = n.getTimeInMillis();
		n.add(Calendar.DAY_OF_YEAR, 1);
		n.set(Calendar.HOUR_OF_DAY, 0);
		n.set(Calendar.MINUTE, 0);
		n.set(Calendar.MILLISECOND, 0);
		long next = n.getTimeInMillis();
		
		Cursor query = db.rawQuery("SELECT * FROM OneTime WHERE startDate > "+now+" AND startDate < "+next+" ORDER BY startDate", null);
		query.moveToFirst();

		if(query.getCount() > 0){
			do{
				if(query.getInt(query.getColumnIndex("enabled")) != 1)
					continue;
				OneTimeQuietActivity oTA = new OneTimeQuietActivity(query);
				db.close();
				query.close();
				return oTA;
			}while(query.moveToNext());
		}
		db.close();
		query.close();
		return null;
	}
	
	private OneTimeQuietActivity getNextCalendar(){
		Calendar c = Calendar.getInstance();

		db.open();
		Cursor query = db.rawQuery("SELECT * FROM CalendarFilter", null);
		query.moveToFirst();
		db.close();

		
		
		if(query.getCount() > 0){
			if(query.getInt(query.getColumnIndex("enabled")) != 1)
				return null;
		
		    String[] projection = {Instances._ID, 
	    		            Instances.BEGIN, 
	    		            Instances.END, 
	    		            Instances.EVENT_ID,
	    		            Instances.AVAILABILITY,
	    		            Instances.TITLE,
	    		            Instances.EVENT_LOCATION,
	    		            Instances.ORGANIZER,
	    		            Instances.ALL_DAY};
	
			long now = c.getTimeInMillis();
			Calendar d = (Calendar) c.clone();
			d.set(Calendar.MINUTE, 0);
			d.set(Calendar.SECOND, 0);
			d.set(Calendar.MILLISECOND, 0);
			d.set(Calendar.HOUR_OF_DAY, 0);			
			d.add(Calendar.DAY_OF_YEAR, 1);
			long tomorrow = d.getTimeInMillis();
			long t = tomorrow + d.getTimeZone().getRawOffset();
			d.add(Calendar.DAY_OF_YEAR, -1);
			long today = d.getTimeInMillis();
					
		    Cursor cursor2 = Instances.query(getContentResolver(), projection, now, t-1);
		    
		    
		    if(cursor2.getCount() != 0){
				cursor2.moveToFirst();				
				do{
					if(query.getInt(query.getColumnIndex("enabled")) != 1)
						continue;
					do{
						CalendarFilter cFilter = new CalendarFilter(query);
						if(cursor2.getInt(cursor2.getColumnIndex(Instances.ALL_DAY)) != 1 && cFilter.check(cursor2)){
							long start = today;
							long end = tomorrow;
							String name = cursor2.getString(cursor2.getColumnIndexOrThrow(Instances.TITLE));
							
							boolean silent = false;
							boolean enabled = true;
							cursor2.close();
							query.close();
							return new OneTimeQuietActivity(name, start, end, silent, enabled, new Date(start), true);
						}
					}while(cursor2.moveToNext());
					
					
				}while(query.moveToNext());
			}
			
			Cursor eventCursor = Instances.query(getContentResolver(), projection, now, tomorrow);
			
			if(eventCursor.getCount() <= 0)
				return null;
			
			query.moveToFirst();
			eventCursor.moveToFirst();
			long startTime, endTime;
			do{
				String name = eventCursor.getString(eventCursor.getColumnIndex(Instances.TITLE));
				startTime = eventCursor.getLong(eventCursor.getColumnIndex(Instances.BEGIN));
				endTime = eventCursor.getLong(eventCursor.getColumnIndex(Instances.END));
				
				CalendarFilter cFilter = new CalendarFilter(query);
				if(!cFilter.getEnabled() || endTime <= now || !cFilter.check(eventCursor) || eventCursor.getInt(eventCursor.getColumnIndex(Instances.ALL_DAY)) == 1)
					continue;
				
				boolean silent = cFilter.getSilent();
				boolean enabled = cFilter.getEnabled();
				eventCursor.close();
				query.close();
				return new OneTimeQuietActivity(name, startTime, endTime, silent, enabled, new Date(startTime), true);
			}while(eventCursor.moveToNext());
		}
		
		return null;
	}
	
	
	public static boolean isQuiet(){
		if(settings == null)
			return false;
		return settings.getBoolean("isQuiet", false);
	}
	
	public static boolean isScheduled(){
		return refreshScheduled;
	}
	
	
	
	//Pre:
	//Post:	On the destruction of the Activity it will undo any QuietService silencing 
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.i("QuietService", "Serivce destroyed");
	}

	public static void setQuiet(boolean b) {
		if(settings != null){
			SharedPreferences.Editor sEditor = settings.edit();
			sEditor.putBoolean("isQuiet", b);
			sEditor.commit();
		}
	}
	
	
	private String checkGPS() {
		
		if(location == null){
			Log.w("GPS", "GPS is null");
			return null;
		}
		
		db.open();
		Cursor query = db.rawQuery("SELECT * FROM GPS", null);
		
		if(query.getCount() < 1){
			query.close();
			db.close();
			return null;
		}

		double earthR = 6371;//meters
		
		query.moveToFirst();

		do{
			if(query.getInt(query.getColumnIndex("enabled")) != 1)
				continue;
			double lat1 = query.getDouble(query.getColumnIndex("latitude"));
			double lat2 = location.getLatitude();
			double lon1 = query.getDouble(query.getColumnIndex("longitude"));
			double lon2 = location.getLongitude();
			double dLat = Math.toRadians(lat1 - lat2);
			double dLon = Math.toRadians(lon1 -lon2);
			
			double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
			
			double distance = Math.abs(earthR * c * 1000);
			
			Log.i("GPS", distance+"");
			
			double range = query.getDouble(query.getColumnIndex("radius"));
			if(distance < range){
				return query.getString(query.getColumnIndex("name"));
			}
		}while(query.moveToNext());
		query.close();
		db.close();
		return null;
	}
	
	private Calendar calculateDate(int hour, int minute, int dayIndex, int repeatIndex){
		return calculateDate(hour, minute, dayIndex, repeatIndex, null);
	}
	

	
	private Calendar calculateDate(Cursor activity, int dayIndex, int repeatIndex){
		return calculateDate(activity, dayIndex, repeatIndex, null);
	}
	
	private Calendar calculateDate(Cursor activity, int dayIndex, int repeatIndex, Calendar after){
		RecurringAdvancedQuietActivity r = new RecurringAdvancedQuietActivity(activity);
		int i;
		if(after == null){
			i = QuietActivity.START;
		}else{
			i = QuietActivity.END;
		}
		return calculateDate(r.getHour(i, true, false), r.getMinutes(i), dayIndex, repeatIndex, after);
	}
	
	private Calendar calculateDate(int hour, int minute, int dayIndex, int repeatIndex, Calendar after){
		if(after == null && !isThisWeek(repeatIndex))
			return null;
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		c.set(Calendar.DAY_OF_WEEK, dayIndex+1);
		if(after != null && after.after(c))
			c.add(Calendar.WEEK_OF_YEAR, 1);
		
		return c;
	}
	
	private boolean isThisWeek(int repeatIndex){
		Calendar c = Calendar.getInstance();
		if(repeatIndex != 0)
			return c.get(Calendar.WEEK_OF_MONTH) == repeatIndex;
		else 
			return true;
	}
}


