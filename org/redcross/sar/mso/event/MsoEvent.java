package org.redcross.sar.mso.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.committer.ICommitWrapperIf;
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
public class MsoEvent extends java.util.EventObject
{
	private static final long serialVersionUID = 1L;

	/**
     * Event type with maskValue values.
     */
    public enum MsoEventType
    {
        EMPTY_EVENT(0),
        ADDED_REFERENCE_EVENT(1),
        REMOVED_REFERENCE_EVENT(2),
        MODIFIED_REFERENCE_EVENT(4),
        CREATED_OBJECT_EVENT(8),
        DELETED_OBJECT_EVENT(16),
        MODIFIED_DATA_EVENT(32),
        COMMIT_EVENT(64),
        CLEAR_ALL_EVENT(128);

        private final int m_maskValue;

        /**
         * Constructor of Enum members
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
    private int m_eventTypeMask;

    /**
     * Create event for a specific {@link org.redcross.sar.mso.event.MsoEvent.MsoEventType}
     *
     * @param aSource         The source object
     * @param anEventTypeMask See {@link org.redcross.sar.mso.event.MsoEvent.MsoEventType}
     */
    public MsoEvent(Object aSource, int anEventTypeMask)
    {
        super(aSource);
        m_eventTypeMask = anEventTypeMask;
    }

    /**
     * Cast source to
     */

    /**
     * Get EventImpl type maskValue
     *
     * @return The sum of {@link org.redcross.sar.mso.event.MsoEvent.MsoEventType} values for this event.
     */
    public int getEventTypeMask()
    {
        return m_eventTypeMask;
    }

