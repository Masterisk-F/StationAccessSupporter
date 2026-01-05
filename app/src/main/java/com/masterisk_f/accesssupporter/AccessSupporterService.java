package com.masterisk_f.accesssupporter;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


import com.masterisk_f.accesssupporter.StationData.Station;
import com.masterisk_f.accesssupporter.StationData.StationHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;






public class AccessSupporterService extends Service implements LocationListener{
	NotificationManager notificationManager;
	
	LocationManager locationManager;
	
	StationHandler stationHandler;
	
	
	SharedPreferences prefs;
	
	Bitmap largeIcon;//通知領域を表示した時に出てくるアイコン
	
	private boolean isRunning=false;//最寄り駅通知を行っている間true
	
	Station currentStation;//現在の最寄り駅
	
	Location currentLocation;//最新の位置情報
	long gpsSignalTime=0;//gpsの最新の受信時刻
	long gpsEnabledTime=20000;//gpsを最後に受信してから、gpsが有効である時間[ms]



	Thread intervalsThread;//5分毎の通知処理で使うThreadをここに置いておく(割り込みを使うため)
	
	//broadcastのキー
	//log用
	public static String STATUS_CHANGED="com.masterisk_f.AccessSupporter.StatusChanged";
	//位置情報更新時
	public static String LOCATION_CANGED="com.masterisk_f.AccessSupporter.LocationChanged";
	//最寄り駅変更時
	public static String NEAREST_STATION_CHANGED="com.masterisk_f.AccessSupporter.NearestStationChanged";
	

	
	boolean screenOn=true;
	BroadcastReceiver screenActionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null) {
				if (action.equals(Intent.ACTION_SCREEN_ON)) {
					// 画面ON時  
					Log.d("AccessSupporter", "SCREEN_ON");
					screenOn=true;
				} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
					// 画面OFF時  
					Log.d("AccessSupporter", "SCREEN_OFF");
					screenOn=false;
				}
			}
		}
	};
	
	public AccessSupporterService(){
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		locationManager=(LocationManager) getSystemService(Service.LOCATION_SERVICE);
		
		largeIcon= BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
		
		prefs=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		((AccessSupporterApplication)getApplication()).setAccessSupporterService(this);
		

		
		registerReceiver(screenActionReceiver,new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(screenActionReceiver,new IntentFilter(Intent.ACTION_SCREEN_OFF));

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(screenActionReceiver);
		
		((AccessSupporterApplication)getApplication()).setAccessSupporterService(null);
		

	}
	
	
	//startService(Intent)で呼び出される
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent==null){//念のため
			return super.onStartCommand(intent,flags,startId);
		}
		Log.d("AccessSupporter",intent.getAction());
		if(intent.getAction().equals("activityCreated")){
			if(stationHandler==null){
				try{
					stationHandler = new StationHandler(
							new InputStreamReader(getAssets().open("station.csv", AssetManager.ACCESS_BUFFER)),
							new InputStreamReader(getAssets().open("line.csv", AssetManager.ACCESS_BUFFER)),
							new InputStreamReader(getAssets().open("register.csv", AssetManager.ACCESS_BUFFER))
					);
					//Applicationに登録
					((AccessSupporterApplication)getApplication()).setStationHandler(stationHandler);
				}catch(IOException e){
					e.printStackTrace();
					Toast.makeText(this,"ファイルを読み込めません",Toast.LENGTH_SHORT).show();
				}
			}
			
			
		}else if(intent.getAction().equals("activityDestroyed")){
			if(!isRunning){
				stopSelf();//service終了
			}
		}else if(intent.getAction().equals("stop")){
			stopLocationUpdate();
			stopForeground(true);//foreground終了
			
			isRunning=false;
			
		}else if(intent.getAction().equals("start")){
			currentStation=null;
			currentLocation=null;
			
			gpsSignalTime=0;
			
			isRunning=true;
			
			// チャンネルの作成 (Android 8.0+)
			createNotificationChannels();

			//最初に表示する通知
			android.app.Notification notification = createNotification(
					"AccessSupporter",
					null,
					PendingIntent.getActivity(this,1,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE),
					false // 初期表示は振動なし
			);
			startForeground(1, notification);//foregroundにする
			
			startLocationUpdate();
			
			screenOn=true;
		}

		return super.onStartCommand(intent, flags, startId);
	}

	public boolean isRunning(){
		return isRunning;
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	void startLocationUpdate(){//位置情報開始
		if(locationManager==null){
			Log.d("AccessSupporter","locationManager is null");
			return;
		}

		int minUpdateDistance=Integer.valueOf(prefs.getString("min_update_distance","50"));
		int minUpdateTime=Integer.valueOf(prefs.getString("min_update_time","5"));


		if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
				==PackageManager.PERMISSION_GRANTED){

			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					minUpdateTime,
					minUpdateDistance,
					this
			);
			
			Log.d("AccessSupporter","requestLocationUpdates(),gps");
		}
		if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
				==PackageManager.PERMISSION_GRANTED){
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER,
					minUpdateTime,
					minUpdateDistance,
					this
			);
		}

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			locationManager.registerGnssStatusCallback(gnssStatusCallback);
		}
	}

	void stopLocationUpdate(){//位置情報終了
		if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
				==PackageManager.PERMISSION_GRANTED
				||ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
				==PackageManager.PERMISSION_GRANTED){
			locationManager.removeUpdates(this);
		}
		locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
	}

	
	//位置情報が更新されたとき呼ばれる
	@Override
	public void onLocationChanged(Location location) {
		Log.d("AccessSupporter","onLocationChanged : provider="+location.getProvider()
				+", location="+location.getLongitude()+", "+location.getLatitude());
		
		onStatusChangedBroadcast("onLocationChanged : \n	provider="+location.getProvider()
				+", location="+location.getLongitude()+", "+location.getLatitude());
		
		if(location.getProvider().equals("gps")){
			currentLocation=location;
			gpsSignalTime= Calendar.getInstance().getTimeInMillis();
		}else if(location.getProvider().equals("network")){
			if(Calendar.getInstance().getTimeInMillis() < gpsSignalTime+gpsEnabledTime){
				return;
			}
			currentLocation=location;
		}else{
			return;
		}

		onLocationChangedBroadcast(location);
		
		long start=System.currentTimeMillis();
		Station st=stationHandler.getNearestStation(location.getLongitude(),location.getLatitude());
		Log.d("AccessSupporter",
				st.getStationName()+" "+"E"+st.getLongitude()+",N"+st.getLatitude()
				+"\ntime : "+(System.currentTimeMillis()-start)+"ms");
		if(currentStation!=null && currentStation.equals(st)){
			return;
		}
		//最寄り駅が変化したとき、ここより下の処理に進む
		onNearestStationChangedBroadcast(st);
		
		String vibration=prefs.getString("vibration","when_needed");
		boolean shouldVibrate = vibration.equals("true") || (vibration.equals("when_needed") && !ekimemoIsForeground());

		android.app.Notification notification = createNotification(
				"AccessSupporter",
				"最寄り駅 : "+st.getStationName()+" ("+location.getProvider()+")",
				PendingIntent.getActivity(this,1,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE),
				shouldVibrate
		);
		
		notificationManager.notify(1, notification);
		
		currentStation=st;

		
		//5分毎処理
		setIntervals();
	}

	private void createNotificationChannels() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			// サイレントチャンネル
			String channelIdSilent = "channel_silent";
			String channelNameSilent = "AccessSupporter (Silent)";
			android.app.NotificationChannel channelSilent = new android.app.NotificationChannel(channelIdSilent, channelNameSilent, NotificationManager.IMPORTANCE_LOW);
			channelSilent.setSound(null, null);
			channelSilent.enableVibration(false);
			notificationManager.createNotificationChannel(channelSilent);

			// バイブレーションチャンネル
			String channelIdVibrate = "channel_vibrate";
			String channelNameVibrate = "AccessSupporter (Vibrate)";
			android.app.NotificationChannel channelVibrate = new android.app.NotificationChannel(channelIdVibrate, channelNameVibrate, NotificationManager.IMPORTANCE_DEFAULT);
			channelVibrate.setSound(null, null);
			channelVibrate.enableVibration(true);
			channelVibrate.setVibrationPattern(new long[]{0, 400, 100, 400}); // バイブレーションパターン設定
			notificationManager.createNotificationChannel(channelVibrate);
		}
	}

	private android.app.Notification createNotification(String title, String text, PendingIntent intent, boolean vibrate) {
		android.app.Notification notification;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			String channelId = vibrate ? "channel_vibrate" : "channel_silent";
			android.app.Notification.Builder builder = new android.app.Notification.Builder(getApplicationContext(), channelId);
			builder.setSmallIcon(R.mipmap.eki_1)
					.setLargeIcon(largeIcon)
					.setContentTitle(title)
					.setContentIntent(intent);
			if (text != null) {
				builder.setContentText(text);
			}
			notification = builder.build();
		} else {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
			builder.setSmallIcon(R.mipmap.eki_1)
					.setLargeIcon(largeIcon)
					.setContentTitle(title)
					.setContentIntent(intent);
			if (text != null) {
				builder.setContentText(text);
			}
			if (vibrate) {
				builder.setVibrate(new long[]{0, 400, 100, 400});
			} else {
				// 初期通知などで少しだけ振動させたい場合はここを調整するが、
				// 基本的にfalseなら振動なしとする
				// existing logic had `setVibrate(new long[]{0,100})` for initial notification.
				// If strictly following `vibrate` flag:
				builder.setVibrate(new long[]{0});
			}
			notification = builder.build();
		}
		return notification;
	}
	

	
	private void setIntervals(){//(設定されていれば)5分毎に処理
		if(!prefs.getBoolean("five_min_access",false))
			return;
		
		if(intervalsThread!=null){
			intervalsThread.interrupt();
		}
		
		final boolean random=prefs.getBoolean("intervals_random",false);
		
		intervalsThread=new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					if(random){
						Thread.sleep(305000+(long)(Math.random()*55000));
					}else{
						Thread.sleep(305000);
					}
					if(prefs.getBoolean("five_min_access",false)){
						String vibration=prefs.getString("vibration","when_needed");
						boolean shouldVibrate = vibration.equals("true") || (vibration.equals("when_needed") && !ekimemoIsForeground());

						if(currentStation!=null && currentLocation!=null){
							android.app.Notification notification = createNotification(
									"AccessSupporter",
									"最寄り駅 : "+currentStation.getStationName()+" ("+currentLocation.getProvider()+")",
									PendingIntent.getActivity(AccessSupporterService.this,1,new Intent(AccessSupporterService.this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE),
									shouldVibrate
							);
							notificationManager.notify(1, notification);
						}
						//Loop
						setIntervals();
					}
				}catch(InterruptedException e){
					//e.printStackTrace();
				}
			}
		});
		intervalsThread.start();
	}
	
	
	@Override
	public void onProviderDisabled(String provider) {
		//Log.d("AccessSupporter","onProviderDisabled:"+provider);
		onStatusChangedBroadcast("onProviderDisabled:"+provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		//Log.d("AccessSupporter","onProviderEnabled:"+provider);
		onStatusChangedBroadcast("onProviderEnabled:"+provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//Log.d("AccessSupporter","onStatusChanged");
		onStatusChangedBroadcast("onStatusChanged");
	}
	
	
	

	private GnssStatus.Callback gnssStatusCallback = new GnssStatus.Callback() {
		@Override
		public void onStarted() {
			super.onStarted();
			onStatusChangedBroadcast("onGpsStatusChanged : \n\tGPS_EVENT_STARTED");
		}

		@Override
		public void onStopped() {
			super.onStopped();
			onStatusChangedBroadcast("onGpsStatusChanged : \n\tGPS_EVENT_STOPPED");
		}

		@Override
		public void onFirstFix(int ttffMillis) {
			super.onFirstFix(ttffMillis);
			onStatusChangedBroadcast("onGpsStatusChanged : \n\tGPS_EVENT_FIRST_FIX");
		}

		@Override
		public void onSatelliteStatusChanged(GnssStatus status) {
			super.onSatelliteStatusChanged(status);
		}
	};

	boolean ekimemoIsForeground(){
		long start=System.currentTimeMillis()-1000*60*60*24;
		long end=System.currentTimeMillis()+100;
		
		UsageStatsManager stats = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE/*"usagestats"*/);
		UsageEvents usageEvents = stats.queryEvents(start, end);//usegeEventsのうち後ろにあるほど新しいevent
		UsageEvents.Event event=null;
		
		
		boolean isForeground=false;
		while (usageEvents.hasNextEvent()) {
			event = new android.app.usage.UsageEvents.Event();
			usageEvents.getNextEvent(event);

			if(event.getPackageName().equals("jp.mfapps.loc.ekimemo")){
				switch(event.getEventType()){
					case UsageEvents.Event.MOVE_TO_BACKGROUND:
						isForeground=false;
						break;
					case UsageEvents.Event.MOVE_TO_FOREGROUND:
						isForeground=true;
						break;
					default:
						break;
				}
			}
		}
		
		Log.d("AccessSupporter","ekimemoIsForeground()="+isForeground);
		return isForeground;
	}
	
	private void onStatusChangedBroadcast(String message){
		((AccessSupporterApplication)getApplication()).addLogString(message);
		sendBroadcast(new Intent(STATUS_CHANGED).putExtra(STATUS_CHANGED,message).setPackage(getPackageName()));
	}
	
	private void onLocationChangedBroadcast(Location loc){
		((AccessSupporterApplication)getApplication()).setCurrentLocation(loc);
		sendBroadcast(new Intent(LOCATION_CANGED).setPackage(getPackageName()));
	}
	private void onNearestStationChangedBroadcast(Station sta){
		((AccessSupporterApplication)getApplication()).addStationHistory(sta);
		sendBroadcast(new Intent(NEAREST_STATION_CHANGED).setPackage(getPackageName()));
	}



	
	
	
}


