/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.panel.PositionFieldPanel;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.Point;

/**
 * @author kennetgu
 *
 */
public class PositionAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	public PositionAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable) {
		// forward
		this(attribute,caption,width,0,isEditable);		
	}
	
	public PositionAttribute(IAttributeIf attribute, String caption, int width, int format, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,width,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// save format
		((PositionFieldPanel)m_component).setFormat(format);
		// get value from attribute
		load();		
	}
	
	public PositionAttribute(String name, String caption, int width, Object value, boolean isEditable) {
		// forward
		this(name,caption,width,value,0,isEditable);		
	}
	
	public PositionAttribute(String name, String caption, int width, Object value, int format, boolean isEditable) {
		// forward
		super(name,caption,width,value,isEditable);
		// save format
		((PositionFieldPanel)m_component).setFormat(format);
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Component getComponent() {
		try {
			if(m_component==null) {
				PositionFieldPanel field = new PositionFieldPanel();
				field.setEditable(m_isEditable);
				field.addChangeListener(new ChangeListener() {

					public void stateChanged(ChangeEvent e) {
						if(isWorking()) return;
						fireOnWorkChange();
					}
					
				});
				m_component = field;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m_component;
	}

	public PositionFieldPanel getPositionField() {
		return (PositionFieldPanel)m_component;
	}
	
	public Object getValue() {
		return ((PositionFieldPanel)m_component).getPosition();
	}
	
	public boolean setValue(Object value) {
		// allowed?
		if(!m_isEditable) return false;
		// validate data type
		if(value instanceof Point)
			((PositionFieldPanel)m_component).setPoint((Point)value);
		else if(value instanceof Position) 
			((PositionFieldPanel)m_component).setPosition((Position)value);
		else if(value instanceof String) 
			((PositionFieldPanel)m_component).setText((String)value);
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
			if(attribute instanceof AttributeImpl.MsoPosition ||
					attribute instanceof AttributeImpl.MsoTimePos) {
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
	
	public void setFormat(int format) {
		// save format
		((PositionFieldPanel)m_component).setFormat(format);		
	}
			
	public int getFormat() {
		// get format
		return ((PositionFieldPanel)m_component).getFormat();		
	}
	
	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);
		getPositionField().setEditable(isEditable);		
	}
	
}
