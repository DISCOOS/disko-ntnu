package org.redcross.sar.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.map.SnappingAdapter;
import org.redcross.sar.map.command.FreeHandTool;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.MsoUtils;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.borland.jbcl.layout.VerticalFlowLayout;

import com.esri.arcgis.interop.AutomationException;

public class FreeHandPanel extends JPanel implements IMsoLayerEventListener {

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
		try {
			// remove all from container
			removeAll();
			// build container
			if(isVertical) {
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setAlignment(VerticalFlowLayout.LEFT);
				vfl.setHgap(0);
				vfl.setVgap(5);
				this.setLayout(vfl);
				this.add(getNorthPanel());
				this.add(getButtonPanel());
			}
			else {
				BorderLayout bl = new BorderLayout();
				bl.setHgap(5);
				bl.setVgap(0);
				this.setLayout(bl);
				this.add(getNorthPanel(),BorderLayout.CENTER);
				this.add(getButtonPanel(),BorderLayout.EAST);				
			}
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
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
		// force update mode off
		tool.setUpdateMode(false);
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
			minStepText.setDocument(new NumericDocument(0,false));
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
			maxStepText.setDocument(new NumericDocument(0,false));
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
				buttonPanel.add(getCancelButton());
				buttonPanel.add(getSnapToButton());
				buttonPanel.add(getApplyButton());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}

	public JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = new JButton();
				String iconName = "cancel.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				cancelButton.setIcon(icon);
				cancelButton.setToolTipText(Utils.getProperty("cancel.text"));
				Dimension size = app.getUIFactory().getSmallButtonSize();
				cancelButton.setPreferredSize(size);
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
				applyButton = new JButton();
				String iconName = "finish.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				applyButton.setIcon(icon);
				applyButton.setToolTipText(Utils.getProperty("finish.text"));
				Dimension size = app.getUIFactory().getSmallButtonSize();
				applyButton.setPreferredSize(size);
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
				snapToButton = new JButton();
				String iconName = "IconEnum.SNAPTO.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				snapToButton.setIcon(icon);
				snapToButton.setToolTipText(Utils.getProperty("IconEnum.SNAPTO.text"));
				Dimension size = app.getUIFactory().getSmallButtonSize();
				snapToButton.setPreferredSize(size);
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
	
	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		if (tool.getMap() == null) {
			return;
		}
		IMsoFeatureLayer msoLayer = (IMsoFeatureLayer)e.getSource();
		List selection = msoLayer.getSelected();
		if (selection != null && selection.size() > 0) {
			// get mso object
			IMsoFeature msoFeature = (IMsoFeature)selection.get(0);
			IMsoObjectIf msoObject = msoFeature.getMsoObject();
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
		else {
			reset();
		}
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
