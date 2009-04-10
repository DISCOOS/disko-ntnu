package org.redcross.sar.mso.event;

import no.cmr.tools.Log;

import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.committer.ICommitWrapperIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.util.except.CommitException;
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
    private final Collection<IMsoCommitListenerIf> m_commitListeners = new Vector<IMsoCommitListenerIf>();

	/**
	 * Collections of derived update listeners
	 */
    private final Collection<IMsoDerivedUpdateListenerIf> m_derivedUpdateListeners = new Vector<IMsoDerivedUpdateListenerIf>();


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
    private final Collection<MsoEvent.Update> m_buffer = new Vector<MsoEvent.Update>(100);

    /* ------------------------------------------------------------------
     * IMsoEventManagerIf implementation
     * ------------------------------------------------------------------ */

    /**
     * Add a listener in the {@link #m_clientUpdateListeners} queue.
     */
    public void addClientUpdateListener(IMsoUpdateListenerIf aListener)
    {
        defineInterests(m_clientUpdateListeners,aListener,aListener.getInterests());
    }

    /**
     * Remove a listener in the {@link #m_clientUpdateListeners} queue.
     */
    public void removeClientUpdateListener(IMsoUpdateListenerIf aListener)
    {
        removeInterests(m_clientUpdateListeners,aListener);
    }

    public void notifyClearAll(IMsoObjectIf root)
    {
        // notify
        fireUpdate(m_clientUpdateListeners,
        		getUpdateEvents(root,MsoModelImpl.getInstance().getUpdateMode(),
        				false, MsoEventType.CLEAR_ALL_EVENT.maskValue()));
    }

    public void notifyClientUpdate(IMsoObjectIf aSource,
    		UpdateMode mode, boolean isLoopback, int anEventTypeMask)
    {
    	// consume?
    	if(anEventTypeMask!=0)
    	{
    		// notify now?
    		if(m_resumeCount==0)
    		{
    			fireUpdate(m_clientUpdateListeners,
    					getUpdateEvents(aSource, mode, isLoopback, anEventTypeMask));
    		}
    		else
    		{
    			// forward
    			update(m_buffer,new MsoEvent.Update( aSource, mode, isLoopback, anEventTypeMask ));
    		}

    	}
    }

    private void update(Collection<MsoEvent.Update> buffer, MsoEvent.Update e) {
    	// get source
    	IMsoObjectIf msoObj = e.getSource();
    	// search for existing event
		for(MsoEvent.Update it : buffer) {
			if(it.getSource().equals(msoObj)) {
				it.union(e); return;
			}
		}
		// add as new
		buffer.add(e);
    }

    public void addServerUpdateListener(IMsoUpdateListenerIf aListener)
    {
        defineInterests(m_serverUpdateListeners,aListener,aListener.getInterests());
    }

    public void removeServerUpdateListener(IMsoUpdateListenerIf aListener)
    {
        removeInterests(m_serverUpdateListeners,aListener);
    }

    public void notifyServerUpdate(IMsoObjectIf aSource, UpdateMode mode, int anEventTypeMask)
    {
    	// consume?
    	if(anEventTypeMask!=0)
    	{
	        fireUpdate(m_serverUpdateListeners, getUpdateEvents(aSource, mode, false, anEventTypeMask));
    	}
    }

    /**
     * Add a listener in the {@link #m_commitListeners} queue.
     */
    public void addCommitListener(IMsoCommitListenerIf aListener)
    {
        m_commitListeners.add(aListener);
    }

    /**
     * Remove a listener in the {@link #m_commitListeners} queue.
     */
    public void removeCommitListener(IMsoCommitListenerIf aListener)
    {
        m_commitListeners.remove(aListener);
    }

    public void notifyCommit(ICommitWrapperIf aSource)  throws CommitException
    {
        fireCommit(m_commitListeners, aSource, MsoEvent.MsoEventType.COMMIT_EVENT.maskValue());
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



    private void fireUpdate(
    		Map<MsoClassCode, List<IMsoUpdateListenerIf>> theListeners,
    		MsoEvent.UpdateList events)
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
        	for(MsoEvent.Update it : events.getEvents()) {
        		List<IMsoUpdateListenerIf> interested = theListeners.get(it.getSource().getMsoClassCode());
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
                listener.handleMsoUpdateEvent(events);

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                Log.printStackTrace("Exception in fireUpdate, listener: " + listener.toString(),ex);
            }

            // update progress monitor?
            if(WORK_TIME<System.currentTimeMillis()-tic) {
                Thread.yield();
                getProgressMonitor().refreshProgress();
                tic = System.currentTimeMillis();
            }

        }
    }

    private void fireCommit(Collection<IMsoCommitListenerIf> theListeners, ICommitWrapperIf aSource, int anEventTypeMask) throws CommitException
    {
        if (theListeners.size() == 0 || anEventTypeMask == 0)
        {
            return;
        }
        MsoEvent.Commit event = new MsoEvent.Commit(aSource, anEventTypeMask);
        for (IMsoCommitListenerIf listener : theListeners)
        {
            try
            {
                listener.handleMsoCommitEvent(event);
            }
            catch (Exception e)
            {
                Log.printStackTrace("Exception in fireCommit, listener: " + listener.toString(),e);
                if (e instanceof  CommitException)
                {
                    throw (CommitException)e;
                }
            }
        }
    }

    private MsoEvent.UpdateList getUpdateEvents(IMsoObjectIf msoObj,
    		UpdateMode mode, boolean isLoopback, int mask)
    {
     	MsoEvent.Update e = new MsoEvent.Update( msoObj, mode, isLoopback, mask );
        return new MsoEvent.UpdateList(getUpdateEvents(e),e.isClearAllEvent());
    }

    private List<MsoEvent.Update> getUpdateEvents(MsoEvent.Update e) {
    	List<MsoEvent.Update> list = new ArrayList<MsoEvent.Update>(1);
    	list.add(e);
    	return list;
    }

    public void addDerivedUpdateListener(IMsoDerivedUpdateListenerIf aListener)
    {
        m_derivedUpdateListeners.add(aListener);
    }

    public void removeDerivedUpdateListener(IMsoDerivedUpdateListenerIf aListener)
    {
        m_derivedUpdateListeners.remove(aListener);
    }

    public void notifyDerivedUpdate(IMsoObjectIf aSource,int anEventTypeMask)
    {
        if (m_derivedUpdateListeners.size() == 0)
        {
            return;
        }
        MsoEvent.DerivedUpdate event = new MsoEvent.DerivedUpdate(aSource, anEventTypeMask);
        for (IMsoDerivedUpdateListenerIf listener : m_derivedUpdateListeners)
        {
            try
            {
                listener.handleMsoDerivedUpdateEvent(event);
            }
            catch (Exception e)
            {
                Log.printStackTrace("Exception in notifyDerivedUpdate, listener: " + listener.toString(),e);
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
    		fireUpdate(m_clientUpdateListeners, new MsoEvent.UpdateList(m_buffer,false));
    		// clear buffer
    		m_buffer.clear();
    	}

    }

}
