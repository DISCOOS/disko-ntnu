package org.redcross.sar.wp.sc;

import java.lang.instrument.IllegalClassFormatException;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.wp.AbstractDiskoWpModule;

/**
 * Implements the DiskoWpStates interface
 * 
 * @author kengu
 * 
 */
public class DiskoWpScImpl extends AbstractDiskoWpModule 
		implements IDiskoWpSc {

    private States m_states;

	/**
	 * Constructs a DiskoWpStatesImpl
	 * 
	 */
	public DiskoWpScImpl() throws IllegalClassFormatException {
		super();
	    initialize();
	}

	private void initialize() {
        m_states = new States(this);
        layoutComponent(m_states);
	}

	public String getCaption() {
		return getBundleText("SC");
	}

	public void activate(IDiskoRole role) {
		
		// forward
		super.activate(role);

		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// forward
			setupNavBar(Utils.getListNoneOf(DiskoToolType.class),false);
		}				
	}
	
	public void deactivate() {
		super.deactivate();
	}
	
	public boolean rollback() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean commit() {
		// TODO Auto-generated method stub
		return false;
	}	
}
