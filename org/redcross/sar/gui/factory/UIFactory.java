package org.redcross.sar.gui.factory;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkEventListener;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.gui.LoginDialog;
import org.redcross.sar.gui.MainMenuPanel;
import org.redcross.sar.gui.MainPanel;
import org.redcross.sar.gui.NumPadDialog;
import org.redcross.sar.gui.SubMenuPanel;
import org.redcross.sar.gui.TaskDialog;
import org.redcross.sar.gui.map.MapOptionDialog;

public class UIFactory {
	
	private IDiskoApplication app = null;
	private JPanel contentPanel = null;
	private DiskoGlassPane glassPane = null;
	private MainMenuPanel mainMenuPanel = null;
	private SubMenuPanel subMenuPanel = null;
	private MainPanel mainPanel = null;
	private JPanel menuPanel = null;
	private LoginDialog loginDialog = null;
	private NumPadDialog numPadDialog = null;
	private MapOptionDialog mapOptionDialog = null;
	private TaskDialog taskDialog = null;
	
	private Dimension largeButtonSize = null;
	private Dimension normalButtonSize = null;
	private Dimension smallButtonSize = null;
	
	public UIFactory(IDiskoApplication app) {
		this.app = app;
	}
	
	public NumPadDialog getNumPadDialog(){
		if (numPadDialog == null) {
			numPadDialog = new NumPadDialog(app.getFrame());
		}
		return numPadDialog;						
	}
	
	public LoginDialog getLoginDialog() {
		if (loginDialog == null) {
			loginDialog = new LoginDialog(app.getFrame());
			loginDialog.getUsernameTextField().setText("disko");
			loginDialog.getPasswordField().setText("disko");
			loginDialog.addDiskoWorkEventListener(new IDiskoWorkEventListener() {
				
				public void onWorkCancel(DiskoWorkEvent e) {
					loginDialog.setVisible(false);
					if (loginDialog.getAllowExit())
						System.exit(0);
				}
				public void onWorkFinish(DiskoWorkEvent e) {
					JComboBox cbox = loginDialog.getRolleComboBox();
					String rolleName = (String)cbox.getSelectedItem();
					String user = loginDialog.getUsernameTextField().getText();
					char[] password = loginDialog.getPasswordField().getPassword();
					// hide me
					loginDialog.setVisible(false);
					// try to login
					app.login(rolleName, user, password);
				}
				public void onWorkChange(DiskoWorkEvent e) {
					// Not in use
				}
			});
		}
		String[] rolleNames = null;
		try {
			rolleNames = app.getDiskoModuleLoader().getRoleTitles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JComboBox cbox = loginDialog.getRolleComboBox();
		cbox.removeAllItems();
		for (int i = 0; i < rolleNames.length; i++) {
			IDiskoRole currentRolle = app.getCurrentRole();
			if (currentRolle != null && 
					currentRolle.getTitle().equals(rolleNames[i])) {
				// skip current rolle
				continue;
			}
			cbox.addItem(rolleNames[i]);
		}
		return loginDialog;
	}
	
	public MapOptionDialog getMapOptionDialog(){
		if (mapOptionDialog == null) {
			mapOptionDialog = new MapOptionDialog(app);
		}
		
		mapOptionDialog.setLocation(200, 200);
		return mapOptionDialog;
	}
	
	public TaskDialog getTaskDialog(){
		if(taskDialog == null){
			taskDialog = new TaskDialog(app);
		}
		return taskDialog;
	}
	
	
	
	/**
	 * This method initializes glassPane	
	 * 	
	 * @return org.redcross.sar.gui.DiskoGlassPane
	 */
	public DiskoGlassPane getGlassPane() {
		if (glassPane == null) {
			try {
				glassPane = new DiskoGlassPane(app.getFrame());
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return glassPane;
	}
	
	/**
	 * This method initializes contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new JPanel();
				contentPanel.setPreferredSize(new Dimension(1024,764));
				contentPanel.setLayout(new BorderLayout());
				contentPanel.add(getMainPanel(), BorderLayout.CENTER);
				contentPanel.add(getMenuPanel(), BorderLayout.EAST);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}
	
	public JPanel getMenuPanel() {
		if (menuPanel == null) {
			menuPanel = new JPanel();
			menuPanel.setLayout(new BorderLayout());
			menuPanel.add(getSubMenuPanel(), BorderLayout.WEST);
			menuPanel.add(getMainMenuPanel(), BorderLayout.EAST);
			menuPanel.setVisible(false);
			//menuPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
		}
		return menuPanel;
	}
	
	public MainMenuPanel getMainMenuPanel() {
		if (mainMenuPanel == null) {
			mainMenuPanel = new MainMenuPanel(app);
		}
		return mainMenuPanel;
	}
	
	public SubMenuPanel getSubMenuPanel() {
		if (subMenuPanel == null) {
			subMenuPanel = new SubMenuPanel(app);
		}
		return subMenuPanel;
	}
	
	public MainPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new MainPanel(app);
		}
		return mainPanel;
	}
	
	/**
	 * Gets the default size of large buttons use in the Disko application
	 * @return
	 */
	public Dimension getLargeButtonSize() {
		if (largeButtonSize == null) {
			int width  = 50; // default width
			int height = 50; // default height
			String widthProp  = app.getProperty("Button.Large.width");
			String heightProp = app.getProperty("Button.Large.height");
			if (widthProp != null && heightProp != null) {
				try {
					width  = Integer.parseInt(widthProp);
					height = Integer.parseInt(heightProp);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			largeButtonSize = new Dimension(width, height);
		}
		return largeButtonSize;
	}	

}
