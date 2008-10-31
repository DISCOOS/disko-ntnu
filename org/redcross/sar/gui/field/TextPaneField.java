/**
 *
 */
package org.redcross.sar.gui.field;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
			m_textPane = new JTextPane();
			m_textPane.setEditable(m_isEditable);
			m_textPane.getDocument().addDocumentListener(new DocumentListener() {

				public void changedUpdate(DocumentEvent e) { change(); }

				public void insertUpdate(DocumentEvent e) { change(); }

				public void removeUpdate(DocumentEvent e) { change(); }

				private void change() {
					if(!isChangeable()) return;
					fireOnWorkChange();
				}

			});
			m_textPane.setBorder(UIFactory.createBorder());
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
		getTextPane().setText(String.valueOf(value));
		// success
		return true;
	}

	public boolean setMsoAttribute(IAttributeIf<?> attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoString) {
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

	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);
		getTextPane().setEditable(isEditable);
	}

}
