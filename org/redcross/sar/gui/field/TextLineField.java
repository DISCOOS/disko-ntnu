/**
 * 
 */
package org.redcross.sar.gui.field;

import java.awt.Component;

import javax.swing.JFormattedTextField;
import javax.swing.text.Document;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class TextLineField extends AbstractField {
	
	private static final long serialVersionUID = 1L;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */	
	
	public TextLineField(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
	}
		
	public TextLineField(String name, String caption, 
			boolean isEditable, int width, int height) {
		super(name, caption, isEditable, width, height, "");
	}
	
	public TextLineField(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		super(name, caption, isEditable, width, height, value);
	}
	
	public TextLineField(IAttributeIf<?> attribute, String caption,
			boolean isEditable, int width, int height) {
		super(attribute, caption, isEditable, width, height);
	}

	public TextLineField(IAttributeIf<?> attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
	}

	/* ==================================================================
	 *  Public methods
	 * ================================================================== */

	public Component getComponent() {
		if(m_component==null) {
			JFormattedTextField field = new JFormattedTextField() {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void setDocument(Document doc) {
					// remove from old
					if(super.getDocument()!=null) {
						super.getDocument().removeUndoableEditListener(m_undoListener);
					}
					// forward
					super.setDocument(doc);
					// add listener
					doc.addUndoableEditListener(m_undoListener);
				}
				
			};
			field.setEditable(m_isEditable);
			// save the component
			m_component = field;			
			// set listeners
			field.getDocument().addUndoableEditListener(m_undoListener);
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
		((JFormattedTextField)m_component).setText(value!=null?String.valueOf(value):null);
		// success
		return true;
	}
	
	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);
		getTextField().setEditable(isEditable);		
	}
	
	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	protected boolean isMsoAttributeSettable(IAttributeIf<?> attr) {
		return (attr instanceof AttributeImpl.MsoString);
	}
	
}
