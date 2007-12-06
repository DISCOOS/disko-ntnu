package org.redcross.sar.gui;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.redcross.sar.app.IDiskoApplication;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.Hashtable;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private IDiskoApplication app = null;
	private JPanel centerPanel = null;
	private NavBar navBar = null;
	private SysBar sysBar = null;
	private Hashtable<String, JComponent> components = null;
	
	/**
	 * This is the default constructor
	 */
	public MainPanel() {
		super(null);
	}
	
	public MainPanel(IDiskoApplication app) {
		super();
		this.app = app;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		components = new Hashtable<String, JComponent>();
		this.setLayout(new BorderLayout());
		this.add(getCenterPanel(), BorderLayout.CENTER);
		this.add(getNavBar(), BorderLayout.NORTH);
		this.add(getSysBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Add a new component to this MainPanel. The component will added to a panel
	 * in the CardLayout with the given name. If a component with the given
	 * name allready exists, false will be returned, true otherwise.
	 * 
	 * @param component The component to add
	 * @param compName A name to identify a component in the CardLayout
	 * @return false if a component with the given name allready exists, 
	 * false will be returned, true otherwise
	 */
	public boolean addComponent(JComponent component, String compName) {
		if (components.get(compName) != null) {
			// component is allready added
			return false;
		}
		getCenterPanel().add(component, compName);
		components.put(compName, component);
		return true;
	}
	
	/**
	 * Show a component in the CardLyout with the given name.
	 * @param compName The name of the component.
	 */
	public void showComponent(String compName) {
		CardLayout cl = (CardLayout)getCenterPanel().getLayout();
		cl.show(getCenterPanel(), compName);
	}

	/**
	 * This method initializes menuPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getCenterPanel() {
		if (centerPanel == null) {
			try {
				centerPanel = new JPanel();
				centerPanel.setLayout(new CardLayout());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return centerPanel;
	}

	
	public NavBar getNavBar() {
		if (navBar == null) {
			navBar = new NavBar(app);
			navBar.setVisible(false);
		}
		return navBar;
	}

	public SysBar getSysBar() {
		if (sysBar == null) {
			sysBar = new SysBar(app);
			sysBar.setVisible(false);
		}
		return sysBar;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
