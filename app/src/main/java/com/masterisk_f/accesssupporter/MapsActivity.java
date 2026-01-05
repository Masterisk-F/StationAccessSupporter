package com.masterisk_f.accesssupporter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.masterisk_f.accesssupporter.StationData.Station;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,CompoundButton.OnCheckedChangeListener {
	
	
	private GoogleMap mMap;
	private MapView mapView;
	
	ToggleButton nearbyStationButton;
	ToggleButton historyButton;
	
	List<Marker> nearbyStations;
	List<Marker> historyStations;
	
	
	
	BroadcastReceiver locationChangedListener=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setHistoryStations();
			setNearbyStations();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		
		mapView=(MapView)findViewById(R.id.mapView);
		mapView.getMapAsync(this);
		mapView.onCreate(savedInstanceState);
		
		/*
		現在地ボタンの位置変更
		参考:
		http://storyboard.jp/blog/locationbutton_googlemap/
		*/
		//View test=(View) mapView.findViewById(Integer.parseInt("1")).getParent();
		View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
		RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
		rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
		rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		rlp.setMargins(30, 30, 30, 200);
		/*
		//コンパスボタン
		View compassButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("5"));
		rlp = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
		rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
		rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
		rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
		rlp.setMargins(30, 30, 30, 30);
		*/
		
		nearbyStationButton=(ToggleButton)findViewById(R.id.showNearbyStations);
		nearbyStationButton.setOnCheckedChangeListener(this);
		
		historyButton=(ToggleButton)findViewById(R.id.showHistory);
		historyButton.setOnCheckedChangeListener(this);
		
		IntentFilter filter=new IntentFilter();
		filter.addAction(AccessSupporterService.LOCATION_CANGED);
		if (android.os.Build.VERSION.SDK_INT >= 33) {
			getApplication().registerReceiver(locationChangedListener,filter, Context.RECEIVER_NOT_EXPORTED);
		} else {
			getApplication().registerReceiver(locationChangedListener,filter);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mapView.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mapView.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		getApplication().unregisterReceiver(locationChangedListener);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}
	
	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.getUiSettings().setRotateGesturesEnabled(false);
		mMap.getUiSettings().setTiltGesturesEnabled(false);
		mMap.setIndoorEnabled(false);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			mMap.setMyLocationEnabled(true);
		}
		
		mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style));
		
		LatLng current;
		AccessSupporterApplication application=(AccessSupporterApplication)getApplication();
		if(application.getCurrentLocation()==null){
			current=new LatLng(35.681391,139.766103);//東京駅
		}else{
			Location loc=application.getCurrentLocation();
			current=new LatLng(loc.getLatitude(),loc.getLongitude());
		}
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,14f));
		
		
		
		setHistoryStations();
		setNearbyStations();
	}
	
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(buttonView==nearbyStationButton){
			setNearbyStations();
		}else if(buttonView==historyButton){
			setHistoryStations();
		}
		
	}
	void setNearbyStations(){
		AccessSupporterApplication application=(AccessSupporterApplication)getApplication();
		if(nearbyStations==null && application.getCurrentLocation()!=null){
			int num=80;
			List<Station>list=application.getStationHandler()
					.getNearbyStationsList(application.getCurrentLocation(),num);
			nearbyStations=new ArrayList<Marker>(num);
			for(Station sta : list){
				LatLng latLng=new LatLng(sta.getLatitude(),sta.getLongitude());
				Marker mk=mMap.addMarker(new MarkerOptions().position(latLng)
						.title(sta.getStationName())
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
						.zIndex(0f));
				nearbyStations.add(mk);
			}
		}
		
		if(nearbyStations!=null){
			for(Marker mk:nearbyStations){
				mk.setVisible(nearbyStationButton.isChecked());
			}
		}
		
	}
	
	void removeHistoryStations(){
		if(historyStations!=null){
			for(Marker mk:historyStations){
				mk.remove();
			}
		}
	}
	
	void setHistoryStations(){
		removeHistoryStations();
		
		AccessSupporterApplication application=(AccessSupporterApplication)getApplication();
		if(historyButton.isChecked()){
			List<Station> list=application.getStationsHistory();
			historyStations=new ArrayList<Marker>();
			for(Station st:list){
				LatLng latLng=new LatLng(st.getLatitude(),st.getLongitude());
				Marker mk=mMap.addMarker(new MarkerOptions().position(latLng)
						.title(st.getStationName())
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
						.zIndex(1.0f));
				historyStations.add(mk);
			}
		}
		
	}
}
