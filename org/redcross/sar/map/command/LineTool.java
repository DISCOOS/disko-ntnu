package org.redcross.sar.map.command;

import java.io.IOException;
import java.util.ArrayList;

import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.ISegmentGraphCursor;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.SegmentGraph;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.map.IDrawDialog;
import org.redcross.sar.gui.map.IPropertyPanel;
import org.redcross.sar.gui.map.LinePanel;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;

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
	public LineTool(IDrawDialog dialog, boolean isPolygon) throws IOException {
		
		// forward
		super(true,(isPolygon ? FeatureType.FEATURE_POLYGON :
			FeatureType.FEATURE_POLYLINE));
		
		// prepare abstract class BasicTool
		caption = "Line " + Utils.getEnumText(featureType); 
		category = "Commands"; 
		message = "Tegner en linje mellom hvert punkt"; 
		name = "CustomCommands_Ployline"; 
		toolTip = "Polylinje"; 
		enabled = true;
		
		// enable snapping
		isSnapping = true;
		
		// set tool type
		type = DiskoToolType.LINE_TOOL;
		
		// map draw operation
		onMouseDownAction = DrawAction.ACTION_BEGIN;
		onMouseMoveAction = DrawAction.ACTION_CHANGE;
		onDblClickAction = DrawAction.ACTION_FINISH;
		
		// create last clicked point
		p1 = new Point();
		p1.setX(0);
		p1.setY(0);

		// create default property panel
		propertyPanel = addPropertyPanel();
		
		// save dialog
		this.dialog = (DiskoDialog)dialog;
		
		// registrate me in dialog
		dialog.register(this);
		
	}
		
	@Override
	public void onCreate(Object obj) {

		// forward
		super.onCreate(obj);

		// has map?
		if(map!=null) {
			
			// get property panel
			LinePanel panel = (LinePanel)propertyPanel;
			
			// release current?
			if(snapAdapter!=null) {
				// add listener
				snapAdapter.removeSnapListener(panel);
			}
			
			// get a snapping adapter from map
			snapAdapter = map.getSnapAdapter();
			
			// register panel?
			if(snapAdapter!=null) {
				// add listener
				snapAdapter.addSnapListener(panel);
				// update panel
				panel.onSnapToChanged();
			}
		}
		
	}
	
	@Override
	public void setAttribute(Object value, String attribute) {
		super.setAttribute(value, attribute);
		if("DRAWPOLYGON".equalsIgnoreCase(attribute)) {
			if((Boolean)value)
				featureType=FeatureType.FEATURE_POLYGON;
			else
				featureType=FeatureType.FEATURE_POLYLINE;
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
			return (featureType==FeatureType.FEATURE_POLYGON);
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
			p2 = snapTo(toMapPoint(x,y));
			
			// initialize path geometry?
			if (geoPath == null) {
				geoPath = new Polyline();
				geoPath.addPoint(p2, null, null);
			}

			// initialize rubber geometry?
			if (geoRubber == null) {
				geoRubber = new Polyline();
				geoRubber.addPoint(p2, null, null);
			}
			
			// update tool drawings
			updateGeometry();
			
			// update last point
			p1 = p2;
			
			// update rubber band
			geoRubber.updatePoint(0,p2);
		
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
		
			// update rubber band
			if (geoRubber != null) {
				geoRubber.updatePoint(1, p);
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

	public IPropertyPanel addPropertyPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<IPropertyPanel>(1);			
		// create panel
		IPropertyPanel panel = new LinePanel(this,true);		
		// try to add
		if (panels.add(panel)) {
			return panel;
		}
		return null;
	}
	
	public void reset() {
		// forward
		super.reset();
		// reset point
		try {
			p1 = new Point();
			p1.setX(0);
			p1.setY(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			pline1 = (Polyline)snapAdapter.getSnapGeometry();
			pline2 = (Polyline)geoSnap;
			
		}

		// nothing to snap to?
		if (pline1 == null || pline2 == null) {
			// add point
			geoPath.addPoint(p2, null, null);
		}
		else {
			// densify rubberband, use vertices as input to segment graph
			Polyline copy = (Polyline)geoRubber.esri_clone();
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
					geoPath.addPoint(trace.getPoint(i), null, null);
				}
			}
			else {
				geoPath.addPoint(p2, null, null);
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
