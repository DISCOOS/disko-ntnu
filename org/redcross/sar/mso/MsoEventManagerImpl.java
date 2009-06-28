package org.redcross.sar.mso;

import no.cmr.tools.Log;

import org.apache.log4j.Logger;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.ITransactionIf.TransactionType;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoChangeListenerIf;
import org.redcross.sar.mso.event.IMsoCoChangeListenerIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoTransactionListenerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.work.ProgressMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Class for managing MsoUpdateEvents.
 * <p/>
 * Manages listener (observer) sets and notifications to the listeners.
 */
public class MsoEventManagerImpl implements IMsoEventManagerIf
{

	/**
	 * Map from MsoClassCode to client update listeners that is interested in each MsoClassCodes
	 */
    private final Map<MsoClassCode, List<IMsoChangeListenerIf>>
    	m_slaveChangeListeners = new HashMap<MsoClassCode,List<IMsoChangeListenerIf>>();

    /**
	 * Map from MsoClassCode to server update listeners that is interested in each MsoClassCodes
	 */
    private final Map<MsoClassCode, List<IMsoUpdateListenerIf>>
    	m_masterUpdateListeners = new HashMap<MsoClassCode,List<IMsoUpdateListenerIf>>();

	/**
	 * Collections of commit listeners
	 */
    private final Collection<IMsoTransactionListenerIf> m_commitListeners = new Vector<IMsoTransactionListenerIf>();

	/**
	 * Collections of Co update listeners
	 */
    private final Collection<IMsoCoChangeListenerIf> m_coUpdateListeners = new Vector<IMsoCoChangeListenerIf>();


	/**
	 * Current progress monitor
	 */
    private ProgressMonitor m_monitor;

    /**
     * The number of started resume client update operations
     */
    private int m_resumeCount;

    /**
     * Pending updates
     */
    private final Collection<MsoEvent.Change> m_buffer = new Vector<MsoEvent.Change>(100);
    
    /**
     * Logging object
     */
    private final Logger m_logger = Logger.getLogger(MsoEventManagerImpl.class);
    
    /**
     * The Mso model instance
     */
    private final IMsoModelIf m_model;

    
    protected MsoEventManagerImpl(IMsoModelIf aMsoModel) {
    	m_model = aMsoModel;
	}
    
    /* ------------------------------------------------------------------
     * IMsoEventManagerIf implementation
     * ------------------------------------------------------------------ */

	/**
     * Add a listener in the {@link #m_slaveChangeListeners} queue.
     */
    public void addChangeListener(IMsoChangeListenerIf aListener)
    {
        defineInterests(m_slaveChangeListeners,aListener,aListener.getInterests());
    }

    /**
     * Remove a listener in the {@link #m_slaveChangeListeners} queue.
     */
    public void removeChangeListener(IMsoChangeListenerIf aListener)
    {
        removeInterests(m_slaveChangeListeners,aListener);
    }

    public void notifyClearAll(IMsoObjectIf root)
    {
    	// create change object
    	IChangeRecordIf aChange = new ChangeRecordImpl(root,
    			m_model.getUpdateMode(),
    			MsoEventType.CLEAR_ALL_EVENT);
        // notify
        fireSlaveChange(m_slaveChangeListeners, getUpdateEvents(aChange));
    }

    public void notifyChange(IChangeRecordIf aChange)
    {
    	// consume?
    	if(aChange.getMask()!=0)
    	{
    		// notify now?
    		if(m_resumeCount==0)
    		{
    			fireSlaveChange(m_slaveChangeListeners,
    					getUpdateEvents(aChange));
    		}
    		else
    		{
    			// forward
    			bufferChange(m_buffer,new MsoEvent.Change(aChange));
    		}

    	}
    }

    private void bufferChange(Collection<MsoEvent.Change> buffer, MsoEvent.Change e) {
    	// get source
    	IMsoObjectIf msoObj = e.getSource();
    	// search for existing event
		for(MsoEvent.Change it : buffer) {
			if(it.getSource() == msoObj) {
				it.union(e); return;
			}
		}
		// add as new
		buffer.add(e);
    }

    public void addUpdateListener(IMsoUpdateListenerIf aListener)
    {
        defineInterests(m_masterUpdateListeners,aListener,aListener.getInterests());
    }

