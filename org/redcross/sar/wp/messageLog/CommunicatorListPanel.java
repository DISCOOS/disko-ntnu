package org.redcross.sar.wp.messageLog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IUnitIf.UnitType;

/**
 * Dialog containing a list of all units in command post communicator list
 *
 * @author thomasl
 */
public class CommunicatorListPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	public final static int COLUMN_WIDTH =
		DiskoButtonFactory.getButtonSize(ButtonSize.LONG).width;

	public final static int CELL_HEIGHT =
		DiskoButtonFactory.getButtonSize(ButtonSize.LONG).height;

	private final Map<JToggleButton, ICommunicatorIf>
		m_buttonMap = new HashMap<JToggleButton, ICommunicatorIf>();;

	private final Map<ICommunicatorIf, JToggleButton>
		m_communicatorMap = new HashMap<ICommunicatorIf, JToggleButton>();

	private final EventListenerList m_listeners = new EventListenerList();

	private final Map<ICommunicatorIf,Boolean> m_selectedMap = new HashMap<ICommunicatorIf,Boolean>();

	private final CommunicatorListModel m_model = new CommunicatorListModel();

	private int m_rows;
	private int m_cols;
	private IDiskoWpMessageLog m_wp;
	private boolean m_isSingleSelection;


	/* ==========================================================
	 * Constructors
	 * ==========================================================*/

	/**
	 * @param wp Message log work process
	 */
	public CommunicatorListPanel(IDiskoWpMessageLog wp, boolean isSingleSelection)
	{
		this(wp,isSingleSelection,5,3);
	}

	public CommunicatorListPanel(IDiskoWpMessageLog wp, boolean isSingleSelection, int rows, int cols)
	{
		// forward
		super();

		// prepare
		m_wp = wp;
		m_rows = rows;
		m_cols = cols;
		m_isSingleSelection = isSingleSelection;

		// add model as MSO update listener
		m_wp.getMsoEventManager().addClientUpdateListener(m_model);

		// add list data listener
		m_model.addListDataListener(new ListDataListener() {

			@Override
			public void contentsChanged(ListDataEvent e) {
				build();
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				build();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				build();
			}

		});

		// forward
		initialize();

	}

	/* ==========================================================
	 * Public methods
	 * ==========================================================*/

	public int getRows() {
		return m_rows;
	}

	public void setRows(int rows) {
		m_rows = rows;
		build();
	}

	public void setCols(int cols) {
		m_cols = cols;
		build();
	}

	public int getCols() {
		return m_cols;
	}

	public void setMatrix(int rows, int cols) {
		m_rows=rows;
		m_cols=cols;
		build();
	}

	public boolean isSingleSelectionMode() {
		return m_isSingleSelection;
	}

	public void setSingleSelectionMode(boolean isSingleSelectionMode) {
		m_isSingleSelection = isSingleSelectionMode;
		m_selectedMap.clear();
		renderSelection();
	}

	public void load()
	{
		m_model.load();
	}

	public void load(UnitType type)
	{
		if(type==null)
			m_model.load();
		else
			m_model.load(type);
	}

	public void load(String regex)
	{
		if(regex==null || regex.isEmpty())
			m_model.load();
		else
			m_model.load(regex);
	}

	public void load(List<ICommunicatorIf> list)
	{
		if(list==null)
			m_model.load();
		else
			m_model.load(list);
	}

	public void load(Selector<ICommunicatorIf> selector)
	{
		if(selector==null)
			m_model.load();
		else
			m_model.load(selector);

	}

	public ICommunicatorIf find(char prefix, int number) {
		return m_model.find(prefix,number);
	}

	public List<ICommunicatorIf> findAll(char prefix, int number) {

		return m_model.findAll(prefix,number);

	}

	public ICommunicatorIf find(String regex) {

		return m_model.find(regex);

	}

	public List<ICommunicatorIf> findAll(String regex) {

		return m_model.findAll(regex);

	}

	public ICommunicatorIf find(UnitType type) {

		return m_model.find(type);

	}

	public List<ICommunicatorIf> findAll(UnitType type) {

		return m_model.findAll(type);

	}

	public boolean isMarked(ICommunicatorIf c) {
		JToggleButton b = getButton(c);
		return (b!=null ? b.getBorder() instanceof DiskoBorder : false);
	}

	public void setMarked(ICommunicatorIf c, boolean isMarked, Color color) {
		JToggleButton b = getButton(c);
		if(b!=null) b.setBorder(isMarked?UIFactory.createBorder(2, 2, 2, 2, color):null);
	}

	public void clearMarked() {
		for(JToggleButton it : getButtons()) {
			it.setBorder(null);
		}
	}

	public CommunicatorListModel getModel()
	{
		return m_model;
	}

	public ICommunicatorIf getCommunicator(JToggleButton b)
	{
		return m_buttonMap.get(b);
	}

	public List<ICommunicatorIf> getCommunicators()
	{
		return new ArrayList<ICommunicatorIf>(m_communicatorMap.keySet());
	}

	public JToggleButton getButton(ICommunicatorIf c) {
		return m_communicatorMap.get(c);
	}

	public List<JToggleButton> getButtons()
	{
		return new ArrayList<JToggleButton>(m_communicatorMap.values());
	}

	public List<ICommunicatorIf> getSelection()  {
		List<ICommunicatorIf> selected = new ArrayList<ICommunicatorIf>();
		for(ICommunicatorIf it : m_selectedMap.keySet()) {
			// update
			Boolean value = m_selectedMap.get(it);
			if(value!=null && value) selected.add(it);
		}
		return selected;
	}

	public void setSelected(ICommunicatorIf c, boolean isSelected) {
		// is valid?
		if(c!=null) {
			// clear current?
			if(m_isSingleSelection) m_selectedMap.clear();
			// update
			Boolean oldValue = m_selectedMap.put(c,isSelected);
			// get change flag
			boolean isChanged = (isSelected != (oldValue!=null ? oldValue : isSelected));
			// forward?
			if(isChanged) fireActionEvent(c);
			// forward
			renderSelection();
		}
	}

	public void setSelected(List<ICommunicatorIf> list, boolean isSelected) {
		// is valid?
		if(list.size()>0) {
			// only select first?
			if(m_isSingleSelection) {
				setSelected(list.get(0),isSelected);
			}
			else {
				// select all in list
				for(ICommunicatorIf it : list) {
					// update
					Boolean oldValue = m_selectedMap.put(it,isSelected);
					// get change flag
					boolean isChanged = (isSelected != (oldValue!=null ? oldValue : isSelected));
					// forward?
					if(isChanged) fireActionEvent(it);
				}
				// forward
				renderSelection();
			}
		}
	}

	/**
	 * Clear all select communicators
	 */
	public void clearSelection()
	{
		m_selectedMap.clear();
		renderSelection();
	}

	public void addActionListener(ActionListener listener) {
		m_listeners.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		m_listeners.remove(ActionListener.class, listener);
	}

	/* ==========================================================
	 * Helper methods
	 * ==========================================================*/

	private void initialize()
	{
		// prepare
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setPreferredSize(new Dimension((COLUMN_WIDTH+5)*m_cols+5, CELL_HEIGHT*m_rows+10
				+Integer.valueOf(UIManager.get("ScrollBar.height").toString())));
	}

	private void build()
	{

		// Clear previous list, brute force maintenance
		removeAll();
		m_buttonMap.clear();
		m_communicatorMap.clear();

		// initialize items
		int i = 0;
		int j = 0;
		JPanel column = null;

		// loop over all active communicators
		for(ICommunicatorIf it : m_model.getElements())
		{
			// create a new panel for each column of given rows
			if(i%m_rows == 0)
			{
				// create column panel
				column = new JPanel();
				column.setAlignmentX(Component.LEFT_ALIGNMENT);
				column.setAlignmentY(Component.TOP_ALIGNMENT);
				column.setPreferredSize(new Dimension(COLUMN_WIDTH, CELL_HEIGHT*m_rows));
				column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
				// add to list
				add(column);
				// increment column index
				j++;
				// add horizontal gap?
				if(j<m_cols) add(Box.createHorizontalStrut(5));
			}
			// add button to current column
			addButton(it,column);
			// increment index
			i++;
		}
		// update size
		setPreferredSize(new Dimension((COLUMN_WIDTH+5)*m_cols + 5,
				CELL_HEIGHT*m_rows + 10 + Integer.valueOf(UIManager.get("ScrollBar.height").toString())));
		// apply selected communicators
		renderSelection();
	}

	private void renderSelection()
	{
		// initialize
		int count = 0;

		// loop over all communicators
		for(ICommunicatorIf it : m_communicatorMap.keySet())
		{

			// get button
			JToggleButton button = m_communicatorMap.get(it);

			// update
			Boolean value = m_selectedMap.get(it);

			// get selection flag
			boolean isSelected = value!=null ? value : false;

			// selection found?
			if(isSelected) count++;

			// selected?
			button.setSelected(isSelected);

		}

		// update
		revalidate();
		repaint();
	}

	private void addButton(final ICommunicatorIf c, JPanel column)
	{

		// create button
		JToggleButton button = DiskoButtonFactory.createToggleButton(c,ButtonSize.LONG);

		// set action command
		button.setActionCommand(c.getCommunicatorShortName());

		// add to column
		column.add(button);

		// create mapping
		m_buttonMap.put(button, c);
		m_communicatorMap.put(c, button);

		// add action listener
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// get source button
				JToggleButton b = (JToggleButton)e.getSource();

				// set selection state
				setSelected(c,b.isSelected());

				// notify
				fireActionEvent(e);

			}
		});

	}

	private void fireActionEvent(ICommunicatorIf c) {
		JToggleButton b = m_communicatorMap.get(c);
		if(b!=null) {
			fireActionEvent(new ActionEvent(b,ActionEvent.ACTION_PERFORMED,b.getActionCommand()));
		}
	}

	private void fireActionEvent(ActionEvent e) {
		ActionListener[] list = m_listeners.getListeners(ActionListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].actionPerformed(e);
		}
	}

	/* ==========================================================
	 * Anonymous classes
	 * ==========================================================*/

}
