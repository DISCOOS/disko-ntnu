package org.redcross.sar.gui.panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;

public class DefaultPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private JButton finishButton;
	private JButton cancelButton;
	private DiskoIcon finishIcon;
	private DiskoIcon cancelIcon;

	/* ===========================================
	 * Constructors
	 * =========================================== */

	public DefaultPanel() {
		this("");
	}

	public DefaultPanel(String caption) {
		// forward
		this(caption,true,true,ButtonSize.SMALL);
	}

	public DefaultPanel(ButtonSize buttonSize) {
		// forward
		this("",true,true,buttonSize);
	}

	public DefaultPanel(String caption, boolean finish, boolean cancel) {
		this(caption,finish,cancel,ButtonSize.SMALL);
	}

	public DefaultPanel(String caption, boolean finish, boolean cancel, ButtonSize buttonSize) {
		// forward
		super(caption,buttonSize);
		// initialize gui
		initialize();
		// hide default buttons
		setButtonVisible("finish", finish);
		setButtonVisible("cancel", cancel);
	}

	/* ===========================================
	 * protected methods
	 * =========================================== */

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

	/* ===========================================
	 * Private methods
	 * =========================================== */
	
	private JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",getButtonSize());
				cancelIcon = new DiskoIcon(cancelButton.getIcon(),Color.RED,0.4f);
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
				finishButton = DiskoButtonFactory.createButton("GENERAL.FINISH",getButtonSize());
				finishIcon = new DiskoIcon(finishButton.getIcon(),Color.GREEN,0.4f);
				finishButton.setIcon(finishIcon);
				finishButton.setDefaultCapable(true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return finishButton;
	}

	/* ===========================================
	 * IPanel interface implementation
	 * =========================================== */

	public void update() {

		// update attributes
		finishIcon.setColored(isDirty());
		cancelIcon.setColored(isDirty());
		finishButton.repaint();
		cancelButton.repaint();

	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
