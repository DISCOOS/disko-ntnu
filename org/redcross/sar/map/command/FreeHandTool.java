/**
 * 
 */
package org.redcross.sar.map.command;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.map.FreeHandPanel;
import org.redcross.sar.gui.map.IHostToolDialog;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;

import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.ISegmentGraphCursor;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.SegmentGraph;
import com.esri.arcgis.interop.AutomationException;

/**
 * Free hand drawing tool
 * 
 * @author kennetgu
 *
 */
public class FreeHandTool extends AbstractDrawTool {


	private static final long serialVersionUID = 1L;
	
	// holds draw geometry
	private Point p1 = null;
	private Point p2 = null;
	
	// search type data
	private SearchSubType searchSubType = null;
	
	/**
	 * Constructs the FreeHandTool
	 */
	public FreeHandTool(IHostToolDialog dialog, boolean isPolygon) throws IOException {
		
		// forward
		super(true,(isPolygon ? DrawFeatureType.DRAW_FEATURE_POLYGON :
			DrawFeatureType.DRAW_FEATURE_POLYLINE));
		
		// initialize abstract class BasicTool
		cursorPath = "cursors/crosshair.cur"; 
		caption = "Frihåndstegning"; 
		category = "Commands"; 
		message = "Tegner en frihåndsstrek"; 
		name = "CustomCommands_FreeHand"; 
		toolTip = "Frihåndstegning"; 
		enabled = true;
		
		// set tool type
		type = DiskoToolType.FREEHAND_TOOL;
		
		// map draw operation
		onMouseDownAction = DrawActionType.DRAW_BEGIN;
		onMouseMoveAction = DrawActionType.DRAW_CHANGE;
		onMouseUpAction = DrawActionType.DRAW_FINISH;
		
		// create initial point 
		p1 = new Point();
		p1.setX(0);
		p1.setY(0);
		
		// create default property panel
		propertyPanel = addPropertyPanel();
		
		// save dialog
		this.dialog = (DiskoDialog)dialog;
		
		// registrate me in dialog
		dialog.register((IDrawTool)this, propertyPanel);		
		
	}
	
	@Override
	public boolean activate(boolean allow) {
		// forward
		boolean flag = super.activate(allow);
		// update modes in panel
		((FreeHandPanel)getPropertyPanel()).updateModes();
		// return state
		return flag;
	}
	
	@Override
	public void setAttribute(Object value, String attribute) {
		super.setAttribute(value, attribute);
		if("DRAWPOLYGON".equalsIgnoreCase(attribute)) {
			if((Boolean)value)
				drawFeatureType=DrawFeatureType.DRAW_FEATURE_POLYGON;
			else
				drawFeatureType=DrawFeatureType.DRAW_FEATURE_POLYLINE;
			return;
		}
		if("SEARCHSUBTYPE".equalsIgnoreCase(attribute)) {
			searchSubType = (SearchSubType)value;
			return;
		}
	}
	
	@Override
	public Object getAttribute(String attribute) {
		if("DRAWPOLYGON".equalsIgnoreCase(attribute)) {
			return (drawFeatureType==DrawFeatureType.DRAW_FEATURE_POLYGON);
		}
		if("SEARCHSUBTYPE".equalsIgnoreCase(attribute)) {
			return searchSubType;
		}
		return super.getAttribute(attribute);
	}
		
