package org.redcross.sar.wp.messageLog;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.redcross.sar.Application;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.util.MsoUtils;

public class CommunicatorListModel implements ListModel, IMsoUpdateListenerIf {

	private final EventListenerList m_listeners = new EventListenerList();

	private final List<ICommunicatorIf> m_items = new ArrayList<ICommunicatorIf>();

	private final EnumSet<IMsoManagerIf.MsoClassCode> m_interests =
		EnumSet.of(MsoClassCode.CLASSCODE_CMDPOST,MsoClassCode.CLASSCODE_UNIT);


	private Selector<ICommunicatorIf> m_selector;

	/* ==============================================================
	 * Constructors
	 * ============================================================== */

	public CommunicatorListModel() {
		this(createActiveSelector(),false);
	}

	public CommunicatorListModel(boolean load) {
		this(createActiveSelector(),load);
	}

	public CommunicatorListModel(Selector<ICommunicatorIf> selector, boolean load) {
		m_selector = selector;
		if(load) load();
	}

	/* ==============================================================
	 * ListModel implementation
	 * ============================================================== */

	public Selector<ICommunicatorIf> getSelector() {
		return m_selector;
	}

	public void setSelector(Selector<ICommunicatorIf> selector) {
		m_selector = selector;
	}

	public boolean addElement(ICommunicatorIf c) {
		if(m_selector.select(c))
		{
			m_items.add(c);
			int index = m_items.size()-1;
			fireIntervalAdded(index,index);
			return true;
		}
		return false;
	}

	public ICommunicatorIf getElementAt(int index) {
		return m_items.get(index);
	}

	public boolean removeElement(ICommunicatorIf c) {
		int index = m_items.indexOf(c);
		if(index!=-1)
		{
			m_items.remove(c);
			fireIntervalRemoved(index,index);
			return true;
		}
		return false;
	}

	public List<ICommunicatorIf> getElements() {
		return new ArrayList<ICommunicatorIf>(m_items);
	}

	public int getSize() {
		return m_items.size();
	}

	public void clear() {
		if(m_items.size()>0)
		{
			int index1 = 0;
			int index2 = m_items.size();
			m_items.clear();
			fireIntervalRemoved(index1, index2);
		}
	}

	public void addAll(List<ICommunicatorIf> list) {

		// get communicators from global list
		m_items.addAll(list);

		// notify?
		if(m_items.size()>0)
		{
			int index1 = 0;
			int index2 = m_items.size();
			fireIntervalAdded(index1, index2);
		}

	}

	public void load()
	{
		// set selector
		load(m_selector);

	}

	public void load(String regexp)
	{
		load(createRegExSelector(regexp));
	}

	public void load(UnitType type)
	{
		load(createTypeSelector(type));
	}

	public void load(List<ICommunicatorIf> list)
	{
		// forward
		clear();
		// copy
		addAll(list);
	}

	public void load(Selector<ICommunicatorIf> selector)
	{

		// prepare
		clear();

		// get model
		IMsoModelIf model = Application.getInstance().getMsoModel();

		// allowed?
		if(model.getMsoManager().operationExists()) {

			// get communicators from global list
			addAll(model.getMsoManager().getCmdPost()
					.getCommunicatorList().selectItems(
							selector, ICommunicatorIf.COMMUNICATOR_COMPARATOR));

		}
	}

	public ICommunicatorIf find(char prefix, int number) {
		return find((String.valueOf(prefix) + " " + number).trim());
	}

	public List<ICommunicatorIf> findAll(char prefix, int number) {
		return findAll((String.valueOf(prefix) + " " + number).trim());
	}

	public ICommunicatorIf find(String regex) {

		// get selector
		Selector<ICommunicatorIf> selector = createRegExSelector(regex);

		// search for it
		for(ICommunicatorIf it : m_items) {
			if(selector.select(it))
				return it;
		}

		return null;
	}

	public List<ICommunicatorIf> findAll(String regex) {

		// initialize
		List<ICommunicatorIf> list = new ArrayList<ICommunicatorIf>(getSize());

		// get selector
		Selector<ICommunicatorIf> selector = createRegExSelector(regex);

		// search for it
		for(ICommunicatorIf it : m_items) {
			if(selector.select(it))
				list.add(it);
		}
		// finished
		return list;
	}

	public ICommunicatorIf find(UnitType type) {

		// get selector
		Selector<ICommunicatorIf> selector = createTypeSelector(type);

		// search for it
		for(ICommunicatorIf it : m_items) {
			if(selector.select(it))
				return it;
		}

		return null;
	}

	public List<ICommunicatorIf> findAll(UnitType type) {

		// initialize
		List<ICommunicatorIf> list = new ArrayList<ICommunicatorIf>(getSize());

		// get selector
		Selector<ICommunicatorIf> selector = createTypeSelector(type);

		// search for it
		for(ICommunicatorIf it : m_items) {
			if(selector.select(it))
				list.add(it);
		}
		// finished
		return list;
	}

	public boolean contains(ICommunicatorIf c) {
		return m_items.contains(c);
	}

	public void addListDataListener(ListDataListener listener) {
		m_listeners.add(ListDataListener.class,listener);
	}

	public void removeListDataListener(ListDataListener listener) {
		m_listeners.remove(ListDataListener.class, listener);
	}

	/* ==========================================================
	 * IMsoUpdateListenerIf implementation
	 * ==========================================================*/

