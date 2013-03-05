package org.redcross.sar.map.feature;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Position;

import java.io.IOException;

public class UnitFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private Position geodata = null;
	private int changeCount;

	public boolean isMsoChanged() {
		IUnitIf unit = (IUnitIf)msoObject;
		boolean gChanged = isGeodataChanged(unit.getPosition());
		boolean cChanged = !caption.equals(MsoUtils.getUnitName(unit,true));
		setDirty(isDirty || gChanged || cChanged);
		return gChanged || cChanged;
	}

	private boolean isGeodataChanged(Position p) {
		return (   geodata==null
				|| p==null
				|| !geodata.equals(p)
				|| changeCount!=p.getChangeCount());
	}

    @Override
    public boolean create() throws IOException, AutomationException {
    	if(super.create()) {
			IUnitIf unit = (IUnitIf)msoObject;
			geodata = unit.getPosition();
			if (geodata !=null) {
				geometry = MapUtil.getEsriPoint(((Position)geodata).getGeoPos(), srs);
				changeCount = geodata.getChangeCount();
			}
			else {
				geometry = null;
				changeCount = 0;
			}
			caption = MsoUtils.getUnitName(unit,true);
			setDirty(isDirty || (getShape()!=null));
			return true;
    	}
    	return false;
	}

	public Object getGeodata() {
		return geodata;
	}

	public Position getPosition() {
		return (Position)geodata;
	}
}
