/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 14.des.2006
 * To change this template use File | Settings | File Templates.
 */
/**
 *
 */
package org.redcross.sar.mso;

import no.cmr.tools.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.redcross.sar.data.DataSourceImpl;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.modeldriver.IModelDriverIf;
import org.redcross.sar.modeldriver.ModelDriver;
import org.redcross.sar.modeldriver.SarModelDriver;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.committer.IUpdateHolderIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEventManagerImpl;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;
import org.redcross.sar.mso.work.IMsoWork;
import org.redcross.sar.util.AppProps;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.CommitException;
import org.redcross.sar.work.WorkLoop;
import org.redcross.sar.work.WorkPool;

/**
 * Singleton class for accessing the MSO model
 */
public class MsoModelImpl 	extends DataSourceImpl<MsoEvent.UpdateList>
							implements IMsoModelIf, ICommitManagerIf
{
    private static MsoModelImpl m_this;

    private final WorkLoop m_workLoop;
    private final IModelDriverIf m_modelDriver;
    private final MsoManagerImpl m_msoManager;
    private final MsoEventManagerImpl m_msoEventManager;
    private final CommitManager m_commitManager;
    private final IMsoUpdateListenerIf m_updateRepeater;

    private final Object m_lock = new Object();
    private final Stack<UpdateMode> m_updateModeStack = new Stack<UpdateMode>();

    private int m_suspendClientUpdate = 0;

  	/*========================================================
  	 * The singleton code
  	 *======================================================== */

	/**
	 * Get singleton instance of class
	 *
	 * @return Returns singleton instance of class
	 */
    public static MsoModelImpl getInstance() {
  		if (m_this == null) {
  			try {
  	  			// it's ok, we can call this constructor
				m_this = new MsoModelImpl();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  		}
  		return m_this;
  	}

	/**
	 * Method overridden to protect singleton
	 *
	 * @throws CloneNotSupportedException
	 * @return Returns nothing. Method overridden to protect singleton
	 */
  	public Object clone() throws CloneNotSupportedException{
  		throw new CloneNotSupportedException();
  		// that'll teach 'em
  	}

  	/*========================================================
  	 * Constructors
  	 *======================================================== */

    /**
     * Constructor.
     * <p/>
     * Initializes other classes that are accessed via this object..
     * @throws Exception
     */
    private MsoModelImpl() throws Exception
    {
    	// create objects
        m_msoEventManager = new MsoEventManagerImpl();
        m_msoManager = new MsoManagerImpl(this,m_msoEventManager);
        m_commitManager = new CommitManager(this);
        // create a MSO work loop and register it as deamon thread in work pool
        m_workLoop = new WorkLoop(500,5000);
        WorkPool.getInstance().add(m_workLoop);
        // initialize to local update mode
        m_updateModeStack.push(UpdateMode.LOCAL_UPDATE_MODE);
        boolean integrate = AppProps.getText("integrate.sara").equalsIgnoreCase("true");
        m_modelDriver = integrate ? new SarModelDriver() : new ModelDriver();
        // create update event repeater
        m_updateRepeater = new IMsoUpdateListenerIf() {

			public EnumSet<MsoClassCode> getInterests()
			{
				return EnumSet.allOf(MsoClassCode.class);
			}

			public void handleMsoUpdateEvent(UpdateList events)
            {
				// forward
				fireSourceChanged(new SourceEvent<MsoEvent.UpdateList>(MsoModelImpl.this,events));
            }


        };
        // connect repeater to client update events
        m_msoEventManager.addClientUpdateListener(m_updateRepeater);
    }

    /* ====================================================================
     * IMsoModelIf Implementation
     * ==================================================================== */

    public IMsoManagerIf getMsoManager()
    {
        return m_msoManager;
    }

    public IMsoEventManagerIf getEventManager()
    {
        return m_msoEventManager;
    }

    public IModelDriverIf getModelDriver()
    {
        return m_modelDriver;
    }

    /**
     * Set update mode to {@link IMsoModelIf.UpdateMode#LOCAL_UPDATE_MODE LOCAL_UPDATE_MODE}.
     */
    public synchronized void setLocalUpdateMode()
    {
        setUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);
    }

    /**
     * Set update mode to {@link IMsoModelIf.UpdateMode#REMOTE_UPDATE_MODE REMOTE_UPDATE_MODE}.
     */
    public synchronized void setRemoteUpdateMode()
    {
        setUpdateMode(UpdateMode.REMOTE_UPDATE_MODE);
    }

    /**
     * Set update mode to {@link IMsoModelIf.UpdateMode#LOOPBACK_UPDATE_MODE LOOPBACK_UPDATE_MODE}.
     */
    public synchronized void setLoopbackUpdateMode()
    {
        setUpdateMode(UpdateMode.LOOPBACK_UPDATE_MODE);
    }

    protected void setUpdateMode(UpdateMode aMode)
    {
        m_updateModeStack.push(aMode);
        if (m_updateModeStack.size() > 10)
        {
            Log.error("Update mode stack grows too large, size:" + m_updateModeStack.size());
        }
    }

    /**
     * Restore previous update mode.
     */
    public synchronized void restoreUpdateMode()
    {
    	// ensure that stack is never empty
    	// (preserves the initial condition)
        if (m_updateModeStack.size() > 1)
        {
            m_updateModeStack.pop();
        }
    }

    /**
     * This method is thread safe
     */
    public void suspendClientUpdate()
    {
    	// increment
    	synchronized(m_lock) {
	        m_suspendClientUpdate++;
    	}
        // notify of irregular operation
        if (m_suspendClientUpdate > 10)
        {
            Log.error("suspend client update stack grows too large, size:" + m_suspendClientUpdate);
        }
    }


    /**
     * This method is thread safe
     */
    public void resumeClientUpdate(boolean all)
    {
    	synchronized(m_lock) {
    		if(m_suspendClientUpdate>0)
	        	m_suspendClientUpdate--;
    	}
        if(m_suspendClientUpdate==0)
        	m_msoManager.resumeClientUpdate(all);
    }

    public synchronized boolean isUpdateSuspended()
    {
        return (m_suspendClientUpdate>0);
    }

    /**
     * Get current update mode.
     */
    public UpdateMode getUpdateMode()
    {
        return m_updateModeStack.peek();
    }

    public boolean isUpdateMode(UpdateMode mode)
    {
        return getUpdateMode().equals(mode);
    }

    /* ====================================================================
     * ICommitManagerIf Implementation
     * ==================================================================== */

    public boolean hasUncommitedChanges()
    {
        return m_commitManager.hasUncommitedChanges();
    }

    public boolean hasUncommitedChanges(MsoClassCode code) {
    	return m_commitManager.hasUncommitedChanges(code);
    }

    public boolean hasUncommitedChanges(IMsoObjectIf msoObj) {
    	return m_commitManager.hasUncommitedChanges(msoObj);
    }

	public List<IUpdateHolderIf> getUpdates() {
		return m_commitManager.getUpdates();
	}

	public List<IUpdateHolderIf> getUpdates(MsoClassCode of) {
		return m_commitManager.getUpdates(of);
	}

	public List<IUpdateHolderIf> getUpdates(Set<MsoClassCode> of) {
		return m_commitManager.getUpdates(of);
	}

	public IUpdateHolderIf getUpdates(IMsoObjectIf of) {
		return m_commitManager.getUpdates(of);
	}

	public List<IUpdateHolderIf> getUpdates(List<IMsoObjectIf> of) {
		return m_commitManager.getUpdates(of);
	}

    public synchronized void commit()
    {
        try
        {
            m_commitManager.commit();
        }
        catch (CommitException e)
        {
        	Utils.showError(e.getMessage());
            rollback();
            return;
        }
        /* ========================================================================
         * postProcessCommit() is not used any more because only server updates
         * should update the model
         * ======================================================================== */
        /*
        suspendClientUpdate();
        setLoopbackUpdateMode();
        m_msoManager.postProcessCommit();
        restoreUpdateMode();
        resumeClientUpdate();
        */
    }

    /**
     * Perform a partial commit. <p/>Only possible to perform on
     * objects that are created and modified. Object and list references
     * are not affected, only the marked attributes. See
     * {@link org.redcross.sar.mso.committer.IUpdateHolderIf} for more information.
     *
     * @param List<UpdateHolder> updates - holder for updates.
     *
     */
    public synchronized void commit(List<IUpdateHolderIf> updates)
    {
        try
        {
            m_commitManager.commit(updates);
        }
        catch (CommitException e)
        {
        	Utils.showError(e.getMessage());
            rollback();
            return;
        }
        /* ========================================================================
         * postProcessCommit() is not used any more because only server updates
         * should update the model
         * ======================================================================== */
        /*
        suspendClientUpdate();
        setLoopbackUpdateMode();
        m_msoManager.postProcessCommit();
        restoreUpdateMode();
        resumeClientUpdate();
        */
    }

    public synchronized void rollback()
    {
        suspendClientUpdate();
        setLocalUpdateMode();
        m_commitManager.rollback();
        m_msoManager.rollback();
        restoreUpdateMode();
        resumeClientUpdate(true);
    }

    /* ====================================================================
     * IDataSourceIf Implementation
     * ==================================================================== */

	@SuppressWarnings("unchecked")
	public Collection<IMsoObjectIf> getItems(Class<?> c) {
		// allowed?
		if(getMsoManager().operationExists()) {
			List list = new ArrayList(100);
			Map<String,IMsoListIf<IMsoObjectIf>> map = getMsoManager().getCmdPost().getReferenceLists(c, true);
			for(IMsoListIf<IMsoObjectIf> it : map.values()) {
				list.addAll(it.getItems());
			}
			// success
			return list;
		}
		// failed
		return null;
	}

	@Override
	public boolean isSupported(Class<?> dataClass) {
		// supports all classes that implement IMsoObjectIf
		return IMsoObjectIf.class.isAssignableFrom(dataClass);
	}

    /* ====================================================================
     *	Work pool
     * ==================================================================== */

	public long getLoopID() {
		return m_workLoop.getID();
	}

	public long schedule(IMsoWork work) {
		return m_workLoop.schedule(work);
	}

}
