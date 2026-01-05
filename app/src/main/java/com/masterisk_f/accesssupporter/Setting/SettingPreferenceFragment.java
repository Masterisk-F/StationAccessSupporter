package com.masterisk_f.accesssupporter.Setting;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.masterisk_f.accesssupporter.R;

public class SettingPreferenceFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preference);

		findPreference("data_version").setSummary(getDataVersion());
	}

	private String getDataVersion() {
		try {
			java.io.InputStream is = getActivity().getAssets().open("latest_info.json");
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			is.close();
			String json = new String(buffer, "UTF-8");
			org.json.JSONObject jsonObject = new org.json.JSONObject(json);
			return String.valueOf(jsonObject.getInt("version"));
		} catch (Exception e) {
			e.printStackTrace();
			return "不明";
		}
	}
}
