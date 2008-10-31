package org.redcross.sar.gui.menu;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;

import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.IDiskoWp;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;

import com.borland.jbcl.layout.VerticalFlowLayout;

public class SubMenu extends JPanel {

	private static final long serialVersionUID = 1L;

	private JPanel menuPanel;
	private JButton rollbackButton;
	private JButton commitButton;
	private JPanel rollbackPanel;
	private JPanel commitPanel;

	private boolean isTransactionMode;

	private final ButtonGroup bgroup = new ButtonGroup();
	private final EventListenerList listeners = new EventListenerList();
	private final Hashtable<String, JPanel> panels = new Hashtable<String, JPanel>();

	public SubMenu() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getRollbackPanel(), BorderLayout.NORTH);
		this.add(getMenuPanel(), BorderLayout.CENTER);
		this.add(getCommitPanel(), BorderLayout.SOUTH);
		Dimension size = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL);
		this.setPreferredSize(size);
	}

	public void setColored(boolean isColored) {
		((DiskoIcon)getRollbackButton().getIcon()).setColored(isColored);
		getRollbackButton().repaint();
		((DiskoIcon)getCommitButton().getIcon()).setColored(isColored);
		getCommitButton().repaint();
	}

	public boolean isTransactionMode() {
		return isTransactionMode;
	}

	public boolean setTransactionMode(boolean isTransactionMode) {
		boolean bFlag = this.isTransactionMode;
		this.isTransactionMode = isTransactionMode;
		getRollbackButton().setVisible(isTransactionMode);
		getCommitButton().setVisible(isTransactionMode);
		return bFlag;
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
		button.addActionListener(actionRepeater);
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

	private JPanel getRollbackPanel() {
		if(rollbackPanel==null) {
			rollbackPanel = new JPanel(new BorderLayout(0,0));
			rollbackPanel.add(getRollbackButton(),BorderLayout.CENTER);
			Dimension d = getRollbackButton().getPreferredSize();
			Utils.setFixedSize(rollbackPanel, d.width, d.height);
		}
		return rollbackPanel;
	}

	private JButton getRollbackButton() {
		if (rollbackButton == null) {
			rollbackButton = DiskoButtonFactory.createButton("SYSTEM.ROLLBACK",ButtonSize.NORMAL);
			rollbackButton.setIcon(new DiskoIcon(rollbackButton.getIcon(),Color.RED,0.4f));
			rollbackButton.setActionCommand("SYSTEM.ROLLBACK");
			rollbackButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					IDiskoWp wp = (IDiskoWp)Utils.getApp().getCurrentRole().getCurrentDiskoWpModule();
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
			rollbackButton.addActionListener(actionRepeater);
		}
		return rollbackButton;
	}

	private JPanel getCommitPanel() {
		if(commitPanel==null) {
			commitPanel = new JPanel(new BorderLayout(0,0));
			commitPanel.add(getCommitButton(),BorderLayout.CENTER);
			Dimension d = getCommitButton().getPreferredSize();
			Utils.setFixedSize(commitPanel, d.width, d.height);
		}
		return commitPanel;
	}

	/**
	 * This method initializes sysToggleButton
	 *
	 * @return javax.swing.JToggleButton
	 */
	private JButton getCommitButton() {
		if (commitButton == null) {
			commitButton = DiskoButtonFactory.createButton("SYSTEM.COMMIT",ButtonSize.NORMAL);
			commitButton.setIcon(new DiskoIcon(commitButton.getIcon(),Color.GREEN,0.4f));
			commitButton.setActionCommand("SYSTEM.COMMIT");
			commitButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					IDiskoWp wp = (IDiskoWp)Utils.getApp().getCurrentRole().getCurrentDiskoWpModule();
		    		// forward
					wp.commit();
				}
			});
			commitButton.addActionListener(actionRepeater);
		}
		return commitButton;
	}

	public void addActionListener(ActionListener listener) {
		listeners.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.remove(ActionListener.class, listener);
	}

	protected void fireAction(ActionEvent e) {
		ActionListener[] list = listeners.getListeners(ActionListener.class);
		for(int i=0;i<list.length;i++) {
			list[i].actionPerformed(e);
		}
	}

	/**
	 * Save current system and navigation button states
	 *
	 * @return State
	 */
	public State save() {
		return new State(this);
	}

	/**
	 * Load saved system and navigation button states
	 *
	 * @return State
	 */
	public void load(State state) {
		if(state!=null) {
			state.load(this);
		}
	}

	public class State {

		private boolean isTransactionMode;

		private State(SubMenu bar) {
			// forward
			save(bar);
		}

		private void save(SubMenu bar) {
			// save states
			isTransactionMode = bar.isTransactionMode;
		}

		private void load(SubMenu bar) {
			// load states
			bar.setTransactionMode(isTransactionMode);
		}

	};

	private final ActionListener actionRepeater = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// forward
			fireAction(e);
		}

	};


}  //  @jve:decl-index=0:visual-constraint="10,10"
