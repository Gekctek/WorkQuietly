package com.Gekctek.WorkQuietly;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

public class AddCalendarFilter extends Activity {
	private boolean edit;
	private EditText filterNameBox;
	private ArrayList<FilterView> filterList;
	private LinearLayout LL;
	private String editName;
	private ImageButton add;
	private Switch enabled;
	private CheckBox vibrate;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DBHelper db = new DBHelper(this);
		Bundle b = getIntent().getExtras();
		
		edit = false;
		if(b != null){
			edit = getIntent().getExtras().getBoolean("Edit", false);
			editName = getIntent().getExtras().getString("Name");
		}
		
		filterNameBox = new EditText(this);
		filterNameBox.setHint("Filter Name");
		filterList = new ArrayList<FilterView>();
		enabled = new Switch(this);
		enabled.setText("Enabled");
		vibrate = new CheckBox(this);
		vibrate.setText("Vibrate");
		if(edit){
			String[] args = {editName};
			db.open();
			Cursor filterQuery = db.rawQuery("SELECT * FROM CalendarFilter WHERE name=?", args);
			filterQuery.moveToFirst();
			CalendarFilter cFilter = new CalendarFilter(filterQuery);
			filterList = getViews(cFilter.getFilters());			
			filterNameBox.setText(editName);
			enabled.setChecked(cFilter.getEnabled());
			vibrate.setChecked(!cFilter.getSilent());
		}else{
			filterList.add(new FilterView(this, true));
			enabled.setChecked(true);
			vibrate.setChecked(true);
		}

		

		refreshView();	
			
		db.close();
	}
	
	public void refreshView(){
		ScrollView sV = new ScrollView(getBaseContext());
		int padding_in_dp = 16;  // 6 dp
	    final float scale = getResources().getDisplayMetrics().density;
	    int dp = (int) (padding_in_dp * scale + 0.5f);
		sV.setPadding(dp, dp, dp, dp);
		LL = new LinearLayout(this);
		LL.setOrientation(LinearLayout.VERTICAL);
		
		LL.addView(filterNameBox);
		LinearLayout subLL = new LinearLayout(this);
		subLL.setOrientation(LinearLayout.HORIZONTAL);
		subLL.setGravity(Gravity.CENTER);
		subLL.addView(enabled);
		subLL.addView(vibrate);
		
		LL.addView(subLL);
		
		for(FilterView v : filterList){
			v.getRemoveButton().setOnClickListener(removeFilter());
			LL.addView(v);
		}
		add = new ImageButton(this);
		add.setImageResource(android.R.drawable.ic_menu_add);
		add.setLayoutParams( 
				new LayoutParams(
		        110,         
		        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
		    ));
		add.setOnClickListener(addNewFilter());
		LL.addView(add);
				
		sV.addView(LL);
		setContentView(sV);
	}
	
	public ArrayList<FilterView> getViews(String filters){
		ArrayList<FilterView> list = new ArrayList<FilterView>();

		boolean first = true;
		String[] fL = filters.split("~\\.~|:", 3);
		while(first || fL.length >= 3){
			if(first){
				list.add(new FilterView(this, fL[0], fL[1], null, true));
				if(fL.length > 2)
					fL = fL[2].split("~\\.~|:", 4);
				else
					return list;
				first = false;
			}else{
				list.add(new FilterView(this, fL[1], fL[2], fL[0]));
				if(fL.length < 4)
					break;
				fL = fL[3].split("~\\.~|:", 4);
			}
		}
		
		return list;
	}
	
	private boolean updateDB(){
		CalendarFilter cFilter = new CalendarFilter(filterNameBox.getText().toString(), enabled.isChecked(), !vibrate.isChecked());
		for(FilterView v : filterList){
			cFilter.add(v);
		}
		int result;
		DBHelper db = new DBHelper(this);
		if(edit)
			result = db.updateDB(editName, cFilter);
		else
			result = db.addToDB(cFilter);
		
		return result != -1;
		
	}
	
	private OnClickListener addNewFilter(){
		final Context ct = this;
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int index = LL.indexOfChild(add);
				FilterView newFV = new FilterView(ct, filterList.size() == 0);
				filterList.add(newFV);
				newFV.getRemoveButton().setOnClickListener(removeFilter());
				LL.addView(newFV, index);
			}
		};
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.calendar_filter_menu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.calendar_filter_save:
        	if(!filterNameBox.getText().toString().trim().matches("")){
    			if(filterList.size() > 0 && updateDB()){
    				setResult(RESULT_OK);
    				finish();
    			}else{
    				Toast.makeText(getBaseContext(), "Filter(s) are invalid", Toast.LENGTH_SHORT).show();
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
	
	private OnClickListener removeFilter(){
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				FilterView fv = (FilterView) v.getParent().getParent();
				LL.removeView(fv);
				filterList.remove(fv);
				if(filterList.size() > 0)
					filterList.get(0).removeOperator();
			}
		};
	}
}

