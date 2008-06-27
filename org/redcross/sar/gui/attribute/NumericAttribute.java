/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class NumericAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	private Class<? extends Number> m_numericClass = Integer.class;
	
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
		getTextField().setDocument(new NumericDocument(maxDigits,decimalPrecision,allowNegative));
		// forward
		registerChangeListener();
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
		getTextField().setDocument(new NumericDocument(maxDigits,decimalPrecision,allowNegative));
		// forward
		registerChangeListener();		
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Component getComponent() {
		if(m_component==null) {
			// create
			JTextField field = new JTextField();
			// set format
			field.setEditable(m_isEditable);
			// save the component
			m_component = field;
		}
		return m_component;
	}
			
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
		// as numeric class?
		if(m_numericClass!=null) {
			// get string
			String value = getTextField().getText();
			// validate
			if(Utils.isNumeric(value, m_numericClass)) {
				// get number in correct class
				return Utils.parseNumeric(value, m_numericClass);
			}
		}
		return null;
	}
	
	public boolean setValue(Object value) {
		// is null?
		if(value==null) value = 0;
		// get number class
		Class<? extends Number> c = Utils.getNumericClass(value);
		// is a number?
		if(c!=null) {
			// save class
			m_numericClass = c;
			// update
			getTextField().setText(String.valueOf(value));
			// success
			return true;
		}
		// failure
		return false;
	}
	
	public boolean setMsoAttribute(IAttributeIf attribute) {
		// reset?
		if(attribute==null) {
			// reset
			m_attribute = null;
		}
		else { 
			// is supported?
			if(isMsoAttributeSupported(attribute)) {
				// match component type and attribute
				if(attribute instanceof AttributeImpl.MsoInteger ||
						attribute instanceof AttributeImpl.MsoDouble) {
					// save attribute
					m_attribute = attribute;
					// update name
					setName(m_attribute.getName());
					// forward
					return load();
				}
			}
		}
		// failure
		return false;
	}	
	
	public void setMaxDigits(int digits) {
		// set precision
		((NumericDocument)getTextField().getDocument()).setMaxDigits(digits); 
	}
 
	public int getMaxDigits() {
		// get precision
		return ((NumericDocument)getTextField().getDocument()).getMaxDigits(); 
	}
	
	public void setDecimalPrecision(int precision) {
		// set precision
		((NumericDocument)getTextField().getDocument()).setDecimalPrecision(precision); 
	}
 
	public int getDecimalPrecision() {
		// get precision
		return ((NumericDocument)getTextField().getDocument()).getDecimalPrecision(); 
	}
   
	public void setAllowNegative(boolean allow) {
		// set flag
		((NumericDocument)getTextField().getDocument()).setAllowNegative(allow); 
	}
 
	public boolean getAllowNegative() {
		// get flag
		return ((NumericDocument)getTextField().getDocument()).getAllowNegative(); 
	}

	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);
		getTextField().setEditable(isEditable);		
	}
	
	/*==================================================================
	 * Private methods
	 *================================================================== 
	 */
	
	private void registerChangeListener() {
		getTextField().getDocument().addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) { change(); }

			public void insertUpdate(DocumentEvent e) { change(); }

			public void removeUpdate(DocumentEvent e) { change(); }
			
			private void change() {
				if(isConsume()) return;
				fireOnWorkChange();
			}
			
		});		
	}
	
}
