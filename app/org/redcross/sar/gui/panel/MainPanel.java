package org.redcross.sar.gui.panel;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.redcross.sar.gui.menu.NavMenu;
import org.redcross.sar.gui.menu.SysMenu;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.Hashtable;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JPanel centerPanel;
	private NavMenu navMenu;
	private SysMenu sysMenu;

	private final Hashtable<String, JComponent> components = new Hashtable<String, JComponent>();

	public MainPanel(NavMenu navMenu, SysMenu sysMenu) {
		// prepare
		this.navMenu = navMenu;
		this.sysMenu = sysMenu;
		// initialize GUI
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getCenterPanel(), BorderLayout.CENTER);
		this.add(getNavMenu(), BorderLayout.NORTH);
		this.add(getSysMenu(), BorderLayout.SOUTH);
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
			// component is already added
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

	private NavMenu getNavMenu() {
		return navMenu;
	}

	private SysMenu getSysMenu() {
		return sysMenu;
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
