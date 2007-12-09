package org.redcross.sar.wp.ds;

import java.util.EnumSet;

import javax.swing.JToggleButton;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.gui.UIFactory;
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
	public DiskoWpDsImpl(IDiskoRole rolle) {
		super(rolle);
		// initialize gui
	    initialize();
	}

	private void initialize() {
		loadProperties("properties");						
        m_routeCost = new RouteCostPanel(this);
        layoutComponent(m_routeCost);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.geodata.engine.disko.task.DiskoAp#getName()
	 */
	public String getName() {
		return "Beslutningsstøtte";
	}

	public void activated() {
		super.activated();

		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get tool set 
	        EnumSet<DiskoToolType> myTools =
	                EnumSet.of(DiskoToolType.ZOOM_IN_TOOL);
	        myTools.add(DiskoToolType.ZOOM_OUT_TOOL);
	        myTools.add(DiskoToolType.PAN_TOOL);
	        myTools.add(DiskoToolType.ZOOM_FULL_EXTENT_COMMAND);
	        myTools.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myTools.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myTools.add(DiskoToolType.MAP_TOGGLE_COMMAND);
	        myTools.add(DiskoToolType.SCALE_COMMAND);
	        myTools.add(DiskoToolType.TOC_COMMAND);
	        myTools.add(DiskoToolType.SELECT_FEATURE_TOOL);
			// forward
			setupNavBar(myTools,true);
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
}
