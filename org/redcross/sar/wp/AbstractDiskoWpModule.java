package org.redcross.sar.wp;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
import org.redcross.sar.event.IDiskoWorkEventListener;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.MainPanel;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.gui.SubMenuPanel;
import org.redcross.sar.gui.UIFactory;
import org.redcross.sar.gui.NavBar.NavState;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.util.Internationalization;


import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * This abstract class is a base class that has a default implementation of the
 * IDiskoWpModule interface and the IDiskoMapEventListener interface.
 *
 * @author geira
 */
public abstract class AbstractDiskoWpModule 
			implements IDiskoWpModule, IMsoUpdateListenerIf {
	
	private IDiskoRole role = null;
    private IDiskoMap map = null;
    private boolean hasSubMenu = false;
    private Properties properties = null;
    private NavState m_navState = null;
    private boolean isNavBarSetupNeeded = true;
    private ArrayList<IDiskoWorkEventListener> listeners = null;
	private EnumSet<IMsoManagerIf.MsoClassCode> wpInterests = null;
	private EnumSet<IMsoFeatureLayer.LayerCode> mapLayers = null;
	
    protected String callingWp = null;
    protected ResourceBundle wpBundle;
    protected boolean isActive = false;
    
    private int isWorking = 0;
    
    private final ArrayList<ITickEventListenerIf> tickListeners;

    protected final ArrayList<DiskoWorkEvent> changeList;
    
    private final TickEvent tickEvent;
    
    private long tickTime = 0;

    /**
     * @param role
     */
    public AbstractDiskoWpModule(IDiskoRole role)
    {
        this.role = role;
        this.listeners = new ArrayList<IDiskoWorkEventListener>();
        this.tickListeners = new ArrayList<ITickEventListenerIf>();
        this.tickEvent = new TickEvent(this);
        this.changeList = new ArrayList<DiskoWorkEvent>();

        // initialize interests
        this.wpInterests = EnumSet.noneOf(IMsoManagerIf.MsoClassCode.class);
        
        // initialize layers
        this.mapLayers = getDefaultMapLayers();
        
        // initialialize map
        getMap();
        
		// initialize timers
        initTickTimer();
        
    }

    /**
     * @param role
     */
    public AbstractDiskoWpModule(IDiskoRole role, 
    		EnumSet<IMsoManagerIf.MsoClassCode> wpInterests,
    		EnumSet<IMsoFeatureLayer.LayerCode> mapLayers)
    {
        // initialize objects
    	this.role = role;
        this.listeners = new ArrayList<IDiskoWorkEventListener>();
        this.tickListeners = new ArrayList<ITickEventListenerIf>();
        this.tickEvent = new TickEvent(this);
        this.changeList = new ArrayList<DiskoWorkEvent>();
        this.wpInterests  = wpInterests;
        this.mapLayers = mapLayers;

        // add listeners?
    	if(wpInterests.size()>0) {
    		getApplication().getMsoModel().
    			getEventManager().addClientUpdateListener(this);
    	}

        // initialialize map
        getMap();
        
        // initialize timer
		initTickTimer();
    }
    
	private static EnumSet<IMsoFeatureLayer.LayerCode> getDefaultMapLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers;
		myLayers = EnumSet.of(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
	    return myLayers;
	}
    
    public IDiskoRole getDiskoRole()
    {
        return role;
    }


    /* (non-Javadoc)
      * @see org.redcross.sar.wp.IDiskoWpModule#getMap()
      */
    public IDiskoMap getMap()
    {
        if (map == null)
        {
            try
            {
            	// get map manager
                IDiskoMapManager manager = role.getApplication().getDiskoMapManager();
                // initialize map
                map = manager.getMapInstance(mapLayers);
                // hide map
                ((DiskoMap)map).setVisible(false);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return map;
    }

    public IDiskoApplication getApplication()
    {
        return role.getApplication();
    }

    /* (non-Javadoc)
      * @see org.redcross.sar.wp.IDiskoWpModule#getName()
      */
    public String getName()
    {
        return null;
    }

    public void setCallingWp(String name)
    {
        callingWp = name;
    }

    public String getCallingWp()
    {
        return callingWp;
    }

    /* (non-Javadoc)
      * @see org.redcross.sar.wp.IDiskoWpModule#hasMap()
      */
    public boolean hasMap()
    {
        return map != null;
    }

    /* (non-Javadoc)
      * @see org.redcross.sar.wp.IDiskoWpModule#hasSubMenu()
      */
    public boolean hasSubMenu()
    {
        return hasSubMenu;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public String getProperty(String key)
    {
        String value = properties != null ?
                properties.getProperty(key) : null;
        if (value == null)
        {
            //try application properties
            value = getApplication().getProperty(key);
        }
        return value;
    }

    public void loadProperties(String fileName)
    {
        try
        {
            properties = Utils.loadProperties("properties");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void addDiskoWorkEventListener(IDiskoWorkEventListener listener)
    {
        if (listeners.indexOf(listener) == -1)
        {
            listeners.add(listener);
        }
    }

    public void removeDiskoWorkEventListener(IDiskoWorkEventListener listener)
    {
        listeners.remove(listener);
    }

    protected void fireOnWorkChange(Object worker, IMsoObjectIf msoObj, Object data)
    {

    	// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				worker,msoObj,data,DiskoWorkEventType.TYPE_CHANGE);
    	// forward
		fireOnWorkChange(e);
    	
    }
    
    protected void fireOnWorkChange(DiskoWorkEvent e)
    {
    	// is active?
    	if(isActive) {
    		
	    	// add to list
	    	changeList.add(e);
	    	
	    	// notify listeners
	        for (int i = 0; i < listeners.size(); i++){
	            listeners.get(i).onWorkChange(e);
	        }
    	}
    }

    protected void fireOnWorkCancel()
    {
    	// create event
    	DiskoWorkEvent e = new DiskoWorkEvent(this,
    			null,null,null,DiskoWorkEventType.TYPE_CANCEL);    	

    	// forward
    	fireOnWorkCancel(e);
    }
    
    protected void fireOnWorkCancel(DiskoWorkEvent e)
    {
    	// is active?
    	if(isActive) {
    			    	
	    	// notify listeners
	        for (int i = 0; i < listeners.size(); i++){
	            listeners.get(i).onWorkCancel(e);
	        }
	        
	        // clear change list
	        changeList.clear();
    	}        
    }

    protected void fireOnWorkFinish()
    {
    	// create event
    	DiskoWorkEvent e = new DiskoWorkEvent(this,
    			null,null,null,DiskoWorkEventType.TYPE_FINISH);
    	// forward
    	fireOnWorkFinish(e);
    }
    
    protected void fireOnWorkFinish(DiskoWorkEvent e)
    {
    	// is active?
    	if(isActive) {
    		
	    	// notify listeners
	        for (int i = 0; i < listeners.size(); i++){
	            listeners.get(i).onWorkFinish(e);
	        }
	        
	        // clear change list
	        changeList.clear();
    	}        
    }

    /**
     * @return True if change count is larger then zero
     */
    public boolean isChanged()
    {
        return (changeList.size()>0);
    }

    /**
     * @return True if change count is larger then zero
     */
    public boolean isActive()
    {
        return isActive;
    }
    
    /* (non-Javadoc)
    * @see org.redcross.sar.wp.IDiskoWpModule#activated()
    */
    public void activated()
    {
    	// set active
        isActive=true;
    	setFrameText(null);
    	// has nav state?
    	if(m_navState!=null) {
        	// get navbar
        	NavBar navBar = getApplication().getUIFactory().getMainPanel().getNavBar();
    		// load state
        	navBar.load(m_navState);
    	}
    }

    /* (non-Javadoc)
    * @see org.redcross.sar.wp.IDiskoWpModule#deactivated()
    */
    public void deactivated()
    {
    	// get navbar
    	NavBar navBar = getApplication().getUIFactory().getMainPanel().getNavBar();
    	// save state
		m_navState =  navBar.save();
		// hide dialogs
		navBar.hideDialogs();
		// reset flags and title
        isActive=false;
    	setFrameText(null);
    }

    /* (non-Javadoc)
     * @see org.redcross.sar.wp.IDiskoWpModule#isNavBarSetupNeeded()
     */
    public boolean isNavBarSetupNeeded() {
    	return isNavBarSetupNeeded;
    }
     
    /* (non-Javadoc)
     * @see org.redcross.sar.wp.IDiskoWpModule#isNavBarSetupNeeded()
     */
    public void setupNavBar(EnumSet<DiskoToolType> tools, boolean isSelected) {
    	// get navbar
    	NavBar navBar = getApplication().getNavBar();
    	// show buttons
    	navBar.setVisibleButtons(tools,true,false);
    	// setup navbar
    	navBar.setup();
		// get nav button
		UIFactory uiFactory = getApplication().getUIFactory();
		AbstractButton navButton = uiFactory.getMainMenuPanel().getNavToggleButton();
		// update navbar selection status
		if (navButton.isSelected() != isSelected) {
			navButton.doClick();			
		}
		// do not need to do setup any more
		isNavBarSetupNeeded = false;
    }
    
    public void setFrameText(String text)
    {
        String s = "DISKO (" + getDiskoRole().getTitle() + ") - <Arbeidsprossess: " + getName() +"> - ";
        String o = "<Aksjon: " + getApplication().getMsoModel().getModelDriver().getActiveOperationName() + ">";
        if (text != null) {
            s += text + " - " + o;
        }
        else {
        	s += o;
        }
        getApplication().getFrame().setTitle(s);
    }

	public void showWarning(String key)
    {
		// Internationalization
		String msg = getText(key); 
		// forward
        Utils.showWarning(msg,getApplication().getFrame());
    }

    protected void layoutComponent(JComponent comp)
    {
        String id = role.getName() + getName();
        MainPanel mainPanel = getApplication().getUIFactory().getMainPanel();
        mainPanel.addComponent(comp, id);
    }

    protected void layoutButton(AbstractButton button)
    {
        layoutButton(button, true);
    }

    protected void layoutButton(AbstractButton button, boolean addToGroup)
    {
        String id = role.getName() + getName();
        SubMenuPanel subMenuPanel = getApplication().getUIFactory().getSubMenuPanel();
        subMenuPanel.addItem(button, id, addToGroup);
        hasSubMenu = true;
    }

    protected JButton createLargeButton(String aText, java.awt.event.ActionListener aListener)
    {
        return createButton(aText, getApplication().getUIFactory().getLargeButtonSize(), aListener);
    }

    protected JButton createSmallButton(String aText, java.awt.event.ActionListener aListener)
    {
        return createButton(aText, getApplication().getUIFactory().getSmallButtonSize(), aListener);
    }

    protected JButton createButton(String aText, Dimension aSize, java.awt.event.ActionListener aListener)
    {
        JButton createdButton = new JButton();
        createdButton.setText(aText);
        createdButton.setPreferredSize(aSize);
        if (aListener != null)
        {
            createdButton.addActionListener(aListener);
        }
        return createdButton;
    }

    public IMsoModelIf getMsoModel()
    {
        return getApplication().getMsoModel();
    }

    public IMsoManagerIf getMsoManager()
    {
        return getMsoModel().getMsoManager();
    }

    public IMsoEventManagerIf getMsoEventManager()
    {
        return getMsoModel().getEventManager();
    }
    
    public ICmdPostIf getCmdPost()
    {
    	return getMsoManager().getCmdPost();
    }

    public String getText(String aKey)
    {
        return Internationalization.getFullBundleText(wpBundle,aKey);
    }

    protected void assignWpBundle(Class aClass)
    {
        wpBundle = Internationalization.getBundle(aClass);
    }

    private static final int TIMER_DELAY = 1000; // 1 second
    private final WpTicker ticker = new WpTicker();

    /**
     * Creates a timer for generating {@link TickEvent} objects periodically.
     */
    private void initTickTimer()
    {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask()
        {
            public void run()
            {
                long newTickTime = Calendar.getInstance().getTimeInMillis();
                if (tickTime == 0)
                {
                    tickTime = newTickTime;
                } else if (newTickTime > tickTime)
                {
                    ticker.setElapsedTime(newTickTime - tickTime);
                    SwingUtilities.invokeLater(ticker);
                    tickTime = newTickTime;
                }

            }
        }, 0, TIMER_DELAY);
    }


    public void addTickEventListener(ITickEventListenerIf listener)
    {
        tickListeners.add(listener);
    }

    public void removeTickEventListener(ITickEventListenerIf listener)
    {
        tickListeners.remove(listener);
    }

    /**
     * Count down the interval timer for each listern and fire tick events to all listeners where interval time has expired.
     *
     * @param aMilli Time in milliseconds since previous call.
     */
    protected void fireTick(long aMilli)
    {
        if (tickListeners.size() == 0 || aMilli == 0)
        {
            return;
        }

        for (ITickEventListenerIf listener : tickListeners)
        {
            long timer = listener.getTimeCounter() - aMilli;
            if (timer > 0)
            {
                listener.setTimeCounter(timer);
            } else
            {
                listener.handleTick(tickEvent);
                listener.setTimeCounter(listener.getInterval());
            }
        }
    }

    /**
     * Class that embeds a runnable that performs the GUI updates by firing the ticks to the listeners.
     *
     * The class is not thread-safe. The run() method is run with the latest given elapsed time.
     */
    class WpTicker implements Runnable
    {
        long m_elapsedTime;

        void setElapsedTime(long aTime)
        {
            m_elapsedTime = aTime;
        }

        public void run()
        {
            fireTick(m_elapsedTime);
        }
    }
    
    public boolean confirmDeactivate()
    {
    	// TODO: Override this method
    	return true;
    }
	
	public void handleMsoUpdateEvent(Update e) {
		// TODO: Override this method
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		if(wpInterests!=null) {
			if(isActive) {
				return wpInterests.contains(aMsoObject.getMsoClassCode());
			}
		}
		return false;
	}
	
	public void beforeOperationChange() {
		// suspend map update
		if(map!=null) {
			map.suspendNotify();
			map.setSupressDrawing(true);
		}						
	}
    
	public void afterOperationChange() {
		// resume map update
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

	public void suspendUpdate() {
		Utils.getApp().getMsoModel().suspendClientUpdate();
		if(map!=null) {
			map.suspendNotify();
			map.setSupressDrawing(true);
		}		
	}
	
	public void resumeUpdate() {
		Utils.getApp().getMsoModel().resumeClientUpdate();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
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
		});
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
	
	/*============================================================
	 * Inner classes
	 *============================================================ 
	 */
	
	public abstract class ModuleWork<T> extends AbstractDiskoWork<T> {
		
		private boolean m_suspend = true;
		
		public ModuleWork() throws Exception {
			// forward
			this("Vent litt",true,true);
		}
		
		public ModuleWork(String msg) throws Exception {
			// forward
			this(msg,true,true);
		}
		
		public ModuleWork(String msg, boolean show, boolean suspend) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					msg,100,show,false);
			// save flag
			m_suspend = suspend;
		}
		
	
		@Override
		public void run() {
			// set flag to prevent reentry
			setIsWorking();
			// suspend for faster execution?
			if(m_suspend)
				suspendUpdate();			
			// forward
			super.run();
		}

		/** 
		 * Implement the work in this method. Rember to call
		 * set(*) before returning the result
		 */
		public abstract T doWork();
		
		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread.
		 * 
		 */
		@Override
		public void done() {
			try {
				// resume update?
				if(m_suspend)
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
}
