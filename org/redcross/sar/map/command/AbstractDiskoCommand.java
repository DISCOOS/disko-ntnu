package org.redcross.sar.map.command;

import com.esri.arcgis.controls.BaseCommand;
import com.esri.arcgis.geometry.*;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
import org.redcross.sar.gui.DiskoDialog;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public abstract class AbstractDiskoCommand extends BaseCommand implements IDiskoCommand {
	
	// properties
	protected int helpContextID = 0;
	
	// flags
	protected boolean isActive = false;
	protected boolean showDirect = false;
	protected boolean showDialog = true;
	
	// objects
	protected Properties properties = null;
	
	// mso objects information
	protected IMsoObjectIf msoOwner = null;
	protected IMsoObjectIf msoObject = null;
	protected IMsoManagerIf.MsoClassCode msoClassCode = null;	
	
	// GUI components
	protected DiskoDialog dialog = null;
	protected JPanel propertyPanel = null;
	protected AbstractButton button = null;

	// types
	protected DiskoCommandType type = null;
	
	// counter
	private int workCount = 0;
	
	// objects
	protected ArrayList<JPanel> panels = null;
	private ArrayList<IDiskoWorkListener> listeners = null;
	
	/**
	 * Constructor
	 *
	 */
	protected AbstractDiskoCommand() {
		listeners = new ArrayList<IDiskoWorkListener>();
	}
	
	/*===============================================
	 * Overridden ICommand methods
	 *===============================================
	 */

	public int getHelpContextID() {
		return helpContextID;
	}


	public boolean isChecked() {
		return getButton().isSelected();
	}

	/*===============================================
	 * IDiskoCommand interface implementation
	 *===============================================
	 */

	/**
	 * Returns the disko command type
	 */
	public DiskoCommandType getType() {
		return type;
	}

	/**
	 * The default behaviour is to execute the 
	 * command. However, an extender
	 * of this class can override this behavior. 
	 */	
	public void onClick() {
		if (dialog != null && showDialog && showDirect)
			dialog.setVisible(!dialog.isVisible());
	}
	
	/**
	 * If true, the command is hosted
	 */
	public boolean isHosted() {
		// TODO: Implement
		return false;
	}
	
	/**
	 * Returns the host command if hosted
	 */
	public IHostDiskoCommand getHostCommand() {
		// TODO: Implement
		return null;
	}
	
	public boolean isShowDialog() {
		return showDialog;
	}

	public void setShowDialog(boolean isShowDialog) {
		showDialog = isShowDialog;
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
	
	public void setMsoDrawData(IDiskoCommand command) {
		if(command instanceof AbstractDiskoCommand && command!=this) {
			AbstractDiskoCommand abstractCommand = (AbstractDiskoCommand)command;
			setMsoDrawData(abstractCommand.msoOwner,abstractCommand.msoObject,abstractCommand.msoClassCode);
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

	public String getName() {
		return name;
	}
		
	@Override
	public IDiskoCommandState save() {
		// get new state
		return new DiskoCommandState(this);
	}
	
	@Override
	public boolean load(IDiskoCommandState state) {
		// valid state?
		if(state instanceof DiskoCommandState) {
			((DiskoCommandState)state).load(this);
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
			// diable
			enabled = false;
			if(button!=null) {
				button.setEnabled(false);
			}
			return true;
		}
		// failed
		return false;
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
		Utils.getApp().getMsoModel().suspendClientUpdate();
	}
	
	protected void resumeUpdate() {
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
			// suspend for faster executionš
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
	public class DiskoCommandState implements IDiskoCommandState {

		// flags
		private boolean showDirect = false;
		private boolean showDialog = false;

		// mso objects and draw information
		private IMsoObjectIf msoOwner = null;
		private IMsoObjectIf msoObject = null;
		private IMsoManagerIf.MsoClassCode msoClassCode = null;
		
		// other objects
		private JPanel propertyPanel = null;
		
		// create state
		public DiskoCommandState(AbstractDiskoCommand command) {
			save(command);
		}
		
		public void save(AbstractDiskoCommand command) {
			this.showDirect = command.showDirect;
			this.showDialog = command.showDialog;
			this.msoClassCode = command.msoClassCode;
			this.msoObject = command.msoObject;
			this.msoOwner = command.msoOwner;
			this.propertyPanel = command.propertyPanel;
		}
		
		public void load(AbstractDiskoCommand command) {
			command.showDirect = this.showDirect;
			command.showDialog = this.showDialog;
			command.msoClassCode = this.msoClassCode;
			command.msoObject = this.msoObject;
			command.msoOwner = this.msoOwner;
			command.propertyPanel = this.propertyPanel;
		}
	}
}
