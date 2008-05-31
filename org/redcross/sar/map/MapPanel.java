package org.redcross.sar.map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.panel.AbstractPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.IPanelManager;
import org.redcross.sar.gui.panel.MapFilterPanel;
import org.redcross.sar.gui.panel.MapStatusPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 * This panel wraps IDiskoMap
 * 
 * @author kennetgu
 *
 */
public class MapPanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;
	
	// components
	private IDiskoMap map;
	private JPanel northBar;
	private JPanel southBar;
	private MapStatusPanel mapStatusBar;
	private MapFilterPanel mapFilterBar;
	
	/**
	 * Default constructor
	 */
	public MapPanel(final IDiskoMap map) {

		// prepare
		this.map = map;
		this.setMsoLayers(map, map.getSupportedMsoLayers());
		
		// initialize GUI
		initialize();
		
		// listen for changes
		map.addDiskoMapListener(new IDiskoMapListener() {

			@Override
			public void onExtentChanged() {
				getMapStatusBar().setScale(map.getScale());				
			}

			@Override
			public void onMapReplaced() {
				getMapStatusBar().setScale(map.getScale());				
			}

			@Override
			public void onMouseClick() {
				getMapStatusBar().onMouseClick(map.getClickPoint());				
			}

			@Override
			public void onMouseMove() {
				getMapStatusBar().onMouseMove(map.getMovePoint());				
			}
			
		});
		
	}

	private void initialize() {
		
		BorderLayout bl = new BorderLayout();
		bl.setVgap(0);
		bl.setHgap(0);
		
		// set layout
		setLayout(bl);
		
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
	public void addDiskoWorkListener(IDiskoWorkListener listener) {
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
	protected void fireOnWorkPerformed(DiskoWorkEvent e) {
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
	public void removeDiskoWorkListener(IDiskoWorkListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setManager(IPanelManager manager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}	
	
}
