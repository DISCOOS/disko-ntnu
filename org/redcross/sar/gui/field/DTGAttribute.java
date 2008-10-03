/**
 * 
 */
package org.redcross.sar.gui.field;

import java.awt.Component;
import java.util.Calendar;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.format.DTGFormat;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoCalendar;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;

/**
 * @author kennetgu
 *
 */
public class DTGAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	private int m_year;
	private int m_month;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */
	
	public DTGAttribute(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
		setOffset(Calendar.getInstance());
	}
		
	public DTGAttribute(String name, String caption, boolean isEditable, int width, int height) {
		super(name, caption, isEditable, width, height, null);
		setOffset(Calendar.getInstance());
	}
	
	public DTGAttribute(String name, String caption, boolean isEditable,
			int width, int height, Calendar time) {
		super(name, caption, isEditable, width, height, time);
		setOffset(time);
	}

	public DTGAttribute(MsoCalendar attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
		setOffset(Calendar.getInstance());
	}

	public DTGAttribute(MsoCalendar attribute, String caption, boolean isEditable,
			int width, int height, Calendar time) {
		super(attribute, caption, isEditable, width, height);
		setOffset(time);
	}
	
	private void setOffset(Calendar t) {
		m_year = t.get(Calendar.YEAR);
		m_month = t.get(Calendar.MONTH);		
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Component getComponent() {
		if(m_component==null) {
			// create
			JFormattedTextField field = new JFormattedTextField();
			// set format
			field.setFormatterFactory(new DTGFormatterFactory());
			field.setEditable(m_isEditable);
			field.getDocument().addDocumentListener(new DocumentListener() {

				public void changedUpdate(DocumentEvent e) { change(); }

				public void insertUpdate(DocumentEvent e) { change(); }

				public void removeUpdate(DocumentEvent e) { change(); }
				
				private void change() {
					if(!isChangeable()) return;
					fireOnWorkChange();
				}
				
			});
			// save the component
			m_component = field;
			
		}
		return m_component;
	}
			
	public JFormattedTextField getTextField() {
		return (JFormattedTextField)m_component;
	}
	
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	@Override
	public Calendar getValue() {
		// initialize to current attribute value
		Calendar time = m_attribute!=null 
				? (Calendar)MsoUtils.getAttribValue(m_attribute) : null;
		// try to get DTG from text field
		try {
			time = DTG.DTGToCal(m_year,m_month,((JFormattedTextField)m_component).getText());
		} catch (IllegalMsoArgumentException e) {
			// consume
		}
		return time;
	}
	
	public boolean setValue(Object value) {
		// validate data type
		if(value instanceof Calendar) {
			setOffset((Calendar)value);
			((JFormattedTextField)m_component).setText(DTG.CalToDTG((Calendar)value));
		}
		else if (value instanceof String ||
				 value instanceof Number) {
			((JFormattedTextField)m_component).setText(String.valueOf(value));
		}
		else {
			return false;
		}
		// success
		return true;
	}
	
	public String getDTG() {
		return DTG.CalToDTG(getValue());
	}
	
	public boolean setDTG(String aDTG) {
		try {
			setValue(DTG.DTGToCal(m_year, m_month, aDTG));
			return true;
		} catch (IllegalMsoArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean setDTG(int year, int month, String aDTG) {
		try {
			m_year = year;
			m_month = month;
			setValue(DTG.DTGToCal(m_year, m_month, aDTG));
			return true;
		} catch (IllegalMsoArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean setMsoAttribute(IAttributeIf<?> attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoCalendar) {
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
		getTextField().setEditable(isEditable);		
	}
	
	/*==================================================================
	 * Inner classes
	 *================================================================== 
	 */
	
	class DTGFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {

		@Override
		public AbstractFormatter getFormatter(JFormattedTextField t) {
			DTGFormat mf1 = null;
			try {
				mf1 = new DTGFormat();
			}
			catch (Exception e) {
				e.printStackTrace();
			}			
			return mf1;
		}
		
	}
	
}
