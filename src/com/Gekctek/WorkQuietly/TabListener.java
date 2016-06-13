package com.Gekctek.WorkQuietly;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;

public class TabListener<T extends Fragment> implements ActionBar.TabListener{
	private Fragment mFragment;
	private Activity mActivity;
	private Class<T> mFragmentClass;
	private String mTag;
	
	
	public TabListener(Activity activity, String fTag, Class<T> fClass){
		this.mTag = fTag;
		this.mFragmentClass = fClass;
		this.mActivity = activity;
		
		mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
		if (mFragment != null && !mFragment.isDetached()) {
		    FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		    ft.detach(mFragment);
		    ft.commit();
		}
	}
	
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
			
		if(mFragment == null){
			String fragmentName = mFragmentClass.getName();
			mFragment = (Fragment)Fragment.instantiate(mActivity, fragmentName);
			ft.add(android.R.id.content, mFragment, mTag);
		}else{
			ft.attach(mFragment);
		}
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if(mFragment != null){
			ft.detach(mFragment);
		}
	}	
}