    public void removeUpdateListener(IMsoUpdateListenerIf aListener)
    {
        removeInterests(m_masterUpdateListeners,aListener);
    }

    public void notifyUpdate(IChangeRecordIf aChange)
    {
    	// consume?
    	if(aChange.getMask()!=0)
    	{
	        fireMasterUpdate(m_masterUpdateListeners, getUpdateEvents(aChange));
    	}
    }

    /**
     * Add a listener in the {@link #m_commitListeners} queue.
     */
    public void addTransactionListener(IMsoTransactionListenerIf aListener)
    {
        m_commitListeners.add(aListener);
    }

    /**
     * Remove a listener in the {@link #m_commitListeners} queue.
     */
    public void removeTransactionListener(IMsoTransactionListenerIf aListener)
    {
        m_commitListeners.remove(aListener);
    }

    public void notifyCommit(ITransactionIf aSource)  throws TransactionException
    {
        fireCommit(m_commitListeners, aSource);
    }

    public ProgressMonitor getProgressMonitor() {
        if(m_monitor == null) {
            try {
            	m_monitor = ProgressMonitor.getInstance();
            } catch (Exception ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
        return m_monitor;
    }



    private void fireSlaveChange(
    		Map<MsoClassCode, List<IMsoChangeListenerIf>> theListeners,
    		MsoEvent.ChangeList events)
    {
        final long WORK_TIME = 100;

        if (theListeners.size() == 0)
        {
            return;
        }

        // initialize
        long tic = System.currentTimeMillis();
        List<IMsoChangeListenerIf> list = new ArrayList<IMsoChangeListenerIf>(100);;

        // get all?
        if (events.isClearAllEvent())
        {
        	for(List<IMsoChangeListenerIf> it : theListeners.values()) {
        		for(IMsoChangeListenerIf listener : it) {
        			if(!list.contains(listener)) list.add(listener);
        		}
        	}
        }
        else
        {
        	// only notify those that are interested
        	for(MsoEvent.Change it : events.getEvents()) {
        		List<IMsoChangeListenerIf> interested = theListeners.get(it.getSource().getClassCode());
        		if(interested!=null) {
            		for(IMsoChangeListenerIf listener : interested) {
            			if(!list.contains(listener)) list.add(listener);
            		}
        		}
        	}
        }

        // get array of interested listeners to prevent concurrent modification
        IMsoChangeListenerIf[] items = new IMsoChangeListenerIf[list.size()];
 
        // loop over all listeners
        for (IMsoChangeListenerIf  it : list.toArray(items))
        {
            try
            {
                it.handleMsoChangeEvent(events);

            }
            catch (Exception ex)
            {
            	String msg = "Exception in fireUpdate, listener: " + it.toString();
            	m_logger.error(msg,ex);
                Log.printStackTrace(msg,ex);
            }

            // update progress monitor?
            if(WORK_TIME<System.currentTimeMillis()-tic) {
                Thread.yield();
                getProgressMonitor().refreshProgress();
                tic = System.currentTimeMillis();
            }

        }
    }

    private void fireMasterUpdate(
    		Map<MsoClassCode, List<IMsoUpdateListenerIf>> theListeners,
    		MsoEvent.ChangeList events)
    {
        final long WORK_TIME = 100;

        if (theListeners.size() == 0)
        {
            return;
        }

        // initialize
        long tic = System.currentTimeMillis();
        List<IMsoUpdateListenerIf> list = new ArrayList<IMsoUpdateListenerIf>(100);;

        // get all?
        if (events.isClearAllEvent())
        {
        	for(List<IMsoUpdateListenerIf> it : theListeners.values()) {
        		for(IMsoUpdateListenerIf listener : it) {
        			if(!list.contains(listener)) list.add(listener);
        		}
        	}
        }
        else
        {
        	// only notify those that are interested
        	for(MsoEvent.Change it : events.getEvents()) {
        		List<IMsoUpdateListenerIf> interested = theListeners.get(it.getSource().getClassCode());
        		if(interested!=null) {
            		for(IMsoUpdateListenerIf listener : interested) {
            			if(!list.contains(listener)) list.add(listener);
            		}
        		}
        	}
        }

        // get array of interested listeners to prevent concurrent modification
        IMsoUpdateListenerIf[] items = new IMsoUpdateListenerIf[list.size()];
        
        // loop over all listeners
        for (IMsoUpdateListenerIf it : list.toArray(items))
        {
            try
            {
                it.handleMsoUpdateEvent(events);

            }
            catch (Exception ex)
            {
            	String msg = "Exception in fireUpdate, listener: " + it.toString();
            	m_logger.error(msg,ex);
                Log.printStackTrace(msg,ex);
            }

            // update progress monitor?
            if(WORK_TIME<System.currentTimeMillis()-tic) {
                Thread.yield();
                getProgressMonitor().refreshProgress();
                tic = System.currentTimeMillis();
            }

        }
    }
    
    private void fireCommit(Collection<IMsoTransactionListenerIf> theListeners, ITransactionIf aSource) throws TransactionException
    {
    	// vaild commit data?
        if (aSource==null 
        		|| TransactionType.COMMIT.equals(aSource.getType()) 
        		|| theListeners.size() == 0)
        {
            return;
        }
        // notify listeners
        MsoEvent.Commit event = new MsoEvent.Commit(aSource);
        for (IMsoTransactionListenerIf listener : theListeners)
        {
            try
            {
                listener.handleMsoCommitEvent(event);
            }
            catch (Exception e)
            {
            	
            	String msg = "Exception in fireCommit, listener: " + listener.toString();
            	m_logger.error(msg,e);
                Log.printStackTrace(msg,e);
                if (e instanceof  TransactionException)
                {
                    throw (TransactionException)e;
                }
            }
        }
    }

    private MsoEvent.ChangeList getUpdateEvents(IChangeRecordIf aChange)
    {
     	MsoEvent.Change e = new MsoEvent.Change(aChange);
        return new MsoEvent.ChangeList(getUpdateEvents(e),e.isClearAllEvent());
    }

    private List<MsoEvent.Change> getUpdateEvents(MsoEvent.Change e) {
    	List<MsoEvent.Change> list = new ArrayList<MsoEvent.Change>(1);
    	list.add(e);
    	return list;
    }

    public void addCoChangeListener(IMsoCoChangeListenerIf aListener)
    {
        m_coUpdateListeners.add(aListener);
    }

    public void removeCoUpdateListener(IMsoCoChangeListenerIf aListener)
    {
        m_coUpdateListeners.remove(aListener);
    }

    public void notifyCoChange(IChangeRecordIf aChange)
    {
        if (m_coUpdateListeners.size() == 0)
        {
            return;
        }
        MsoEvent.CoChange event = new MsoEvent.CoChange(aChange);
        for (IMsoCoChangeListenerIf listener : m_coUpdateListeners)
        {
            try
            {
                listener.handleMsoCoChangeEvent(event);
            }
            catch (Exception e)
            {
            	String msg = "Exception in notifyCoUpdate, listener: " + listener.toString();
            	m_logger.error(msg,e); // local log
                Log.printStackTrace(msg,e); // distributed log (message queue server)
            }
        }
    }

    @SuppressWarnings("unchecked")
	private void defineInterests(
    		Map theListeners,
    		EventListener aListener, EnumSet<MsoClassCode> interests) {
    	// cleanup
    	removeInterests(theListeners, aListener);
    	// register interests
    	for(MsoClassCode it : interests) {
    		List list = (List)theListeners.get(it);
    		if(list==null) {
    			list = new ArrayList();
    			theListeners.put(it,list);
    		}
    		list.add(aListener);
    	}
    }

    @SuppressWarnings("unchecked")
    private void removeInterests(
    		Map theListeners,
    		EventListener aListener) {

    	Collection<List> lists = (Collection<List>)theListeners.values();
        for(List it : lists) {
            if(it.contains(aListener)) {
            	it.remove(aListener);
            }
        }
    }

    public void enterResume() {
    	m_resumeCount++;
    }

    public void leaveResume() {
    	// decrement?
    	if(m_resumeCount>0) m_resumeCount--;
    	// fire client update notifications?
    	if(m_resumeCount==0 && m_buffer.size()>0) {
    		// notify listeners
    		fireSlaveChange(m_slaveChangeListeners, new MsoEvent.ChangeList(m_buffer,false));
    		// clear buffer
    		m_buffer.clear();
    	}

    }

}
