package org.redcross.sar.map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.panel.AbstractPanel;
import org.redcross.sar.gui.panel.IPanelManager;
import org.redcross.sar.gui.panel.MapFilterPanel;
import org.redcross.sar.gui.panel.MapStatusPanel;
import org.redcross.sar.map.event.DiskoMapEvent;
import org.redcross.sar.map.event.IDiskoMapListener;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

import com.esri.arcgis.interop.AutomationException;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * This panel wraps IDiskoMap
 *
 * @author kennetgu
 *
 */
public class MapPanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;

	private int m_isMarked = 0;

	// components
	private IDiskoMap map;
	private JPanel northBar;
	private JPanel southBar;
	private MapStatusPanel mapStatusBar;
	private MapFilterPanel mapFilterBar;

	/* ===============================================================
	 * Constructors
	 * ===============================================================*/

	public MapPanel(IDiskoMap map) {
		this(map,false);
	}

	public MapPanel(IDiskoMap map, boolean showBorder) {

		// forward
		super();

		// prepare
		this.map = map;
		this.setMsoLayers(map, map.getSupportedLayers());

		// initialize GUI
		initialize(showBorder);

		// listen for changes
		map.addDiskoMapListener(new IDiskoMapListener() {

			@Override
			public void onExtentChanged(DiskoMapEvent e) {
				getMapStatusBar().setScale(e.getSource().getScale());
			}

			@Override
			public void onMapReplaced(DiskoMapEvent e) {
				getMapStatusBar().setScale(e.getSource().getScale());
			}

			@Override
			public void onMouseClick(DiskoMapEvent e) {
				getMapStatusBar().onMouseClick(e.getSource().getClickPoint());
			}

			@Override
			public void onMouseMove(DiskoMapEvent e) {
				getMapStatusBar().onMouseMove(e.getSource().getMovePoint());
			}

			@Override
			public void onSelectionChanged(DiskoMapEvent e) {
				try {
					getMapStatusBar().onSelectionChanged(e.getSource().getMsoSelection());
				} catch (AutomationException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}

		});

	}

	private void initialize(boolean showBorder) {

		// set layout
		setLayout(new BorderLayout(0,0));
		if(showBorder) setBorder(new DiskoBorder());

		// add components
		add(getNorthBar(),BorderLayout.NORTH);
		if(map instanceof JComponent) {
			add((JComponent)map,BorderLayout.CENTER);
		}
		add(getSouthBar(),BorderLayout.SOUTH);

	}

	public IDiskoMap getMap() {
		return map;
	}

	private JPanel getNorthBar() {
		if(northBar==null) {
			northBar = new JPanel();
			northBar.setLayout(new CardLayout());
			northBar.add(getMapStatusBar(),"MapStatusBar");
			northBar.setBorder(null);
		}
		return northBar;
	}

	public MapStatusPanel getMapStatusBar() {
		if(mapStatusBar==null) {
			mapStatusBar = new MapStatusPanel();

		}
		return mapStatusBar;
	}

	private JPanel getSouthBar() {
		if(southBar==null) {
			southBar = new JPanel();
			southBar.setLayout(new CardLayout());
			southBar.add(getMapFilterBar(),"MapFilterBar");
			//southBar.add(getProgressor().getProgressBar(),"ProgressBar");
			showBar(southBar,"MapFilterBar");
			southBar.setBorder(null);
		}
		return southBar;
	}

	private void showBar(JPanel panel, String key) {
		((CardLayout)panel.getLayout()).show(panel, key);
	}

	public MapFilterPanel getMapFilterBar() {
		if(mapFilterBar==null) {
			mapFilterBar = new MapFilterPanel();
			mapFilterBar.setMap(map);
		}
		return mapFilterBar;
	}

	public boolean isNorthBarVisible() {
		return getNorthBar().isVisible();
	}

	public boolean isSouthBarVisible() {
		return getSouthBar().isVisible();
	}

	public void setNorthBarVisible(boolean isVisible) {
		getNorthBar().setVisible(isVisible);

	}

	public void setSouthBarVisible(boolean isVisible) {
		getSouthBar().setVisible(isVisible);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addActionListener(ActionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addWorkFlowListener(IWorkFlowListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean doAction(String command) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void fireActionEvent(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireOnWorkCancel(Object source, Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireOnWorkChange(Object source, Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireOnWorkFinish(Object source, Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireOnWorkPerformed(WorkFlowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public IPanelManager getManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeActionListener(ActionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeWorkFlowListener(IWorkFlowListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setManager(IPanelManager manager, boolean isMainPanel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	/* ===========================================
	 * IChangeable implementation
	 * =========================================== */

	public int isMarked() {
		return m_isMarked;
	}

	public void setMarked(int isMarked) {
		m_isMarked = isMarked;
	}

}
