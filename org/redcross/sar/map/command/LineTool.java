package org.redcross.sar.map.command;

import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.ISegmentGraphCursor;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.SegmentGraph;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.IHostToolDialog;
import org.redcross.sar.gui.LinePanel;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

/**
 * A custom line draw tool.
 * @author geira
 *
 */
public class LineTool extends AbstractDrawTool {

	private static final long serialVersionUID = 1L;
	
	// holds draw geometry
	private Point p1 = null;
	private Point p2 = null;
	
	// search type data
	private SearchSubType searchSubType = null;
	
	/**
	 * Constructs the LineTool
	 */
	public LineTool(IHostToolDialog dialog, boolean isPolygon) throws IOException {
		
		// forward
		super(true,(isPolygon ? DrawFeatureType.DRAW_FEATURE_POLYGON :
			DrawFeatureType.DRAW_FEATURE_POLYLINE));
		
		// prepare abstract class BasicTool
		cursorPath = "cursors/crosshair.cur"; 
		caption = "Polylinje"; 
		category = "Commands"; 
		message = "Tegner en linje mellom hvert valgte punkt"; 
		name = "CustomCommands_Ployline"; 
		toolTip = "PolyLine"; 
		enabled = true;
		
		// set tool type
		type = DiskoToolType.LINE_TOOL;
		
		// map draw operation
		onMouseDownAction = DrawActionType.DRAW_BEGIN;
		onMouseMoveAction = DrawActionType.DRAW_CHANGE;
		onDblClickAction = DrawActionType.DRAW_FINISH;
		
		// create last clicked point
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
		// set mso object in panel
		((LinePanel)getPropertyPanel()).setMsoObject((msoObject!=null ? msoObject : msoOwner));
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
				rubberBand.addPoint(p2, null, null);
			}
			
			// update tool drawings
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
		
		try {
		
			// get screen-to-map transformation and try to snap
			p = snapTo(transform(x,y));
	
			// update rubber band
			if (rubberBand != null) {
				rubberBand.updatePoint(1, p);
			}
			
			// finished
			return true;

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// finished
		return true;		
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
	
	public JPanel addPropertyPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<JPanel>(1);			
		// create panel
		JPanel panel = new LinePanel(Utils.getApp(),this,true);
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
		
		// any change?
		if (p1.returnDistance(p2) == 0) return;

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

		// nothing to snap to?
		if (pline1 == null || pline2 == null) {
			// add point
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
	
	@Override
	public IDiskoToolState save() {
		// get new state
		return new LineToolState(this);
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
		// valid state?
		if(state instanceof LineToolState) {
			((LineToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
	
	public class LineToolState extends DiskoToolState {

		// points
		private Point p1 = null;
		private Point p2 = null;

		// search type data
		private SearchSubType searchSubType = null;

		// create state
		public LineToolState(LineTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		public void save(LineTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.p1 = tool.p1;
			this.p2 = tool.p2;
			this.searchSubType = tool.searchSubType;
		}
		
		public void load(LineTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.p1 = this.p1;
			tool.p2 = this.p2;
			tool.searchSubType = this.searchSubType;
		}
	}
}
