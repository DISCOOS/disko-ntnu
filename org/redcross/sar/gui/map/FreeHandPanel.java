package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.SnappingAdapter;
import org.redcross.sar.map.command.FreeHandTool;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;

import com.borland.jbcl.layout.VerticalFlowLayout;

public class FreeHandPanel extends DiskoPanel {

	private static final long serialVersionUID = 1L;
	private IDiskoApplication app = null;
	private FreeHandTool tool = null;
	private JPanel northPanel = null;
	private JPanel objectPanel = null;
	private JPanel minStepPanel = null;
	private JPanel maxStepPanel = null;
	private JPanel buttonPanel = null;
	private JButton snapToButton = null;
	private JButton applyButton = null;
	private JButton cancelButton = null;
	private boolean isVertical = true;
	private JLabel objectLabel = null;
	private JTextField objectText = null;
	private JLabel minStepTextLabel = null;
	private JLabel maxStepTextLabel = null;
	private JCheckBox snapToCheckBox = null;
	private JCheckBox constraintsCheckBox = null;
	private JTextField minStepText = null;
	private JTextField maxStepText = null;
	
	public FreeHandPanel(IDiskoApplication app, FreeHandTool tool, boolean isVertical) {
		
		// prepare
		this.app = app;
		this.tool = tool;
		
		// set layout information
		this.isVertical = isVertical;
		
		// initialize gui
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		// build container
		if(isVertical) {
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setAlignment(VerticalFlowLayout.LEFT);
			vfl.setHgap(0);
			vfl.setVgap(5);
			// get default panel
			JPanel panel = (JPanel)getBodyComponent();
			// remove panels from pane
			panel.removeAll();
			// update
			panel.setLayout(vfl);
			panel.add(getNorthPanel());
			panel.add(getButtonPanel());
		}
		else {
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(0);
			// get default panel
			JPanel panel = (JPanel)getBodyComponent();
			// remove panels from pane
			panel.removeAll();
			// update
			panel.setLayout(bl);
			panel.add(getNorthPanel(),BorderLayout.CENTER);
			panel.add(getButtonPanel(),BorderLayout.EAST);				
		}
	}
	
	public void cancel() {
		// forward
		tool.cancel();
	}
	
	private void snapTo() {
		// get adapter
		SnappingAdapter snapping = tool.getSnappingAdapter();
		if(snapping==null || !snapping.isSnapReady()) {
			Utils.showWarning("Du må først velge lag å snappe til");
		}
		else {
			// force update mode
			tool.setUpdateMode(true);
			tool.doSnapTo();
		}
	}
	
	private void apply() {
		// forward
		tool.apply();
	}
	
	public void setVertical(boolean isVertical) {
		// is changed?
		if(this.isVertical != isVertical) {
			this.isVertical = isVertical;
			// initialize gui
			initialize();
		}
	}
	
	public boolean isVertical() {
		return isVertical;
	}
	
	public void reset() {
		getObjectText().setText("<velg eller tegn nytt>");
		tool.cancel();
	}

	public void updateModes() {
		getSnapToCheckBox().setSelected(tool.isSnapToMode());
		getConstraintsCheckBox().setSelected(tool.isConstrainMode());
		getMinStepText().setText(String.valueOf(tool.getMinStep()));
		getMaxStepText().setText(String.valueOf(tool.getMaxStep()));
	}
		
	public boolean isButtonsVisible() {
		return getButtonPanel().isVisible();
	}
	
	public void setButtonsVisible(boolean isVisible) {
		getButtonPanel().setVisible(isVisible);
	}
	
