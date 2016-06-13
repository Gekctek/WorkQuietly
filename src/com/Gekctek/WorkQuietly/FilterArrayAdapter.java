package com.Gekctek.WorkQuietly;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

class FilterArrayAdapter extends ArrayAdapter<String> {
	private String[] cItems;
	private Context context;

	//Constructors
	
	//Pre:	
	//Post:	Constructs this array adapter
	public FilterArrayAdapter(Context context, int textViewResourceId, String[] items, String tab) {
		super(context, textViewResourceId, items);
		this.cItems = items;
		this.context = context;
	}

	
	//Get Methods
	
	//Pre:	
	//Post:	Returns the view from this adapter at each list item
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater=((Activity)context).getLayoutInflater();
		View row = inflater.inflate(R.layout.filter_list, parent, false);
		CheckedTextView txt = (CheckedTextView)row.findViewById(R.id.item_list);
		txt.setText(cItems[position]);		
		
		return row;
	}
}