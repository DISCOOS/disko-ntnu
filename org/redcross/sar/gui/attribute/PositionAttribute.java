/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.PositionSelectorDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.CoordinatePanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
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
	
	private CoordinatePanel m_coordinatePanel;
	private GotoPanel m_gotoPanel = null;
	private PositionSelectorDialog m_selectorDialog = null;
	
	public PositionAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable) {
		// forward
		this(attribute,caption,width,1,isEditable);		
	}
	
	public PositionAttribute(IAttributeIf attribute, String caption, int width, int format, boolean isEditable) {
		// forward
		super(attribute.getName(),caption,width,null,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// save format
		getCoordinatePanel().setFormat(format);
		// get value from attribute
		load();		
		// forward
		initalizeEdit();
	}
	
	public PositionAttribute(String name, String caption, int width, Object value, boolean isEditable) {
		// forward
		this(name,caption,width,value,0,isEditable);		
	}
	
	public PositionAttribute(String name, String caption, int width, Object value, int format, boolean isEditable) {
		// forward
		super(name,caption,width,null,isEditable);
		// set value
		setValue(value);
		// set format
		setFormat(format);
		// forward
		initalizeEdit();
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Component getComponent() {
		if(m_component==null) {
			JFormattedTextField field = new JFormattedTextField();
			field.setEditable(false);
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
	
	public CoordinatePanel getCoordinatePanel() {
		if(m_coordinatePanel==null) {
			m_coordinatePanel = getGotoPanel().getCoordinatePanel();
		}
		return m_coordinatePanel;
	}
	
	public Object getValue() {
		return getCoordinatePanel().getPosition();
	}
	
	public boolean setValue(Object value) {
		// validate data type
		if(value instanceof Point)
			getCoordinatePanel().setPoint((Point)value);
		else if(value instanceof Position) 
			getCoordinatePanel().setPosition((Position)value);
		else if(value instanceof String) 
			getCoordinatePanel().setText((String)value);
		else {
			// failure
			return false;
		}
		// update text panel
		getTextField().setText(getCoordinatePanel().getText());
		// notify change?
		if(!isConsume()) fireOnWorkChange();
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
		getCoordinatePanel().setFormat(format);		
	}
			
	public int getFormat() {
		// get format
		return getCoordinatePanel().getFormat();		
	}

	/*==================================================================
	 * Private methods
	 *================================================================== 
	 */
	
	private GotoPanel getGotoPanel() {
		if(m_gotoPanel==null) {
			m_gotoPanel = getSelectorDialog().getGotoPanel();
		}
		return m_gotoPanel;
	}
	
	private PositionSelectorDialog getSelectorDialog() {
		if(m_selectorDialog==null) {
			m_selectorDialog = new PositionSelectorDialog(Utils.getApp().getFrame());
		}
		return m_selectorDialog;
	}
	
	private void initalizeEdit() {
		// initialize gui
		setButton(DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.SMALL), true);
		// handle actions
		getButton().addActionListener(new ActionListener() {
	
			@Override
			public void actionPerformed(ActionEvent e) {
				// get old value
				Position resume = (Position)getValue();
				// get install map and prepare goto panel
				IDiskoMap map = getInstalledMap();
				getSelectorDialog().onLoad(map);
				if(map!=null)
					getSelectorDialog().setLocationRelativeTo((JComponent)map, DefaultDialog.POS_EAST, false, true);
				else
					getSelectorDialog().setLocationRelativeTo(getButton(), DefaultDialog.POS_WEST, false, false);
				// select position
				Position p = getSelectorDialog().select();
				// update or resume?
				if(p!=null)
					setValue(p);
				else {
					// consume
					setConsume(true);
					// forward
					setValue(resume);
					// resume
					setConsume(false);
				}
				
			}
			
		});
	}
}