	@Override
	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		// forward
		super.setMsoDrawData(msoOwner, msoObject, msoClassCode);
		// forward
		((FreeHandPanel)getPropertyPanel()).setMsoObject((msoObject!=null ? msoObject : msoOwner));
	}

	@Override
	public boolean doPrepare() {
		// is owner search assignment?
		if(msoOwner instanceof IAreaIf) {
			// get owning assignment
			IAssignmentIf assignment = ((IAreaIf)msoOwner).getOwningAssignment();
			// is search assignment?
			if(assignment instanceof ISearchIf) {
				// cast to ISearchIf
				ISearchIf search = (ISearchIf)assignment; 
				// update?
				if(searchSubType!=null && !searchSubType.equals(search.getSubType()))
					search.setSubType(searchSubType);
			}
		}
		// forward
		return super.doPrepare();
	}
	
	public boolean onBegin(int button, int shift, int x, int y) {
		
		try {

			// get screen-to-map transformation and try to snap
			p2 = snapTo(transform(x,y));
	
			// initialize path geometry?
			if (pathGeometry == null) {
				pathGeometry = new Polyline();
				pathGeometry.addPoint(p2, null, null);
				rubberBand = new Polyline();
				rubberBand.addPoint(p2, null, null);
			}
			
			// update drawn geometry
			updateGeometry();
			
			// update last point
			p1 = p2;
			
			// update rubber band
			rubberBand.updatePoint(0,p2);
			
			// success
			return true;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// failed
		return false;
	}

	public boolean onChange(int button, int shift, int x, int y) {
		
		// only update if left mouse button is pressed
		if(button==1) {
		
			try {
	
				// get screen-to-map transformation and try to snap
				p2 = snapTo(transform(x,y));

				// update drawn geometry
				updateGeometry();
				
				// update last point
				p1 = p2;
				
				// success
				return true;					
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		// failed
		return false;
	}
	

	@Override
	public boolean onCancel() {
		try {
			// reset tool
			reset();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// always return success
		return true;
	}

	@Override
	public boolean onFinish(int button, int shift, int x, int y) {
		try {
			// reset tool
			reset();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// always return success
		return true;
	}	
	
	@Override
	public JPanel addPropertyPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<JPanel>(1);			
		// create panel
		JPanel panel = new FreeHandPanel(Utils.getApp(),this,true);
		// try to add
		if (panels.add(panel)) {
			return panel;
		}
		return null;
	}
	
	private void reset() throws IOException {
		// reset point
		p1 = new Point();
		p1.setX(0);
		p1.setY(0);
	}	

	private void updateGeometry() throws IOException, AutomationException {

		// has new line been found?
		if (p1.returnDistance(p2) != 0 && isDrawing) {
		
			// initialize
			Polyline pline1 = null;
			Polyline pline2 = null;
			
			// try to snap?
			if(isSnapToMode()) {

				// forward
				snapTo(p1);
				
				// search polyline inside envelope
				pline1 = (Polyline)snapping.getSnapGeometry();
				pline2 = (Polyline)snapGeometry;
				
			}
			
			// nothing to snap onto?
			if (pline1 == null || pline2 == null) {
				// add to geometry
				pathGeometry.addPoint(p2, null, null);
			}
			else {
				// densify rubberband, use vertices as input to segment graph
				Polyline copy = (Polyline)rubberBand.esri_clone();
				copy.densify(getSnapTolerance()/10, -1);
				// greate a geometry bag to hold selected polylines
				GeometryBag gb = new GeometryBag();
				if (pline1 != null) {
					gb.addGeometry(pline1, null, null);
				}
				if (pline2 != null) {
					gb.addGeometry(pline2, null, null);
				}
				// create the segment graph
				SegmentGraph segmentGraph = new SegmentGraph();
				segmentGraph.load(gb, false, true);
				ISegmentGraphCursor segmentGraphCursor = segmentGraph.getCursor(p1);
		
				// tracing the segmnet graph
				for (int i = 0; i < copy.getPointCount(); i++) {
					IPoint p = copy.getPoint(i);
					if (!segmentGraphCursor.moveTo(p)) {
						segmentGraphCursor.finishMoveTo(p);
					}
				}
				Polyline trace = (Polyline)segmentGraphCursor.getCurrentTrace();
				if (trace != null && trace.getPointCount() > 0) {
					// add tracepoints to path
					for (int i = 0; i < trace.getPointCount(); i++ ) {
						pathGeometry.addPoint(trace.getPoint(i), null, null);
					}
				}
				else {
					pathGeometry.addPoint(p2, null, null);
				}
				// reset
				segmentGraphCursor = null;
			}
		}		
	}
	
	@Override
	public IDiskoToolState save() {
		// get new state
		return new FreeHandToolState(this);
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
		// valid state?
		if(state instanceof FreeHandToolState) {
			((FreeHandToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
	
	public class FreeHandToolState extends AbstractDrawTool.DrawToolState {

		// holds draw geometry
		private Point p1 = null;
		private Point p2 = null;
		
		// search type data
		private SearchSubType searchSubType = null;

		// create state
		public FreeHandToolState(FreeHandTool tool) {
			super((AbstractDrawTool)tool);
			save(tool);
		}	
		
		public void save(FreeHandTool tool) {
			super.save((AbstractDrawTool)tool);
			this.p1 = tool.p1;
			this.p2 = tool.p2;
			this.searchSubType = tool.searchSubType;
		}
		
		public void load(FreeHandTool tool) {
			super.load((AbstractDrawTool)tool);
			tool.p1 = this.p1;
			tool.p2 = this.p2;
			tool.searchSubType = this.searchSubType;
		}
	}
}
