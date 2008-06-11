package org.redcross.sar.map.tool;

import com.esri.arcgis.controls.BaseTool;
import com.esri.arcgis.display.IDisplayTransformation;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IRelationalOperator;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.esriTransformDirection;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.thread.AbstractDiskoWork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

public abstract class AbstractDiskoTool extends BaseTool implements IDiskoTool {
	
	// flags
	protected boolean isDirty = false;			// true:=change is pending
	protected boolean isActive = false;
	protected boolean showDirect = false;
	protected boolean showDialog = true;
	
	// objects
	protected Properties properties = null;
	protected IDisplayTransformation transform = null;
	
	// mso objects information
	protected IMsoObjectIf msoOwner = null;
	protected IMsoObjectIf msoObject = null;
	protected IMsoManagerIf.MsoClassCode msoCode = null;	
	
	// GUI components
	protected DiskoMap map = null;
	protected DefaultDialog dialog = null;
	protected IToolPanel propertyPanel = null;
	protected IToolPanel defaultPropertyPanel = null;
	protected AbstractButton button = null;

	// types
	protected DiskoToolType type = null;
	
	// counter
	private int workCount = 0;
	
	// objects
	protected ArrayList<IToolPanel> panels = null;
	private ArrayList<IDiskoWorkListener> listeners = null;
	
	/**
	 * Constructor
	 *
	 */
	protected AbstractDiskoTool() {
		// prepare
		listeners = new ArrayList<IDiskoWorkListener>();		
	}
	
	/*===============================================
	 * IDiskoTool interface implementation
	 *===============================================
	 */

	/**
	 * Returns the tool active state
	 */
	public boolean isActive() {
		return isActive;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	/**
	 * Set dirty bit
	 * 
	 */	
	protected void setDirty(boolean isDirty) {
		// set flag
		this.isDirty = isDirty;
		// update panel
		getPropertyPanel().update();		
	}	
	
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
		return (dialog instanceof IToolCollection);
	}
	
