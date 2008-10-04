package org.redcross.sar.map.tool;
 
import java.awt.Event;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.SwingUtilities;

import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.DrawFrame;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.ToolEvent.ToolEventType;
import org.redcross.sar.mso.MsoModelImpl;
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
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentPriority;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

import com.esri.arcgis.carto.InvalidArea;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnAfterScreenDrawEvent;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.display.RgbColor;
import com.esri.arcgis.display.SimpleLineSymbol;
import com.esri.arcgis.display.SimpleMarkerSymbol;
import com.esri.arcgis.display.esriScreenCache;
import com.esri.arcgis.display.esriSimpleMarkerStyle;
import com.esri.arcgis.geometry.IEnvelope;
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
public abstract class AbstractMsoDrawTool extends AbstractMsoTool implements IDrawTool {

	private static final long serialVersionUID = 1L;
	private static final double FONT_SIZE = 12;

	protected enum DrawAction {
		ACTION_BEGIN,
		ACTION_CHANGE,
		ACTION_FINISH,
		ACTION_CANCEL,
		ACTION_DISCARD
	}
	
	// private flags
	private boolean isDrawing = false;			// true:=drawing in progress
	private boolean isMoving = false;			// true:=traced mouse move in progress
	private boolean isRubberInUse = false;		// true:=rubber geometry is active
	private boolean isConstrainMode = true;		// true:=limit line length to [min,max]
	private boolean isWorkPoolMode = true;		// true:=execute work in work pool
	
	// temporary flags
	private boolean doSnapTo = false;			// true:=force a snap operation on active draw geometry
	private boolean isBatchUpdate = true;		// true:=a batch is executing, this inhibit setGeometries 
	private boolean isMouseOverIcon = false;	// true:=mouse is over icon
	private boolean isSnapToMode = false;		// true:=if snapping is available, snapping is enabled every time the tool is activated

	
	// protected flags
	protected boolean isShowDrawFrame = false;	// true:= an draw frame is shown
	protected boolean isContinued = false;		// true:= continue on finished object

	// state
	protected DrawMode drawMode = DrawMode.MODE_CREATE;
	
	// constrain attributes
	protected int minStep = 10;
	protected int maxStep = 100;

	// gesture constants
	protected DrawAction onMouseDownAction;
	protected DrawAction onMouseMoveAction;
	protected DrawAction onMouseUpAction;	
	protected DrawAction onClickAction;	
	protected DrawAction onDblClickAction;	
	
	// draw feature constant
	protected FeatureType featureType = null;
	
	// counters
	protected int moveCount = 0;
	protected long previous = 0;
	
	// point buffers
	protected Point p = null;
		
	// holds draw geometry
	protected Polyline geoPath;
	protected Point geoPoint;
	protected Polyline geoRubber;
	protected IGeometry geoSnap;
		
	// adapters
	protected MsoDrawAdapter drawAdapter;
	protected SnapAdapter snapAdapter;
	protected MapControlAdapter mapAdapter;
	
	// some draw information used to ensure that old draw 
	// geometries are removed from the screen
	protected InvalidArea lastInvalidArea;
	
	// draw symbols
	protected SimpleMarkerSymbol markerSymbol;
	protected SimpleLineSymbol pathSymbol;
	protected SimpleLineSymbol snapSymbol;	

	// elements
	protected DrawFrame drawFrame = null;	
	
	
	/**
	 * Constructs the DrawTool
	 */
	public AbstractMsoDrawTool(boolean isRubberInUse, 
			FeatureType featureType) throws IOException {
		
		// forward
		super();
		
		// set cursor
		cursorPath = "cursors/create.cur";
		
		// create the symbol to draw with
		markerSymbol = new SimpleMarkerSymbol();
		RgbColor markerColor = new RgbColor();
		markerColor.setRed(255);
		markerColor.setGreen(128);
		markerSymbol.setColor(markerColor);
		markerSymbol.setStyle(esriSimpleMarkerStyle.esriSMSCross);
		markerSymbol.setSize(FONT_SIZE);
		
		// create the symbol to draw with
		pathSymbol = new SimpleLineSymbol();
		RgbColor lineColor = new RgbColor();
		lineColor.setRed(255);
		lineColor.setGreen(128);
		pathSymbol.setColor(lineColor);
		pathSymbol.setWidth(2);

		// create symbol to indicate snapping
		snapSymbol = new SimpleLineSymbol();
		RgbColor snapColor = new RgbColor();
		snapColor.setGreen(255);
		snapColor.setBlue(255);
		snapSymbol.setColor(snapColor);
		snapSymbol.setWidth(2);

		// discard all operations
		onClickAction = DrawAction.ACTION_DISCARD;
		onDblClickAction = DrawAction.ACTION_DISCARD;
		onMouseDownAction = DrawAction.ACTION_DISCARD;
		onMouseMoveAction = DrawAction.ACTION_DISCARD;
		onMouseUpAction = DrawAction.ACTION_DISCARD;
		
		// set draw type and rubber flag
		this.isRubberInUse = isRubberInUse;
		this.featureType = featureType;
		
		// set flags
		this.showDirect = false;
		
		// create button
		button = DiskoButtonFactory.createToggleButton(buttonSize);

		// add show dialog listener
		button.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				// double click?
				if(e.getClickCount() > 1) {
					if(dialog!=null) dialog.setVisible(!dialog.isVisible());
				}
			}

			public void mousePressed(MouseEvent e) {
				// start show/hide
				if(dialog!=null) dialog.delayedSetVisible(!dialog.isVisible(), 250);				
			}

