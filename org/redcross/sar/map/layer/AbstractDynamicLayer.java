package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.map.feature.IDynamicFeature;

import com.esri.arcgis.carto.CompositeGraphicsLayer;
import com.esri.arcgis.interop.AutomationException;

public abstract class AbstractDynamicLayer extends CompositeGraphicsLayer implements IDynamicLayer {

	protected List<IDynamicFeature> features = null;

	public AbstractDynamicLayer(String name) throws UnknownHostException, IOException {

		// forward
		super();
		
		// prepare
		features = new ArrayList<IDynamicFeature>();
		
		// forward
		setName(name);

	}
	
	public void addFeature(IDynamicFeature feature) {
		features.add(feature);
		try {
			addElement(feature, 0);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removeFeature(IDynamicFeature feature) {
		features.remove(feature);
		try {
			deleteElement(feature);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}