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

import org.apache.log4j.Logger;
import org.redcross.sar.data.AbstractDataSource;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEventManagerImpl;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;
import org.redcross.sar.mso.work.IMsoWork;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.work.WorkLoop;
import org.redcross.sar.work.WorkPool;

/**
 * Singleton class for accessing the MSO model
 */
public class MsoModelImpl 	extends AbstractDataSource<MsoEvent.UpdateList>
							implements IMsoModelIf
{
 
	private static final Logger m_logger = Logger.getLogger(MsoModelImpl.class); 
	
	private final WorkLoop m_workLoop;
    private final IDispatcherIf m_dispatcher;
    private final MsoManagerImpl m_msoManager;
    private final MsoEventManagerImpl m_msoEventManager;
    private final TransactionManagerImpl m_msoTransactionManager;
    private final IMsoUpdateListenerIf m_updateRepeater;

    private final Object m_lock = new Object();
    private final Stack<UpdateMode> m_updateModeStack = new Stack<UpdateMode>();

    private int m_suspendClientUpdate = 0;
    private boolean m_isEditable = true;

  	/*========================================================
  	 * Constructors
  	 *======================================================== */

    /**
     * Constructor.
     * <p/>
     * @throws Exception
     */
    public MsoModelImpl(IDispatcherIf dispatcher) throws Exception
    {
    	// prepare
    	m_dispatcher = dispatcher;
    	// create objects
        m_msoEventManager = new MsoEventManagerImpl();
        m_msoManager = new MsoManagerImpl(this,m_msoEventManager);
        m_msoTransactionManager = new TransactionManagerImpl(this);
        // create a MSO work loop and register it as deamon thread in work pool
        m_workLoop = new WorkLoop(500,5000);
        WorkPool.getInstance().add(m_workLoop);
        // initialize to local update mode
        m_updateModeStack.push(UpdateMode.LOCAL_UPDATE_MODE);
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
     * IDataSource implementation
     * ==================================================================== */
    
    public String getID() {
    	return m_dispatcher.getActiveOperationID();
    }
    
    public boolean isAvailable() {
    	return getMsoManager().operationExists();
    }
    
    /* ====================================================================
     * IMsoModelIf implementation
     * ==================================================================== */

	public boolean isEditable() {
		return m_isEditable;
	}
	
    public IMsoManagerIf getMsoManager()
    {
        return m_msoManager;
    }

    public IMsoEventManagerIf getEventManager()
    {
        return m_msoEventManager;
    }

    public IDispatcherIf getDispatcher()
    {
        return m_dispatcher;
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
        return m_msoTransactionManager.hasUncommitedChanges();
    }

    public boolean hasUncommitedChanges(MsoClassCode code) {
    	return m_msoTransactionManager.hasUncommitedChanges(code);
    }

    public boolean hasUncommitedChanges(IMsoObjectIf msoObj) {
    	return m_msoTransactionManager.hasUncommitedChanges(msoObj);
    }

	public List<IChangeSourceIf> getChanges() {
		return m_msoTransactionManager.getChanges();
	}

	public List<IChangeSourceIf> getChanges(MsoClassCode of) {
		return m_msoTransactionManager.getChanges(of);
	}

	public List<IChangeSourceIf> getChanges(Set<MsoClassCode> of) {
		return m_msoTransactionManager.getChanges(of);
	}

	public IChangeSourceIf getChanges(IMsoObjectIf of) {
		return m_msoTransactionManager.getChanges(of);
	}

	public List<IChangeSourceIf> getChanges(List<IMsoObjectIf> of) {
		return m_msoTransactionManager.getChanges(of);
	}

    /**
     * Perform a commit of all changes. <p/>
     * 
     * Generates a {@link org.redcross.sar.mso.event.MsoEvent.Commit} event.
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public synchronized void commit()
    {
        try
        {
            m_msoTransactionManager.commit();
        }
        catch (TransactionException e)
        {
        	m_logger.error("Failed to commit changes",e);
            rollback();
        	Utils.showError(e.getMessage());
            return;
        }
    }

    /**
     * Perform a commit on a subset of all changes<p/>
     * 
     * Note that partial commits (attributes only) is only possible to perform on objects 
     * that exists remotely (modified). If a IChangeSourceIf is marked for partial commit, object references 
     * and list references are not affected, only the marked attributes. See 
     * {@link org.redcross.sar.mso.IChangeSourceIf} for more information.
     *
     * @param UpdateHolder updates - holder for updates.
     *
     */
    public synchronized void commit(IChangeSourceIf changes)
    {
        try
        {
            m_msoTransactionManager.commit(changes);
        }
        catch (TransactionException e)
        {
        	m_logger.error("Failed to commit changes",e);
            rollback();
        	Utils.showError(e.getMessage());
            return;
        }
    }
    
    /**
     * Perform a commit on a subset of all changes<p/>
     * 
     * Note that partial commits (attributes only) is only possible to perform on objects 
     * that exists remotely (modified). If a IChangeSourceIf is marked for partial commit, object references 
     * and list references are not affected, only the marked attributes. See 
     * {@link org.redcross.sar.mso.IChangeSourceIf} for more information.
     * 
     * @param List<UpdateHolder> updates - list of holders of updates.
     *
     */
    public synchronized void commit(List<IChangeSourceIf> changes)
    {
        try
        {
            m_msoTransactionManager.commit(changes);
        }
        catch (TransactionException e)
        {
        	m_logger.error("Failed to commit changes",e);
            rollback();
        	Utils.showError(e.getMessage());
        }
    }    
    
    /**
     * Performs a rollback of all changes. <p/>
     * 
     * Clears all accumulated information.
     */
    public synchronized void rollback()
    {
        try
        {
            m_msoTransactionManager.rollback();
        }
        catch (TransactionException e)
        {
        	m_logger.error("Failed to rollback changes",e);
        	Utils.showError(e.getMessage());
        }
    }

    /**
     * Perform a rollback on a subset of all changes<p/>
     * 
     * @param UpdateHolder updates - holder for updates
     */
    public synchronized void rollback(IChangeSourceIf changes){
        try
        {
            m_msoTransactionManager.rollback(changes);    	
        }
        catch (TransactionException e)
        {
        	m_logger.error("Failed to rollback changes",e);
        	Utils.showError(e.getMessage());
        }
    }

    /**
     * Perform a rollback on a subset of all changes<p/>
     * 
     * @param List<UpdateHolder> updates - list of holders of updates
     */
    public synchronized void rollback(List<IChangeSourceIf> changes) {
        try
        {
            m_msoTransactionManager.rollback(changes);    	    	
        }
        catch (TransactionException e)
        {
        	m_logger.error("Failed to rollback changes",e);
        	Utils.showError(e.getMessage());
        }
    }    
    
    public IMsoTransactionManagerIf getMsoTransactionManager() {
    	return m_msoTransactionManager;
    }

    /* ====================================================================
     * IDataSourceIf Implementation
     * ==================================================================== */

	@SuppressWarnings("unchecked")
	public Collection<IMsoObjectIf> getItems(Class<?> c) {
		// allowed?
		if(isAvailable()) {
			List list = new ArrayList(100);
			Map<String,IMsoListIf<IMsoObjectIf>> map = getMsoManager().getCmdPost().getListReferences(c, true);
			for(IMsoListIf<IMsoObjectIf> it : map.values()) {
				list.addAll(it.getItems());
			}
			// success
			return list;
		}
		// failed
		return null;
	}
	
    @SuppressWarnings("unchecked")
	public Collection<?> getItems(Enum<?> e) {
    	// allowed?
		if(isAvailable() && e instanceof MsoClassCode) {
			List list = new ArrayList(100);
			Map<String,IMsoListIf<IMsoObjectIf>> map = getMsoManager().getCmdPost().getListReferences((MsoClassCode)e);
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
