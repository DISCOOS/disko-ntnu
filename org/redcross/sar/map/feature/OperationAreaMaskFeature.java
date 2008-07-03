package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.util.mso.Polygon;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.interop.AutomationException;

public class OperationAreaMaskFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	
	private IEnvelope extent;
	private Polygon polygon;
	private int changeCount;
	
	public boolean isMsoChanged(IMsoObjectIf msoObj) {
		IOperationAreaIf opArea = (IOperationAreaIf)msoObj;
		return isGeodataChanged(opArea.getGeodata());
	}
	
	private boolean isGeodataChanged(Polygon p) {
		return (   polygon==null 
				|| p==null 
				|| !polygon.equals(p)
				|| changeCount!=p.getChangeCount());
	}
	
	public void msoChanged() throws IOException, AutomationException {
		IOperationAreaIf opArea = (IOperationAreaIf)msoObject;
		polygon = opArea.getGeodata();
		geometry = null;
		if (polygon != null) {
			IGeometry poly = MapUtil.getEsriPolygon(polygon, srs);
			extent = poly.getEnvelope().getEnvelope(); // a copy
			IEnvelope env = extent.getEnvelope();	// another copy
			if(!env.isEmpty()) {
				env.expand(50, 50, true);			
				com.esri.arcgis.geometry.Polygon outerPoly = 
					new com.esri.arcgis.geometry.Polygon();
				outerPoly.addPoint(env.getLowerLeft(), null, null);
				outerPoly.addPoint(env.getLowerRight(), null, null);
				outerPoly.addPoint(env.getUpperRight(), null, null);
				outerPoly.addPoint(env.getUpperLeft(), null, null);
				outerPoly.addPoint(env.getLowerLeft(), null, null);
				geometry = outerPoly.difference(poly);
			}
			changeCount = polygon.getChangeCount();
		}
		else {
			geometry = null;
			changeCount = 0;
		}
		super.msoChanged();
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
