package com.Gekctek.WorkQuietly;

import android.app.Activity;
import android.os.Bundle;

public class EditPreferences extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		        .replace(android.R.id.content, new EditPreferenceFragment())
		        .commit();
	}
}
