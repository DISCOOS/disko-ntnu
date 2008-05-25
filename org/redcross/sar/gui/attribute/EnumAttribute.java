/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.util.EnumSet;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class EnumAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;

	public EnumAttribute(AttributeImpl.MsoEnum attribute, String caption, int width, boolean isEditable) {
		// forward
		this(attribute,caption,width,getAllEnumValues(attribute),isEditable);						
	}
	
	public EnumAttribute(IAttributeIf attribute, String caption, int width, Enum[] values, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,width,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// fill values
		setValues(values);	
		// get value from attribute
		load();		
	}
	
	public EnumAttribute(String name, String caption, int width, Enum value, boolean isEditable) {
		this(name,caption,width,value,null,isEditable);
	}
	
	public EnumAttribute(String name, String caption, int width, Enum value, Enum[] values, boolean isEditable) {
		// forward
		super(name,caption,width,null,isEditable);
		// fill values
		setValues(values);	
	}
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected Component getComponent() {
		if(m_component==null) {
			JList list = new JList();
			list.setEnabled(m_isEditable);
			list.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					if(isWorking()) return;
					fireOnWorkChange();					
				}
				
			});
			
			m_component = list;
		}
		return m_component;
	}
			
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	public Object getValue() {
		return ((JList)m_component).getSelectedValue();
	}
	
	public boolean setValue(Object value) {
		// allowed?
		if(!m_isEditable) return false;
		// get list
		JList list = ((JList)m_component);
		// select new item?
		if(list.getSelectedValue()!=value) {
			list.setSelectedValue(value,true);
		}
		// success
		return true;
	}
	
	public Enum[] getValues() {
		// get list
		JList list = ((JList)m_component);
		// get current model
		ListModel model = list.getModel();
		Enum[] values = new Enum[model.getSize()];
		for (int i = 0; i < model.getSize(); i++) {
			values[i]=(Enum)model.getElementAt(i);
		}
		return values;
	}
	
	public void setValues(Enum[] values) {
		// get new model
		DefaultListModel model = new DefaultListModel();
		// get list
		JList list = ((JList)m_component);
		// get current selected value
		Enum current = (Enum)list.getSelectedValue();
		// fill new values?
		if(values!=null) {
			for (int i = 0; i < values.length; i++) {
				model.addElement(values[i]);
			}
		}
		// update list model
		list.setModel(model);
		// reselect
		if(current!=null)
			list.setSelectedValue(current,true);
		else
			// has new values?
			if(model.getSize()>0)
				list.setSelectedIndex(0);
	}
	
	public void setVisibleRowCount(int rows) {
		((JList)m_component).setVisibleRowCount(rows);
	}
	
	public int getVisibleRowCount() {
		return ((JList)m_component).getVisibleRowCount();
	}
	
	public boolean setMsoAttribute(IAttributeIf attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoEnum) {
				// save attribute
				m_attribute = attribute;
				// update name
				setName(m_attribute.getName());
				// success
				return true;
			}
		}
		// failure
		return false;
	}	
	
	/*==================================================================
	 * Private static methods
	 *================================================================== 
	 */
	
	private static Enum[] getAllEnumValues(AttributeImpl attribute) {
		Class c = attribute.getClass();
		EnumSet set = EnumSet.allOf(c);
		return (Enum[])set.toArray();
	}
}
