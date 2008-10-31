package org.redcross.sar.gui.menu;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import com.borland.jbcl.layout.VerticalFlowLayout;

import javax.swing.BorderFactory;
import javax.swing.event.EventListenerList;

import org.redcross.sar.gui.ButtonState;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class MainMenu extends JPanel {

	private static final long serialVersionUID = 1L;

	private UIFactory factory;

	private JPanel menuPanel;
	private JToggleButton navToggleButton;
	private JToggleButton sysToggleButton;

	private final ButtonGroup bgroup = new ButtonGroup();
	private final EventListenerList listeners = new EventListenerList();
	private final Hashtable<String, JPanel> panels = new Hashtable<String, JPanel>();

	public MainMenu(UIFactory factory) {
		// prepare
		this.factory = factory;
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
	public void addItem(AbstractButton button, DiskoIcon icon, String menuName) {
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
		button.addActionListener(actionRepeater);
		button.setIcon(icon);
		((DiskoIcon)button.getIcon()).setColored(false);
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

	public void setColored(String menuName, int index, boolean isColored) {
		AbstractButton button = getButton(menuName,index);
		if(button.getIcon() instanceof DiskoIcon) {
			((DiskoIcon)button.getIcon()).setColored(isColored);
			button.repaint();
		}
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

	protected JToggleButton getNavToggleButton() {
		if (navToggleButton == null) {
			// get NAV button
			navToggleButton = DiskoButtonFactory.createToggleButton(
					"SYSTEM.NAV", ButtonSize.NORMAL, 0, 0,null);
			navToggleButton.setActionCommand("SYSTEM.NAV");
			navToggleButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (navToggleButton.isSelected()) {
						factory.setNavMenuVisible(true);
					} else {
						factory.setNavMenuVisible(false);
					}
				}
			});
			navToggleButton.addActionListener(actionRepeater);
		}
		return navToggleButton;
	}

	/**
	 * This method initializes sysToggleButton
	 *
	 * @return javax.swing.JToggleButton
	 */
	protected JToggleButton getSysToggleButton() {
		if (sysToggleButton == null) {
			sysToggleButton = DiskoButtonFactory.createToggleButton(
					"SYSTEM.SYS", ButtonSize.NORMAL);
			sysToggleButton.setActionCommand("SYSTEM.SYS");
			sysToggleButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (sysToggleButton.isSelected()) {
						factory.setSysMenuVisible(true);
					} else {
						factory.setSysMenuVisible(false);
					}
				}
			});
			sysToggleButton.addActionListener(actionRepeater);
		}
		return sysToggleButton;
	}

	public class State {

		private ButtonState m_navState;
		private ButtonState m_sysState;

		private State(MainMenu bar) {
			// forward
			save(bar);
		}

		private void save(MainMenu bar) {
			// save states
			m_navState = new ButtonState("NavButton",bar.getNavToggleButton());
			m_sysState = new ButtonState("SysButton",bar.getSysToggleButton());
		}

		private void load(MainMenu bar) {
			// load states
			m_navState.load(bar.getNavToggleButton());
			m_sysState.load(bar.getSysToggleButton());
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
