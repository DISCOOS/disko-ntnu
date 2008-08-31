/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoBoolean;


/**
 * @author kennetgu
 *
 */
public class CheckBoxAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */
	
	public CheckBoxAttribute(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		super(name, caption, isEditable, width, height, value);
	}

	public CheckBoxAttribute(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
	}
		
	public CheckBoxAttribute(MsoBoolean attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
	}

	public CheckBoxAttribute(MsoBoolean attribute, String caption, boolean isEditable,
			int width, int height) {
		super(attribute, caption, isEditable, width, height);
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Component getComponent() {
		if(m_component==null) {
			JCheckBox cb = new JCheckBox();
			m_component = cb;
			m_component.setEnabled(m_isEditable);
			cb.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if(isConsume()) return;
					fireOnWorkChange();
				}
				
			});
		}
		return m_component;
	}

	public JCheckBox getCheckBox() {
		return ((JCheckBox)m_component);
	}
	
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	@Override
	public Boolean getValue() {
		return ((JCheckBox)m_component).isSelected();
	}
	
	@Override
	public boolean setValue(Object value) {
		if(value instanceof String)
			((JCheckBox)m_component).setSelected(Boolean.valueOf((String)value));
		else if(value instanceof Boolean) 
			((JCheckBox)m_component).setSelected(Boolean.valueOf((Boolean)value));
		else {
			// failure
			return false;
		}
		// success
		return true;
	}
	
	@Override
	public boolean setMsoAttribute(IAttributeIf<?> attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof MsoBoolean) {
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
		getCheckBox().setEnabled(isEditable && isEnabled());		
	}
		
}
