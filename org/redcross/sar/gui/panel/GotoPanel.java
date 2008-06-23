package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMap.CoordinateFormat;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.beans.map.MapBean;
import com.esri.arcgis.carto.IActiveView;
import com.esri.arcgis.carto.InvalidArea;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnAfterScreenDrawEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseDownEvent;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.display.RgbColor;
import com.esri.arcgis.display.SimpleMarkerSymbol;
import com.esri.arcgis.display.esriScreenCache;
import com.esri.arcgis.display.esriSimpleMarkerStyle;
import com.esri.arcgis.geometry.IPoint;
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
	private static final double MARKER_SIZE = 12;
	
	private JTabbedPane m_tabbedPane = null;
	private JPanel m_mgrsPanel = null;
	private JPanel m_utmPanel = null;
	private JPanel m_degPanel = null;
	private JPanel m_demPanel = null;
	private JPanel m_desPanel = null;
	private CoordinatePanel m_coordinatePanel = null;
	private JButton m_gotoButton = null;
	
	private IPoint m_p = null;
	private IDiskoMap m_map = null;
	protected SimpleMarkerSymbol markerSymbol = null;
	
	private boolean m_isAutoUpdate = false;
	private boolean m_isCaptionUpdate = true;
	private boolean m_isPositionMarked = false;
	
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
	 * @param isAutoUpdate Update coordinate from clicks in map
	 */
	public GotoPanel(String caption, boolean isAutoUpdate) {
		this(caption,isAutoUpdate,true,false,false,ButtonSize.NORMAL);
	}
		
	/**
	 * Constructor 
	 * 
	 * @param caption
	 * @param isAutoUpdate Update coordinate from clicks in map
	 * @param isAutoCaption Update caption according to selected format
	 */
	public GotoPanel(String caption, boolean isAutoUpdate, boolean isCaptionUpdate) {
		this(caption,isAutoUpdate,isCaptionUpdate,false,false,ButtonSize.NORMAL);
	}
	
	/**
	 * Constructor 
	 * 
	 * @param caption
	 */
	public GotoPanel(String caption, boolean isAutoUpdate, boolean isCaptionUpdate, boolean finish, boolean cancel, ButtonSize buttonSize) {

		// forward
		super(caption,finish,cancel,buttonSize);
		
		// prepare
		m_isAutoUpdate = isAutoUpdate;
		m_isCaptionUpdate = isCaptionUpdate;

		try {
			// create the symbol to draw with
			markerSymbol = new SimpleMarkerSymbol();
			RgbColor markerColor = new RgbColor();
			markerColor.setRed(255);
			markerColor.setGreen(128);
			markerSymbol.setColor(markerColor);
			markerSymbol.setStyle(esriSimpleMarkerStyle.esriSMSCross);
			markerSymbol.setSize(MARKER_SIZE);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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

	public void setCaptionUpdate(boolean isCaptionUpdate) {
		// save state
		m_isCaptionUpdate = isCaptionUpdate;
	}
	
	public boolean isCaptionUpdate() {
		return m_isCaptionUpdate;
	}
	
	public void setPositionMarked(boolean isPositionMarked) {
		// save state
		m_isPositionMarked = isPositionMarked;
		// update
		refresh(true);
	}
	
	public boolean isPositionMarked() {
		return m_isPositionMarked;
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
	public CoordinatePanel getCoordinatePanel() {
		if (m_coordinatePanel == null) {
			try {
				m_coordinatePanel = new CoordinatePanel();
				m_coordinatePanel.addChangeListener(new ChangeListener() {

					public void stateChanged(ChangeEvent e) {
						if(!isChangeable()) return;
						refresh(false);
						fireOnWorkChange(e.getSource(),null);
					}
					
				});
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_coordinatePanel;
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
			getCoordinatePanel().setText(null);		
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
			getCoordinatePanel().setPoint(m_map.getClickPoint());
	}
	
	public void getMovePoint() {
		if(m_map!=null)
			getCoordinatePanel().setPoint(m_map.getMovePoint());
	}
	
	private void centerAt() {
		// has map?
		if(m_map!=null) {						
			try {
				// get position 
				Position p = getCoordinatePanel().getPosition();
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
		switch(getCoordinatePanel().getFormat()) {
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
		// initialize
		String caption = null;
		// set new
		switch(format) {
		case 1:
			caption = "Gå til posisjon (MGRS)";
			getMGRSPanel().add(getCoordinatePanel(),BorderLayout.WEST);
			getMGRSPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 2:			
			caption = "Gå til posisjon (UTM)";
			getUTMPanel().add(getCoordinatePanel(),BorderLayout.WEST);
			getUTMPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 3:			
			caption = "Gå til posisjon (desimal grader)";
			getDESPanel().add(getCoordinatePanel(),BorderLayout.WEST);
			getDESPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 4:			
			caption = "Gå til posisjon (desimal minutter)";
			getDEMPanel().add(getCoordinatePanel(),BorderLayout.WEST);
			getDEMPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		case 5:			
			caption = "Gå til posisjon (grad-minutt-sekund)";
			getDEGPanel().add(getCoordinatePanel(),BorderLayout.WEST);
			getDEGPanel().add(getGotoButton(),BorderLayout.EAST);			
			break;
		}
		if(m_isCaptionUpdate)
			this.setCaptionText("Gå til posisjon (grad-minutt-sekund)");
			
		// forward
		getCoordinatePanel().setFormat(format);
	}
	
	/**
	 * Draws the point on screen
	 * 
	 */
	protected void refresh(boolean cleanup) {
		
		// consume?
		if(!m_isPositionMarked) return;
		
		try {
			// has map?
			if(m_map instanceof MapBean) {
			
				// get point
				IPoint p = getCoordinatePanel().getPoint(m_map.getSpatialReference());
				
				// draw in screen display
				if (p != null && !p.isEmpty()) {
					// get active view
					IActiveView activeView = ((MapBean)m_map).getActiveView();
					// get invalid area
					InvalidArea invalidArea = new InvalidArea();
					// add current?
					if(!cleanup)
						invalidArea.add(p);
					if(m_p!=null)
						invalidArea.add(m_p);
					// invalidate
					invalidArea.setDisplayByRef(activeView.getScreenDisplay());
					invalidArea.invalidate((short) esriScreenCache.esriNoScreenCache);
				}
				// update point
				m_p = p;
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void draw() throws IOException, AutomationException {
		
		// consume?
		if(!m_isPositionMarked) return;
		
		// has map?
		if(m_map instanceof MapBean) {
		
			// get point
			IPoint p = getCoordinatePanel().getPoint(m_map.getSpatialReference());
			
			// draw in screen display
			if (p != null && !p.isEmpty()) {

				// get active view
				IActiveView activeView = ((MapBean)m_map).getActiveView();
				
				// get screen display and start drawing on it
				IScreenDisplay screenDisplay = activeView.getScreenDisplay();
				screenDisplay.startDrawing(screenDisplay.getHDC(),(short) esriScreenCache.esriNoScreenCache);
	
				screenDisplay.setSymbol(markerSymbol);
				screenDisplay.drawPoint(p);
				
				// notify that drawing is finished
				screenDisplay.finishDrawing();
				
			}
		}
		
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
		public void onAfterScreenDraw(IMapControlEvents2OnAfterScreenDrawEvent e)
				throws IOException, AutomationException {
			// forward?
			draw();
		}
		
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
