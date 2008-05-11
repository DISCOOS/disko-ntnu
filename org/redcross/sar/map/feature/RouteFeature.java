package org.redcross.sar.map.feature;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.IGeodataIf;
import org.redcross.sar.util.mso.Polygon;
import org.redcross.sar.util.mso.Route;

import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class RouteFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private IGeodataIf geodata = null;
    private IMsoModelIf msoModel = null;
    private IAreaIf msoArea = null;
    private AssignmentStatus asgStatus = AssignmentStatus.EMPTY;

	public RouteFeature(IMsoModelIf msoModel) {
		this.msoModel = msoModel;
	}

	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		IRouteIf route = (IRouteIf)msoObject;
        return (route.getGeodata() != null && !route.getGeodata().equals(getGeodata())) ||
        getAssignmentStatus(getOwningArea(route)) != asgStatus ;
	}

	private AssignmentStatus getAssignmentStatus(IAreaIf anArea)
    {
        // no area found?
    	if(anArea == null) return AssignmentStatus.EMPTY;
    	IAssignmentIf asg = anArea.getOwningAssignment();
    	// no assignment allocated?
        if (asg == null) return AssignmentStatus.EMPTY;
        // return assignment status
        return asg.getStatus();
    }

    private IAreaIf getOwningArea(IRouteIf anRoute)
    {
    	// find?
    	if(msoArea==null) {
    		msoArea = MsoUtils.getOwningArea(anRoute);
    	}
    	// return owning area
        return msoArea;
    }
    
    public IAreaIf getOwningArea()
    {
    	return getOwningArea((IRouteIf)msoObject);
    }
    
    @Override
    public void msoGeometryChanged() throws IOException, AutomationException {       // todo sjekk etter endring av GeoCollection
		if (srs == null || msoObject == null) return;
		IRouteIf route = (IRouteIf)msoObject;
		geodata = route.getGeodata();
		if (geodata instanceof Route) {
			geometry = MapUtil.getEsriPolyline((Route)geodata, srs);
		}
		else {
			geometry = null;
		}
        asgStatus = getAssignmentStatus(getOwningArea(route));
		super.msoGeometryChanged();
	}
	
	public Object getGeodata() {
		return geodata;
	}

}
