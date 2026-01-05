package com.masterisk_f.accesssupporter;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.masterisk_f.accesssupporter.Setting.SettingActivity;
import com.masterisk_f.accesssupporter.Tabs.HistoryOfStationsFragment;
import com.masterisk_f.accesssupporter.Tabs.NearbyStationsFragment;

import java.util.List;


public class MainActivity extends AppCompatActivity 
		implements View.OnClickListener{
	ToggleButton notification;
	Button launchEkimemo;
	Button displayMap;
	
	/*TextView logTextView;
	BroadcastReceiver eventReceiver;
	IntentFilter eventFilter;
	*/

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		
		setContentView(R.layout.activity_main);

		
		setTabAndPager();
		
		
		notification=(ToggleButton)findViewById(R.id.notification);

		//判定条件これで良い？？
		notification.setChecked(serviceRunning());

		notification.setOnClickListener(this);
		
		launchEkimemo=(Button)findViewById(R.id.launchEkimemo);
		launchEkimemo.setOnClickListener(this);
		
		displayMap=(Button)findViewById(R.id.displayMap);
		displayMap.setOnClickListener(this);
		
		Intent in=new Intent(MainActivity.this,AccessSupporterService.class);
		startService(in.setAction("activityCreated"));
		
	}
	
	
	void setTabAndPager(){
		final String[] title={"nearby stations","history"};
		
		TabLayout tabLayout=(TabLayout)findViewById(R.id.tabs);
		for(int i=0;i<title.length;i++){
			tabLayout.addTab(tabLayout.newTab());
		}
		
		ViewPager viewPager=(ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int position) {
				Fragment fragment=null;
				switch(position){
					case 0:
						fragment= NearbyStationsFragment.newInstance(null,null);
						break;
					case 1:
						fragment= HistoryOfStationsFragment.newInstance(null,null);
						break;
				}
				return fragment;
			}
			
			@Override
			public int getCount() {
				return title.length;
			}
			
			@Override
			public CharSequence getPageTitle(int position) {
				return title[position];
			}
		});
		
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				
			}
			
			@Override
			public void onPageSelected(int position) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		});
		tabLayout.setupWithViewPager(viewPager);
	}
	
	
	@Override
	protected void onDestroy() {
		Intent in=new Intent(MainActivity.this,AccessSupporterService.class);
		startService(in.setAction("activityDestroyed"));
		//unregisterReceiver(eventReceiver);
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.notification:
				//logTextView.setText("");
				Intent in=new Intent(MainActivity.this,AccessSupporterService.class);

				if(notification.isChecked()){
					startService(in.setAction("start"));
				}else{
					startService(in.setAction("stop"));
				}
				break;
			case R.id.launchEkimemo:
				try{
					PackageManager pm = getPackageManager();
					Intent intent = pm.getLaunchIntentForPackage("jp.mfapps.loc.ekimemo");
					startActivity(intent);
				}catch(Exception e){
					Toast.makeText(this,"駅メモがインストールされていません",Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.displayMap:
				startActivity(new Intent(MainActivity.this,MapsActivity.class));
				break;
		}
		Log.d("AccessSupporter","onclick");
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.setting:
				startActivity(new Intent(this, SettingActivity.class));
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	boolean serviceRunning(){//AccessSupporterServiceが起動中か判定
		ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
		List<RunningServiceInfo> listServiceInfo = am.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo curr : listServiceInfo) {
			// クラス名を比較
			if (curr.service.getClassName().equals(AccessSupporterService.class.getName())) {
				// サービスが実行中
				
				return ((AccessSupporterApplication)getApplication()).getAccessSupporterService().isRunning();
			}
		}
		return false;
	}
}
