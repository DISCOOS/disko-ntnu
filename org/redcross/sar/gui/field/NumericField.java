/**
 * 
 */
package org.redcross.sar.gui.field;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
public class NumericField extends AbstractField {
	
	private static final long serialVersionUID = 1L;
	
	private static final int DEFAULT_DECIMAL_PRECISION = 0;
	private static final int DEFAULT_MAX_DIGITS = 0;
	private static final boolean ALLOW_NEGATIVE = false;
	
	private Class<? extends Number> m_numericClass = Integer.class;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */
	
	public NumericField(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
		// forward
		initialize(DEFAULT_MAX_DIGITS,DEFAULT_DECIMAL_PRECISION,ALLOW_NEGATIVE);
	}
	
	public NumericField(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		// forward
		super(name, caption, isEditable, width, height, value);
		// forward
		initialize(DEFAULT_MAX_DIGITS,DEFAULT_DECIMAL_PRECISION,ALLOW_NEGATIVE);
	}

	public NumericField(String name, String caption, boolean isEditable ,
			int width, int height, Object value,  
			int maxDigits, int decimalPrecision, boolean allowNegative) {
		// forward
		super(name, caption, isEditable,width, height, value);
		// forward
		initialize(maxDigits,decimalPrecision,allowNegative);		
	}
		
	
	public NumericField(IAttributeIf<?> attribute, String caption,
			boolean isEditable) {
		// forward
		super(attribute, caption, isEditable);
		// forward
		initialize(DEFAULT_MAX_DIGITS,DEFAULT_DECIMAL_PRECISION,ALLOW_NEGATIVE);
	}

	public NumericField(IAttributeIf<?> attribute, String caption, boolean isEditable,
			int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
		// forward
		initialize(DEFAULT_MAX_DIGITS,DEFAULT_DECIMAL_PRECISION,ALLOW_NEGATIVE);
	}
	
	public NumericField(IAttributeIf<?> attribute, String caption, boolean isEditable,
			int width, int height,   
			int maxDigits, int decimalPrecision, boolean allowNegative) {
		// forward
		super(attribute, caption, isEditable, width, height);
		// forward
		initialize(maxDigits,decimalPrecision,allowNegative);		
	}
	
	private void initialize(int maxDigits, int decimalPrecision, boolean allowNegative) {
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
	
	public boolean setMsoAttribute(IAttributeIf<?> attribute) {
		// reset?
		if(attribute==null) {
			// reset
			m_attribute = null;
		}
		else { 
			// is supported?
			if(isMsoAttributeSupported(attribute)) {
				// match component type and attribute
				if( 	attribute instanceof AttributeImpl.MsoInteger ||
						attribute instanceof AttributeImpl.MsoDouble		) {
					// save attribute
					m_attribute = attribute;
					// update name
					setName(m_attribute.getName());
					// forward
					reset();
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
				if(!isChangeable()) return;
				fireOnWorkChange();
			}
			
		});		
	}
	
}
