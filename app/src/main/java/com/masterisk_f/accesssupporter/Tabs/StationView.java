package com.masterisk_f.accesssupporter.Tabs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.masterisk_f.accesssupporter.R;
import com.masterisk_f.accesssupporter.StationData.Station;

/**
 * Created by FT on 2017/08/08.
 */

public class StationView extends RelativeLayout {
	View view;
	public StationView(Context context){
		this(context,null);
	}
	public StationView(Context context, AttributeSet attrs){
		this(context,attrs,0);
	}
	public StationView(Context context, AttributeSet attrs, int defStyleAttr){
		this(context,attrs,defStyleAttr,0);
	}
	public StationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		super(context,attrs,defStyleAttr,defStyleRes);
		
		view= LayoutInflater.from(context).inflate(R.layout.view_station,this);
	}
	
	
	StationView setStaton(int i,Station sta,int dist){
		TextView num=(TextView)view.findViewById(R.id.num);
		TextView station=(TextView)view.findViewById(R.id.station);
		TextView lines=(TextView)view.findViewById(R.id.lines);
		TextView distance=(TextView)view.findViewById(R.id.distance);
		
		num.setText(getNumText(i+1));
		station.setText(sta.getStationName());
		lines.setText(sta.getLineNames());
		
		if(dist>=0){
			distance.setText(dist+"m");
		}else{
			distance.setText("");
		}
		
		return this;
	}
	
	private static String getNumText(int i){
		return i<10? "0"+i : ""+i;
	}
	
}
