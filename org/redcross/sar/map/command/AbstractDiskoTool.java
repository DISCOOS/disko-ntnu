package org.redcross.sar.map.command;

import com.esri.arcgis.controls.BaseTool;
import com.esri.arcgis.display.IDisplayTransformation;
import com.esri.arcgis.geodatabase.*;
import com.esri.arcgis.geometry.*;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.map.IHostToolDialog;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public abstract class AbstractDiskoTool extends BaseTool implements IDiskoTool {
	
	// flags
	protected boolean isActive = false;
	protected boolean showDirect = false;
	protected boolean showDialog = true;
	
	// objects
	protected Properties properties = null;
	protected IDisplayTransformation transform = null;
	
	// mso objects information
	protected IMsoObjectIf msoOwner = null;
	protected IMsoObjectIf msoObject = null;
	protected IMsoManagerIf.MsoClassCode msoClassCode = null;	
	
	// GUI components
	protected DiskoMap map = null;
	protected DiskoDialog dialog = null;
	protected JPanel propertyPanel = null;
	protected AbstractButton button = null;

	// types
	protected DiskoToolType type = null;
	
	// counter
	private int workCount = 0;
	
	// objects
	protected ArrayList<JPanel> panels = null;
	private ArrayList<IDiskoWorkListener> listeners = null;
	
	/**
	 * Constructor
	 *
	 */
	protected AbstractDiskoTool() {
		listeners = new ArrayList<IDiskoWorkListener>();
	}
	
	/*===============================================
	 * IDiskoTool interface implementation
	 *===============================================
	 */

	/**
	 * Returns the disko tool type
	 */
	public DiskoToolType getType() {
		return type;
	}

	/**
	 * If true, the tool is hosted
	 */
	public boolean isHosted() {
		// is the dialog is a host dialog, then
		// the tool must be hosted
		return (dialog instanceof IHostToolDialog);
	}
	
	/**
	 * Returns the host tool is hosted
	 */
	public IDiskoHostTool getHostTool() {
		// is hosted?
		if (dialog instanceof IHostToolDialog) {
			// return current host tool
			return ((IHostToolDialog)dialog).getHostTool();
		}
		return null;
	}
	
	public boolean isShowDialog() {
		return showDialog;
	}

	public void setShowDialog(boolean isShowDialog) {
		showDialog = isShowDialog;
	}

	/**
	 * The default behaviour is that is allways allowed 
	 * to activate this class. Howerver, an extender
	 * of this class can override this behavior. 
	 */	
	public boolean activate(boolean allow){
		// set flag
		isActive = true;
		// toogle dialog?
		if (dialog != null && allow && showDialog && showDirect)
				dialog.setVisible(!dialog.isVisible());
		// allways allowed
		return true;
	}

	/**
	 * The default behaviour is that is allways allowed 
	 * to deactivate this class. Howerver, an extender
	 * of this class can override this behavior. 
	 */
	public boolean deactivate(){
		// reset flag
		isActive = false;
		// An extender of this class could override this method
		if (dialog != null && showDialog && showDirect)
			dialog.setVisible(false);
		// return state
		return true;
	}

	public MsoClassCode getMsoClassCode() {
		return msoClassCode;
	}

	public IMsoObjectIf getMsoObject() {
		return msoObject;
	}

	public void setMsoObject(IMsoObjectIf msoObject) {
		setMsoDrawData(msoOwner,msoObject,msoClassCode);
	}
	
	public IMsoObjectIf getMsoOwner() {
		return msoOwner;
	}

	public void setMsoOwner(IMsoObjectIf msoOwner) {
		setMsoDrawData(msoOwner,msoObject,msoClassCode);
	}
	
	public void setMsoDrawData(IDiskoTool tool) {
		if(tool instanceof AbstractDiskoTool && tool!=this) {
			AbstractDiskoTool abstractTool = (AbstractDiskoTool)tool;
			setMsoDrawData(abstractTool.msoOwner,abstractTool.msoObject,abstractTool.msoClassCode);
		}
	}
	
	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode) {
		
		// is working?
		if(isWorking()) return;
		
		// set mso owner object
		this.msoOwner = msoOwner;
		// set mso object
		this.msoObject = msoObject;
		// set mso object
		this.msoClassCode = msoClassCode;
		// set class code
		this.msoClassCode = msoClassCode;
		
	}

	public void addDiskoWorkEventListener(IDiskoWorkListener listener) {
		listeners.add(listener);
	}

	public void removeDiskoWorkEventListener(IDiskoWorkListener listener) {
		listeners.remove(listener);
	}
	
	public AbstractButton getButton() {
		return button;
	}
	
	public DiskoDialog getDialog() {
		return dialog;
	}

	public DiskoMap getMap() {
		return map;
	}
	
	public String getName() {
		return name;
	}
		
	@Override
	public IDiskoToolState save() {
		// get new state
		return new DiskoToolState(this);
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
		// valid state?
		if(state instanceof DiskoToolState) {
			((DiskoToolState)state).load(this);
			return true;
		}
		return false;
	
	}	
	
	/*====================================================
	 * Protected methods (only intended for use inside
	 * this package)
	 *====================================================
	 */


	protected boolean setEnabled(boolean isEnabled) {
		// enable?
		if(isEnabled && !enabled) {
			enabled = true;
			if(button!=null) {
				button.setEnabled(true);
			}
			// success
			return true;
		}
		// disable?
		else if(!isEnabled && enabled) {
			// must also be deactivated
			if (deactivate()) {
				// diable
				enabled = false;
				if(button!=null) {
					button.setEnabled(false);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Method used to get current display transformation. Display
	 * transformation is a utility class for the map spatial reference
	 * and convertion between map and screen
	 * 
	 * @return Current display transformation
	 * @throws IOException
	 * @throws AutomationException
	 */
	protected IDisplayTransformation getTransform()
			throws IOException, AutomationException {
		if (transform == null) {
			transform = map.getActiveView().getScreenDisplay().getDisplayTransformation();
		}
		return transform;
	}

	/**
	 * Utility function used to transform screen coordinates to map point
	 * 
	 * @param x Screen x-position
	 * @param y Screen y-position
	 * @return Point in map coordinates
	 * @throws IOException
	 * @throws AutomationException
	 */
	protected Point transform(int x, int y) throws IOException, AutomationException {
		return (Point)getTransform().toMapPoint(x,y);
	}

	/**
	 * Utility function used to transform from map paint to screen coordinates
	 * @param p
	 * @return Point2D of screen coordinates 
	 * @throws IOException
	 * @throws AutomationException
	 */
	protected Point2D toScreen(Point p) throws IOException, AutomationException {
		int x[] = {0};
		int y[] = {0};
		getTransform().fromMapPoint(p, x, y);
		return new Point2D.Double(x[0],y[0]);
	}
	
	protected void transform(Point p) throws IOException, AutomationException {
		p.transform(com.esri.arcgis.geometry.esriTransformDirection.esriTransformReverse, getTransform());
	}

	protected IFeatureCursor search(IFeatureClass fc, IPoint p, double size) throws UnknownHostException, IOException {
		//System.out.println("Extent.Width:="+map.getActiveView().getExtent().getWidth());
		IEnvelope env = MapUtil.getEnvelope(p, size); //map.getActiveView().getExtent().getWidth()/50);
		ISpatialFilter filter = new SpatialFilter();
		filter.setGeometryByRef(env);
		filter.setSpatialRel(esriSpatialRelEnum.esriSpatialRelOverlaps);
		/*IFeatureCursor cursor = fc.search(filter, false);
		IFeature feature = cursor.nextFeature();*/
		return fc.search(filter, false);
	}

	protected int getGeomIndex(GeometryBag geomBag, IPoint p) throws AutomationException, IOException {
		IEnvelope env = MapUtil.getEnvelope(p, map.getActiveView().getExtent().getWidth()/50);
		for (int i = 0; i < geomBag.getGeometryCount(); i++) {
			IRelationalOperator relOp = (IRelationalOperator)geomBag.getGeometry(i);
			if (!relOp.disjoint(env)) {
				return i;
			}
		}
		return -1;
	}

	protected double getMinimumDistance(Object f, IPoint p) throws AutomationException, IOException {
		double min = -1;
		if(f instanceof IProximityOperator) {
			min = ((IProximityOperator)f).returnDistance(p);
		}
		else if(f instanceof IMsoFeature) {
			// get shape
			IGeometry geom = ((IMsoFeature)f).getShape();
			// is geometry bag?
			if(geom instanceof GeometryBag) {
				// cast
				GeometryBag geomBag = (GeometryBag)geom;
				// get count
				int count = geomBag.getGeometryCount();
				// has items?
				if(count>0) {
					// get minimum length of first
					min = getMinimumDistance(geomBag.getGeometry(0), p);
					// loop
					for(int i=1;i<count;i++) {
						// get distance
						double d = getMinimumDistance(geomBag.getGeometry(i), p);
						// update minimum distance
						if(d>0)
							min = java.lang.Math.min(min, d);
					}
				}
			}
			// has proximity operator?
			else if(geom instanceof IProximityOperator) {
				// get point
				IProximityOperator opr = (IProximityOperator)(geom);
				IProximityOperator p2 =  (IProximityOperator)opr.returnNearestPoint(p, 0);
				min = p2.returnDistance(p);
			}
		}
		return min;
	}
	
	protected void updateMsoObject(IMsoFeature msoFeature, IGeometry geom) throws IOException, AutomationException {
		
		// get mso object
		IMsoObjectIf msoObj = msoFeature.getMsoObject();
		// get class code
		IMsoManagerIf.MsoClassCode classCode = msoObj.getMsoClassCode();
		// dispatch
		if(classCode == IMsoManagerIf.MsoClassCode.CLASSCODE_POI) {
			IPOIIf msoPOI = (IPOIIf)msoObj;
			msoPOI.setPosition(MapUtil.getMsoPosistion((Point)geom));
		}
		else if(classCode == IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT) {
			IUnitIf msoUnit = (IUnitIf)msoObj;
			msoUnit.setPosition(MapUtil.getMsoPosistion((Point)geom));
		}
		else if(classCode == IMsoManagerIf.MsoClassCode.CLASSCODE_ROUTE) {
			IRouteIf msoRoute = (IRouteIf)msoObj;
			msoRoute.setGeodata(MapUtil.getMsoRoute((Polyline)geom));			
		}
		else if(classCode == IMsoManagerIf.MsoClassCode.CLASSCODE_TRACK) {
			ITrackIf msoTrack = (ITrackIf)msoObj;
			Track track = msoTrack.getGeodata();
			List<TimePos> list = new ArrayList<TimePos>(track.getTrackTimePos());
			List<Calendar> timesteps = new ArrayList<Calendar>(list.size());
			for(int i=0;i<list.size();i++) {
				timesteps.add(list.get(i).getTime());
			}
			msoTrack.setGeodata(MapUtil.getMsoTrack((Polyline)geom,timesteps));						
		}
		else if(classCode == IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA) {
			ISearchAreaIf msoSearchArea = (ISearchAreaIf)msoObj;
			msoSearchArea.setGeodata(MapUtil.getMsoPolygon((Polygon)geom));			
		}
		else if(classCode == IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA) {
			IOperationAreaIf msoOperationArea = (IOperationAreaIf)msoObj;
			msoOperationArea.setGeodata(MapUtil.getMsoPolygon((Polygon)geom));						
		}
	}

	protected void fireOnWorkFinish() {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				null,null,null,DiskoWorkEventType.TYPE_FINISH);
	   	// forward
    	fireOnWorkFinish(e);
    }
    
    protected void fireOnWorkFinish(DiskoWorkEvent e)
    {
		// notify listeners
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onWorkFinish(e);
		}
	}

	protected void fireOnWorkCancel() {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				null,null,null,DiskoWorkEventType.TYPE_CANCEL);
    	// forward
    	fireOnWorkCancel(e);
    }
    
    protected void fireOnWorkCancel(DiskoWorkEvent e)
    {
		// notify listeners
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onWorkCancel(e);
		}
	}

	protected void fireOnWorkChange() {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				null,null,null,DiskoWorkEventType.TYPE_CHANGE);
    	// forward
    	fireOnWorkCancel(e);
    }
	
    protected void fireOnWorkChange(DiskoWorkEvent e)
    {
		// notify listeners
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onWorkChange(e);
		}
	}
    
	protected void fireOnWorkChange(Object worker, 
			IMsoObjectIf msoObj, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				worker,msoObj,data,DiskoWorkEventType.TYPE_CHANGE);
		// forward
		fireOnWorkChange(e);    	
    }
    
    protected boolean isWorking() {
		return (workCount>0);
	}

	protected int isWorkingCount() {
		return workCount;
	}
	
	protected int setIsWorking() {
		workCount++;
		return workCount; 
	}
	
	protected int setIsNotWorking() {
		if(workCount>0) {
			workCount--;
		}
		return workCount; 
	}
	
	protected void suspendUpdate() {
		if(map!=null) {
			try {
				map.suspendNotify();
				map.setSupressDrawing(true);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}		
		Utils.getApp().getMsoModel().suspendClientUpdate();
	}
	
	protected void resumeUpdate() {
		if(map!=null) {
			try {
				map.setSupressDrawing(false);
				map.refreshMsoLayers();
				map.resumeNotify();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}		
		Utils.getApp().getMsoModel().resumeClientUpdate();
	}
	
	public Object getAttribute(String attribute) {
		return null;
	}

	public void setAttribute(Object value, String attribute) {
		return;
	}

	public JPanel addPropertyPanel() {
		// override this if needed
		return null;
	}

	public boolean removePropertyPanel(JPanel panel) {
		// has panels?
		if(panels!=null) {
			return panels.remove(panel);			
		}
		return false;
	}		
	
	public boolean setPropertyPanel(JPanel panel) {
		// has panels?
		if(panels!=null) {
			// in array?
			if(panels.contains(panel)) {
				propertyPanel = panel;
			}
		}
		return (propertyPanel == panel);			
	}
	
	public JPanel getPropertyPanel() {
		return propertyPanel;		
	}

	/*=============================================================
	 * Inner classes
	 *============================================================= 
	 */
	protected abstract class AbstractToolWork<T> extends AbstractDiskoWork<T> {
		
		public AbstractToolWork(boolean notify) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Vent litt",100,notify);
		}

		@Override
		public abstract T doWork();

		@Override
		public void run() {
			// set flag to prevent reentry
			setIsWorking();
			// suspend for faster execution¨
			suspendUpdate();			
			// forward
			super.run();
			// is on event dispatch thread?
			if(SwingUtilities.isEventDispatchThread())
				done();
		}

		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread.
		 * 
		 */
		@Override
		public void done() {
			try {
				// resume update
		        resumeUpdate();
				// reset flag
		        setIsNotWorking();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
	        // forward
	        super.done();
		}
	}
	
	/**
	 * Abstract tool state class
	 * 
	 * @author kennetgu
	 *
	 */
	public class DiskoToolState implements IDiskoToolState {

		// flags
		private boolean isActive = false;
		private boolean showDirect = false;
		private boolean showDialog = false;

		// mso objects and draw information
		private IMsoObjectIf msoOwner = null;
		private IMsoObjectIf msoObject = null;
		private IMsoManagerIf.MsoClassCode msoClassCode = null;
		
		// other objects
		private JPanel propertyPanel = null;
		
		// create state
		public DiskoToolState(AbstractDiskoTool tool) {
			save(tool);
		}
		
		public void save(AbstractDiskoTool tool) {
			this.isActive = tool.isActive;
			this.showDirect = tool.showDirect;
			this.showDialog = tool.showDialog;
			this.msoClassCode = tool.msoClassCode;
			this.msoObject = tool.msoObject;
			this.msoOwner = tool.msoOwner;
			this.propertyPanel = tool.propertyPanel;
		}
		
		public void load(AbstractDiskoTool tool) {
			tool.isActive = this.isActive;
			tool.showDirect = this.showDirect;
			tool.showDialog = this.showDialog;
			tool.msoClassCode = this.msoClassCode;
			tool.msoObject = this.msoObject;
			tool.msoOwner = this.msoOwner;
			tool.propertyPanel = this.propertyPanel;
		}
	}
}
