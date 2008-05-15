package org.redcross.sar.map.command;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;

public class MapToggleCommand extends AbstractDiskoCommand {

	private static final long serialVersionUID = 1L;
	
	private IDiskoMap map = null;
	
	public MapToggleCommand() {
		
		// forward
		super();
		
		// set tool type
		type = DiskoCommandType.MAP_TOGGLE_COMMAND;
		
		// set flags
		this.showDirect = true;
		
		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);

	}
	
	public void onCreate(Object obj) {
		if (obj instanceof IDiskoMap) {
			map = (DiskoMap)obj;
		}
	}

	@Override
	public void onClick() {
		try {
			// forward
			map.getMapManager().toggleMapBase();
			// toggle nav bar icon
			Utils.getApp().getNavBar().switchIcon("maptoggle", map.getMapBaseIndex());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
