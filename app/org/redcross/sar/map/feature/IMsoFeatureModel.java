package org.redcross.sar.map.feature;


import org.redcross.sar.mso.data.IMsoObjectIf;

import com.esri.arcgis.geodatabase.IFeatureClass;

public interface IMsoFeatureModel extends IFeatureClass {

	public IMsoFeature getFeature(String id);
	public IMsoFeature getFeature(IMsoObjectIf msoObj);

	public boolean addFeature(IMsoFeature feature);
	public boolean removeFeature(IMsoFeature feature);
	public boolean removeAll();



}
