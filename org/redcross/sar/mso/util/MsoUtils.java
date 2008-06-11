package org.redcross.sar.mso.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.AbstractUnit;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineListIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIListIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.util.mso.IGeodataIf;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;

import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.interop.AutomationException;

/**
 * Utility class containing access to methods for handling properties.
 * @author kennetgu
 *
 */
public class MsoUtils {
		
	public static boolean isEditable(IMsoObjectIf msoObj) {
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			if(assignment!=null)
				return AssignmentStatus.FINISHED.compareTo(assignment.getStatus())>0;
		}
		return true;
	}
	
	public static boolean isDeleteable(IMsoObjectIf msoObj) {
		// choose delete operations
		if (msoObj instanceof IOperationAreaIf)
			return true;
		else if (msoObj instanceof ISearchAreaIf) {
			return true;
		}
		else if (msoObj instanceof IAreaIf) {
			return isEditable(msoObj);
		}					
		else if (msoObj instanceof IRouteIf) {
			return isEditable(msoObj);
		}					
		else if (msoObj instanceof IPOIIf) {
			return isEditable(msoObj);
		}		
		// all else is deletable
		return true;
	}
	
	public static boolean delete(IMsoObjectIf msoObject, int options) {
		
		// allowd?
		if(isDeleteable(msoObject)) {		
			// dispatch type
			if(msoObject instanceof IAreaIf) {
				// cast to IAreaIf
				IAreaIf area = (IAreaIf)msoObject;
				// get assignment?
				IAssignmentIf assignment = (options!=0) ? area.getOwningAssignment() : null; 
				// delete assignment or area?
				if(assignment!=null)
					return deleteAssignment(assignment);
				else
					return deleteArea(area);
			}
			else if (msoObject instanceof IAssignmentIf) {
				// forward
				return deleteAssignment((IAssignmentIf)msoObject);
			}
			else {
				// forward 
				return msoObject.deleteObject();
			}
		}
		// failure
		return false;
	}
	
	public static boolean deleteArea(IAreaIf area) {
		
		// is object?
		if(area!=null) {
		
			try {
				// delete POI
				for(IPOIIf it : area.getAreaPOIsItems()) {
					// get POIType
					Enum type = getType(it, false);
					// can only delete area poi
					if(POIType.START.equals(type) || POIType.VIA.equals(type) || POIType.STOP.equals(type)) 
						if(!it.deleteObject()) return false;
				}
				// delete geodata
				for(IMsoObjectIf it: area.getAreaGeodataItems()) {
					if(!it.deleteObject()) return false;
				}
				// delete area
				return area.deleteObject();	
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		// failed
		return false;
		
	}
	
	public static boolean deleteAssignment(IAssignmentIf assignment) {
		
		// is object?
		if(assignment!=null) {
		
			try {
				// get area
				IAreaIf area = assignment.getPlannedArea();
				// delete assignment?
				if(assignment!=null)
				// delete planned area
				if(deleteArea(area)) {
					// delete assignment
					return assignment.deleteObject();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		// failed
		return false;
		
	}
	
	public static String getDeleteMessage(IMsoObjectIf msoObj) {
		// get default value
		String message = MsoUtils.getMsoObjectName(msoObj, 1) + " kan ikke slettes";		
		// is deletable?
		if(MsoUtils.isDeleteable(msoObj)) {
			// choose delete operations
			if (msoObj instanceof IOperationAreaIf)
				message ="Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + ". Vil du fortsette?";
			else if (msoObj instanceof ISearchAreaIf) {
				message ="Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + ". Vil du fortsette?";
			}
			else if (msoObj instanceof IAreaIf) {
				message ="Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + ". Vil du fortsette?";
			}					
			else if (msoObj instanceof IRouteIf) {
				// get owning area
				IAreaIf area = MsoUtils.getOwningArea(msoObj);
				// get message
				message ="Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + " fra " 
		            + MsoUtils.getMsoObjectName(area, 1) + ". Vil du fortsette?";
			}					
			else if (msoObj instanceof IPOIIf) {
				// get owning area
				IAreaIf area = MsoUtils.getOwningArea(msoObj);
				// has area?
				if(area!=null)
					// get message
					message ="Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + " fra " 
			            + MsoUtils.getMsoObjectName(area, 1) + ". Vil du fortsette?";
				else
					// get message
					message ="Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + ". Vil du fortsette?";
				
			}
		}		
		// finished
		return message;
	}
	
	public static Enum getType(IMsoObjectIf msoObj, boolean subtype)
    {
    	// initialize
    	Enum e = null;
    	IMsoObjectIf msoOwn = null;
    	
		// get type object
    	if(msoObj instanceof IRouteIf) {
			msoOwn = getOwningArea(msoObj).getOwningAssignment();
		}
		else if(msoObj instanceof IAreaIf) {
			msoOwn = ((IAreaIf)msoObj).getOwningAssignment();
		}

		// replace with owner?
		if(msoOwn!=null)
			msoObj = msoOwn;
		
		// get subtype
		if(msoObj instanceof IAssignmentIf && !subtype) {
			e =((IAssignmentIf)msoObj).getType();			
		}
		else if(msoObj instanceof ISearchIf) {
			e =((ISearchIf)msoObj).getSubType();
		}
		else if(msoObj instanceof IUnitIf) {
			// get type?
			if(!subtype)
				e =((IUnitIf)msoObj).getType();
			else if(msoObj instanceof AbstractUnit){
				// cast
				e = ((AbstractUnit)msoObj).getSubType();
			}
		}
		else if(msoObj instanceof IPOIIf) {
			e =((IPOIIf)msoObj).getType();
		}
		
    	// return sub type
        return e;
    }    
    
    public static Enum getStatus(IMsoObjectIf msoObj)
    {
    	// initialize
    	Enum e = null;
    	IMsoObjectIf msoOwner = null;
    	
		// get type object
    	if(msoObj instanceof IRouteIf) {
			msoOwner = getOwningArea(msoObj).getOwningAssignment();
		}
		else if(msoObj instanceof IAreaIf) {
			msoOwner = ((IAreaIf)msoObj).getOwningAssignment();
		}

		// replace with owner?
		if(msoOwner!=null)
			msoObj = msoOwner;
		
		// get subtype
		if(msoObj instanceof IAssignmentIf) {
			e =((IAssignmentIf)msoObj).getStatus();			
		}
		else if(msoObj instanceof IUnitIf) {
			e =((IUnitIf)msoObj).getStatus();
		}
		else if(msoObj instanceof ITaskIf) {
			e =((ITaskIf)msoObj).getStatus();
		}
		
    	// return sub type
        return e;
    }    
    
    public static IMsoObjectIf getGeoDataParent(IMsoObjectIf msoObj)
    {
    	// found
    	IMsoObjectIf found = null;
		
		// dispatch method
		if(msoObj instanceof IOperationAreaIf) {
			found = msoObj;
		}
		else if(msoObj instanceof ISearchAreaIf) {
			found = msoObj;
		}
		else if(msoObj instanceof IAreaIf) {
			found = msoObj;
		}
    	else if(msoObj instanceof IRouteIf) {
			found = getOwningArea((IRouteIf)msoObj);
		}
		else if(msoObj instanceof IPOIIf) {
			found = getOwningArea((IPOIIf)msoObj);
		}
		
    	// return owning area
        return found;
    }    
    
    public static IAreaIf getOwningArea(IMsoObjectIf msoObj)
    {
		// found
		IAreaIf found = null;
		
		// dispatch method
		if(msoObj instanceof IAreaIf) {
			found = (IAreaIf)msoObj;
		}
		else if(msoObj instanceof IRouteIf) {
			found = getOwningArea((IRouteIf)msoObj);
		}
		else if(msoObj instanceof IPOIIf) {
			found = getOwningArea((IPOIIf)msoObj);			
		}
		
    	// return owning area
        return found;
    } 
    
    public static IAreaIf getOwningArea(IRouteIf anRoute)
    {
		// found
		IAreaIf found = null;
		
		// exists?
    	if(anRoute!=null) {
			
    		// get model
    		IMsoModelIf anModel = Utils.getApp().getMsoModel();
    		
            ICmdPostIf cmdPost = anModel.getMsoManager().getCmdPost();        
            if(cmdPost!=null)	{
            	
		    	// get all areas
		    	ArrayList<IAreaIf> areaList = new ArrayList<IAreaIf>(anModel.getMsoManager().getCmdPost().getAreaList().getItems());
		
		    	// searh for route
		    	for(int i=0;i<areaList.size();i++) {
		    		// get area
		    		IAreaIf area = areaList.get(i); 
		    		// found?
		    		if(area.getAreaGeodata().contains(anRoute)) {
		    			// found
		    			found = area;
		    			break;
		    		}
		    	}
            }
    	}
    	// return owning area
        return found;
    }
    
    public static IAreaIf getOwningArea(IPOIIf anPOI) {

		// found
		IAreaIf found = null;
		
		// exists?
    	if(anPOI!=null) {
    		
    		// get model
    		IMsoModelIf anModel = Utils.getApp().getMsoModel();
    		
    		// get cmd post
    		ICmdPostIf cmdPost = anModel.getMsoManager().getCmdPost();
    		
    		// has cmd post?
    		if(cmdPost!=null) {
    			
	    		// get all areas
		    	ArrayList<IAreaIf> areaList = new ArrayList<IAreaIf>(cmdPost.getAreaList().getItems());
		
		    	// searh for route
		    	for(int i=0;i<areaList.size();i++) {
		    		// get area
		    		IAreaIf area = areaList.get(i); 
		    		// found?
		    		if(area.getAreaPOIs().contains(anPOI)) {
		    			// found
		    			found = area;
		    			break;
		    		}
		    	}
    		}
    	}
    	
    	// return owning area
        return found;    	
    }
    
	public static GeometryBag getGeometryBag(DiskoMap map, IAreaIf area) {
        
        // initialize
		GeometryBag geomBag = null;
        
		// get geometry list from area
		IMsoListIf<IMsoObjectIf> geoList = area.getAreaGeodata().getClone();
        
		// has data
        if (geoList != null && geoList.size() > 0) {
        	try {
	        	// get spatial reference
	        	ISpatialReference srs = map.getSpatialReference();
	        	// create objects
				geomBag = new GeometryBag();
				// get iterator
	            Iterator<IGeodataIf> iter = area.getAreaGeodataIterator();
	            // add to gemotetry
	            while (iter.hasNext()) {
					IGeodataIf geodata = iter.next();
					if (geodata instanceof Route) {
						Polyline polyline = MapUtil.getEsriPolyline((Route)geodata, srs);
						geomBag.addGeometry(polyline, null, null);
					}
				}
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        	}
		}
		return geomBag;
	}	
	
	public static void updateAreaPOIs(DiskoMap map, IAreaIf area) throws IOException, AutomationException {
		// parameters is valid?
		if (area != null)  {
			GeometryBag bag = getGeometryBag(map,area);
			if (bag != null && bag.getGeometryCount() > 0) {
				// first and last line
				IPolyline startPline = (IPolyline)bag.getGeometry(0);
				IPolyline stopPline  = (IPolyline)bag.getGeometry(
						bag.getGeometryCount()-1);
				// get start point
				Point startPoint = (Point)startPline.getFromPoint();
				startPoint.setSpatialReferenceByRef(map.getSpatialReference());
				// get stop point
				Point stopPoint  = (Point)stopPline.getToPoint();
				stopPoint.setSpatialReferenceByRef(map.getSpatialReference());
				// add poi's
				addPOI((IAreaIf)area, startPoint, POIType.START,false);
				addPOI((IAreaIf)area, stopPoint, POIType.STOP,false);
			}
		}
	}

	public static void addPOI(IAreaIf area, Point point, POIType poiType, boolean force)
									throws IOException, AutomationException {
		// try to get poi
		IPOIIf poi = null;
		if(!force)
			poi = getPOI(area, poiType);
		
		// has no poi of requested type?
		if (poi == null) {
			// get command post
			ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
			// get global poi list
			IPOIListIf poiList = cmdPost.getPOIList();
			// create new poi
			poi = poiList.createPOI();
			poi.setPosition(MapUtil.getMsoPosistion(point));
			poi.setType(poiType);
			// add to area
			area.addAreaPOI(poi);
		} else {
			// update current position
			poi.setPosition(MapUtil.getMsoPosistion(point));
		}
	}
	
	public static IPOIIf getPOI(IAreaIf area, POIType poiType) {
		Iterator iter = area.getAreaPOIs().getItems().iterator();
		while (iter.hasNext()) {
			IPOIIf poi = (IPOIIf)iter.next();
			if (poi.getType() == poiType) {
				return poi;
			}
		}
		return null;
	}
	
	public static String getAssignmentName(IAssignmentIf assignment, int options) {
		String name = "<Unknown>";
		if(assignment!=null) {
			if(assignment instanceof ISearchIf) {
				ISearchIf search = (ISearchIf)assignment;
				name = DiskoEnumFactory.getText(search.getSubType()); 
			}
			else {
				name = DiskoEnumFactory.getText(assignment.getType());
			}
			// include number?
			if(options >0)
				name += " " + assignment.getNumber();
			// include status text?
			if(options >1)
				name += " (" + DiskoEnumFactory.getText(assignment.getStatus()) + ")";
		}
		return name;
	}

	public static String getAssignmentStatusText(IAssignmentIf assignment) {
		String name = "<Unknown>";
		if(assignment!=null) {
			name = DiskoEnumFactory.getText(assignment.getStatus());
		}
		return name;
	}
	
	public static String getUnitName(IUnitIf unit, boolean include) {
		String name = "<Unknown>";
		if(unit!=null) {
			name = DiskoEnumFactory.getText(unit.getType()) + " " + unit.getNumber();
			// include status text?
			if(include)
				name += " (" + (DiskoEnumFactory.getText(unit.getStatus())) + ")";
		}
		return name;
	}
	
	public static String getUnitStatusText(IUnitIf unit) {
		String name = "<Unknown>";
		if(unit!=null) {
			name = DiskoEnumFactory.getText(unit.getStatus());
		}
		return name;
	}
	
	public static String getOperationAreaName(IOperationAreaIf area, boolean include) {
		String name = "<Unknown>";
		if(area!=null) {
			name = DiskoEnumFactory.getText(MsoClassCode.CLASSCODE_OPERATIONAREA);
			if(include) {
				int i = 0;
				IMsoModelIf model = MsoModelImpl.getInstance();
				Collection<IOperationAreaIf> c = model.getMsoManager().getCmdPost().getOperationAreaListItems();
				Iterator<IOperationAreaIf> it = c.iterator();
				while(it.hasNext()) {
					i++;
					if(it.next() == area)
						break;			
				}
				name.concat(String.valueOf(getOperationNumber(area)));
			}
		}
		return name; 
	}
	
	public static int getOperationNumber(IOperationAreaIf operationArea) {
		if(operationArea!=null) {
			int i = 0;
			IMsoModelIf model = MsoModelImpl.getInstance();
			Collection<IOperationAreaIf> c = model.getMsoManager().getCmdPost().getOperationAreaListItems();
			Iterator<IOperationAreaIf> it = c.iterator();
			while(it.hasNext()) {
				i++;
				if(it.next() == operationArea)
					break;			
			}
			return (i+1);
		}
		return 0; 
	}
	
	public static String getSearchAreaName(ISearchAreaIf searchArea) {
		String name = "<Unknown>";
		if(searchArea!=null) {
			switch(searchArea.getPriority()) {
			case 0: name = DiskoStringFactory.getText("PRIMARY_SEARCH_AREA"); break;
			case 1: name = DiskoStringFactory.getText("SECONDARY_SEARCH_AREA"); break;
			case 2: name = DiskoStringFactory.getText("PRIORITY3_SEARCH_AREA"); break;
			case 3: name = DiskoStringFactory.getText("PRIORITY4_SEARCH_AREA"); break;
			case 4: name = DiskoStringFactory.getText("PRIORITY5_SEARCH_AREA"); break;
			}
		}
		return name;
	}
	
	public static String getAreaName(IAreaIf area, int options) {
		String name = "<Unknown>";
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			// has assignment?
			if(assignment!=null) {
				name = getAssignmentName(assignment, options);
			}
			else {
				name = DiskoEnumFactory.getText(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
			}
		}
		return name;
	}
	
	public static String getRouteName(IRouteIf route, boolean include) {
		String name = "<Unknown>";
		if(route!=null) {
			name = DiskoEnumFactory.getText(route.getMsoClassCode());
			// include status text?
			if(include)
				name += " " + (route.getAreaSequenceNumber()+1);
		}
		return name;
	}
	
	public static String getTrackName(ITrackIf track, boolean include) {
		String name = "<Unknown>";
		if(track!=null) {
			name = DiskoEnumFactory.getText(track.getMsoClassCode());
			// include status text?
			if(include)
				name += " " + (track.getAreaSequenceNumber()+1);
		}
		return name;
	}
	
	public static String getPOIName(IPOIIf poi, boolean replace, boolean include) {
		// get name
		String name = "<Unknown>";
		
		if(poi!=null) {

			name = DiskoEnumFactory.getText(poi.getType()); 			
			String remark = poi.getRemarks();
			// replace with comment?
			if (remark != null) {
				if(replace && remark.length() > 0) {
					return remark;
				}
			}
			// include sequence number?
			if(include) {			
				IPOIIf.POIType type = poi.getType();
				boolean isNumberedPoi = (
						type == POIType.FINDING || 
						type == POIType.GENERAL || 
						type == POIType.INTELLIGENCE || 
						type == POIType.SILENT_WITNESS || 
						type == POIType.OBSERVATION || 
						type == POIType.VIA);
				if (isNumberedPoi)
					name += " " + (poi.getAreaSequenceNumber()+1);
			}			
		}
		return name;
	}
	
	public static String getMsoObjectName(IMsoObjectIf msoObj, int options) {
		if (msoObj instanceof IOperationAreaIf) 
			if(options==0)
				return getOperationAreaName((IOperationAreaIf)msoObj,false);
			else
				return getOperationAreaName((IOperationAreaIf)msoObj,true);
		if (msoObj instanceof ISearchAreaIf) 
			return getSearchAreaName((ISearchAreaIf)msoObj);
		else if(msoObj instanceof IAreaIf)
			return getAreaName((IAreaIf)msoObj,options);
		else if(msoObj instanceof IAssignmentIf) {
			return getAssignmentName((IAssignmentIf)msoObj, options);
		}
		else if (msoObj instanceof IPOIIf) { 
			if(options==0)
				return getPOIName((IPOIIf)msoObj, false, false);
			else if(options==1)
				return getPOIName((IPOIIf)msoObj, false, true);
			else
				return getPOIName((IPOIIf)msoObj, true, false);
		}
		else if(msoObj instanceof IUnitIf) {
			if(options==0)
				return getUnitName((IUnitIf)msoObj, false);
			else
				return getUnitName((IUnitIf)msoObj, true);			
		}
		else if(msoObj instanceof IRouteIf) {
			if(options==0)
				return getRouteName((IRouteIf)msoObj, false);
			else
				return getRouteName((IRouteIf)msoObj, true);			
		}
		else if(msoObj instanceof ITrackIf) {
			if(options==0)
				return getTrackName((ITrackIf)msoObj, false);
			else
				return getTrackName((ITrackIf)msoObj, true);			
		}
		else if(msoObj!=null)
			return DiskoEnumFactory.getText(msoObj.getMsoClassCode());
		else
			return "<null>";
		
	}
	
	public static String getMessageText(IMessageIf message) {

		// initialize
    	StringBuilder stringBuilder = new StringBuilder();

    	// get message lines
    	IMessageLineListIf lines = message.getMessageLines();
    	
    	// loop over all lines
    	for(IMessageLineIf line : lines.getItems())
    	{
    		String lineText = line.toString();
    		if (stringBuilder.length()>0)
            	stringBuilder.append(". " + lineText);
    		else
    			stringBuilder.append(lineText);
        }
        return stringBuilder.toString();		
	}
	
	public static boolean inAssignment(IMsoObjectIf msoObj) {
		
		if (msoObj instanceof IRouteIf) {
			return true;
		}
		else if (msoObj instanceof IPOIIf) {
			
			// get poi
			IPOIIf poi = (IPOIIf)msoObj;
			
			// get poi type
			IPOIIf.POIType poiType = poi.getType();
			
			// get flag
			return (poiType == IPOIIf.POIType.START) || 
				(poiType == IPOIIf.POIType.VIA) || (poiType == IPOIIf.POIType.STOP);
			
		}
		
		return false;
		
	}

	//	create name comparator
	private static final MsoCompareName compareNames = new MsoCompareName();
	
	public static void sortByName(List<IMsoObjectIf> data, int options) {
		compareNames.setOptions(options);
		Collections.sort(data,compareNames);
	}
	
	public static void sortByName(IMsoObjectIf[] data, int options) {
		compareNames.setOptions(options);
		Arrays.sort(data, compareNames);
	}
	
	public static int compare(IMsoObjectIf m1, IMsoObjectIf m2, int options) {
		// handle combinations of null
		if(m1==null && m2==null) return 0;
		if(m1==null && m2!=null) return -1;
		if(m1!=null && m2==null) return 1;
		// initialize
		String s1 = null;
		String s2 = null;
		MsoClassCode code = m1.getMsoClassCode();
		// is of not of same class?
		if(!m2.getMsoClassCode().equals(code)) {
			// get names with no additional information
			s1 = DiskoEnumFactory.getText(m1.getMsoClassCode());
			s2 = DiskoEnumFactory.getText(m2.getMsoClassCode());
			s1 = (s1==null) ? "" : s1;
			s2 = (s2==null) ? "" : s2;
			// compare mso class names
			return s1.compareTo(s2);
		}
		// get names with no additional information
		s1 = getMsoObjectName(m1, 0);
		s2 = getMsoObjectName(m2, 0);
		// get difference
		int d = s1.compareTo(s2);
		// direct name compare is enough?
		if(options==0) return d;
		// are names different?
		if(d!=0) return d;
		// continue to compare sequence numbers
		if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code)) 
			return compare((IOperationAreaIf)m1,(IOperationAreaIf)m2);
		if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(code))
			return 0;
		if(MsoClassCode.CLASSCODE_AREA.equals(code)) { 
			return compare(((IAreaIf)m1).getOwningAssignment(),
					((IAreaIf)m2).getOwningAssignment());
		}
		if(MsoClassCode.CLASSCODE_ASSIGNMENT.equals(code)) {
			return compare((IAssignmentIf)m1,(IAssignmentIf)m2);
		}
		if(MsoClassCode.CLASSCODE_POI.equals(code)) {
			return compare(((IPOIIf)m1).getAreaSequenceNumber(), 
					((IPOIIf)m2).getAreaSequenceNumber());
		}
		if(MsoClassCode.CLASSCODE_UNIT.equals(code))
			return compare(((IUnitIf)m1).getNumber(), ((IUnitIf)m2).getNumber());
		if(MsoClassCode.CLASSCODE_ROUTE.equals(code)) {
			return compare(((IRouteIf)m1).getAreaSequenceNumber(), 
					((IRouteIf)m2).getAreaSequenceNumber());
		}
		if(MsoClassCode.CLASSCODE_ROUTE.equals(code)) {
			return compare(((ITrackIf)m1).getAreaSequenceNumber(), 
					((ITrackIf)m2).getAreaSequenceNumber());
		}
		// compare not supported, is equal by default
		return 0;
	}

	public static int compare(IOperationAreaIf a1,IOperationAreaIf a2) {
		return compare(getOperationNumber((IOperationAreaIf)a1),
				getOperationNumber((IOperationAreaIf)a2));		
	}
	
	public static int compare(IAssignmentIf a1, IAssignmentIf a2) {
		// handle combinations of null
		if(a1==null && a2==null) return 0;
		if(a1==null && a2!=null) return -1;
		if(a1!=null && a2==null) return 1;
		// compare numbers
		return compare(a1.getNumber(),a2.getNumber());		
	}
	
	private static int compare(int a, int b) {
		if(a==b) return 0;
		else if(a<b) return -1;
		else return 1;
	}
	
	public static POIType[] getAvailablePOITypes(MsoClassCode code, IMsoObjectIf msoObj) {
		if(MsoClassCode.CLASSCODE_ROUTE.equals(code)) {
			// initialize
			EnumSet<POIType> list = EnumSet.of(POIType.START, POIType.VIA, POIType.STOP);
			// get area if exists
			IAreaIf area = getOwningArea(msoObj);
			if(area!=null) {
				// Only allow one Start and one Stop POI per area
				for(IPOIIf it: area.getAreaPOIsItems()) {
					POIType type = it.getType();
					if(!POIType.VIA.equals(type)) {
						if(list.contains(it.getType())) {
							list.remove(it.getType());
						}
					}
				}
			}
			int i=0;
			POIType[] types = new POIType[list.size()];
			for(POIType type : list) { 
				types[i] = type;
				i++;
			}
			return types;
		}
		else if(MsoClassCode.CLASSCODE_POI.equals(code)) {
			// default POI types
			return new POIType[] { POIType.GENERAL, POIType.INTELLIGENCE,
					POIType.OBSERVATION, POIType.FINDING,
					POIType.SILENT_WITNESS };
		}
		// not supported
		return null;
	}
	
}
