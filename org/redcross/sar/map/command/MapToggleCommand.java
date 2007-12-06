package org.redcross.sar.map.command;

import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.DiskoMapManagerImpl;
import org.redcross.sar.map.IDiskoMap;

public class MapToggleCommand extends AbstractDiskoTool {

	private static final long serialVersionUID = 1L;
	
	public MapToggleCommand() {
		// forward
		super();
		// set tool type
		type = DiskoToolType.MAP_TOGGLE_COMMAND;		

	}
	
	public void onCreate(Object obj) {
		if (obj instanceof IDiskoMap) {
			map = (DiskoMap)obj;
		}
	}

	public boolean activate(boolean allow) {
		// forward
		boolean bflag = super.activate(false);
		
		try {
			((DiskoMapManagerImpl)map.getMapManager()).toggleMap();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// forward
		return bflag;
	}
	
	public boolean deactivate(){
		// forward
		return super.deactivate();
	}	
}
