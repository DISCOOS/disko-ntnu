/**
 * 
 */
package org.redcross.sar.mso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.redcross.sar.app.Utils;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.AbstractUnit;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
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
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.util.mso.IGeodataIf;
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
		
    public static Enum getType(IMsoObjectIf msoObj, boolean subtype)
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
		if(msoObj instanceof IRouteIf) {
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
				name = Utils.translate(search.getSubType()); 
			}
			else {
				name = Utils.translate(assignment.getType());
			}
			// include number?
			if(options >0)
				name += " " + assignment.getNumber();
			// include status text?
			if(options >1)
				name += " - " + Utils.translate(assignment.getStatus());
		}
		return name;
	}

	public static String getUnitName(IUnitIf unit, boolean include) {
		String name = "<Unknown>";
		if(unit!=null) {
			name = Utils.translate(unit.getType()) + " " + unit.getNumber();
			// include status text?
			if(include)
				name += " - " + Utils.translate(unit.getStatus());
		}
		return name;
	}
	
	public static String getOperationAreaName(IOperationAreaIf operationArea, boolean include) {
		String name = "<Unknown>";
		if(operationArea!=null) {
			name = Utils.translate(MsoClassCode.CLASSCODE_OPERATIONAREA);
			if(include) {
				int i = 0;
				IMsoModelIf model = MsoModelImpl.getInstance();
				Collection<IOperationAreaIf> c = model.getMsoManager().getCmdPost().getOperationAreaListItems();
				Iterator<IOperationAreaIf> it = c.iterator();
				while(it.hasNext()) {
					i++;
					if(it.next() == operationArea)
						break;			
				}
				name += " " + i;
			}
		}
		return name; 
	}
	
	public static String getSearchAreaName(ISearchAreaIf searchArea) {
		String name = "<Unknown>";
		if(searchArea!=null) {
			switch(searchArea.getPriority()) {
			case 0: name = Utils.translate("PRIMARY_SEARCH_AREA"); break;
			case 1: name = Utils.translate("SECONDARY_SEARCH_AREA"); break;
			case 2: name = Utils.translate("PRIORITY3_SEARCH_AREA"); break;
			case 3: name = Utils.translate("PRIORITY4_SEARCH_AREA"); break;
			case 4: name = Utils.translate("PRIORITY5_SEARCH_AREA"); break;
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
				name = Utils.translate(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
			}
		}
		return name;
	}
	
	public static String getRouteName(IRouteIf route, boolean include) {
		String name = "<Unknown>";
		if(route!=null) {
			name = Utils.translate(route.getMsoClassCode());
			// include status text?
			if(include)
				name += " " + route.getAreaSequenceNumber();
		}
		return name;
	}
	
	public static String getTrackName(ITrackIf track, boolean include) {
		String name = "<Unknown>";
		if(track!=null) {
			name = Utils.translate(track.getMsoClassCode());
			// include status text?
			if(include)
				name += " " + track.getAreaSequenceNumber();
		}
		return name;
	}
	
	public static String getPOIName(IPOIIf poi, boolean include) {
		// get name
		String name = "<Unknown>";
		
		if(poi!=null) {
			name = Utils.translate(poi.getType()); 			

			// include status text?
			if(include) {
				IPOIIf.POIType type = poi.getType();
				boolean isAreaPoi = (type == IPOIIf.POIType.START || 
						type == IPOIIf.POIType.VIA || 
						type == IPOIIf.POIType.STOP);
				if (isAreaPoi)
					name += " " + poi.getAreaSequenceNumber();
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
			return getAssignmentName(((IAreaIf)msoObj).getOwningAssignment(), options);
		else if(msoObj instanceof IAssignmentIf) {
			return getAssignmentName((IAssignmentIf)msoObj, options);
		}
		else if (msoObj instanceof IPOIIf) { 
			if(options==0)
				return getPOIName((IPOIIf)msoObj, false);
			else
				return getPOIName((IPOIIf)msoObj, true);
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
			return Utils.translate(msoObj.getMsoClassCode());
		else
			return "<null>";
		
	}
}
