/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class TextAreaAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	private JTextArea m_textArea;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */	
	
	public TextAreaAttribute(IAttributeIf<?> attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
	}

	public TextAreaAttribute(IAttributeIf<?> attribute, String caption,
			boolean isEditable) {
		// forward
		super(attribute, caption, isEditable);
	}

	public TextAreaAttribute(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		// forward
		super(name, caption, isEditable, width, height, value);
	}

	public TextAreaAttribute(String name, String caption, boolean isEditable) {
		// forward
		super(name, caption, isEditable);
	}

	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Component getComponent() {
		if(m_component==null) {
			JScrollPane scrollPane = new JScrollPane(getTextArea());
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// save the component
			m_component = scrollPane;
		}
		return m_component;
	}
			
	public JTextArea getTextArea() {
		if(m_textArea==null) {
			m_textArea = new JTextArea();
			m_textArea.setEditable(m_isEditable);
			m_textArea.getDocument().addDocumentListener(new DocumentListener() {

				public void changedUpdate(DocumentEvent e) { change(); }

				public void insertUpdate(DocumentEvent e) { change(); }

				public void removeUpdate(DocumentEvent e) { change(); }
				
				private void change() {
					if(isConsume()) return;
					fireOnWorkChange();
				}
				
			});
			m_textArea.setRows(10);
			m_textArea.setLineWrap(true);
			m_textArea.setWrapStyleWord(true);			
		}
		return m_textArea;
	}
	
	public JScrollPane getScrollPane() {
		return (JScrollPane)m_component;
	}
	
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	public String getValue() {
		return getTextArea().getText();
	}
	
	public boolean setValue(Object value) {
		// update
		getTextArea().setText(String.valueOf(value));
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
		getTextArea().setEditable(isEditable);		
	}
	
}
