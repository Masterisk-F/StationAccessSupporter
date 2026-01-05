package com.masterisk_f.accesssupporter.StationData;


public class Line {
	
	
	private int line_cd;
	private int company_cd;
	private String line_name;
	private String line_name_k;
	private String line_name_h;
	private int e_status;
	
	
	
	public void setLineCode(int cd){
		line_cd=cd;
	}
	public int getLineCode(){
		return line_cd;
	}
	
	public void setCompanyCode(int cd){
		company_cd=cd;
	}
	public int getCompanyCode(){
		return company_cd;
	}
	
	public void setLineName(String name){
		line_name=name;
	}
	public String getLineName(){
		return line_name;
	}
	
	public void setLineNameKana(String name){
		line_name_k=name;
	}
	public String getLineNameKana(){
		return line_name_k;
	}
	
	
	public void setLineNameFormal(String name){
		line_name_h=name;
	}
	public String getLineNameFormal(){
		return line_name_h;
	}
	
	public void setStatus(int s){
		e_status=s;
	}
	public int getStatus(){
		return e_status;
	}
}
