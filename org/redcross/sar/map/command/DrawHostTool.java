/**
 * 
 */
package org.redcross.sar.map.command;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.JToggleButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoCustomIcon;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DrawDialog;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.IDiskoTool.IDiskoToolState;

import com.esri.arcgis.controls.BaseCommand;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.ITool;

/**
 * @author kennetgu
 *
 */
public class DrawHostTool extends BaseCommand implements IDiskoHostTool {
	private static final long serialVersionUID = 1L; 
	
	// flags
	protected boolean isActive = false;
	protected boolean showDirect = false;
	protected boolean showDialog = true;

	// current hosted tool
	private IDiskoTool tool = null;
	
	// GUI components
	protected DiskoMap map = null;
	protected DiskoDialog dialog = null;
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
		Dimension size = app.getUIFactory().getSmallButtonSize();
		button = new JToggleButton();
		button.setPreferredSize(size);
		
		// add show dialog listener
		button.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				// double click?
				if(e.getClickCount() == 2) {
					dialog.setVisible(!dialog.isVisible());
				}
			}

			public void mousePressed(MouseEvent e) {
				// start show/hide
				dialog.doShow(!dialog.isVisible(), 250);				
			}

			public void mouseReleased(MouseEvent e) {
				// stop show if not shown already
				dialog.cancelShow();				
			}
			
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}

		});

		// create draw dialog
		dialog = new DrawDialog(app.getFrame());
		dialog.setIsToggable(false);

		// register me as host tool
		((DrawDialog)dialog).setHostTool(this);
		
		// do not show dialog first time onClick is invoked
		showDirect = false; 
		
	}
	
	public void onCreate(Object obj) {
		
		try {
			if (obj instanceof IDiskoMap) {
				map = (DiskoMap)obj;
				DrawDialog drawDialog = (DrawDialog)dialog;
				drawDialog.onLoad(map);
				drawDialog.setLocationRelativeTo(map, DiskoDialog.POS_WEST, true);
				if(button!=null && button.getIcon() instanceof DiskoCustomIcon) {
					DiskoCustomIcon icon = (DiskoCustomIcon)button.getIcon();
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
			if(map!=null && (tool instanceof ITool)) {
				try {
					// set as current map tool
					map.setCurrentToolByRef((ITool)tool,false);
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

	
	public void setTool(IDiskoTool tool) {
		// host can't host as empty tool
		if(tool!=null) {
			// save tool
			this.tool = tool;
			showDialog = tool.isShowDialog();
			// update button
			button.setIcon(tool.getButton().getIcon());
			button.setToolTipText(tool.getButton().getToolTipText());
		}
	}

	public IDiskoTool getTool() {
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

	public DiskoDialog getDialog() {
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
		private IDiskoTool tool = null;
		
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
			tool.setTool(this.tool);
		}
	}
}
