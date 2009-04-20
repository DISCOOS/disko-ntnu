/**
 *
 */
package org.redcross.sar.gui.field;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.redcross.sar.Application;
import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.PositionSelectorDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.format.MGRSFormatter;
import org.redcross.sar.gui.format.UTMFormatter;
import org.redcross.sar.gui.panel.CoordinatePanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
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
public class PositionField extends AbstractField {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_FORMAT = 0;

	private CoordinatePanel m_coordinatePanel;
	private GotoPanel m_gotoPanel;
	private JLabel m_fieldLabel;
	private PositionSelectorDialog m_selectorDialog;
	
	private int m_digits = 5;
	private int m_sIdx = 0;
	private int m_eIdx = 3;
	private boolean m_isHtml = true;

	/*==================================================================
	 * Constructors
	 *==================================================================
	 */

	public PositionField(String name, String caption, boolean isEditable) {
		// forward
		super(name, caption, isEditable);
		// forward
		initialize(null,DEFAULT_FORMAT,isEditable);
	}

	public PositionField(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		// forward
		super(name, caption, isEditable, width, height, value);
		// forward
		initialize(value,DEFAULT_FORMAT,isEditable);
	}

	public PositionField(String name, String caption, boolean isEditable,
			int width, int height, Object value, int format) {
		// forward
		super(name, caption, isEditable, width, height, value);
		// forward
		initialize(value,format,isEditable);
	}

	public PositionField(MsoPosition attribute, String caption, boolean isEditable) {
		// forward
		super(attribute, caption, isEditable);
		// forward
		initialize(null,DEFAULT_FORMAT,isEditable);
	}

	public PositionField(IAttributeIf<?> attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
		// forward
		initialize(getValue(),DEFAULT_FORMAT,isEditable);
	}

	public PositionField(IAttributeIf<?> attribute, String caption,
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
			JPanel panel = new JPanel(new BorderLayout());
			m_fieldLabel = new JLabel();
			m_fieldLabel.setBorder((new JTextField()).getBorder());
			panel.add(m_fieldLabel,BorderLayout.CENTER);
			// save the component
			m_component = panel;
		}
		return m_component;
	}

	public JLabel getLabel() {
		return m_fieldLabel;
	}

	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}

	public boolean getAutoSave() {
		return m_autoSave;
	}
	
	private CoordinatePanel getCoordinatePanel() {
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
		else if(value == null)
			getCoordinatePanel().setText(null);
		else {
			// failure
			return false;
		}
		// set formatted text
		getLabel().setText(getFormattedText());
		// notify change?
		if(isChangeable()) fireOnWorkChange();
		// success
		return true;
	}
	
	public String getFormattedText() {
		String text = getCoordinatePanel().getText();		
		if(getCoordinatePanel().isPositionValid() && m_isHtml) {
			switch(getCoordinatePanel().getFormat()) {
			case CoordinatePanel.MGRS_FORMAT:
				text = MapUtil.formatMGRS(text, MGRSFormatter.MAX_DIGITS, m_digits, m_sIdx, m_eIdx);
				break;
			case CoordinatePanel.UTM_FORMAT:
				text = MapUtil.formatUTM(text, UTMFormatter.MAX_DIGITS, m_digits, m_sIdx, m_eIdx);
				break;
			}		
		}
		// finished
		return Utils.getHtml(text);		
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

	public int getFormat() {
		return getCoordinatePanel().getFormat();
	}

	public void setFormat(int format) {
		switch(format) {
		case CoordinatePanel.MGRS_FORMAT:
			m_sIdx = 0; m_eIdx = 3; break;
		case CoordinatePanel.UTM_FORMAT:
			m_sIdx = 2; m_eIdx = 5; break;
		}
		// update format
		getCoordinatePanel().setFormat(format);
		// set formatted text
		getLabel().setText(getFormattedText());
	}
	
	public int getDigits() {
		return m_digits;
	}
	
	public void setDigits(int digits) {
		// prepare
		m_digits = digits;
		// set formatted text
		getLabel().setText(getFormattedText());
	}

	public boolean isHtml() {
		return m_isHtml;
	}
	
	public void setHtml(boolean isHtml) {
		// prepare
		m_isHtml = isHtml;
		// set formatted text
		getLabel().setText(getFormattedText());
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
			m_selectorDialog = new PositionSelectorDialog(Application.getInstance());
		}
		return m_selectorDialog;
	}

	private void initalizeEdit() {
		// initialize GUI
		installButton(DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.SMALL), true);
		// handle actions
		getButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// get old value
				Position resume = (Position)getValue();
				// get install map and prepare goto panel
				IDiskoMap map = getInstalledMap();
				// map installed?
				if(map!=null) {
					getSelectorDialog().onLoad(map);
					if(map!=null)
						getSelectorDialog().setSnapToLocation((JComponent)map, DefaultDialog.POS_EAST, 0, true, false);
					else
						getSelectorDialog().setSnapToLocation(getButton(), DefaultDialog.POS_WEST, 0, false, false);
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
			}

		});
	}
}
