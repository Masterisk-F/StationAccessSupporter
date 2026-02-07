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

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.location.LocationComponentActivationOptions; // 追加
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.IconFactory;
import com.masterisk_f.accesssupporter.StationData.Station;

import android.graphics.Bitmap;
import android.graphics.Color;
import org.maplibre.android.annotations.Icon;

import java.util.ArrayList;
import java.util.List;

	public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,CompoundButton.OnCheckedChangeListener {
	
	
	private MapLibreMap mMap;
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
		MapLibre.getInstance(this);
		setContentView(R.layout.activity_maps);
		
		mapView=(MapView)findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);
		
		
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
	 * Mapが利用可能になった際の処理
\	 */
	@Override
	public void onMapReady(MapLibreMap mapLibreMap) {
		mMap = mapLibreMap;
		mMap.getUiSettings().setRotateGesturesEnabled(false);
		mMap.getUiSettings().setTiltGesturesEnabled(false);
		//mMap.setIndoorEnabled(false); // MapLibreには直接的なIndoorモード設定がない場合がある
		
		// OSMスタイルを設定
		mMap.setStyle("https://tile.openstreetmap.jp/styles/osm-bright-ja/style.json", new Style.OnStyleLoaded() {
			@Override
			public void onStyleLoaded(Style style) {
				if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
						== PackageManager.PERMISSION_GRANTED) {
					LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(MapsActivity.this, style)
							.build();
					mMap.getLocationComponent().activateLocationComponent(locationComponentActivationOptions);
					mMap.getLocationComponent().setLocationComponentEnabled(true);
				}
			}
		});
		
		LatLng current;
		AccessSupporterApplication application=(AccessSupporterApplication)getApplication();
		if(application.getCurrentLocation()==null){
			current=new LatLng(35.681391,139.766103);//東京駅
		}else{
			Location loc=application.getCurrentLocation();
			current=new LatLng(loc.getLatitude(),loc.getLongitude());
		}
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,14.0));
		
		
		
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
		// 既存のマーカーを削除
		if(nearbyStations!=null){
			for(Marker mk:nearbyStations){
				mk.remove();
			}
			nearbyStations.clear();
		} else {
			nearbyStations = new ArrayList<>();
		}
		
		if(!nearbyStationButton.isChecked()){
			return;
		}

		AccessSupporterApplication application=(AccessSupporterApplication)getApplication();
		if(application.getCurrentLocation()!=null){
			int num=80;
			List<Station>list=application.getStationHandler()
					.getNearbyStationsList(application.getCurrentLocation(),num);
			
			for(Station sta : list){
				LatLng latLng=new LatLng(sta.getLatitude(),sta.getLongitude());
				Marker mk=mMap.addMarker(new MarkerOptions().position(latLng)
						.title(sta.getStationName())
						.icon(getMarkerIcon(240.0f))); // HUE_BLUE
				nearbyStations.add(mk);
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
						.icon(getMarkerIcon(120.0f))); // HUE_GREEN
						//.zIndex(1.0f));
				historyStations.add(mk);
			}
		}
		
	}

	// マーカーの色を変更するヘルパーメソッド
	// Google Maps APIの defaultMarker(float hue) を模倣
	private Icon getMarkerIcon(float hue) {
		IconFactory iconFactory = IconFactory.getInstance(this);
		Icon defaultIcon = iconFactory.defaultMarker();
		Bitmap source = defaultIcon.getBitmap();
		
		int width = source.getWidth();
		int height = source.getHeight();
		int[] pixels = new int[width * height];
		source.getPixels(pixels, 0, width, 0, 0, width, height);
		
		float[] hsv = new float[3];
		for (int i = 0; i < pixels.length; i++) {
			int color = pixels[i];
			// アルファチャンネルを保持
			int alpha = Color.alpha(color);
			if (alpha == 0) continue; // 透明ならスキップ
			
			Color.colorToHSV(color, hsv);
			
			// 彩度が低い（白い部分や影）は色相変更の影響を受けすぎないように配慮もできるが、
			// シンプルに色相を上書きする
			hsv[0] = hue;
			
			pixels[i] = Color.HSVToColor(alpha, hsv);
		}
		
		Bitmap coloredBitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
		return iconFactory.fromBitmap(coloredBitmap);
	}
}
