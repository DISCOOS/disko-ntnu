/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.util.Calendar;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;

import org.redcross.sar.gui.format.DTGFormat;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.util.mso.DTG;

/**
 * @author kennetgu
 *
 */
public class DTGAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public DTGAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,width,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	public DTGAttribute(String name, String caption, int width, Object value, boolean isEditable) {
		// forward
		super(name,caption,width,value,isEditable);
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Component getComponent() {
		if(m_component==null) {
			// create
			JFormattedTextField field = new JFormattedTextField();
			// set format
			field.setFormatterFactory(new DTGFormatterFactory());
			field.setEditable(m_isEditable);
			field.getDocument().addDocumentListener(new DocumentListener() {

				public void changedUpdate(DocumentEvent e) { change(); }

				public void insertUpdate(DocumentEvent e) { change(); }

				public void removeUpdate(DocumentEvent e) { change(); }
				
				private void change() {
					if(isWorking()) return;
					fireOnWorkChange();
				}
				
			});
			// save the component
			m_component = field;
			
		}
		return m_component;
	}
			
	public JFormattedTextField getTextField() {
		return (JFormattedTextField)m_component;
	}
	
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	public Object getValue() {
		return ((JFormattedTextField)m_component).getText();
	}
	
	public boolean setValue(Object value) {
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
	
	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);
		getTextField().setEditable(isEditable);		
	}
	
	/*==================================================================
	 * Inner classes
	 *================================================================== 
	 */
	
	class DTGFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {

		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			DTGFormat mf1 = null;
			try {
				mf1 = new DTGFormat();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return mf1;
		}
		
	}
	
}
