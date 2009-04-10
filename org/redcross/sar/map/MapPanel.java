package org.redcross.sar.map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.panel.MapFilterPanel;
import org.redcross.sar.gui.panel.MapStatusPanel;
import org.redcross.sar.map.event.DiskoMapEvent;
import org.redcross.sar.map.event.IDiskoMapListener;

import com.esri.arcgis.interop.AutomationException;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.io.IOException;

/**
 * This panel wraps IDiskoMap
 *
 * @author kennetgu
 *
 */
public class MapPanel extends JPanel{

	private static final long serialVersionUID = 1L;

	private int m_isMarked = 0;

	// components
	private IDiskoMap map;
	private JPanel northBar;
	private JPanel southBar;

	//private EnumSet<LayerCode> msoLayers;

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

		/*
		this.msoLayers = map.getSupportedLayers();

		// register?
		if(msoLayers!=null) {
			// loop over all layers
			for(LayerCode it: msoLayers) {
				IMsoFeatureLayer l = map.getMsoLayer(it);
				if(l!=null) l.addMsoLayerEventListener(listener);
			}
		}
		*/


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

	/* ===========================================
	 * IChangeable implementation
	 * =========================================== */

	public int isMarked() {
		return m_isMarked;
	}

	public void setMarked(int isMarked) {
		m_isMarked = isMarked;
	}

	/*

	IMsoLayerEventListener listener = new IMsoLayerEventListener() {

		@Override
		public void onSelectionChanged(MsoLayerEvent e) throws IOException,
				AutomationException {
			// TODO Auto-generated method stub

		}

	};
	*/

}
