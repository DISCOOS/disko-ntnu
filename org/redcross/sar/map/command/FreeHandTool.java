/**
 * 
 */
package org.redcross.sar.map.command;

import java.io.IOException;
import java.util.ArrayList;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.map.FreeHandPanel;
import org.redcross.sar.gui.map.IDrawToolCollection;
import org.redcross.sar.gui.map.IPropertyPanel;
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
	public FreeHandTool(IDrawToolCollection dialog, boolean isPolygon) throws IOException {
		
		// forward
		super(true,(isPolygon ? FeatureType.FEATURE_POLYGON :
			FeatureType.FEATURE_POLYLINE));
		
		// initialize abstract class BasicTool
		caption = "Frihånd (" + DiskoEnumFactory.getText(featureType) + ")"; 
		category = "Commands"; 
		message = "Tegner en frihåndsstrek"; 
		name = "CustomCommands_FreeHand"; 
		toolTip = "Frihånd"; 
		enabled = true;
		
		// set tool type
		type = DiskoToolType.FREEHAND_TOOL;
		
		// map draw operation
		onMouseDownAction = DrawAction.ACTION_BEGIN;
		onMouseMoveAction = DrawAction.ACTION_CHANGE;
		onMouseUpAction = DrawAction.ACTION_FINISH;
		
		// create initial point 
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
			FreeHandPanel panel = (FreeHandPanel)propertyPanel;
			
			// release current?
			if(snapAdapter!=null) {
				// add listener
				snapAdapter.removeSnapListener(panel);
			}
			
			// get a snapping adapter from map
			snapAdapter = map.getSnapAdapter();
			
			// register freehand panel?
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
			caption = "Frihånd (" + DiskoEnumFactory.getText(featureType) + ")";
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
	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		// forward
		super.setMsoDrawData(msoOwner, msoObject, msoClassCode);
		// select mso object
		((FreeHandPanel)getPropertyPanel()).setMsoObject((msoOwner!=null ? msoOwner : msoObject));
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

			// get next point. This point is already snapped 
			// by the abstract class is snapping is on.
			p2 = p;
	
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
			
			// update drawn geometry
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
		
		// only update if left mouse button is pressed
		if(button==1) {
		
			try {
	
				// get next point. This point is already snapped 
				// by the abstract class if snapping is on
				p2 = p;

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
	public IPropertyPanel addPropertyPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<IPropertyPanel>(1);			
		// create panel
		IPropertyPanel panel = new FreeHandPanel(this,true);
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

		// has new line been found?
		if (p1.returnDistance(p2) != 0 && isDrawing()) {
		
			// initialize
			Polyline pline1 = null;
			Polyline pline2 = null;
			
			// try to snap?
			if(isSnapToMode()) {

				// only update snap geometry
				snapTo(p1);
				
				// search polyline inside envelope
				pline1 = (Polyline)snapAdapter.getSnapGeometry();
				pline2 = (Polyline)geoSnap;
				
			}
			
			// nothing to snap onto?
			if (pline1 == null || pline2 == null) {
				// add to geometry
				geoPath.addPoint(p2, null, null);
				// replace rubber
				geoRubber = geoPath;
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
