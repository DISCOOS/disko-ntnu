package org.redcross.sar.mso.event;

import no.cmr.tools.Log;

import org.apache.log4j.Logger;
import org.redcross.sar.Application;
import org.redcross.sar.mso.ChangeRecordImpl;
import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.ITransactionIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.work.ProgressMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
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
    private final Map<MsoClassCode, List<IMsoUpdateListenerIf>>
    	m_clientUpdateListeners = new HashMap<MsoClassCode,List<IMsoUpdateListenerIf>>();

    /**
	 * Map from MsoClassCode to server update listeners that is interested in each MsoClassCodes
	 */
    private final Map<MsoClassCode, List<IMsoUpdateListenerIf>>
    	m_serverUpdateListeners = new HashMap<MsoClassCode,List<IMsoUpdateListenerIf>>();

	/**
	 * Collections of commit listeners
	 */
    private final Collection<IMsoTransactionListenerIf> m_commitListeners = new Vector<IMsoTransactionListenerIf>();

	/**
	 * Collections of Co update listeners
	 */
    private final Collection<IMsoCoUpdateListenerIf> m_coUpdateListeners = new Vector<IMsoCoUpdateListenerIf>();


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

    /* ------------------------------------------------------------------
     * IMsoEventManagerIf implementation
     * ------------------------------------------------------------------ */

    /**
     * Add a listener in the {@link #m_clientUpdateListeners} queue.
     */
    public void addLocalUpdateListener(IMsoUpdateListenerIf aListener)
    {
        defineInterests(m_clientUpdateListeners,aListener,aListener.getInterests());
    }

    /**
     * Remove a listener in the {@link #m_clientUpdateListeners} queue.
     */
    public void removeLocalUpdateListener(IMsoUpdateListenerIf aListener)
    {
        removeInterests(m_clientUpdateListeners,aListener);
    }

    public void notifyClearAll(IMsoObjectIf root)
    {
    	// get update mode
    	UpdateMode mode = Application.getInstance().getMsoModel().getUpdateMode();
    	// create change object
    	IChangeRecordIf aChange = new ChangeRecordImpl(root,mode,
    			MsoEventType.CLEAR_ALL_EVENT.maskValue());
        // notify
        fireChange(m_clientUpdateListeners, getUpdateEvents(aChange));
    }

    public void notifyLocalUpdate(IChangeRecordIf aChange)
    {
    	// consume?
    	if(aChange.getMask()!=0)
    	{
    		// notify now?
    		if(m_resumeCount==0)
    		{
    			fireChange(m_clientUpdateListeners,
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
			if(it.getSource().equals(msoObj)) {
				it.union(e); return;
			}
		}
		// add as new
		buffer.add(e);
    }

    public void addRemoteUpdateListener(IMsoUpdateListenerIf aListener)
    {
        defineInterests(m_serverUpdateListeners,aListener,aListener.getInterests());
    }

    public void removeRemoteUpdateListener(IMsoUpdateListenerIf aListener)
    {
        removeInterests(m_serverUpdateListeners,aListener);
    }

    public void notifyRemoteUpdate(IChangeRecordIf aChange)
    {
    	// consume?
    	if(aChange.getMask()!=0)
    	{
	        fireChange(m_serverUpdateListeners, getUpdateEvents(aChange));
    	}
    }

    /**
     * Add a listener in the {@link #m_commitListeners} queue.
     */
    public void addCommitListener(IMsoTransactionListenerIf aListener)
    {
        m_commitListeners.add(aListener);
    }

    /**
     * Remove a listener in the {@link #m_commitListeners} queue.
     */
    public void removeCommitListener(IMsoTransactionListenerIf aListener)
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



    private void fireChange(
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
        list.toArray(items);

        // get number of listeners
        int count = items.length;

        // loop over all listeners
        for (int i=0; i< count; i++)
        {
            // get listener
            IMsoUpdateListenerIf listener = items[i];

            try
            {
                listener.handleMsoChangeEvent(events);

            }
            catch (Exception ex)
            {
            	String msg = "Exception in fireUpdate, listener: " + listener.toString();
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
        if (theListeners.size() == 0)
        {
            return;
        }
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

    public void addCoUpdateListener(IMsoCoUpdateListenerIf aListener)
    {
        m_coUpdateListeners.add(aListener);
    }

    public void removeCoUpdateListener(IMsoCoUpdateListenerIf aListener)
    {
        m_coUpdateListeners.remove(aListener);
    }

    public void notifyCoUpdate(IChangeRecordIf aChange)
    {
        if (m_coUpdateListeners.size() == 0)
        {
            return;
        }
        MsoEvent.CoChange event = new MsoEvent.CoChange(aChange);
        for (IMsoCoUpdateListenerIf listener : m_coUpdateListeners)
        {
            try
            {
                listener.handleMsoCoUpdateEvent(event);
            }
            catch (Exception e)
            {
            	String msg = "Exception in notifyCoUpdate, listener: " + listener.toString();
            	m_logger.error(msg,e); // local log
                Log.printStackTrace(msg,e); // distributed log (message queue server)
            }
        }
    }

    private void defineInterests(
    		Map<MsoClassCode, List<IMsoUpdateListenerIf>> theListeners,
    		IMsoUpdateListenerIf aListener, EnumSet<MsoClassCode> interests) {
    	// cleanup
    	removeInterests(theListeners, aListener);
    	// register interests
    	for(MsoClassCode it : interests) {
    		List<IMsoUpdateListenerIf> list = theListeners.get(it);
    		if(list==null) {
    			list = new ArrayList<IMsoUpdateListenerIf>();
    			theListeners.put(it,list);
    		}
    		list.add(aListener);
    	}
    }

    private void removeInterests(
    		Map<MsoClassCode, List<IMsoUpdateListenerIf>> theListeners,
    		IMsoUpdateListenerIf aListener) {

        for(List<IMsoUpdateListenerIf> it : theListeners.values()) {
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
    		fireChange(m_clientUpdateListeners, new MsoEvent.ChangeList(m_buffer,false));
    		// clear buffer
    		m_buffer.clear();
    	}

    }

}
