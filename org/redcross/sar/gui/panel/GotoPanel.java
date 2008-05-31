package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMap.CoordinateFormat;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseDownEvent;
import com.esri.arcgis.interop.AutomationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author kennetgu
 *
 */
public class GotoPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;
	
	private JTabbedPane m_tabbedPane = null;
	private JPanel m_mgrsPanel = null;
	private JPanel m_utmPanel = null;
	private JPanel m_degPanel = null;
	private JPanel m_demPanel = null;
	private JPanel m_desPanel = null;
	private PositionFieldPanel m_positionField = null;
	private JButton m_gotoButton = null;
	
	private IDiskoMap m_map = null;
	
	private boolean m_isAutoUpdate = false;
	
	/**
	 * Constructor 
	 * 
	 */
	public GotoPanel() {
		// forward
		this("",true);		
	}

	/**
	 * Constructor 
	 * 
	 * @param caption
	 */
	public GotoPanel(String caption, boolean isAutoUpdate) {
		
		// forward
		super(caption,false,false);
		
		// prepare
		m_isAutoUpdate = isAutoUpdate;
		
		// initialize GUI
		initialize();
		
		// force caption
		setCaptionText(caption);
		
	}
	
	private void initialize() {
		try {
			this.setCaptionText("Gå til posisjon");
			this.setBodyComponent(getTabbedPane());
			this.setScrollBarPolicies(
					BasePanel.VERTICAL_SCROLLBAR_NEVER, 
					BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
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
			m_mgrsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
			m_utmPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
			m_degPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		return m_degPanel;
	}
	
	/**
	 * This method initializes m_demPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDEMPanel() {
		if (m_demPanel == null) {
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(5);
			m_demPanel = new JPanel();
			m_demPanel.setLayout(bl);
			m_demPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		return m_demPanel;
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
			m_desPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		return m_desPanel;
	}
	
	/**
	 * This method initializes m_positionField	
	 * 	
	 * @return getPositionField	
	 */
	public PositionFieldPanel getPositionField() {
		if (m_positionField == null) {
			try {
				m_positionField = new PositionFieldPanel();
				m_positionField.addChangeListener(new ChangeListener() {

					public void stateChanged(ChangeEvent e) {
						fireOnWorkChange(e.getSource(),null);
					}
					
				});
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_positionField;
	}

	/**
	 * This method initializes GotoButton	
	 * 	
	 * @return {@link JButton}
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
					m_map.flashPosition(p);
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
			m_tabbedPane.addTab(null, DiskoButtonFactory.getIcon(CoordinateFormat.FORMAT_MGRS,"32x32"), 
					getMGRSPanel(), DiskoButtonFactory.getTooltip(CoordinateFormat.FORMAT_MGRS));
			m_tabbedPane.addTab(null, DiskoButtonFactory.getIcon(CoordinateFormat.FORMAT_UTM,"32x32"), 
					getUTMPanel(), DiskoButtonFactory.getTooltip(CoordinateFormat.FORMAT_UTM));
			m_tabbedPane.addTab(null, DiskoButtonFactory.getIcon(CoordinateFormat.FORMAT_DES,"32x32"), 
					getDESPanel(), DiskoButtonFactory.getTooltip(CoordinateFormat.FORMAT_DES));
			m_tabbedPane.addTab(null, DiskoButtonFactory.getIcon(CoordinateFormat.FORMAT_DEM,"32x32"), 
					getDEMPanel(), DiskoButtonFactory.getTooltip(CoordinateFormat.FORMAT_DEM));
			m_tabbedPane.addTab(null, DiskoButtonFactory.getIcon(CoordinateFormat.FORMAT_DEG,"32x32"), 
					getDEGPanel(), DiskoButtonFactory.getTooltip(CoordinateFormat.FORMAT_DEG));
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
			getDEMPanel().removeAll();
			break;
		case 5:			
			getDEGPanel().removeAll();
			break;
		}
		// set new
		switch(format) {
		case 1:
			this.setCaptionText("Gå til posisjon (MGRS)");
			getMGRSPanel().add(getPositionField(),BorderLayout.WEST);
			getMGRSPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 2:			
			this.setCaptionText("Gå til posisjon (UTM)");
			getUTMPanel().add(getPositionField(),BorderLayout.WEST);
			getUTMPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 3:			
			this.setCaptionText("Gå til posisjon (desimal grader)");
			getDESPanel().add(getPositionField(),BorderLayout.WEST);
			getDESPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 4:			
			this.setCaptionText("Gå til posisjon (desimal minutter)");
			getDEMPanel().add(getPositionField(),BorderLayout.WEST);
			getDEMPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 5:			
			this.setCaptionText("Gå til posisjon (grad-minutt-sekund)");
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
	
	private final IMapControlEvents2Adapter m_mouseAdapter = new IMapControlEvents2Adapter() {

		private static final long serialVersionUID = 1L;

		@Override
		public void onMouseDown(IMapControlEvents2OnMouseDownEvent e) throws IOException, AutomationException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// update point?
					if (m_map != null) getClickPoint();
				}
			});			
		}
		
	};
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
