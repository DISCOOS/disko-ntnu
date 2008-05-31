/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Array;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class ComboAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public ComboAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,width,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	public ComboAttribute(String name, String caption, int width, String value, boolean isEditable) {
		// forward
		super(name,caption,width,value,isEditable);
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Component getComponent() {
		if(m_component==null) {
			JComboBox field = new JComboBox();
			field.setEditable(m_isEditable);
			field.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(isWorking()) return;
					fireOnWorkChange();
					
				}
			});
			// save the component
			m_component = field;			
		}
		return m_component;
	}
	
	public JComboBox getComboBox() {
		return (JComboBox)m_component;
	}
			
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	public boolean fill(Object values) {
		try {
			if(values instanceof Object[])
				((JComboBox)m_component).setModel(new DefaultComboBoxModel((Object[])values));
			else
				((JComboBox)m_component).setModel((ComboBoxModel)values);
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Object getValue() {
		return ((JComboBox)m_component).getSelectedItem();
	}
	
	public boolean setValue(Object value) {
		// update
		((JComboBox)m_component).setSelectedItem(value);
		// success
		return true;
	}
	
	public boolean setMsoAttribute(IAttributeIf attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoString) {
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
	
	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);
		getComboBox().setEditable(isEditable);		
	}
		
}
