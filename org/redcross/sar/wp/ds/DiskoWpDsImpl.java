package org.redcross.sar.wp.ds;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.map.command.IDiskoCommand.DiskoCommandType;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
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
	 * @param rolle
	 *            A reference to the DiskoRolle
	 */
	public DiskoWpDsImpl(IDiskoRole rolle) throws IllegalClassFormatException {
		super(rolle);
		// initialize gui
	    initialize();
	}

	private void initialize() {
        m_routeCost = new RouteCostPanel(this);
        layoutComponent(m_routeCost);
	}

	public void activated() {
		super.activated();

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
	
	public void deactivated() {
		super.deactivated();
		NavBar navBar = getApplication().getNavBar();
		navBar.hideDialogs();
	}
	
	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean finish() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getCaption() {
		return "Beslutningsstøtte";
	}
	
}
