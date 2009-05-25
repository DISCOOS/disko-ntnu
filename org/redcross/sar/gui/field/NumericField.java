/**
 * 
 */
package org.redcross.sar.gui.field;
 
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoDouble;
import org.redcross.sar.mso.data.AttributeImpl.MsoInteger;
import org.redcross.sar.mso.data.AttributeImpl.MsoString;
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
@SuppressWarnings("unchecked")
public class NumericField extends AbstractField<Number,JFormattedTextField,JTextField> {
	
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
		
	
	public NumericField(IMsoAttributeIf attribute, String caption,
			boolean isEditable) {
		// forward
		super(attribute, caption, isEditable);
		// forward
		initialize(DEFAULT_MAX_DIGITS,DEFAULT_DECIMAL_PRECISION,ALLOW_NEGATIVE);
	}

	public NumericField(IMsoAttributeIf attribute, String caption, boolean isEditable,
			int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
		// forward
		initialize(DEFAULT_MAX_DIGITS,DEFAULT_DECIMAL_PRECISION,ALLOW_NEGATIVE);
	}
	
	public NumericField(IMsoAttributeIf attribute, String caption, boolean isEditable,
			int width, int height,   
			int maxDigits, int decimalPrecision, boolean allowNegative) {
		// forward
		super(attribute, caption, isEditable, width, height);
		// forward
		initialize(maxDigits,decimalPrecision,allowNegative);		
	}
	
	private void initialize(int maxDigits, int decimalPrecision, boolean allowNegative) {
		// apply number document
		getEditComponent().setDocument(new NumericDocument(maxDigits,decimalPrecision,allowNegative));
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public JFormattedTextField getEditComponent() {
		if(m_editComponent==null) {
			// create
			m_editComponent = new JFormattedTextField() {
				
				private static final long serialVersionUID = 1L;
											
				@Override
				public void setDocument(Document doc) {
					// illegal operation?
					if(super.getDocument() instanceof NumericDocument)  
						throw new IllegalArgumentException("Document can not be replaced");
					// replace default (only allowed once after NumericDocument is set)
					super.setDocument(doc);
					if(doc!=null) doc.addDocumentListener(m_documentListener);
				}
				
			};
		}
		return (JFormattedTextField)m_editComponent;
	}
			
	public JTextField getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createDefaultComponent(false);
		}
		return m_viewComponent;
	}
	
	public Number getEditValue() {
		// get string
		String value = getEditComponent().getText();
		// get numeric class?
		if(m_numericClass==null) {
			m_numericClass = Utils.getNumericClass(value);
		}
		// found numeric class?
		if(m_numericClass!=null) {
			// validate
			if(Utils.isNumeric(value, m_numericClass)) {
				// get number in correct class
				return Utils.parseNumeric(value, m_numericClass);
			}
		}
		return null;
	}
	
	@Override
	protected boolean setNewEditValue(Object value) {
		// get number class
		m_numericClass = Utils.getNumericClass(value);
		// get text
		String text = (value!=null?String.valueOf(value):"");
		// update
		getEditComponent().setText(text);
		getViewComponent().setText(getFormattedText());
		// success
		return true;
	}
	
	@Override
	public String getFormattedText() {
		return getEditComponent().getText();
	}
	
	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	protected boolean isMsoAttributeSettable(IMsoAttributeIf<?> attr) {
		return (attr instanceof MsoInteger ||
				attr instanceof MsoDouble || 
				attr instanceof MsoString);
	}
		
	public void setMaxDigits(int digits) {
		// set precision
		((NumericDocument)getEditComponent().getDocument()).setMaxDigits(digits); 
	}
 
	public int getMaxDigits() {
		// get precision
		return ((NumericDocument)getEditComponent().getDocument()).getMaxDigits(); 
	}
	
	public void setDecimalPrecision(int precision) {
		// set precision
		((NumericDocument)getEditComponent().getDocument()).setDecimalPrecision(precision); 
	}
 
	public int getDecimalPrecision() {
		// get precision
		return ((NumericDocument)getEditComponent().getDocument()).getDecimalPrecision(); 
	}
   
	public void setAllowNegative(boolean allow) {
		// set flag
		((NumericDocument)getEditComponent().getDocument()).setAllowNegative(allow); 
	}
 
	public boolean getAllowNegative() {
		// get flag
		return ((NumericDocument)getEditComponent().getDocument()).getAllowNegative(); 
	}

}
