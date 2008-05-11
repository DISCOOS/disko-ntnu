package org.redcross.sar.wp.states;

import java.lang.instrument.IllegalClassFormatException;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
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
	public DiskoWpStatesImpl(IDiskoRole rolle) throws IllegalClassFormatException {
		super(rolle);
	    initialize();
	}

	private void initialize() {
        m_states = new States(this);
        layoutComponent(m_states);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.geodata.engine.disko.task.DiskoAp#getCaption()
	 */
	public String getCaption() {
		return "Tilstand";
	}

	public void activated() {
		super.activated();

		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// forward
			setupNavBar(Utils.getListNoneOf(DiskoToolType.class),false);
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
