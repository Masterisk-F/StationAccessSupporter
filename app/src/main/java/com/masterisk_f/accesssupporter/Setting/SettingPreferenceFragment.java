package com.masterisk_f.accesssupporter.Setting;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.app.AlertDialog;
import android.widget.ScrollView;
import android.widget.TextView;

import com.masterisk_f.accesssupporter.R;

public class SettingPreferenceFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preference);

		findPreference("data_version").setSummary(getDataVersion());
		
		findPreference("oss_licenses").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showLicenseDialog();
				return true;
			}
		});
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

	private void showLicenseDialog() {
		try {
			java.io.InputStream is = getActivity().getAssets().open("licenses.txt");
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			is.close();
			String licenseText = new String(buffer, "UTF-8");

			ScrollView scrollView = new ScrollView(getActivity());
			TextView textView = new TextView(getActivity());
			textView.setText(licenseText);
			textView.setPadding(20, 20, 20, 20);
			scrollView.addView(textView);

			new AlertDialog.Builder(getActivity())
					.setTitle("オープンソースライセンス")
					.setView(scrollView)
					.setPositiveButton("閉じる", null)
					.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