			public void mouseReleased(MouseEvent e) {
				// stop show if not shown already
				if(dialog!=null) dialog.cancelSetVisible();				
			}
			
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}

		});
				
		// create map control adapter
		mapAdapter = new MapControlAdapter();
		
	}

	/* ==================================================
	 * Public methods (override with care) 
	 * ==================================================
	 */
	
	public boolean isDrawing() {
		return isDrawing;
	}
	
	public Point getPoint() {
		return geoPoint;
	}
	
	public boolean setPoint(Point p) {
		return setPoint(p,false);
	}
	
	public boolean setPoint(Point p, boolean isDirty) {
		try {
			//System.out.println(p!=null ? MapUtil.getMGRSfromPoint(p) : "null");
			// is valid?
			if(p!=null && !p.isEmpty()) {
				// update 
				this.p = p;
				this.geoPoint = p;				
				// forward
				setDirty(isDirty);
				// forward?
				if (!prepareDrawFrame() && isGeometriesDrawn())
					refresh();
				// finished
				return true;
			}
			else {
				// replace
				this.p = null;
				this.geoPoint = null;				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		// failed
		return false;
	}
	
	public boolean setPointFromMap() {
		return setPointFromMap(false);
	}	
	
	public boolean setPointFromMap(boolean isDirty) {
		return setPoint(map.getClickPoint(),isDirty);
	}	
	
	/* =========================================================
	 * Overriden AbstractDiskoTool methods (override with care) 
	 * =========================================================
	 */
	
	@Override
	public boolean load(IMapToolState state) {
		// valid state?
		if(state instanceof DrawToolState) {
			((DrawToolState)state).load(this);
			return true;
		}
		return false;
	}
	
	@Override
	public Object getAttribute(String attribute) {
		if("GETDRAWMODE".equalsIgnoreCase(attribute)) {
			return drawMode;
		}
		return super.getAttribute(attribute);
	}

	@Override
	public void setAttribute(Object value, String attribute) {
		super.setAttribute(value, attribute);
		if("SETDRAWMODE".equalsIgnoreCase(attribute)) {
			setDrawMode((DrawMode)value); return;
		}
	}

	@Override
	public boolean activate(int options) {
		// forward
		boolean flag = super.activate(options);
		try {
			// allowed?
			if(flag) {
				// update modes in panel
				getToolPanel().update();		
				// forward?
				if(!isDirty()) setGeometries();
				// turn on snapping?
				if(snapAdapter!=null) {
					// forward
					snapAdapter.setSnapToMode(isSnapToMode);
				}
			}
			// forward
			refresh();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return state
		return flag;
	}
	
	@Override
	public boolean deactivate(){
		
		// forward?
		if(isHosted()) getHostTool().deactivate();
		
		// forward
		super.deactivate();
				
		// forward
		try {
			refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// turn off snapping?
		if(snapAdapter!=null) {
			try {
				// forward
				snapAdapter.setSnapToMode(false);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// forward		
		return true;
		
	}
		
	/* =========================================================
	 * Overriden ICommand interface methods (override with care) 
	 * =========================================================
	 */
	
	@Override
	public int getCursor() {
		// show default?
		if(isMouseOverIcon && !isDrawing())
			return 0;
		else 
			return super.getCursor();
	}
	
	public void onCreate(Object obj) {
		
		// is working?
		if(isWorking()) return;
		
		try {

			// only a DiskoMap object is accepted
			if (obj instanceof DiskoMap) {	
				
				// unregister?
				if(map!=null) {
					map.removeIMapControlEvents2Listener(mapAdapter);					
				}
								
				// initialize map object
				map = (DiskoMap)obj;
				
				// register map in draw dialog?
				if(dialog instanceof IDrawToolCollection && !isHosted()) {
					((IDrawToolCollection)dialog).register(map);
				}
								
				// set marked button
				if(button!=null && button.getIcon() instanceof DiskoIcon) {
					DiskoIcon icon = (DiskoIcon)button.getIcon();
					icon.setMarked(true);
				}
				
				// add listener to used to draw tool geometries
				map.addIMapControlEvents2Listener(mapAdapter);
				
				// get edit support from map?
				if(map.isEditSupportInstalled()) {
				
					// get draw frame
					drawFrame = map.getDrawFrame();
					
					// get draw adapter
					drawAdapter = map.getDrawAdapter();
		
					// get snap adapter
					snapAdapter = map.getSnapAdapter();
				
				}
								
			}
			
			// reset flags
			isMoving=false;

			// forward
			super.onCreate(obj);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick() {
		// forward to extenders
		onAction(onClickAction);
	}

	/* =========================================================
	 * Overriden ITool interface methods (override with care) 
	 * =========================================================
	 */
	
	@Override
	public void onDblClick() {
		// forward to extender
		onAction(onDblClickAction);		
	}	
	
	@Override
	public void onMouseDown(int button, int shift, int x, int y) {
		try {
			// get position in map units
			p = toMapPoint(x,y);
			// set focus on button?
			requestFocustOnButton();
			// forward to draw adapter?
			if(drawAdapter==null || !drawAdapter.onMouseDown(button,shift,p)){
				// do snapping
				p = snapTo(p);
				// forward to extenders
				onAction(onMouseDownAction, button, shift, x, y);			
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMouseMove(int button, int shift, int x, int y) {

		moveCount++;
		
		if(moveCount<2) return;
		
		moveCount = 0;
		
		// is moving
		isMoving = true;
		
		try {
			
			// get screen-to-map transformation and try to snap
			p = toMapPoint(x,y);
			
			// get flag
			isMouseOverIcon = isShowDrawFrame;			
			if(isMouseOverIcon && drawFrame!=null && p!=null && !p.isEmpty()) { 
				isMouseOverIcon = drawFrame.hitIcon(p.getX(), p.getY(), 1)!=null;
			}
			
			// get screen-to-map transformation and try to snap
			p = snapTo(p);
			
			// only forward to extenders of this class if 
			// drawing or mouse not over draw frame icon
			if(!isMouseOverIcon || isDrawing)
				onAction(onMouseMoveAction,button,shift,x,y);		
			else {
				// force a refresh?
				boolean bFlag = geoSnap != null; 
				// reset snap geometry
				geoSnap = null;
				// force a refresh?
				if(bFlag || isSnapToMode()) refresh();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// is not moving any more
		isMoving = false;
		
	}

	@Override
	public void onMouseUp(int button, int shift, int x, int y) {
		try {
			// get position in map units
			p = toMapPoint(x,y);
			// forward to draw adapter?
			if(drawAdapter==null || !drawAdapter.onMouseUp(button,shift,p)){
				// do snapping
				p = snapTo(p);
				// forward
				onAction(onMouseUpAction, button, shift, x, y);
				// reset snap geometry
				geoSnap = null;
				// force a refresh
				refresh();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onKeyDown(int keyCode , int shift) {
		
		if(!isActive || isWorking()) return;
		
		// cancel drawing?
		if(keyCode == Event.ESCAPE) {
			onAction(DrawAction.ACTION_CANCEL, 0, 0, 0, 0);
		}
	}
			
	/* ==================================================
	 * Implementation of IDrawTool (override with care)
	 * ==================================================
	 */
	
	public FeatureType getFeatureType() {
		return featureType;
	}
	
	public int getMaxStep() {
		return maxStep;
	}

	public void setMaxStep(int distance) {
		// get flag
		boolean isDirty = isDirty() || (distance!=maxStep);
		// prepare
		maxStep = distance;
		// forward
		setDirty(isDirty);
	}

	public int getMinStep() {
		return minStep;
	}

	public void setMinStep(int distance) {
		// get flag
		boolean isDirty = isDirty() || (distance!=minStep);
		// prepare
		minStep = distance;
		// forward
		setDirty(isDirty);
	}
		
	public boolean isConstrainMode() {
		return isConstrainMode;
	}

	public void setConstrainMode(boolean isConstrainMode) {
		// get flag
		boolean isDirty = isDirty() || (this.isConstrainMode!=isConstrainMode);
		// prepare
		this.isConstrainMode = isConstrainMode;
		// forward
		setDirty(isDirty);
	}

	public boolean isShowDrawFrame() {
		return isShowDrawFrame;
	}

	public void setShowDrawFrame(boolean isShowDrawFrame) {
		// get flag
		boolean bFlag = isShowDrawFrame && map.isEditSupportInstalled();
		try {
			// any change?
			if(this.isShowDrawFrame != bFlag) {
				// prepare
				this.isShowDrawFrame = bFlag;
				// update both draw frame and geometries? 
				if(isShowDrawFrame && drawAdapter!=null) {
					// re-apply mso frame;
					drawAdapter.setMsoFrame();
					// forward
					drawAdapter.setFrameUnion(getGeoEnvelope());
					// forward
					drawAdapter.prepareFrame(true);
				}
				// draw geometries only
				refresh();	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isWorkPoolMode() {
		return isWorkPoolMode;
	}

	public boolean setWorkPoolMode(boolean isWorkPoolMode) {
		boolean bFlag = this.isWorkPoolMode; 
		this.isWorkPoolMode = isWorkPoolMode;
		return bFlag;
	}
		
	public void setSnapTolerance(double tolerance) throws IOException, AutomationException {
		if(snapAdapter!=null)
			snapAdapter.setSnapTolerance(tolerance);
	}

	public double getSnapTolerance() throws IOException {
		if(snapAdapter!=null)
			return snapAdapter.getSnapTolerance();
		return 100;
	}

	public void setMsoData(IMsoObjectIf msoOwn, IMsoObjectIf msoObj, MsoClassCode msoClassCode) {
		
		// is working?
		if(isWorking()) return;
		
		// only IAreaIf is a valid owner
		if(!(msoOwn instanceof IAreaIf)) msoOwn = null;			
		
		// is drawing point?
		if(featureType==FeatureType.FEATURE_POINT) {
			// get point flag
			boolean supportsPoint = msoObj!=null &&
				(MsoClassCode.CLASSCODE_POI.equals(msoObj.getMsoClassCode()) || 
				 MsoClassCode.CLASSCODE_UNIT.equals(msoObj.getMsoClassCode())); 
			// point not supported?
			if(msoObj!=null && !supportsPoint) msoObj=null; // invalid object reference
		}
		else {
			// get line support flag
			boolean supportsLine = msoObj!=null &&
				(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(msoObj.getMsoClassCode()) ||
						MsoClassCode.CLASSCODE_SEARCHAREA.equals(msoObj.getMsoClassCode()) ||
						MsoClassCode.CLASSCODE_ROUTE.equals(msoObj.getMsoClassCode())); 
			// point not supported?
			if(msoObj!=null && !supportsLine) msoObj=null; // invalid object reference
		}
		// set mso owner object
		this.msoOwner = msoOwn;
		// set mso object
		this.msoObject = msoObj;
		// set mso object
		this.msoCode = msoClassCode;
		// forward?
		if(!(isBatchUpdate || isWorking())) setGeometries();
	}

	public SnapAdapter getSnapAdapter() {
		return snapAdapter;
	}
	
	public boolean doSnapTo() {
		
		// is not allowed?
		if(snapAdapter==null) return false;

		// get geometries from mso?
		if(geoPath==null) setGeometries();
					
		// set flags
		doSnapTo = geoPath!=null;
		
		// forward
		return finish(doSnapTo);
			
	}
	
	public boolean isBatchUpdate() {
		return isBatchUpdate;
	}
	
	public void setBatchUpdate(boolean isBatchUpdate) {
		// any change?
		if(this.isBatchUpdate != isBatchUpdate) {
			// prepare
			this.isBatchUpdate = isBatchUpdate;
			// apply geometries?
			if(!(isBatchUpdate || isWorking())) setGeometries();
		}
	}
	
	public MsoDrawAdapter getDrawAdapter() {
		return drawAdapter;
	}
	
	/**
	 * Get the geometry from current mso geodata object
	 *
	 */	
	private boolean setGeometries() {
			
		// initialize flag
		boolean isDirty = (geoPath!=null || geoPoint!=null);
			
		try {
			
			// continue on existing geometries?
			if(isActive) {
				
				// update drawings?
				if(isDirty) refresh(); 
			
				// reset flag
				isContinued = false;
				
				// reset current draw geometries
				p = null;
				geoPath = null;
				geoPoint = null;
				geoSnap = null;
				geoRubber = null;
			
				// has been deleted?
				if(msoObject!=null && msoObject.hasBeenDeleted()) 
					doMsoInit();
				
				// continue on existing geometries?
				if(isContinueMode()) {
					// is type defined?
					if(featureType!=null){ 
						// do work depending on type of feature
						if(FeatureType.FEATURE_POLYGON.equals(featureType))
							isDirty = setPolygon();
						else if(FeatureType.FEATURE_POLYLINE.equals(featureType))
							isDirty = setPolyline();
						else if(FeatureType.FEATURE_POINT.equals(featureType))
							isDirty = setPoint();
						// set flag?
						isContinued = isDirty;
					}
				}
			}
		
			// update drawings?
			if(isDirty) refresh(); 
		}
		catch(Exception e) {
			e.printStackTrace();
		}
				
		// failed
		return isDirty;
	}
	
	private boolean setPolyline() {
		try {
							
            // mso object exist?
			if(msoObject!=null) {			
				// parse type
				if (MsoClassCode.CLASSCODE_ROUTE.equals(msoObject.getMsoClassCode())) {						
					// get polyline
					geoPath = MapUtil.getEsriPolyline(((IRouteIf)msoObject).getGeodata(),map.getSpatialReference());
				}
				// set flag
				return (geoPath!=null);
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// failed
		return false;
		
	}

	private boolean setPolygon() {
		try {
							
            // get polygon?
			if(msoObject!=null) {
				// dispatch the mso draw data
				if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(msoObject.getMsoClassCode())) {
					// get polygon
					Polygon polygon = MapUtil.getEsriPolygon(((IOperationAreaIf)msoObject).getGeodata(),map.getSpatialReference());
					// get polyline
					geoPath = MapUtil.getPolyline(polygon);
				}
				else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(msoObject.getMsoClassCode())) {
					// get polygon
					Polygon polygon = MapUtil.getEsriPolygon(((ISearchAreaIf)msoObject).getGeodata(),map.getSpatialReference());
					// get polyline
					geoPath = MapUtil.getPolyline(polygon);
				}
				else if (MsoClassCode.CLASSCODE_ROUTE.equals(msoObject.getMsoClassCode())) {
					// get polyline
					geoPath = MapUtil.getEsriPolyline(((IRouteIf)msoObject).getGeodata(),map.getSpatialReference());
				}
				// update rubber?
				if(isRubberInUse) geoRubber = geoPath;
				// set flag
				return (geoPath!=null);
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// failed
		return false;
		
	}
	
	private boolean setPoint() {
		try {
							
            // get polygon?
			if(msoObject!=null) {
				// parse data
				if (MsoClassCode.CLASSCODE_POI.equals(msoObject.getMsoClassCode())) {
					// cast to IPOIIf
					IPOIIf msoPoi = (IPOIIf)msoObject;	
					// get polyline
					p = MapUtil.getEsriPoint(msoPoi.getPosition().getGeoPos(),map.getSpatialReference());
				}
				else if (MsoClassCode.CLASSCODE_UNIT.equals(msoObject.getMsoClassCode())) {
					// cast to unit
					IUnitIf msoUnit = (IUnitIf)msoObject;
					// get polyline
					p = MapUtil.getEsriPoint(msoUnit.getPosition().getGeoPos(),map.getSpatialReference());
				}
				// update geometry point
				geoPoint = p;
				// set flag
				return (p!=null);							
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// failed
		return false;
		
	}	
	
	@Override
	public IMapToolState save() {
		// get new state
		return new DrawToolState(this);
	}

	/**
	 * This method reset current tool. It only affects this tool.
	 */
	public void reset() {
		try {
			// forward
			refresh();
			// rest draw geometries
			p = null;
			geoPath = null;
			geoRubber = null;
			geoPoint = null;
			geoSnap = null;
			// reset flags
			setDirty(false);
			isMoving = false;
			isDrawing = false;
			// forward
			refresh();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}	
	
	public boolean finish(boolean force) {
		// set flag
		setDirty(isDirty || force && (geoPath!=null));
		// forward
		return finish();
	}	
	
	public boolean finish() {
		// forward
		boolean bFlag = doFinishWork();
		// forward
		if (map.isEditSupportInstalled() || bFlag) {
			drawAdapter.finish();
		}
		// finish
		return bFlag;
	}
	
	/**
	 * This method will cancel a started drawing procedure. If edit support is
	 * installed, this will affect all registered draw tools.
	 */
	public boolean cancel() {
		// initialize flag
		boolean bFlag = isDirty;
		try {
			// forward to draw adapter?
			if(map.isEditSupportInstalled()) {
				// forward
				if(drawAdapter.cancel()) {
					// refresh any changes 
					drawAdapter.refreshFrame();				
				}
				// any change? 
				bFlag = (bFlag != isDirty);
			}
			else if(bFlag) {
				// forward
				reset();
				// success!
				bFlag = true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// finished
		return bFlag;
	}
		
	public DrawMode getDrawMode() {
		return drawMode;
	}

	public void setDrawMode(DrawMode mode) {
		// prepare
		drawMode = mode;
	}
		
	public boolean isInterchangable(FeatureType type) {
		if(isDrawingLines(type) && isDrawingLines(featureType))
			return true;
		else if(isDrawingPoints(type) && isDrawingPoints(featureType))
			return true;
		// not supported
		return false;		
	}
	
	private boolean isDrawingLines(FeatureType type) {
		
		return FeatureType.FEATURE_POLYLINE.equals(type) 
				|| FeatureType.FEATURE_POLYGON.equals(type);
	}
	
	public boolean isDrawingPoints(FeatureType type) {
		return FeatureType.FEATURE_POINT.equals(type);
	}
	
	public boolean isCreateMode() {
		return DrawMode.MODE_CREATE.equals(drawMode);
	}
	
	public boolean isReplaceMode() {
		return DrawMode.MODE_REPLACE.equals(drawMode);
	}

	public boolean isAppendMode() {
		return DrawMode.MODE_APPEND.equals(drawMode);
	}
	
	public boolean isContinueMode() {
		return DrawMode.MODE_CONTINUE.equals(drawMode);
	}
	
	public boolean isSnapToMode() {
		return isSnapToMode; 
	}

	public void setSnapToMode(boolean isSnapToMode) {
		// can snap?
		if(snapAdapter!=null) {
			try {
				// update flag
				this.isSnapToMode = isSnapToMode;
				// forward?
				if(isActive())
					snapAdapter.setSnapToMode(isSnapToMode);
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
	 * Protected methods intended for extending classes 
	 * ==================================================
	 */

	/**
	 * This method is rised from either onClick(*) or 
	 * onDblClick(*)
	 *  
	 * @param keycode
	 * @param shift
	 * @return True if begin is a success 
	 */	
	protected boolean onBegin() {
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
	protected boolean onBegin(int button, int shift, int x, int y) {
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
	protected boolean onChange() {
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
	protected boolean onChange(int button, int shift, int x, int y) {
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
	protected boolean onFinish() {
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
	protected boolean onFinish(int button, int shift, int x, int y) {
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
	protected boolean onCancel() {
		return true;
	}
	
	/**
	 * This method clears current mso objects and 
	 * then calls the doPrepare() method
	 * 
	 */
	protected void doMsoInit() {
		// reset
		msoOwner = null;
		msoObject = null;
		// forward
		doPrepare(null,false);		
	}
	
	
	/**
	 * This method is rised after doFinish() is returned with true. 
	 * Mso object updates before finished is completed should be 
	 * done in this method.
	 * 
	 * @param IMsoObjectIf msoObj The mso object to prepare for update
	 * @param boolean isDefined Indicates that this prepare is 
	 * defined in AbstractDrawTool
	 * 
	 * @return True if prepare is a success
	 * 
	 */
	protected boolean doPrepare(IMsoObjectIf msoObj, boolean isDefined) {
		try {
			if(isDefined) {
				if(msoObj instanceof ISearchIf) {
					ISearchIf search = (ISearchIf)msoObj;
					search.setPlannedAccuracy(50);
					search.setPlannedPersonnel(3);
					search.setPriority(AssignmentPriority.NORMAL);
					search.setStatus(AssignmentStatus.DRAFT);
				}
			}
			// success
			return true;
		} catch (IllegalOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failure
		return false;
	}		
	
	/**
	 * Set drawing state flag
	 * 
	 */	
	protected void setIsDrawing(boolean isDrawing) {
		// set flag
		this.isDrawing = isDrawing;
		// update panel
		getToolPanel().update();		
	}
	
	
	/**
	 * Snap to point
	 * 
	 * @throws IOException
	 * @throws AutomationException
	 */	
	protected Point snapTo(Point p) throws IOException, AutomationException {
		// try to snap?
		if(snapAdapter!=null && snapAdapter.isSnapToMode()) {	
			// forward
			p = snapAdapter.doSnapTo(p);
			// update snap geometry
			geoSnap = snapAdapter.getSnapGeometry();			
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
		if(map!=null) {
			boolean bdirty = false;
			InvalidArea invalidArea = new InvalidArea();
			// is type defined?
			if(featureType!=null){ 
				
				// do work depending on type of feature
				if(featureType==FeatureType.FEATURE_POINT) {
					if (geoPoint != null && !isMoving) {
						invalidArea.add(geoPoint);
						bdirty = true;
					}
				}
				else {
					if (geoPath != null && !isMoving) {
						invalidArea.add(geoPath);
						bdirty = true;
					}
					if (geoRubber != null && isRubberInUse) {
						invalidArea.add(geoRubber);
						bdirty = true;
					}
					if (geoSnap != null) {
						invalidArea.add(geoSnap);
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
	
	protected boolean isGeometriesDrawn() {
		boolean bFlag = (featureType!=null) 
			&& (isActive || 
					(isShowDrawFrame 
							&& map.isEditSupportInstalled() 
							&& drawFrame.isActive() 
							&& this == drawAdapter.getSelectedDrawTool()
					)
				);
		return bFlag;
	}

	/* ==================================================
	 * Private methods intended internal use
	 * ==================================================
	 */
	
	private void draw() throws IOException, AutomationException {
		
		// draw geometries?
		if (isGeometriesDrawn()) {
			
			// do work depending on type of feature
			if(featureType==FeatureType.FEATURE_POINT) {
				// forward
				drawPoint();
			}
			else {
				drawLine();
			}
			
		}
		
	}
	
	private void drawPoint() throws IOException, AutomationException {
		
		// draw in screen display
		if (geoPoint != null && !isMoving) {
			
			// get screen display and start drawing on it
			IScreenDisplay screenDisplay = map.getActiveView().getScreenDisplay();
			screenDisplay.startDrawing(screenDisplay.getHDC(),(short) esriScreenCache.esriNoScreenCache);

			screenDisplay.setSymbol(markerSymbol);
			screenDisplay.drawPoint(geoPoint);
			
			// notify that drawing is finished
			screenDisplay.finishDrawing();
		}

		
	}

	private void drawLine() throws IOException, AutomationException {
		
		// get screen display and start drawing on it
		IScreenDisplay screenDisplay = map.getActiveView().getScreenDisplay();
		screenDisplay.startDrawing(screenDisplay.getHDC(),(short) esriScreenCache.esriNoScreenCache);

		// get current lines
		Polyline path = geoPath!=null ? (Polyline)geoPath.esri_clone() : null;
		Polyline rubber = geoRubber!=null ? (Polyline)geoRubber.esri_clone() : null;
		
		// create polygon?
		if(featureType == FeatureType.FEATURE_POLYGON && !isDrawing) {				
			if(path!=null && rubber!=null) {
				Point from = (Point)path.getFromPoint();
				path.addPoint((IPoint)from.esri_clone(), null, null);
			}
		}
		
		// draw in screen display
		if(!isMoving) {
			if (path != null) {
				screenDisplay.setSymbol(pathSymbol);
				screenDisplay.drawPolyline(path);
				// is in continued mode?
				//if(isContinued) {
				//	// get radius
				//	double r = getSnapTolerance()*10;
				//	screenDisplay.drawPolygon(MapUtil.createCircle(path.getFromPoint(), r));
				//	screenDisplay.drawPolygon(MapUtil.createCircle(path.getToPoint(), r));
				//}
			}
			if (geoRubber != null) {
				screenDisplay.setSymbol(pathSymbol);
				screenDisplay.drawPolyline(geoRubber);
			}
			if (geoSnap != null) {
				screenDisplay.setSymbol(snapSymbol);
				screenDisplay.drawPolyline(geoSnap);
			}
		}

		// notify that drawing is finished
		screenDisplay.finishDrawing();
		
	}	
	
	private void onAction(DrawAction action, 
			int button, int shift, int x, int y) {
		if(action.equals(DrawAction.ACTION_BEGIN)) {
			if(beforeBegin()) {
				afterBegin(onBegin(button, shift, x, y));
			}
		}
		else if(action.equals(DrawAction.ACTION_CHANGE)) {
			if(beforeChange()){
				afterChange(onChange(button, shift, x, y));
			}
			// update snapping geometry?
			else if(isSnapToMode()) {
				try {
					refresh();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(action.equals(DrawAction.ACTION_CANCEL)) {
			if(beforeCancel()) {
				afterCancel(onCancel());
			}
		}
		else if(action.equals(DrawAction.ACTION_FINISH)) {
			// validate finish
			if (beforeFinish()) {
				afterFinish(onFinish(button, shift, x, y));
			}
		}
	}
	
	private void onAction(DrawAction action) {
		if(action.equals(DrawAction.ACTION_BEGIN)) {
			if(beforeBegin()) {
				afterBegin(onBegin());
			}
		}
		else if(action.equals(DrawAction.ACTION_CHANGE)) {
			if(beforeChange()){
				afterChange(onChange());
			} 
		}
		else if(action.equals(DrawAction.ACTION_CANCEL)) {
			if(beforeCancel()) {
				afterCancel(onCancel());
			}
		}
		else if(action.equals(DrawAction.ACTION_FINISH)) {
			// validate finish
			if (beforeFinish()) {
				afterFinish(onFinish());
			}
		}
	}
		
	private boolean beforeBegin() {

		// valid operation?
		if(!isActive || isWorking()) return false;
				
		// valid zoom?
		if(!map.isDrawAllowed())  {
			Utils.showWarning("Tegning er kun mulig fra skala 1:" + ((int)map.getMaxDrawScale()) + " og lavere");
			map.showProgressor(true);
			Runnable r = new Runnable() {
				public void run() {
					try {
						map.centerAt(p);
						map.setMapScale(75000);
					} catch (AutomationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					map.hideProgressor();
				}
			};
			SwingUtilities.invokeLater(r);
			return false;
		}
		
		// set as working (prevents reentry)
		setIsWorking();
		
		// notify
		fireToolEvent(ToolEventType.BEGIN_EVENT, 0);		
		
		// valid
		return true;
			
	}

	private void afterBegin(boolean success) {

		// was begin operation successful?
		if(success) {

			try {
	
				// update tool drawings
				refresh();
						
				// is drawing
				setIsDrawing(true);
		
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// notify
			fireToolEvent(ToolEventType.BEGIN_EVENT, 1);
		}
		// reset work flag
		setIsNotWorking();
	}
	
	private boolean beforeChange() {

		// valid operation?
		if(!isActive || !isDrawing || isWorking()) return false;
		
		// set working flag
		setIsWorking();
		
		// notify
		fireToolEvent(ToolEventType.CHANGE_EVENT, 0);		
		
		// valid
		return true;
	}

	private void afterChange(boolean success) {
		// was operation successful?
		if(success) {
			try {				
				// draw the geometries on map 
				refresh();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// notify
			fireOnWorkChange(this, msoObject);
			// notify
			fireToolEvent(ToolEventType.CHANGE_EVENT, 1);		
		}
		// reset working flag
		setIsNotWorking();		
	}
	
	
	private boolean beforeCancel() {
		// valid operation?
		if(!isActive || !isDrawing) return false;
		// set working flag
		setIsWorking();
		// notify
		fireToolEvent(ToolEventType.CANCEL_EVENT, 0);		
		// is valid
		return true;
	}
	
	private void afterCancel(boolean success) {
		// was cancel operation successful?
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
			fireOnWorkCancel(this,msoObject);
			// notify
			fireToolEvent(ToolEventType.CANCEL_EVENT, 1);		
		}
		// reset working flag
		setIsNotWorking();
	}	
	
	private boolean beforeFinish() {
		// valid operation?
		if(!isActive || !isDrawing || isWorking()) return false;
		// set working flag
		setIsWorking();
		// notify
		fireToolEvent(ToolEventType.FINISH_EVENT, 0);		
		// valid
		return true;
	}
	
	private void afterFinish(boolean success) {
		
		// was finish operation successful?
		if(success) {
			
			try {
				
				// set flags
				setIsDrawing(false);
				
				// forward?
				if(isDirty) 
					prepareDrawFrame();
				else
					reset();
				
				// draw geometries only
				refresh();	
				
				// reset last invalid area
				lastInvalidArea = null;
				
				// notify
				fireToolEvent(ToolEventType.FINISH_EVENT, 1);		
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// reset working flag
		setIsNotWorking();
	}
	
	protected boolean prepareDrawFrame() throws AutomationException, IOException {
		// update both draw frame and geometries? 
		if(isShowDrawFrame && drawAdapter!=null) {
			// re-apply MSO frame;
			drawAdapter.setMsoFrame();
			// forward
			drawAdapter.setFrameUnion(getGeoEnvelope());
			// forward
			drawAdapter.prepareFrame(true);
			// finished
			return true;
		}		
		return false;
	}
	
	private IEnvelope getGeoEnvelope() throws AutomationException, IOException {
		if(geoPath!=null) {
			return MapUtil.getConstrainExtent(geoPath.getEnvelope(),map);
		}
		else if(geoPoint!=null){
			return MapUtil.getConstrainExtent(geoPoint, map);
		}
		// failure
		return null;
	}	
	
	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
	
	protected boolean doFinishWork() {
		try {
			// has work pending?
			if(isDirty) {
				// create work
				DrawWork work = new DrawWork();
				// decide on worker
				if(isWorkPoolMode) {
					// schedule on work pool thread
					DiskoWorkPool.getInstance().schedule(work);
				}
				else {
					// do work on this thread
					work.run();
				}
				// assume that work is executed
				setDirty(false);
				// success
				return true;
			}
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
		public void onAfterScreenDraw(IMapControlEvents2OnAfterScreenDrawEvent e)
				throws IOException, AutomationException {
			// forward?
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
			
			// does model exists?
			if(MsoModelImpl.getInstance().getMsoManager().operationExists()) {		
			
				// has been deleted?
				if(msoObject!=null && msoObject.hasBeenDeleted()) 
					doMsoInit();
				
				// is type defined?
				if(featureType!=null){ 
					// do work depending on type of feature
					if(featureType==
							FeatureType.FEATURE_POLYLINE) {
						// forward
						return doPolylineWork();
					}
					else if(featureType
							==FeatureType.FEATURE_POINT) {
						// forward
						return doPointWork();
					}
					else if(featureType
							==FeatureType.FEATURE_POLYGON) {
						// forward
						return doPolygonWork();
					}			
				}
			}
			// failed
			return false;
		}

		private boolean doPolygonWork() {
			
			// assume success
			boolean workDone = true;
			
			try {
				
				// nothing to do?
				if(geoPath==null) return false;
				
				// reset current mso object?
				if(isCreateMode() && !doSnapTo)
					doMsoInit();
				
				// get current path
				Polyline polyline = geoPath;
				polyline.setSpatialReferenceByRef(map.getSpatialReference());
				
				// in constrain mode?
				if(isConstrainMode)
					polyline = MapUtil.densify(polyline, minStep, maxStep);				
				
				// do snapping?
				if(snapAdapter!=null && doSnapTo) {
					polyline = snapAdapter.doSnapTo(polyline);
				}
				
				// cast to polygon
				Polygon polygon = MapUtil.getPolygon(polyline);
				
				// get command post
				ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
	
				// dispatch the mso draw data
				if (msoCode == MsoClassCode.CLASSCODE_OPERATIONAREA) {
					// create or update operation area?
					if (msoObject == null || isCreateMode()) {
						// create new operation area
						IOperationAreaListIf opAreaList = cmdPost.getOperationAreaList();
						msoObject = opAreaList.createOperationArea();
					}			
					// update the operation area geodata
					((IOperationAreaIf)msoObject).setGeodata(MapUtil.getMsoPolygon(polygon));
				}
				else if (msoCode == MsoClassCode.CLASSCODE_SEARCHAREA) {
					// create or update search area?
					if (msoObject == null || isCreateMode()) {
						// create search area
						ISearchAreaListIf searchAreaList = cmdPost.getSearchAreaList();
						msoObject = searchAreaList.createSearchArea();
					}			
					// update search area geodata
					((ISearchAreaIf)msoObject).setGeodata(MapUtil.getMsoPolygon(polygon));
				}
				else if (msoCode == MsoClassCode.CLASSCODE_ROUTE) {
					// create or update area?
					if(msoOwner == null || isCreateMode()) {
						// create owner area
						IAreaListIf areaList = cmdPost.getAreaList();
						msoOwner = areaList.createArea(true);				
						// create search object
						IAssignmentListIf assignmentList = cmdPost.getAssignmentList();
						ISearchIf search = assignmentList.createSearch();
						// forward
						doPrepare(search,true);
						// set planned area
						search.setPlannedArea((IAreaIf)msoOwner);
					}
					// cast owner area to interface IAreaIf
		            IAreaIf area = (IAreaIf)msoOwner;
		            // add new route to owner area?
					if (msoObject == null || isCreateMode() || isAppendMode()) {
						IRouteListIf routeList = cmdPost.getRouteList();
						Route route = MapUtil.getMsoRoute(polyline);
						route.setLayout(getLayout(true));
						msoObject = routeList.createRoute(route);
						// add new route to area
						area.addAreaGeodata((IRouteIf)msoObject);
					}
					else {
						// create new route
						Route route = MapUtil.getMsoRoute(polyline);
						route.setLayout(getLayout(true));
						// replace current route with new route (update)
						((IRouteIf)msoObject).setGeodata(route);
					}
				}
				else {
					// mso draw data is not supported
					workDone = false;
					// notify
					//Toolkit.getDefaultToolkit().beep();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			// forward?
			if(workDone) {
				if(msoOwner!=null) doPrepare(msoOwner,false);
				if(msoObject!=null) doPrepare(msoObject,false);
			}
			
			// finished
			return workDone;
			
		}
		
		private boolean doPolylineWork() {
			
			boolean workDone = true;
			
			try {
				
				// nothing to do?
				if(geoPath==null) return false;
				
				// reset current mso object?
				if(isCreateMode() && !doSnapTo)
					doMsoInit();
				
				// get current path
				Polyline polyline = geoPath;
				polyline.setSpatialReferenceByRef(map.getSpatialReference());
				
				// in constrain mode?
				if(isConstrainMode)
					polyline = MapUtil.densify(polyline, minStep, maxStep);
				
				// do snapping?
				if(snapAdapter!=null && doSnapTo) {
					polyline = snapAdapter.doSnapTo(polyline);
				}
				
				// get command post
				ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
	
				// only route is polyline
				if (msoCode == MsoClassCode.CLASSCODE_ROUTE) {
					// create or update area
					if(msoOwner == null || isCreateMode()) {
						// create owner area
						IAreaListIf areaList = cmdPost.getAreaList();
						msoOwner = areaList.createArea(true);				
						IAssignmentListIf assignmentList = cmdPost.getAssignmentList();
						ISearchIf search = assignmentList.createSearch();
						doPrepare(search,true);
						search.setPlannedArea((IAreaIf)msoOwner);
					}
					// cast owner area to interface IAreaIf
		            IAreaIf area = (IAreaIf)msoOwner;
		            // add new route to owner area?
					if (msoObject == null || isCreateMode() || isAppendMode()) {
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
					//Toolkit.getDefaultToolkit().beep();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			// forward?
			if(workDone) {
				if(msoOwner!=null) doPrepare(msoOwner,false);
				if(msoObject!=null) doPrepare(msoObject,false);
			}
			
			// finished
			return workDone;
			
		}		
		
		private boolean doPointWork() {
			
			try {

				// nothing to do?
				if(geoPoint==null) return false;
				
				// reset current MSO object?
				if(isCreateMode() && !doSnapTo)
					doMsoInit();
				
				// do snapping?
				if(snapAdapter!=null && doSnapTo) {
					boolean flag = snapAdapter.isSnapToMode();
					snapAdapter.setSnapToMode(true);
					geoPoint = snapAdapter.doSnapTo(geoPoint);
					snapAdapter.setSnapToMode(flag);
				}
				
				// move or add?
				if(msoObject==null || isCreateMode()) {
					
					// dispatch the MSO draw data
					if (msoCode == MsoClassCode.CLASSCODE_ROUTE 
							|| msoCode == MsoClassCode.CLASSCODE_POI) {
					
						// get command post
						ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
						
						// get POI list
						IPOIListIf poiList = cmdPost.getPOIList();
						
						// add to global list
						msoObject = poiList.createPOI();
						
						// prepare
						doPrepare(msoObject, false);
						
						// cast to IPOIIf
						IPOIIf poi = (IPOIIf)msoObject;
						
						// update
						poi.setPosition(MapUtil.getMsoPosistion(geoPoint));
						
						// initialize update flag
						boolean bUpdateSeqNum = true;
						
						// is drawing an area?
						if (msoCode == MsoClassCode.CLASSCODE_ROUTE) {
							
							// get POI type
							POIType poiType = poi.getType();
							
							// get flag
							boolean isAreaPOI = IPOIIf.AREA_SET.contains(poiType);
							
							//is an area POI type?
							if(isAreaPOI) {									
								
								// create owner?
								if(msoOwner==null || isCreateMode()) {
									IAreaListIf areaList = cmdPost.getAreaList();
									msoOwner = areaList.createArea(true);
									IAssignmentListIf assignmentList = cmdPost.getAssignmentList();
									ISearchIf search = assignmentList.createSearch();
									search.setPlannedArea((IAreaIf)msoOwner);
									doPrepare(search,true);
								}
								// get area
								IAreaIf area = (IAreaIf)msoOwner;
								area.getAreaPOIs().add(poi);
								// use local sequence number set by area 
								bUpdateSeqNum = false;
							}
						}
						
						// update area sequence number?
						if(bUpdateSeqNum) {
							poi.setAreaSequenceNumber(cmdPost.getPOIList().getNextSequenceNumber(poi.getType()));
						}
						
						// forward?
						if(msoOwner!=null) doPrepare(msoOwner,false);
						
						// success
						return true;
					}
				}
				else {

					// dispatch the MSO draw data
					if (msoCode != MsoClassCode.CLASSCODE_UNIT) {
						// cast to IPOIIf
						IPOIIf poi = (IPOIIf)msoObject;	
						// update 
						poi.setPosition(MapUtil.getMsoPosistion(geoPoint));
					}
					else {
						
						// get attributes
						Calendar logTimeStamp = (Calendar)
								getAttribute("LOGTIMESTAMP");
						boolean bLogPosition = Boolean.valueOf(
								getAttribute("LOGPOSITION").toString());
						TimePos timePos = (TimePos)
								getAttribute("UPDATETRACKPOSITION");
																	
						// cast to unit
						IUnitIf msoUnit = (IUnitIf)msoObject;
						
						// get track
						ITrackIf track = msoUnit.getTrack();
						
						// create track?
						if(track==null && (bLogPosition || timePos!=null)) {
							// get command post
							ICmdPostIf cmdPost = Utils.getApp().getMsoModel()
															   .getMsoManager().getCmdPost();
							// create new track
							track = cmdPost.getTrackList().createTrack();
							// set geodata
							track.setGeodata(new Track(null, null, 1));
							// set track reference in unit
							msoUnit.setTrack(track);
						}
						
						// get position
						Position p = MapUtil.getMsoPosistion(geoPoint);
													
						// initialize update flag
						boolean bUpdatePosition = true;
						
						// update track point?
						if(timePos!=null) {
							// try to find position
							int i = track.getGeodata().find(timePos);
							// found?
							if(i!=-1) {
								// get found position
								TimePos found = track.getGeodata().get(i);
								// only update unit position if logged point equals current position
								bUpdatePosition = found.getGeoPos().equals(msoUnit.getPosition().getGeoPos());
								// update logged position
								track.getGeodata().set(i,p.getPosition());
							}
							else {
								System.out.println("Error! Did not find required track point in log");
							}
						}
						
						// update unit position?
						if(bUpdatePosition)
							msoUnit.setPosition(p);
						
						// log position?
						if(bLogPosition)
							msoUnit.logPosition(logTimeStamp);
												
					}
					
					// forward
					doPrepare(msoObject,false);

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
		public void afterDone() {		
			
			// get result
			boolean isDone = (Boolean)get();
			
			// reset tool
			reset();
			
			// is type defined?
			if(featureType!=null){ 
				// do work depending on type of feature
				if(featureType==
						FeatureType.FEATURE_POLYLINE) {
					// forward
					polylineWorkDone();
				}
				else if(featureType
						==FeatureType.FEATURE_POINT) {
					// forward
					pointWorkDone();
				}
				else if(featureType
						==FeatureType.FEATURE_POLYGON) {
					// forward
					polygonWorkDone();
				}
			}
			
			// forward
			super.afterDone();
			
			// notify?
			if(isDone) fireOnWorkFinish(AbstractMsoDrawTool.this,msoObject);
			
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
	public class DrawToolState extends AbstractMsoTool.MsoToolState {

		// flags
		private boolean isDrawing = false;
		private boolean isMoving = false;
		private boolean isDirty = false;
		private boolean isShowDrawFrame = false;
		private boolean isConstrainMode = false;
		private boolean isWorkPoolMode = false;
		private boolean isContinued = false;

		// state
		private DrawMode drawMode = null;
		
		// constrain attributes
		private int minStep = 10;
		private int maxStep = 100;

		// holds draw geometry
		private Point p = null;
		private Polyline geoPath = null;
		private Polyline geoRubber = null;
		private IGeometry geoSnap = null;

		// counters
		private int moveCount = 0;

		// last invalid area
		InvalidArea lastInvalidArea = null;
		
		// create state
		public DrawToolState(AbstractMsoDrawTool tool) {
			super((AbstractMsoTool)tool);
			save(tool);
		}
		
		public void save(AbstractMsoDrawTool tool) {
			super.save((AbstractMsoTool)tool);			
			this.isDrawing = tool.isDrawing;
			this.isMoving = tool.isMoving;
			this.isShowDrawFrame = tool.isShowDrawFrame;
			this.isDirty = tool.isDirty;
			this.drawMode = tool.drawMode;
			this.isConstrainMode = tool.isConstrainMode;
			this.isWorkPoolMode = tool.isWorkPoolMode;
			this.isContinued = tool.isContinued;
			this.minStep = tool.minStep;
			this.maxStep = tool.maxStep;
			this.moveCount = tool.moveCount;
			this.p = tool.p;
			this.geoPath = tool.geoPath;
			this.geoRubber = tool.geoRubber;
			this.geoSnap = tool.geoSnap;
			this.lastInvalidArea = tool.lastInvalidArea;
		}
		
		public void load(AbstractMsoDrawTool tool) {
			tool.isMoving = this.isMoving;
			tool.isShowDrawFrame = this.isShowDrawFrame;
			tool.drawMode = this.drawMode;
			tool.isConstrainMode = this.isConstrainMode;
			tool.isWorkPoolMode = this.isWorkPoolMode;
			tool.isContinued = this.isContinued;
			tool.minStep = this.minStep;
			tool.maxStep = this.maxStep;
			tool.moveCount = this.moveCount;
			tool.p = this.p;
			tool.geoPath = this.geoPath;
			tool.geoRubber = this.geoRubber;
			tool.geoSnap = this.geoSnap;
			tool.lastInvalidArea= this.lastInvalidArea;
			tool.setDirty(this.isDirty);
			tool.setIsDrawing(this.isDrawing);
			super.load((AbstractMsoTool)tool);
		}
	}	
}
