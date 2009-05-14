package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.map.tool.IMapTool;

public class BaseToolPanel extends BasePanel implements IToolPanel {

	private static final long serialVersionUID = 1L;

	private IMapTool tool;

	private JPanel toolPanel;
	private HeaderPanel descriptionPanel;

	/* ===========================================
	 * Constructors
	 * =========================================== */

	public BaseToolPanel(IMapTool tool) {
		// forward
		this(tool!=null ? tool.getCaption() : "",tool);
	}

	public BaseToolPanel(IMapTool tool, ButtonSize buttonSize) {
		this("Utfør",tool!=null ? tool.getCaption() : "",tool,buttonSize);
	}

	public BaseToolPanel(String description, IMapTool tool) {
		this("Utfør",description,tool,ButtonSize.SMALL);
	}

	public BaseToolPanel(String caption, String description, IMapTool tool, ButtonSize buttonSize) {

		// forward
		super(caption,buttonSize);

		// prepare
		this.tool = tool;

		// initialize GUI
		initialize();

		// update captions
		setDescriptionText(description);

	}

	/* ===========================================
	 * Public methods
	 * =========================================== */

	public HeaderPanel getDescriptionPanel() {
		if(descriptionPanel==null) {
			descriptionPanel = new HeaderPanel("",getButtonSize());
		}
		return descriptionPanel;
	}

	/**
	 * This method gets the actions icon
	 *
	 * @return Icon
	 */
	public Icon getDescriptionIcon() {
		return getDescriptionPanel().getCaptionIcon();
	}

	/**
	 * This method sets the Description icon
	 *
	 */
	public void setDescriptionIcon(Icon icon) {
		getDescriptionPanel().setCaptionIcon(icon);
	}

	/**
	 * This method gets the Description text
	 *
	 * @return String
	 */
	public String getDescriptionText() {
		return getDescriptionPanel().getCaptionText();
	}

	/**
	 * This method sets the Description text
	 *
	 */
	public void setDescriptionText(String caption) {
		getDescriptionPanel().setCaptionText(caption);
	}

	/**
	 * This method sets the caption colors
	 *
	 */
	public void setDescriptionColor(Color foreground,Color background) {
		getDescriptionPanel().setCaptionColor(foreground,background);
	}

	public void setDescriptionVisible(boolean isVisible) {
		getDescriptionPanel().setVisible(isVisible);
	}

	public boolean isDescriptionVisible() {
		return getDescriptionPanel().isVisible();
	}

	@Override
    public void setBorderVisible(boolean isVisible) {
		super.setBorderVisible(isVisible);
        if(isVisible) {
        	getDescriptionPanel().setInsets(0, 0, 1, 0);
            getHeaderPanel().setInsets(1, 0, 1, 0);
        }
        else {
        	getDescriptionPanel().setInsets(1, 1, 1, 1);
        	getHeaderPanel().setInsets(1, 1, 1, 1);
        }
    }

	/* ===========================================
	 * IToolPanel implementation
	 * =========================================== */

	public IMapTool getTool() {
		return tool;
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */

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

	/* ===========================================
	 * Helper methods
	 * =========================================== */

	private void initialize() {

		// clear
		this.removeAll();

		// rebuild
		this.setLayout(new BorderLayout(0,5));
		this.add(getDescriptionPanel(),BorderLayout.NORTH);
		this.add(getToolPanel(),BorderLayout.CENTER);

		// add height of description panel including gap
		minimumCollapsedHeight += getDescriptionPanel().getPreferredSize().height + 5;

        // hide borders
        setBorderVisible(false);

	}

	private JPanel getToolPanel() {
		if(toolPanel==null) {
			toolPanel = new JPanel(new BorderLayout(0,5));
			toolPanel.add(getHeaderPanel(),BorderLayout.NORTH);
			toolPanel.add(getScrollPane(),BorderLayout.CENTER);
		}
		return toolPanel;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"