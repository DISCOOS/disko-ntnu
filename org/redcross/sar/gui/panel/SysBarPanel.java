package org.redcross.sar.gui.panel;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.gui.dialog.MapOptionDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

import java.awt.FlowLayout;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;


public class SysBarPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private IDiskoApplication app = null;
	private ButtonGroup bgroup = null;
	private JButton swapToButton = null;
	private JButton mapOptionButton = null;
    private JButton newOpButton = null;
    private JButton finishOperationButton=null;
    private JButton mergeButton=null;
    private JButton chooseOperationButton=null;



   /**
	 * This is the default constructor
	 */
	public SysBarPanel() {
		super(null);
	}

	public SysBarPanel(IDiskoApplication app) {
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
		bgroup = new ButtonGroup();
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setHgap(0);
		flowLayout.setVgap(0);
		flowLayout.setAlignment(FlowLayout.RIGHT);
		this.setLayout(flowLayout);
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
            newOpButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  app.createOperation();
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
            mergeButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  app.mergeOperations();
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
            finishOperationButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  app.finishOperation();
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
            chooseOperationButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  app.selectActiveOperation(true);
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
				swapToButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						app.getUIFactory().getLoginDialog().showSwapTo();
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
				mapOptionButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						MapOptionDialog dialog = app.getUIFactory().getMapOptionDialog();
						dialog.selectMap("Velg kart", app.getMapManager().getMapInfoList());
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
	}
	
	public void removeButton(AbstractButton button) {
		remove(button);
		if (button instanceof JToggleButton) {
			bgroup.remove(button);
		}
	}
}
