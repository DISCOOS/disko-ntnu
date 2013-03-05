package org.redcross.sar.map.feature;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Route;

import java.io.IOException;
import java.util.Hashtable;

public class RouteFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;

	private Route geodata;
    private IAreaIf msoArea;
    private AssignmentStatus asgStatus = AssignmentStatus.EMPTY;

    private int changeCount;

	public boolean isMsoChanged() {
		IRouteIf route = (IRouteIf)msoObject;
		boolean gChanged = isGeodataChanged(route.getGeodata())
						|| getAssignmentStatus(getOwningArea(route)) != asgStatus;
		boolean cChanged = false;
		IAreaIf area = getOwningArea(route);
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			if(assignment!=null) {
				cChanged = !caption.equals(MsoUtils.getAssignmentName(assignment,2));
			}
		}
		setDirty(isDirty || gChanged || cChanged);
		return gChanged || cChanged;
	}

	private boolean isGeodataChanged(Route r) {
		return (   geodata==null
				|| r==null
				|| !geodata.equals(r)
				|| changeCount!=r.getChangeCount());
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
    public boolean create() throws IOException, AutomationException {
    	if(super.create()) {
			IRouteIf route = (IRouteIf)msoObject;
			geodata = route.getGeodata();
			if (geodata != null) {
				Hashtable<String,String> params = MapUtil.getLayoutParams(
						route.getGeodata().getLayout());
				if(Boolean.valueOf((String)params.get("isPolygon")))
					geometry = MapUtil.getEsriPolygon(geodata, srs);
				else
					geometry = MapUtil.getEsriPolyline(geodata, srs);
				changeCount = geodata.getChangeCount();
			}
			else {
				geometry = null;
				changeCount = 0;
			}
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
			 * 4. The change events are not grouped properly: When the Route is created, the add event is raised before
			 *    the relation to assignment is established (change event), thus the error will occur below.
			 *
			 * In addition to this, a potential linked error exists in the errouneous log
			 * file, which has a double occurrence of this false object in it (is found by
			 * inspection of log file using the MsoObject id.
			 *
			 * POSSIBLE SOLUTION FOUND: When assignments with polygon geodata was created, search area was not set
			 */

			// TODO: THIS IS AN HACK AND SHOULD BE RESOLVED! Route objects MUST have a assignment
			if(area!=null) {
				IAssignmentIf assignment = area.getOwningAssignment();
				caption = MsoUtils.getAssignmentName(assignment,2);
				//System.out.println("IRouteIf:="+route.getObjectId()+ " changed in " + this);
			}
			/*
			else if(route!=null)
				System.out.println("IRouteIf:="+route.getObjectId()+ " is dangling in " + this);
			else
				System.out.println("Empty IRouteIf found and discarded by " + this);
			*/

			setDirty(isDirty || (getShape()!=null));
			return true;
    	}
    	return false;
	}

	public Object getGeodata() {
		return geodata;
	}

}
