package com.masterisk_f.accesssupporter.Tabs;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.masterisk_f.accesssupporter.AccessSupporterApplication;
import com.masterisk_f.accesssupporter.R;
import com.masterisk_f.accesssupporter.StationData.Station;

import java.util.List;


public abstract class StationsListFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	static final String ARG_PARAM1 = "param1";
	static final String ARG_PARAM2 = "param2";
	
	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	
	
	LinearLayout linearLayout;
	
	public StationsListFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getArguments() != null){
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view=inflater.inflate(R.layout.fragment_stations_list, container, false);
		
		linearLayout=(LinearLayout)view.findViewById(R.id.stationsList);
		
		return view;
	}
	
	void setStationsList(List<Station> list,boolean displayDistance){
		if(linearLayout==null){
			return;
		}
		
		linearLayout.removeAllViews();
		Location loc=null;
		if(displayDistance){
			loc=((AccessSupporterApplication)getActivity().getApplication()).getCurrentLocation();
		}
		for(int i=0;i<list.size();i++){
			double distance=-1;
			
			if(loc!=null){
				distance=list.get(i).distanceTo(loc.getLongitude(),loc.getLatitude());
			}
			
			linearLayout.addView(new StationView(getContext()).setStaton(i,list.get(i),(int)distance));
		}
		
	}
	
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
}
