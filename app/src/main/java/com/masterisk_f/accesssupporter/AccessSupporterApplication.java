package com.masterisk_f.accesssupporter;

import android.app.Application;
import android.location.Location;

import com.masterisk_f.accesssupporter.StationData.Station;
import com.masterisk_f.accesssupporter.StationData.StationHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FT on 2017/08/02.
 */

public class AccessSupporterApplication extends Application {
	
	private StationHandler stationHandler;
	private AccessSupporterService service;
	
	private Location currentLocation;
	
	private List<Station> stationsHistory=new ArrayList<Station>();
	
	private String logString="";
	
	public void setStationHandler(StationHandler handler){
		stationHandler=handler;
	}
	public StationHandler getStationHandler(){
		return stationHandler;
	}
	
	public void setAccessSupporterService(AccessSupporterService acs){
		service=acs;
	}
	public AccessSupporterService getAccessSupporterService(){
		return service;
	}
	
	public void setCurrentLocation(Location loc){
		currentLocation=loc;
	}
	public Location getCurrentLocation(){
		return currentLocation;
	}
	
	public void addStationHistory(Station sta){
		if(stationsHistory.size()==0 || stationsHistory.get(0)!=sta){
			stationsHistory.add(0,sta);
		}
	}
	public List<Station> getStationsHistory(){
		return stationsHistory;
	}
	
	public void addLogString(String str){
		logString=str+"\n"+logString;
		logString=logString.substring(0,Math.min(logString.length(),4000));
	}
	public void clearLogString(){
		logString="";
	}
	public String getLogString(){
		return logString;
	}
}
