/**
 *
 */
package org.redcross.sar.gui.field;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class TextAreaField extends AbstractField {

	private static final long serialVersionUID = 1L;

	private JTextArea m_textArea;

	/*==================================================================
	 * Constructors
	 *==================================================================
	 */

	public TextAreaField(IAttributeIf<?> attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
	}

	public TextAreaField(IAttributeIf<?> attribute, String caption,
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

	public Component getComponent() {
		if(m_component==null) {
			JScrollPane scrollPane = UIFactory.createScrollPane(getTextArea(),false);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// save the component
			m_component = scrollPane;
		}
		return m_component;
	}

	public JTextArea getTextArea() {
		if(m_textArea==null) {
			m_textArea = new JTextArea() {
				
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
			m_textArea.setEditable(m_isEditable);
			m_textArea.setRows(2);
			m_textArea.setLineWrap(true);
			m_textArea.setWrapStyleWord(true);
			m_textArea.setBorder(UIFactory.createBorder());
			m_textArea.setLineWrap(true);
			m_textArea.setWrapStyleWord(true);
			// set listeners
			m_textArea.getDocument().addUndoableEditListener(m_undoListener);
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
		getTextArea().setText(value!=null?String.valueOf(value):null);
		// success
		return true;
	}

	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);
		getTextArea().setEditable(isEditable);
	}

	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	protected boolean isMsoAttributeSettable(IAttributeIf<?> attr) {
		return (attr instanceof AttributeImpl.MsoString);
	}
	
}
