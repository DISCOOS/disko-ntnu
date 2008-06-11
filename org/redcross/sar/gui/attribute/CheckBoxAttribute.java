/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;


/**
 * @author kennetgu
 *
 */
public class CheckBoxAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public CheckBoxAttribute(AttributeImpl.MsoBoolean attribute, String caption, int width, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,150,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	public CheckBoxAttribute(String name, String caption, int width, boolean value, boolean isEditable) {
		// forward
		super(name,caption,width,value,isEditable);
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
					if(isWorking()) return;
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
	
	public Object getValue() {
		return ((JCheckBox)m_component).isSelected();
	}
	
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
	
	public boolean setMsoAttribute(IAttributeIf attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoBoolean) {
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
