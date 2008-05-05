package org.redcross.sar.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.Timer;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;

import com.esri.arcgis.interop.AutomationException;

public class DiskoDialog extends JDialog 
		implements IMsoUpdateListenerIf, IMsoLayerEventListener, IDiskoWorkListener {

	private static final long serialVersionUID = 1L;
	
	private static int PAUSE_MILLIS = 100;
	private static int MILLIS_TO_SHOW = 1000;

	public static final int POS_WEST   = 1;
	public static final int POS_NORTH  = 2;
	public static final int POS_EAST   = 3;
	public static final int POS_SOUTH  = 4;
	public static final int POS_CENTER = 5;
	
	private ArrayList<IDiskoWorkListener> listeners = null;  //  @jve:decl-index=0:
	
	private Component buddyComponent = null;
	private int positionPolicy = POS_CENTER;
	private boolean sizeToFit = false;
	private boolean snapToInside = true;
	private int width  = -1;
	private int height = -1;
	private int isWorking = 0;
	
	protected EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	protected EnumSet<IMsoFeatureLayer.LayerCode> myLayers = null;
	
	protected IMsoObjectIf currentMsoObj = null;
	protected IMsoFeature currentMsoFeature = null;
	
	private final DialogWorker m_worker = new DialogWorker(MILLIS_TO_SHOW);

	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public DiskoDialog(Frame owner) {
		// forward
		super(owner);
		// prepare objects
        myInterests = EnumSet.noneOf(IMsoManagerIf.MsoClassCode.class);
        myLayers =  EnumSet.noneOf(IMsoFeatureLayer.LayerCode.class);
        // listen to component events from frame
        owner.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent arg0) {
				setVisible(false);
			}
			public void componentMoved(ComponentEvent arg0) {
				setPosition();
			}
			public void componentResized(ComponentEvent arg0) {
				setFixedSize();
				setPosition();
			}
			public void componentShown(ComponentEvent arg0) {
				setFixedSize();
				setPosition();
			}
		});        
		// initialize ui
		initialize();
	}
	
	public void setFixedSize() {}
	
	public Component createRigidArea() {
		return Box.createRigidArea(new Dimension(5,5));
	}

	/**
	 * Constructor 
	 * 
	 * @param owner
	 * @param myInterests
	 */
	public DiskoDialog(Frame owner, IDiskoMap map,
			EnumSet<IMsoManagerIf.MsoClassCode> myInterests, 
			EnumSet<IMsoFeatureLayer.LayerCode> myLayers) {
		// forward
		super(owner);
		// prepare objects
		this.myInterests = myInterests;
		this.myLayers = myLayers;
        // add listeners
        owner.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent arg0) {
				setVisible(false);		
			}
			public void componentMoved(ComponentEvent arg0) {
				setPosition();
			}
			public void componentResized(ComponentEvent arg0) {
				setPosition();
			}
			public void componentShown(ComponentEvent arg0) {
				setPosition();
			}
		});		
		if(myInterests!=null && myInterests.size()>0) {
        	Utils.getApp().getMsoModel().getEventManager().addClientUpdateListener(this);
    	}
		if(myLayers!=null && myLayers.size()>0) {
			// loop over all layers
			Iterator<IMsoFeatureLayer.LayerCode> it = myLayers.iterator();
			while(it.hasNext()) {
				map.getMsoLayer(it.next()).addDiskoLayerEventListener(this);
			}
		}
		// initialize ui
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setUndecorated(true);
		listeners = new ArrayList<IDiskoWorkListener>();
	}
	
	public void addDiskoWorkEventListener(IDiskoWorkListener listener) {
		listeners.add(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener) {
		listeners.remove(listener);
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
				null,null,null,DiskoWorkEventType.TYPE_CHANGE);
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
	
	protected void fireOnWorkChange(Object worker, IMsoObjectIf msoObj, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				worker,msoObj,data,DiskoWorkEventType.TYPE_CHANGE);

		// forward
		fireOnWorkChange(e);    	
    }
    
    protected void fireOnWorkChange(DiskoWorkEvent e)
    {
		// notify listeners
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onWorkChange(e);
		}
	}
    
	public void onWorkChange(DiskoWorkEvent e) {
		// forward
		fireOnWorkChange(e);						
	}
	
	public void onWorkCancel(DiskoWorkEvent e) {
		// forward
		fireOnWorkCancel(e);						
	}
    
	public void onWorkFinish(DiskoWorkEvent e) {
		// forward
		fireOnWorkFinish(e);						
	}
    	
	@Override 
	public void setVisible(boolean isVisible) {
		// forward
		super.setVisible(isVisible);
		// update
		setPosition();
	}
	
	public void setVisibleDelay(boolean isVisible, int millisToShow) {
		// any change?
		if(isVisible!=this.isVisible()) {
			// forward
			m_worker.start(isVisible,millisToShow);
		}
	}
	
	public void cancelSetVisible() {
		m_worker.cancel();
	}
	
	public void setLocationRelativeTo(Component buddy, int policy, boolean sizeToFit, boolean snapToInside) {
		this.buddyComponent = buddy;
		this.positionPolicy = policy;
		this.sizeToFit = sizeToFit;
		this.snapToInside = snapToInside;
		
		buddyComponent.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent arg0) {
				setVisible(false);		
			}
			public void componentMoved(ComponentEvent arg0) {
				setPosition();
			}
			public void componentResized(ComponentEvent arg0) {
				setPosition();
			}
			public void componentShown(ComponentEvent arg0) {
				setPosition();
			}
		});
		setPosition();
	}
	
	private void setPosition() {
		// position not defined?
		if (buddyComponent == null || !buddyComponent.isShowing()) return;
		// initialize size?
		if (width == -1 || height == -1) {
			width  = getWidth() !=0 ? getWidth() : -1;
			height = getHeight() !=0 ? getHeight() : -1;
		}
		// initialize
		int offset = 2;
		int x = buddyComponent.getLocationOnScreen().x;
		int y = buddyComponent.getLocationOnScreen().y;
		int w = 0;
		int h = 0;
		int bw = buddyComponent.getWidth();
		int bh = buddyComponent.getHeight();
		// get position data
		switch (positionPolicy) {
			case POS_WEST:	
				w = snapToInside ? (width > bw - 2*offset ? bw - 2*offset : width) : bw;
				h = snapToInside ? h = bh - 2*offset : bh;
				x += snapToInside ? offset : - w - offset;
				y += snapToInside ? offset : 0;
				break;
			case POS_EAST:
				w = snapToInside ? (width > bw - 2*offset ? bw - 2*offset : width) : bw;
				h = snapToInside ? bh - 2*offset : bh;
				x += snapToInside ? (bw - w - offset) : (bw + offset);
				y += snapToInside ? offset : 0;
				break;
			case POS_NORTH:
				w = snapToInside ? bw - 2*offset : bw;
				h = snapToInside ? (height > bh - 2*offset ? bh - 2*offset : height) : bh;
				x += snapToInside ? offset : 0;
				y += snapToInside ? offset : - h - offset;
				break;
			case POS_SOUTH:
				w = snapToInside ? bw - 2*offset : bw;
				h = snapToInside ? (height > bh - 2*offset ? bh - 2*offset : height) : bh;
				x += snapToInside ? offset : 0;
				y += snapToInside ? (bh - h - offset) : (bh + offset);
				break;
			case POS_CENTER:
				w = (width > bw - 2*offset) ? bw - 2*offset : width;
				h = (height > bh - 2*offset) ? bh - 2*offset : height;
				x += (bw - w) / 2;
				y += (bh - h) / 2;
				break;
		}
		// size to fit?
		if (sizeToFit && w > 0 && h > 0)
			Utils.setFixedSize(this, w, h);
		// get screen size
		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		// ensure visible in both directions
		x = (x + w > screen.width) ? screen.width - w : x; 
		y = (y + h > screen.height) ? screen.height - h : y; 
		// update location
		this.setLocation(x, y);
		// apply location change
		this.validate();
	}

	public void handleMsoUpdateEvent(Update e) {
		// get flags
		int mask = e.getEventTypeMask();
        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
		
        // get mso object
        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
        
        // add object?
		if (createdObject) {
			msoObjectCreated(msoObj,mask);
		}
		// is object modified?
		if ( (addedReference || removedReference || modifiedObject)) {
			msoObjectChanged(msoObj,mask);
		}
		// delete object?
		if (deletedObject) {
			msoObjectDeleted(msoObj,mask);		}
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}
	
	public void msoObjectCreated(IMsoObjectIf msoObject, int mask) {
		return;
	}
	
	public void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		return;
	}

	public void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		return;
	}

	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		// initialize
		IMsoObjectIf msoObj = null;
		IMsoFeature msoFeature = null;
		// get selection list
		IMsoFeatureLayer msoLayer = (IMsoFeatureLayer)e.getSource();
		List selection = msoLayer.getSelected();
		// select new?
		if (selection != null && selection.size() > 0) {
			// get feature
			msoFeature = (IMsoFeature)selection.get(0);
			// get mso object
			msoObj = msoFeature.getMsoObject();
		} 
		// forward
		switch(setMsoObject(msoObj)) {
			case 1: 
				// set current objects
				currentMsoObj = msoObj;
				currentMsoFeature = msoFeature;
				// finished
				break;
			case -1:			
				// reset current 
				currentMsoObj = null;
				currentMsoFeature = null;
				// finished
				break;
		}
	}
	
	public int setMsoObject(IMsoObjectIf msoObj) {
		return 0;
	}
	
	public boolean isWorking() {
		return (isWorking>0);
	}

	public int isWorkingCount() {
		return isWorking;
	}
	
	public int setIsWorking() {
		isWorking++;
		return isWorking; 
	}
	
	public int setIsNotWorking() {
		if(isWorking>0) {
			isWorking--;
		}
		return isWorking; 
	}
	
	public void setSelectedMsoFeature(IDiskoMap map) {

		// initialize
		IMsoObjectIf msoObj = null;
		IMsoFeature msoFeature = null;

		// catch exceptions
		try {
	        // get selected object
	        List<IMsoFeature> features = map.getMsoSelection();
	        if(features!=null && features.size()>0) {
        		// cast first item to IMsoFeature and get mso object	        	
	        	msoFeature = features.get(0);
        		msoObj = msoFeature.getMsoObject();
	        }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// forward
		switch(setMsoObject(msoObj)) {
			case 1: 
				// set current objects
				currentMsoObj = msoObj;
				currentMsoFeature = msoFeature;
				// finished
				break;
			case -1:			
				// reset current 
				currentMsoObj = null;
				currentMsoFeature = null;
				// finished
				break;
		}
				
	}
	
  	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */
  	
	class DialogWorker implements ActionListener {
		
		private long m_start = 0;
		private long m_millisToShow = 0;
		private Timer m_timer = null;
		private boolean m_isVisible = false;
		private boolean m_isCancelled = false;
		
		public DialogWorker(long millisToShow) {
			// save decision delay
			m_millisToShow = millisToShow;
			// create timer
			m_timer = new Timer(PAUSE_MILLIS, this);
		}

		public boolean start(boolean isVisible, int millisToShow) {
			// is not running?
			if(!m_timer.isRunning()) {
				// save
				m_isVisible = isVisible;
				m_millisToShow = millisToShow;
				// on construction, set time in milli seconds
				m_start = System.currentTimeMillis();
				// start timer
				m_timer.start();
				// reset flag
				m_isCancelled = false;
				// success
				return true;
			}
			// invalid
			return false;			
		}
		
		public boolean cancel() {
			// is running?
			if(m_timer.isRunning()) {
				// reset flag
				m_isCancelled = true;
				// stop timer
				m_timer.stop();
				// success
				return true;
			}
			// invalid
			return false;			
		}
		
		public boolean isRunning() {
			return m_timer.isRunning();
		}
							
		/**
		 * Worker 
		 * 
		 * Executed on the Event Dispatch Thread
		 * 
		 */
		@Override		
		public void actionPerformed(ActionEvent e) {
			// has no progress?
			if(!m_isCancelled && System.currentTimeMillis()- m_start > m_millisToShow) {
				// stop timer
				m_timer.stop();
				// show me!
				setVisible(m_isVisible);
			}
		}			
	}	
}
