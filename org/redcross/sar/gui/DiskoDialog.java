package org.redcross.sar.gui;

import javax.swing.JPanel;
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

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.BorderFactory;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkEventListener;
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

public class DiskoDialog extends JDialog implements IMsoUpdateListenerIf, IMsoLayerEventListener  {

	private static final long serialVersionUID = 1L;
	
	private static int PAUSE_MILLIS = 100;
	private static int MILLIS_TO_SHOW = 1000;

	public static final int POS_WEST   = 1;
	public static final int POS_NORTH  = 2;
	public static final int POS_EAST   = 3;
	public static final int POS_SOUTH  = 4;
	public static final int POS_CENTER = 5;
	
	private ArrayList<IDiskoWorkEventListener> listeners = null;  //  @jve:decl-index=0:
	
	private JToggleButton toggleButton = null;
	private JDialog navDialogToggle = null;
	private JComponent positionComp = null;
	private int positionPolicy = POS_CENTER;
	private boolean sizeToFit = false;
	private boolean isToggable = false;
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
		// initialize ui
		initialize();
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
		listeners = new ArrayList<IDiskoWorkEventListener>();
	}
	
	public void addDiskoWorkEventListener(IDiskoWorkEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkEventListener listener) {
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
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (isToggable) {
			if (visible) {
				getNavDialogToggle().setVisible(true);
				getToggleButton().setSelected(true);
				getToggleButton().setText("<<");
			}
			else {
				getNavDialogToggle().setVisible(false);
			}
		}
		updatePosition();
	}
	
	public void doShow(boolean isVisible, int millisToShow) {
		// any change?
		if(isVisible!=this.isVisible()) {
			// forward
			m_worker.start(isVisible,millisToShow);
		}
	}
	
	public void cancelShow() {
		m_worker.cancel();
	}
	
	public void setLocationRelativeTo(JComponent comp, int posPolicy, boolean sizeToFit) {
		this.positionComp = comp;
		this.positionPolicy = posPolicy;
		this.sizeToFit = sizeToFit;
		
		positionComp.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent arg0) {
				setVisible(false);		
			}
			public void componentMoved(ComponentEvent arg0) {
				updatePosition();
			}
			public void componentResized(ComponentEvent arg0) {
				updatePosition();
			}
			public void componentShown(ComponentEvent arg0) {
				updatePosition();
			}
		});
		updatePosition();
	}
	
	public void setIsToggable(boolean isToggable) {
		this.isToggable = isToggable;
		updatePosition();
	}
	
	private void updatePosition() {
		if (positionComp == null || !positionComp.isShowing()) {
			return;
		}
		if (width == -1 && height == -1) {
			width  = getWidth();
			height = getHeight();
		}
		int offset = 2;
		int x = positionComp.getLocationOnScreen().x;
		int y = positionComp.getLocationOnScreen().y;
		int w = 0;
		int h = 0;
		switch (positionPolicy) {
			case POS_WEST:
				w = width;
				h = positionComp.getHeight();
				x += offset;
				y += offset;
				break;
			case POS_NORTH:
				w = positionComp.getWidth();
				h = height;
				x += offset;
				y += offset;
				break;
			case POS_EAST:
				w = width;
				h = positionComp.getHeight();
				x += (positionComp.getWidth() - w + offset);
				y += offset;
				break;
			case POS_SOUTH:
				w = positionComp.getWidth();
				h = height;
				x += offset;
				y += (positionComp.getHeight()- h + offset);
				break;
			case POS_CENTER:
				w = width;
				h = height;
				x += (positionComp.getWidth() /2)-(w/2);
				y += (positionComp.getHeight()/2)-(h/2);
				break;
		}
		// size to fit?
		if (sizeToFit && positionPolicy != POS_CENTER) {
			setSize(w-offset*2, h-offset*2);
		}
		// get screen size
		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		// ensure visible in x-direction
		if(x+w>screen.width)
			x = screen.width-w; 
		// ensure visible in y-direction
		if(y+h>screen.height)
			y = screen.height-h; 
		setLocation(x, y);
		validate();
		if (isToggable) {
			int yy = sizeToFit ? y+getHeight()-getNavDialogToggle().getHeight() : y;
			getNavDialogToggle().setLocation(x, yy);
		}
	}
	
	private JDialog getNavDialogToggle() {
		if (navDialogToggle == null) {
			navDialogToggle = new JDialog((Frame)getOwner());
			navDialogToggle.setAlwaysOnTop(true);
			JPanel contenPane = (JPanel)navDialogToggle.getContentPane();
			contenPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			contenPane.add(getToggleButton());
			navDialogToggle.setUndecorated(true);
			navDialogToggle.pack();
		}
		return navDialogToggle;
	}
	
	public JToggleButton getToggleButton() {
		if (toggleButton == null) {
			try {
				toggleButton = new JToggleButton("<<");
				toggleButton.setSelected(true);
				toggleButton.setPreferredSize(new Dimension(50, 50));
				toggleButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						if (toggleButton.isSelected()) {
							toggleButton.setText("<<");
							setVisible(true);
						}
						else {
							toggleButton.setText(">>");
							setVisible(false);
							getNavDialogToggle().setVisible(true);
						}				
					}
				});
				
			} catch (java.lang.Throwable e) {
				// TODO: Something
			}
		}
		return toggleButton;
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
		IMsoFeatureLayer msoLayer = (IMsoFeatureLayer)e.getSource();
		List selection = msoLayer.getSelected();
		// reset current selection
		currentMsoObj = null;
		currentMsoFeature = null;
		// select new?
		if (selection != null && selection.size() > 0) {
			// get feature
			IMsoFeature msoFeature = (IMsoFeature)selection.get(0);
			// get mso object
			IMsoObjectIf msoObj = msoFeature.getMsoObject();
			// forward
			if (setMsoObject(msoObj)) {
				// set current feature
				currentMsoObj = msoObj;
				currentMsoFeature = msoFeature;
			}
		} 
	}
	
	public boolean setMsoObject(IMsoObjectIf msoObj) {
		currentMsoObj = msoObj;
		return true;
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

		// reset current selection
		currentMsoObj = null;
		currentMsoFeature = null;
		
		// catch exceptions
		try {
	        // get selected object
	        List<IMsoFeature> features = map.getMsoSelection();
	        if(features!=null) {
		        for(int i=0;i<features.size();i++) {
	        		// cast to IMsoFeature
	        		IMsoFeature msoFeature = features.get(i);
	        		// try to select
	        		if (setMsoObject(msoFeature.getMsoObject())) {
	        			// set current selection
	        			currentMsoObj = msoFeature.getMsoObject();
	        			currentMsoFeature = msoFeature;
	        			return;
	        		}
		        }
	        }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// do cleanup
		setMsoObject(null);
		
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
				// show me!
				setVisible(m_isVisible);
			}
		}			
	}	
}
