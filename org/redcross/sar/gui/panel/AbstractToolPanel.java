package org.redcross.sar.gui.panel;

import org.redcross.sar.map.tool.IDiskoTool;

public abstract class AbstractToolPanel extends AbstractPanel implements IToolPanel {


	private static final long serialVersionUID = 1L;
	
	private IDiskoTool tool = null;		
	
	/* ===========================================
	 * Constructors
	 * ===========================================
	 */	
	
	public AbstractToolPanel(IDiskoTool tool) {
		// forward
		this(tool.getCaption(),tool);
	}
	
	public AbstractToolPanel(String caption, IDiskoTool tool) {
		
		// forward
		super(caption);
		
		// prepare
		this.tool = tool;
		
	}

	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */	
	
	public IDiskoTool getTool() {
		return tool;
	}
	
	/* ===========================================
	 * Protected methods
	 * ===========================================
	 */	
	
	@Override
	protected boolean beforeCancel() {
		// forward
		if(super.beforeCancel()) {
			return tool.cancel();
		}
		return false;
	}

	@Override
	protected boolean beforeFinish() {
		// forward
		if(super.beforeFinish()) {
			return tool.finish();
		}
		return false;
	}	
}  //  @jve:decl-index=0:visual-constraint="10,10"