	/**
	 * Returns the host tool is hosted
	 */
	public IHostDiskoTool getHostTool() {
		// is hosted?
		if (dialog instanceof IToolCollection) {
			// return current host tool
			return ((IToolCollection)dialog).getHostTool();
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
	public boolean activate(int options){
		// set flag
		isActive = true;
		// toogle dialog?
		if (dialog != null && (options==1) && showDialog && showDirect)
				dialog.setVisible(!dialog.isVisible());
		// always allowed
		return true;
	}

	/**
	 * The default behaviour is that is allways allowed 
	 * to deactivate this class. However, an extender
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

	public MsoClassCode getMsoCode() {
		return msoCode;
	}

	public IMsoObjectIf getMsoObject() {
		return msoObject;
	}

	public void setMsoObject(IMsoObjectIf msoObject) {
		setMsoData(msoOwner,msoObject,msoCode);
	}
	
	public IMsoObjectIf getMsoOwner() {
		return msoOwner;
	}

	public void setMsoOwner(IMsoObjectIf msoOwner) {
		setMsoData(msoOwner,msoObject,msoCode);
	}
	
	public void setMsoData(IDiskoTool tool) {
		if(tool instanceof AbstractDiskoTool && tool!=this) {
			AbstractDiskoTool abstractTool = (AbstractDiskoTool)tool;
			setMsoData(abstractTool.msoOwner,abstractTool.msoObject,abstractTool.msoCode);
		}
	}
	
	public void setMsoData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode) {
		
		// is working?
		if(isWorking()) return;
		
		// set mso owner object
		this.msoOwner = msoOwner;
		// set mso object
		this.msoObject = msoObject;
		// set mso object
		this.msoCode = msoClassCode;
		
	}

	public void addDiskoWorkListener(IDiskoWorkListener listener) {
		listeners.add(listener);
	}

	public void removeDiskoEventListener(IDiskoWorkListener listener) {
		listeners.remove(listener);
	}
	
	public AbstractButton getButton() {
		return button;
	}
	
	public DefaultDialog getDialog() {
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
		//if (transform == null) {
			transform = map.getActiveView().getScreenDisplay().getDisplayTransformation();
		//}
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
	protected Point toMapPoint(int x, int y) throws IOException, AutomationException {
		return (Point)getTransform().toMapPoint(x,y);
	}

	/**
	 * Utility function used to transform from map paint to screen coordinates
	 * @param p
	 * @return Point2D of screen coordinates 
	 * @throws IOException
	 * @throws AutomationException
	 */
	protected Point2D fromMapPoint(Point p) throws IOException, AutomationException {
		int x[] = {0};
		int y[] = {0};
		getTransform().fromMapPoint(p, x, y);
		return new Point2D.Double(x[0],y[0]);
	}
	
	protected void transform(Point p) throws IOException, AutomationException {
		p.transform(esriTransformDirection.esriTransformReverse, getTransform());
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

	protected void fireOnWorkFinish(Object source, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(source, data,DiskoWorkEvent.EVENT_FINISH);
	   	// forward
		fireOnWorkPerformed(e);
    }
    
	protected void fireOnWorkCancel(Object source, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(source, data,DiskoWorkEvent.EVENT_CANCEL);
    	// forward
		fireOnWorkPerformed(e);
    }
    
	protected void fireOnWorkChange(Object source, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(source,data,DiskoWorkEvent.EVENT_CHANGE);
		// forward
		fireOnWorkPerformed(e);    	
    }
    
    protected void fireOnWorkPerformed(DiskoWorkEvent e)
    {
		// notify listeners
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onWorkPerformed(e);
		}
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
		// start with notifying all mso listeners
		Utils.getApp().getMsoModel().resumeClientUpdate();
		// allow map to update
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
	}
	
	public Object getAttribute(String attribute) {
		return null;
	}

	public void setAttribute(Object value, String attribute) {
		return;
	}

	public IToolPanel addPropertyPanel() {
		// override this if needed
		return null;
	}

	public boolean removePropertyPanel(IToolPanel panel) {
		// has panels?
		if(panels!=null) {
			return panels.remove(panel);			
		}
		return false;
	}		
	
	public IToolPanel getDefaultPropertyPanel() {
		return defaultPropertyPanel;
	}
	
	public boolean setDefaultPropertyPanel() {
		return setPropertyPanel(defaultPropertyPanel);
	}
	
	public boolean setPropertyPanel(IToolPanel panel) {
		// has panels?
		if(panels!=null) {
			// in array?
			if(panels.contains(panel)) {
				// set as default?
				if(propertyPanel==null)
					defaultPropertyPanel = panel;
				// save hook
				propertyPanel = panel;
			}
		}
		return (panel!=null && propertyPanel == panel);			
	}
	
	public IToolPanel getPropertyPanel() {
		return propertyPanel;		
	}

	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean finish() {
		// TODO Auto-generated method stub
		return false;
	}

	public void reset() {
		// TODO Auto-generated method stu	
	}
	
	/*=============================================================
	 * Inner classes
	 *============================================================= 
	 */
	protected abstract class AbstractToolWork<T> extends AbstractDiskoWork<T> {
		
		public AbstractToolWork(boolean notify) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Vent litt",100,notify,true);
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
		protected boolean isActive = false;
		protected boolean showDirect = false;
		protected boolean showDialog = false;

		// mso objects and draw information
		protected IMsoObjectIf msoOwner = null;
		protected IMsoObjectIf msoObject = null;
		protected IMsoManagerIf.MsoClassCode msoClassCode = null;
		
		// other objects
		protected IToolPanel propertyPanel = null;
		
		// create state
		public DiskoToolState(AbstractDiskoTool tool) {
			save(tool);
		}
		
		public void save(AbstractDiskoTool tool) {
			this.isActive = tool.isActive;
			this.showDirect = tool.showDirect;
			this.showDialog = tool.showDialog;
			this.msoClassCode = tool.msoCode;
			this.msoObject = tool.msoObject;
			this.msoOwner = tool.msoOwner;
			this.propertyPanel = tool.propertyPanel;
		}
		
		public void load(AbstractDiskoTool tool) {
			tool.isActive = this.isActive;
			tool.showDirect = this.showDirect;
			tool.showDialog = this.showDialog;
			tool.msoCode = this.msoClassCode;
			tool.msoObject = this.msoObject;
			tool.msoOwner = this.msoOwner;
			tool.propertyPanel = this.propertyPanel;
			if(tool.propertyPanel!=null)
				tool.propertyPanel.update();
		}
	}
}
