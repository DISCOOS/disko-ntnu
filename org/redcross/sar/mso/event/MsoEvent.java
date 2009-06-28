package org.redcross.sar.mso.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.ITransactionIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * Class for event objects in a observer/observable pattern for the MSO model.
 * <p/>
 * The event object is passed from updating (observable) to updated (observer) objects.
 * <p/>
 * The MSO model requires three sets of listeners: Client Update Listeners
 * handle updates sent to the client, either by the user or the server.
 * The Server Update Listener shall handle update that shall be sent to the server
 * at a (later) commit. Commit Listeners handle the Commit / Rollback events.
 */
public abstract class MsoEvent extends java.util.EventObject
{
	private static final long serialVersionUID = 1L;

	/**
     * Event type with maskValue values.
     */
    public enum MsoEventType
    {
        EMPTY_EVENT(0),
        ADDED_RELATION_EVENT(1),
        REMOVED_RELATION_EVENT(2),
        CREATED_OBJECT_EVENT(4),
        DELETED_OBJECT_EVENT(8),
        MODIFIED_DATA_EVENT(16),
        COMMIT_EVENT(32),
        CLEAR_ALL_EVENT(64);

        private final int m_maskValue;

        /**
         * Constructor of enum members
         *
         * @param value Value related to member.
         */
        MsoEventType(int value)
        {
            m_maskValue = value;
        }

        /**
         * Get mask value for this Enum member
         *
         * @return The mask value
         */
        public int maskValue()
        {
            return m_maskValue;
        }
    }

    /**
     * Event type mask
     */
    protected int m_eventTypeMask;

    /**
     * Create event for a specific {@link org.redcross.sar.mso.event.MsoEvent.MsoEventType}
     *
     * @param aSource         The source object
     * @param aMask See {@link org.redcross.sar.mso.event.MsoEvent.MsoEventType}
     */
    protected MsoEvent(Object aSource, int aMask)
    {
        super(aSource);
        m_eventTypeMask = aMask;
    }

    /**
     * Get EventImpl type maskValue
     *
     * @return The sum of {@link org.redcross.sar.mso.event.MsoEvent.MsoEventType} values for this event.
     */
    public int getMask()
    {
        return m_eventTypeMask;
    }

    public boolean isCreateObjectEvent()
    {
    	return isMask(m_eventTypeMask,MsoEventType.CREATED_OBJECT_EVENT);
    }

    public boolean isDeleteObjectEvent()
    {
    	return isMask(m_eventTypeMask,MsoEventType.DELETED_OBJECT_EVENT);
    }

    public boolean isModifyObjectEvent()
    {
    	return isMask(m_eventTypeMask,MsoEventType.MODIFIED_DATA_EVENT);
    }

    public boolean isAddedReferenceEvent()
    {
    	return isMask(m_eventTypeMask,MsoEventType.ADDED_RELATION_EVENT);
    }

    public boolean isRemovedReferenceEvent()
    {
    	return isMask(m_eventTypeMask,MsoEventType.REMOVED_RELATION_EVENT);
    }

    public boolean isModifiedReferenceEvent()
    {
    	return isMask(m_eventTypeMask,MsoEventType.ADDED_RELATION_EVENT)
    	    || isMask(m_eventTypeMask,MsoEventType.REMOVED_RELATION_EVENT);
    }

    public boolean isClearAllEvent()
    {
    	return isMask(m_eventTypeMask,MsoEventType.CLEAR_ALL_EVENT);
    }
    
    public static boolean isMask(int mask, MsoEventType type)
    {
    	return (mask & type.maskValue()) == type.maskValue();
    }

    /**
     * Create copy of list, sort if sorting is enabled, and return it.
     * @param <T> - the list data type
     * @param list - the list to create a copy of and sort
     * @return Returns a sorted copy of given list.
     */
    private static <T extends Comparable<? super T>> List<T> sort(List<T> list, boolean copy) 
    {
    	if(copy) list = clone(list);
    	Collections.sort(list);
    	return list;
    }

    /**
     * Clone given list
     * @param <T> - the list data type
     * @param list - the list to clone
     * @return Returns a copy of the given list
     */
    private static <T extends Comparable<? super T>> List<T> clone(Collection<T> list) 
    {
    	return new Vector<T>(list);
    }
    
    /**
     * Event that triggers an update of the user interface and/or the server handler.
     */
    public static class Change extends MsoEvent implements Comparable<Change>
    {
		private static final long serialVersionUID = 1L;

	    /**
	     * Reference to change record instance
	     */
	    private IChangeRecordIf m_rs;

	    /* ============================================
	     * Constructors
	     * ============================================ */
	    
		public Change(IChangeRecordIf aRs)
        {
            super(aRs.getMsoObject(), aRs.getMask());
            m_rs = aRs;
        }

		public IMsoObjectIf getSource() {
			return (IMsoObjectIf)super.getSource();
		}

		/**
		 * Get the update mode of the model when the change occurred
		 * 
		 * @return Returns the update mode of the model when the change occurred.
		 */
	    public UpdateMode getUpdateMode() {
	    	return m_rs.getUpdateMode();
	    }

