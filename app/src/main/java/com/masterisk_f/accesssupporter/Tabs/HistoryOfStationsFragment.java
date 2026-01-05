package com.masterisk_f.accesssupporter.Tabs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import com.masterisk_f.accesssupporter.AccessSupporterApplication;
import com.masterisk_f.accesssupporter.AccessSupporterService;
import com.masterisk_f.accesssupporter.StationData.Station;

import java.util.List;

public class HistoryOfStationsFragment extends StationsListFragment {
	/*// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
*/
	
	
	BroadcastReceiver nearestStationChangedReceiver=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setStationsList();
		}
	};
	
	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment HistoryOfStationsFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static HistoryOfStationsFragment newInstance(String param1, String param2) {
		HistoryOfStationsFragment fragment = new HistoryOfStationsFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}
	
	
	public HistoryOfStationsFragment() {
		// Required empty public constructor
	}
	
	
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		setStationsList();
		
		IntentFilter filter=new IntentFilter();
		filter.addAction(AccessSupporterService.NEAREST_STATION_CHANGED);
		if (android.os.Build.VERSION.SDK_INT >= 33) {
			getActivity().registerReceiver(nearestStationChangedReceiver,filter, Context.RECEIVER_NOT_EXPORTED);
		} else {
			getActivity().registerReceiver(nearestStationChangedReceiver,filter);
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		getActivity().unregisterReceiver(nearestStationChangedReceiver);
	}
	
	void setStationsList(){
		List<Station> list=((AccessSupporterApplication)getActivity().getApplication()).getStationsHistory();
		setStationsList(list,false);
	}

}
