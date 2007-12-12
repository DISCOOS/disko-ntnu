/**
 * 
 */
package org.redcross.sar.map.command;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JToggleButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
import org.redcross.sar.gui.DiskoCustomIcon;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.IDrawDialog;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.SnappingAdapter;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAreaListIf;
import org.redcross.sar.mso.data.IAssignmentListIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IOperationAreaListIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIListIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.IRouteListIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.ISearchAreaListIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentPriority;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.mso.Route;

import com.esri.arcgis.carto.InvalidArea;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnAfterScreenDrawEvent;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.display.RgbColor;
import com.esri.arcgis.display.SimpleLineSymbol;
import com.esri.arcgis.display.SimpleMarkerSymbol;
import com.esri.arcgis.display.esriScreenCache;
import com.esri.arcgis.display.esriSimpleMarkerStyle;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.esriSegmentExtension;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractDrawTool extends AbstractDiskoTool implements IDrawTool {

	private static final long serialVersionUID = 1L;
	
	private static final double pointSize = 12;
	private static final int SNAP_TOL_FACTOR = 200;

	protected enum DrawActionType {
		DRAW_BEGIN,
		DRAW_CHANGE,
		DRAW_FINISH,
		DRAW_CANCEL,
		DRAW_DISCARD
	}
	
	protected enum DrawFeatureType {
		DRAW_FEATURE_POINT,
		DRAW_FEATURE_POLYLINE,
		DRAW_FEATURE_POLYGON
	}
	
	// flags
	protected boolean isDrawing = false;
	protected boolean isMoving = false;
	protected boolean isRubberInUse = false;
	protected boolean isBuffered = false;
	protected boolean isPending = false;
	protected boolean isUpdateMode = false;
	protected boolean isConstrainMode = true;
	protected boolean isWorkPoolMode = true;
	
	// temporary flags
	private boolean doSnapTo = false;
	
	// constrain attributes
	protected int minStep = 10;
	protected int maxStep = 100;

	// gesture constants
	protected DrawActionType onMouseDownAction = null;
	protected DrawActionType onMouseMoveAction = null;
	protected DrawActionType onMouseUpAction = null;	
	protected DrawActionType onClickAction = null;	
	protected DrawActionType onDblClickAction = null;	
	
	// draw feature constant
	protected DrawFeatureType drawFeatureType = null;
	
	// counters
	protected int moveCount = 0;
	
	// point buffers
	protected Point p = null;
		
	// holds draw geometry
	protected Polyline pathGeometry = null;
	protected Polyline rubberBand = null;
	protected IGeometry snapGeometry = null;

	// snapping
	protected SnappingAdapter snapping = null;
	
	// some draw information used to ensure that old draw 
	// geometries are removed from the screen
	protected InvalidArea lastInvalidArea = null;
	
	// draw symbols
	protected SimpleMarkerSymbol markerSymbol = null;
	protected SimpleLineSymbol lineSymbol = null;
	protected SimpleLineSymbol flashSymbol = null;
	
	
	/**
	 * Constructs the DrawTool
	 */
	public AbstractDrawTool(boolean isRubberInUse, 
			DrawFeatureType drawFeatureType) throws IOException {
		
		// forward
		super();
		
		// create the symbol to draw with
		markerSymbol = new SimpleMarkerSymbol();
		RgbColor markerColor = new RgbColor();
		markerColor.setRed(255);
		markerSymbol.setColor(markerColor);
		markerSymbol.setStyle(esriSimpleMarkerStyle.esriSMSCross);
		markerSymbol.setSize(pointSize);
		
		// create the symbol to draw with
		lineSymbol = new SimpleLineSymbol();
		RgbColor lineColor = new RgbColor();
		lineColor.setRed(255);
		lineSymbol.setColor(lineColor);
		lineSymbol.setWidth(1);

		// create symbol to indicate snapping
		flashSymbol = new SimpleLineSymbol();
		RgbColor flashColor = new RgbColor();
		flashColor.setGreen(255);
		flashColor.setBlue(255);
		flashSymbol.setColor(flashColor);
		flashSymbol.setWidth(2);
		
		// discard all operations
		onClickAction = DrawActionType.DRAW_DISCARD;
		onDblClickAction = DrawActionType.DRAW_DISCARD;
		onMouseDownAction = DrawActionType.DRAW_DISCARD;
		onMouseMoveAction = DrawActionType.DRAW_DISCARD;
		onMouseUpAction = DrawActionType.DRAW_DISCARD;
		
		// set draw type and rubber flag
		this.isRubberInUse = isRubberInUse;
		this.drawFeatureType = drawFeatureType;
		
		// set flags
		this.showDirect = false;
		
		// get current application
		IDiskoApplication app = Utils.getApp();
				
		// create button
		Dimension size = app.getUIFactory().getSmallButtonSize();
		button = new JToggleButton();
		button.setPreferredSize(size);

		// add show dialog listener
		button.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				// double click?
				if(e.getClickCount() == 2) {
					dialog.setVisible(!dialog.isVisible());
				}
			}

			public void mousePressed(MouseEvent e) {
				// start show/hide
				dialog.doShow(!dialog.isVisible(), 250);				
			}

			public void mouseReleased(MouseEvent e) {
				// stop show if not shown already
				dialog.cancelShow();				
			}
			
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}

		});
		
	}

	/* ==================================================
	 * Public methods (override with care) 
	 * ==================================================
	 */
	
	public boolean isDrawing() {
		return isDrawing;
	}
	
	public void onCreate(Object obj) {
		
		// is working?
		if(isWorking()) return;
		
		try {

			// only a DiskoMap object is accecpted
			if (obj instanceof DiskoMap) {
	
				// initialize map object
				map = (DiskoMap)obj;
				
				// load draw dialog 
				((IDrawDialog)dialog).onLoad(map);
				dialog.setLocationRelativeTo(map, DiskoDialog.POS_WEST, true);
				
				// set marked button
				if(button!=null && button.getIcon() instanceof DiskoCustomIcon) {
					DiskoCustomIcon icon = (DiskoCustomIcon)button.getIcon();
					icon.setMarked(true);
				}
				
				// update snapping
				updateSnapping();
				
				// add listener to used to draw tool geometries
				map.addIMapControlEvents2Listener(new MapControlAdapter());
	
				// activate tool
				isActive = true;
				
			}
			
			// reset flags
			isMoving=false;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// forward
		super.onCreate(obj);
		
	}

	@Override
	public void onClick() {
		// forward to extenders
		onAction(onClickAction);
	}

	@Override
	public void onDblClick() {
		// forward to extenders
		onAction(onDblClickAction);		
	}	
	
	@Override
	public void onMouseDown(int button, int shift, int x, int y) {
		// forward to extenders
		onAction(onMouseDownAction, button, shift, x, y);
	}
	
	@Override
	public void onMouseMove(int button, int shift, int x, int y) {

		// increment mouse move
		moveCount++;
		
		//Only create the trace every other time the mouse moves
		if(moveCount < 2)
		    return;
		
		// reset move counter
		moveCount = 0;
			
		// is moving
		isMoving = true;
		
		// forward to extenders of this class
		onAction(onMouseMoveAction,button,shift,x,y);		
		
		// is not moving any more
		isMoving = false;
		
	}

	@Override
	public void onMouseUp(int button, int shift, int x, int y) {
		// forward
		onAction(onMouseUpAction, button, shift, x, y);
	}
	
	@Override
	public void onKeyDown(int keyCode , int shift) {
		if(!isActive || isWorking()) return;
		// cancel drawing?
		if(keyCode == Event.ESCAPE) {
			onAction(DrawActionType.DRAW_CANCEL, 0, 0, 0, 0);
		}
	}

	/**
	 * This method is rised from either onClick(*) or 
	 * onDblClick(*)
	 *  
	 * @param keycode
	 * @param shift
	 * @return True if begin is a success 
	 */	
	public boolean onBegin() {
		return true;
	}

	/**
	 * This method is rised from either onMouseDown(*), 
	 * onMouseMove(*) or onMouseDown(*)
	 * 
	 * @param button
	 * @param shift
	 * @param x
	 * @param y
	 * @return True if begin is a success
	 */
	public boolean onBegin(int button, int shift, int x, int y) {
		return true;
	}
	
	/**
	 * This method is rised from either onClick(*) or 
	 * onDblClick(*)
	 *  
	 * @param keycode
	 * @param shift
	 * @return True if change is a success 
	 */	
	public boolean onChange() {
		return true;
	}
	
	/**
	 * This method is rised from either onMouseDown(*), 
	 * onMouseMove(*) or onMouseDown(*)
	 * 
	 * @param button
	 * @param shift
	 * @param x
	 * @param y
	 * @return True if change is a success
	 */
	public boolean onChange(int button, int shift, int x, int y) {
		return true;
	}
	
	/**
	 * This method is rised from either onClick(*) or 
	 * onDblClick(*)
	 *  
	 * @param keycode
	 * @param shift
	 * @return True of finish is a success 
	 */	
	public boolean onFinish() {
		return true;
	}
	
	/**
	 * This method is rised from either onMouseDown(*), 
	 * onMouseMove(*) or onMouseDown(*)
	 * 
	 * @param button
	 * @param shift
	 * @param x
	 * @param y
	 * @return True if finish is a success
	 */
	public boolean onFinish(int button, int shift, int x, int y) {
		return true;
	}
	
	/**
	 * This method is rised when user has pressed the ESC button
	 * 
	 * @param button
	 * @param shift
	 * @param x
	 * @param y
	 * @return True if cancel is a success
	 */
	public boolean onCancel() {
		return true;
	}
	
	
	/**
	 * This method is rised after doFinish() is returned with true and isBuffered=false 
	 * or if isPending=true and apply() is invoked. Any spesific mso object updates should
	 * be done in this method.
	 * 
	 * @return True if cancel is a success
	 */
	public boolean doPrepare() {
		return true;
	}	
	
	/**
	 * This method initialize the tool
	 * 
	 */
	public void doInit() {
		// reset
		msoOwner = null;
		msoObject = null;
		// forward
		doPrepare();		
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
		// valid state?
		if(state instanceof DrawToolState) {
			((DrawToolState)state).load(this);
			return true;
		}
		return false;
	}
	
	@Override
	public Object getAttribute(String attribute) {
		if("ISUPDATEMODE".equalsIgnoreCase(attribute)) {
			return isUpdateMode;
		}
		return super.getAttribute(attribute);
	}

	@Override
	public void setAttribute(Object value, String attribute) {
		super.setAttribute(value, attribute);
		if("ISUPDATEMODE".equalsIgnoreCase(attribute)) {
			isUpdateMode = (Boolean)value;
			return;
		}
	}
		
	@Override
	public boolean deactivate(){
		
		// cancel operation?
		if((isDrawing || isWorking()) && !isBuffered) {
			afterCancel(true);
		}
		
		// forward?
		if(isHosted())
			getHostTool().deactivate();
		
		// forward
		super.deactivate();
				
		// set flag 
		isActive = false;
		
		// forward		
		return true;
		
	}
		
	/* ==================================================
	 * Implemention of IDrawTool (override with care)
	 * ==================================================
	 */
	
	public int getMaxStep() {
		return maxStep;
	}

	public int getMinStep() {
		return minStep;
	}

	public boolean isConstrainMode() {
		return isConstrainMode;
	}

	public void setConstrainMode(boolean isConstrainMode) {
		this.isConstrainMode = isConstrainMode;
	}

	public boolean isWorkPoolMode() {
		return isWorkPoolMode;
	}

	public void setWorkPoolMode(boolean isWorkPoolMode) {
		this.isWorkPoolMode = isWorkPoolMode;
	}
	
	public void setMaxStep(int distance) {
		maxStep = distance;
	}

	public void setMinStep(int distance) {
		minStep = distance;
	}
	
	public void setSnapTolerance(double tolerance) throws IOException, AutomationException {
		if(snapping!=null)
			snapping.setSnapTolerance(tolerance);
	}

	public int getSnapTolerance() throws IOException {
		if(snapping!=null)
			return snapping.getSnapTolerance();
		return -1;
	}

	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		
		// is working?
		if(isWorking()) return;
		
		// is not drawing?
		if(!isDrawing) {
			
			// only IAreaIf is a valid owner
			if(!(msoOwner instanceof IAreaIf)) 
				msoOwner = null;			
			
			// is drawing point?
			if(drawFeatureType==DrawFeatureType.DRAW_FEATURE_POINT) {
				// get point flag
				boolean supportsPoint = msoObject!=null &&
					(MsoClassCode.CLASSCODE_POI.equals(msoObject.getMsoClassCode()) || 
					 MsoClassCode.CLASSCODE_UNIT.equals(msoObject.getMsoClassCode())); 
				// point not supported?
				if(msoObject!=null && !supportsPoint) {
					// invalid object referense
					msoObject=null;
				}
			}
			else {
				// get point flag
				boolean supportsLine = msoObject!=null &&
					(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(msoObject.getMsoClassCode()) ||
							MsoClassCode.CLASSCODE_SEARCHAREA.equals(msoObject.getMsoClassCode()) ||
							MsoClassCode.CLASSCODE_ROUTE.equals(msoObject.getMsoClassCode())); 
				// point not supported?
				if(msoObject!=null && !supportsLine) {
					// invalid object referense
					msoObject=null;
				}
			}

			// set mso owner object
			this.msoOwner = msoOwner;
			// set mso object
			this.msoObject = msoObject;
			// set mso object
			this.msoClassCode = msoClassCode;
		}
	}

	public SnappingAdapter getSnappingAdapter() {
		return snapping;
	}
	
	public void setSnappingAdapter(SnappingAdapter adapter) {
		// save adapter
		snapping = adapter;
	}
	
	public boolean doSnapTo() {
		
		// is no work pending?
		if(!isPending && snapping!=null) {
			
			try {
	
				// dispatch the mso draw data
				if (msoClassCode == MsoClassCode.CLASSCODE_OPERATIONAREA) {
		            // get polygon?
					if(msoObject!=null) {
						// get polygon
						Polygon polygon = MapUtil.getEsriPolygon(((IOperationAreaIf)msoObject).getGeodata(),map.getSpatialReference());
						// get polyline
						pathGeometry = MapUtil.getPolyline(polygon);
						// set flag
						isPending = true;
					}
				}
				else if (msoClassCode == MsoClassCode.CLASSCODE_SEARCHAREA) {
		            // get polygon?
					if(msoObject!=null) {
						// get polygon
						Polygon polygon = MapUtil.getEsriPolygon(((ISearchAreaIf)msoObject).getGeodata(),map.getSpatialReference());
						// get polyline
						pathGeometry = MapUtil.getPolyline(polygon);
						// set flag
						isPending = true;
					}
				}
				else if (msoClassCode == MsoClassCode.CLASSCODE_ROUTE) {
					
		            // get route?
					if(msoObject!=null) {
						// get polyline
						pathGeometry = MapUtil.getEsriPolyline(((IRouteIf)msoObject).getGeodata(),map.getSpatialReference());
						// set flag
						isPending = true;
					}
				}
				
				// set flag
				doSnapTo = isPending;
				
				// forward
				return apply();
					
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		// failed
		return false;
	}
	
	@Override
	public IDiskoToolState save() {
		// get new state
		return new DrawToolState(this);
	}

	public boolean isPending() {
		return isPending;
	}
	
	public boolean isBuffered() {
		return isBuffered;
	}

	public void setBuffered(boolean isBuffered) {
		this.isBuffered = isBuffered;
	}
	
	public boolean apply() {
		// has work pending?
		if(isPending) {
			// forward
			if(doFinishWork()) {
				// rest flag
				isPending = false;
				// success
				return true;
			}
		}
		// failure
		return false;
	}
	
	public boolean cancel() {
		// has work pending?
		if(isPending) {
			// rest flag
			isPending = false;
			// forward
			reset();
			// return success
			return true;
		}
		// return failed
		return false;
	}
	
	public boolean isUpdateMode() {
		return isUpdateMode;
	}

	public void setUpdateMode(boolean isUpdateMode) {
		this.isUpdateMode = isUpdateMode;
	}	
	
	public boolean isSnapToMode() {
		return snapping==null ? false : snapping.isSnapToMode();
	}

	public void setSnapToMode(boolean isSnapToMode) {
		// can snap?
		if(snapping!=null) {
			try {
				// forward
				snapping.setSnapToMode(isSnapToMode);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}	
	
	/* ==================================================
	 * Protected methods intended for use by
	 * extending classes (override with care)
	 * ==================================================
	 */

	/**
	 * Snap to point
	 * 
	 * @throws IOException
	 * @throws AutomationException
	 */	
	protected Point snapTo(Point p) throws IOException, AutomationException {
		// try to snap?
		if(snapping!=null && snapping.isSnapToMode()) {	
			// forward
			p = snapping.doSnapTo(p);
			// update snap geometry
			snapGeometry = snapping.getSnapGeometry();			
		}	
		return p;
	}
		
	/**
	 * Draws the geometry on screen
	 * 
	 * @throws IOException
	 * @throws AutomationException
	 */
	protected void refresh() throws IOException, AutomationException {
		if(map!=null & isRubberInUse) {
			boolean bdirty = false;
			InvalidArea invalidArea = new InvalidArea();
			// is type defined?
			if(drawFeatureType!=null){ 
				
				// do work depending on type of feature
				if(drawFeatureType==DrawFeatureType.DRAW_FEATURE_POINT) {
					if (p != null && !isMoving) {
						invalidArea.add(p);
						bdirty = true;
					}
				}
				else {
					if (pathGeometry != null && !isMoving) {
						invalidArea.add(pathGeometry);
						bdirty = true;
					}
					if (rubberBand != null) {
						invalidArea.add(rubberBand);
						bdirty = true;
					}
					if (snapGeometry != null) {
						invalidArea.add(snapGeometry);
						bdirty = true;
					}
				}
			}
			 
			// refresh old area?
			if(lastInvalidArea!=null) {
				lastInvalidArea.setDisplayByRef(map.getActiveView().getScreenDisplay());
				lastInvalidArea.invalidate((short) esriScreenCache.esriNoScreenCache);
			}			
			
			// refresh new area?
			if(bdirty) {
				invalidArea.setDisplayByRef(map.getActiveView().getScreenDisplay());
				invalidArea.invalidate((short) esriScreenCache.esriNoScreenCache);
				lastInvalidArea = invalidArea;
			}
			else
				lastInvalidArea = null;

		}
	}

	protected Point getNearestPoint(Polyline pline, Point point)
			throws IOException, AutomationException {
		// forward
		return (Point)pline.returnNearestPoint(point, 
				esriSegmentExtension.esriNoExtension);
	}

	/* ==================================================
	 * Private methods intended internal use
	 * ==================================================
	 */
	private void onAction(DrawActionType action, 
			int button, int shift, int x, int y) {
		if(action.equals(DrawActionType.DRAW_BEGIN)) {
			if(beforeBegin()) {
				afterBegin(onBegin(button, shift, x, y));
			}
		}
		else if(action.equals(DrawActionType.DRAW_CHANGE)) {
			if(beforeChange()){
				afterChange(onChange(button, shift, x, y));
			}
		}
		else if(action.equals(DrawActionType.DRAW_CANCEL)) {
			if(beforeCancel()) {
				afterCancel(onCancel());
			}
		}
		else if(action.equals(DrawActionType.DRAW_FINISH)) {
			// validate finish
			if (beforeFinish()) {
				afterFinish(onFinish(button, shift, x, y));
			}
		}
	}
	
	private void onAction(DrawActionType action) {
		if(action.equals(DrawActionType.DRAW_BEGIN)) {
			if(beforeBegin()) {
				afterBegin(onBegin());
			}
		}
		else if(action.equals(DrawActionType.DRAW_CHANGE)) {
			if(beforeChange()){
				afterChange(onChange());
			} 
		}
		else if(action.equals(DrawActionType.DRAW_CANCEL)) {
			if(beforeCancel()) {
				afterCancel(onCancel());
			}
		}
		else if(action.equals(DrawActionType.DRAW_FINISH)) {
			// validate finish
			if (beforeFinish()) {
				afterFinish(onFinish());
			}
		}
	}
		
	private boolean beforeBegin() {

		// valid operation?
		if(!isActive || isWorking()) return false;
				
		// set as working (prevents reentry)
		setIsWorking();
		
		// valid
		return true;
			
	}

	private void afterBegin(boolean success) {

		// was begin operation successfull?
		if(success) {

			try {
	
				// update tool drawings
				refresh();
						
				// is drawing
				isDrawing = true;
		
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// apply any change
			afterChange(true);
		}
		// reset work flag
		setIsNotWorking();
	}
	
	private boolean beforeChange() {

		// valid operation?
		if(!isActive || !isDrawing || isWorking()) return false;
		
		// set working flag
		setIsWorking();
		
		// valid
		return true;
	}

	private void afterChange(boolean success) {
		// was begin operation successfull?
		if(success) {
			try {				
				// draw the geometries on map 
				refresh();				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		// reset working flag
		setIsNotWorking();		
	}
	
	
	private boolean beforeCancel() {
		// valid operation?
		if(!isActive || !isDrawing) return false;
		// set working flag
		setIsWorking();
		// is valid
		return true;
	}
	
	private void afterCancel(boolean success) {
		// was cancel operation successfull?
		if(success) {
			// reset tool
			reset();
			try {
				refresh();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			// notify
			fireOnWorkCancel();
		}
		// reset working flag
		setIsNotWorking();
	}	
	
	private boolean beforeFinish() {
		// valid operation?
		if(!isActive || !isDrawing || isWorking()) return false;
		// set working flag
		setIsWorking();
		// valid
		return true;
	}
	
	private void afterFinish(boolean success) {
		
		try {
			// set flags
			isDrawing = false;
			// buffer work?
			if(isBuffered) {
				// set flag
				isPending = true;
				// draw the geometries on map 
				refresh();				
			}
			else {
				// schedule work 
				if(!doFinishWork()) {
					Utils.showWarning("Kunne ikke utføre oppgaven");
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// reset working flag
		setIsNotWorking();
	}
	
	@Override
	protected void fireOnWorkCancel() {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				null,msoObject,null,DiskoWorkEventType.TYPE_CANCEL);
	   	// forward
		fireOnWorkCancel(e);
	}
	
	@Override
	protected void fireOnWorkChange() {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				null,msoObject,null,DiskoWorkEventType.TYPE_CHANGE);
	   	// forward
    	fireOnWorkChange(e);
	}

	private void reset() {
		try {
			// rest draw geometries
			p = null;
			pathGeometry = null;
			rubberBand = null;
			snapGeometry = null;
			// refresh draw geometries
			refresh();
			// set flags
			isDrawing = false;
			isMoving = false;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}	
	
	private void updateSnapping() throws IOException, AutomationException {
		// get IDrawDialog interface
		IDrawDialog drawdialog = (IDrawDialog)dialog;
		// set snap tolerance of tool
		setSnapTolerance(map.getActiveView().getExtent().getWidth()/SNAP_TOL_FACTOR);
		// get available snappable layers from map and update draw dialg
		drawdialog.setSnapableLayers(map.getSnappableLayers());
		// update snap tolerance of draw dialog
		drawdialog.setSnapTolerance(snapping.getSnapTolerance());
	}

	private void draw() throws IOException, AutomationException {
		
		if (isActive) {
			
			// is type defined?
			if(drawFeatureType!=null){ 
				// do work depending on type of feature
				if(drawFeatureType==DrawFeatureType.DRAW_FEATURE_POINT) {
					// forward
					drawPoint();
				}
				else {
					drawLine();
				}
			}
		}
	}
	
	private void drawPoint() throws IOException, AutomationException {
		
		// get screen display and start drawing on it
		IScreenDisplay screenDisplay = map.getActiveView().getScreenDisplay();
		screenDisplay.startDrawing(screenDisplay.getHDC(),(short) esriScreenCache.esriNoScreenCache);

		// draw in screen display
		if (p != null && !isMoving) {
			screenDisplay.setSymbol(markerSymbol);
			screenDisplay.drawPoint(p);
		}

		// notify that drawing is finished
		screenDisplay.finishDrawing();
		
	}

	private void drawLine() throws IOException, AutomationException {
		
		// get screen display and start drawing on it
		IScreenDisplay screenDisplay = map.getActiveView().getScreenDisplay();
		screenDisplay.startDrawing(screenDisplay.getHDC(),(short) esriScreenCache.esriNoScreenCache);

		// get current lines
		Polyline path = pathGeometry!=null ? (Polyline)pathGeometry.esri_clone() : null;
		Polyline rubber = rubberBand!=null ? (Polyline)rubberBand.esri_clone() : null;
		
		// create polygon?
		if(drawFeatureType == DrawFeatureType.DRAW_FEATURE_POLYGON && !isBuffered) {				
			if(path!=null && rubber!=null) {
				Point from = (Point)path.getFromPoint();
				rubber.addPoint((IPoint)from.esri_clone(), null, null);
			}
		}
		
		// draw in screen display
		if (path != null && !isMoving) {
			screenDisplay.setSymbol(lineSymbol);
			screenDisplay.drawPolyline(path);
		}
		if (rubber != null) {
			screenDisplay.setSymbol(lineSymbol);
			screenDisplay.drawPolyline(rubber);
		}
		if (snapGeometry != null) {
			screenDisplay.setSymbol(flashSymbol);
			screenDisplay.drawPolyline(snapGeometry);
		}

		// notify that drawing is finished
		screenDisplay.finishDrawing();
		
	}
	
	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
	
	protected boolean doFinishWork() {
		try {
			// create work
			DrawWork work = new DrawWork();
			if(isWorkPoolMode) {
				// schedule on work pool thread
				DiskoWorkPool.getInstance().schedule(work);
			}
			else {
				// do work on this thread
				work.run();
			}
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
		
	/**
	 * Class implementing the IMapControlEvents2Adapter intefaces
	 * 
	 * Is used to catch events that is used to draw the tool 
	 * geometries on the map
	 * 
	 *
	 */
	class MapControlAdapter extends IMapControlEvents2Adapter {

		private static final long serialVersionUID = 1L;

		@Override
		public void onAfterScreenDraw(IMapControlEvents2OnAfterScreenDrawEvent arg0)
				throws IOException, AutomationException {
			draw();
		}

	}

	/**
	 * DiskoWork class for scheduling work in the disko woork pool.
	 * 
	 * The work is converting tool geometry to map features and Mso data
	 * 
	 * @author kennetgu
	 *
	 * The class is extending the class AbstractToolWork 
	 *
	 */
	class DrawWork extends AbstractToolWork<Boolean> {

		DrawWork() throws Exception {
			// notify progress monitor
			super(true);
		}
		
		/**
		 * Worker 
		 * 
		 * Executed on the WORKER thread in the disko work pool
		 * 
		 * Does the actual convertion of draw geometry to mso model data. The 
		 * mso update events will in turn update the appropriate IMsoFeature
		 * layers
		 */	   
		@Override
		public Boolean doWork() {
			
			// has been deleted?
			if(msoObject!=null && msoObject.hasBeenDeleted()) 
				doInit();
			
			// is type defined?
			if(drawFeatureType!=null){ 
				// do work depending on type of feature
				if(drawFeatureType==
						DrawFeatureType.DRAW_FEATURE_POLYLINE) {
					// forward
					return doPolylineWork();
				}
				else if(drawFeatureType
						==DrawFeatureType.DRAW_FEATURE_POINT) {
					// forward
					return doPointWork();
				}
				else if(drawFeatureType
						==DrawFeatureType.DRAW_FEATURE_POLYGON) {
					// forward
					return doPolygonWork();
				}			
			}
			// failed
			return false;
		}

		private boolean doPolygonWork() {
			
			boolean workDone = true;
			
			try {
				
				// reset current mso object?
				if(!isUpdateMode && !doSnapTo)
					doInit();
				
				// get current path and simplify it
				Polyline polyline = pathGeometry;
				polyline.setSpatialReferenceByRef(map.getSpatialReference());
				
				// do snapping?
				if(snapping!=null && doSnapTo) {
					boolean flag = snapping.isSnapToMode();
					snapping.setSnapToMode(true);
					polyline = snapping.doSnapTo(polyline);
					snapping.setSnapToMode(flag);
				}
				
				// cast to polygon
				Polygon polygon = MapUtil.getPolygon(polyline);
				polygon.setSpatialReferenceByRef(map.getSpatialReference());
				
				// get command post
				ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
	
				// dispatch the mso draw data
				if (msoClassCode == MsoClassCode.CLASSCODE_OPERATIONAREA) {
					// create or update operation area?
					if (msoObject == null) {
						// create new operation area
						IOperationAreaListIf opAreaList = cmdPost.getOperationAreaList();
						msoObject = opAreaList.createOperationArea();
					}			
					// update the operation area geodata
					((IOperationAreaIf)msoObject).setGeodata(MapUtil.getMsoPolygon(polygon));
				}
				else if (msoClassCode == MsoClassCode.CLASSCODE_SEARCHAREA) {
					// create or update search area?
					if (msoObject == null) {
						// create search area
						ISearchAreaListIf searchAreaList = cmdPost.getSearchAreaList();
						msoObject = searchAreaList.createSearchArea();
					}			
					// update search area geodata
					((ISearchAreaIf)msoObject).setGeodata(MapUtil.getMsoPolygon(polygon));
				}
				else if (msoClassCode == MsoClassCode.CLASSCODE_ROUTE) {
					// create or update area?
					if(msoOwner == null) {
						// create owner area
						IAreaListIf areaList = cmdPost.getAreaList();
						msoOwner = areaList.createArea(true);				
						IAssignmentListIf assignmentList = cmdPost.getAssignmentList();
						ISearchIf search = assignmentList.createSearch();
						search.setPlannedAccuracy(50);
						search.setPlannedPersonnel(3);
						search.setPriority(AssignmentPriority.NORMAL);
						search.setStatus(AssignmentStatus.DRAFT);
						search.setPlannedArea((IAreaIf)msoOwner);
						// forward
						doPrepare();
					}
					// cast owner area to interface IAreaIf
		            IAreaIf area = (IAreaIf)msoOwner;
		            // add new route to owner area?
					if (msoObject == null) {
						IRouteListIf routeList = cmdPost.getRouteList();
						Route route = MapUtil.getMsoRoute(polyline);
						route.setLayout(getLayout(true));
						msoObject = routeList.createRoute(route);
						// add new route to area
						area.addAreaGeodata((IRouteIf)msoObject);
					}
					else {
						// replace current route with new route (update)
						((IRouteIf)msoObject).setGeodata(MapUtil.getMsoRoute(polyline));
					}
				}
				else {
					// mso draw data is not supported
					workDone = false;
					// notify
					Toolkit.getDefaultToolkit().beep();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			// do preparation?
			if(workDone)
				doPrepare();
			
			// finished
			return workDone;
			
		}
		
		private boolean doPolylineWork() {
			
			boolean workDone = true;
			
			try {
				
				// reset current mso object?
				if(!isUpdateMode && !doSnapTo)
					doInit();
				
				// get current path and simplify it
				Polyline polyline = pathGeometry;
				polyline.setSpatialReferenceByRef(map.getSpatialReference());
				
				// do snapping?
				if(snapping!=null && doSnapTo) {
					boolean flag = snapping.isSnapToMode();
					snapping.setSnapToMode(true);
					polyline = snapping.doSnapTo(polyline);
					snapping.setSnapToMode(flag);
				}
				
				// get command post
				ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
	
				// only route is 
				if (msoClassCode == MsoClassCode.CLASSCODE_ROUTE) {
					// create or update area
					if(msoOwner == null) {
						// create owner area
						IAreaListIf areaList = cmdPost.getAreaList();
						msoOwner = areaList.createArea(true);				
						IAssignmentListIf assignmentList = cmdPost.getAssignmentList();
						ISearchIf search = assignmentList.createSearch();
						search.setPlannedAccuracy(50);
						search.setPlannedPersonnel(3);
						search.setPriority(AssignmentPriority.NORMAL);
						search.setStatus(AssignmentStatus.DRAFT);
						search.setPlannedArea((IAreaIf)msoOwner);
					}
					// cast owner area to interface IAreaIf
		            IAreaIf area = (IAreaIf)msoOwner;
		            // add new route to owner area?
					if (msoObject == null) {
						IRouteListIf routeList = cmdPost.getRouteList();
						Route route = MapUtil.getMsoRoute(polyline);
						route.setLayout(getLayout(false));
						msoObject = routeList.createRoute(route);
						// add new route to area
						area.addAreaGeodata((IRouteIf)msoObject);
					}
					else {
						// get new route
						Route route = MapUtil.getMsoRoute(polyline);
						route.setLayout(getLayout(false));
						// replace current route with new route (update)
						((IRouteIf)msoObject).setGeodata(route);
					}
		            // update start and stop poi of the owner area
		            MsoUtils.updateAreaPOIs(map,area); 
				}
				else {
					// mso draw data is not supported
					workDone = false;
					// notify
					Toolkit.getDefaultToolkit().beep();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			// do preparation?
			if(workDone)
				doPrepare();
			
			// finished
			return workDone;
			
		}		
		
		private boolean doPointWork() {
			
			try {

				// reset current mso object?
				if(!isUpdateMode && !doSnapTo)
					doInit();
				
				// do snapping?
				if(snapping!=null && doSnapTo) {
					boolean flag = snapping.isSnapToMode();
					snapping.setSnapToMode(true);
					p = snapping.doSnapTo(p);
					snapping.setSnapToMode(flag);
				}
				
				// move or add?
				if(msoObject==null) {
					
					// dispatch the mso draw data
					if (msoClassCode != MsoClassCode.CLASSCODE_UNIT) {
					
						// get command post
						ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
						
						// get poi list
						IPOIListIf poiList = cmdPost.getPOIList();
						
						// add to global list
						msoObject = poiList.createPOI();
						
						// cast to IPOIIf
						IPOIIf poi = (IPOIIf)msoObject;
						
						// update
						poi.setPosition(MapUtil.getMsoPosistion(p));
						
						// dispatch the mso draw data
						if (msoClassCode == MsoClassCode.CLASSCODE_ROUTE) {
							
							// forward to get poi type
							doPrepare();
							
							// get poi type
							POIType poiType = poi.getType();
							
							// get flag
							boolean isAreaPOI = (poiType == IPOIIf.POIType.START) || 
								(poiType == IPOIIf.POIType.VIA) || (poiType == IPOIIf.POIType.STOP);
							
							// add to area poi list?
							if(isAreaPOI) {
								
								// create owner?
								if(msoOwner==null) {
									IAreaListIf areaList = cmdPost.getAreaList();
									msoOwner = areaList.createArea(true);
									IAssignmentListIf assignmentList = cmdPost.getAssignmentList();
									ISearchIf search = assignmentList.createSearch();
									search.setPlannedAccuracy(50);
									search.setPlannedPersonnel(3);
									search.setPriority(AssignmentPriority.NORMAL);
									search.setStatus(AssignmentStatus.DRAFT);
									try {
										search.setPlannedArea((IAreaIf)msoOwner);
									}
									catch(Exception e) {
										e.printStackTrace();
									}
								}
								// get area
								IAreaIf area = (IAreaIf)msoOwner;
								area.getAreaPOIs().add(poi);								
							}
						}
						// forward
						doPrepare();
						// success
						return true;
					}
				}
				else {

					// dispatch the mso draw data
					if (msoClassCode != MsoClassCode.CLASSCODE_UNIT) {
						// cast to IPOIIf
						IPOIIf poi = (IPOIIf)msoObject;	
						// update 
						poi.setPosition(MapUtil.getMsoPosistion(p));
					}
					else {
						// cast to unit
						IUnitIf msoUnit = (IUnitIf)msoObject;
						// update
						msoUnit.setPosition(MapUtil.getMsoPosistion(p));					
					}
					
					// forward
					doPrepare();

					// success
					return true;
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// failed
			return false;
		}
				
		private String getLayout(boolean isPolygon) throws AutomationException, IOException {
			String layout = "";
			layout = "isPolygon="+isPolygon+"&";
			return layout;
		}
			
		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread
		 * 
		 */
		@Override
		public void done() {		
			
			// get result
			boolean workDone = (Boolean)get();
			
			// reset tool
			reset();
			
			// forward to super
			super.done();
			
			// is type defined?
			if(drawFeatureType!=null){ 
				// do work depending on type of feature
				if(drawFeatureType==
						DrawFeatureType.DRAW_FEATURE_POLYLINE) {
					// forward
					polylineWorkDone();
				}
				else if(drawFeatureType
						==DrawFeatureType.DRAW_FEATURE_POINT) {
					// forward
					pointWorkDone();
				}
				else if(drawFeatureType
						==DrawFeatureType.DRAW_FEATURE_POLYGON) {
					// forward
					polygonWorkDone();
				}
			}
			
			// notify disko work listeners?
			if(workDone) 
				fireOnWorkChange();			
			
		}		
		
		private void polylineWorkDone() {
			try{

				// work clean up
				refresh();
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}						
		}
		
		private void polygonWorkDone() {
			try{
				
				// work clean up
				refresh();				
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}						
		}
		
		private void pointWorkDone() {
			
			try {
				
				// work clean up
				refresh();
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}						
		}
	}
	
	/**
	 * Abstract tool state class
	 * 
	 * @author kennetgu
	 *
	 */
	public class DrawToolState extends AbstractDiskoTool.DiskoToolState {

		// flags
		private boolean isActive = false;
		private boolean isDrawing = false;
		private boolean isMoving = false;
		private boolean isBuffered = false;
		private boolean isPending = false;
		private boolean isUpdateMode = false;
		private boolean isConstrainMode = false;
		private boolean isWorkPoolMode = false;
		
		// constrain attributes
		private int maxStep = 250;
		private int minStep = 50;

		// holds draw geometry
		private Polyline pathGeometry = null;
		private Polyline rubberBand = null;
		private IGeometry snapGeometry = null;

		// counters
		private int moveCount = 0;

		// last invalid area
		InvalidArea lastInvalidArea = null;
		
		// create state
		public DrawToolState(AbstractDrawTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}
		
		public void save(AbstractDrawTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.isActive = tool.isActive;
			this.isDrawing = tool.isDrawing;
			this.isMoving = tool.isMoving;
			this.isBuffered = tool.isBuffered;
			this.isPending = tool.isPending;
			this.isUpdateMode = tool.isUpdateMode;
			this.isConstrainMode = tool.isConstrainMode;
			this.isWorkPoolMode = tool.isWorkPoolMode;
			this.minStep = tool.minStep;
			this.maxStep = tool.maxStep;
			this.moveCount = tool.moveCount;
			this.pathGeometry = tool.pathGeometry;
			this.rubberBand = tool.rubberBand;
			this.snapGeometry = tool.snapGeometry;
			this.lastInvalidArea = tool.lastInvalidArea;
			
		}
		
		public void load(AbstractDrawTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.isActive = this.isActive;
			tool.isDrawing = this.isDrawing;
			tool.isMoving = this.isMoving;
			tool.isBuffered = this.isBuffered;
			tool.isPending = this.isPending;
			tool.isUpdateMode = this.isUpdateMode;
			tool.isConstrainMode = this.isConstrainMode;
			tool.isWorkPoolMode = this.isWorkPoolMode;
			tool.minStep = this.minStep;
			tool.maxStep = this.maxStep;
			tool.moveCount = this.moveCount;
			tool.pathGeometry = this.pathGeometry;
			tool.rubberBand = this.rubberBand;
			tool.snapGeometry = this.snapGeometry;
			tool.lastInvalidArea= this.lastInvalidArea;
			
		}
	}	
}
