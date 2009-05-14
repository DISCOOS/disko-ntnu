package org.redcross.sar.wp.ds.old;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.redcross.sar.IDiskoRole;
import org.redcross.sar.gui.menu.NavMenu;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
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
		super(Logger.getLogger(DiskoWpDsImpl.class));
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
		if(isNavMenuSetupNeeded()) {
			// get tool set 
	        List<Enum<?>> myButtons = new ArrayList<Enum<?>>();	  
	        myButtons.add(MapToolType.ZOOM_IN_TOOL);
	        myButtons.add(MapToolType.ZOOM_OUT_TOOL);
	        myButtons.add(MapToolType.PAN_TOOL);
	        myButtons.add(MapCommandType.ZOOM_FULL_EXTENT_COMMAND);
	        myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myButtons.add(MapCommandType.MAP_TOGGLE_COMMAND);
	        myButtons.add(MapCommandType.SCALE_COMMAND);
	        myButtons.add(MapCommandType.TOC_COMMAND);
	        myButtons.add(MapToolType.SELECT_TOOL);
			// forward
			setupNavMenu(myButtons,true);
		}				
	}
	
	public void deactivate() {
		super.deactivate();
		NavMenu navBar = getApplication().getNavMenu();
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
		return "Beslutningsstøtte";
	}
	
}
