/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JTextField;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class TextAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public TextAttribute(IAttributeIf attribute, String caption, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	public TextAttribute(String name, String caption, String value, boolean isEditable) {
		// forward
		super(name,caption,value,isEditable);
	}
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected Component getComponent() {
		if(m_component==null) {
			m_component = new JTextField();
			((JTextField)m_component).setEditable(m_isEditable);
		}
		return m_component;
	}
			
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Object getValue() {
		return ((JTextField)m_component).getText();
	}
	
	public boolean setValue(Object value) {
		// update
		((JTextField)m_component).setText(String.valueOf(value));
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
	
}
