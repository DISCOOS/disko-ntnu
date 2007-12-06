package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.esriGeometryDimension;
import com.esri.arcgis.interop.AutomationException;

public class OperationAreaMaskFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	
	private IEnvelope extent = null;
	private org.redcross.sar.util.mso.Polygon polygon = null;
	
	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		IOperationAreaIf opArea = (IOperationAreaIf)msoObj;
		return opArea.getGeodata() != null && !opArea.getGeodata().equals(getGeodata());
	}

	public void msoGeometryChanged() throws IOException, AutomationException {
		IOperationAreaIf opArea = (IOperationAreaIf)msoObject;
		polygon = opArea.getGeodata();
		if (polygon != null) {
			Polygon poly = MapUtil.getEsriPolygon(polygon, srs);
			extent = poly.getEnvelope().getEnvelope(); // a copy
			IEnvelope env = extent.getEnvelope();	// another copy
			if(!env.isEmpty()) {
				env.expand(50, 50, true);			
				Polygon outerPoly = new Polygon();
				outerPoly.addPoint(env.getLowerLeft(), null, null);
				outerPoly.addPoint(env.getLowerRight(), null, null);
				outerPoly.addPoint(env.getUpperRight(), null, null);
				outerPoly.addPoint(env.getUpperLeft(), null, null);
				outerPoly.addPoint(env.getLowerLeft(), null, null);
				geometry = outerPoly.difference(poly);
			}
			else 
				geometry = null;
				
		}
		else {
			geometry = null;
		}
	}
	
	public Object getGeodata() {
		return polygon;
	}

	@Override
	public void setSelected(boolean selected) {
		// not selectable!
	}
	
	@Override
	public IEnvelope getExtent() throws IOException, AutomationException {
		if (extent != null) {
			return extent.getEnvelope();
		}
		return null;
	}
	
}
