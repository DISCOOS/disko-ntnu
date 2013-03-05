/**
 *
 */
package org.redcross.sar.gui.field;

import javax.swing.JTextPane;

import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoString;

/**
 * @author kennetgu
 *
 */
@SuppressWarnings("unchecked")
public class TextPaneField extends AbstractField<String,JTextPane,JTextPane> {

	private static final long serialVersionUID = 1L;

	/*==================================================================
	 * Constructors
	 *==================================================================
	 */

	public TextPaneField(IMsoAttributeIf attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
	}

	public TextPaneField(IMsoAttributeIf attribute, String caption,
			boolean isEditable) {
		// forward
		super(attribute, caption, isEditable);
	}

	public TextPaneField(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		// forward
		super(name, caption, isEditable, width, height, value);
	}

	public TextPaneField(String name, String caption, boolean isEditable) {
		// forward
		super(name, caption, isEditable);
	}

	/*==================================================================
	 * Public methods
	 *==================================================================
	 */

	public JTextPane getEditComponent() {
		if(m_editComponent==null) {
			m_editComponent = createTextPaneComponent("text/html",true);
			m_editComponent.getDocument().addDocumentListener(m_documentListener);
		}
		return m_editComponent;
	}

	public JTextPane getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createTextPaneComponent("text/html",false);
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
