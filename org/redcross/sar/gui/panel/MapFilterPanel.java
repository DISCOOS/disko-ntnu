/**
 * 
 */
package org.redcross.sar.gui.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.EnumSet;

import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.ToggableTabPane;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IMapLayer.LayerCode;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;

/**
 * @author kennetgu
 *
 */
public class MapFilterPanel extends ToggableTabPane {

	private static final long serialVersionUID = 1L;

	private IDiskoMap map = null;
	
	/**
	 * Constructor 
	 */
	public MapFilterPanel() {
		// initialize GUI
		initialize();		
	}
	
	private void initialize() {
		// is not focusable
		setFocusable(false);		

		// set preferred tab size
		setPreferredTabSize(new Dimension(80,25));
		
		setCaption("Filter:");
		
		// add tabs
		addTab("Planlagt", "P",0);
		addTab("Utføres","U",1);
		addTab("Ferdig","F",2);
				
	}

	public void setMap(IDiskoMap map) {
		this.map = map;
	}
	
	public IDiskoMap getMap() {
		return this.map;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// apply filter to map?
		if(map!=null) {
			try {
				
				// forward
				setAssignmentFilters();
				
				// apply filter changes
				map.refreshMsoLayers();
				
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		// forward
		super.actionPerformed(e);
	}
	
	private boolean setAssignmentFilters() {
		// initialize enum set
		EnumSet<AssignmentStatus> status = 
			EnumSet.noneOf(AssignmentStatus.class);				
		// get arguments
		if(isTabSelected(0))
			status.addAll(EnumSet.range(AssignmentStatus.EMPTY,AssignmentStatus.ALLOCATED));
		if(isTabSelected(1))
			status.add(AssignmentStatus.EXECUTING);
		if(isTabSelected(2))
			status.add(AssignmentStatus.FINISHED);
			
		// reset current filters
		setFilter(map.getMsoLayer(LayerCode.AREA_LAYER),null,0);
		setFilter(map.getMsoLayer(LayerCode.ROUTE_LAYER),null,0);			
		setFilter(map.getMsoLayer(LayerCode.POI_LAYER),null,0);
		
		// apply assignment filter?
		if(status.size()>0) {
			// apply assignment filters
			setFilter(map.getMsoLayer(LayerCode.AREA_LAYER),
					new Filter(MsoClassCode.CLASSCODE_AREA, status,0),0);
			setFilter(map.getMsoLayer(LayerCode.ROUTE_LAYER),
					new Filter(MsoClassCode.CLASSCODE_ROUTE, status,0),0);
			setFilter(map.getMsoLayer(LayerCode.POI_LAYER),
					new Filter(MsoClassCode.CLASSCODE_POI, status,0),0);
		}
		
		// TODO: Calculate dirty bit
		return true;
	}
	
	private void setFilter(IMsoFeatureLayer msoLayer,Filter filter, int id) {
		if(msoLayer!=null) {
			if(filter!=null)
				msoLayer.addSelector(filter, id);
			else
				msoLayer.removeSelector(id);
		}
		//System.out.println(msoLayer!=null ? msoLayer.isDirty() : "");
	}
	
	/*
	public static JPanel createPanel(IDiskoMap map, 
			MapFilterBar buddy, String position, Border border) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(buddy,position);
		panel.add((JComponent)map,BorderLayout.CENTER);
		buddy.setMap(map);
		panel.setBorder(border);
		return panel;
	}
	*/
	
	private class Filter implements Selector<IMsoObjectIf> {

		private int state = 0;
		private MsoClassCode code = null;
		private Object compare = null;
		
		public Filter(MsoClassCode code, Object compare, int state) {
			this.state = state;
			this.code = code;
			this.compare = compare;			
		}
		
		public boolean select(IMsoObjectIf anObject) {
			// assume selected
			boolean bSelect = true;
			// is filtered?
			if(code.equals(anObject.getMsoClassCode())) {
				// dispatch filter type
				if(MsoClassCode.CLASSCODE_AREA.equals(anObject.getMsoClassCode())) {
					// cast to area
					IAreaIf area = (IAreaIf)anObject;
					// get area
					AssignmentStatus status = area.getOwningAssignment().getStatus();
					// get set
					EnumSet<AssignmentStatus> set = (EnumSet<AssignmentStatus>)compare;
					// get filter operation
					bSelect = set.contains(status);
				}				
				else if(MsoClassCode.CLASSCODE_ROUTE.equals(anObject.getMsoClassCode())) {
					// cast to route
					IRouteIf route = (IRouteIf)anObject;
					// get area
					IAreaIf area = MsoUtils.getOwningArea(route);
					// has area?
					if(area!=null) {
						// get area
						AssignmentStatus status = area.getOwningAssignment().getStatus();
						// get set
						EnumSet<AssignmentStatus> set = (EnumSet<AssignmentStatus>)compare;
						// get filter operation
						bSelect = set.contains(status);
					}
				}
				else if(MsoClassCode.CLASSCODE_POI.equals(anObject.getMsoClassCode())) {
					// cast to pui
					IPOIIf poi = (IPOIIf)anObject;
					// get area
					IAreaIf area = MsoUtils.getOwningArea(poi);
					// has area?
					if(area!=null) {
						// get area
						AssignmentStatus status = area.getOwningAssignment().getStatus();
						// get set
						EnumSet<AssignmentStatus> set = (EnumSet<AssignmentStatus>)compare;
						// get filter operation
						bSelect = set.contains(status);
					}
					/*
					else {
						bSelect = false;
					}
					*/
				}
			}
			// finished
			return bSelect;
		}
		
	}
		
}
