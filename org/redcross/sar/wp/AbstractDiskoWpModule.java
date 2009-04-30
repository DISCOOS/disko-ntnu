package org.redcross.sar.wp;

import java.awt.Component;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.redcross.sar.Application;
import org.redcross.sar.IApplication;
import org.redcross.sar.IDiskoRole;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.factory.UIFactory.State;
import org.redcross.sar.gui.menu.NavMenu;
import org.redcross.sar.gui.panel.MainPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoTransactionManagerIf;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;
import org.redcross.sar.work.event.WorkFlowEventRepeater;

import com.esri.arcgis.interop.AutomationException;

/**
 * This abstract class is a base class that has a default implementation of the
 * IDiskoWpModule interface and the IDiskoMapEventListener interface.
 *
 * @author geira
 */
public abstract class AbstractDiskoWpModule
			implements IDiskoWp, IMsoUpdateListenerIf {

	private IDiskoRole role;
    private IDiskoMap map;
    private boolean hasSubMenu = false;
    private boolean isNavMenuSetupNeeded = true;
    private State mainState;
	private List<Enum<?>> mapLayers;

    protected String callingWp;
    protected ResourceBundle wpBundle;
    protected boolean isActive = false;

    private int isWorking = 0;

    protected final Logger logger = Logger.getLogger(getClass());
    protected final WorkFlowEventRepeater repeater = new WorkFlowEventRepeater();
    protected final List<WorkFlowEvent> changeStack = new ArrayList<WorkFlowEvent>();
	protected final EnumSet<IMsoManagerIf.MsoClassCode> interests = EnumSet.noneOf(IMsoManagerIf.MsoClassCode.class);

    public AbstractDiskoWpModule() throws IllegalClassFormatException
    {

    	// valid package name?
    	if(Utils.getPackageName(getClass()) == null)
    		throw new IllegalClassFormatException("Implementation of an IDiskoWpModule must be inside a unique package");


        // initialize layers
        this.mapLayers = getDefaultMapLayers();

		// initialize timers
        initTickTimer();
        

    }

    /**
     * @param role
     */
    public AbstractDiskoWpModule(
    		EnumSet<MsoClassCode> interests,
    		List<Enum<?>> mapLayers) 
    throws IllegalClassFormatException
    {

    	// valid package name?
    	if(Utils.getPackageName(getClass()) == null)
    		throw new IllegalClassFormatException("Implementation of an IDiskoWpModule must be inside a unique package");

        // initialize objects
        this.mapLayers = mapLayers;

        // add listeners?
    	if(interests.size()>0) {
    		getApplication().getMsoModel().
    			getEventManager().addClientUpdateListener(this);
    	}

        // initialize timer
		initTickTimer();

    }

	private static List<Enum<?>> getDefaultMapLayers() {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		list.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.AREA_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
	    return list;
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
                ((Component)map).setVisible(false);
                // success
                return true;
            }
            catch (Exception e)
            {
                logger.error("Failed to install map",e);
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

    public IApplication getApplication()
    {
        return Application.getInstance();
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

    public void addWorkFlowListener(IWorkFlowListener listener)
    {
    	repeater.addWorkFlowListener(listener);
    }

    public void removeWorkFlowListener(IWorkFlowListener listener)
    {
    	repeater.removeWorkFlowListener(listener);
    }

    public List<IMsoObjectIf> getUncomittedChanges() {
    	List<IMsoObjectIf> changes = new Vector<IMsoObjectIf>();
    	for(WorkFlowEvent it : changeStack) {
    		IMsoObjectIf msoObj = it.getMsoObject();
    		if(msoObj!=null&&!changes.contains(msoObj)) {
    			changes.add(msoObj);
    		}
    	}
    	return changes;
    }
    
	public void onFlowPerformed(WorkFlowEvent e) {
    	// update change stack?
		if(e.isChange() || e.isFinish())
			changeStack.add(e);

	    // forward
		repeater.fireOnWorkPerformed(e);
	}

    protected void fireOnWorkChange(Object data)
    {

    	// create event
		WorkFlowEvent e = new WorkFlowEvent(this,data,WorkFlowEvent.EVENT_CHANGE);

		// forward
		fireOnWorkPerformed(e);

    }

    protected void fireOnWorkRollback()
    {
    	// create event
    	WorkFlowEvent e = new WorkFlowEvent(this,null,WorkFlowEvent.EVENT_ROLLBACK);

    	// forward
    	fireOnWorkPerformed(e);
    }

    protected void fireOnWorkCommit()
    {
    	// create event
    	WorkFlowEvent e = new WorkFlowEvent(this,null,WorkFlowEvent.EVENT_COMMIT);
    	// forward
    	fireOnWorkPerformed(e);
    }

    protected void fireOnWorkPerformed(WorkFlowEvent e)
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
        setFrameText("");

        // refresh map?
        if(isMapInstalled()) {

            // activate map?
    		getMap().activate();

    		// show later
        	SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					getMap().setVisible(true);
					if(getMap().isInitMode()) {
						try {
							getMap().refreshMapBase();
						} catch (AutomationException e) {
							logger.error("Failed to active map",e);
						} catch (IOException e) {
							logger.error("Failed to active map",e);
						}
					}

				}

    		});

    		/* =======================================================
    		 * start executing all pending map work in the background.
    		 * =======================================================
    		 *
    		 * Excluding the map installed in this work process, this
    		 * will be executed later above.
    		 *
    		 * ======================================================= */

    		getApplication().getMapManager().execute(getMap(),false);

        }
        else {

        	// start executing all pending map work in the background
    		getApplication().getMapManager().execute(false);

        }


    	// load state saved when deactived?
    	if(mainState!=null) {
        	getApplication().getUIFactory().load(mainState);
    	}
    }

    /* (non-Javadoc)
    * @see org.redcross.sar.wp.IDiskoWpModule#deactivated()
    */
    public void deactivate()
    {
    	// hide map ?
    	if(isMapInstalled()) {
    		getMap().deactivate();
    		getMap().setVisible(false);
    	}
    	// save current state
    	mainState = getApplication().getUIFactory().save();
    	// hide all registered dialogs
    	getApplication().getUIFactory().hideDialogs();
		// reset flags and title
        isActive=false;
        // reset frame caption
    	setFrameText(null);
    }

    /* (non-Javadoc)
     * @see org.redcross.sar.wp.IDiskoWpModule#isNavBarSetupNeeded()
     */
    public boolean isNavMenuSetupNeeded() {
    	return isNavMenuSetupNeeded;
    }

    /* (non-Javadoc)
     * @see org.redcross.sar.wp.IDiskoWpModule#isNavBarSetupNeeded()
     */
    public void setupNavMenu(List<Enum<?>> buttons, boolean isVisible) {
    	// get navbar
    	NavMenu navBar = getApplication().getNavMenu();
    	// show buttons
    	navBar.setVisibleButtons(buttons,true,false);
    	// setup navbar
    	navBar.setup();
		// get nav button
		getApplication().getUIFactory().setNavMenuVisible(isVisible);
    	// save state
		mainState =  getApplication().getUIFactory().save();
		// do not need to do setup any more
		isNavMenuSetupNeeded = false;
    }

    public void setFrameText(String text)
    {
        String s = "DISKO (" + getDiskoRole().getTitle() + ") - <Arbeidsprossess: " + getBundleText(getName()) +"> - ";
        String o = "<Aksjon: " + getApplication().getMsoModel().getDispatcher().getActiveOperationName() + ">";
        if (text != null && !text.isEmpty()) {
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
        getApplication().getUIFactory().getSubMenu().addItem(button, id, addToGroup);
        // set sub menu existence flag
        hasSubMenu = true;
    }

    public IMsoModelIf getMsoModel()
    {
        return getApplication().getMsoModel();
    }

    public IMsoTransactionManagerIf getCommitManager()
    {
        return getApplication().getTransactionManager();
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
    	if(getMsoManager().operationExists())
    		return getMsoManager().getCmdPost();
    	else
    		return null;
    }

    public String getBundleText(String aKey)
    {
        return Internationalization.getString(wpBundle,aKey);
    }

    protected void assignWpBundle(Class<?> aClass)
    {
        wpBundle = Internationalization.getBundle(aClass);
    }

    public boolean confirmDeactivate()
    {
    	// TODO: Override this method
    	return true;
    }

	public EnumSet<MsoClassCode> getInterests() {
		return interests;
	}

	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {
		// TODO: Override this method
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
				logger.error("Failed to refresh map",e);
			}
		}
		// update title bar text?
		if(isActive()) setFrameText(null);
	}

	public void suspendUpdate() {
		Application.getInstance().getMsoModel().suspendClientUpdate();
		if(map!=null) {
			map.suspendNotify();
			map.setSupressDrawing(true);
		}
	}

	public void resumeUpdate() {
		Application.getInstance().getMsoModel().resumeClientUpdate(true);
		if(map!=null) {
			try {
				map.setSupressDrawing(false);
				map.refreshMsoLayers();
				map.resumeNotify();
			}
			catch(Exception e) {
				logger.error("Failed to resume update",e);
			}
		}
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
	 * Global timer implementation
	 *============================================================ */

    private static final int TIMER_DELAY = 1000; // 1 second
    private static final Timer timer = new Timer(true);
    private static final WpTicker ticker = new WpTicker();
    private static final EventListenerList tickListeners = new EventListenerList();

    private static boolean isTimerRunning = false;
    private static long tickTime = 0;

    /**
     * Creates a timer for generating {@link TickEvent} objects periodically.
     */
    private static void initTickTimer()
    {
    	// initialize?
    	if(!isTimerRunning) {
    		// set flag to prevent more than one timer running
    		isTimerRunning = true;
    		// schedule ticker work
	        timer.schedule(new TimerTask()
	        {
	            public void run()
	            {
	                long newTickTime = System.currentTimeMillis();
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
    }

    public void addTickEventListener(ITickEventListenerIf listener)
    {
        tickListeners.add(ITickEventListenerIf.class,listener);
    }

    public void removeTickEventListener(ITickEventListenerIf listener)
    {
        tickListeners.remove(ITickEventListenerIf.class,listener);
    }

    /**
     * Count down the interval timer for each listener and fire tick events to all listeners where interval time has expired.
     *
     * @param aMilli Time in milliseconds since previous call.
     */
    protected static void fireTick(long aMilli)
    {

        // ensure access to listeners are serialized
        synchronized(tickListeners) {

	        if (tickListeners.getListenerCount() == 0 || aMilli == 0)
	        {
	            return;
	        }

	        ITickEventListenerIf[] list = tickListeners.getListeners(ITickEventListenerIf.class);

	        for (int i=0; i<list.length; i++)
	        {

	        	ITickEventListenerIf listener = list[i];

	        	TickEvent e = new TickEvent(listener);

	            long timer = listener.getTimeCounter() - aMilli;
	            if (timer > 0)
	            {
	                listener.setTimeCounter(timer);
	            } else
	            {
	                listener.handleTick(e);
	                listener.setTimeCounter(listener.getInterval());
	            }
	        }
        }

    }


	/*============================================================
	 * Inner classes
	 *============================================================
	 */

    /**
     * Class that embeds a runnable that performs the GUI updates by firing the ticks to the listeners.
     *
     * The run() method is run with the latest given elapsed time.
     */
    private static class WpTicker implements Runnable
    {
        long m_elapsedTime;

        void setElapsedTime(long aTime)
        {
            m_elapsedTime = aTime;
        }

        public void run()
        {
            SwingUtilities.invokeLater(new Runnable() {
				
            	public void run() {
					fireTick(m_elapsedTime);
				
			}});
        }
    }

    public abstract class ModuleWork extends AbstractWork {

    	// Override
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
			super(0,false,true,ThreadType.WORK_ON_SAFE,msg,100,show,false);
			// prepare
			m_suspend = suspend;
		}


		@Override
		public void beforePrepare() {
			// set flag to prevent reentry
			setIsWorking();
			// suspend for faster execution?
			if(m_suspend) suspendUpdate();
		}

		/**
		 * Implement the work in this method. Remember to call
		 * ModuleWork::set() before returning the result
		 */
		public abstract Object doWork();

		/**
		 * done
		 *
		 * Executed on the Event Dispatch Thread.
		 *
		 */
		@Override
		public void afterDone() {
			try {
				// resume update?
				if(m_suspend) resumeUpdate();
				// reset flag
		        setIsNotWorking();
			}
			catch(Exception e) {
				logger.error("Failed to resume suspended update",e);
			}
		}
	}
}