	    /**
	     * Get update loopback mode </p> 
	     * 
	     * </p>If the update is a loopback, no data is changed, 
	     * only the <i>data state</i> is changed from LOCAL to REMOTE. 
	     * The method can therefore be used to decide if data 
	     * state rendering should be updated or not.</p>
	     *   
	     * <i>This flag is only true if all changes are loopbacks</i>.</p>
	     * 
	     * <b>NOTE</b>: <code>MsoEvent.Update::isLoopbackMode</code> and 
	     * <code>IMsoObjectIf::isLoopbackMode</code> may return different 
	     * values. The reason is that <code>IMsoObjectIf::isLoopbackMode</code> 
	     * checks if all it's data objects (attributes and references) are in 
	     * loopback mode. If one or more data objects are not in 
	     * loopback mode, the IMsoObjectIf object instance is not in loopback 
	     * mode. The same rationale applies to the calculation of 
	     * <code>IMsoObjectIf::isLoopbackMode</code>. However, since only
	     * changed data objects is included in this calculation, a difference
	     * may occur. </p>
	     * @return Returns <code>true</code> if all all changes are loopbacks. 
	     * @see See <code>IMsoUpdateStateIf</code> for more information. 
	     */
	    public boolean isLoopbackMode() {
	    	return m_rs.isLoopbackMode();
	    }
	    
	    /**
	     * Get update rollback mode </p> 
	     * 
	     * </p>If the update is a rollback, all data is a rollback 
	     * from LOCAL to REMOTE. The method can be used to 
	     * decide if data state rendering should be updated or not.</p>
	     *   
	     * <i>This flag is only true if all changes are rollbacks</i>.</p>
	     * 
	     * <b>NOTE</b>: <code>MsoEvent.Update::isRollbackMode</code> and 
	     * <code>IMsoObjectIf::isRollbackMode</code> may return different 
	     * values. The reason is that <code>IMsoObjectIf::isRollbackMode</code> 
	     * checks if all it's data objects (attributes and references) are in 
	     * rollback mode. If one or more data objects are not in 
	     * rollback mode, the IMsoObjectIf object instance is not in rollback 
	     * mode. The same rationale applies to the calculation of 
	     * <code>IMsoObjectIf::isRollbackMode</code>. However, since only
	     * changed data objects is included in this calculation, a difference
	     * may occur. </p>
	     * @return Returns <code>true</code> if all all changes are rollbacks. 
	     * @see See <code>IMsoUpdateStateIf</code> for more information. 	     */
	    public boolean isRollbackMode() {
	    	return m_rs.isLoopbackMode();
	    }
	    
	    /**
	     * Get the change object
	     * 
	     * @return Returns the change object
	     */
	    public IChangeRecordIf getChange()
	    {
	    	return m_rs;
	    }	    
	    
	    /**
	     * Make a union of this an given change event. 
	     * @param e - the change event to union with this
	     * @return Returns {@code true} if union succeeded.
	     */
	    public boolean union(MsoEvent.Change e)
	    {
	    	// is allowed?
	    	if(e!=null)
	    	{
		    	// make union
		    	if(m_rs.union(e.getChange(), false)) {
		    		// update event mask
		    		m_eventTypeMask |= m_rs.getMask();
		    		// union performed
		    		return true;
		    	}
	    	}
	    	// union not performed
	    	return false;
	    }

		@Override
		public int compareTo(Change e) {
			return (int)(m_rs.getSeqNo() - e.getChange().getSeqNo());
		}

    }

    /**
     * Event that triggers Co updates of the data structures.
     */
    public static class CoChange extends MsoEvent.Change
    {
		private static final long serialVersionUID = 1L;

		public CoChange(IChangeRecordIf aChange)
        {
            super(aChange);
        }
    }

    /**
     * Class that implements as list of change events
     * 
     * @author kenneth
     *
     */
    public static class ChangeList
    {
		private static final long serialVersionUID = 1L;

		private final boolean m_isClearAll;

		private List<MsoEvent.Change> m_events;

		private final Map<MsoClassCode, List<MsoEvent.Change>> m_map =
			new HashMap<MsoClassCode, List<MsoEvent.Change>>();

		public ChangeList(Collection<MsoEvent.Change> items, boolean isClearAll) {
			// initialize
			List<MsoEvent.Change> list;
			// sort items
			List<MsoEvent.Change> events = sort(new Vector<MsoEvent.Change>(items),false);
			// get all classes
			for(MsoEvent.Change it : events) {
				MsoClassCode classCode = it.getSource().getClassCode();
				list = m_map.get(classCode);
				if(list==null) {
					list = new ArrayList<Change>();
					m_map.put(classCode, list);
				}
				list.add(it);
			}
			// prepare
			m_events = events;
			m_isClearAll = isClearAll;
		}

		public boolean isClearAllEvent() {
			return m_isClearAll;
		}

		public void contains(MsoClassCode classCode) {
			m_map.containsKey(classCode);
		}

		public List<MsoEvent.Change> getEvents() {
			return m_events;
		}

		public List<MsoEvent.Change> getEvents(MsoClassCode classCode) {
			List<MsoEvent.Change> list = new ArrayList<Change>();
			list.addAll(m_map.get(classCode));
			return list;
		}

		public List<MsoEvent.Change> getEvents(EnumSet<MsoClassCode> classCodes) {
			List<MsoEvent.Change> list = new ArrayList<Change>();
			for(MsoClassCode it : classCodes) {
				List<MsoEvent.Change> found =  m_map.get(it);
				if(found!=null) list.addAll(found);
			}
			return list;
		}

    }

    /**
     * Event that triggers a server commit.
     */
    public static class Commit extends MsoEvent
    {
		private static final long serialVersionUID = 1L;

        public Commit(ITransactionIf aSource)
        {
            super(aSource, MsoEvent.MsoEventType.COMMIT_EVENT.maskValue());
        }
    }

}