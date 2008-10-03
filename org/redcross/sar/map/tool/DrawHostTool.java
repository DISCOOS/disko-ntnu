/**
 * 
 */
package org.redcross.sar.map.tool;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.AbstractButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.DrawDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.IMapTool.IDiskoToolState;
import org.redcross.sar.util.Utils;

import com.esri.arcgis.controls.BaseCommand;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author kennetgu
 *
 */
public class DrawHostTool extends BaseCommand implements IHostDiskoTool {

	private static final long serialVersionUID = 1L; 
	
	// flags
	protected boolean isActive = false;
	protected boolean showDirect = false;
	protected boolean showDialog = true;

	// current hosted tool
	private IMapTool tool = null;
	
	// GUI components
	protected DiskoMap map = null;
	protected DrawDialog dialog = null;
	protected AbstractButton button = null;
	
	/**
	 * Constructs the DrawHostTool
	 */
	public DrawHostTool() throws IOException, AutomationException {

		// forward
		super();
		
		// get current application
		IDiskoApplication app = Utils.getApp();
		
		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
		
		// add show dialog listener
		button.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				// consume?
				if(dialog == null || !showDialog ) return;
				// double click?
				if(e.getClickCount() == 2) {
					dialog.setVisible(!dialog.isVisible());
				}
			}

			public void mousePressed(MouseEvent e) {
				// consume?
				if(dialog == null || !showDialog ) return;
				// start show/hide
				dialog.delayedSetVisible(!dialog.isVisible(), 250);				
			}

			public void mouseReleased(MouseEvent e) {
				// consume?
				if(dialog == null || !showDialog ) return;
				// stop show if not shown already
				dialog.cancelSetVisible();				
			}
			
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}

		});

		// create draw dialog
		dialog = new DrawDialog(app.getFrame());

		// register me as host tool
		dialog.setHostTool(this);
		
		// do not show dialog first time onClick is invoked
		showDirect = false; 
		
	}
	
	public void onCreate(Object obj) {
		
		try {
			if (obj instanceof IDiskoMap) {
				map = (DiskoMap)obj;
				dialog.register(map);
				dialog.setLocationRelativeTo(map, DefaultDialog.POS_WEST, true, true);
				if(button!=null && button.getIcon() instanceof DiskoIcon) {
					DiskoIcon icon = (DiskoIcon)button.getIcon();
					icon.setMarked(true);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onClick() {
		// forward to map?
		if(tool!=null) {
			if(map!=null) {
				try {
					// set as current map tool
					map.setActiveTool(tool,0);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		// toogle dialog?
		if (dialog != null && showDialog && showDirect )
			dialog.setVisible(!dialog.isVisible());
	}

	
	public void setTool(IMapTool tool, boolean activate) {
		// host can't host as empty tool
		if(tool!=null) {
			// save tool
			this.tool = tool;
			showDialog = tool.isShowDialog();
			// forward?
			if(dialog.getSelectedTool()!=tool)
				dialog.setSelectedTool(tool, activate);
			// update button
			button.setIcon(tool.getButton().getIcon());
			button.setToolTipText(tool.getButton().getToolTipText());
			button.setSelected(tool.getButton().isSelected());
		}
	}

	public IMapTool getTool() {
		return tool;
	}

	/**
	 * The default behaviour is that is allways allowed 
	 * to activate this class. Howerver, an extender
	 * of this class can override this behavior. 
	 */	
	public boolean activate(boolean allow){
		// set flag
		isActive = true;
		// toogle dialog?
		if (dialog != null && allow && showDialog && showDirect)
				dialog.setVisible(!dialog.isVisible());
		// allways allowed
		return true;
	}

	/**
	 * The default behaviour is that is allways allowed 
	 * to deactivate this class. Howerver, an extender
	 * of this class can override this behavior. 
	 */
	public boolean deactivate(){
		// reset flag
		isActive = false;
		// An extender of this class could override this method
		if (dialog != null && showDialog && showDirect)
			dialog.setVisible(false);
		// return state
		return true;
	}

	public DrawDialog getDialog() {
		return dialog;
	}

	public AbstractButton getButton() {
		return button;
	}
	
	public IDiskoToolState save() {
		// get new state
		return new DrawHostToolState(this);
	}
	
	public boolean load(IDiskoToolState state) {
		// valid state?
		if(state instanceof DrawHostToolState) {
			((DrawHostToolState)state).load(this);
			return true;
		}
		return false;
	
	}	
	
	/*===============================================
	 * Overridden ICommand methods
	 *===============================================
	 */
	
	@Override
	public String getCaption() {
		try {
			return tool==null ? super.getCaption() : tool.getCaption();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getCategory() {
		try {
			return tool==null ? super.getCategory() : tool.getCategory();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getHelpContextID() {
		try {
			return tool==null ? super.getHelpContextID() : tool.getHelpContextID();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String getHelpFile() {
		try {
			return tool==null ? super.getHelpFile() : tool.getHelpFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getMessage() {
		try {
			return tool==null ? super.getMessage() : tool.getMessage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getName() {
		try {
			return tool==null ? super.getName() : tool.getName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getTooltip() {
		try {
			return tool==null ? super.getTooltip() : tool.getTooltip();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isChecked() {
		try {
			return tool==null ? super.isChecked() : tool.isChecked();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		try {
			return tool==null ? super.isEnabled() : tool.isEnabled();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
		
	/*===============================================
	 * Inner classes
	 *===============================================
	 */
	
	/**
	 * Abstract tool state class
	 * 
	 * @author kennetgu
	 *
	 */
	public class DrawHostToolState implements IDiskoToolState {

		// flags
		private boolean isActive = false;
		private boolean showDirect = false;
		private boolean showDialog = false;

		// current hosted tool
		private IMapTool tool = null;
		
		// create state
		public DrawHostToolState(DrawHostTool tool) {
			save(tool);
		}
		
		public void save(DrawHostTool tool) {
			this.isActive = tool.isActive;
			this.showDirect = tool.showDirect;
			this.showDialog = tool.showDialog;
			this.tool = tool.tool;
		}
		
		public void load(DrawHostTool tool) {
			tool.isActive = this.isActive;
			tool.showDirect = this.showDirect;
			tool.showDialog = this.showDialog;
			// forward?
			if(this.tool!=null)
				dialog.setSelectedTool(this.tool,this.isActive);
		}
	}
}
