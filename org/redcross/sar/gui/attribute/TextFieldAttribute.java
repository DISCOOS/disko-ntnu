/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JFormattedTextField;
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
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */	
	
	public TextFieldAttribute(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
	}
		
	public TextFieldAttribute(String name, String caption, 
			boolean isEditable, int width, int height) {
		super(name, caption, isEditable, width, height, "");
	}
	
	public TextFieldAttribute(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		super(name, caption, isEditable, width, height, value);
	}
	
	public TextFieldAttribute(IAttributeIf<?> attribute, String caption,
			boolean isEditable, int width, int height) {
		super(attribute, caption, isEditable, width, height);
	}

	public TextFieldAttribute(IAttributeIf<?> attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
	}

	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */

	public Component getComponent() {
		if(m_component==null) {
			JFormattedTextField field = new JFormattedTextField();
			field.setEditable(m_isEditable);
			field.getDocument().addDocumentListener(new DocumentListener() {

				public void changedUpdate(DocumentEvent e) { change(); }

				public void insertUpdate(DocumentEvent e) { change(); }

				public void removeUpdate(DocumentEvent e) { change(); }
				
				private void change() {
					if(isConsume()) return;
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
	
	public String getValue() {
		return ((JFormattedTextField)m_component).getText();
	}
	
	public boolean setValue(Object value) {
		// update
		((JFormattedTextField)m_component).setText(String.valueOf(value));
		// success
		return true;
	}
	
	public boolean setMsoAttribute(IAttributeIf<?> attribute) {
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
		getTextField().setEditable(isEditable);		
	}
}
