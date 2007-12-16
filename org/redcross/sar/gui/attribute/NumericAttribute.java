/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JTextField;

import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class NumericAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public NumericAttribute(IAttributeIf attribute, String caption, boolean isEditable) {
		// forward
		this(attribute,caption,0,false,isEditable);		
	}
	
	public NumericAttribute(IAttributeIf attribute, String caption, 
			int decimalPrecision, boolean allowNegative, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// apply number document
		((JTextField)m_component).setDocument(
				new NumericDocument(decimalPrecision,allowNegative));		
		// get value from attribute
		load();		
	}
	
	public NumericAttribute(String name, String caption, Object value, boolean isEditable) {
		// forward
		this(name,caption,value,0,false,isEditable);
	}
	
	public NumericAttribute(String name, String caption, Object value, 
			int decimalPrecision, boolean allowNegative, boolean isEditable) {
		// forward
		super(name,caption,null,isEditable);
		// apply number document
		((JTextField)m_component).setDocument(
				new NumericDocument(decimalPrecision,allowNegative));		
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
