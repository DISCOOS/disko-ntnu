/**
 * 
 */
package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.util.mso.Position;

/**
 * @author kennetgu
 *
 */
public class PositionSelectorDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;
	
	private boolean m_cancel = false;
	
	private GotoPanel m_gotoPanel = null;
	
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public PositionSelectorDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
		// add listener
		addDiskoWorkListener(new IDiskoWorkListener() {

			@Override
			public void onWorkPerformed(DiskoWorkEvent e) {
				if(e.isCancel()) m_cancel = true;				
			}
			
		});
		
	}

	private void initialize() {
		try {
			// prepare dialog
			Utils.setFixedSize(this, 340, 160);
	        this.setContentPane(getGotoPanel());
	        this.setModal(true);
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onLoad(IDiskoMap map) {
		getGotoPanel().setMap(map);
	}	
	
	/**
	 * This method initializes m_contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public GotoPanel getGotoPanel() {
		if (m_gotoPanel == null) {
			m_gotoPanel = new GotoPanel("Velg posisjon",true,true,true,true,ButtonSize.NORMAL);
		}
		return m_gotoPanel;
	}
	
	public void getClickPoint() {
		getGotoPanel().getClickPoint();
	}
	
	public void getMovePoint() {
		getGotoPanel().getMovePoint();
	}
	
	public Position select() {
		// reset
		m_cancel = false;
		// show position in map
		getGotoPanel().setPositionMarked(true);
		// show
		setVisible(true);
		// hide position in map
		getGotoPanel().setPositionMarked(false);
		// translate action
		if(m_cancel)
			return null;
		else
			return getGotoPanel().getCoordinatePanel().getPosition();
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
