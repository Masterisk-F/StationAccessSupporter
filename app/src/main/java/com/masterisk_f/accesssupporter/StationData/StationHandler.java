package com.masterisk_f.accesssupporter.StationData;


import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import static java.util.Collections.binarySearch;

public class StationHandler {
	
	//List<Station> stationList;
	
	private StationNode root;//kd木の根
	
	private List<Station> stationList;//複数の近傍駅を調べるため残しておく
	
	
	private Comparator<Station> compareByLon=new Comparator<Station>() {
		@Override
		public int compare(Station o1, Station o2) {
			return Double.compare(o1.getLongitude(),o2.getLongitude());
		}
	};
	private Comparator<Station> compareByLat=new Comparator<Station>() {
		@Override
		public int compare(Station o1, Station o2) {
			return Double.compare(o1.getLatitude(),o2.getLatitude());
		}
	};
	private Comparator<Station> compareById=new Comparator<Station>() {
		@Override
		public int compare(Station o1, Station o2) {
			int a=Integer.compare(o1.getStationGroupCode(),o2.getStationGroupCode());
			if(a!=0){
				return a;
			}else{
				return o1.getStationName().compareTo(o2.getStationName());
			}
		}
	};
	
	private class StationNode{//kd木を用いたデータ構造
		Station station;
		
		StationNode parent;
		
		StationNode bigger;
		StationNode smaller;
		
		int depth;//使うかどうかわからないけど
	}


	//Seo-4d696b75 - station_databaseを使う
	public StationHandler(Reader station, Reader line, Reader register){
		long start=System.currentTimeMillis();

		Map<Integer,Line> lineMap=createLineMap(line);
		stationList=createStationList(station,lineMap);
		register(stationList, lineMap, register);
		Log.d("AccessSupporter","File reading time : "+(System.currentTimeMillis()-start)+"ms");
		Log.d("AccessSupporter","Number of station :: "+stationList.size());
		start=System.currentTimeMillis();
		root=createStationTree(null,stationList,0);
		Log.d("AccessSupporter","tree creation time : "+(System.currentTimeMillis()-start)+"ms");
	}

		private void register(Collection<Station> stations, Map<Integer, Line> lineMap, Reader registerReader){
		Map<Integer, Collection<Line>> registerMap = createRegisterMap(registerReader, lineMap);

		for(Station sta : stations){
			Collection<Line> lines = registerMap.get(sta.getStationCodes()[0]);
			if (lines != null) {
				sta.addAllLine(lines);
			}
		}
	}

	private Map<Integer, Collection<Line>> createRegisterMap(Reader register, Map<Integer, Line> lineMap){
		//<駅コード,路線>のマップを返す
		BufferedReader reader=new BufferedReader(register);

		HashMap<Integer, Collection<Line>> returnMap = new HashMap<Integer, Collection<Line>>();
		String[] legend=null;//凡例
		try{
			legend=reader.readLine().split(",");
		}catch(IOException e){
			e.printStackTrace();
		}
		if(legend==null || legend.length==0)
			return null;

		int station_code_index=getIndex(legend,"station_code");
		int line_code_index=getIndex(legend,"line_code");

		try {
			String str;
			while ((str = reader.readLine()) != null) {
				if (str.length() == 0 || str.startsWith("#")) {
					continue;
				}
				String[] data = str.split(",");
				int station_code = Integer.parseInt(data[station_code_index]);
				int line_code = Integer.parseInt(data[line_code_index]);
				Collection<Line> lines = returnMap.get(station_code);
				if(lines == null){
					lines = new HashSet<Line>();
				}
				lines.add(lineMap.get(line_code));
				returnMap.put(station_code, lines);
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if(reader!=null)
					reader.close();
			}catch(IOException e){}
		}

		return returnMap;
	}
	private List<Station> createStationList(Reader stationReader,Map<Integer,Line> lineMap){
		return createStationList(stationReader,lineMap,new ArrayList<Station>());
	}
	
