package com.Gekctek.WorkQuietly;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

class ActivityArrayAdapter extends ArrayAdapter<Object> {
	private Object[] activityItems;
	private String tab;
	private LayoutInflater inflator;

	//Constructors
	
	//Pre:	
	//Post:	Constructs this array adapter
	public ActivityArrayAdapter(Context context, int textViewResourceId, Object[] items, String tab) {
		super(context, textViewResourceId, items);
		this.activityItems = items;
		this.tab = tab;
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
		
	
	
	
	//Get Methods
	
	//Pre:	
	//Post:	Returns the view from this adapter at each list item
	public View getView(int position, View convertView, ViewGroup parent){
		Calendar start = Calendar.getInstance();
		Calendar end  = Calendar.getInstance();
		String format;
		if(PreferenceManager.getDefaultSharedPreferences(WorkQuietlyActivity.context).getBoolean("clockFormat", false))
			format = "H:mm";
		else
			format = "h:mm a";
			
		SimpleDateFormat date = new SimpleDateFormat(format, Locale.getDefault());
		View row;
		
		if(tab.toLowerCase().equals("recurring")){
			QuietActivity[] items = (QuietActivity[]) activityItems;
			row=inflator.inflate(R.layout.recurring_checked_list, parent, false);

			QuietActivity activity = items[position];
			RecurringTextView txt = (RecurringTextView)row.findViewById(R.id.recurring_item_check);
			txt.setText(activity.getName());
			txt.setName(activity.getName());
			if(activity instanceof RecurringQuietActivity){
				RecurringQuietActivity r = (RecurringQuietActivity)activity;
				
				start.setTimeInMillis(r.getTimeInMillis(QuietActivity.START));
				String dateString = date.format(start.getTime());
				txt.setStartTime(dateString);
				
				end.setTimeInMillis(r.getTimeInMillis(QuietActivity.END));
				String dateString2 = date.format(end.getTime());
				txt.setEndTime(dateString2);
				
				
				txt.setEnabled(r.getEnabled());
				String datez = "";
				String[] days = {"M","Tu","W","Th","F","Sa","Su"};
				boolean[] dW = r.getDays();
				for(int i = 0; i<7; i++){
					if(dW[i] && datez == "")
						datez += days[i];
					else if(dW[i])
						datez += "-"+days[i];
				}
				txt.setDate(datez);
				txt.setChecked(r.getEnabled());
			}else{
				RecurringAdvancedQuietActivity r = (RecurringAdvancedQuietActivity)activity;
				txt.setStartTime(r.dateString(QuietActivity.START));
				txt.setEndTime(r.dateString(QuietActivity.END));
				txt.setDate(r.getRepeat());
				txt.setChecked(r.getEnabled());
			}
		}else if(tab.toLowerCase().equals("calendar")){
			CalendarFilter[] items  = (CalendarFilter[]) activityItems;
			row = inflator.inflate(R.layout.calendar_checked_list, parent, false);
			CalendarTextView txt = (CalendarTextView)row.findViewById(R.id.calendar_item_check);
			txt.setText(items[position].getName());
			txt.setFilterText(items[position].getFilters());
			txt.setChecked(items[position].getEnabled());
			txt.setName(items[position].getName());
			
		}else if(tab.toLowerCase().equals("gps")){
			GPSFilter[] items  = (GPSFilter[]) activityItems;
			row = inflator.inflate(R.layout.gps_checked_list, parent, false);
			GPSTextView txt = (GPSTextView)row.findViewById(R.id.gps_item_check);
			txt.setText(items[position].getName());
			txt.setRadius(items[position].getRadius());
			txt.setChecked(items[position].getEnabled());
			txt.setName(items[position].getName());
			txt.setLatitude(items[position].getLatitude());
			txt.setLongitude(items[position].getLongitude());
		}else{
			OneTimeQuietActivity[] items = (OneTimeQuietActivity[]) activityItems;
			row=inflator.inflate(R.layout.one_time_checked_list, parent, false);
			OneTimeTextView txt = (OneTimeTextView)row.findViewById(R.id.one_time_item_check);
			txt.setText(items[position].getName());
	
			end.setTimeInMillis(items[position].getTimeInMillis(QuietActivity.END));
			String dateString2 = date.format(end.getTime());
			txt.setEndTime(dateString2);
			
			start.setTimeInMillis(items[position].getTimeInMillis(QuietActivity.START));
			String dateString = date.format(start.getTime());
			txt.setStartTime(dateString);
			
			
			txt.setName(items[position].getName());
			
			txt.setEnabled(true);
			
			date = new SimpleDateFormat("MMM d, y", Locale.getDefault());
			String startDateString = date.format(start.getTime());
			String endDateString = date.format(end.getTime());
			txt.setStartTime(dateString);			
			
			txt.setStartDate(startDateString);
			txt.setEndDate(endDateString);
			txt.setChecked(true);		
		}
		
		return row;
	}
}
