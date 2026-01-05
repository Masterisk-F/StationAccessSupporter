package com.masterisk_f.accesssupporter.Tabs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;

import com.masterisk_f.accesssupporter.AccessSupporterApplication;
import com.masterisk_f.accesssupporter.AccessSupporterService;
import com.masterisk_f.accesssupporter.StationData.Station;

import java.util.List;


public class NearbyStationsFragment extends StationsListFragment {
	/*// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	
	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	
	private OnFragmentInteractionListener mListener;
	*/
	
	BroadcastReceiver locationChangedReceiver=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setStationsList();
		}
	};
	
	public NearbyStationsFragment() {
		// Required empty public constructor
	}
	
	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment NearbyStationsFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static NearbyStationsFragment newInstance(String param1, String param2) {
		NearbyStationsFragment fragment = new NearbyStationsFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		setStationsList();
		
		IntentFilter filter=new IntentFilter();
		filter.addAction(AccessSupporterService.LOCATION_CANGED);
		if (android.os.Build.VERSION.SDK_INT >= 33) {
			getActivity().registerReceiver(locationChangedReceiver,filter, Context.RECEIVER_NOT_EXPORTED);
		} else {
			getActivity().registerReceiver(locationChangedReceiver,filter);
		}
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		getActivity().unregisterReceiver(locationChangedReceiver);
	}
	
	void setStationsList(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				AccessSupporterApplication application=(AccessSupporterApplication)getActivity().getApplication();
				if(application==null || application.getStationHandler()==null){
					return;
				}
				Location location=application.getCurrentLocation();
				
				if(location==null){
					return;
				}
				
				final List<Station> list=application.getStationHandler().getNearbyStationsList(location);
				
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setStationsList(list,true);
					}
				});
			}
		}).start();
		
	}
	
	
	
}
