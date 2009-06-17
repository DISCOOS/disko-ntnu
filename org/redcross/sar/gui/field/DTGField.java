package org.redcross.sar.gui.field;

import java.util.Calendar;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.format.DTGFormatter;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoCalendar;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;

/**
 * @author kennetgu
 *
 */
public class DTGField extends AbstractField<Calendar,JFormattedTextField,JTextField> {

	private static final long serialVersionUID = 1L;
	private static final Logger m_logger = Logger.getLogger(DTGField.class); 

	private int m_year=Calendar.getInstance().get(Calendar.YEAR);
	private int m_month=Calendar.getInstance().get(Calendar.MONTH);

	/*==================================================================
	 * Constructors
	 *==================================================================
	 */

	public DTGField(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
		setOffset(Calendar.getInstance());
	}

	public DTGField(String name, String caption, boolean isEditable, int width, int height) {
		super(name, caption, isEditable, width, height, null);
		setOffset(Calendar.getInstance());
	}

	public DTGField(String name, String caption, boolean isEditable,
			int width, int height, Calendar time) {
		super(name, caption, isEditable, width, height, time);
		setOffset(time);
	}

	public DTGField(MsoCalendar attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
		setOffset(Calendar.getInstance());
	}

	public DTGField(MsoCalendar attribute, String caption, boolean isEditable,
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

	public JFormattedTextField getEditComponent() {
		if(m_editComponent==null) {
			// create
			m_editComponent = new JFormattedTextField();
			m_editComponent.setFormatterFactory(new DTGFormatterFactory());
			m_editComponent.getDocument().addDocumentListener(m_documentListener);
		}
		return m_editComponent;
	}

	public JTextField getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createDefaultComponent(false);
		}
		return m_viewComponent;
	}

	@Override
	public Calendar getEditValue() {
		// initialize to current attribute value
		Calendar time = m_model.getValue();
		try {
			Object value = getEditComponent().getValue();
			time = DTG.DTGToCal(m_year,m_month,value!=null?value.toString():"000000");
		} catch (IllegalMsoArgumentException e) {
			// consume
		}
		return time;
	}

	@Override
	public String getFormattedText() {
		return getDTG();
	}
	
	public int getYear() {
		return m_year;
	}
	
	public void setYear(int year) {
		m_year = year;
	}
	
	public int getMonth() {
		return m_month;
	}
	
	public void setMonth(int month) {
		m_month = month;
	}
	
	public String getDTG() {
		return DTG.CalToDTG(isDirty()?getEditValue():getValue());
	}

	public boolean setDTG(String aDTG) {
		try {
			setValue(DTG.DTGToCal(m_year, m_month, aDTG));
			return true;
		} catch (IllegalMsoArgumentException e) {
			m_logger.error("Failed to convert DTG to Calendar",e);
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
			m_logger.error("Failed to convert DTG to Calendar",e);
		}
		return false;
	}

	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	protected boolean isMsoAttributeSettable(IMsoAttributeIf<?> attr) {
		return (attr instanceof MsoCalendar);
	}
	
	@Override
	protected boolean setNewEditValue(Object value) {
		String text = "";
		// validate data type
		if(value instanceof Calendar) {
			setOffset((Calendar)value);
			text = DTG.CalToDTG((Calendar)value);
		}
		else if (value instanceof String || 
				value instanceof Number) {
			try {
				setValue(DTG.DTGToCal(m_year,m_month,value.toString()));
			} catch (IllegalMsoArgumentException e) {
				m_logger.error("Failed to convert value to Calendar",e);
			}
		}
		else if(value!=null) {
			return false;
		}
		getEditComponent().setText(text);
		getViewComponent().setText(getFormattedText());
		// success
		return true;
	}
	
	protected static boolean isValueChanged(Object oldValue, Object newValue) {
		if(newValue instanceof Calendar) {
			return !DTG.isEqualDTG((Calendar)oldValue,(Calendar)newValue);
		}
		return false;
	}
	
	/*==================================================================
	 * Inner classes
	 *==================================================================
	 */

	class DTGFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {

		@Override
		public AbstractFormatter getFormatter(JFormattedTextField t) {
			DTGFormatter mf1 = null;
			try {
				mf1 = new DTGFormatter();
			}
			catch (Exception e) {
				m_logger.error("Failed to create DTG formatter",e);
			}
			return mf1;
		}

	}
	
}
