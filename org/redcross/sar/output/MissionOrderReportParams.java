package org.redcross.sar.output;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIListIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;

public class MissionOrderReportParams {
	public final static String MISSION_TITLE	= "Mission title";
	public final static String PRIORITY_TEXT = "Priority text";
	public final static String ENTITY_NAME = "Entity name";
	public final static String PLANNED_ACCURACY = "Planned accuracy";
	public final static String N50_MAP_NAME = "N50Mapname";
	
	public final static String MISSION_TYPE = "Mission type";
	public final static String POI_DESCRIPTION = "Poi description";//not in use
	
	public String keyPoiPrefix = "Poi";	
	public int maxPoisInPrint = 6;
	
	public final static String REMARKS = "Remarks";
	public final static String DTG = "dtg";
	public final static String SCALE = "Scale";
	public final static String ROLE_NAME = "role name";
	public final static String SUBREPORT_DIR = "SUBREPORT_DIR";
	
	public final static String MAP = "map_path";
	
	private Map<String, Object> printParams = null;
			
	public MissionOrderReportParams(){
		printParams = new HashMap<String, Object>();
	}
	
	public Map setPrintParams(IAssignmentIf assignment, String exportMapPath, String role_name, String templateDir, ISpatialReference srs, double mapScale, String time){			
		printParams.clear();	
		if (assignment instanceof ISearchIf) {
			ISearchIf search = (ISearchIf)assignment;
			putNotNullValue(printParams, MISSION_TITLE, search.getTypeAndNumber());
			putNotNullValue(printParams, PRIORITY_TEXT, search.getPriorityText());
			IUnitIf unit = search.getOwningUnit();
			if(unit != null)
				putNotNullValue(printParams, ENTITY_NAME, unit.getUnitNumber());
			else
				putNotNullValue(printParams, ENTITY_NAME, null);
			putNotNullValue(printParams, N50_MAP_NAME, null);
			printParams.put(PLANNED_ACCURACY, Integer.toString(search.getPlannedAccuracy())+" %");
			
			putNotNullValue(printParams, MISSION_TYPE, search.getTypeText());
			makePoiList(search, srs);
			
			putNotNullValue(printParams, MAP, exportMapPath);
			
			putNotNullValue(printParams, REMARKS, search.getRemarks());
			putNotNullValue(printParams, ROLE_NAME, role_name);			
			putNotNullValue(printParams, DTG, time);
			int t = (int) mapScale;
			String sTime = Integer.toString(t);
			putNotNullValue(printParams, SCALE, sTime);
			//printParams.put(SUBREPORT_DIR, templateDir);//for subreport
			
		}
		
		
		
		return printParams;
	}
	
	private void makePoiList(ISearchIf search, ISpatialReference srs){
		IPOIListIf poiList = search.getPlannedArea().getAreaPOIs();
		int poiCount = poiList.size();
		Collection poiItems = poiList.getItems();
		Iterator iter = poiItems.iterator();
		int i = 1; 
		String poiDesc = new String();
		String keyPoi = new String();
		String mgrs = new String();
		while(iter.hasNext()){
			keyPoi = keyPoiPrefix + i;
			IPOIIf poi = (IPOIIf) iter.next();
			poi.getAreaSequenceNumber();
			//System.out.println(poi.getRemarks());
			Position pos = poi.getPosition();	
			/*Point p = null;
			int x = 0;
			int y = 0;*/
			try{
				//p = MapUtil.getEsriPoint(pos, srs);
				mgrs = MapUtil.formatMGRS(MapUtil.getMGRSfromPosition(pos),3);
				//System.out.println("Posisjon: x = " + p.getX() + ", y = " + p.getY() + ". mgrs = " + mgrs);
				//x = (int) p.getX();
				//y = (int) p.getY();
			}catch(Exception e){
				e.printStackTrace();
			}			
			
			if(i == 1){
				poiDesc = Integer.toString(1) + ". Start i " + mgrs + ", " + poi.getRemarks();
			}
			else if (i == poiList.size())
				poiDesc = Integer.toString(poiList.size()) + ". Slutt i " + mgrs + ", " + poi.getRemarks();
			else
				poiDesc = Integer.toString(i + 1) + ". Via "   + mgrs +  ", " + poi.getRemarks();
			printParams.put(keyPoi, poiDesc);
			i++;
		}
		for (int j = 0; j < (maxPoisInPrint-poiCount); j++){
			keyPoi = keyPoiPrefix + (poiCount+1+j);
			printParams.put(keyPoi, "");
		}
	}
	
	private void putNotNullValue(Map<String, Object> printParams, String key, String value){
		if (value == null){
			//printParams.put(key, "");//
			printParams.put(key, "<ikke tilgj.>");//testkommentar
		}
		else
			printParams.put(key, value);
	}
	
}
