package org.redcross.sar.map.command;

import javax.swing.AbstractButton;

import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.map.IPropertyPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
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
	
	public void reset();
	public boolean finish();
	public boolean cancel();
	
	public boolean isActive();
	public boolean activate(int options);	
	public boolean deactivate();
	
	public boolean isDirty();

	public IMsoObjectIf getMsoObject();
	public void setMsoObject(IMsoObjectIf msoObj);
		
	public IMsoObjectIf getMsoOwner();
	public void setMsoOwner(IMsoObjectIf msoOwn);
	
	public MsoClassCode getMsoCode();

	public void setMsoData(IDiskoTool tool);	
	public void setMsoData(IMsoObjectIf msoOwn, IMsoObjectIf msoObj, MsoClassCode msoCode);
	
		
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
	
	public boolean setDefaultPropertyPanel();
	
	public IPropertyPanel getDefaultPropertyPanel();
	
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
