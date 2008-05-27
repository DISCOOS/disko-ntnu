package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.SwingUtilities;

import org.redcross.sar.map.IDiskoMap;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.ICommand;
import com.esri.arcgis.systemUI.ITool;

/**
 * This class is used to wrap tools that implement the 
 * ITool and ICommand interface with the IDiskoTool interface
 * <p>
 * <b>To prevent deadlock situastion this class should be used 
 * to wrap any default ArcObject tools.</b>
 * <p>
 * This is accomplished by intercept user interactions,
 * which is important because of the following reason:
 * <p>
 * Swing has a serious internal error which occur relatively 
 * will deadlock the application. This error comes from the fact
 * that under certain conditions, the Evend Dispatch Thread (EDT)
 * becomse asynchronious if multithreading is used (which the 
 * system does), even if all rules are followed (which the system does)
 * <p>
 * The error occures in sun.awt.windows.WComponentPeer.pShow(Native Method);
 * a lock is not released properly because of the asynchronious state
 * of EDT, and deadlock occurs. In the system, this happends if the 
 * progress dialog box is show using the ArcObjects event stack. ArcObjects 
 * do run its own threads and the error may reside here. However, 
 * similar cases is reported by SDN members and fixes has been provides by 
 * sun. 
 * <p>
 * 
 * @author kennetgu
 *
 */

public class DiskoToolWrapper extends AbstractDiskoTool {

	private static final long serialVersionUID = 1L;

	// gesture constants
	public enum WrapAction {
		NONE,
		ONMOUSEDOWN,
		ONMOUSEMOVE,
		ONMOUSEUP,
		ONCLICK,
		ONDBLCLICK
	}
	
	private ITool tool = null;
	private IDiskoMap map = null;
	private ICommand command = null;
	private WrapAction wrap = WrapAction.NONE;
	
	public static DiskoToolWrapper create(Object tool, WrapAction wrap) {
		return new DiskoToolWrapper(tool,wrap);
	}
	
	public DiskoToolWrapper(Object tool, WrapAction wrap) {
		// prepare
		this.wrap = wrap;
		if(tool instanceof ITool)
			this.tool = (ITool)tool;
		if(tool instanceof ICommand)
			this.command = (ICommand)tool;
	}

	public boolean isTool() {
		return tool!=null;
	}
	
	public boolean isCommand() {
		return command!=null;
	}
	
	public ITool getTool() {
		return tool;
	}
	
	public ICommand getCommand() {
		return command;
	}
	
	/* ==========================================
	 * Wrapper of ITool interface
	 * ==========================================
	 */
	
	@Override
	public int getCursor() {
		if(tool!=null) {
			try {
				return tool.getCursor();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	@Override
	public boolean onContextMenu(int arg0, int arg1) {
		if(tool!=null) {
			try {
				return tool.onContextMenu(arg0, arg1);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void onDblClick() {
		if(tool!=null) {
			beforeAction(WrapAction.ONDBLCLICK);
			try {
				tool.onDblClick();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			afterAction(WrapAction.ONDBLCLICK);
		}
	}

	@Override
	public void onKeyDown(int arg0, int arg1) {
		if(tool!=null) {
			try {
				tool.onKeyDown(arg0, arg1);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onKeyUp(int arg0, int arg1) {
		if(tool!=null) {
			try {
				tool.onKeyUp(arg0, arg1);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onMouseDown(int arg0, int arg1, int arg2, int arg3) {
		if(tool!=null) {
			beforeAction(WrapAction.ONMOUSEDOWN);
			try {
				tool.onMouseDown(arg0, arg1, arg2, arg3);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			afterAction(WrapAction.ONMOUSEDOWN);
		}
	}

	@Override
	public void onMouseMove(int arg0, int arg1, int arg2, int arg3) {
		if(tool!=null) {
			beforeAction(WrapAction.ONMOUSEMOVE);
			try {
				tool.onMouseMove(arg0, arg1, arg2, arg3);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			afterAction(WrapAction.ONMOUSEMOVE);
		}
	}

	@Override
	public void onMouseUp(int arg0, int arg1, int arg2, int arg3) {
		if(tool!=null) {
			beforeAction(WrapAction.ONMOUSEUP);
			try {
				tool.onMouseUp(arg0, arg1, arg2, arg3);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			afterAction(WrapAction.ONMOUSEUP);
		}
	}

	@Override
	public void refresh(int arg0) {
		if(tool!=null) {
			try {
				tool.refresh(arg0);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	/* ==========================================
	 * Wrapper of ICommand interface
	 * ==========================================
	 */
	
	@Override
	public void onCreate(Object arg0) {
		if(arg0 instanceof IDiskoMap)
			map = (IDiskoMap)arg0;
		if(command==null) return;
		try {
			command.onCreate(arg0);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick() {
		beforeAction(WrapAction.ONCLICK);
		try {
			command.onClick();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		afterAction(WrapAction.ONCLICK);
	}

	@Override
	public String getCaption() {
		if(command!=null) {
			try {
				return command.getCaption();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getCategory() {
		if(command!=null) {
			try {
				return command.getCategory();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public int getHelpContextID() {
		if(command!=null) {
			try {
				return command.getHelpContextID();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	@Override
	public String getHelpFile() {
		if(command!=null) {
			try {
				return command.getHelpFile();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getMessage() {
		if(command!=null) {
			try {
				return command.getMessage();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getTooltip() {
		if(command!=null) {
			try {
				return command.getTooltip();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public boolean isChecked() {
		if(command!=null) {
			try {
				return command.isChecked();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		if(command!=null) {
			try {
				return command.isEnabled();
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private void beforeAction(WrapAction type) {
		// wrap?
		if(!WrapAction.NONE.equals(wrap)  
				&& type.equals(wrap)) {
			// trace event
			//System.out.println(toString() + "::beforeAction("+type+")");						
			// forward?
		}			
	}
	
	private void afterAction(WrapAction type) {
		// wrap?
		if(!WrapAction.NONE.equals(wrap) 
				&& type.equals(wrap)) {
			// trace event
			//System.out.println(toString() + "::afterAction("+type+")");						
		}		
	}
	
}
