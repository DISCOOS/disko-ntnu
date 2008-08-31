package org.redcross.sar.map.command;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
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
		button.setFocusable(false);

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
			if(map.getMapBaseIndex()==1) {
				getButton().setIcon(DiskoIconFactory.getIcon(
						DiskoEnumFactory.getIcon(DiskoCommandType.MAP_TOGGLE_COMMAND), "48x48"));
			}
			else { 
				getButton().setIcon(DiskoIconFactory.getIcon(
						DiskoEnumFactory.getText("DiskoCommandType.MAP_TOGGLE_COMMAND_2.icon",null), "48x48"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
