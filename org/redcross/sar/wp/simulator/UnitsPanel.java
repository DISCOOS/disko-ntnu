package org.redcross.sar.wp.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.Utils;


public class UnitsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private UnitTableModel m_model;
	private Map<IUnitIf,Component> m_struts;
	private Map<IUnitIf,UnitStatusPanel> m_units;

	public UnitsPanel(boolean archived) {

		// forward
		super();

		// prepare
		m_model = new UnitTableModel(Utils.getApp().getMsoModel(),archived);
		m_struts = new HashMap<IUnitIf,Component>();
		m_units = new HashMap<IUnitIf,UnitStatusPanel>();

		// add listeners
		m_model.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				int uBound = Math.min(e.getLastRow(),m_model.getRowCount());
				switch(e.getType()) {
				case TableModelEvent.INSERT:
					insert(e.getFirstRow(),uBound,true);
					break;
				case TableModelEvent.UPDATE:
					update(e.getFirstRow(),uBound);
					break;
				case TableModelEvent.DELETE:
					delete(e.getFirstRow(),uBound);
					break;
				}
			}

		});

		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if(cmd.equalsIgnoreCase("toggle")) {
					// get list
					JComponent list = (JComponent)getContainer();
					// initialize
					int w = 0;
					int h = 0;
					int count = list.getComponentCount();
					// loop over all and get height
					for(int i=0; i<count; i++) {
						// get component
						Component c = list.getComponent(i);
						// is unit status panel?
						if(c instanceof UnitStatusPanel) {
							// add height
							h += c.getHeight();
							// find maximum width
							w = Math.max(c.getWidth(), w);
						}
						else h += 5;

					}
					// update preferred body size
					list.setPreferredSize(new Dimension(w,h));
				}

			}

		});

		// initialize gui
		initialize();

		// forward
		load();

	}

	private void initialize() {
		// prepare base panel
		setHeaderVisible(false);
		setScrollBarPolicies(
				BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED,
				BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
		JComponent items = (JComponent)getContainer();
		items.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		items.setLayout(new BoxLayout(items,BoxLayout.Y_AXIS));
	}

	private void insert(int firstRow, int lastRow, boolean arrange) {
		for(int i = firstRow;i<=lastRow;i++) {
			IUnitIf unit = m_model.getUnit(i);
			Component strut = Box.createVerticalStrut((i>0) ? 5 : 0);
			UnitStatusPanel panel = new UnitStatusPanel(unit);
			panel.addWorkFlowListener(this);
			panel.addActionListener(this);
			m_struts.put(unit, strut);
			m_units.put(unit, panel);
		}
		if(arrange) arrange();
	}

	private void update(int firstRow, int lastRow) {
		for(int i = firstRow;i<=lastRow;i++) {
			IUnitIf unit = m_model.getUnit(i);
			UnitStatusPanel panel = m_units.get(unit);
			if(panel!=null) panel.update();
		}
	}

	private void delete(int firstRow, int lastRow) {
		for(int i = firstRow;i<=lastRow;i++) {
			IUnitIf unit = m_model.getUnit(i);
			m_struts.remove(unit);
			m_units.remove(unit);
		}
		arrange();
	}

	private void arrange() {
		// get items panel
		JComponent items = (JComponent)getContainer();
		// get total items count
		int count = items.getComponentCount();
		// loop over all and remove listeners
		for(int i=0;i<count;i++) {
			if(items.getComponent(i) instanceof UnitStatusPanel) {
				UnitStatusPanel panel = (UnitStatusPanel)items.getComponent(i);
				panel.removeWorkFlowListener(this);
				panel.removeActionListener(this);
			}
		}
		items.removeAll();
		// get unit count
		count = m_model.getRowCount();
		// loop over all units
		for(int i=0;i<count;i++) {
			IUnitIf unit = m_model.getUnit(i);
			// get strut
			Component strut = m_struts.get(unit);
			// is deleted?
			if(strut==null) continue;
			// add to component
			items.add(m_struts.get(unit));
			UnitStatusPanel panel = m_units.get(unit);
			panel.addWorkFlowListener(this);
			panel.addActionListener(this);
			items.add(panel);
		}
		items.add(Box.createVerticalGlue());
	}

	public void load() {
		m_struts.clear();
		m_units.clear();
		insert(0,m_model.getRowCount()-1,false);
		arrange();
	}

}