	public EnumSet<MsoClassCode> getInterests() {
		return m_interests;
	}

	/**
	 * Updates unit list based on MSO communicator events
	 *
	 * @see org.redcross.sar.mso.event.IMsoUpdateListenerIf#handleMsoChangeEvent(org.redcross.sar.mso.event.MsoEvent.Change)
	 */
	public void handleMsoChangeEvent(MsoEvent.ChangeList events) {

        // clear all?
        if(events.isClearAllEvent())
        {
        	clear();
        }
        else {

    		// loop over all events
    		for(MsoEvent.Change e : events.getEvents(m_interests)) {

    			// consume loopback updates
    			if(!e.isLoopbackMode()) {

		            // get ICommunicatorIf object
		    		ICommunicatorIf c = (ICommunicatorIf)e.getSource();

			        // add object?
					if (e.isCreateObjectEvent()) {
						addElement(c);
					}

					// is object modified?
					if (e.isModifyObjectEvent()) {
						int index = m_items.indexOf(c);
						if(index!=-1) fireContentsChanged(index,index);
					}

					// delete object?
					if (e.isDeleteObjectEvent()) {
						removeElement(c);
					}
	    		}
    		}
        }

	}

	/* ==============================================================
	 * Helper methods
	 * ============================================================== */

	protected void fireContentsChanged(int index1, int index2) {
		ListDataEvent e = new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,index1,index2);
		ListDataListener[] list = m_listeners.getListeners(ListDataListener.class);
		for(int i=0;i<list.length;i++)
			list[i].contentsChanged(e);
	}

	protected void fireIntervalAdded(int index1, int index2) {
		ListDataEvent e = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,index1,index2);
		ListDataListener[] list = m_listeners.getListeners(ListDataListener.class);
		for(int i=0;i<list.length;i++)
			list[i].intervalAdded(e);
	}

	protected void fireIntervalRemoved(int index1, int index2) {
		ListDataEvent e = new ListDataEvent(this,ListDataEvent.INTERVAL_REMOVED,index1,index2);
		ListDataListener[] list = m_listeners.getListeners(ListDataListener.class);
		for(int i=0;i<list.length;i++)
			list[i].intervalRemoved(e);
	}

	protected static Selector<ICommunicatorIf> createAllSelector()
	{
		return new Selector<ICommunicatorIf>()
		{
			public boolean select(ICommunicatorIf c)
			{
				return true;
			}
		};

	}

	protected static Selector<ICommunicatorIf> createActiveSelector()
	{
		return new Selector<ICommunicatorIf>()
		{
			public boolean select(ICommunicatorIf c)
			{
				if(c instanceof ICmdPostIf)
				{
					// cast to ICmdPostIf
					ICmdPostIf cmdPost = (ICmdPostIf)c;
					// select?
					return ICmdPostIf.ACTIVE_CMDPOST_SET.contains(cmdPost.getStatus());

				}
				else if(c instanceof IUnitIf)
				{
					// cast to IUnitIf
					IUnitIf unit = (IUnitIf)c;
					// select?
					return IUnitIf.ACTIVE_SET.contains(unit.getStatus());
				}
				else
				{
					return true;
				}
			}
		};
	}

	protected static Selector<ICommunicatorIf> createRegExSelector(final String regex)
	{
		return new Selector<ICommunicatorIf>()
		{
			public boolean select(ICommunicatorIf c)
			{
				if(	isMatch(c.getCommunicatorShortName(),regex) ||
					isMatch(MsoUtils.getCommunicatorName(c, false),regex) ||
					isMatch(c.getCallSign(),regex) ||
					isMatch(c.getToneID(),regex)) {

					if(c instanceof ICmdPostIf)
					{
						// cast to ICmdPostIf
						ICmdPostIf cmdPost = (ICmdPostIf)c;
						// select?
						return ICmdPostIf.ACTIVE_CMDPOST_SET.contains(cmdPost.getStatus());

					}
					else if(c instanceof IUnitIf)
					{
						// cast to IUnitIf
						IUnitIf unit = (IUnitIf)c;
						// select?
						return IUnitIf.ACTIVE_SET.contains(unit.getStatus());
					}
					else
					{
						return true;
					}
				}
				return false;
			}

			private boolean isMatch(String text, String regex) {
				regex = regex!=null && !regex.isEmpty() ? regex.toLowerCase() : "";
				text = text!=null && !text.isEmpty() ? text.toLowerCase() : "";
				return !text.isEmpty() && text.matches(regex);
			}

		};


	}

	protected static Selector<ICommunicatorIf> createTypeSelector(final UnitType type)
	{
		return new Selector<ICommunicatorIf>()
		{
			public boolean select(ICommunicatorIf c)
			{
				if(c instanceof ICmdPostIf)
				{
					// cast to ICmdPostIf
					ICmdPostIf cmdPost = (ICmdPostIf)c;
					// select?
					return ICmdPostIf.ACTIVE_CMDPOST_SET.contains(cmdPost.getStatus()) &&
							(type == UnitType.CP || type == null);

				}
				else if(c instanceof IUnitIf)
				{
					// cast to IUnitIf
					IUnitIf unit = (IUnitIf)c;
					// select?
					return IUnitIf.ACTIVE_SET.contains(unit.getStatus()) &&
							(type == unit.getType() || type == null);
				}
				else
				{
					return true;
				}
			}
		};
	}
}