	/**
	 * This method initializes northPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getNorthPanel() {
		if (northPanel == null) {
			try {
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setVgap(5);
				vfl.setHgap(0);
				vfl.setAlignment(VerticalFlowLayout.LEFT);
				northPanel = new JPanel();
				northPanel.setBorder(BorderFactory.createTitledBorder(
						null, "Frihåndstegning", TitledBorder.DEFAULT_JUSTIFICATION, 
						TitledBorder.DEFAULT_POSITION, 
						new Font("Tahoma", Font.PLAIN, 12), 
						new Color(0, 70, 213)));
				northPanel.setLayout(vfl);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		northPanel.removeAll();
		northPanel.add(getObjectPanel(), null);
		northPanel.add(new JSeparator(JSeparator.HORIZONTAL), null);
		northPanel.add(getSnapToCheckBox(), null);
		northPanel.add(new JSeparator(JSeparator.HORIZONTAL), null);
		northPanel.add(getConstraintsCheckBox(), null);
		northPanel.add(getMinStepPanel(), null);
		northPanel.add(getMaxStepPanel(), null);
		return northPanel;
	}
	

	private JPanel getObjectPanel() {
		if(objectPanel == null) {
			BorderLayout bl = new BorderLayout();
			objectPanel = new JPanel();
			objectPanel.setLayout(bl);
			objectLabel = new JLabel("Objekt");
			objectLabel.setLabelFor(getMinStepText());
			objectPanel.add(objectLabel,BorderLayout.WEST);
			objectPanel.add(getObjectText(),BorderLayout.EAST);
		}
		return objectPanel;
	}	
	
	private JTextField getObjectText() {
		if(objectText == null) {
			objectText = new JTextField("<velg eller tegn nytt>");
			objectText.setToolTipText("Valgt objekt");
			objectText.setEditable(false);
			objectText.setPreferredSize(new Dimension(200,20));
			objectText.setBorder(null);
		}
		return objectText;
	}	

	private JCheckBox getSnapToCheckBox() {
		if(snapToCheckBox == null) {
			snapToCheckBox = new JCheckBox("Bruk snapping");
			snapToCheckBox.setToolTipText("Snapper automatisk linje " +
					"til valgte snappelag");
			snapToCheckBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
				    if (e.getStateChange() == ItemEvent.DESELECTED)
				    	tool.setSnapToMode(false);
				    else {
				    	// get snapping adapter
				    	SnappingAdapter snapping = tool.getSnappingAdapter();
				    	// valid?
				    	if(snapping!=null && snapping.isSnapReady())
				    		tool.setSnapToMode(true);
				    	else {
				    		snapToCheckBox.setSelected(false);
				    		Utils.showWarning("Du må først velge lag å snappe til");
				    	}
				    }
				}
				
			});
		}
		return snapToCheckBox;
	}
	
	private JCheckBox getConstraintsCheckBox() {
		if(constraintsCheckBox == null) {
			constraintsCheckBox = new JCheckBox("Begrens avstand");
			constraintsCheckBox.setToolTipText("Begrenser avstand mellom punkter på en linje");
			constraintsCheckBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {

					// get flag
					boolean mode = (e.getStateChange() == ItemEvent.SELECTED);
					
					// update
				    tool.setConstrainMode(mode);
				    getMinStepText().setEnabled(mode);
				    getMaxStepText().setEnabled(mode);
				    
				}
				
			});
		}
		return constraintsCheckBox;
	}	
	
	private JPanel getMinStepPanel() {
		if(minStepPanel == null) {
			BorderLayout bl = new BorderLayout();
			minStepPanel = new JPanel();
			minStepPanel.setLayout(bl);
			minStepTextLabel = new JLabel("Minimum avstand");
			minStepTextLabel.setLabelFor(getMinStepText());
			minStepPanel.add(minStepTextLabel,BorderLayout.WEST);
			minStepPanel.add(getMinStepText(),BorderLayout.EAST);
		}
		return minStepPanel;
	}	
	
	private JTextField getMinStepText() {
		if(minStepText == null) {
			minStepText = new JTextField("0");
			minStepText.setToolTipText("Minimum avstand mellom to punktet");
			minStepText.setPreferredSize(new Dimension(100,20));
			minStepText.setDocument(new NumericDocument(-1,0,false));
		}
		return minStepText;
	}	
	
	private JPanel getMaxStepPanel() {
		if(maxStepPanel == null) {
			BorderLayout bl = new BorderLayout();
			maxStepPanel = new JPanel();
			maxStepPanel.setLayout(bl);
			maxStepTextLabel = new JLabel("Maksimum avstand");
			maxStepTextLabel.setLabelFor(getMaxStepText());
			maxStepPanel.add(maxStepTextLabel,BorderLayout.WEST);
			maxStepPanel.add(getMaxStepText(),BorderLayout.EAST);
		}
		return maxStepPanel;
	}	
	
	private JTextField getMaxStepText() {
		if(maxStepText == null) {
			maxStepText = new JTextField("0");
			maxStepText.setToolTipText("Maksimum avstand mellom to punktet");
			maxStepText.setPreferredSize(new Dimension(100,20));
			maxStepText.setDocument(new NumericDocument(-1,0,false));
		}
		return maxStepText;
	}	
	
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setHgap(0);
				fl.setVgap(0);
				fl.setAlignment(FlowLayout.RIGHT);
				buttonPanel = new JPanel();
				buttonPanel.setLayout(fl);
				buttonPanel.add(getApplyButton());
				buttonPanel.add(getSnapToButton());
				buttonPanel.add(getCancelButton());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}

	public JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
				cancelButton.setActionCommand("cancel");
				cancelButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// forward
						cancel();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return cancelButton;
	}
	
	private JButton getApplyButton() {
		if (applyButton == null) {
			try {
				applyButton = DiskoButtonFactory.createButton("GENERAL.FINISH",ButtonSize.NORMAL);
				applyButton.setActionCommand("finish");
				applyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// forward
						apply();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return applyButton;
	}	
	
	
	private JButton getSnapToButton() {
		if (snapToButton == null) {
			try {
				snapToButton = DiskoButtonFactory.createButton("MAP.SNAPTO",ButtonSize.NORMAL);
				snapToButton.setActionCommand("snapTo");
				snapToButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// forward
						snapTo();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return snapToButton;
	}	
	
	public void setMsoObject(IMsoObjectIf msoObject) {
		// consume?
		if (tool.getMap() == null) return;
		// reset?
		if(msoObject==null) {
			reset();
		}
		else {
			// get area
			IMsoObjectIf parent = MsoUtils.getGeoDataParent(msoObject);
			// get object name
			if(parent!=msoObject) {
				// get name
				String name = MsoUtils.getMsoObjectName(parent, 1);
				name += " - " + MsoUtils.getMsoObjectName(msoObject, 1);
				// update label
				getObjectText().setText(name);				
			}
			else {
				String name = MsoUtils.getMsoObjectName(msoObject, 1);
				// update label
				getObjectText().setText(name);				
			}
		}
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
