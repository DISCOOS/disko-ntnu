/**
 *
 */
package org.redcross.sar.gui.field;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Document;

import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;

/**
 * @author kennetgu
 *
 */
public class TextPaneField extends AbstractField {

	private static final long serialVersionUID = 1L;

	private JTextPane m_textPane;

	/*==================================================================
	 * Constructors
	 *==================================================================
	 */

	public TextPaneField(IAttributeIf<?> attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
	}

	public TextPaneField(IAttributeIf<?> attribute, String caption,
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

	public Component getComponent() {
		if(m_component==null) {
			JScrollPane scrollPane = UIFactory.createScrollPane(getTextPane(),false);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// save the component
			m_component = scrollPane;
		}
		return m_component;
	}

	public JTextPane getTextPane() {
		if(m_textPane==null) {
			m_textPane = new JTextPane() {
				
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
			m_textPane.setEditable(m_isEditable);
			m_textPane.setBorder(UIFactory.createBorder());
			// set listeners
			m_textPane.getDocument().addUndoableEditListener(m_undoListener);
		}
		return m_textPane;
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
		return getTextPane().getText();
	}

	public boolean setValue(Object value) {
		// update
		getTextPane().setText(value!=null?String.valueOf(value):null);
		// success
		return true;
	}

	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);
		getTextPane().setEditable(isEditable);
	}

	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	protected boolean isMsoAttributeSettable(IAttributeIf<?> attr) {
		return (attr instanceof AttributeImpl.MsoString);
	}
	
}
