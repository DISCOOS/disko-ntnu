package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseUpEvent;
import com.esri.arcgis.interop.AutomationException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author kennetgu
 *
 */
public class GotoPanel extends DiskoPanel {

	private static final long serialVersionUID = 1L;
	
	private JTabbedPane m_tabbedPane = null;
	private JPanel m_mgrsPanel = null;
	private JPanel m_utmPanel = null;
	private JPanel m_degPanel = null;
	private JPanel m_desPanel = null;
	private PositionField m_positionField = null;
	private JButton m_gotoButton = null;
	
	private IDiskoMap m_map = null;
	
	private boolean m_isAutoUpdate = false;
	
	private final IMapControlEvents2Adapter m_mouseAdapter = new IMapControlEvents2Adapter() {

		private static final long serialVersionUID = 1L;

		@Override
		public void onMouseUp(IMapControlEvents2OnMouseUpEvent e) throws IOException, AutomationException {
			// forward
			super.onMouseUp(e);
			// update point?
			if(m_map!=null)
				getPositionField().setPoint(m_map.getClickPoint());
		}
		
	};
	
	/**
	 * Constructor 
	 * 
	 */
	public GotoPanel() {
		
		// initialize GUI
		initialize();
		
	}

	/**
	 * Constructor 
	 * 
	 * @param caption
	 */
	public GotoPanel(String caption) {
		
		// initialize GUI
		initialize();
		
		// force caption
		setCaptionText(caption);
		
	}
	
	private void initialize() {
		try {
			this.setCaptionText("Gå til posisjon");
			this.setBodyComponent(getTabbedPane());
			// apply mgrs format
			formatChanged(1);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public void setAutoUpdate(boolean isAutoUpdate) {
		// save state
		m_isAutoUpdate = isAutoUpdate;
		// set listener status
		registerMouseListener(isAutoUpdate);
	}
	
	public boolean isAutoUpdate() {
		return m_isAutoUpdate;
	}

	/**
	 * This method initializes m_mgrsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMGRSPanel() {
		if (m_mgrsPanel == null) {
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(5);
			m_mgrsPanel = new JPanel();
			m_mgrsPanel.setLayout(bl);
		}
		return m_mgrsPanel;
	}
	
	/**
	 * This method initializes m_utmPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getUTMPanel() {
		if (m_utmPanel == null) {
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(5);
			m_utmPanel = new JPanel();
			m_utmPanel.setLayout(bl);
		}
		return m_utmPanel;
	}
	
	/**
	 * This method initializes m_utmPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDEGPanel() {
		if (m_degPanel == null) {
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(5);
			m_degPanel = new JPanel();
			m_degPanel.setLayout(bl);
		}
		return m_degPanel;
	}
	
	/**
	 * This method initializes m_desPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDESPanel() {
		if (m_desPanel == null) {
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(5);
			m_desPanel = new JPanel();
			m_desPanel.setLayout(bl);
		}
		return m_desPanel;
	}
	
	/**
	 * This method initializes m_positionField	
	 * 	
	 * @return getPositionField	
	 */
	public PositionField getPositionField() {
		if (m_positionField == null) {
			m_positionField = new PositionField();
		}
		return m_positionField;
	}

	/**
	 * This method initializes m_poiField	
	 * 	
	 * @return POIField	
	 */
	private JButton getGotoButton() {
		if (m_gotoButton == null) {
			m_gotoButton = DiskoButtonFactory.createButton("MAP.CENTERAT",ButtonSize.NORMAL);			
			m_gotoButton.setEnabled(false);
			m_gotoButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// forward
					centerAt();
				}
				
			});
		}
		return m_gotoButton;
	}
	
	public void setGotoButtonVisible(boolean isVisible) {
		getGotoButton().setVisible(isVisible);
	}
	
	public boolean isGotoButtonVisible() {
		return getGotoButton().isVisible();
	}
	
	public void reset() {
		if(m_map!=null) 
			getClickPoint();
		else
			getPositionField().setText(null);		
	}
	
	public void setMap(IDiskoMap map) {
		// unregister 
		registerMouseListener(false);
		// prepare
		this.m_map = map;		
		// Enable goto button?
		getGotoButton().setEnabled(m_map!=null);
		// set listener status
		registerMouseListener(m_isAutoUpdate);
	}
	
	public void getClickPoint() {
		if(m_map!=null)
			getPositionField().setPoint(m_map.getClickPoint());
	}
	
	public void getMovePoint() {
		if(m_map!=null)
			getPositionField().setPoint(m_map.getMovePoint());
	}
	
	private void centerAt() {
		// has map?
		if(m_map!=null) {						
			try {
				// get position 
				Position p = getPositionField().getPosition();
				// center at position?
				if(p!=null) {
					m_map.centerAtPosition(p);
				}
				else
					Utils.showWarning("Du må oppgi korrekte koordinater");
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * This method initializes m_tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane() {
		if (m_tabbedPane == null) {
			m_tabbedPane = new JTabbedPane();
			m_tabbedPane.addTab(null, DiskoIconFactory.getIcon("FORMAT.MGRS","32x32"), 
					getMGRSPanel(), Utils.getProperty("IconEnum.COORDINATE.text"));
			m_tabbedPane.addTab(null, DiskoIconFactory.getIcon("FORMAT.UTM","32x32"), 
					getUTMPanel(), Utils.getProperty("IconEnum.FORMAT.text"));
			m_tabbedPane.addTab(null, DiskoIconFactory.getIcon("FORMAT.DES","32x32"), 
					getDESPanel(), Utils.getProperty("IconEnum.FORMAT.text"));
			m_tabbedPane.addTab(null, DiskoIconFactory.getIcon("FORMAT.DEG","32x32"), 
					getDEGPanel(), Utils.getProperty("IconEnum.FORMAT.text"));
			m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			m_tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
			m_tabbedPane.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					// forward
					formatChanged(m_tabbedPane.getSelectedIndex()+1);
				}
				
			});
		}
		return m_tabbedPane;
	}
	
	public void formatChanged(int format) {
		// remove old
		switch(getPositionField().getFormat()) {
		case 1:
			getMGRSPanel().removeAll();
			break;
		case 2:			
			getUTMPanel().removeAll();
			break;
		case 3:			
			getDESPanel().removeAll();
			break;
		case 4:			
			getDEGPanel().removeAll();
			break;
		}
		// set new
		switch(format) {
		case 1:
			getMGRSPanel().add(getPositionField(),BorderLayout.WEST);
			getMGRSPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 2:			
			getUTMPanel().add(getPositionField(),BorderLayout.WEST);
			getUTMPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 3:			
			getDESPanel().add(getPositionField(),BorderLayout.WEST);
			getDESPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 4:			
			getDEGPanel().add(getPositionField(),BorderLayout.WEST);
			getDEGPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		}
		// forward
		getPositionField().setFormat(format);
	}
	
	private void registerMouseListener(boolean register) {
		try {
			if(m_map!=null) {
				if(register)
					m_map.addIMapControlEvents2Listener(m_mouseAdapter);
				else
					m_map.removeIMapControlEvents2Listener(m_mouseAdapter);							
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
