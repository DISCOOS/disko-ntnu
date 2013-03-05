/**
 *
 */
package org.redcross.sar.gui.field;

import javax.swing.JTextArea;

import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoString;

/**
 * @author kennetgu
 *
 */
@SuppressWarnings("unchecked")
public class TextAreaField extends AbstractField<String,JTextArea,JTextArea> {

	private static final long serialVersionUID = 1L;

	private JTextArea m_editComponent;

	/*==================================================================
	 * Constructors
	 *==================================================================
	 */

	public TextAreaField(IMsoAttributeIf attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
	}

	public TextAreaField(IMsoAttributeIf attribute, String caption,
			boolean isEditable) {
		// forward
		super(attribute, caption, isEditable);
	}

	public TextAreaField(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		// forward
		super(name, caption, isEditable, width, height, value);
	}

	public TextAreaField(String name, String caption, boolean isEditable) {
		// forward
		super(name, caption, isEditable);
	}

	/*==================================================================
	 * Public methods
	 *==================================================================
	 */

	public JTextArea getEditComponent() {
		if(m_editComponent==null) {
			m_editComponent = createTextAreaComponent(true,getViewComponent(),m_documentListener);
			m_editComponent.getDocument().addDocumentListener(m_documentListener);
		}
		return m_editComponent;
	}

	@Override
	public JTextArea getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createTextAreaComponent(false);
		}
		return m_viewComponent;
	}
	
	@Override
	public String getEditValue() {
		return getEditComponent().getText();
	}

	@Override
	protected boolean setNewEditValue(Object value) {
		// update
		getEditComponent().setText(value!=null?String.valueOf(value):null);		
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
		return (attr instanceof MsoString);
	}
	
}
