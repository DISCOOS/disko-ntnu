package org.redcross.sar.map.command;

import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.SwingUtilities;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
import org.redcross.sar.gui.DiskoCustomIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.IDrawToolCollection;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.DrawAdapter;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.SnapAdapter;
import org.redcross.sar.map.element.DrawFrame;
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
public abstract class AbstractDrawTool extends AbstractDiskoTool implements IDrawTool {

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
	private boolean isDirty = false;			// true:=change is pending
	private boolean isConstrainMode = true;		// true:=limit line length to [min,max]
	private boolean isWorkPoolMode = true;		// true:=execute work in work pool
	
	
	// temporary flags
	private boolean doSnapTo = false;			// true:=force a snap operation on active draw geometry
	private boolean isBatchUpdate = true;		// true:=a batch is executing, this inhibit setGeometries 
	private boolean isMouseOverIcon = false;	// true:=mouse is over icon
	
	// protected flags
	protected boolean isShowDrawFrame = false;	// true:= and edit is supported by map, an draw frame is shown
	
	// state
	protected DrawMode drawMode = DrawMode.MODE_CREATE;
	
	// constrain attributes
	protected int minStep = 10;
	protected int maxStep = 100;

	// gesture constants
	protected DrawAction onMouseDownAction = null;
	protected DrawAction onMouseMoveAction = null;
	protected DrawAction onMouseUpAction = null;	
	protected DrawAction onClickAction = null;	
	protected DrawAction onDblClickAction = null;	
	
	// draw feature constant
	protected FeatureType featureType = null;
	
	// counters
	protected int moveCount = 0;
	protected long previous = 0;
	
	// point buffers
	protected Point p = null;
		
	// holds draw geometry
	protected Polyline geoPath = null;
	protected Point geoPoint = null;
	protected Polyline geoRubber = null;
	protected IGeometry geoSnap = null;
		
	// adapters
	protected DrawAdapter drawAdapter = null;
	protected SnapAdapter snapAdapter = null;
	protected MapControlAdapter mapAdapter = null;
	
	// some draw information used to ensure that old draw 
	// geometries are removed from the screen
	protected InvalidArea lastInvalidArea = null;
	
	// draw symbols
	protected SimpleMarkerSymbol markerSymbol = null;
	protected SimpleLineSymbol pathSymbol = null;
	protected SimpleLineSymbol snapSymbol = null;	

	// elements
	protected DrawFrame drawFrame = null;	
	
