package org.redcross.sar.map;

public class MapSourceInfo {
	private String mxdDoc;
	private String mxdPath;
	private String status;
	private String type;
	private boolean isCurrent;
	private int no_Attributes = 5;
	
	public MapSourceInfo(){}
	
	public int getNoAttribute(){
		return no_Attributes;
	}
	
	public void setMxdDoc(String mxddoc){
		this.mxdDoc = mxddoc;
	}	
	public String getMxdDoc(){
		return this.mxdDoc;
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
	
	public void setCurrent(boolean isCurrent){
		this.isCurrent = isCurrent;
	}
	
	public boolean isCurrent(){
		return this.isCurrent;
	}
	
}
