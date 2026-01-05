package com.masterisk_f.accesssupporter.Setting;

import android.app.Activity;
import android.os.Bundle;


public class SettingActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content,new SettingPreferenceFragment())
				.commit();
	}
}
