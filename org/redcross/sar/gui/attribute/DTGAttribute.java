/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.util.Calendar;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.MaskFormatter;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.util.mso.DTG;

/**
 * @author kennetgu
 *
 */
public class DTGAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public DTGAttribute(IAttributeIf attribute, String caption, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	public DTGAttribute(String name, String caption, Object value, boolean isEditable) {
		// forward
		super(name,caption,value,isEditable);
	}
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected Component getComponent() {
		if(m_component==null) {
			// create
			m_component = new JFormattedTextField();
			// set format
			((JFormattedTextField)m_component).setFormatterFactory(new DTGFormat());
			((JFormattedTextField)m_component).setEditable(m_isEditable);
		}
		return m_component;
	}
			
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Object getValue() {
		return ((JFormattedTextField)m_component).getText();
	}
	
	public boolean setValue(Object value) {
		// allowed?
		if(!m_isEditable) return false;
		// validate data type
		if(value instanceof Calendar)
			((JFormattedTextField)m_component).setText(DTG.CalToDTG((Calendar)value));
		else 
			((JFormattedTextField)m_component).setText(String.valueOf(value));
		// success
		return true;
	}
	
	public boolean setMsoAttribute(IAttributeIf attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoCalendar) {
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
	 * Inner classes
	 *================================================================== 
	 */
	
	class DTGFormat extends JFormattedTextField.AbstractFormatterFactory {

		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			MaskFormatter mf1 = null;
			try {
				mf1 = new MaskFormatter("######");
				mf1.setPlaceholder("00000");
				mf1.setPlaceholderCharacter('0');
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return mf1;
		}
		
	}
	
}
