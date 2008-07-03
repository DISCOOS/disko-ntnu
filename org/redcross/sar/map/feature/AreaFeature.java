package org.redcross.sar.map.feature;

import com.esri.arcgis.interop.AutomationException;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.IGeodataIf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AreaFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
    private IMsoListIf<IMsoObjectIf>  geoList = null;
    private List<Integer>  changeList = null;
    private AssignmentStatus asgStatus = AssignmentStatus.DRAFT;
    private IMsoModelIf msoModel = null;

	public AreaFeature(IMsoModelIf msoModel) {
		this.msoModel = msoModel;
	}

	public boolean isMsoChanged(IMsoObjectIf msoObj) {
		IAreaIf area = (IAreaIf)msoObject;
        boolean gChanged = isGeodataChanged(area.getAreaGeodata()) 
        				|| getAssignmentStatus(area) != asgStatus ;
		ISearchIf search = (ISearchIf)area.getOwningAssignment();
		boolean cChanged = !caption.equals(MsoUtils.getAssignmentName(search,2));
		isDirty = isDirty || gChanged || cChanged;
		return gChanged || cChanged;
	}

	private boolean isGeodataChanged(IMsoListIf<IMsoObjectIf> list) {
		// check instance first
		if(geoList==null || list==null || !geoList.equals(list)) return true;
		// check change counters
		int i=0;
		for(IMsoObjectIf it : list.getItems()) {
			// parse
			if(it instanceof IRouteIf) {
				// get geodata
				IGeodataIf geodata = ((IRouteIf)it).getGeodata();
				// changed?
				if(!changeList.get(i).equals(geodata.getChangeCount())) return true;
			}
			else if(it instanceof ITrackIf) {
				// get geodata
				IGeodataIf geodata = ((ITrackIf)it).getGeodata();
				// changed?
				if(!changeList.get(i).equals(geodata.getChangeCount())) return true;	
			}			
		}   
		return false;
	}
	
    AssignmentStatus getAssignmentStatus(IAreaIf anArea)
    {
        IAssignmentIf asg = anArea.getOwningAssignment();
        if (asg == null) return AssignmentStatus.DRAFT;
        return asg.getStatus();
    }

    @Override
    public void msoChanged() throws IOException, AutomationException {       // todo sjekk etter endring av GeoCollection
		if (srs == null) return;
		IAreaIf area = (IAreaIf)msoObject;
        geoList = area.getAreaGeodata();
        setChangeList();
        geometry = MapUtil.getEsriGeometryBag(geoList,MsoClassCode.CLASSCODE_ROUTE, srs);
        asgStatus = getAssignmentStatus(area);
		ISearchIf search = (ISearchIf)area.getOwningAssignment();
		caption = MsoUtils.getAssignmentName(search,2);
		super.msoChanged();
	}
    
    private void setChangeList() {
    	changeList = new ArrayList<Integer>(geoList!=null ? geoList.size() : 0);
    	if(geoList!=null) {
			for(IMsoObjectIf it : geoList.getItems()) {
				// parse
				if(it instanceof IRouteIf) {
					// get geodata
					IGeodataIf geodata = ((IRouteIf)it).getGeodata();
					// add change count
					changeList.add(geodata.getChangeCount());
				}
				else if(it instanceof ITrackIf) {
					// get geodata
					IGeodataIf geodata = ((ITrackIf)it).getGeodata();
					// add change count
					changeList.add(geodata.getChangeCount());
				}
			}
    	}
	}

	public Object getGeodata() {
		return geoList;
	}

	public int getGeodataCount() {
        return geoList != null ? geoList.size() : 0;

    }

}
