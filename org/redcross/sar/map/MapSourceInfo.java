package org.redcross.sar.map;

public class MapSourceInfo {
	private boolean primarMap;
	private boolean secondaryMap;
	private String mxdPath;
	private String status;
	private String type;
	private int no_Attributes = 5;
	
	public MapSourceInfo(boolean primar, boolean secondary, String mxdPath, String status, String type){
		this.primarMap = primar;
		this.secondaryMap = secondary;
		this.mxdPath = mxdPath;
		this.status = status;
		this.type = type;
		
	}
	
	public MapSourceInfo(){
		
	}
	
	public int getNoAttribute(){
		return no_Attributes;
	}
	
	public void setPrimarMap(boolean primar){
		this.primarMap = primar;
	}	
	public boolean getPrimarMap(){
		return this.primarMap;
	}
	
	public void setSecondaryMap(boolean secondaryMap){
		this.secondaryMap = secondaryMap;
	}
	public boolean getSecondaryMap(){
		return this.secondaryMap;
	}
	
	public void setMxdPath(String path){
		this.mxdPath = path;
	}
	public String getMxdPath(){
		return this.mxdPath;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	public String getStatus(){
		return this.status;
	}
		
	public void setType(String type){
		this.type = type;
	}
	public String getType(){
		return this.type;
	}
}
