package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.interop.AutomationException;

public class POIFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private Position pos = null;
	private int changeCount;

	public boolean isMsoChanged() {
		IPOIIf poi = (IPOIIf)msoObject;
		boolean gChanged = isGeodataChanged(poi != null ? poi.getPosition() : null);
		boolean cChanged = !caption.equals(MsoUtils.getPOIName(poi, true, true));
		setDirty(isDirty || gChanged || cChanged);
		return gChanged || cChanged;
	}

	private boolean isGeodataChanged(Position p) {
		return (   pos==null
				|| p==null
				|| !pos.equals(p)
				|| changeCount!=p.getChangeCount());
	}

	@Override
	public boolean create() throws IOException, AutomationException {
    	if(super.create()) {
			IPOIIf poi = (IPOIIf)msoObject;
			pos = poi.getPosition();
			geometry = null;
			if (pos != null) {
				geometry = MapUtil.getEsriPoint(pos.getGeoPos(), srs);
				changeCount=pos.getChangeCount();
			}
			else {
				geometry = null;
				changeCount=0;
			}
			caption = MsoUtils.getPOIName(poi, true, true);
			setDirty(isDirty || (getShape()!=null));
			return true;
    	}
    	return false;
	}

	public int getGeodataCount() {
		return pos != null ? 1: 0;
	}

	public Object getGeodata() {
		return pos;
	}
}
