package org.redcross.sar.gui.panel;

import org.redcross.sar.map.tool.IMapTool;

public abstract class AbstractToolPanel extends AbstractPanel implements IToolPanel {


	private static final long serialVersionUID = 1L;

	private IMapTool tool;

	/* ===========================================
	 * Constructors
	 * ===========================================
	 */

	public AbstractToolPanel(IMapTool tool) {
		// forward
		this(tool.getCaption(),tool);
	}

	public AbstractToolPanel(String caption, IMapTool tool) {

		// forward
		super(caption);

		// prepare
		this.tool = tool;

	}

	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */

	public IMapTool getTool() {
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
			tool.cancel();
		}
		return true;
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
