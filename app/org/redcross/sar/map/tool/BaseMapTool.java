package org.redcross.sar.map.tool;

import com.esri.arcgis.beans.map.MapBean;
import com.esri.arcgis.carto.IActiveView;
import com.esri.arcgis.controls.BaseTool;
import com.esri.arcgis.display.IDisplayTransformation;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IRelationalOperator;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.esriTransformDirection;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.Application;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.dialog.IDialog;
import org.redcross.sar.gui.event.DialogToggleListener;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.IToolListener;
import org.redcross.sar.map.event.ToolEvent;
import org.redcross.sar.map.event.ToolEvent.ToolEventType;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.event.IFlowListener;
import org.redcross.sar.work.event.FlowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

public class BaseMapTool extends BaseTool implements IMapTool {

	private static final long serialVersionUID = 1L;
	
	// flags
	protected boolean isDirty = false;			// true:=change is pending
	protected boolean isActive = false;
	protected boolean showDirect = false;
	protected boolean showDialog = true;

	// objects
	protected Properties properties;
	protected IDisplayTransformation transform;

	// GUI components
	protected IDiskoMap map;
	protected IDialog dialog;
	protected IToolPanel toolPanel;
	protected IToolPanel defaultToolPanel;
	protected AbstractButton button;

	// types
	protected MapToolType type = null;
	protected ButtonSize buttonSize = ButtonSize.NORMAL;

	// counter
	private int workCount = 0;
	
	// lists
	protected List<IToolPanel> panels;
	protected List<IToolListener> toolListeners;
	protected List<IFlowListener> workListeners;	

	/**
	 * Constructor
	 *
	 */
	protected BaseMapTool() {
		// prepare
		toolListeners = new ArrayList<IToolListener>();
		workListeners = new ArrayList<IFlowListener>();		
	}

	/*===============================================
	 * IMapTool interface implementation
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
	public MapToolType getType() {
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

	public void addWorkFlowListener(IFlowListener listener) {
		workListeners.add(listener);
	}

	public void removeDiskoEventListener(IFlowListener listener) {
		workListeners.remove(listener);
	}

	public AbstractButton getButton() {
		return button;
	}

	public boolean requestFocustOnButton() {

		if (SwingUtilities.isEventDispatchThread()) {
			// notify
			if (!fireToolEvent(ToolEventType.FOCUS_EVENT, 0)) {

				AbstractButton b = null;

				// forward
				if (isHosted())
					b = getHostTool().getButton();
				else
					b = getButton();

				// can request focus?
				if (b != null && b.isEnabled() && b.isVisible()
						&& !b.hasFocus()) {
					return b.requestFocusInWindow();
				}

			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					requestFocustOnButton();
				}
			});
		}

		// failed
		return false;
	}

	public IDialog getDialog() {
		return dialog;
	}
	
	protected void setDialog(IDialog dialog) {
		
		// store
		this.dialog = dialog;
		
		// add dialog toggle listener?
		if(button!=null) button.addMouseListener(new DialogToggleListener(dialog));
		
	}

	public IDiskoMap getMap() {
		return map;
	}
	
	protected boolean setMap(IDiskoMap map) {
		
		try {

			// store
			this.map = map;
			
			// register map in draw dialog?
			if(dialog instanceof IDrawToolCollection && !isHosted()) {
				((IDrawToolCollection)dialog).register(map);
			}

			// set marked button
			if(button!=null && button.getIcon() instanceof DiskoIcon) {
				DiskoIcon icon = (DiskoIcon)button.getIcon();
				icon.setMarked(true);
			}

			// success
			return true;
			
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;
	}

	public String getName() {
		return name;
	}

	@Override
	public IMapToolState save() {
		// get new state
		return new MsoToolState(this);
	}

	@Override
	public boolean load(IMapToolState state) {
		// valid state?
		if(state instanceof MsoToolState) {
			((MsoToolState)state).load(this);
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
		if(map instanceof DiskoMap) {			
			transform = getActiveView().getScreenDisplay().getDisplayTransformation();
		}
		else {
			transform = null;
		}
		return transform;
	}

	/**
	 * Method used to get active view in map
	 *
	 * @return Active view
	 * @throws IOException
	 * @throws AutomationException
	 */
	protected IActiveView getActiveView()
			throws IOException, AutomationException {
		return ((MapBean)map.getMapImpl()).getActiveView();
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
		IEnvelope env = MapUtil.getEnvelope(p, getActiveView().getExtent().getWidth()/50);
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
		FlowEvent e = new FlowEvent(source, data,FlowEvent.EVENT_FINISH);
	   	// forward
		fireOnWorkPerformed(e);
    }

