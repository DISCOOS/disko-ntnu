package org.redcross.sar.wp.states;

import java.util.EnumSet;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.wp.AbstractDiskoWpModule;

/**
 * Implements the DiskoWpStates interface
 * 
 * @author kengu
 * 
 */
public class DiskoWpStatesImpl extends AbstractDiskoWpModule 
		implements IDiskoWpStates {

    private States m_states;

	/**
	 * Constructs a DiskoWpStatesImpl
	 * 
	 * @param rolle
	 *            A reference to the DiskoRolle
	 */
	public DiskoWpStatesImpl(IDiskoRole rolle) {
		super(rolle);
	    initialize();
	}

	private void initialize() {
		loadProperties("properties");						
        m_states = new States(this);
        layoutComponent(m_states);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.geodata.engine.disko.task.DiskoAp#getName()
	 */
	public String getName() {
		return "Tilstand";
	}

	public void activated() {
		super.activated();

		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get tool set 
	        EnumSet<DiskoToolType> myTools =
	                EnumSet.noneOf(DiskoToolType.class);
			// forward
			setupNavBar(myTools,false);
		}				
	}
	
	public void deactivated() {
		super.deactivated();
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
