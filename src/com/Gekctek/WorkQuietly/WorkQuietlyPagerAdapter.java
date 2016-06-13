package com.Gekctek.WorkQuietly;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class WorkQuietlyPagerAdapter extends FragmentPagerAdapter {
	private FragmentManager fragmentManager;
	
	public WorkQuietlyPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
		this.fragmentManager = fragmentManager;
	}


	@Override
	public Fragment getItem(int tab) {
		Fragment r = null;
		switch(tab){
		case 0:
			r = new RecurringEventsFragment();
			break;
		case 1:
			r = new OneTimeEventsFragment();
			break;
		case 2:
			r = new CalendarEventsFragment();
			break;
		case 3:
			r = new GPSEventsFragment();
			break;
		}
		return r;
	}

	@Override
	public int getCount() {
		return 4;
	}

}