    public boolean isDeleteObjectEvent()
    {
    	return (m_eventTypeMask & MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
    }

    public boolean isCreateObjectEvent()
    {
    	return (m_eventTypeMask & MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
    }

    public boolean isModifyObjectEvent()
    {
    	return (m_eventTypeMask & MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
    }

    public boolean isChangeReferenceEvent()
    {
    	return (m_eventTypeMask & MsoEventType.MODIFIED_REFERENCE_EVENT.maskValue())!=0
		|| (m_eventTypeMask & MsoEventType.REMOVED_REFERENCE_EVENT.maskValue())!=0
		||  (m_eventTypeMask & MsoEventType.ADDED_REFERENCE_EVENT.maskValue())!=0;
    }

    public boolean isModifiedReferenceEvent()
    {
    	return (m_eventTypeMask & MsoEventType.MODIFIED_REFERENCE_EVENT.maskValue())!=0;
    }

    public boolean isRemovedReferenceEvent()
    {
    	return (m_eventTypeMask & MsoEventType.REMOVED_REFERENCE_EVENT.maskValue())!=0;
    }

    public boolean isAddedReferenceEvent()
    {
    	return (m_eventTypeMask & MsoEventType.ADDED_REFERENCE_EVENT.maskValue())!=0;
    }

    public boolean isClearAllEvent()
    {
    	return (m_eventTypeMask & MsoEventType.CLEAR_ALL_EVENT.maskValue()) != 0;
    }

    public boolean union(MsoEvent e) {
    	// initialize dirty flag
    	boolean isDirty = false;
    	// is union possible?
    	if(e==null || !e.getSource().equals(getSource())) return false;
    	// get union mask
		int mask = 0;
		if(e.isDeleteObjectEvent() || isDeleteObjectEvent())
			mask += MsoEventType.DELETED_OBJECT_EVENT.maskValue();
		if(e.isModifyObjectEvent() || isModifyObjectEvent())
			mask += MsoEventType.MODIFIED_DATA_EVENT.maskValue();
		if(e.isAddedReferenceEvent() || isAddedReferenceEvent())
			mask += MsoEventType.ADDED_REFERENCE_EVENT.maskValue();
		if(e.isModifiedReferenceEvent() || isModifiedReferenceEvent())
			mask += MsoEventType.MODIFIED_REFERENCE_EVENT.maskValue();
		if(e.isRemovedReferenceEvent() || isRemovedReferenceEvent())
			mask += MsoEventType.REMOVED_REFERENCE_EVENT.maskValue();
		// mask changed?
		if(m_eventTypeMask!=mask) {
			m_eventTypeMask = mask;
			isDirty = true;
		}
		// is both update events?
		if(this instanceof Update && e instanceof Update) {

			// cast to MsoEvent.Update
			Update u1 = (Update)this;
			Update u2 = (Update)e;

	        /* =========================================
	         * Get union of dominant update modes. If a
	         * local update has occurred, this should
	         * override any existing update mode.
	         * ========================================= */
	        UpdateMode aMode = (u1.m_updateMode==null
	        		|| !UpdateMode.LOCAL_UPDATE_MODE.equals(u1.m_updateMode) ?
	        				u2.m_updateMode : u1.m_updateMode);

	        /* =========================================
	         * Get union of update loopback flags. Is loopback as long
	         * as all updates are loopbacks. Else, flag
	         * is reset.
	         * ========================================= */
	        boolean isLoopback = u2.m_isLoopback && u2.m_isLoopback;

	        // changed?
	        if(u1.m_updateMode != aMode) {
	        	u1.m_updateMode = aMode;
	        	isDirty = true;
	        }
	        if(u1.m_isLoopback != isLoopback) {
	        	u1.m_isLoopback = isLoopback;
	        	isDirty = true;
	        }

		}
		// finished
		return isDirty;

    }

    /**
     * Event that triggers an update of the user interface and/or the server handler.
     */
    public static class Update extends MsoEvent
    {
		private static final long serialVersionUID = 1L;

	    /**
	     * Type of update mode
	     */
	    private UpdateMode m_updateMode;

	    /**
	     * Looback flag
	     */
	    private boolean m_isLoopback;

		public Update(IMsoObjectIf aSource, UpdateMode mode, boolean isLooback, int anEventTypeMask)
        {
            super(aSource, anEventTypeMask);
            m_updateMode = mode;
            m_isLoopback = isLooback;

        }

		public IMsoObjectIf getSource() {
			return (IMsoObjectIf)super.getSource();
		}

	    public UpdateMode getUpdateMode() {
	    	return m_updateMode;
	    }

	    public boolean isLoopback() {
	    	return m_isLoopback;
	    }

    }

    /**
     * Event that triggers a server commit.
     */
    public static class Commit extends MsoEvent
    {
		private static final long serialVersionUID = 1L;

        public Commit(ICommitWrapperIf aSource, int anEventTypeMask)
        {
            super(aSource, anEventTypeMask);
        }
    }

    /**
     * Event that triggers derived updates of the data structures.
     */
    public static class DerivedUpdate extends MsoEvent
    {
		private static final long serialVersionUID = 1L;

		public DerivedUpdate(IMsoObjectIf aSource, int anEventTypeMask)
        {
            super(aSource, anEventTypeMask);
        }
    }

    public static class UpdateList
    {
		private static final long serialVersionUID = 1L;

		private final boolean m_isClearAll;

		private final List<MsoEvent.Update> m_events =
			new ArrayList<MsoEvent.Update>();

		private final Map<MsoClassCode, List<MsoEvent.Update>> m_map =
			new HashMap<MsoClassCode, List<MsoEvent.Update>>();

		public UpdateList(Collection<MsoEvent.Update> items, boolean isClearAll) {
			// initialize
			List<MsoEvent.Update> list;
			// get all classes
			for(MsoEvent.Update it : items) {
				MsoClassCode classCode = it.getSource().getMsoClassCode();
				list = m_map.get(classCode);
				if(list==null) {
					list = new ArrayList<Update>();
					m_map.put(classCode, list);
				}
				list.add(it);
			}
			// prepare
			m_events.addAll(items);
			m_isClearAll = isClearAll;
		}

		public boolean isClearAllEvent() {
			return m_isClearAll;
		}

		public void contains(MsoClassCode classCode) {
			m_map.containsKey(classCode);
		}

		public List<MsoEvent.Update> getEvents() {
			return m_events;
		}

		public List<MsoEvent.Update> getEvents(MsoClassCode classCode) {
			List<MsoEvent.Update> list = new ArrayList<Update>();
			list.addAll(m_map.get(classCode));
			return list;
		}

		public List<MsoEvent.Update> getEvents(EnumSet<MsoClassCode> classCodes) {
			List<MsoEvent.Update> list = new ArrayList<Update>();
			for(MsoClassCode it : classCodes) {
				List<MsoEvent.Update> found =  m_map.get(it);
				if(found!=null) list.addAll(found);
			}
			return list;
		}

    }

}