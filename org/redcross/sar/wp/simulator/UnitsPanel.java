package org.redcross.sar.wp.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.mso.data.IUnitIf;

public class UnitsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;
	
	private UnitTableModel m_model = null;
	private Map<IUnitIf,Component> m_struts = null;
	private Map<IUnitIf,UnitStatusPanel> m_units = null;
			
	public UnitsPanel() {
		
		// forward
		super();
						
		// prepare
		m_model = new UnitTableModel(Utils.getApp().getMsoModel());
		m_struts = new HashMap<IUnitIf,Component>();
		m_units = new HashMap<IUnitIf,UnitStatusPanel>();
		
		// add listeners
		m_model.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				switch(e.getType()) {
				case TableModelEvent.INSERT:
					insert(e.getFirstRow(),e.getLastRow());
					break;
				case TableModelEvent.UPDATE:
					update(e.getFirstRow(),e.getLastRow());
					break;
				case TableModelEvent.DELETE:
					delete(e.getFirstRow(),e.getLastRow());
					break;
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
		setPreferredSize(new Dimension(300,125*4));
		setScrollBarPolicies(
				BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED, 
				BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
		JComponent items = (JComponent)getBodyComponent();
		items.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		items.setLayout(new BoxLayout(items,BoxLayout.Y_AXIS));
	}
	
	private void insert(int firstRow, int lastRow) {
		for(int i = firstRow;i<=lastRow;i++) {
			IUnitIf unit = m_model.getUnit(i);
			Component strut = Box.createVerticalStrut((i>0) ? 5 : 0); 
			UnitStatusPanel panel = new UnitStatusPanel(unit);
			panel.addDiskoWorkListener(this);
			m_struts.put(unit, strut);
			m_units.put(unit, panel);
		}
		arrange();
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
		JComponent items = (JComponent)getBodyComponent();
		// get total items count
		int count = items.getComponentCount();		
		// loop over all and remove listeners
		for(int i=0;i<count;i++) {
			if(items.getComponent(i) instanceof UnitStatusPanel) {
				UnitStatusPanel panel = (UnitStatusPanel)items.getComponent(i);
				panel.removeDiskoWorkListener(this);
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
			items.add(m_units.get(unit));			
		}
		items.add(Box.createVerticalGlue());		
	}
	
	public void load() {
		m_struts.clear();
		m_units.clear();
		insert(0,m_model.getRowCount()-1);
	}
	
}
