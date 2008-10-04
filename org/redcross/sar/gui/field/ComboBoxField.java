/**
 * 
 */
package org.redcross.sar.gui.field;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoString;

/**
 * @author kennetgu
 *
 */
public class ComboBoxField extends AbstractField {
	
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
	
	public ComboBoxField(MsoString attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
	}

	public ComboBoxField(MsoString attribute, String caption, int width,
			int height, boolean isEditable) {
		super(attribute, caption, isEditable, width, height);
	}

	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */

	public Component getComponent() {
		if(m_component==null) {
			JComboBox field = new JComboBox();
			field.setEditable(m_isEditable);
			field.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(!isChangeable()) return;
					fireOnWorkChange();
					
				}
			});
			
			// save the component
			m_component = field;			
		}
		return m_component;
	}
	
	public JComboBox getComboBox() {
		return (JComboBox)m_component;
	}
			
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	public boolean fill(Object values) {
		try {
			if(values instanceof Object[])
				((JComboBox)m_component).setModel(new DefaultComboBoxModel((Object[])values));
			else
				((JComboBox)m_component).setModel((ComboBoxModel)values);
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Object getValue() {
		return ((JComboBox)m_component).getSelectedItem();
	}
	
	public boolean setValue(Object value) {
		// update
		((JComboBox)m_component).setSelectedItem(value);
		// success
		return true;
	}
	
	public boolean setMsoAttribute(IAttributeIf<?> attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof MsoString) {
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
		getComboBox().setEditable(isEditable);		
	}
	
	
	/*
		private JLabel m_label;
		
		@Override
		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected, boolean hasFocus) {
			
			if(m_label==null) m_label = new JLabel();
			
			// translate
			if(value instanceof Enum) {			
				m_label.setText(DiskoEnumFactory.getText((Enum)value));
			}
			else if (value!=null) {
				m_label.setText(value.toString());
			}
			else {
				m_label.setText("");				
			}
			
			// update selection state
			if (isSelected){
				m_label.setBackground(list.getSelectionBackground());
				m_label.setForeground(list.getSelectionForeground());
			} 
			else {
				m_label.setBackground(list.getBackground());
				m_label.setForeground(list.getForeground());
			}
			// finished
			return m_label;
		}
		
	};
	*/

}
