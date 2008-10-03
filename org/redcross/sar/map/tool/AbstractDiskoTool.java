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

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.IToolListenerIf;
import org.redcross.sar.map.event.ToolEvent;
import org.redcross.sar.map.event.ToolEvent.ToolEventType;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.event.DiskoWorkEvent;
import org.redcross.sar.thread.event.IDiskoWorkListener;
import org.redcross.sar.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;

public abstract class AbstractDiskoTool extends BaseTool implements IDiskoTool {
	
	// flags
	protected boolean isDirty = false;			// true:=change is pending
	protected boolean isActive = false;
	protected boolean showDirect = false;
	protected boolean showDialog = true;
	
	// objects
	protected Properties properties;
	protected IDisplayTransformation transform;
	
	// mso objects information
	protected IMsoObjectIf msoOwner = null;
	protected IMsoObjectIf msoObject = null;
	protected IMsoManagerIf.MsoClassCode msoCode = null;	
	
	// GUI components
	protected DiskoMap map;
	protected DefaultDialog dialog;
	protected IToolPanel toolPanel;
	protected IToolPanel defaultToolPanel;
	protected AbstractButton button;

	// types
	protected DiskoToolType type = null;
	protected ButtonSize buttonSize = ButtonSize.NORMAL;
	
	// counter
	private int workCount = 0;
	
	// lists
	protected List<IToolPanel> panels;
	protected List<IToolListenerIf> toolListeners;		
	protected List<IDiskoWorkListener> workListeners;
	
	/**
	 * Constructor
	 *
	 */
	protected AbstractDiskoTool() {
		// prepare
		toolListeners = new ArrayList<IToolListenerIf>();		
		workListeners = new ArrayList<IDiskoWorkListener>();		
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
	
	public boolean resetDirtyFlag() {
		boolean bFlag = isDirty;
		setDirty(false);
		return bFlag;
	}
	
	/**
	 * Set dirty bit
	 * 
	 */	
	protected void setDirty(boolean isDirty) {
		// get change flag
		boolean isChanged = this.isDirty != isDirty;
		// any change
		if(isChanged) {
			// set flag
			this.isDirty = isDirty;
			// forward
			getToolPanel().update();		
		}
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
		workListeners.add(listener);
	}

	public void removeDiskoEventListener(IDiskoWorkListener listener) {
		workListeners.remove(listener);
	}
	
	public AbstractButton getButton() {
		return button;
	}
	
	public boolean requestFocustOnButton() {
		
		// notify
		if(!fireToolEvent(ToolEventType.FOCUS_EVENT, 0)) {
			
			AbstractButton b = null;
			
			// forward
			if(isHosted())
				b = getHostTool().getButton();
			else
				b = getButton();
			
			// can request focus?
			if(b!=null && b.isEnabled() && b.isVisible() && !b.hasFocus()) {
				return b.requestFocusInWindow();
			}
			
		}
		// failed
		return false;
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
		for (int i = 0; i < workListeners.size(); i++) {
			workListeners.get(i).onWorkPerformed(e);
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
	
	protected boolean fireToolEvent(ToolEventType type, int flags) {
		ToolEvent e = new ToolEvent(this,type,flags);
		for(IToolListenerIf listener : toolListeners) {
			listener.onAction(e);
			if(e.isConsumed()) return true;
		}
		return false;
	}
	
	
	
	public Object getAttribute(String attribute) {
		return null;
	}

	public void setAttribute(Object value, String attribute) {
		return;
	}

	public IToolPanel addToolPanel() {
		// override this if needed
		return null;
	}

	public boolean removeToolPanel(IToolPanel panel) {
		// has panels?
		if(panels!=null) {
			return panels.remove(panel);			
		}
		return false;
	}		
	
	public IToolPanel getDefaultToolPanel() {
		return defaultToolPanel;
	}
	
	public boolean setDefaultPropertyPanel() {
		return setToolPanel(defaultToolPanel);
	}
	
	public boolean setToolPanel(IToolPanel panel) {
		// has panels?
		if(panels!=null) {
			// in array?
			if(panels.contains(panel)) {
				// set as default?
				if(toolPanel==null)
					defaultToolPanel = panel;
				// save hook
				toolPanel = panel;
			}
		}
		return (panel!=null && toolPanel == panel);			
	}
	
	public IToolPanel getToolPanel() {
		return toolPanel;		
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
	
	/* ==================================================
	 * IToolListenerIf
	 * ==================================================
	 */
	
	public boolean addToolListener(IToolListenerIf listener) {
		if(!toolListeners.contains(listener)) {
			return toolListeners.add(listener); 
		}
		return false;
	}
	
	public boolean removeToolListener(IToolListenerIf listener) {
		if(toolListeners.contains(listener)) {
			return toolListeners.remove(listener); 
		}
		return false;
	}	
	
	/*=============================================================
	 * Inner classes
	 *============================================================= 
	 */
	protected abstract class AbstractToolWork<T> extends AbstractDiskoWork<T> {
		
		public AbstractToolWork(boolean notify) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Vent litt",100,notify,false,false,0);
		}

		@Override
		public void beforePrepare() {
			// set flag to prevent reentry
			setIsWorking();
			// suspend for faster execution
			suspendUpdate();			
		}		
		
		@Override
		public abstract T doWork();

		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread.
		 * 
		 */
		@Override
		public void afterDone() {
			try {
				// resume update
		        resumeUpdate();
				// reset flag
		        setIsNotWorking();
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
	public class DiskoToolState implements IDiskoToolState {

		// flags
		protected boolean isActive;
		protected boolean showDirect;
		protected boolean showDialog;
		protected boolean isDialogVisible;

		// MSO objects and draw information
		protected IMsoObjectIf msoOwner;
		protected IMsoObjectIf msoObject;
		protected IMsoManagerIf.MsoClassCode msoClassCode;
		
		// other objects
		protected IToolPanel propertyPanel;
		
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
			this.propertyPanel = tool.toolPanel;
			this.isDialogVisible = tool.dialog!=null ? tool.dialog.isVisible() : false;
		}
		
		public void load(AbstractDiskoTool tool) {
			tool.isActive = this.isActive;
			tool.showDirect = this.showDirect;
			tool.showDialog = this.showDialog;
			tool.msoCode = this.msoClassCode;
			tool.msoObject = this.msoObject;
			tool.msoOwner = this.msoOwner;
			tool.toolPanel = this.propertyPanel;
			if(tool.toolPanel!=null)
				tool.toolPanel.update();
			if(tool.dialog!=null) 
				tool.dialog.setVisible(this.isDialogVisible);
		}
	}
}