	private List<Station> createStationList(Reader stationReader, Map<Integer,Line> lineMap,
											List<Station> returnList){
		//returnListにstationReaderの駅を加える

		//!!!!!compareByIdに従ってreturnListに挿入していく
		
		BufferedReader reader=new BufferedReader(stationReader);
		
		String[] legend=null;//凡例
		try{
			legend=reader.readLine().split(",");
		}catch(IOException e){
			e.printStackTrace();
		}
		if(legend==null || legend.length==0)
			return null;
		
		
		int station_cd_index=getIndex(legend,"code");
		//int station_g_cd_index=getIndex(legend,"station_g_cd");
		int station_name_index=getIndex(legend,"name");
		//int line_cd_index=getIndex(legend,"line_cd");	//TODO:路線との対応付
		int address_index=getIndex(legend,"address");
		int lon_index=getIndex(legend,"lng");
		int lat_index=getIndex(legend,"lat");
		int e_status_index=getIndex(legend,"e_status");
		
		try{
			String str;
			while((str=reader.readLine())!=null){
				if(str.length()==0|| str.startsWith("#")){
					continue;
				}
				
				String[] data=str.split(",");

				Station st=new Station();
				st.addStationCode(Integer.parseInt(data[station_cd_index]));
				//st.setStationGroupCode(Integer.parseInt(data[station_g_cd_index]));
				st.setStationName(data[station_name_index]);
				//st.addLine(lineMap.get(Integer.parseInt(data[line_cd_index])));	//TODO:路線との対応付
				//st.setAddress(data[address_index]);
				st.setLongitude(Double.parseDouble(data[lon_index]));
				st.setLatitude(Double.parseDouble(data[lat_index]));

				//TODO:同じ駅コードのレコードが複数現れないならソート不要では？
				int i=Collections.binarySearch(returnList,st,compareById);
				//iが負の数のときその値は(-(挿入ポイント) - 1)
				//参考: http://qiita.com/taskie/items/b4e45e2005aa38e90bcb
				if(i>=0){
					//同じ駅がすでにある場合（基本ありえない）
					Log.d("ACTest","index is positive : "+i+"\n"
							+st.getStationName()+" :: "+returnList.get(i).getLineNames()+" : "+st.getLineNames()+"\n"
							+returnList.get(i).getStationGroupCode()+" :: "+st.getStationCodes()[0]+" "+st.getStationGroupCode()+"\n"
							+returnList.get(i).getLatitude()+","+returnList.get(i).getLongitude()
								+" :: "+st.getLatitude()+","+st.getLongitude()+"\n"
					);
				}else{
					returnList.add(-i-1,st);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if(reader!=null)
					reader.close();
			}catch(IOException e){}
		}
		
		return returnList;
	} 
	
	private StationNode createStationTree(StationNode parent,List<Station> list,final int depth){
		//StationNodeを用いたkd木の構築
		if(list.size()==0)
			return null;
		
		if(depth%2==0){
			Collections.sort(list,compareByLon);
		}else{
			Collections.sort(list,compareByLat);
		}
		//subListは内部の配列をそのまま渡すから気をつけて！
		final List<Station> smaller=list.subList(0,list.size()/2);
		
		final StationNode node=new StationNode();
		node.station=list.get(list.size()/2);
		node.depth=depth;
		node.parent=parent;
		
		final List<Station> bigger=list.subList(list.size()/2+1,list.size());
		
		if(bigger.size()>2000){
			Thread t1=new Thread(new Runnable() {
				@Override
				public void run() {
					node.bigger=createStationTree(node,bigger,depth+1);
				}
			});
			
			t1.start();
			node.smaller=createStationTree(node,smaller,depth+1);
			try{
				t1.join();
			}catch(InterruptedException e){}
		}else{
			node.bigger=createStationTree(node,bigger,depth+1);
			node.smaller=createStationTree(node,smaller,depth+1);
		}
		return node;
	}
	
	public Station getNearestStation(double longitude,double latitude){
		//kd木を用いた最近傍駅の探索
		
		return getNearestStation(root,null,longitude,latitude).station;
	}
	private StationNode getNearestStation(StationNode node,StationNode parent,double longitude,double latitude){
		//nodeはnullでないとする
		//nodeをrootとする木構造で、最近傍のノードを返す
		//parent : nodeがnullだったとき、parentをそのまま帰す
		
		if(node==null)
			return parent;
		
		if(node.bigger==null && node.smaller==null){
			return node;
		}
		
		StationNode nearestNode=getField(null,node,longitude,latitude);
		//近似最近傍のノードが子を持っている場合、そっちをまず探索
		if(nearestNode.bigger!=null){
			StationNode tmp=getNearestStation(nearestNode.bigger,nearestNode,longitude,latitude);
			if(nearestNode.station.distanceTo(longitude,latitude)>tmp.station.distanceTo(longitude,latitude)){
				nearestNode=tmp;
			}
		}else if(nearestNode.smaller!=null){
			StationNode tmp=getNearestStation(nearestNode.smaller,nearestNode,longitude,latitude);
			if(nearestNode.station.distanceTo(longitude,latitude)>tmp.station.distanceTo(longitude,latitude)){
				nearestNode=tmp;
			}
		}
		
		return searchAbove(nearestNode,nearestNode,
				nearestNode.station.distanceTo(longitude, latitude),
				node,longitude,latitude);
	}
	
	//テスト用
	public Station getField(double longitude,double latitude){
		return getField(root,root,longitude,latitude).station;
	}
	private StationNode getField(StationNode parent,StationNode tree,double longitude,double latitude){
		//kd木で、引数の座標を含む領域を表す葉ノードを返す
		//木構造の上から下への探索
		//tree : いま考えるノード, parent : treeの親ノード
		if(tree==null){
			return parent;
		}
		
		StationNode node;
		if(tree.depth%2==0){
			if(longitude>=tree.station.getLongitude()){
				node=getField(tree,tree.bigger,longitude,latitude);
			}else{
				node=getField(tree,tree.smaller,longitude,latitude);
			}
		}else{
			if(latitude>=tree.station.getLatitude()){
				node=getField(tree,tree.bigger,longitude,latitude);
			}else{
				node=getField(tree,tree.smaller,longitude,latitude);
			}
		}
		return node;
	}
	
	private StationNode searchAbove(StationNode node,StationNode nearest,double distance,StationNode root,double longitude,double latitude){
		//木構造を上方向に辿って最近傍の点を見つける
		//node : いま考えるノード、nearest : 現時点での最近傍、distance : 最近傍との距離,root : 再帰をやめるノード
		
		double d;
		if((d=node.station.distanceTo(longitude, latitude))<distance){
			nearest=node;
			distance=d;
		}
		
		if(node==root){
			return nearest;
		}
		
		StationNode tmp=null;
		if(node.parent.depth%2==0){
			double deltaLongitude=toLongitude(distance,latitude);
			if(longitude-deltaLongitude<=node.parent.station.getLongitude()
					&& node.parent.station.getLongitude()<=longitude+deltaLongitude){
				//parentの反対側の枝も調べる
				if(node.parent.bigger==node){
					tmp=getNearestStation(node.parent.smaller,node,longitude,latitude);
				}else{
					tmp=getNearestStation(node.parent.bigger,node,longitude,latitude);
				}
			}
		}else{
			double deltaLatitude=toLatitude(distance);
			if(latitude-deltaLatitude<=node.parent.station.getLatitude()
					&& node.parent.station.getLatitude()<=latitude+deltaLatitude){
				if(node.parent.bigger==node){
					tmp=getNearestStation(node.parent.smaller,node,longitude,latitude);
				}else{
					tmp=getNearestStation(node.parent.bigger,node,longitude,latitude);
				}
			}
		}
		if(tmp!=null && tmp.station.distanceTo(longitude, latitude)<distance){
			nearest=tmp;
			distance=tmp.station.distanceTo(longitude, latitude);
		}
		
		return searchAbove(node.parent,nearest,distance,root,longitude,latitude);
	}
	
	private double toLongitude(double distance,double latitude){
		//緯度latitudeで、緯度線に沿ってdistanceだけ移動したときの経度の差
		double r=6371000;//地球の平均半径[m]
		double rad=2*Math.PI/360;
		return distance/(2*Math.PI*r)*360;
	}
	private double toLatitude(double distance){
		//distanceだけ経度線に沿って移動したときの緯度の差を出す
		double r=6371000;//地球の平均半径[m]
		return distance/(2*Math.PI*r)*360;
	}
	
	
	//listで最寄り駅を求める。リニアサーチ
	//NearbyStationsFragmentから呼ばれる
	public List<Station>  getNearbyStationsList(Location loc){
		return getNearbyStationsList(loc,12);
	}
	public List<Station> getNearbyStationsList(final Location loc, int num) {
		List<Station> list = new ArrayList<Station>(stationList);
		Collections.sort(list, new Comparator<Station>() {
			@Override
			public int compare(Station o1, Station o2) {
				return Double.compare(
						o1.distanceTo(loc.getLongitude(), loc.getLatitude()),
						o2.distanceTo(loc.getLongitude(), loc.getLatitude())
				);
			}
		});
		return new ArrayList<Station>(list.subList(0, Math.min(list.size(), num)));
	}
	
	
	public List<Station> getAllStationsList(){
		return stationList;
	}
	
	private Station getSameStation(List<Station> list, int group_code, String name){
		//listはcompareByIdに従ってソートされているとする
		
		Station tmp=new Station();
		tmp.setStationGroupCode(group_code);
		tmp.setStationName(name);
		int i= binarySearch(list,tmp,compareById);
		if(i<0){
			return null;
		}
		return list.get(i);
	}
	
	
	
	private static int getIndex(String[] list,String key){
		for(int i=0;i<list.length;i++){
			if(list[i].equals(key)){
				return i;
			}
		}
		return -1;
	}
	
	private Map<Integer,Line> createLineMap(Reader inputStreamReader){
		BufferedReader reader=new BufferedReader(inputStreamReader);
		
		String[] legend=null;//凡例
		try{
			legend=reader.readLine().split(",");
		}catch(IOException e){
			e.printStackTrace();
		}
		if(legend==null || legend.length==0)
			return null;
		
		
		int line_cd_index=getIndex(legend,"code");
		int company_cd_index=getIndex(legend,"company_code");
		int line_name_index=getIndex(legend,"name");
		int line_name_KANA_index=getIndex(legend,"name_kana");
		int line_name_FORMAL_index=getIndex(legend,"name_formal");
		//int e_status_index=getIndex(legend,"e_status");
		
		Map<Integer,Line> map=new HashMap<Integer,Line>(650);
		
		String str;
		try {
			while((str=reader.readLine())!=null){
				if(str.length()==0 || str.startsWith("#"))
					continue;
				
				String[] data=str.split(",",-1);
				Line line=new Line();
				Integer key=Integer.valueOf(data[line_cd_index]);
				line.setLineCode(key);
				//line.setCompanyCode(Integer.parseInt(data[company_cd_index]));
				line.setLineName(data[line_name_index]);
				line.setLineNameKana(data[line_name_KANA_index]);
				line.setLineNameFormal(data[line_name_FORMAL_index]);
				//line.setStatus(Integer.parseInt(data[e_status_index]));
				
				map.put(key, line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(reader!=null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return map;
	}
	
}
