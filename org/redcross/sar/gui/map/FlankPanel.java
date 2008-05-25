package org.redcross.sar.gui.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.ScaleBar;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.FlankTool;

import com.borland.jbcl.layout.VerticalFlowLayout;

public class FlankPanel extends DefaultDiskoPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel centerPanel = null;
	private JPanel flankerPanel = null;
	private JPanel clipPanel = null;
	private JCheckBox leftCheckBox = null;
	private JCheckBox rightCheckBox = null;
	private ScaleBar leftScaleBar = null;
	private ScaleBar rightScaleBar = null;
	private JSpinner leftSpinner = null;
	private JSpinner rightSpinner = null;
	private FlankTool tool = null;
	
	public FlankPanel(IDiskoApplication app, FlankTool tool) {
		// forward
		super("",false,false);
		// prepare
		this.tool = tool;
		// initialize gui
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setBodyComponent(getCenterPanel());
	}
	
	public void onLoad(IDiskoMap diskoMap) throws IOException {
		updateLayerSelection();
	}

	/**
	 * This method initializes centerPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCenterPanel() {
		if (centerPanel == null) {
			try {
				centerPanel = new JPanel();
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setVgap(0);
				vfl.setHgap(0);
				vfl.setAlignment(VerticalFlowLayout.TOP);
				centerPanel.setLayout(vfl);
				centerPanel.add(getFlankerPanel(), null);
				centerPanel.add(getClipPanel(), null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return centerPanel;
	}

	/**
	 * This method initializes flankerPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getFlankerPanel() {
		if (flankerPanel == null) {
			try {
				GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
				gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints5.gridy = 3;
				gridBagConstraints5.weightx = 1.0;
				gridBagConstraints5.anchor = GridBagConstraints.WEST;
				gridBagConstraints5.insets = new Insets(0, 0, 10, 0);
				gridBagConstraints5.gridx = 2;
				GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
				gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints4.gridy = 1;
				gridBagConstraints4.weightx = 1.0;
				gridBagConstraints4.anchor = GridBagConstraints.WEST;
				gridBagConstraints4.gridx = 2;
				GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
				gridBagConstraints3.gridx = 2;
				gridBagConstraints3.anchor = GridBagConstraints.WEST;
				gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints3.insets = new Insets(10, 0, 5, 0);
				gridBagConstraints3.gridy = 2;
				GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
				gridBagConstraints2.insets = new Insets(10, 0, 5, 0);
				gridBagConstraints2.anchor = GridBagConstraints.WEST;
				gridBagConstraints2.gridy = 2;
				GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
				gridBagConstraints1.gridx = 2;
				gridBagConstraints1.anchor = GridBagConstraints.WEST;
				gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints1.insets = new Insets(0, 0, 5, 0);
				GridBagConstraints gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.insets = new Insets(0, 0, 5, 0);
				flankerPanel = new JPanel();
				flankerPanel.setLayout(new GridBagLayout());
				flankerPanel.setBorder(BorderFactory.createTitledBorder(null, "Flanker", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
				flankerPanel.add(getLeftCheckBox(), gridBagConstraints);
				flankerPanel.add(getLeftScaleBar(), gridBagConstraints1);
				flankerPanel.add(getRightCheckBox(), gridBagConstraints2);
				flankerPanel.add(getRightScaleBar(), gridBagConstraints3);
				flankerPanel.add(getLeftSpinner(), gridBagConstraints4);
				flankerPanel.add(getRightSpinner(), gridBagConstraints5);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return flankerPanel;
	}

	/**
	 * This method initializes clipPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getClipPanel() {
		if (clipPanel == null) {
			try {
				clipPanel = new JPanel();
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setVgap(0);
				vfl.setHgap(0);
				vfl.setAlignment(VerticalFlowLayout.TOP);
				clipPanel.setLayout(vfl);
				clipPanel.setBorder(BorderFactory.createTitledBorder(null, "Klipp til", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return clipPanel;
	}
	
	private void updateLayerSelection() {
		//adding checkboxes
		/*try {
			getClipPanel().removeAll();
			for (int i = 0; i < clipLayerSelectionModel.getLayerCount(); i++) {
				final int index = i;
				final JCheckBox cb = new JCheckBox();
				IFeatureLayer flayer = clipLayerSelectionModel.getFeatureLayer(i);
				cb.setText(flayer.getName());
				cb.setSelected(clipLayerSelectionModel.isSelected(i));
				getClipPanel().add(cb);
				cb.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						clipLayerSelectionModel.setSelected(index, cb.isSelected());
					}
				});
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	/**
	 * This method initializes leftCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getLeftCheckBox() {
		if (leftCheckBox == null) {
			try {
				leftCheckBox = new JCheckBox();
				leftCheckBox.setText("Venstre");
				leftCheckBox.setSelected(true);
				leftCheckBox.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						getLeftSpinner().setEnabled(leftCheckBox.isSelected());
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return leftCheckBox;
	}

	/**
	 * This method initializes leftProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private ScaleBar getLeftScaleBar() {
		if (leftScaleBar == null) {
			try {
				leftScaleBar = new ScaleBar();
				leftScaleBar.setPreferredSize(new Dimension(20, 20));
				leftScaleBar.setValue(25);
				leftScaleBar.setMaximum(500);
				leftScaleBar.setColor(Color.red);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return leftScaleBar;
	}
	
	private ScaleBar getRightScaleBar() {
		if (rightScaleBar == null) {
			try {
				rightScaleBar = new ScaleBar();
				rightScaleBar.setPreferredSize(new Dimension(20, 20));
				rightScaleBar.setValue(25);
				rightScaleBar.setMaximum(500);
				rightScaleBar.setColor(Color.blue);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return rightScaleBar;
	}

	/**
	 * This method initializes rightCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getRightCheckBox() {
		if (rightCheckBox == null) {
			try {
				rightCheckBox = new JCheckBox();
				rightCheckBox.setText("Høyre");
				rightCheckBox.setSelected(true);
				rightCheckBox.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						getRightSpinner().setEnabled(rightCheckBox.isSelected());
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return rightCheckBox;
	}

	/**
	 * This method initializes leftComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JSpinner getLeftSpinner() {
		if (leftSpinner == null) {
			try {
				leftSpinner = new JSpinner();
				leftSpinner.setPreferredSize(new Dimension(47, 20));
				SpinnerNumberModel model = new SpinnerNumberModel(25, 0, 500, 25); 
				leftSpinner.setModel(model);
				leftSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent e) {
						int value = ((Integer)leftSpinner.getValue()).intValue();
						leftScaleBar.setValue(value);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return leftSpinner;
	}
	
	/**
	 * This method initializes leftComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JSpinner getRightSpinner() {
		if (rightSpinner == null) {
			try {
				rightSpinner = new JSpinner();
				rightSpinner.setPreferredSize(new Dimension(47, 20));
				SpinnerNumberModel model = new SpinnerNumberModel(25, 0, 500, 25); 
				rightSpinner.setModel(model);
				rightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent e) {
						int value = ((Integer)rightSpinner.getValue()).intValue();
						rightScaleBar.setValue(value);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return rightSpinner;
	}
}  //  @jve:decl-index=0:visual-constraint="10,2"
