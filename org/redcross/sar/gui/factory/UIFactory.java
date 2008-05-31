package org.redcross.sar.gui.factory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.gui.dialog.LoginDialog;
import org.redcross.sar.gui.dialog.MapOptionDialog;
import org.redcross.sar.gui.dialog.NumPadDialog;
import org.redcross.sar.gui.dialog.OperationDialog;
import org.redcross.sar.gui.dialog.TaskDialog;
import org.redcross.sar.gui.panel.MainMenuPanel;
import org.redcross.sar.gui.panel.MainPanel;
import org.redcross.sar.gui.panel.SubMenuPanel;

public class UIFactory {
	
	private IDiskoApplication app = null;
	private JPanel contentPanel = null;
	private DiskoGlassPane glassPane = null;
	private MainMenuPanel mainMenuPanel = null;
	private SubMenuPanel subMenuPanel = null;
	private MainPanel mainPanel = null;
	private JPanel menuPanel = null;
	private LoginDialog loginDialog = null;
	private OperationDialog operationDialog = null;
	private NumPadDialog numPadDialog = null;
	private MapOptionDialog mapOptionDialog = null;
	private TaskDialog taskDialog = null;
	
	private final List<Component> components = new ArrayList<Component>(); 
	private final Map<Component,Boolean> states = new HashMap<Component,Boolean>(); 
	
	public UIFactory(IDiskoApplication app) {
		// prepare
		this.app = app;		
		// initialize content panel
		getContentPanel();
		// hide this
		hideAll();
	}
	
	public NumPadDialog getNumPadDialog(){
		if (numPadDialog == null) {
			numPadDialog = new NumPadDialog(app.getFrame());
			register(numPadDialog);
		}
		return numPadDialog;						
	}
	
	public LoginDialog getLoginDialog() {
		if (loginDialog == null) {
			loginDialog = new LoginDialog(app.getFrame());
			loginDialog.getUserName().setValue("disko");
			loginDialog.getPassword().setValue("disko");
			loginDialog.setLocationRelativeTo(app.getFrame(),LoginDialog.POS_CENTER,false,true);
			register(loginDialog);
		}
		loginDialog.load();
		return loginDialog;
	}
	
	public OperationDialog getOperationDialog() {
		if (operationDialog == null) {
			operationDialog= new OperationDialog(app.getFrame());
			operationDialog.setLocationRelativeTo(app.getFrame(),OperationDialog.POS_CENTER,false,true);
			register(operationDialog);
		}
		operationDialog.load();
		return operationDialog;
	}
	
	public MapOptionDialog getMapOptionDialog(){
		if (mapOptionDialog == null) {
			mapOptionDialog = new MapOptionDialog(app);
			register(mapOptionDialog);
		}
		mapOptionDialog.setLocation(200, 200);
		return mapOptionDialog;
	}
	
	public TaskDialog getTaskDialog(){
		if(taskDialog == null || true){
			taskDialog = new TaskDialog(app.getFrame());
			register(taskDialog);
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
	
	public void hideAll() {
		getContentPanel().setVisible(false);
		for(Component c: components) {
			states.put(c, c.isVisible());
			c.setVisible(false);
		}
	}
	
	public void showAgain() {
		getContentPanel().setVisible(true);
		for(Component c: components) {
			c.setVisible(states.get(c));
		}		
	}
	
	private void register(Component c) {
		components.add(c);
		states.put(c, c.isVisible());
	}

}
