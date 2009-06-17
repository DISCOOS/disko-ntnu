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
import org.redcross.sar.mso.event.MsoEvent.ChangeList;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.TransactionException;

/**
 * Singleton class for accessing the MSO model
 */
public class MsoModelImpl 	extends AbstractDataSource<MsoEvent.ChangeList>
							implements IMsoModelIf
{
 
	private static final Logger m_logger = Logger.getLogger(MsoModelImpl.class); 
	
    private final IDispatcherIf m_dispatcher;
    private final MsoManagerImpl m_msoManager;
    private final MsoEventManagerImpl m_msoEventManager;
    private final TransactionManagerImpl m_msoTransactionManager;
    private final IMsoUpdateListenerIf m_updateRepeater;

    private final Object m_lock = new Object();
    private final Stack<UpdateMode> m_updateModeStack = new Stack<UpdateMode>();

    private int m_suspendUpdate = 0;
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
        // initialize to local update mode
        m_updateModeStack.push(UpdateMode.LOCAL_UPDATE_MODE);
        // create update event repeater
        m_updateRepeater = new IMsoUpdateListenerIf() {

			public EnumSet<MsoClassCode> getInterests()
			{
				return EnumSet.allOf(MsoClassCode.class);
			}

			public void handleMsoChangeEvent(ChangeList events)
            {
				// forward
				fireSourceChanged(new SourceEvent<MsoEvent.ChangeList>(MsoModelImpl.this,events));
            }


        };
        // connect repeater to client update events
        m_msoEventManager.addLocalUpdateListener(m_updateRepeater);
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

    @Override
    public boolean exists()
    {
    	return getMsoManager().operationExists();
    }
    
    @Override
    public boolean isDeleted()
    {
    	return getMsoManager().isOperationDeleted();
    }
    
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
    public void suspendUpdate()
    {
    	// increment
    	synchronized(m_lock) {
	        m_suspendUpdate++;
    	}
        // notify of irregular operation
        if (m_suspendUpdate > 10)
        {
            Log.error("suspend client update stack grows too large, size:" + m_suspendUpdate);
        }
    }

    public void resumeUpdate()
    {
    	synchronized(m_lock) {
    		if(m_suspendUpdate>0)
	        	m_suspendUpdate--;
    	}
        if(m_suspendUpdate==0)
        	m_msoManager.resumeUpdate();
    }

    public synchronized boolean isUpdateSuspended()
    {
        return (m_suspendUpdate>0);
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

    public boolean isChanged()
    {
        return m_msoTransactionManager.isChanged();
    }

    public boolean isChanged(MsoClassCode code) {
    	return m_msoTransactionManager.isChanged(code);
    }

    public boolean isChanged(IMsoObjectIf msoObj) {
    	return m_msoTransactionManager.isChanged(msoObj);
    }

	public List<IChangeRecordIf> getChanges() {
		return m_msoTransactionManager.getChanges();
	}

	public List<IChangeRecordIf> getChanges(MsoClassCode of) {
		return m_msoTransactionManager.getChanges(of);
	}

	public List<IChangeRecordIf> getChanges(Set<MsoClassCode> of) {
		return m_msoTransactionManager.getChanges(of);
	}

	public IChangeRecordIf getChanges(IMsoObjectIf of) {
		return m_msoTransactionManager.getChanges(of);
	}

	public List<IChangeRecordIf> getChanges(List<IMsoObjectIf> of) {
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
     * {@link org.redcross.sar.mso.IChangeRecordIf} for more information.
     *
     * @param UpdateHolder updates - holder for updates.
     *
     */
    public synchronized void commit(IChangeRecordIf changes)
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
     * {@link org.redcross.sar.mso.IChangeRecordIf} for more information.
     * 
     * @param List<UpdateHolder> updates - list of holders of updates.
     *
     */
    public synchronized void commit(List<IChangeRecordIf> changes)
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
    public synchronized void rollback(IChangeRecordIf changes){
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
    public synchronized void rollback(List<IChangeRecordIf> changes) {
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
			Map<String,IMsoListIf<?>> map = getMsoManager().getCmdPost().getListRelations(c, true);
			for(IMsoListIf<?> it : map.values()) {
				list.addAll(it.getObjects());
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
			Map<String,IMsoListIf<?>> map = getMsoManager().getCmdPost().getListRelations((MsoClassCode)e);
			for(IMsoListIf<?> it : map.values()) {
				list.addAll(it.getObjects());
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
	
}
