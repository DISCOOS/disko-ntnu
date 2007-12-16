/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;


/**
 * @author kennetgu
 *
 */
public class CheckBoxAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public CheckBoxAttribute(AttributeImpl.MsoBoolean attribute, String caption, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	public CheckBoxAttribute(String name, String caption, boolean value, boolean isEditable) {
		// forward
		super(name,caption,value,isEditable);
	}
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected Component getComponent() {
		if(m_component==null) {
			m_component = new JCheckBox();
			m_component.setEnabled(m_isEditable);
		}
		return m_component;
	}

	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Object getValue() {
		return ((JCheckBox)m_component).isSelected();
	}
	
	public boolean setValue(Object value) {
		// allowed?
		if(!m_isEditable) return false;
		if(value instanceof String)
			((JCheckBox)m_component).setSelected(Boolean.valueOf((String)value));
		else if(value instanceof Boolean) 
			((JCheckBox)m_component).setSelected(Boolean.valueOf((Boolean)value));
		else {
			// failure
			return false;
		}
		// success
		return true;
	}
	
	public boolean setMsoAttribute(IAttributeIf attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoBoolean) {
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
		
}
