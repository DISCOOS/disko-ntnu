package org.redcross.sar.gui;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.util.Internationalization;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;


public class SysBar extends JPanel {

    public final static String bundleName = "org.redcross.sar.gui.properties.SysBar";
private static final ResourceBundle bundle = Internationalization.getBundle(SysBar.class);
   private static final long serialVersionUID = 1L;
	private IDiskoApplication app = null;
	private ButtonGroup bgroup = null;
	private JButton changeRolleButton = null;
	private JButton mapOptionButton = null;
    private JButton newOpButton = null;
   private JButton finishOperationButton=null;
   private JButton mergeButton=null;
   private JButton chooseOperationButton=null;
   private static final String MERGETEXT = "MERGE.TEXT";
   private static final String FINISHTEXT = "FINISH.TEXT";
   private static final String CHOOSETEXT = "CHOOSE.OP.TEXT";
   private static final String NEWOPTEXT = "NEW.OP.TEXT";
   private static final String SWITCHTEXT = "SWITCH.ROLE.TEXT";
   private static final String MAPTEXT = "MAP.TEXT";


   /**
	 * This is the default constructor
	 */
	public SysBar() {
		super(null);
	}

	public SysBar(IDiskoApplication app) {
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
		addButton(getChangeRolleButton());
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
            newOpButton = new JButton();
            String iconName = "NewOperation.icon";
            Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
            newOpButton.setIcon(icon);
            Dimension size = app.getUIFactory().getSmallButtonSize();
            newOpButton.setPreferredSize(size);
            newOpButton.setMaximumSize(size);
            newOpButton.setIcon(icon);
            //newOpButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            //newOpButton.setHorizontalTextPosition(SwingConstants.CENTER);
            newOpButton.setToolTipText(bundle.getString(NEWOPTEXT));
            //newOpButton.setText(bundle.getString(NEWOPTEXT));
            newOpButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  app.newOperation();
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
            mergeButton = new JButton();
            String iconName = "Merge.icon";
            Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
            mergeButton.setIcon(icon);
            Dimension size = app.getUIFactory().getSmallButtonSize();
            mergeButton.setPreferredSize(size);
            mergeButton.setMaximumSize(size);
            mergeButton.setIcon(icon);
            //mergeButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            //mergeButton.setHorizontalTextPosition(SwingConstants.CENTER);
            mergeButton.setToolTipText(bundle.getString(MERGETEXT));
            //mergeButton.setText(bundle.getString(MERGETEXT));
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
            finishOperationButton = new JButton();
            String iconName = "FinishOperation.icon";
            Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
            finishOperationButton.setIcon(icon);
            Dimension size = app.getUIFactory().getSmallButtonSize();
            finishOperationButton.setPreferredSize(size);
            finishOperationButton.setMaximumSize(size);
            finishOperationButton.setIcon(icon);
            //finishOperationButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            //finishOperationButton.setHorizontalTextPosition(SwingConstants.CENTER);
            finishOperationButton.setToolTipText(bundle.getString(FINISHTEXT));
            //finishOperationButton.setText(bundle.getString(FINISHTEXT));
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
            chooseOperationButton = new JButton();
            String iconName = "SelectOperation.icon";
            Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
            chooseOperationButton.setIcon(icon);
            Dimension size = app.getUIFactory().getSmallButtonSize();
            chooseOperationButton.setPreferredSize(size);
            chooseOperationButton.setMaximumSize(size);
            chooseOperationButton.setIcon(icon);
            //chooseOperationButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            //chooseOperationButton.setHorizontalTextPosition(SwingConstants.CENTER);
            chooseOperationButton.setToolTipText(bundle.getString(CHOOSETEXT));
            //chooseOperationButton.setText(bundle.getString(CHOOSETEXT));
            chooseOperationButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                  app.chooseActiveOperation(true);
               }
            });
         } catch (java.lang.Throwable e) {
            e.printStackTrace();
         }
      }
      return chooseOperationButton;
   }

   private JButton getChangeRolleButton() {
		if (changeRolleButton == null) {
			try {
				changeRolleButton = new JButton();
				String iconName = "SwitchRole.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				changeRolleButton.setIcon(icon);
				Dimension size = app.getUIFactory().getSmallButtonSize();
				changeRolleButton.setPreferredSize(size);
				changeRolleButton.setIcon(icon);
				//changeRolleButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				//changeRolleButton.setHorizontalTextPosition(SwingConstants.CENTER);
				changeRolleButton.setToolTipText(bundle.getString(SWITCHTEXT));
				//changeRolleButton.setText(bundle.getString(SWITCHTEXT));				
				changeRolleButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						LoginDialog loginDialog = app.getUIFactory().getLoginDialog();
						loginDialog.setVisible(true,false);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return changeRolleButton;
	}
	
	private JButton getMapOptionButton() {
		
		if (mapOptionButton == null) {
			try {
				mapOptionButton = new JButton();
				String iconName = "Kart.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				mapOptionButton.setIcon(icon);
				Dimension size = app.getUIFactory().getSmallButtonSize();
				mapOptionButton.setPreferredSize(size);
				mapOptionButton.setIcon(icon);
				//mapOptionButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				//mapOptionButton.setHorizontalTextPosition(SwingConstants.CENTER);
				mapOptionButton.setToolTipText(bundle.getString(MAPTEXT));
				//mapOptionButton.setText(bundle.getString(MAPTEXT));				
				mapOptionButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						MapOptionDialog mapOptionDialog = app.getUIFactory().getMapOptionDialog();
						mapOptionDialog.setVisible(true);
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