	/**
	 * Constructs the DrawTool
	 */
	public AbstractDrawTool(boolean isRubberInUse, 
			FeatureType featureType) throws IOException {
		
		// forward
		super();
		
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
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);

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
				if(dialog!=null) dialog.setVisibleDelay(!dialog.isVisible(), 250);				
			}

			public void mouseReleased(MouseEvent e) {
				// stop show if not shown already
				if(dialog!=null) dialog.cancelSetVisible();				
			}
			
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}

		});
		
		// add global keyevent listener
		Utils.getApp().getKeyEventDispatcher().addKeyListener(
				KeyEvent.KEY_PRESSED, KeyEvent.VK_ESCAPE, new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				// can process event?
				if(map!=null && map.isVisible() && isActive()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// forward
							cancel();
						}
					});					
				}
				
			}
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
	
	@Override
	public int getCursor() {
		// show default?
		if(isMouseOverIcon && !isDrawing())
			return 0;
		else {
			if(msoObject==null && isCreateMode())
				return super.getCursorFromLocation("cursors/create.cur");
			else if(msoObject!=null && isReplaceMode())
				return super.getCursorFromLocation("cursors/replace.cur");
			else if(msoObject!=null && isContinueMode())
				return super.getCursorFromLocation("cursors/continue.cur");
			else if(msoOwner!=null && isAppendMode())
				return super.getCursorFromLocation("cursors/append.cur");
			else
				return super.getCursorFromLocation("cursors/cursor.cur");			
		}	
	}

	public void onCreate(Object obj) {
		
		// is working?
		if(isWorking()) return;
		
		try {

			// only a DiskoMap object is accecpted
			if (obj instanceof DiskoMap) {	
				
				// unregister?
				if(map!=null) {
					removeDiskoWorkEventListener(map);
					map.removeIMapControlEvents2Listener(mapAdapter);					
				}
								
				// initialize map object
				map = (DiskoMap)obj;
				
				// register map in draw dialog?
				if(dialog instanceof IDrawToolCollection && !isHosted()) {
					((IDrawToolCollection)dialog).register(map);
				}
								
				// set marked button
				if(button!=null && button.getIcon() instanceof DiskoCustomIcon) {
					DiskoCustomIcon icon = (DiskoCustomIcon)button.getIcon();
					icon.setMarked(true);
				}
				
				// add map as work listener
				addDiskoWorkEventListener(map);
				
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

	@Override
	public void onDblClick() {
		// forward to extenders
		onAction(onDblClickAction);		
	}	
	
	@Override
	public void onMouseDown(int button, int shift, int x, int y) {
		try {
			// get position in map units
			Point p = toMapPoint(x,y);
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

		// get tic
		long tic = Calendar.getInstance().getTimeInMillis();
		
		// consume?
		if(tic-previous<250) return;
		
		// update tic
		previous = tic;
		
		// is moving
		isMoving = true;
		
		try {
			
			// get screen-to-map transformation and try to snap
			p = snapTo(toMapPoint(x,y));
			
			// get flag
			isMouseOverIcon = isShowDrawFrame && (drawFrame!=null) 
									? drawFrame.hitIcon(p.getX(), p.getY(), 1)!=null : false;
			
			// only forward to extenders of this class if 
			// drawing or mouse not over draw fram icon
			if(!isMouseOverIcon || isDrawing)
				onAction(onMouseMoveAction,button,shift,x,y);		
			else {
				// reset snap geometry
				geoSnap = null;
				// force a refresh
				refresh();
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
			Point p = toMapPoint(x,y);
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
	
	public void reset() {
		try {
			// refresh draw geometries
			refresh();
			// rest draw geometries
			p = null;
			geoPath = null;
			geoRubber = null;
			geoPoint = null;
			geoSnap = null;
			// set flags
			isDrawing = false;
			isMoving = false;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
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
	public boolean activate(boolean allow) {
		// forward
		boolean flag = super.activate(allow);
		// allowed?
		if(flag) {
			// update modes in panel
			getPropertyPanel().update();		
			// forward?
			if(!isDirty) setGeometries();
		}
		// return state
		return flag;
	}
	
	@Override
	public boolean deactivate(){
		
		// forward?
		if(isHosted())
			getHostTool().deactivate();
		
		// forward
		super.deactivate();
				
		// forward
		try {
			refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// forward		
		return true;
		
	}
		
	/* ==================================================
	 * Implemention of IDrawTool (override with care)
	 * ==================================================
	 */
	
	public FeatureType getFeatureType() {
		return featureType;
	}
	
	public int getMaxStep() {
		return maxStep;
	}

	public void setMaxStep(int distance) {
		// buffer work?
		isDirty = isDirty || (distance!=maxStep);
		// prepare
		maxStep = distance;
	}

	public int getMinStep() {
		return minStep;
	}

	public void setMinStep(int distance) {
		// buffer work?
		isDirty = isDirty || (distance!=minStep);
		// prepare
		minStep = distance;
	}
		
	public boolean isConstrainMode() {
		return isConstrainMode;
	}

	public void setConstrainMode(boolean isConstrainMode) {
		// buffer work?
		isDirty = isDirty || (this.isConstrainMode!=isConstrainMode);
		// prepare
		this.isConstrainMode = isConstrainMode;
	}

	public boolean isShowDrawFrame() {
		return isShowDrawFrame;
	}

	public void isShowDrawFrame(boolean isShowDrawFrame) {
		// get flag
		boolean bFlag = isShowDrawFrame && map.isEditSupportInstalled();
		try {
			// any change?
			if(this.isShowDrawFrame != bFlag) {
				// prepare
				this.isShowDrawFrame = bFlag;
				// update both draw frame and geometries? 
				if(isShowDrawFrame && drawAdapter!=null) {
					// reapply mso frame;
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

	public void setWorkPoolMode(boolean isWorkPoolMode) {
		this.isWorkPoolMode = isWorkPoolMode;
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

	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		
		// is working?
		if(isWorking()) return;
		
		// is not drawing?
		if(!isDrawing) {
			
			// only IAreaIf is a valid owner
			if(!(msoOwner instanceof IAreaIf)) msoOwner = null;			
			
			// is drawing point?
			if(featureType==FeatureType.FEATURE_POINT) {
				// get point flag
				boolean supportsPoint = msoObject!=null &&
					(MsoClassCode.CLASSCODE_POI.equals(msoObject.getMsoClassCode()) || 
					 MsoClassCode.CLASSCODE_UNIT.equals(msoObject.getMsoClassCode())); 
				// point not supported?
				if(msoObject!=null && !supportsPoint) msoObject=null; // invalid object reference
			}
			else {
				// get point flag
				boolean supportsLine = msoObject!=null &&
					(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(msoObject.getMsoClassCode()) ||
							MsoClassCode.CLASSCODE_SEARCHAREA.equals(msoObject.getMsoClassCode()) ||
							MsoClassCode.CLASSCODE_ROUTE.equals(msoObject.getMsoClassCode())); 
				// point not supported?
				if(msoObject!=null && !supportsLine) msoObject=null; // invalid object referense
			}
			// set mso owner object
			this.msoOwner = msoOwner;
			// set mso object
			this.msoObject = msoObject;
			// set mso object
			this.msoClassCode = msoClassCode;
			// forward?
			if(!(isBatchUpdate || isWorking())) setGeometries();
				
		}
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
		return apply(doSnapTo);
			
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
	
	public DrawAdapter getDrawAdapter() {
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
				
				// reset current draw geometries
				p = null;
				geoPath = null;
				geoPoint = null;
				geoSnap = null;
				geoRubber = null;
			
				// has been deleted?
				if(msoObject!=null && msoObject.hasBeenDeleted()) 
					doInit();
				
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
					p = MapUtil.getEsriPoint(msoPoi.getPosition(),map.getSpatialReference());
				}
				else if (MsoClassCode.CLASSCODE_UNIT.equals(msoObject.getMsoClassCode())) {
					// cast to unit
					IUnitIf msoUnit = (IUnitIf)msoObject;
					// get polyline
					p = MapUtil.getEsriPoint(msoUnit.getPosition(),map.getSpatialReference());
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
	public IDiskoToolState save() {
		// get new state
		return new DrawToolState(this);
	}

	public boolean isDirty() {
		return isDirty;
	}
	
	public boolean apply(boolean force) {
		// set flag
		isDirty = isDirty || force && (geoPath!=null);
		// forward
		return apply();
	}	
	
	public boolean apply() {
		// initialize flag
		boolean bFlag = false;
		// has work pending?
		if(isDirty) {
			// forward
			if(doFinishWork()) {
				// rest flag
				isDirty = false;
				// success
				bFlag = true;
			}
		}
		// forward
		if (map.isEditSupportInstalled()) drawAdapter.finish();
		// finish
		return bFlag;
	}
	
	public boolean cancel() {
		// initialize flag
		boolean bFlag = false;
		// has work pending?
		if(isDirty) {
			// rest flag
			isDirty = false;
			// forward
			reset();
			// success!
			bFlag = true;
		}
		try {
			// forward?
			if(map.isEditSupportInstalled() && map.isVisible())
				if(drawAdapter.cancel()) drawAdapter.refreshFrame();
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
		return snapAdapter==null ? false : snapAdapter.isSnapToMode();
	}

	public void setSnapToMode(boolean isSnapToMode) {
		// can snap?
		if(snapAdapter!=null) {
			try {
				// forward
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
	 * Protected methods intended for use by
	 * extending classes (override with care)
	 * ==================================================
	 */

	
	/**
	 * Set dirty bit
	 * 
	 */	
	protected void setDirty() {
		isDirty = true;
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

	/* ==================================================
	 * Private methods intended internal use
	 * ==================================================
	 */
	
	private void draw() throws IOException, AutomationException {
		
		// allow?
		if (isActive || isShowDrawFrame && (map.isEditSupportInstalled() && drawFrame.isActive())) {
			
			// is drawing?
			if(featureType!=null) { //&& isDrawing) { // || isBufferedMode)){ 
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
				rubber.addPoint((IPoint)from.esri_clone(), null, null);
			}
		}
		
		// draw in screen display
		if(!isMoving) {
			if (path != null) {
				screenDisplay.setSymbol(pathSymbol);
				screenDisplay.drawPolyline(path);
			}
			if (rubber != null) {
				screenDisplay.setSymbol(pathSymbol);
				screenDisplay.drawPolyline(rubber);
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
			return false;
		}
		
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
		// check for valid change?
		if(!FeatureType.FEATURE_POINT.equals(featureType)) {
			// has change b
		}
		// set working flag
		setIsWorking();
		// valid
		return true;
	}
	
	private void afterFinish(boolean success) {
		
		try {
			
			// set flags
			isDrawing = false;
			
			// forward?
			if(isDirty) 
				prepareDrawFrame();
			else
				reset();
			
			// draw geometries only
			refresh();	
			
			// reset last invalid area
			lastInvalidArea = null;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// reset working flag
		setIsNotWorking();
	}
	
	protected void prepareDrawFrame() throws AutomationException, IOException {
		// update both draw frame and geometries? 
		if(isShowDrawFrame && drawAdapter!=null) {
			// reapply mso frame;
			drawAdapter.setMsoFrame();
			// forward
			drawAdapter.setFrameUnion(getGeoEnvelope());
			// forward
			drawAdapter.prepareFrame(true);
		}		
	}
	
	private IEnvelope getGeoEnvelope() throws AutomationException, IOException {
		if(geoPath!=null) {
			return geoPath.getEnvelope();
		}
		else if(geoPoint!=null){
			return MapUtil.getEnvelope(geoPoint, getSnapTolerance());
		}
		// failure
		return null;
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

	
	
	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
	
	protected boolean doFinishWork() {
		try {
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
		public void onAfterScreenDraw(IMapControlEvents2OnAfterScreenDrawEvent e)
				throws IOException, AutomationException {
			// TODO Auto-generated method stub
			super.onAfterScreenDraw(e);
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
			
			// has been deleted?
			if(msoObject!=null && msoObject.hasBeenDeleted()) 
				doInit();
			
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
					doInit();
				
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
				if (msoClassCode == MsoClassCode.CLASSCODE_OPERATIONAREA) {
					// create or update operation area?
					if (msoObject == null || isCreateMode()) {
						// create new operation area
						IOperationAreaListIf opAreaList = cmdPost.getOperationAreaList();
						msoObject = opAreaList.createOperationArea();
					}			
					// update the operation area geodata
					((IOperationAreaIf)msoObject).setGeodata(MapUtil.getMsoPolygon(polygon));
				}
				else if (msoClassCode == MsoClassCode.CLASSCODE_SEARCHAREA) {
					// create or update search area?
					if (msoObject == null || isCreateMode()) {
						// create search area
						ISearchAreaListIf searchAreaList = cmdPost.getSearchAreaList();
						msoObject = searchAreaList.createSearchArea();
					}			
					// update search area geodata
					((ISearchAreaIf)msoObject).setGeodata(MapUtil.getMsoPolygon(polygon));
				}
				else if (msoClassCode == MsoClassCode.CLASSCODE_ROUTE) {
					// create or update area?
					if(msoOwner == null || isCreateMode()) {
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
				
				// nothing to do?
				if(geoPath==null) return false;
				
				// reset current mso object?
				if(isCreateMode() && !doSnapTo)
					doInit();
				
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
	
				// only route is 
				if (msoClassCode == MsoClassCode.CLASSCODE_ROUTE) {
					// create or update area
					if(msoOwner == null || isCreateMode()) {
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

				// nothing to do?
				if(geoPoint==null) return false;
				
				// reset current mso object?
				if(isCreateMode() && !doSnapTo)
					doInit();
				
				// do snapping?
				if(snapAdapter!=null && doSnapTo) {
					boolean flag = snapAdapter.isSnapToMode();
					snapAdapter.setSnapToMode(true);
					geoPoint = snapAdapter.doSnapTo(geoPoint);
					snapAdapter.setSnapToMode(flag);
				}
				
				// move or add?
				if(msoObject==null || isCreateMode()) {
					
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
						poi.setPosition(MapUtil.getMsoPosistion(geoPoint));
						
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
								if(msoOwner==null || isCreateMode()) {
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
						poi.setPosition(MapUtil.getMsoPosistion(geoPoint));
					}
					else {
						// cast to unit
						IUnitIf msoUnit = (IUnitIf)msoObject;
						// update
						msoUnit.setPosition(MapUtil.getMsoPosistion(geoPoint));					
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
		private boolean isDrawing = false;
		private boolean isMoving = false;
		private boolean isDirty = false;
		private boolean isShowDrawFrame = false;
		private boolean isConstrainMode = false;
		private boolean isWorkPoolMode = false;

		// state
		private DrawMode drawMode = null;
		
		// constrain attributes
		private int minStep = 10;
		private int maxStep = 100;

		// holds draw geometry
		private Polyline geoPath = null;
		private Polyline geoRubber = null;
		private IGeometry geoSnap = null;

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
			this.isDrawing = tool.isDrawing;
			this.isMoving = tool.isMoving;
			this.isShowDrawFrame = tool.isShowDrawFrame;
			this.isDirty = tool.isDirty;
			this.drawMode = tool.drawMode;
			this.isConstrainMode = tool.isConstrainMode;
			this.isWorkPoolMode = tool.isWorkPoolMode;
			this.minStep = tool.minStep;
			this.maxStep = tool.maxStep;
			this.moveCount = tool.moveCount;
			this.geoPath = tool.geoPath;
			this.geoRubber = tool.geoRubber;
			this.geoSnap = tool.geoSnap;
			this.lastInvalidArea = tool.lastInvalidArea;
			
		}
		
		public void load(AbstractDrawTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.isDrawing = this.isDrawing;
			tool.isMoving = this.isMoving;
			tool.isShowDrawFrame = this.isShowDrawFrame;
			tool.isDirty = this.isDirty;
			tool.drawMode = this.drawMode;
			tool.isConstrainMode = this.isConstrainMode;
			tool.isWorkPoolMode = this.isWorkPoolMode;
			tool.minStep = this.minStep;
			tool.maxStep = this.maxStep;
			tool.moveCount = this.moveCount;
			tool.geoPath = this.geoPath;
			tool.geoRubber = this.geoRubber;
			tool.geoSnap = this.geoSnap;
			tool.lastInvalidArea= this.lastInvalidArea;
		}
	}	
}
