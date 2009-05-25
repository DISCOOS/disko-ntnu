/**
 * 
 */
package org.redcross.sar.gui.field;

import java.awt.Component;

import javax.swing.JFormattedTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoString;

/**
 * @author kennetgu
 *
 */
@SuppressWarnings("unchecked")
public class TextField extends AbstractField<String,JFormattedTextField,JTextField> {
	
	private static final long serialVersionUID = 1L;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */	
	
	public TextField(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
	}
		
	public TextField(String name, String caption, 
			boolean isEditable, int width, int height) {
		super(name, caption, isEditable, width, height, "");
	}
	
	public TextField(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		super(name, caption, isEditable, width, height, value);
	}
	
	public TextField(IMsoAttributeIf attribute, String caption,
			boolean isEditable, int width, int height) {
		super(attribute, caption, isEditable, width, height);
	}

	public TextField(IMsoAttributeIf attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
	}

	/* ==================================================================
	 *  Public methods
	 * ================================================================== */

	public JFormattedTextField getEditComponent() {
		if(m_editComponent==null) {
			// create
			m_editComponent = createDefaultComponent(true,m_documentListener);
			//m_editComponent.getDocument().addDocumentListener(m_documentListener);
		}
		return m_editComponent;
	}

	public JTextField getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createDefaultComponent(false);
			m_viewComponent.setEditable(false);
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
	
	@Override
	protected boolean isScrollable(Component c) {
		return false;
	}
	
	protected boolean isMsoAttributeSettable(IMsoAttributeIf<?> attr) {
		return (attr instanceof MsoString);
	}
	
	@Override
	protected JScrollPane createDefaultScrollPane(Component c) {
		JScrollPane scrollPane = UIFactory.createScrollPane(c,false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}
}
