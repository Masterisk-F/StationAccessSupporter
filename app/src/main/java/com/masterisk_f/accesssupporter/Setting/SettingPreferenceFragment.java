package com.masterisk_f.accesssupporter.Setting;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.masterisk_f.accesssupporter.R;

public class SettingPreferenceFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preference);
	}
}
