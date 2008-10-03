/**
 * 
 */
package org.redcross.sar.gui.field;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.PositionSelectorDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.CoordinatePanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoPosition;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.Point;

/**
 * @author kennetgu
 *
 */
public class PositionAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	private static final int DEFAULT_FORMAT = 0; 
	
	private CoordinatePanel m_coordinatePanel;
	private GotoPanel m_gotoPanel = null;
	private PositionSelectorDialog m_selectorDialog = null;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */	
		
	public PositionAttribute(String name, String caption, boolean isEditable) {
		// forward
		super(name, caption, isEditable);
		// forward
		initialize(null,DEFAULT_FORMAT,isEditable);
	}

	public PositionAttribute(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		// forward
		super(name, caption, isEditable, width, height, value);
		// forward
		initialize(value,DEFAULT_FORMAT,isEditable);
	}

	public PositionAttribute(String name, String caption, boolean isEditable,
			int width, int height, Object value, int format) {
		// forward
		super(name, caption, isEditable, width, height, value);
		// forward
		initialize(value,format,isEditable);
	}
	
	public PositionAttribute(MsoPosition attribute, String caption, boolean isEditable) {
		// forward
		super(attribute, caption, isEditable);
		// forward
		initialize(null,DEFAULT_FORMAT,isEditable);
	}

	public PositionAttribute(IAttributeIf<?> attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
		// forward
		initialize(getValue(),DEFAULT_FORMAT,isEditable);
	}

	public PositionAttribute(IAttributeIf<?> attribute, String caption,
			boolean isEditable, int width, int height, int format) {
		// forward
		super(attribute, caption, isEditable, width, height);
		// forward
		initialize(getValue(),format,isEditable);
	}
	
	private void initialize(Object value, int format, boolean isEditable) {
		// set format
		setFormat(format);
		// set value
		setValue(value);
		// forward
		initalizeEdit();
		// forward
		setEditable(isEditable);
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
		if(!isChangeable()) fireOnWorkChange();
		// success
		return true;
	}
	
	public boolean setMsoAttribute(IAttributeIf<?> attribute) {
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
		installButton(DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.SMALL), true);
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
				Position p = getSelectorDialog().select(resume);
				// update or resume?
				if(p!=null)
					setValue(p);
				else {
					// consume
					setChangeable(false);
					// forward
					setValue(resume);
					// resume
					setChangeable(true);
				}
				
			}
			
		});
	}
}
