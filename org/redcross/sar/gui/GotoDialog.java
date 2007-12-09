/**
 * 
 */
package org.redcross.sar.gui;

import java.awt.Frame;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.POIFormatPanel.POIFormatEventListener;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.interop.AutomationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

/**
 * @author kennetgu
 *
 */
public class GotoDialog extends DiskoDialog  implements POIFormatEventListener {

	private static final long serialVersionUID = 1L;
	
	private JPanel m_contentPanel = null;
	private JPanel m_gotoPanel = null;

	private POIField m_poiField = null;
	private POIFormatPanel m_formatPanel = null;
	
	private JTabbedPane m_tabbedPane = null;
	private IDiskoMap map = null;
	
	private JButton m_gotoButton = null;
	

	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public GotoDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
	}

	private void initialize() {
		try {
			// prepare dialog
			Dimension dim = new Dimension(425, 85);
	        this.setMinimumSize(dim);
	        this.setPreferredSize(dim);
	        this.setMaximumSize(dim);
	        this.setContentPane(getContentPanel());
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onLoad(IDiskoMap map) throws IOException {
		this.map = map;
	}	
	
	/**
	 * This method initializes m_contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPanel() {
		if (m_contentPanel == null) {
			m_contentPanel = new JPanel();
			m_contentPanel.setLayout(new CardLayout());
			m_contentPanel.setBorder(BorderFactory.
					createBevelBorder(BevelBorder.RAISED));
			m_contentPanel.add(getTabbedPane(), BorderLayout.CENTER);
		}
		return m_contentPanel;
	}

	/**
	 * This method initializes m_gotoPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGotoPanel() {
		if (m_gotoPanel == null) {
			BorderLayout bl = new BorderLayout();
			m_gotoPanel = new JPanel();
			m_gotoPanel.setLayout(bl);
			m_gotoPanel.add(getPOIField(),BorderLayout.WEST);
			m_gotoPanel.add(getGotoButton(),BorderLayout.EAST);			
		}
		return m_gotoPanel;
	}
	
	/**
	 * This method initializes m_poiField	
	 * 	
	 * @return POIField	
	 */
	private POIField getPOIField() {
		if (m_poiField == null) {
			m_poiField = new POIField();
		}
		return m_poiField;
	}

	/**
	 * This method initializes m_poiField	
	 * 	
	 * @return POIField	
	 */
	private JButton getGotoButton() {
		if (m_gotoButton == null) {
			Dimension dim = Utils.getApp().getUIFactory().getSmallButtonSize();
			m_gotoButton = new JButton();
			m_gotoButton.setMinimumSize(dim);
			m_gotoButton.setPreferredSize(dim);
			m_gotoButton.setMaximumSize(dim);
			m_gotoButton.setIcon(Utils.getIcon("IconEnum.CENTERAT.icon"));
			m_gotoButton.setToolTipText(Utils.getProperty("IconEnum.CENTERAT.text"));			
			m_gotoButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					// has map?
					if(map!=null) {						
						try {
							// get position 
							Position p = getPOIField().getPosition();
							// center at position?
							if(p!=null) {
								map.centerAtPosition(p);
							}
							else
								Utils.showWarning("Du må oppgi korrekte koordinater");
						} catch (AutomationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
				
			});
		}
		return m_gotoButton;
	}
	
	/**
	 * This method initializes m_gotoPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private POIFormatPanel getFormatPanel() {
		if (m_formatPanel == null) {
			m_formatPanel = new POIFormatPanel();
			m_formatPanel.setPreferredSize(new Dimension(350,75));
			m_formatPanel.addPOIFormatEventListener(this);
		}
		return m_formatPanel;
	}
	
	/**
	 * This method initializes m_tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane() {
		if (m_tabbedPane == null) {
			m_tabbedPane = new JTabbedPane();
			m_tabbedPane.addTab(null, Utils.getIcon("IconEnum.COORDINATE.icon"), 
					getGotoPanel(), Utils.getProperty("IconEnum.COORDINATE.text"));
			m_tabbedPane.addTab(null, Utils.getIcon("IconEnum.FORMAT.icon"), 
					getFormatPanel(), Utils.getProperty("IconEnum.FORMAT.text"));
			m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			m_tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		}
		return m_tabbedPane;
	}
	
	public void formatChanged(int format) {
		// forward
		getPOIField().setFormat(format);
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
