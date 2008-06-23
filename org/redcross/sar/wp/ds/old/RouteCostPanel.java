/**
 * 
 */
package org.redcross.sar.wp.ds.old;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;

import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.panel.MapStatusPanel;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.AreaLayer;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.mso.data.RouteImpl;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.AbstractDiskoWpModule;
import org.redcross.sar.wp.tactics.AssignmentTable;

import com.esri.arcgis.carto.FeatureLayer;
import com.esri.arcgis.interop.AutomationException;
import java.awt.GridLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This class implements the administrator user interface for route cost estimation
 * 
 * @author kennetgu
 *
 */
public class RouteCostPanel extends JPanel 
		implements IMsoLayerEventListener {

	// Logger for this object
	private static final Logger LOGGER = Logger.getLogger(
			"org.redcross.sar.wp.ds.RouteCostPanel");
	
	private static final long serialVersionUID = 1L;

	private AbstractDiskoWpModule m_wp;
	private IMsoObjectIf m_currentMsoObj = null;  //  @jve:decl-index=0:
	private AreaLayer m_plannedAreaLayer = null;
	
	private JProgressBar m_progressBar = null;
	private JSplitPane m_splitPane = null;
	private JPanel m_topPanel = null;
	private JPanel m_bottomPanel = null;
	private JButton m_runButton = null;
	private JPanel m_routeInfoPane = null;
	private JLabel m_routePointCountLabel = null;
	private JLabel m_routeNameLabel = null;
	private JLabel m_routeLengthLabel = null;
	private JLabel m_routeEteLabel = null;
	private JTextField m_routeNameText = null;
	private JTextField m_routePointCountText = null;
	private JTextField m_routeLengthText = null;
	private JTextField m_routeEteText = null;
	
	private DiskoMap m_map = null;  //  @jve:decl-index=0:
	
	private RouteCost m_rce = null;

	private JLabel m_routeAverageSpeedLabel = null;

	private JTextField m_routeAverageSpeedText = null;

	private JTextField m_routeEtaText = null;

	private JLabel m_routeEtaLabel = null;
	

	public RouteCostPanel(AbstractDiskoWpModule wp) {
		m_wp = wp;
		m_plannedAreaLayer = (AreaLayer) getMap().getMsoLayer(IMsoFeatureLayer.LayerCode.AREA_LAYER);
		//listeners
		m_plannedAreaLayer.addMsoLayerEventListener(this);
		initialize();
	}

	/**
	 * This method initializes the dialog content
	 * 
	 */
	private void initialize() {
		try {
	        setPreferredSize(new Dimension(593, 600));
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			add(getProgressBar(), BorderLayout.SOUTH);
			add(getSplitPane(), BorderLayout.NORTH);
			// create tools
			//m_menu = new ToolbarMenu();
			//m_freeHand = new ControlsNewFreeHandTool();
			//m_freeHand.onCreate(m_map);
			//m_menu.addItem(m_freeHand, 0, -1, false, 2);
			//m_map.setCurrentToolByRef(m_freeHand);
			//FeatureLayer layer = new FeatureLayer();
			//layer.setName("freehand");
			//layer.setSpatialReferenceByRef(m_map.getSpatialReference());
			//m_map.getActiveView().getFocusMap().addLayer(layer);
			//layer.setCached(true);
		} catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes ProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getProgressBar() {
		if (m_progressBar == null) {
			m_progressBar = new JProgressBar();
			m_progressBar.setVisible(false);
		}
		return m_progressBar;
	}

	/**
	 * This method initializes SplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getSplitPane() {
		if (m_splitPane == null) {
			m_splitPane = new JSplitPane();
			m_splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			m_splitPane.setBottomComponent(getBottomPanel());
			m_splitPane.setTopComponent(getTopPanel());
		}
		return m_splitPane;
	}

	/**
	 * This method initializes topPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getTopPanel() {
		if (m_topPanel == null) {
			m_topPanel = new JPanel();
			m_topPanel.setLayout(new BorderLayout());
			m_topPanel.setPreferredSize(new Dimension(150, 350));
			MapPanel panel = new MapPanel((DiskoMap)getMap());
			panel.setNorthBarVisible(true);
			panel.setSouthBarVisible(true);
			m_topPanel.add(panel);
			
		}
		return m_topPanel;
	}

	/**
	 * This method initializes bottomPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getBottomPanel() {
		if (m_bottomPanel == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setColumns(2);
			m_bottomPanel = new JPanel();
			m_bottomPanel.setLayout(gridLayout);
			m_bottomPanel.setPreferredSize(new Dimension(150, 210));
			m_bottomPanel.add(getInfoPane(), null);
			m_bottomPanel.add(getRunButton(), null);
		}
		return m_bottomPanel;
	}

	/**
	 * This method initializes the map	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private IDiskoMap getMap() {
		if (m_map == null) {
			m_map = (DiskoMap)m_wp.getMap();
			((JComponent)m_map).setPreferredSize(new Dimension(350, 350));
		}
		return m_map;
	}
	
	/**
	 * This method calculates the estimate of selected route
	 */
	private void runEstimate() {

		// get route from mso object?
		if(m_currentMsoObj instanceof IRouteIf){
			
			// initialize
			double cost = 0;
			
			
			// create new route
	    	/*/Route r = new Route("1","Test",100);
	    	for(int i = 0;i<100000;i++){
	        	// add positions
	        	r.add(i, i);        		
	    	}*/
	    	
	    	// get new time instance
	    	Calendar t = Calendar.getInstance();
	    	
	    	// get first tic
	    	long tic1 = t.getTimeInMillis();

	    	
    		// get new route
	    	//Route r = (Route)((IAreaIf)m_currentMsoObj).getGeodata().getPositions().get(0);
	    	Route r = ((IRouteIf)m_currentMsoObj).getGeodata();

	    	// create estimator?
	    	if (m_rce == null) {
	    		m_rce = new RouteCost(r,2,m_map);
	    	}
	    	else {
	    		m_rce.setRoute(r);
	    	}
	    	
	    	try {
		    	// run the route cost estimator n times
		    	for(int i = 0; i < 1 ;i++) {
			    	// calculate
		    		cost += m_rce.estimate();    		
		    	}
	    	}
	    	catch (Exception e) {
	    		e.printStackTrace();
	    	}

	    	// get new route
	    	r = m_rce.getRoute();
	    	
			/*((IAreaIf)area).addAreaGeodata(route);
			msoObj = area;*/	
	    	((IRouteIf)m_currentMsoObj).setGeodata(r);
	    	
	    	// get calendar
	    	t = Calendar.getInstance();
	    	
	    	// get last tic
	    	long tic2 = t.getTimeInMillis();
	    	
	    	// logg result
	    	LOGGER.info("ete:=" + (int)cost + " sec calulcated in " + (tic2 - tic1) + "ms");
			
	    	NumberFormat nf = new DecimalFormat("0.00");
	    	
	    	double kph = m_rce.getDistance() / m_rce.ete() * 3.6;
	    	
	    	double km = Math.floor(m_rce.getDistance() / (1000));
	    	double m = Math.round(m_rce.getDistance() - (km * 1000));

	    	double tt = Math.floor(cost / (60 * 60));
	    	double mm = Math.round((cost / 60) - (tt * 60));
	    	
	    	// show result
	    	getRoutePointCountText().setText((new Integer(r.getPositions().size())).toString());	    	
	    	getRouteLengthText().setText((new Double(km).intValue()) + " km " + (new Double(m).intValue()) + " m");
	    	getRouteAverageSpeedText().setText(nf.format(kph) + " km/h ");
	    	getRouteEteText().setText((new Double(tt).intValue()) + " h " + (new Double(mm).intValue()) + " min");
	    	getRouteEtaText().setText(m_rce.eta().getTime().toLocaleString());
	    	
		}
	}
	/**
	 * This method initializes runButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRunButton() {
		if (m_runButton == null) {
			m_runButton = new JButton("Calculate");
			m_runButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// forward...
					runEstimate();
				}
			});
		}
		return m_runButton;
	}	

	public void onSelectionChanged(MsoLayerEvent e)
	throws IOException, AutomationException {
		try {
			List<IMsoObjectIf> selection = e.getSelectedMsoObjects();
			if (selection != null && selection.size() > 0) {
				m_currentMsoObj = selection.get(0);
				if(m_currentMsoObj instanceof IRouteIf){
					IAreaIf area = MsoUtils.getOwningArea(m_currentMsoObj);
					if(area!=null) {
						if(area.getOwningAssignment() instanceof ISearchIf){
							getRouteNameText().setText(area.getOwningAssignment().getTypeAndNumber());						
						}
						else {
							getRouteNameText().setText(area.getObjectId());
						}
						getRoutePointCountText().setText("<Unknown>");
						getRouteLengthText().setText("<Unknown>");
						getRouteEteText().setText("<Unknown>");
					}
					else {
						getRouteNameText().setText("<Object has no route>");
						getRoutePointCountText().setText("<Not calculated>");
						getRouteLengthText().setText("<Not calculated>");
						getRouteEteText().setText("<Not calculated>");						
					}
				}
				else {
					getRouteNameText().setText("<Object is not a route>");
					getRoutePointCountText().setText("<Not calculated>");
					getRouteLengthText().setText("<Not calculated>");
					getRouteEteText().setText("<Not calculated>");
				}
	}
			else {
				m_currentMsoObj = null;
				getRouteNameText().setText("<Nothing is selected>");
				getRoutePointCountText().setText("<Not calculated>");
				getRouteLengthText().setText("<Not calculated>");
				getRouteEteText().setText("<Not calculated>");
			}
		} catch (RuntimeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * This method initializes InfoPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getInfoPane() {
		if (m_routeInfoPane == null) {
			m_routeEtaLabel = new JLabel("Estimated Time of Arrival");
			m_routeAverageSpeedLabel = new JLabel("Average Speed");
			m_routeNameLabel = new JLabel("Route name");
			m_routeLengthLabel = new JLabel("Route length");
			m_routePointCountLabel = new JLabel("Point Count");
			m_routeEteLabel = new JLabel("Estimated Time Enroute");
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(6);
			gridLayout.setColumns(2);
			gridLayout.setHgap(0);
			gridLayout.setVgap(6);
			m_routeInfoPane = new JPanel();
			m_routeInfoPane.setLayout(gridLayout);
			m_routeInfoPane.add(m_routeNameLabel, null);
			m_routeInfoPane.add(getRouteNameText(), null);
			m_routeInfoPane.add(m_routePointCountLabel, null);
			m_routeInfoPane.add(getRoutePointCountText(), null);
			m_routeInfoPane.add(m_routeLengthLabel, null);
			m_routeInfoPane.add(getRouteLengthText(), null);
			m_routeInfoPane.add(m_routeAverageSpeedLabel, null);
			m_routeInfoPane.add(getRouteAverageSpeedText(), null);
			m_routeInfoPane.add(m_routeEteLabel, null);
			m_routeInfoPane.add(getRouteEteText(), null);
			m_routeInfoPane.add(m_routeEtaLabel, null);
			m_routeInfoPane.add(getRouteEtaText(), null);
		}
		return m_routeInfoPane;
	}

	/**
	 * This method initializes routeNameText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRouteNameText() {
		if (m_routeNameText == null) {
			m_routeNameText = new JTextField();
			m_routeNameText.setEditable(false);
		}
		return m_routeNameText;
	}

	/**
	 * This method initializes pointCountText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRoutePointCountText() {
		if (m_routePointCountText == null) {
			m_routePointCountText = new JTextField();
			m_routePointCountText.setEditable(false);
		}
		return m_routePointCountText;
	}

	/**
	 * This method initializes routeLengthText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRouteLengthText() {
		if (m_routeLengthText == null) {
			m_routeLengthText = new JTextField();
			m_routeLengthText.setEditable(false);
		}
		return m_routeLengthText;
	}

	/**
	 * This method initializes routeEteText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRouteEteText() {
		if (m_routeEteText == null) {
			m_routeEteText = new JTextField();
			m_routeEteText.setEditable(false);
		}
		return m_routeEteText;
	}

	/**
	 * This method initializes m_routeAverageSpeedText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRouteAverageSpeedText() {
		if (m_routeAverageSpeedText == null) {
			m_routeAverageSpeedText = new JTextField();
			m_routeAverageSpeedText.setEditable(false);
		}
		return m_routeAverageSpeedText;
	}

	/**
	 * This method initializes m_routeEtaText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRouteEtaText() {
		if (m_routeEtaText == null) {
			m_routeEtaText = new JTextField();
			m_routeEtaText.setEditable(false);
		}
		return m_routeEtaText;
	}
}

