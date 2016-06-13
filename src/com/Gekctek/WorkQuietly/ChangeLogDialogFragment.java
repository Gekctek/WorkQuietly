package com.Gekctek.WorkQuietly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ChangeLogDialogFragment extends DialogFragment {

	public ChangeLogDialogFragment(){}
	 
	@Override
	 public Dialog onCreateDialog(Bundle savedInstanceState){
	    // Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(readChangeLog())
			   .setCancelable(false)
			   .setTitle("Change Log")
		       .setPositiveButton(R.string.close_button, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dismiss();
		           }
		       });
		return builder.create();
	 }
	 
	 
	 
	 public static ChangeLogDialogFragment newInstance(){
		ChangeLogDialogFragment f = new ChangeLogDialogFragment();
		Bundle args = new Bundle();
		f.setArguments(args);
		
		return f;
	}
	 
	 private String readChangeLog(){
		 InputStream inputStream = getResources().openRawResource(R.raw.change_log);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	        String line = "";
			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        String fileText = "";
	        while (line != null) { 
	        	fileText += line+"\n";
	        	try {
					line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return fileText;
	 }
}
