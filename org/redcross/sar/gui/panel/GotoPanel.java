package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMap.CoordinateFormat;
import org.redcross.sar.util.Utils;
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
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
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
public class GotoPanel extends TogglePanel {

	private static final long serialVersionUID = 1L;
	private static final double MARKER_SIZE = 12;

	private JTabbedPane m_tabbedPane;
	private JPanel m_mgrsPanel;
	private JPanel m_utmPanel;
	private JPanel m_degPanel;
	private JPanel m_demPanel;
	private JPanel m_desPanel;
	private CoordinatePanel m_coordinatePanel;
	private JButton m_gotoButton;

	private IPoint m_p = null;
	private IDiskoMap m_map = null;
	protected SimpleMarkerSymbol markerSymbol;

	private boolean m_isAutoUpdate;
	private boolean m_isPositionMarked = false;

	private String m_caption;

	/**
	 * Constructor
	 *
	 */
	public GotoPanel() {
		// forward
		this("Gå til posisjon",true);
	}

	/**
	 * Constructor
	 *
	 * @param caption
	 * @param isAutoUpdate Update coordinate from clicks in map
	 * @param isAutoCaption Update caption according to selected format
	 */
	public GotoPanel(String caption, boolean isAutoUpdate) {
		this(caption,isAutoUpdate,false,false,ButtonSize.SMALL);
	}

	/**
	 * Constructor
	 *
	 * @param caption
	 */
	public GotoPanel(String caption, boolean isAutoUpdate, boolean finish,
			boolean cancel, ButtonSize buttonSize) {
		this(caption,isAutoUpdate,finish,cancel,true,buttonSize);
	}

	/**
	 * Constructor
	 *
	 * @param caption
	 */
	public GotoPanel(String caption, boolean isAutoUpdate, boolean finish, boolean cancel, boolean toggle, ButtonSize buttonSize) {

		// forward
		super(caption,finish,cancel,toggle,buttonSize);

		// prepare
		m_isAutoUpdate = isAutoUpdate;

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

		// update caption
		setCaptionText(caption);

	}

	private void initialize() {
		try {
			this.setBodyComponent(getTabbedPane());
			this.setNotScrollBars();
			this.insertButton("finish", getGotoButton(), "goto");
			this.formatChanged(1);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setCaptionText(String caption) {
		setChangeable(false);
		m_caption = caption;
		formatChanged(getCoordinatePanel().getFormat());
		setChangeable(true);
	}

	public void setAutoUpdate(boolean isAutoUpdate) {
		// any change?
		if(m_isAutoUpdate!=isAutoUpdate) {
			// forward
			unregisterMouseListener();
			// save state
			m_isAutoUpdate = isAutoUpdate;
			// forward
			registerMouseListener();
		}
	}

	public boolean isAutoUpdate() {
		return m_isAutoUpdate;
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
			m_mgrsPanel = createTabPane();
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
			m_utmPanel = createTabPane();
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
			m_degPanel = createTabPane();
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
			m_demPanel = createTabPane();
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
			m_desPanel = createTabPane();
		}
		return m_desPanel;
	}

	private JPanel createTabPane() {
		JPanel pane = new JPanel(new BorderLayout(5,5));
		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pane.setOpaque(false);
		return pane;
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
				m_coordinatePanel.setOpaque(false);
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
			m_gotoButton = DiskoButtonFactory.createButton("MAP.CENTERAT",getButtonSize());
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
		setPoint();
	}

	public IDiskoMap getMap() {
		return m_map;
	}

	public void setMap(IDiskoMap map) {
		// forward
		unregisterMouseListener();
		// prepare
		this.m_map = map;
		// Enable goto button?
		getGotoButton().setEnabled(m_map!=null);
		// forward
		registerMouseListener();
	}

	public Position getPosition() {
		return getCoordinatePanel().getPosition();
	}

	public void setPosition(Position p) {
		getCoordinatePanel().setPosition(p);
	}

	public Point getPoint(ISpatialReference srs) {
		return getCoordinatePanel().getPoint(srs);
	}

	public Point getPoint() {
		try {
			if(m_map!=null)
				return getCoordinatePanel().getPoint(m_map.getSpatialReference());
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void setPoint(Point p) {
		getCoordinatePanel().setPoint(p);
	}

	public void setPoint() {
		if(m_map!=null) {
			if(m_isAutoUpdate)
				setClickPoint();
			else
				setCenterPoint();
		}
		else
			getCoordinatePanel().setText(null);
	}

	public void setCenterPoint() {
		if(m_map!=null)
			getCoordinatePanel().setPoint(m_map.getCenterPoint());
	}

	public void setClickPoint() {
		if(m_map!=null)
			getCoordinatePanel().setPoint(m_map.getClickPoint());
	}

	public void setMovePoint() {
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
					m_map.centerAtPosition(p.getGeoPos());
					m_map.flashPosition(p.getGeoPos());
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
					// suspend change event
					setChangeable(false);
					// forward
					formatChanged(m_tabbedPane.getSelectedIndex()+1);
					// resume change events
					setChangeable(true);
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
			caption = m_caption + " (MGRS)";
			getMGRSPanel().add(getCoordinatePanel(),BorderLayout.CENTER); // BorderLayout.WEST
			//getMGRSPanel().add(getGotoButton(),BorderLayout.EAST);
			break;
		case 2:
			caption = m_caption + " (UTM)";
			getUTMPanel().add(getCoordinatePanel(),BorderLayout.WEST);
			//getUTMPanel().add(getGotoButton(),BorderLayout.EAST);
			break;
		case 3:
			caption = m_caption + " (desimal grader)";
			getDESPanel().add(getCoordinatePanel(),BorderLayout.CENTER);
			//getDESPanel().add(getGotoButton(),BorderLayout.EAST);
			break;
		case 4:
			caption = m_caption + " (desimal minutter)";
			getDEMPanel().add(getCoordinatePanel(),BorderLayout.CENTER);
			//getDEMPanel().add(getGotoButton(),BorderLayout.EAST);
			break;
		case 5:
			caption = m_caption + " (grad minutt sekund)";
			getDEGPanel().add(getCoordinatePanel(),BorderLayout.CENTER);
			//getDEGPanel().add(getGotoButton(),BorderLayout.EAST);
			break;
		}
		// forward
		super.setCaptionText(caption);

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

	private void registerMouseListener() {
		try {
			if(m_map!=null && m_isAutoUpdate) {
				m_map.addIMapControlEvents2Listener(m_mouseAdapter);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void unregisterMouseListener() {
		try {
			if(m_map!=null && m_isAutoUpdate) {
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
					if (m_map != null) setClickPoint();
				}
			});
		}

	};

}  //  @jve:decl-index=0:visual-constraint="23,0"
