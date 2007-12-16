/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;

import javax.swing.JTextField;

import org.redcross.sar.gui.map.PositionField;
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
	
	public PositionAttribute(IAttributeIf attribute, String caption, boolean isEditable) {
		// forward
		this(attribute,caption,0,isEditable);		
	}
	
	public PositionAttribute(IAttributeIf attribute, String caption, int format, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// save format
		((PositionField)m_component).setFormat(format);
		// get value from attribute
		load();		
	}
	
	public PositionAttribute(String name, String caption, Object value, boolean isEditable) {
		// forward
		this(name,caption,value,0,isEditable);		
	}
	
	public PositionAttribute(String name, String caption, Object value, int format, boolean isEditable) {
		// forward
		super(name,caption,value,isEditable);
		// save format
		((PositionField)m_component).setFormat(format);
	}
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected Component getComponent() {
		if(m_component==null) {
			m_component = new PositionField();
			((PositionField)m_component).setEditable(m_isEditable);
		}
		return m_component;
	}

	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Object getValue() {
		return ((PositionField)m_component).getPosition();
	}
	
	public boolean setValue(Object value) {
		// allowed?
		if(!m_isEditable) return false;
		// validate data type
		if(value instanceof Point)
			((PositionField)m_component).setPoint((Point)value);
		else if(value instanceof Position) 
			((PositionField)m_component).setPosition((Position)value);
		else if(value instanceof String) 
			((PositionField)m_component).setText((String)value);
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
		((PositionField)m_component).setFormat(format);		
	}
			
	public int getFormat() {
		// get format
		return ((PositionField)m_component).getFormat();		
	}
}
