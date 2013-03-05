package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;

public class MapSourceInfo {

	private int coverage;
	
	private boolean isCurrent;
	
	private String mxdDoc;
	private String status;
	private String type;
	
	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;

	private final List<String> bases = new ArrayList<String>();
	
	public MapSourceInfo(){}
	
	public void setMxdDoc(String mxddoc){
		this.mxdDoc = mxddoc;
	}	
	
	public String getMxdDoc(){
		return this.mxdDoc;
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
	
	public int getMapBaseCount() {
		return bases.size();
	}
	
	public String getMapBase(int index) {
		if(index>0 && index<=bases.size())
			return bases.get(index-1);
		return null;
	}
	
	public boolean addMapBase(String name) {
		if(!bases.contains(name)) {
			return bases.add(name);
		}
		return false;
	}

	public boolean removeMapBase(String name) {
		if(bases.contains(name)) {
			return bases.remove(name);
		}
		return false;
	}
	
	public double getXMin() {
		return xMin;
	}
	
	public void setXMin(double min) {
		this.xMin = min;
	}
	
	public double getXMax() {
		return xMax;
	}
	
	public void setXMax(double max) {
		this.xMax = max;
	}

	public double getYMin() {
		return yMin;
	}
	
	public void setYMin(double min) {
		this.yMin = min;
	}
	
	public double getYMax() {
		return yMax;
	}
	
	public void setYMax(double max) {
		this.yMax = max;
	}

	public int getCoverage() {
		return coverage;
	}
	
	public void setCoverage(int coverage) {
		this.coverage = coverage;
	}
	
	public IEnvelope getExtent(ISpatialReference srs) {
		try {
			IEnvelope extent = MapUtil.createEnvelope();
			extent.setSpatialReferenceByRef(srs);
			IPoint p = MapUtil.getEsriPoint(xMin, yMax, srs);
			extent.setXMin(p.getX());
			extent.setYMax(p.getY());
			p = MapUtil.getEsriPoint(xMax, yMin, srs);
			extent.setXMax(p.getX());
			extent.setYMin(p.getY());
			return extent;
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
