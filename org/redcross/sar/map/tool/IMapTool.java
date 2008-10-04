package org.redcross.sar.map.tool;

import javax.swing.AbstractButton;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.event.IToolListenerIf;
import org.redcross.sar.thread.event.IDiskoWorkListener;

import com.esri.arcgis.systemUI.ITool;

public interface IMapTool extends ITool {

	public enum MapToolType {
		DRAW_HOST_TOOL,
		FREEHAND_TOOL,
		LINE_TOOL,
		POI_TOOL,
		POSITION_TOOL,
		FLANK_TOOL,
		SPLIT_TOOL,
		SELECT_TOOL,
		ZOOM_IN_TOOL,
		ZOOM_OUT_TOOL,
		PAN_TOOL,
		ERASE_TOOL
    }	
	
	/*===============================================
	 * IDiskoTool methods
	 *===============================================
	 */
	
	public String getName();
	public MapToolType getType();
	
	public boolean isHosted();	
	public IHostDiskoTool getHostTool();
	
	public void onCreate(Object obj);
	
	public void reset();
	public boolean finish();
	public boolean cancel();
	
	public boolean isActive();
	public boolean activate(int options);	
	public boolean deactivate();
	
	public boolean isDirty();
	public boolean resetDirtyFlag();
		
	public IDiskoMap getMap();
	
	public DefaultDialog getDialog();
	
	public AbstractButton getButton();
	
	public void addDiskoWorkListener(IDiskoWorkListener listener);	
	public void removeDiskoEventListener(IDiskoWorkListener listener);
	
	public boolean addToolListener(IToolListenerIf listener);	
	public boolean removeToolListener(IToolListenerIf listener);	
	
	public Object getAttribute(String attribute);
	
	public void setAttribute(Object value, String attribute);
	
	public boolean isShowDialog();
		
	public void setShowDialog(boolean isShowDialog);
	
	public boolean setToolPanel(IToolPanel panel);
	
	public boolean setDefaultPropertyPanel();
	
	public IToolPanel getDefaultToolPanel();
	
	public IToolPanel getToolPanel();
	
	public IToolPanel addToolPanel();
	
	public boolean removeToolPanel(IToolPanel panel);
	
	public IMapToolState save();
	
	public boolean load(IMapToolState state);
	
	public interface IMapToolState {};
	
	/*===============================================
	 * ICommand methods
	 *===============================================
	 */
	
	public String getCaption();

	public String getCategory();

	public int getHelpContextID();

	public String getHelpFile();

	public String getMessage();

	public String getTooltip();

	public boolean isChecked();

	public boolean isEnabled();	
	
}
