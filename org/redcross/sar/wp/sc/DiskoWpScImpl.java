package org.redcross.sar.wp.sc;

import java.lang.instrument.IllegalClassFormatException;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.util.Utils;
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
        m_states = new States();
        layoutComponent(m_states);
	}

	public String getCaption() {
		return getBundleText("SC");
	}

	public void activate(IDiskoRole role) {

		// forward
		super.activate(role);

		// setup of navbar needed?
		if(isNavMenuSetupNeeded()) {
			// forward
			setupNavMenu(Utils.getListNoneOf(MapToolType.class),false);
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
