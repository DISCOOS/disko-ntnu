/**
 *
 */
package org.redcross.sar.gui.field;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JTextPane;

import org.redcross.sar.Application;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.PositionSelectorDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.format.MGRSFormatter;
import org.redcross.sar.gui.format.UTMFormatter;
import org.redcross.sar.gui.panel.CoordinatePanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.AttributeImpl.MsoTimePos;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoPosition;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.Point;

/**
 * @author kennetgu
 *
 */
@SuppressWarnings("unchecked")
public class PositionField extends AbstractField<Position,JTextPane,JTextPane> {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_FORMAT = 0;

	private CoordinatePanel m_coordinatePanel;
	private GotoPanel m_gotoPanel;
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

	public PositionField(IMsoAttributeIf attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, isEditable, width, height);
		// forward
		initialize(getValue(),DEFAULT_FORMAT,isEditable);
	}

	public PositionField(IMsoAttributeIf attribute, String caption,
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
	}

	/* ==================================================================
	 *  anonymous classes
	 * ================================================================== */
	
	private final ActionListener m_actionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// get install map and prepare goto panel
			IDiskoMap map = getInstalledMap();
			// map installed?
			if(map!=null) {
				// prepare selector dialog				
				setSelectionMap(map);
				// prompt user for selection
				Position p = getSelectorDialog().select(getEditValue());
				// user selected a value?
				if(p!=null) {
					setValue(p);
				} else { 
					reset(); 
				}
			}
			
		}

	};	
	
	/* ==================================================================
	 *  Public methods
	 * ================================================================== */

	public JTextPane getEditComponent() {
		if(m_editComponent==null) {
			m_editComponent = createTextPaneComponent("text/html",true);
		}
		m_editComponent.setEditable(true);
		return m_editComponent;
	}

	public JTextPane getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createTextPaneComponent("text/html",false);
		}
		return m_viewComponent;
	}
	
	@Override
	public Position getEditValue() {
		return getCoordinatePanel().getPosition();
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
		getViewComponent().setText(getFormattedText());
	}
	
	public int getDigits() {
		return m_digits;
	}
	
	public void setDigits(int digits) {
		// prepare
		m_digits = digits;
		// set formatted text
		getViewComponent().setText(getFormattedText());
	}

	public boolean isHtml() {
		return m_isHtml;
	}
	
	public void setHtml(boolean isHtml) {
		// prepare
		m_isHtml = isHtml;
		// set formatted text
		getViewComponent().setText(getFormattedText());
	}
	
	@Override
	public final void installButton(AbstractButton button, boolean isVisible) {
		// forward
		super.installButton(button, isVisible);
		// handle actions
		getButton().addActionListener(m_actionListener);
	}
	
	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	@Override
	protected boolean setNewEditValue(Object value) {
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
		getEditComponent().setText(getFormattedText());
		getViewComponent().setText(getFormattedText());
		// success
		return true;
	}
	
	protected boolean isMsoAttributeSettable(IMsoAttributeIf<?> attr) {
		return (attr instanceof MsoPosition ||
				attr instanceof MsoTimePos);
	}
	
	/* ==================================================================
	 *  Private methods
	 * ================================================================== */

	private CoordinatePanel getCoordinatePanel() {
		if(m_coordinatePanel==null) {
			m_coordinatePanel = getGotoPanel().getCoordinatePanel();
		}
		return m_coordinatePanel;
	}

	private GotoPanel getGotoPanel() {
		if(m_gotoPanel==null) {
			m_gotoPanel = getSelectorDialog().getGotoPanel();
		}
		return m_gotoPanel;
	}

	private PositionSelectorDialog getSelectorDialog() {
		if(m_selectorDialog==null) {
			m_selectorDialog = new PositionSelectorDialog(Application.getFrameInstance());
		}
		return m_selectorDialog;
	}

	private void initalizeEdit() {
		// initialize GUI
		installButton(DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.SMALL), true);
	}
	
	private void setSelectionMap(IDiskoMap map) {
		getSelectorDialog().onLoad(map);
		if(map!=null)
			getSelectorDialog().setSnapToLocation((JComponent)map, DefaultDialog.POS_CENTER, 0, true, false);
		else
			getSelectorDialog().setSnapToLocation(getButton(), DefaultDialog.POS_WEST, 0, false, false);
	}
}
