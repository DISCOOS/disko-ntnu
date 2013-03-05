package org.redcross.sar.output;


public class POIDescription {
	
	private String id = null;
	private String name = null;
	private String coord = null;
	private String poiDescription = null;

	public POIDescription(String pid, String pname, String pcoord, String pPoiDescription){
		id = pid;
		name = pname;
		coord = pcoord;
		poiDescription = pPoiDescription;
	}
	
	public POIDescription getMe(){
		return this;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCoord() {
		return coord;
	}

	public String getPoiDescription() {
		return poiDescription;
	}
	
	
}
