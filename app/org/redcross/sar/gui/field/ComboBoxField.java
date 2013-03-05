/**
 *
 */
package org.redcross.sar.gui.field;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.redcross.sar.gui.IStringConverter;
import org.redcross.sar.mso.data.IMsoAttributeIf;

/**
 * @author kennetgu
 *
 */
@SuppressWarnings("unchecked")
public class ComboBoxField extends AbstractField<Object,JComboBox,JTextField> {

	private static final long serialVersionUID = 1L;

	/*==================================================================
	 * Constructors
	 *==================================================================
	 */

	public ComboBoxField(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		super(name, caption, isEditable, width, height, value);
	}

	public ComboBoxField(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
	}

	public ComboBoxField(IMsoAttributeIf attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
	}

	public ComboBoxField(IMsoAttributeIf attribute, String caption, int width,
			int height, boolean isEditable) {
		super(attribute, caption, isEditable, width, height);
	}

	/*==================================================================
	 * Public methods
	 *==================================================================
	 */

	public JComboBox getEditComponent() {
		if(m_editComponent==null) {
			JComboBox field = new JComboBox();
			field.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					onEditValueChanged();
				}
			});
			// save the component
			m_editComponent = field;
		}
		return m_editComponent;
	}

	public JTextField getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createDefaultComponent(false);
		}
		return m_viewComponent;
	}
	
	public boolean fill(Object values) {
		try {
			if(values instanceof Object[])
				getEditComponent().setModel(new DefaultComboBoxModel((Object[])values));
			else if(values instanceof ComboBoxModel)
				getEditComponent().setModel((ComboBoxModel)values);
			else 
				getEditComponent().setModel(new DefaultComboBoxModel());
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Object getEditValue() {
		return ((JComboBox)m_editComponent).getSelectedItem();
	}

	@Override
	protected boolean setNewEditValue(Object value) {
		// update
		if(value==null || (value instanceof String && value.toString().isEmpty())) {
			getEditComponent().setSelectedIndex(-1);
		} else {
			getEditComponent().setSelectedItem(value);
		}
		getViewComponent().setText(getFormattedText());
		// success
		return true;
	}
	
	@Override
	public String getFormattedText() {
		int index = getEditComponent().getSelectedIndex();
		if(index!=-1) {
			Object selected = getEditComponent().getSelectedItem();
			ListCellRenderer renderer = getEditComponent().getRenderer();
			if(renderer instanceof IStringConverter) {
				return ((IStringConverter)renderer).toString(selected);
			}
			return selected!=null?selected.toString():"";
		}
		return "";
	}
	
	public boolean isListEditable() {
		return getEditComponent().isEditable();
	}
	
	public void setListEditable(boolean isEditable) {
		getEditComponent().setEditable(isEditable);
	}
	
	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	protected boolean isMsoAttributeSettable(IMsoAttributeIf<?> attr) {
		return true;
	}
	
}
