package org.redcross.sar.map.command;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.map.IDrawToolCollection;
import org.redcross.sar.gui.map.IPropertyPanel;
import org.redcross.sar.gui.map.LinePanel;
import org.redcross.sar.map.MapUtil;
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
	private IGeometry geoSnap1 = null;
	private IGeometry geoSnap2 = null;
	
	// search type data
	private SearchSubType searchSubType = null;
	
	/**
	 * Constructs the LineTool
	 */
	public LineTool(IDrawToolCollection dialog, boolean isPolygon) throws IOException {
		
		// forward
		super(true,(isPolygon ? FeatureType.FEATURE_POLYGON :
			FeatureType.FEATURE_POLYLINE));
		
		// prepare abstract class BasicTool
		caption = DiskoEnumFactory.getText(featureType); 
		category = "Commands"; 
		message = "Tegner en linje mellom hvert punkt"; 
		name = "CustomCommands_Ployline"; 
		toolTip = "Polylinje"; 
		enabled = true;
		
		// set tool type
		type = DiskoToolType.LINE_TOOL;
		
		// show draw frame when appropriate
		isShowDrawFrame = true;
		
		// map draw operation
		onMouseDownAction = DrawAction.ACTION_BEGIN;
		onMouseMoveAction = DrawAction.ACTION_CHANGE;
		onDblClickAction = DrawAction.ACTION_FINISH;
		
		// create last clicked point
		p1 = (Point)MapUtil.createPoint();

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
			// update caption
			caption = DiskoEnumFactory.getText(featureType);
			// finished
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
	public void setMsoData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		// forward
		super.setMsoData(msoOwner, msoObject, msoClassCode);
		// set mso object in panel
		((LinePanel)getPropertyPanel()).setMsoObject((msoObject!=null ? msoObject : msoOwner));
	}

	@Override
	public boolean doPrepare(IMsoObjectIf msoObj, boolean isDefined) {
		// forward
		if(super.doPrepare(msoObj,isDefined)) {
			// is handling this?
			if(!isDefined) {
				// is owner search assignment?
				if(msoObj instanceof IAreaIf) {
					// get owning assignment
					IAssignmentIf assignment = ((IAreaIf)msoObj).getOwningAssignment();
					// is search assignment?
					if(assignment instanceof ISearchIf) {
						// cast to ISearchIf
						ISearchIf search = (ISearchIf)assignment; 
						// update?
						if(searchSubType!=null && !searchSubType.equals(search.getSubType()))
							search.setSubType(searchSubType);
					}
				}
			}
			// finished
			return true;
		}
		// failed
		return false;
	}

	public boolean onBegin(int button, int shift, int x, int y) {
		
		try {
			
			// get next point. This point is already snapped 
			// by the abstract class is snapping is on.
			p2 = p;
			geoSnap2 = geoSnap;
			
			// initialize geometry?
			if (geoPath == null) {
				// create new polyline
				geoPath = new Polyline();
				geoPath.addPoint(p2, null, null);
			}

			// initialize rubber geometry?
			if (geoRubber == null) {
				geoRubber = new Polyline();
				geoRubber.addPoint(p, null, null);
				geoRubber.addPoint(p, null, null);
			}
			
			// update tool drawings
			updateGeometry();
			
			// revert direction?
			if(isContinued && geoPath!=null 
					&& !FeatureType.FEATURE_POLYGON.equals(featureType)) {
				// get distances
				double d1 = p.returnDistance(geoPath.getFromPoint());
				double d2 = p.returnDistance(geoPath.getToPoint());
				// get start area radius
				double r = getSnapTolerance();
				// not within init distance?
				if(d1>r && d2>r) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// notify later
							Utils.showMessage("Begrensing","Du må starte med et endepunkt");
						}
					});					
					return false;
				}
				// reverse direction?
				if(d1<d2) {
					geoPath.reverseOrientation();
				}
				// continue 
				p1 = (Point)geoPath.getToPoint();
				// forward
				setDirty(true);
			}
			else {
				// initialize
				p1 = p2;				
			}
			// reset flag
			isContinued = false;
			
			// save current geometries
			geoSnap1 = geoSnap2;
			
			// initialize rubber band to this point 
			geoRubber.updatePoint(0,p2);
			geoRubber.updatePoint(1,p2);
		
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
		
			// update last point in rubber band?
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
		IPropertyPanel panel = new LinePanel(this);		
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
			// create last clicked point
			p1 = (Point)MapUtil.createPoint();
			// reset the rest
			p2=null;
			geoSnap1 = null;
			geoSnap2 = null;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateGeometry() throws IOException, AutomationException {
		
		// consume?
		if (p1.returnDistance(p2) < (isSnapToMode()?getSnapTolerance():0) || !isDrawing()) return;

		// trace snap path from p1 to p2?
		if(isSnapToMode() && geoSnap1!=null && geoSnap2!=null) {

			// clone snapping geometries
			Polyline s1 = (Polyline)((Polyline)geoSnap1).esri_clone();
			Polyline s2 = (Polyline)((Polyline)geoSnap2).esri_clone();
			
			// is two differentlines?
			if(!s1.esri_equals(s2)) {
				
				// this must be done for the ITopologicalOperator to function corretly
				s1.simplify();
				s1.setIsKnownSimple(true);
				s2.simplify();
				s2.setIsKnownSimple(true);
				
				// get flags
				boolean touches = s1.touches(s2); 
				boolean crosses = s1.crosses(s2); 
				
				System.out.println("s1 touches s2:" + touches);
				System.out.println("s1 crosses s2:" + crosses);
				
				// translate states
				if(touches && !crosses) {

					/* --------------------------------------------------
					 * s1 and s2 only touches
					 * --------------------------------------------------
					 * ACTION: 	1. Identify touch points
					 * --------------------------------------------------
					 */

					// start with to point
					Point p = null; 
					// determine which end points to use
					if(s2.returnDistance((Point)s1.getFromPoint())==0)
						p = (Point)s1.getFromPoint();
					if(s2.returnDistance((Point)s1.getToPoint())==0) {
						// is both touching?
						if(p!=null) {
							// TODO: Handle this situation (circle)
						}
						else 
							p = (Point)s1.getToPoint();
					}
					
					// add points from nearest point on s1 from 
					// p1 to touch point (a From or To point on s1)
					MapUtil.addPointsBetween(p1, p, s1, geoPath);
										
					// add points from touch point on s2 to p2
					MapUtil.addPointsBetween(p, p2, s2, geoPath);
				}
				else if(!touches && crosses) {
					
					/* --------------------------------------------------
					 * s1 and s2 only crosses
					 * --------------------------------------------------
					 * ACTION: 	
					 * --------------------------------------------------
					 */ 					
					
				}
				else if (touches && crosses) {

					/* --------------------------------------------------
					 * s1 and s2 only touches and crosses
					 * --------------------------------------------------
					 * ACTION: 	1. Identify all touch and cross points
					 * 			2. Trace shortest path from p1 to p2.
					 * 
					 * --------------------------------------------------
					 */ 					
				}
				else {
					
					/* --------------------------------------------------
					 * s1 and s2 do not touch or cross
					 * --------------------------------------------------
					 * ACTION: 	Add p2 to the end of path directly.
					 * 
					 * TODO: 	Implement trace of shortest path from p1 
					 * 			to p2 along a existing connected path 
					 * 			inside the bounding rectangle (p1,p2)
					 * 			add this trace to the path.
					 * --------------------------------------------------
					 */ 
	
					// add current clicked point
					geoPath.addPoint(p2, null, null);			
									
				}
			}
			else {
				
				/* --------------------------------------------------
				 * p1 and p2 snap to the same geometry (s1:=s2)
				 * --------------------------------------------------
				 * ACTION: 	Add all points on s1 after nearest point 
				 * 			on s1 from p1 to the nearest point on s1 
				 * 			from p2
				 * --------------------------------------------------
				 */ 
				
				// add points from nearest point on s1 from p1 to end of s1				
				MapUtil.addPointsBetween(p1, p2, s1, geoPath);
				
				
			}

			
		}
		else {
			// add current clicked point
			geoPath.addPoint(p2, null, null);			
		}

		// forward
		setDirty(true);

		
	}
	
	/*
	private void updateGeometry() throws IOException, AutomationException {
		
		// any change?
		if (p1.returnDistance(p2) == 0 || !isDrawing()) return;

		// get current snap geometry
		
		Polyline pline1 = null;
		Polyline pline2 = null;
		
		// try to snap?
		if(isSnapToMode()) {

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
		// mark change
		setDirty();			
	}*/
	
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
		private IGeometry geoSnap1 = null;
		private IGeometry geoSnap2 = null;

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
			this.geoSnap1 = tool.geoSnap1;
			this.geoSnap2 = tool.geoSnap2;
			this.searchSubType = tool.searchSubType;
		}
		
		public void load(LineTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.p1 = this.p1;
			tool.p2 = this.p2;
			tool.geoSnap1 = this.geoSnap1;
			tool.geoSnap1 = this.geoSnap2;
			tool.searchSubType = this.searchSubType;
		}
	}
}
