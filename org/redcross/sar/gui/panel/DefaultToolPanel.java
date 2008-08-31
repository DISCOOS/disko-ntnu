package org.redcross.sar.gui.panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.tool.IDiskoTool;

public class DefaultToolPanel extends BaseToolPanel {

	private static final long serialVersionUID = 1L;
	
	private JButton finishButton = null;
	private JButton cancelButton = null;
	private DiskoIcon finishIcon = null;
	private DiskoIcon cancelIcon = null;
	
	/* ===========================================
	 * Constructors
	 * ===========================================
	 */	
	
	public DefaultToolPanel(IDiskoTool tool) {
		// forward
		this(tool.getCaption(),tool);
	}
	
	public DefaultToolPanel(String caption, IDiskoTool tool) {
		// forward
		super(caption,tool);
		// initialize gui
		initialize();
	}
	
	/* ===========================================
	 * Private methods
	 * ===========================================
	 */
	
	/**
	 * This method initializes the panel
	 * 	
	 */
	private void initialize() {
		
		// add default buttons
		addButton(getFinishButton(),"finish");
		addButton(getCancelButton(),"cancel");
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if("finish".equalsIgnoreCase(cmd)) finish();
				else if("cancel".equalsIgnoreCase(cmd)) cancel();
			}
			
		});
		
	}
	
	private JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.SMALL);
				cancelIcon = new DiskoIcon(cancelButton.getIcon(),Color.RED,0.4f);
				cancelIcon.setMarked(false);
				cancelButton.setIcon(cancelIcon);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return cancelButton;
	}
	
	private JButton getFinishButton() {
		if (finishButton == null) {
			try {
				finishButton = DiskoButtonFactory.createButton("GENERAL.FINISH",ButtonSize.SMALL);
				finishIcon = new DiskoIcon(finishButton.getIcon(),Color.GREEN,0.4f);
				finishIcon.setMarked(false);
				finishButton.setIcon(finishIcon);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return finishButton;
	}	
	
	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */

	@Override
	public boolean isDirty() {
		return super.isDirty() || getTool().isDirty();		
	}
	
	@Override
	public void update() {
		
		// update attributes
		finishIcon.setColored(isDirty());
		cancelIcon.setColored(isDirty());
		finishButton.repaint();
		cancelButton.repaint();
			
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
