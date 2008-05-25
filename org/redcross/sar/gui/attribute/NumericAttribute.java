/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class NumericAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public NumericAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable) {
		// forward
		this(attribute,caption,width,-1,0,false,isEditable);		
	}
	
	public NumericAttribute(IAttributeIf attribute, String caption, int width,  
			int maxDigits, int decimalPrecision, boolean allowNegative, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,width,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// apply number document
		((JTextField)m_component).setDocument(
				new NumericDocument(maxDigits,decimalPrecision,allowNegative));		
		// get value from attribute
		load();		
	}
	
	public NumericAttribute(String name, String caption, int width, Object value, boolean isEditable) {
		// forward
		this(name,caption,width,value,-1,0,false,isEditable);
	}
	
	public NumericAttribute(String name, String caption, int width, Object value, 
			int maxDigits, int decimalPrecision, boolean allowNegative, boolean isEditable) {
		// forward
		super(name,caption,width,null,isEditable);
		// apply number document
		((JTextField)m_component).setDocument(
				new NumericDocument(maxDigits,decimalPrecision,allowNegative));
		
	}
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected Component getComponent() {
		if(m_component==null) {
			// create
			JFormattedTextField field = new JFormattedTextField();
			// set format
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
	
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	public Object getValue() {
		String value = ((JTextField)m_component).getText();
		return value!=null && value.length()>0? Integer.valueOf(value) : 0;
	}
	
	public boolean setValue(Object value) {
		// allowed?
		if(!m_isEditable) return false;
		// is null?
		if(value==null) value = 0;
		// update
		((JTextField)m_component).setText(String.valueOf(value));
		// success
		return true;
	}
	
	public boolean setMsoAttribute(IAttributeIf attribute) {
		// reset?
		if(attribute==null) {
			// reset
			m_attribute = null;			
		}
		else 
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoInteger ||
					attribute instanceof AttributeImpl.MsoDouble) {
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
	
	public void setMaxDigits(int digits) {
		// set precision
		((NumericDocument)((JTextField)m_component).getDocument()).setMaxDigits(digits); 
	}
 
	public int getMaxDigits() {
		// get precision
		return ((NumericDocument)((JTextField)m_component).getDocument()).getMaxDigits(); 
	}
	
	public void setDecimalPrecision(int precision) {
		// set precision
		((NumericDocument)((JTextField)m_component).getDocument()).setDecimalPrecision(precision); 
	}
 
	public int getDecimalPrecision() {
		// get precision
		return ((NumericDocument)((JTextField)m_component).getDocument()).getDecimalPrecision(); 
	}
   
	public void setAllowNegative(boolean allow) {
		// set flag
		((NumericDocument)((JTextField)m_component).getDocument()).setAllowNegative(allow); 
	}
 
	public boolean getAllowNegative() {
		// get flag
		return ((NumericDocument)((JTextField)m_component).getDocument()).getAllowNegative(); 
	}

	
}
