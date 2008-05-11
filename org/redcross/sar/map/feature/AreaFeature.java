package org.redcross.sar.map.feature;

import com.esri.arcgis.interop.AutomationException;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;

import java.io.IOException;

public class AreaFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
    private IMsoListIf<IMsoObjectIf>  geoList = null;
    private AssignmentStatus asgStatus = AssignmentStatus.DRAFT;
    private IMsoModelIf msoModel = null;

	public AreaFeature(IMsoModelIf msoModel) {
		this.msoModel = msoModel;
	}

	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		IAreaIf area = (IAreaIf)msoObject;
        return (area.getAreaGeodata() != null && !area.getAreaGeodata().equals(getGeodata())) ||
                getAssignmentStatus(area) != asgStatus ;
	}

    AssignmentStatus getAssignmentStatus(IAreaIf anArea)
    {
        IAssignmentIf asg = anArea.getOwningAssignment();
        if (asg == null) return AssignmentStatus.DRAFT;
        return asg.getStatus();
    }

    @Override
    public void msoGeometryChanged() throws IOException, AutomationException {       // todo sjekk etter endring av GeoCollection
		if (srs == null) return;
		IAreaIf area = (IAreaIf)msoObject;
        geoList = area.getAreaGeodata().getClone();
        geometry = MapUtil.getEsriGeometryBag(geoList,MsoClassCode.CLASSCODE_ROUTE, srs);
        asgStatus = getAssignmentStatus(area);
		super.msoGeometryChanged();
	}

	public Object getGeodata() {
		return geoList;
	}

	public int getGeodataCount() {
        return geoList != null ? geoList.size() : 0;

    }

}
