package org.redcross.sar.gui.menu;

import org.redcross.sar.gui.dialog.MapOptionDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.util.Utils;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;


public class SysMenu extends JPanel {

	private static final long serialVersionUID = 1L;

	private UIFactory factory;

	/**
	 * Button that controls this menu visible state
	 */
	private JToggleButton menuToggleButton;

	private JButton swapToButton;
	private JButton mapOptionButton;
    private JButton newOpButton;
    private JButton finishOperationButton;
    private JButton mergeButton;
    private JButton chooseOperationButton;

	private final ButtonGroup bgroup = new ButtonGroup();
	private final EventListenerList listeners = new EventListenerList();

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
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setHgap(0);
		flowLayout.setVgap(0);
		flowLayout.setAlignment(FlowLayout.RIGHT);
		setLayout(flowLayout);
		addButton(getSwapToButton());
		addButton(getMapOptionButton());
		addButton(getFinishOperationButton());
        addButton(getNewOpButton());
        addButton(getMergeButton());
        addButton(getChooseOperationButton());
   }

   private JButton getNewOpButton()
   {
      if (newOpButton == null) {
         try {
            newOpButton = DiskoButtonFactory.createButton("SYSTEM.CREATE",ButtonSize.NORMAL);
            newOpButton.setActionCommand("SYSTEM.CREATE");
            newOpButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  Utils.getApp().createOperation(true);
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
                  Utils.getApp().mergeOperations();
               }
            });
         } catch (java.lang.Throwable e) {
            e.printStackTrace();
         }
      }
      return mergeButton;
   }

   private JButton getFinishOperationButton()
   {
      if (finishOperationButton == null) {
         try {
            finishOperationButton = DiskoButtonFactory.createButton("SYSTEM.TERMINATE",ButtonSize.NORMAL);
            finishOperationButton.setActionCommand("SYSTEM.TERMINATE");
            finishOperationButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  Utils.getApp().finishOperation();
               }
            });
         } catch (java.lang.Throwable e) {
            e.printStackTrace();
         }
      }
      return finishOperationButton;
   }

   private JButton getChooseOperationButton()
   {
      if (chooseOperationButton == null) {
         try {
            chooseOperationButton = DiskoButtonFactory.createButton("SYSTEM.SELECT",ButtonSize.NORMAL);
            chooseOperationButton.setActionCommand("SYSTEM.SELECT");
            chooseOperationButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  Utils.getApp().selectActiveOperation(true);
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
						Utils.getApp().getUIFactory().getLoginDialog().showSwapTo();
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
						dialog.selectMap("Velg kart", Utils.getApp().getMapManager().getMapInfoList());
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return mapOptionButton;
	}

	public void addButton(AbstractButton button) {
		add(button);
		if (button instanceof JToggleButton) {
			bgroup.add(button);
		}
		button.addActionListener(actionRepeater);
	}

	public void removeButton(AbstractButton button) {
		remove(button);
		if (button instanceof JToggleButton) {
			bgroup.remove(button);
		}
		button.removeActionListener(actionRepeater);
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
