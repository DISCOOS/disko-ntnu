package org.redcross.sar.map.command;

import javax.swing.AbstractButton;

import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.map.IPropertyPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.esri.arcgis.systemUI.ITool;

public interface IDiskoTool extends ITool {

	public enum DiskoToolType {
		DRAW_HOST_TOOL,
		FREEHAND_TOOL,
		LINE_TOOL,
		POI_TOOL,
		POSITION_TOOL,
		FLANK_TOOL,
		SPLIT_TOOL,
		SELECT_FEATURE_TOOL,
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
	
	public DiskoToolType getType();
	
	public boolean isHosted();
	
	public IHostDiskoTool getHostTool();
	
	public void onCreate(Object obj);
	
	public boolean isActive();
	
	public boolean activate(boolean allow);
	
	public boolean deactivate();
	
	public void setMsoDrawData(IDiskoTool tool);
	
	public void setMsoDrawData(IMsoObjectIf msoOwner, 
			IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode);
	
	public void setMsoObject(IMsoObjectIf msoObject);
	
	public IMsoObjectIf getMsoObject();
	
	public void setMsoOwner(IMsoObjectIf msoOwner);
	
	public IMsoObjectIf getMsoOwner();
	
	public IMsoManagerIf.MsoClassCode getMsoClassCode();

	public IDiskoMap getMap();
	
	public DiskoDialog getDialog();
	
	public AbstractButton getButton();
	
	public void addDiskoWorkEventListener(IDiskoWorkListener listener);
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener);
	
	public Object getAttribute(String attribute);
	
	public void setAttribute(Object value, String attribute);
	
	public boolean isShowDialog();
		
	public void setShowDialog(boolean isShowDialog);
	
	public boolean setPropertyPanel(IPropertyPanel panel);
	
	public IPropertyPanel getPropertyPanel();
	
	public IPropertyPanel addPropertyPanel();
	
	public boolean removePropertyPanel(IPropertyPanel panel);
	
	public IDiskoToolState save();
	
	public boolean load(IDiskoToolState state);
	
	public interface IDiskoToolState {};
	
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
