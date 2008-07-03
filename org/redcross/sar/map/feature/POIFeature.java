package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.interop.AutomationException;

public class POIFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private Position pos = null;
	private int changeCount;
	
	public boolean isMsoChanged(IMsoObjectIf msoObj) {
		IPOIIf poi = (IPOIIf)msoObj;
		boolean gChanged = isGeodataChanged(poi != null ? poi.getPosition() : null);
		boolean cChanged = !caption.equals(MsoUtils.getPOIName(poi, true, true));
		isDirty = gChanged || cChanged;
		return gChanged || cChanged;
	}

	private boolean isGeodataChanged(Position p) {
		return (   pos==null 
				|| p==null 
				|| !pos.equals(p)
				|| changeCount!=p.getChangeCount());
	}
	
	@Override
	public void msoChanged() throws IOException, AutomationException {
		if (srs == null) return;
		IPOIIf poi = (IPOIIf)msoObject;
		pos = poi.getPosition();
		geometry = null;
		if (pos != null) {
			geometry = MapUtil.getEsriPoint(pos, srs);
			changeCount=pos.getChangeCount();
		}
		else {
			geometry = null;
			changeCount=0;
		}
		caption = MsoUtils.getPOIName(poi, true, true);
		super.msoChanged();
	}
	
	public int getGeodataCount() {
		return pos != null ? 1: 0;
	}
	
	public Object getGeodata() {
		return pos;
	}
}
