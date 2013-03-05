package org.redcross.sar.gui.menu;

import org.redcross.sar.Application;
import org.redcross.sar.gui.dialog.MapOptionDialog;
import org.redcross.sar.gui.dialog.ServiceManagerDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonPlacement;
import org.redcross.sar.gui.UIConstants.ButtonSize;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;


public class SysMenu extends JPanel {

	private static final long serialVersionUID = 1L;

	private UIFactory factory;

	private JPanel leftPanel ;
	private JPanel rightPanel;
	
	private JToggleButton menuToggleButton;

	private JButton swapToButton;
	private JButton mapOptionButton;
	private JButton newOpButton;
	private JButton shutdownButton;
	private JButton mergeButton;
	private JButton chooseOperationButton;
	private JButton networkButton;

	private final ButtonGroup bgroup = new ButtonGroup();
	private final EventListenerList listeners = new EventListenerList();
	private final Map<AbstractButton,JPanel> buttonMap = new HashMap<AbstractButton, JPanel>();

	public SysMenu(UIFactory factory, MainMenu mainMenu) {
		// prepare
		this.factory = factory;
		this.menuToggleButton = mainMenu.getSysToggleButton();
		// initialize GUI
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		
		// defaults
		setVisible(false);
		
		// add to layout
		setLayout(new BorderLayout());
		add(getLeftPanel(), BorderLayout.WEST);
		add(getRightPanel(), BorderLayout.EAST);
		
		// add default buttons
		addButton(getShutdownButton(),ButtonPlacement.LEFT);
		addButton(getSwapToButton(),ButtonPlacement.RIGHT);
		addButton(getMapOptionButton(),ButtonPlacement.RIGHT);
		addButton(getNewOpButton(),ButtonPlacement.RIGHT);
		addButton(getMergeButton(),ButtonPlacement.RIGHT);
		addButton(getChooseOperationButton(),ButtonPlacement.RIGHT);
		addButton(getNetworkButton(),ButtonPlacement.RIGHT);
		
	}
	
	private JPanel getLeftPanel() {
		if (leftPanel == null) {
			try {
				leftPanel = new JPanel();
				FlowLayout fl = new FlowLayout();
				fl.setHgap(0);
				fl.setVgap(0);
				fl.setAlignment(FlowLayout.LEFT);
				leftPanel.setLayout(fl);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return leftPanel;
	}

	private JPanel getRightPanel() {
		if (rightPanel == null) {
			try {
				rightPanel = new JPanel();
				FlowLayout fl = new FlowLayout();
				fl.setHgap(0);
				fl.setVgap(0);
				fl.setAlignment(FlowLayout.RIGHT);
				rightPanel.setLayout(fl);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return rightPanel;
	}
	

	private JButton getNewOpButton()
	{
		if (newOpButton == null) {
			try {
				newOpButton = DiskoButtonFactory.createButton("SYSTEM.CREATE",ButtonSize.NORMAL);
				newOpButton.setActionCommand("SYSTEM.CREATE");
				newOpButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						Application.getInstance().createOperation(true);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return newOpButton;
	}

	private JButton getMergeButton()
	{
		if (mergeButton == null) {
			try {
				mergeButton = DiskoButtonFactory.createButton("SYSTEM.MERGE",ButtonSize.NORMAL);
				mergeButton.setActionCommand("SYSTEM.MERGE");
				mergeButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						Application.getInstance().mergeOperations();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return mergeButton;
	}

	private JButton getShutdownButton()
	{
		if (shutdownButton == null) {
			try {
				shutdownButton = DiskoButtonFactory.createButton("SYSTEM.TERMINATE",ButtonSize.NORMAL);
				shutdownButton.setActionCommand("SYSTEM.TERMINATE");
				shutdownButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						Application.getInstance().finishOperation();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return shutdownButton;
	}

	private JButton getChooseOperationButton()
	{
		if (chooseOperationButton == null) {
			try {
				chooseOperationButton = DiskoButtonFactory.createButton("SYSTEM.SELECT",ButtonSize.NORMAL);
				chooseOperationButton.setActionCommand("SYSTEM.SELECT");
				chooseOperationButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						Application.getInstance().selectActiveOperation(true);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return chooseOperationButton;
	}

	private JButton getSwapToButton() {
		if (swapToButton == null) {
			try {
				swapToButton = DiskoButtonFactory.createButton("SYSTEM.SWAPTO",ButtonSize.NORMAL);
				swapToButton.setActionCommand("SYSTEM.SWAPTO");
				swapToButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						Application.getInstance().getUIFactory().getLoginDialog().showSwapTo();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return swapToButton;
	}

	private JButton getMapOptionButton() {

		if (mapOptionButton == null) {
			try {
				mapOptionButton = DiskoButtonFactory.createButton("SYSTEM.MAP",ButtonSize.NORMAL);
				mapOptionButton.setActionCommand("SYSTEM.MAP");
				mapOptionButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						MapOptionDialog dialog = factory.getMapOptionDialog();
						dialog.selectMap("Velg kart", Application.getInstance().getMapManager().getMapInfoList());
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return mapOptionButton;
	}

	private JButton getNetworkButton()
	{
		if (networkButton == null) {
			try {
				networkButton = DiskoButtonFactory.createButton("SYSTEM.SERVICES",ButtonSize.NORMAL);
				networkButton.setActionCommand("SYSTEM.SERVICES");
				networkButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						ServiceManagerDialog dialog = factory.getServiceManagerDialog();
						dialog.manage();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return networkButton;
	}	

	public boolean addButton(AbstractButton button, ButtonPlacement buttonPlacement) {
		if(!buttonMap.containsKey(button)) {
			if (buttonPlacement == ButtonPlacement.LEFT) {
				getLeftPanel().add(button);
				buttonMap.put(button,getLeftPanel());
			} else {
				getRightPanel().add(button);
				buttonMap.put(button,getRightPanel());
			}
			if (button instanceof JToggleButton) {
				bgroup.add(button);
			}
			button.addActionListener(actionRepeater);
			return true;
		}
		return false;
	}

	public boolean removeButton(AbstractButton button) {
		JPanel panel = buttonMap.get(button);
		if(panel!=null) {
			panel.remove(button);
			if (button instanceof JToggleButton) {
				bgroup.remove(button);
			}
			button.removeActionListener(actionRepeater);
			return true;
		}
		return false;
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

	@Override
	public void setVisible(boolean isVisible) {
		// select button
		menuToggleButton.setSelected(isVisible);
		// forward
		super.setVisible(isVisible);
	}

	private final ActionListener actionRepeater = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// forward
			fireAction(e);
		}

	};

}
