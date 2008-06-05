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
		boolean gChanged = (route.getGeodata() != null && !route.getGeodata().equals(getGeodata())) ||
        			getAssignmentStatus(getOwningArea(route)) != asgStatus;
		IAreaIf area = getOwningArea(route);
		IAssignmentIf assignment = area.getOwningAssignment();
		boolean cChanged = !caption.equals(MsoUtils.getAssignmentName(assignment,2));
		isDirty = gChanged || cChanged;
		return gChanged || cChanged;
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
		if (geodata instanceof Route)
			geometry = MapUtil.getEsriPolyline((Route)geodata, srs);
		else 
			geometry = null;
        asgStatus = getAssignmentStatus(getOwningArea(route));
		IAreaIf area = getOwningArea(route);
		
		/* TODO: Correct serious error in MSO model
		 * 
		 * In some case a route objects arrives with no parent assignment. When this
		 * happens, it is a model inconsistancy which should not happen. The direct cause 
		 * is not known, although it usually only happens when after a while when the
		 * log is large. Several scenarios may apply
		 * 
		 * 1. The drawing tool falsely allows creation of route without assignment
		 * 2. The MSO model has an error
		 * 3. The Sara model drive has an error
		 * 
		 * In addition to this, a potential linked error exists in the errouneous log
		 * file, which has a double occurrence of this false object in it (is found by
		 * inspection of log file using the MsoObject id.
		 * 
		 * 
		 */
		
		
		// TODO: THIS IS AN HACK AND SHOULD BE RESOLVED! Route objects MUST have a assignment
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			caption = MsoUtils.getAssignmentName(assignment,2);   
		}
		else if(route!=null)
			System.out.println("IRouteIf:="+route.getObjectId()+ " is dangling");
		else
			System.out.println("Empty IRouteIf found and discarded by RouteFeature");
		super.msoGeometryChanged();
	}
	
	public Object getGeodata() {
		return geodata;
	}

}
