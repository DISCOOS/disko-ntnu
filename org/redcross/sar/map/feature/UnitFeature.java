package org.redcross.sar.map.feature;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.IGeodataIf;
import org.redcross.sar.util.mso.Position;

import java.io.IOException;

public class UnitFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private IGeodataIf geodata = null;

	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		IUnitIf unit = (IUnitIf)msoObject;
		boolean gChanged = (unit.getPosition() != null && !unit.getPosition().equals(getPosition()));;
		boolean cChanged = !caption.equals(MsoUtils.getUnitName(unit,true));
		isDirty = gChanged || cChanged;
		return gChanged || cChanged;
	}

    @Override
    public void msoGeometryChanged() throws IOException, AutomationException {       // todo sjekk etter endring av GeoCollection
		if (srs == null || msoObject == null) return;
		IUnitIf unit = (IUnitIf)msoObject;
		geodata = unit.getPosition();
		if (geodata instanceof Position)
			geometry = MapUtil.getEsriPoint((Position)geodata, srs);
		else
			geometry = null;
		caption = MsoUtils.getUnitName(unit,true);
		super.msoGeometryChanged();		
	}
	
	public Object getGeodata() {
		return geodata;
	}

	public Position getPosition() {
		return (Position)geodata;
	}
}
