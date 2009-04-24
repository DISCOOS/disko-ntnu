/**
 * 
 */
package org.redcross.sar.gui.dialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.XYPlotPanel;
import org.redcross.sar.gui.table.TrackTable;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

import com.esri.arcgis.geometry.Polyline;

/**
 * @author kennetgu
 *
 */
public class TrackDialog extends DefaultDialog  {

	private static final long serialVersionUID = 1L;
	
	private DefaultPanel m_contentPanel;
	private BasePanel m_pointsPanel;
	private TrackTable m_trackTable;
	private XYPlotPanel m_profilePanel;
	
	private IDiskoMap m_map;
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public TrackDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
	}

	private void initialize() {
		try {
			// prepare dialog
	        this.setContentPane(getContentPanel());
	        this.showProfile(false);
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes m_trackPanel	
	 * 	
	 * @return BasePanel
	 */
	public DefaultPanel getContentPanel() {
		if (m_contentPanel == null) {
			// create panels
			m_contentPanel = new DefaultPanel("",false,true,ButtonSize.SMALL);
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "32x32"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");
			m_contentPanel.setNotScrollBars();
			AbstractButton b = m_contentPanel.insertButton("finish", 
					DiskoButtonFactory.createToggleButton("GENERAL.CHART", ButtonSize.SMALL), "profile");
			b.setToolTipText("Profile");
			m_contentPanel.insertButton("finish", 
					DiskoButtonFactory.createButton("MAP.CENTERAT", ButtonSize.SMALL), "centerat");
			JComponent c = (JComponent)m_contentPanel.getContainer();
			c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));
			c.add(getPointsPanel());
			c.add(Box.createVerticalStrut(5));
			c.add(getProfilePanel());
			m_contentPanel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("profile".equalsIgnoreCase(cmd)) {
						JToggleButton b = (JToggleButton)e.getSource();
						showProfile(b.isSelected());
					}
					else if("centerat".equalsIgnoreCase(cmd)) {
						if(m_map!=null) {
							int[] idx = getTrackTable().getSelectedRows();
							if(idx.length>0) {
								Track track = getTrackTable().getTrack();
								if(idx[0]!=-1 && idx[0] < track.size()) {		
									// adjust index?
									if(idx[0] == track.size()-1) idx[0]--;
									// initialize polyline
									Polyline line = null;
									// create polyline from selection 
									try {
										// single selection?
										if(idx.length==1) {
											TimePos p1 = track.get(idx[0]); 
											TimePos p2 = track.get(idx[0]+1);
											line = MapUtil.getEsriPolyline(p1,p2,m_map.getSpatialReference());
											m_map.zoomTo(line.getEnvelope(),2.0);
											m_map.flash(line.getEnvelope());
										}
										else {
											int count = idx.length;
											List<GeoPos> points = new ArrayList<GeoPos>(count);
											for(int i=0; i<count; i++) {
												points.add(track.get(idx[i]));
											}
											points.add(track.get(idx[count-1]+1));
											line = MapUtil.getEsriPolyline(points,m_map.getSpatialReference());;
											m_map.zoomTo(line.getEnvelope(),2.0);
											m_map.flash(line);
										}
									} catch (UnknownHostException ex) {
										// TODO Auto-generated catch block
										ex.printStackTrace();
									} catch (IOException ex) {
										// TODO Auto-generated catch block
										ex.printStackTrace();
									}
									
								}
							}
						}
					}
					
				}
				
			});
		}
		return m_contentPanel;
	}
	
	/**
	 * This method initializes m_xyPlotPanel
	 * 	
	 * @return XYPlotPanel
	 */
	public BasePanel getPointsPanel() {
		if (m_pointsPanel == null) {
			m_pointsPanel = new BasePanel("Punkter");
			m_pointsPanel.setContainer(getTrackTable());
			Utils.setFixedSize(m_pointsPanel, 490, 250);
		}
		return m_pointsPanel;
	}
	
	/**
	 * This method initializes m_trackTable
	 * 	
	 * @return TrackPanel
	 */
	public TrackTable getTrackTable() {
		if (m_trackTable == null) {
			m_trackTable = new TrackTable();
		}
		return m_trackTable;
	}
	
	
	/**
	 * This method initializes m_profilePanel
	 * 	
	 * @return XYPlotPanel
	 */
	public XYPlotPanel getProfilePanel() {
		if (m_profilePanel == null) {
			m_profilePanel = new XYPlotPanel("Høydeprofil");
			m_profilePanel.setNotScrollBars();
			Utils.setFixedSize(m_profilePanel, 490, 200);
		}
		return m_profilePanel;
	}
	
	public void load(IDiskoMap map, IAssignmentIf assignment, Track track) {
		m_map = map;
		getTrackTable().load(track);
		getContentPanel().setButtonEnabled("centerat",m_map!=null);
		getContentPanel().setButtonEnabled("profile",loadProfile(track));
		// update icon
		if(assignment!=null) {
			Enum<?> e = MsoUtils.getType(assignment,true);
			getContentPanel().setCaptionIcon(
					DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"32x32"));
			getContentPanel().setCaptionText("Estimert rute for <b>" + 
					MsoUtils.getAssignmentName(assignment, 1).toLowerCase() + "</b>");
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "32x32"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");			
		}		
		
	}
	
	private boolean loadProfile(Track track) {
		int count = track.size();
		if(count>0) {
			double[] x = new double[count];
			double[] y = new double[count];
			for(int i=0;i<count;i++) {
				x[i] = i;
				y[i] = track.get(i).getAltitude();
			}
			getProfilePanel().plot("", "Punkter", "Høyde", x, y);
			return true;
		}
		return false;
	}
	
	private void showProfile(boolean isVisible) {
		getProfilePanel().setVisible(isVisible);
		if(isVisible) {
			requestResize(500, 520, false);
		}
		else {
			requestResize(500, 315, false);
		}

	}
	
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
