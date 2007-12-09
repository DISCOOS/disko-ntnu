package org.redcross.sar.gui;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.Hashtable;
import com.borland.jbcl.layout.VerticalFlowLayout;


import javax.swing.BorderFactory;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;

public class MainMenuPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private IDiskoApplication app = null;
	private JPanel menuPanel = null;
	private ButtonGroup bgroup = new ButtonGroup();  //  @jve:decl-index=0:
	private Hashtable<String, JPanel> panels = null;
	private JToggleButton navToggleButton = null;
	private JToggleButton sysToggleButton = null;
	/**
	 * This is the default constructor
	 */
	public MainMenuPanel() {
		this(null);
	}
	
	public MainMenuPanel(IDiskoApplication app) {
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
		panels = new Hashtable<String, JPanel>();
		this.setLayout(new BorderLayout());
		this.add(getNavToggleButton(), BorderLayout.NORTH);
		this.add(getMenuPanel(), BorderLayout.CENTER);
		this.add(getSysToggleButton(), BorderLayout.SOUTH);
	}
	
	/**
	 * Add a new Button to this MainMenuPanel. The button will added to a panel
	 * in the CardLayout with the given name. If this panel does not exists, 
	 * a new panel will be created.
	 * 
	 * @param button The button to add
	 * @param menuName A name to identify a panel (menu) in the CardLayout
	 */
	public void addItem(AbstractButton button, DiskoCustomIcon icon, String menuName) {
		JPanel panel = (JPanel)panels.get(menuName);
		if (panel == null) {
			panel = new JPanel();
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setVgap(0);
			vfl.setHgap(0);
			vfl.setAlignment(VerticalFlowLayout.TOP);
			panel.setLayout(vfl);
			getMenuPanel().add(panel, menuName);
			panels.put(menuName, panel);
		}
		panel.add(button);
		if (button instanceof JToggleButton) {
			bgroup.add(button);
		}
		button.setIcon(icon);
		((DiskoCustomIcon)button.getIcon()).setColored(false);
	}
	
	/**
	 * Show a panel in the CardLayout with the given name.
	 * @param menuName The name of the panel (menu).
	 */
	public void showMenu(String menuName) {
		CardLayout cl = (CardLayout)getMenuPanel().getLayout();
		cl.show(getMenuPanel(), menuName);
	}
	
	public AbstractButton getButton(String menuName, int index) {
		JPanel panel = (JPanel)panels.get(menuName);
		if (panel != null && index < panel.getComponentCount()) {
			return (AbstractButton)panel.getComponent(index);
		}
		return null;
	}

	/**
	 * This method initializes menuPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMenuPanel() {
		if (menuPanel == null) {
			try {
				menuPanel = new JPanel();
				menuPanel.setLayout(new CardLayout());
				menuPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return menuPanel;
	}
	
	public JToggleButton getNavToggleButton() {
		if (navToggleButton == null) {
			try {
				navToggleButton = new JToggleButton();
				String iconName = "NAV.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				navToggleButton.setIcon(icon);
				Dimension size = app.getUIFactory().getLargeButtonSize();
				navToggleButton.setPreferredSize(size);
				navToggleButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						MainPanel mainPanel = app.getUIFactory().getMainPanel();
						if (navToggleButton.isSelected()) {
							mainPanel.getNavBar().setVisible(true);
						}
						else {
							mainPanel.getNavBar().setVisible(false);
						}
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return navToggleButton;
	}

	/**
	 * This method initializes sysToggleButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	public JToggleButton getSysToggleButton() {
		if (sysToggleButton == null) {
			try {
				sysToggleButton = new JToggleButton();
				String iconName = "SYS.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				sysToggleButton.setIcon(icon);
				Dimension size = app.getUIFactory().getLargeButtonSize();
				sysToggleButton.setPreferredSize(size);
				sysToggleButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						MainPanel mainPanel = app.getUIFactory().getMainPanel();
						if (sysToggleButton.isSelected()) {
							mainPanel.getSysBar().setVisible(true);
						}
						else {
							mainPanel.getSysBar().setVisible(false);
						}
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return sysToggleButton;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
