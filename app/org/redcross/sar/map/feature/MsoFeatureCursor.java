package org.redcross.sar.map.feature;

import java.io.IOException;
import java.util.ArrayList;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureBuffer;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.interop.AutomationException;

public class MsoFeatureCursor implements IFeatureCursor {

	private static final long serialVersionUID = 1L;
	private ArrayList<IFeature> data = null;
	private int index = 0;
	
	public MsoFeatureCursor() {
		data = new ArrayList<IFeature>();
	}
	
	public void add(IFeature feature) {
		data.add(feature);
	}

	public void deleteFeature() throws IOException, AutomationException {
		// TODO Auto-generated method stub
	}

	public int findField(String arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void flush() throws IOException, AutomationException {
		// TODO Auto-generated method stub
	}

	public IFields getFields() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object insertFeature(IFeatureBuffer arg0) throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IFeature nextFeature() throws IOException, AutomationException {
		if (index >= data.size()) {
			index = 0;
			return null;
		}
		return (IFeature)data.get(index++);
	}

	public void updateFeature(IFeature arg0) throws IOException,
			AutomationException {
		// TODO Auto-generated method stub

	}
}