	protected void fireOnWorkCancel(Object source, Object data) {
		// create event
		FlowEvent e = new FlowEvent(source, data,FlowEvent.EVENT_CANCEL);
    	// forward
		fireOnWorkPerformed(e);
    }

	protected void fireOnWorkChange(Object source, Object data) {
		// create event
		FlowEvent e = new FlowEvent(source,data,FlowEvent.EVENT_CHANGE);
		// forward
		fireOnWorkPerformed(e);
    }

    protected void fireOnWorkPerformed(FlowEvent e)
    {
		// notify listeners
		for (int i = 0; i < workListeners.size(); i++) {
			workListeners.get(i).onFlowPerformed(e);
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
		Application.getInstance().getMsoModel().suspendChange();
	}

	protected void resumeUpdate() {
		// start with notifying all mso listeners
		Application.getInstance().getMsoModel().resumeUpdate();
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
		for(IToolListener listener : toolListeners) {
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

	public boolean addToolListener(IToolListener listener) {
		if(!toolListeners.contains(listener)) {
			return toolListeners.add(listener);
		}
		return false;
	}

	public boolean removeToolListener(IToolListener listener) {
		if(toolListeners.contains(listener)) {
			return toolListeners.remove(listener);
		}
		return false;
	}

	/*=============================================================
	 * Inner classes
	 *============================================================= */
	
	protected abstract class AbstractToolWork<T> extends AbstractWork {

		public AbstractToolWork(boolean notify) throws Exception {
			this(false,notify,"Vent litt");
		}
		public AbstractToolWork(boolean isThreadSafe, boolean notify, String message) throws Exception {
			// forward
			super(HIGH_PRIORITY,isThreadSafe,true,
					isThreadSafe ? WorkerType.UNSAFE : WorkerType.SAFE,
					"Vent litt",500,notify,false);
		}

		@Override
		public void beforePrepare() {
			// set flag to prevent reentry
			setIsWorking();
			// suspend for faster execution
			suspendUpdate();
		}

		@Override
		public abstract T doWork(IWorkLoop loop);

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
	public class MsoToolState implements IMapToolState {

		// flags
		protected boolean isActive;
		protected boolean showDirect;
		protected boolean showDialog;
		protected boolean isDialogVisible;

		// other objects
		protected IToolPanel propertyPanel;

		// create state
		public MsoToolState(BaseMapTool tool) {
			save(tool);
		}

		public void save(BaseMapTool tool) {
			this.isActive = tool.isActive;
			this.showDirect = tool.showDirect;
			this.showDialog = tool.showDialog;
			this.propertyPanel = tool.toolPanel;
			this.isDialogVisible = tool.dialog!=null ? tool.dialog.isVisible() : false;
		}

		public void load(BaseMapTool tool) {
			tool.isActive = this.isActive;
			tool.showDirect = this.showDirect;
			tool.showDialog = this.showDialog;
			tool.toolPanel = this.propertyPanel;
			if(tool.toolPanel!=null)
				tool.toolPanel.update();
			if(tool.dialog!=null)
				tool.dialog.setVisible(this.isDialogVisible);
		}
	}
}
