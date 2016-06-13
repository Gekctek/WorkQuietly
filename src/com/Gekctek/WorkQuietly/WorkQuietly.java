package com.Gekctek.WorkQuietly;

import android.app.Application;

public class WorkQuietly extends Application {

	  private String myState;

	  public String getState(){
		  return myState;
	  }
	  public void setState(String s){
		  myState = s;
	  }
}
