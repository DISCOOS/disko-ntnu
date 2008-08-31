package org.redcross.sar.gui.dialog;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.MessagePanel;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MessageDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;
	
	public static final int ERROR_MESSAGE = 0;
	public static final int INFORMATION_MESSAGE = 1;
	public static final int WARNING_MESSAGE = 2;
	public static final int QUESTION_MESSAGE = 3;
	
	private String respons = null;
	
	private MessagePanel msgPanel = null;

	/**
	 * @param owner
	 */
	public MessageDialog(Frame owner) {
		// forward
		super(owner);
		// initialize GUI
		initialize();
	}
	
	private void initialize() {
		try {
            this.setModal(true);
            this.setUndecorated(true);
            this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.setContentPane(getMessagePanel());
            this.setPreferredSize(new Dimension(300,150));
            this.pack();
				
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	public void setVisible(boolean isVisible) {
		// Only use showLogin() og showChangeRole()
	}
	
	/**
	 * This method initializes msgPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private MessagePanel getMessagePanel() {
		if (msgPanel == null) {
			msgPanel = new MessagePanel();
			msgPanel.setScrollBarPolicies(
					DefaultPanel.VERTICAL_SCROLLBAR_NEVER, 
					DefaultPanel.HORIZONTAL_SCROLLBAR_NEVER);
			msgPanel.setPreferredSize(new Dimension(300,150));
			msgPanel.setPreferredBodySize(new Dimension(270,100));
			msgPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					respons(e.getActionCommand());
				}
				
			});
		}
		return msgPanel;
	}

	/*
	public void onResize() {
		getMessagePanel().onResize();
	}	
	*/
	
	public String showMessage(String title, String msg, int type) {
		// reset
		respons = null;
		// update panel
		Icon icon = null;
		switch(type) {
		case ERROR_MESSAGE: 
			icon = UIManager.getIcon("OptionPane.errorIcon"); break;
		case INFORMATION_MESSAGE: 
			icon = UIManager.getIcon("OptionPane.informationIcon"); break;
		case WARNING_MESSAGE: 
			icon = UIManager.getIcon("OptionPane.warningIcon"); break;
		case QUESTION_MESSAGE: 
			icon = UIManager.getIcon("OptionPane.questionIcon"); break;
		}
		getMessagePanel().setCaptionIcon(icon);
		getMessagePanel().setCaptionText(title);
		getMessagePanel().setMessage(msg);
		getMessagePanel().setButtonVisible("cancel", type == QUESTION_MESSAGE);
		// show modal dialog
		super.setVisible(true);
		// finished
		return respons;
	}
	
	private void respons(String cmd) {
		respons = cmd;
		super.setVisible(false);		
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
