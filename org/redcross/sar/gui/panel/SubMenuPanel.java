package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.wp.IDiskoWp;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;

import com.borland.jbcl.layout.VerticalFlowLayout;

public class SubMenuPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private IDiskoApplication app = null;
	private JPanel menuPanel = null;
	private ButtonGroup bgroup = new ButtonGroup();  //  @jve:decl-index=0:
	private Hashtable<String, JPanel> panels = null;
	private JButton rollbackButton = null;
	private JButton commitButton = null;
	/**
	 * This is the default constructor
	 */
	public SubMenuPanel() {
		this(null);
	}
	
	public SubMenuPanel(IDiskoApplication app) {
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
		this.add(getRollbackButton(), BorderLayout.NORTH);
		this.add(getMenuPanel(), BorderLayout.CENTER);
		this.add(getCommitButton(), BorderLayout.SOUTH);
		Dimension size = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL);
		this.setPreferredSize(size);
	}
	
	/**
	 * Add a new Button to this MainMenuPanel. The button will added to a panel
	 * in the CardLayout with the given name. If this panel does not exists, 
	 * a new panel will be created.
	 * 
	 * @param button The button to add
	 * @param menuName A name to identify a panel (menu) in the CardLayout
	 */
	public void addItem(AbstractButton button, String menuName, boolean addToGroup) {
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
		if (button instanceof JToggleButton && addToGroup) {
			bgroup.add(button);
		}
	}
	
	public void addItem(AbstractButton button, String menuName) {
		addItem(button,menuName, false);
	}
	
	/**
	 * Show a panel in the CardLayout with the given name.
	 * @param menuName The name of the panel (menu).
	 */
	public void showMenu(String menuName) {
		CardLayout cl = (CardLayout)getMenuPanel().getLayout();
		cl.show(getMenuPanel(), menuName);
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
	
	public JButton getRollbackButton() {
		if (rollbackButton == null) {
			try {
				rollbackButton = DiskoButtonFactory.createButton("SYSTEM.ROLLBACK",ButtonSize.NORMAL);
				rollbackButton.setIcon(new DiskoIcon(rollbackButton.getIcon(),Color.RED,0.4f));
				rollbackButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						IDiskoWp wp = (IDiskoWp)app.getCurrentRole().getCurrentDiskoWpModule();
			    		// forward
						boolean isCanceled = wp.rollback();
						// forward to draw adapter?
						if(isCanceled && wp.isMapInstalled()) {
							// get map
							IDiskoMap map = wp.getMap();
							if(map.isEditSupportInstalled()) {
								map.getDrawAdapter().cancel();
							}							
						}
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return rollbackButton;
	}

	/**
	 * This method initializes sysToggleButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	public JButton getCommitButton() {
		if (commitButton == null) {
			try {
				commitButton = DiskoButtonFactory.createButton("SYSTEM.COMMIT",ButtonSize.NORMAL);
				commitButton.setIcon(new DiskoIcon(commitButton.getIcon(),Color.GREEN,0.4f));
				commitButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						IDiskoWp wp = (IDiskoWp)app.getCurrentRole().getCurrentDiskoWpModule();
			    		// forward
						wp.commit();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return commitButton;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
