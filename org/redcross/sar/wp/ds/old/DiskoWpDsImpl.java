package org.redcross.sar.wp.ds.old;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.map.command.IDiskoCommand.DiskoCommandType;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.wp.AbstractDiskoWpModule;

/**
 * Implements the DiskoWpStates interface
 * 
 * @author kengu
 * 
 */
public class DiskoWpDsImpl extends AbstractDiskoWpModule 
		implements IDiskoWpDs {

    private RouteCostPanel m_routeCost;
    
	/**
	 * Constructs a DiskoWpDsImpl
	 * 
	 */
	public DiskoWpDsImpl() throws IllegalClassFormatException {
		// forward
		super();
		// initialize gui
	    initialize();
	}

	private void initialize() {
		installMap();
        m_routeCost = new RouteCostPanel(this);
        layoutComponent(m_routeCost);
	}

	public void activate(IDiskoRole role) {
		
		// forward
		super.activate(role);

		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get tool set 
	        List<Enum<?>> myButtons = new ArrayList<Enum<?>>();	  
	        myButtons.add(DiskoToolType.ZOOM_IN_TOOL);
	        myButtons.add(DiskoToolType.ZOOM_OUT_TOOL);
	        myButtons.add(DiskoToolType.PAN_TOOL);
	        myButtons.add(DiskoCommandType.ZOOM_FULL_EXTENT_COMMAND);
	        myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myButtons.add(DiskoCommandType.MAP_TOGGLE_COMMAND);
	        myButtons.add(DiskoCommandType.SCALE_COMMAND);
	        myButtons.add(DiskoCommandType.TOC_COMMAND);
	        myButtons.add(DiskoToolType.SELECT_FEATURE_TOOL);
			// forward
			setupNavBar(myButtons,true);
		}				
	}
	
	public void deactivate() {
		super.deactivate();
		NavBarPanel navBar = getApplication().getNavBar();
		navBar.hideDialogs();
	}
	
	public boolean rollback() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean commit() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getCaption() {
		return "Beslutningsst�tte";
	}
	
}