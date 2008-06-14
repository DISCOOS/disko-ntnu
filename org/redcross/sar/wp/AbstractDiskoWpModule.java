package org.redcross.sar.wp;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkRepeater;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.panel.MainPanel;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.gui.panel.NavBarPanel.NavState;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.MapUtil;
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

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.interop.AutomationException;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * This abstract class is a base class that has a default implementation of the
 * IDiskoWpModule interface and the IDiskoMapEventListener interface.
 *
 * @author geira
 */
public abstract class AbstractDiskoWpModule 
			implements IDiskoWp, IMsoUpdateListenerIf, IDiskoWorkListener {
	
	private IDiskoRole role = null;
    private IDiskoMap map = null;
    private boolean hasSubMenu = false;
    private NavState m_navState = null;
    private boolean isNavBarSetupNeeded = true;
	private EnumSet<IMsoManagerIf.MsoClassCode> wpInterests = null;
	private EnumSet<IMsoFeatureLayer.LayerCode> mapLayers = null;
	
    protected String callingWp = null;
    protected ResourceBundle wpBundle;
    protected boolean isActive = false;
    
    protected final DiskoWorkRepeater repeater;

    private int isWorking = 0;
    
    private final ArrayList<ITickEventListenerIf> tickListeners;

    protected final ArrayList<DiskoWorkEvent> changeStack;
    
    private final TickEvent tickEvent;
    
    private long tickTime = 0;

    /**
     */
    public AbstractDiskoWpModule() throws IllegalClassFormatException
    {
        
    	// valid package name?
    	if(Utils.getPackageName(getClass()) == null) 
    		throw new IllegalClassFormatException("Implementation of an IDiskoWpModule must be inside a unique package");

    	// prepare
        this.repeater = new DiskoWorkRepeater();
        this.tickListeners = new ArrayList<ITickEventListenerIf>();
        this.tickEvent = new TickEvent(this);
        this.changeStack = new ArrayList<DiskoWorkEvent>();

        // initialize interests
        this.wpInterests = EnumSet.noneOf(IMsoManagerIf.MsoClassCode.class);
        
        // initialize layers
        this.mapLayers = getDefaultMapLayers();
        
		// initialize timers
        initTickTimer();
        
    }

    /**
     * @param role
     */
    public AbstractDiskoWpModule(EnumSet<IMsoManagerIf.MsoClassCode> wpInterests,
    		EnumSet<IMsoFeatureLayer.LayerCode> mapLayers)
    {
        // initialize objects
        this.repeater = new DiskoWorkRepeater();
        this.tickListeners = new ArrayList<ITickEventListenerIf>();
        this.tickEvent = new TickEvent(this);
        this.changeStack = new ArrayList<DiskoWorkEvent>();
        this.wpInterests  = wpInterests;
        this.mapLayers = mapLayers;

        // add listeners?
    	if(wpInterests.size()>0) {
    		getApplication().getMsoModel().
    			getEventManager().addClientUpdateListener(this);
    	}

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

    public boolean isMapInstalled() {
    	return map!=null;
    }
    
    
    public boolean installMap() {
        if (map == null)
        {
            try
            {
            	// get map manager
                IDiskoMapManager manager = getApplication().getMapManager();
                // initialize map
                map = manager.createMap(mapLayers);
                // hide map
                ((DiskoMap)map).setVisible(false);
                // success
                return true;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // failed
        return false;
    }

    /* (non-Javadoc)
      * @see org.redcross.sar.wp.IDiskoWpModule#getMap()
      */
    public IDiskoMap getMap()
    {
        return map;
    }

    public IDiskoApplication getApplication()
    {
        return Utils.getApp();
    }

    /* (non-Javadoc)
      * @see org.redcross.sar.wp.IDiskoWpModule#getName()
      *
      * Extenders should not override this method!
      *
      */
     public String getName()
     {
         return Utils.getPackageName(getClass());
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
      * @see org.redcross.sar.wp.IDiskoWpModule#hasSubMenu()
      */
    public boolean hasSubMenu()
    {
        return hasSubMenu;
    }

	public boolean commit() {
		// get flag
		boolean bFlag = isChanged();
		// notify
		fireOnWorkCommit();
		// finished
		return bFlag;
	}

	public boolean rollback() {
		// get flag
		boolean bFlag = isChanged();
		// notify
		fireOnWorkRollback();
		// finished
		return bFlag;
	}

    public void addDiskoWorkListener(IDiskoWorkListener listener)
    {
    	repeater.addDiskoWorkListener(listener);
    }

    public void removeDiskoWorkListener(IDiskoWorkListener listener)
    {
    	repeater.removeDiskoWorkListener(listener);
    }

	public void onWorkPerformed(DiskoWorkEvent e) {
    	// update change stack?
		if(e.isChange() || e.isFinish())			
			changeStack.add(e);
		
	    // forward
		repeater.fireOnWorkPerformed(e);
	}

    protected void fireOnWorkChange(Object data)
    {

    	// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,data,DiskoWorkEvent.EVENT_CHANGE);
    	
		// forward
		fireOnWorkPerformed(e);
    	
    }
    
    protected void fireOnWorkRollback()
    {
    	// create event
    	DiskoWorkEvent e = new DiskoWorkEvent(this,null,DiskoWorkEvent.EVENT_ROLLBACK);    	

    	// forward
    	fireOnWorkPerformed(e);
    }
    
    protected void fireOnWorkCommit()
    {
    	// create event
    	DiskoWorkEvent e = new DiskoWorkEvent(this,null,DiskoWorkEvent.EVENT_COMMIT);
    	// forward
    	fireOnWorkPerformed(e);
    }
    
    protected void fireOnWorkPerformed(DiskoWorkEvent e)
    {
    	// update change stack?
		if(e.isCommit() || e.isRollback())
			changeStack.clear();
		
		// forward
		repeater.fireOnWorkPerformed(e);
    }

    /**
     * @return True if change count is larger then zero
     */
    public boolean isChanged()
    {
        return (changeStack.size()>0);
    }

    /**
     * @return True if change count is larger then zero
     */
    public boolean isActive()
    {
        return isActive;
    }

    public void activate(IDiskoRole role)
    {
    	// set active
        isActive=true;
        
        // prepare
        this.role = role;
        
        // update frame text
        setFrameText(null);
        
        // refresh map?
        if(isMapInstalled()) {
    		
        	SwingUtilities.invokeLater(new Runnable() {

				public void run() {
		        	try {
		        		if(getMap().isInitMode()) {
		        			// reset flag
		        			getMap().setInitMode(false);
		        			// initialize
		        			IEnvelope e = MapUtil.getOperationExtent(getMap());
		        			// set extent?
		        			if(e!=null && !e.isEmpty()) 
		        				getMap().setExtent(MapUtil.expand(1.25,e));
		        			// update
		        			getMap().refreshGeography(null, map.getExtent());
		        		}
						getMap().setVisible(true);
					} catch (AutomationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}						
				}        			
    		});
        }
    	setFrameText(null);
    	// has nav state?
    	if(m_navState!=null) {
        	// get navbar
        	NavBarPanel navBar = getApplication().getUIFactory().getMainPanel().getNavBar();
    		// load state
        	navBar.load(m_navState);
    	}
    }

    /* (non-Javadoc)
    * @see org.redcross.sar.wp.IDiskoWpModule#deactivated()
    */
    public void deactivate()
    {
    	// hide map ?
    	if(isMapInstalled()) getMap().setVisible(false);
    	
    	// get navbar
    	NavBarPanel navBar = getApplication().getUIFactory().getMainPanel().getNavBar();
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
    public void setupNavBar(List<Enum<?>> buttons, boolean isSelected) {
    	// get navbar
    	NavBarPanel navBar = getApplication().getNavBar();
    	// show buttons
    	navBar.setVisibleButtons(buttons,true,false);
    	// setup navbar
    	navBar.setup();
		// get nav button
		UIFactory uiFactory = getApplication().getUIFactory();
		AbstractButton navButton = uiFactory.getMainMenuPanel().getNavToggleButton();
		// update navbar selection status
		if (navButton.isSelected() != isSelected) {
			navButton.doClick();			
		}
    	// save state
		m_navState =  navBar.save();
		// do not need to do setup any more
		isNavBarSetupNeeded = false;
    }
    
    public void setFrameText(String text)
    {
        String s = "DISKO (" + getDiskoRole().getTitle() + ") - <Arbeidsprossess: " + getBundleText(getName()) +"> - ";
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
		String msg = getBundleText(key); 
		// forward
        Utils.showWarning(msg);
    }

	public void showMessage(String key)
    {
		// Internationalization
		String msg = getBundleText(key); 
		// forward
        Utils.showMessage(msg);
    }
	
    protected void layoutComponent(JComponent comp)
    {
        String id = getName();
        MainPanel mainPanel = getApplication().getUIFactory().getMainPanel();
        mainPanel.addComponent(comp, id);
    }

    protected void layoutButton(AbstractButton button)
    {
        layoutButton(button, true);
    }

    protected void layoutButton(AbstractButton button, boolean addToGroup)
    {
    	// get menu name
        String id = getName();
        // add button to sub menu
        getApplication().getUIFactory().getSubMenuPanel().addItem(button, id, addToGroup);
        // set sub menu existence flag
        hasSubMenu = true;
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

    public String getBundleText(String aKey)
    {
        return Internationalization.getString(wpBundle,aKey);
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
		// clear stack
		changeStack.clear();
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
		//SwingUtilities.invokeLater(new Runnable() {
		//	public void run() {
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
		//	}			
		//});
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
