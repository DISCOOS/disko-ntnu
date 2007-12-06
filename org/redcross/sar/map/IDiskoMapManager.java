package org.redcross.sar.map;

import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.map.layer.IMsoFeatureLayer;

public interface IDiskoMapManager {

	public IDiskoMap getMapInstance(EnumSet<IMsoFeatureLayer.LayerCode> myLayers);

	public List getMaps();
	
	public String getCurrentMxd();
	
	public IDiskoMap getPrintMap();
}