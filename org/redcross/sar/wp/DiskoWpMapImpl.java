/**
 * 
 */
package org.redcross.sar.wp;

import java.util.EnumSet;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;

/**
 * Implements the DiskoApKart interface
 * @author geira
 *
 */
public class DiskoWpMapImpl extends AbstractDiskoWpModule implements IDiskoWpMap {
	
	/**
	 * Constructs a DiskoApKartImpl
	 * @param rolle A reference to the DiskoRolle
	 */
	public DiskoWpMapImpl(IDiskoRole rolle) {
		super(rolle);
		initialize();
	}
	
	private void initialize() {
		DiskoMap map = (DiskoMap)getMap();
		layoutComponent(map);
	}
	
	public void activated() {
		NavBar navBar = getApplication().getNavBar();
		EnumSet<DiskoToolType> myInterests = 
			EnumSet.of(DiskoToolType.ZOOM_IN_TOOL);
		myInterests.add(DiskoToolType.ZOOM_OUT_TOOL);
		myInterests.add(DiskoToolType.PAN_TOOL);
		myInterests.add(DiskoToolType.ZOOM_FULL_EXTENT_COMMAND);
		myInterests.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
		myInterests.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
		myInterests.add(DiskoToolType.MAP_TOGGLE_COMMAND);
		navBar.setVisibleButtons(myInterests,true,false);
	}
	
	/* (non-Javadoc)
	 * @see com.geodata.engine.disko.task.DiskoAp#getName()
	 */
	public String getName() {
		return "Kart";
	}

	public void reInitWP()
	{
		// TODO Auto-generated method stub
		
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
