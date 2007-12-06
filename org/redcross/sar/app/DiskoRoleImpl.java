package org.redcross.sar.app;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkEventListener;
import org.redcross.sar.gui.DiskoCustomIcon;
import org.redcross.sar.gui.MainMenuPanel;
import org.redcross.sar.gui.MainPanel;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.gui.SubMenuPanel;
import org.redcross.sar.wp.IDiskoWpModule;

import com.esri.arcgis.interop.AutomationException;


/**
 * Implements the DiskoRolle interface.
 * @author geira
 */
/**
 * @author geira
 *
 */
public class DiskoRoleImpl implements IDiskoRole, IDiskoWorkEventListener {
	
	private IDiskoApplication app = null;
	private String name = null;
	private String title = null;
	private String description = null;
	private IDiskoWpModule currentModule = null;
	private ArrayList<IDiskoWpModule> modules = null;
	//private Border bussyBorder = null;
	//private Border normalBorder = null;
	
	/**
	 * Constructs a DiskoRolleImpl
	 * @param app A reference to DiskoApplication
	 */
	public DiskoRoleImpl(IDiskoApplication app) {
		this(app, null, null, null);
	}
	
	/**
	 * Constructs a DiskoRoleImpl
	 * @param app A reference to DiskoApplication
	 * @param name The name of this 
	 * @param title The title
	 * @param description The description
	 */
	public DiskoRoleImpl(IDiskoApplication app, String name, String title, String description) {
		this.app = app;
		this.name = name;
		this.title = title;
		this.description = description;
		this.modules = new ArrayList<IDiskoWpModule>();
		//this.bussyBorder = BorderFactory.createLineBorder(Color.red, 2);
	}
	
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#addDiskoWpModule(org.redcross.sar.wp.IDiskoWpModule)
	 */
	public void addDiskoWpModule(final IDiskoWpModule module) {
		try {
			final String id = getName()+module.getName();
			module.addDiskoWorkEventListener(this);
			JToggleButton tbutton = new JToggleButton();
			//normalBorder = tbutton.getBorder();
			String iconName = module.getName()+".icon";
			String iconText = module.getName()+".text";
			Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
			tbutton.setToolTipText(app.getProperty(iconText));
			Dimension size = app.getUIFactory().getLargeButtonSize();
			tbutton.setPreferredSize(size);
			tbutton.setMaximumSize(size);
			tbutton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectDiskoWpModule(id);
				}
			});
			MainMenuPanel mainMenuPanel = app.getUIFactory().getMainMenuPanel();
			mainMenuPanel.addItem(tbutton, new DiskoCustomIcon(icon,Color.ORANGE,0.3f),getName());
			modules.add(module);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reInitWpModules() {
		for (int i = 0; i < modules.size(); i++) {
			// forward
			modules.get(i).reInitWP();
		}
	}
	
	public IDiskoWpModule getDiskoWpModule(String id) {
		for (int i = 0; i < modules.size(); i++) {
			if ((getName()+modules.get(i).getName()).equals(id)) {
				return modules.get(i);
			}
		}
		return null;
	}
	
	public IDiskoWpModule getDiskoWpModule(int index)  {
		if (index >= 0 && index < modules.size()) {
			return modules.get(index);
		}
		return null;
	}
	
	public int getDiskoWpModuleCount() {
		return modules.size();
	}
	
	public IDiskoWpModule selectDiskoWpModule(int index) {
		return selectDiskoWpModule(getDiskoWpModule(index));
	}
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#selectDiskoWpModule(java.lang.String)
	 */
	public IDiskoWpModule selectDiskoWpModule(String id)  {
		IDiskoWpModule module = getDiskoWpModule(id);
		if (module != null) {
			return selectDiskoWpModule(module);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#selectDiskoWpModule(int)
	 */
	public IDiskoWpModule selectDiskoWpModule(IDiskoWpModule module) {
		if (module != null) {
	    	
	    	// has a current module 
			if (currentModule!=null) {
				// Require current WP to confirm switch (E.g. confirm abort for uncommitted changes).
				// Override AbstractDiskoWpModule#confirmDeactivate() to define WP specific confirm
				if(!currentModule.confirmDeactivate()) {
					MainMenuPanel mainMenu = app.getUIFactory().getMainMenuPanel();
					int index =  modules.indexOf(currentModule);
					AbstractButton button = mainMenu.getButton(getName(), index);
					if (button != null) {
						button.setSelected(true);
					}
					return currentModule;
				}
				// deactiat previous module
				try {
					currentModule.getMap().setCurrentToolByRef(null,false);
				} catch (AutomationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				currentModule.deactivated();
			}
			String id = getName()+module.getName();
			MainMenuPanel mainMenu = app.getUIFactory().getMainMenuPanel();
			SubMenuPanel subMenu   = app.getUIFactory().getSubMenuPanel();
			NavBar navBar = app.getUIFactory().getMainPanel().getNavBar();
			MainPanel mainPanel = app.getUIFactory().getMainPanel();
			mainPanel.showComponent(id);
		
			if (module.hasSubMenu()) {
				subMenu.setVisible(true);
				subMenu.showMenu(id);
			}
			else {
				subMenu.setVisible(false);
			}
			navBar.hideDialogs();
			currentModule = module;
			module.activated();
			setColoredIcon(module.isChanged());
			// set the button selected in the main menu
			int index =  modules.indexOf(module);
			AbstractButton button = mainMenu.getButton(getName(), index);
			if (button != null) {
				button.setSelected(true);
			}
  		}		
		return currentModule;
	}
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#getCurrentDiskoWpModule()
	 */
	public IDiskoWpModule getCurrentDiskoWpModule() {
		return currentModule;
	}
	
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#getDiskoWpModules()
	 */
	public List getDiskoWpModules() {
		return modules;
	}
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#getName()
	 */
	public String getName() {
		return name;
	}
	
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#getTitle()
	 */
	public String getTitle() {
		return title;
	}
	
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoRole#getApplication()
	 */
	public IDiskoApplication getApplication() {
		return app;
	}

	public void onWorkCancel(DiskoWorkEvent e) {
		setColoredIcon(false);
	}

	public void onWorkFinish(DiskoWorkEvent e) {
		setColoredIcon(false);
	}

	public void onWorkChange(DiskoWorkEvent e) {
		setColoredIcon(true);
	}
	
	private void setColoredIcon(boolean show) {
		MainMenuPanel mainMenu = app.getUIFactory().getMainMenuPanel();
		SubMenuPanel subMenu = app.getUIFactory().getSubMenuPanel();
		String id = getName()+currentModule.getName();
		IDiskoWpModule module = getDiskoWpModule(id);
		if (module != null) {
			int index = modules.indexOf(module);
			AbstractButton button = mainMenu.getButton(getName() ,index);
			((DiskoCustomIcon)button.getIcon()).setColored(show);
			button.repaint();
			button = subMenu.getCancelButton();
			((DiskoCustomIcon)button.getIcon()).setColored(show);
			button.repaint();
			button = subMenu.getFinishButton();
			((DiskoCustomIcon)button.getIcon()).setColored(show);
			button.repaint();
		}
	}
}
