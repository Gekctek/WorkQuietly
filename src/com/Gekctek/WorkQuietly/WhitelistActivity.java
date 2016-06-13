package com.Gekctek.WorkQuietly;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class WhitelistActivity extends Activity implements OnItemLongClickListener, OnItemClickListener{
	private final int PICK_CONTACT = 1;
	private ListView contactList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.whitelist_activity);
		setTitle("Whitelist");
		
		
		contactList = (ListView)findViewById(R.id.whitelist_listview);
		contactList.setOnItemClickListener(this);
		contactList.setOnItemLongClickListener(this);
		refreshView();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.whitelist_menu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.whitelist_add:
        	Intent intent = new Intent(Intent.ACTION_PICK, CommonDataKinds.Phone.CONTENT_URI);
        	startActivityForResult(intent, PICK_CONTACT);
        	break;
        default:
        	return false;
        
        }
        return true;
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = getContentResolver().query(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String contactId = c.getString(c.getColumnIndex(CommonDataKinds.Phone.CONTACT_ID));
					String contactNumber = c.getString(c.getColumnIndex(CommonDataKinds.Phone.NUMBER));
					String contactName = c.getString(c.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
					int type = c.getInt(c.getColumnIndex(CommonDataKinds.Phone.TYPE));
					String numberType = (String) CommonDataKinds.Phone.getTypeLabel(getResources(), type, "");
					
					DBHelper db = new DBHelper(this);
					db.open();
					if(db.addToDB(contactNumber, contactName, contactId, numberType) == -1)
						Toast.makeText(this, "This number is already on the whitelist", Toast.LENGTH_SHORT).show();
					else
						refreshView();
					
					c.close();
					db.close();
				}
			}
			break;
		}
	}
	
	public void refreshView(){
		DBHelper db = new DBHelper(getBaseContext());
		db.open();
		Cursor contacts = db.rawQuery("SELECT * FROM WHITELIST", null);
		String[] contactNumbers = new String[contacts.getCount()];
		if(contacts.moveToFirst()){
			int numberIndex = contacts.getColumnIndex("displayNumber");
			int nameIndex = contacts.getColumnIndex("name");
			int typeIndex = contacts.getColumnIndex("numberType");
			for(int i = 0; i<contacts.getCount(); i++){
				contactNumbers[i] = contacts.getString(nameIndex) + " ("+ contacts.getString(typeIndex)+ "): " + contacts.getString(numberIndex);	
				contacts.moveToNext();
			}
		}
		contactList.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, contactNumbers));
		db.close();
		contacts.close();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final String name = (String)arg0.getAdapter().getItem(arg2);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this)
			.setTitle("Delete")
			.setMessage("Do you want to delete this Number")
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
				}
			})
			.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					DBHelper db = new DBHelper(getBaseContext());
					db.open();
					db.deleteWhitelistNumber(name.split(":")[1]);
					db.close();
					refreshView();
				}
			});
		dialog.create().show();
		return false;
	}	
}
