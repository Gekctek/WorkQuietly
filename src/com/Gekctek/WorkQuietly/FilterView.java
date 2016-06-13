package com.Gekctek.WorkQuietly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class FilterView extends LinearLayout implements OnItemSelectedListener{
	private ArrayList<ArrayList<String>> listList = new ArrayList<ArrayList<String>>();
	private ArrayList<String> typeList;
	private ArrayList<String> titleList;
	private ArrayList<String> availabilityList;
	private ArrayList<String> organizerList;
	private ArrayList<String> locationList;
	private ArrayList<String> operatorList;
	private int typeIndex = 0;
	private int choiceIndex = 0;
	private int operatorIndex = 0;
	private boolean firstRun;
	
	private Spinner typeSpinner;
	private View choiceView;
	private Spinner operatorSpinner;
	private ImageButton removeButton;
	private LinearLayout LL;
	
	private boolean firstView;
	private String textValue;
	
	

	public FilterView(Context context, boolean firstView) {
		super(context);
		firstRun = true;
		this.firstView = firstView;
		textValue = "";
		
		initializeLists();
		
		setOrientation(LinearLayout.VERTICAL);
				
		typeSpinner = new Spinner(context);
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, typeList);
		typeSpinner.setAdapter(typeAdapter);
		typeSpinner.setOnItemSelectedListener(this);
		
		choiceView = new EditText(getContext());
		choiceView.setId(0);
		
		operatorSpinner = new Spinner(context);
		ArrayAdapter<String> operatorAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, operatorList);
		operatorSpinner.setAdapter(operatorAdapter);
		
		removeButton = new ImageButton(context);
		removeButton.setImageResource(android.R.drawable.ic_menu_delete);
		
		
		LL = new LinearLayout(context);
		LL.setOrientation(LinearLayout.HORIZONTAL);
		LL.setGravity(Gravity.CENTER_VERTICAL);
		LL.addView(typeSpinner, 275, LinearLayout.LayoutParams.WRAP_CONTENT);
		LL.addView(choiceView, 275 , LinearLayout.LayoutParams.MATCH_PARENT);
		

		if(!this.firstView){
			addView(operatorSpinner, 150, LinearLayout.LayoutParams.WRAP_CONTENT);
		}else{
			operatorSpinner.setEnabled(false);
		}
		LL.addView(removeButton);
		addView(LL);
	}
	
	public FilterView(Context context, String type, String value, String operator){
		this(context, type, value, operator, false);

	}
	
	public FilterView(Context context, String type, String value, String operator, boolean firstView){
		this(context, firstView);
		typeIndex = typeList.indexOf(type);
		typeSpinner.setSelection(typeIndex);
		choiceIndex = listList.get(typeIndex).indexOf(value);
		if(typeIndex == 0 || choiceIndex == -1){
			if(typeIndex == 0){
				textValue = value;
				((EditText)findViewById(0)).setText(value);
			}else
				listList.get(typeIndex).add(value);
			
			choiceIndex = listList.get(typeIndex).indexOf(value);
		}
		
		if(operator != null){
			operatorIndex = operatorList.indexOf(operator);
			operatorSpinner.setSelection(operatorIndex);
			operatorSpinner.setOnItemSelectedListener(operatorSelected());
		}
	}

	public String getFilter(){
		String result = ":"+getFilterOp()+"~.~";
		if(getFilterOp() == "")
			result = "";
		
		return result+getFilterType()+":"+getFilterValue();
	}
	
	public ImageButton getRemoveButton(){
		return removeButton;
	}
	
	public String getFilterType(){
		return typeSpinner.getSelectedItem().toString();
	}
	
	public String getFilterValue(){
		if(typeIndex == 0){
			return ((EditText)findViewById(0)).getText().toString();
		}else{
			return ((Spinner) choiceView).getSelectedItem().toString();			
		}
	}
	
	public String getFilterOp(){
		if(!operatorSpinner.isEnabled())
			return "";
		return operatorSpinner.getSelectedItem().toString();
	}
	
	private void initializeLists(){
		SortedSet<String> titleSet = new TreeSet<String>();
		SortedSet<String> organizerSet = new TreeSet<String>();
		SortedSet<String> locationSet = new TreeSet<String>();
		
		availabilityList = new ArrayList<String>();
		availabilityList.add("AVAILABILITY_BUSY");
		availabilityList.add("AVAILABILITY_FREE");
		availabilityList.add("AVAILABILITY_TENTATIVE");
		
		typeList = new ArrayList<String>();
		typeList.add("Keyword");
		typeList.add("Title");
		typeList.add("Availability");
		typeList.add("Organizer");
		typeList.add("Location");
		
		operatorList = new ArrayList<String>();
		operatorList.add("And");
		operatorList.add("Or");
		
		
		String[] projection = {Instances._ID, 
				Instances.TITLE, 
				Instances.BEGIN, 
				Instances.END,
				Instances.ORGANIZER,
				Instances.EVENT_LOCATION};

		long now = Calendar.getInstance().getTimeInMillis();
		Cursor cursor = Instances.query(getContext().getContentResolver(), projection, now, now+31536000000000l);
		
		int titleIndex = cursor.getColumnIndex(Instances.TITLE);
		int organizerIndex = cursor.getColumnIndex(Instances.ORGANIZER);
		int locationIndex = cursor.getColumnIndex(Instances.EVENT_LOCATION);
		
		String title, org, loc;
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			title = cursor.getString(titleIndex);
			org = cursor.getString(organizerIndex);
			loc = cursor.getString(locationIndex);
			
			if(title != null && !title.trim().matches(""))
				titleSet.add(title);
			if(org != null && !org.trim().matches(""))
				organizerSet.add(org);
			if(loc != null && !loc.trim().matches(""))
				locationSet.add(loc);
							
			cursor.moveToNext();
		}
		titleList = new ArrayList<String>(Arrays.asList(titleSet.toArray(new String[titleSet.size()])));
		organizerList = new ArrayList<String>(Arrays.asList(organizerSet.toArray(new String[organizerSet.size()])));
		locationList = new ArrayList<String>(Arrays.asList(locationSet.toArray(new String[locationSet.size()])));
		
		listList.add(new ArrayList<String>());
		listList.add(titleList);
		listList.add(availabilityList);
		listList.add(organizerList);
		listList.add(locationList);
		listList.add(operatorList);		
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int itemIndex, long arg3) {
		LL.removeView(choiceView);
		typeIndex = itemIndex;
		if(itemIndex == 0){
			choiceView = new EditText(getContext());
			((EditText)choiceView).setText(textValue);
		}else{
			choiceView = new Spinner(getContext());
			String[] newSet = listList.get(itemIndex).toArray(new String[listList.get(itemIndex).size()]);
			if(newSet.length != 0){
				((Spinner)choiceView).setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, newSet));
				((Spinner)choiceView).setEnabled(true);
			}else{
				((Spinner)choiceView).setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"None Available"}));
				choiceView.setEnabled(false);
			}
			if(firstRun){
				((Spinner)choiceView).setSelection(choiceIndex);
				firstRun = false;
			}
		}

		choiceView.setId(0);
		LL.addView(choiceView, 1, new LayoutParams(275 , LinearLayout.LayoutParams.MATCH_PARENT));
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	
	public int getTypeIndex(){
		return typeIndex;
	}
	
	public int getChoiceIndex(){
		return choiceIndex;
	}
	
	private OnItemSelectedListener operatorSelected(){
		return new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int itemIndex, long arg3) {
				String[] opSet = operatorList.toArray(new String[operatorList.size()]);
				if(opSet[itemIndex].equalsIgnoreCase("and")){
					
				}else{
					
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		};
	}
	
	public void removeOperator(){
		removeView(operatorSpinner);
		operatorSpinner.setEnabled(false);
	}
	
}

