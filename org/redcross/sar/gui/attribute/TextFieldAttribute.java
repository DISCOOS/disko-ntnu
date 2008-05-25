/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class TextFieldAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public TextFieldAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,width,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	public TextFieldAttribute(String name, String caption, int width, String value, boolean isEditable) {
		// forward
		super(name,caption,width,value,isEditable);
	}
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected Component getComponent() {
		if(m_component==null) {
			JTextField field = new JTextField();
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
			
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public JTextField getTextField() {
		return (JTextField)m_component;
	}
	
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
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
