package org.redcross.sar.wp.tactics;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.NumPadDialog;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.MsoUtils;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentPriority;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.wp.IDiskoWpModule;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.EnumSet;
import java.util.Hashtable;

public class SearchRequirementDialog extends DiskoDialog implements IMsoLayerEventListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPanel = null;
	private JPanel titlePanel = null;
	private JLabel iconLabel = null;
	private JLabel titleLabel = null;
	private JPanel requirementPanel = null;
	private JLabel accuracyLabel = null;
	private JSlider accuracySlider = null;
	private JTextField accuracyTextField = null;
	private JLabel priorityLabel = null;
	private JSlider prioritySlider = null;
	private JTextField priorityTextField = null;
	private JLabel personelLabel = null;
	private JScrollPane remarksScrollPane = null;
	private JTextArea remarksTextArea = null;
	private JTabbedPane tabbedPane = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private JTextField personelTextField = null;
	private IDiskoWpModule wp = null;
	private ISearchIf currentAssignment = null;


	public SearchRequirementDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(),getMyInterest(),getMyLayers());
		// prepare objeckts
		this.wp = wp;
		// initialize gui
		initialize();
		// get selected mso feature
		setSelectedMsoFeature(wp.getMap());
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		try {
            this.setPreferredSize(new Dimension(800, 200));
            this.setSize(new Dimension(989, 145));
            this.setContentPane(getContentPanel());
            this.pack();
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}

	private static EnumSet<IMsoManagerIf.MsoClassCode> getMyInterest() {
		EnumSet<IMsoManagerIf.MsoClassCode> myInterests 
			= EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMyLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers 
			= EnumSet.of(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
	    return myLayers;
	}
	
	/**
	 * This method initializes contentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new JPanel();
				contentPanel.setLayout(new BorderLayout());
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				contentPanel.add(getTitlePanel(),BorderLayout.NORTH);
				contentPanel.add(getTabbedPane(), BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}
	
	/**
	 * This method initializes titlePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getTitlePanel() {
		if (titlePanel == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setAlignment(FlowLayout.LEFT);
				fl.setHgap(5);
				fl.setVgap(0);
				JPanel labels = new JPanel();
				labels.setLayout(fl);
				iconLabel = new JLabel();
				titleLabel = new JLabel();
				labels.add(iconLabel,null);
				labels.add(titleLabel,null);
				titlePanel = new JPanel();
				titlePanel.setLayout(new BorderLayout());
				titlePanel.add(labels,BorderLayout.CENTER);
				titlePanel.add(new JSeparator(JSeparator.HORIZONTAL),BorderLayout.SOUTH);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return titlePanel;
	}
	
	/**
	 * This method initializes tabbedPane
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			try {
				tabbedPane = new JTabbedPane();
				tabbedPane.addTab("Krav", null, getRequirementPanel(), null);
				tabbedPane.addTab("Merknad", null, getRemarksScrollPane(), null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return tabbedPane;
	}

	private void reset() {
		setIsWorking();
		getAccuracyTextField().setText(null);
		getPriorityTextField().setText(null);
		getRemarksTextArea().setText(null);
		getPersonelTextField().setText(null);
		getPrioritySlider().setValue(2);
		getAccuracySlider().setValue(50);
		currentAssignment = null;
		setIsNotWorking();
	}

	public int getAccuracy() {
		return getAccuracySlider().getValue();
	}

	public AssignmentPriority getPriority() {
		int value = getPrioritySlider().getValue();
		switch (value) {
			case 1: return AssignmentPriority.LOW;
			case 2: return AssignmentPriority.NORMAL;
			case 3: return AssignmentPriority.HIGH;
		}
		return null;
	}

	public int getPersonelNeed() {
		try {
			return Integer.parseInt(getPersonelTextField().getText());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
		}
		return 0;
	}

	public String getRemarks() {
		return getRemarksTextArea().getText();
	}

	public void setAccuracy(int accuracy) {
		setAccuracy(accuracy,true,true);
	}

	private void setAccuracy(int accuracy, boolean gui, boolean mso) {
		setIsWorking();
		if(gui) {
			getAccuracySlider().setValue(accuracy);
		}
		if(mso) {
			if(currentAssignment!=null) {
				if(currentAssignment.getPlannedAccuracy()!=accuracy) {
					currentAssignment.setPlannedAccuracy(accuracy);
					fireOnWorkChange(getAccuracySlider(),currentAssignment,accuracy);

				}
			}
		}
		setIsNotWorking();
	}
	
	public void setPriority(AssignmentPriority priority) {
		setPriority(priority,true,true);
	}

	private void setPriority(AssignmentPriority priority, boolean gui, boolean mso) {
		setIsWorking();
		if(gui) {
			if (priority == AssignmentPriority.LOW) {
				getPrioritySlider().setValue(1);
			}
			else if (priority == AssignmentPriority.NORMAL) {
				getPrioritySlider().setValue(2);
			}
			if (priority == AssignmentPriority.HIGH) {
				getPrioritySlider().setValue(3);
			}
		}
		if(mso) {
			if(currentAssignment!=null) {
				if(currentAssignment.getPriority()!=priority) {
					currentAssignment.setPriority(priority);
					fireOnWorkChange(getPrioritySlider(),currentAssignment,priority);

				}
			}
		}
		setIsNotWorking();
	}
	
	public void setPersonelNeed(int number) {
		setPersonelNeed(number,true,true);
	}

	private void setPersonelNeed(int number, boolean gui, boolean mso) {
		setIsWorking();
		if(gui) {
			getPersonelTextField().setText(String.valueOf(number));
		}
		if(mso) {
			if(currentAssignment!=null) {
				if(currentAssignment.getPlannedPersonnel()!=number) {
					currentAssignment.setPlannedPersonnel(number);
					fireOnWorkChange(getPersonelTextField(),currentAssignment,number);
				}
			}
		}
		setIsNotWorking();
	}
	
	public void setRemarks(String remarks) {
		setRemarks(remarks,true,true);
	}

	private void setRemarks(String remarks, boolean gui, boolean mso) {
		setIsWorking();
		if(gui) {
			getRemarksTextArea().setText(remarks);
		}
		if(mso) {
			if(currentAssignment!=null) {
				if(!currentAssignment.getRemarks().equals(remarks)) {
					currentAssignment.setRemarks(remarks);
					fireOnWorkChange(getRemarksTextArea(),currentAssignment,remarks);
				}
			}
		}
		setIsNotWorking();
	}	
	
	/**
	 * This method initializes requirementPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRequirementPanel() {
		if (requirementPanel == null) {
			try {
				GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
				gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints1.gridy = 1;
				gridBagConstraints1.weightx = 1.0;
				gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
				gridBagConstraints1.gridx = 3;
				GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
				gridBagConstraints13.fill = GridBagConstraints.NONE;
				gridBagConstraints13.gridy = 4;
				gridBagConstraints13.weightx = 1.0;
				gridBagConstraints13.anchor = GridBagConstraints.WEST;
				gridBagConstraints13.insets = new Insets(0, 0, 5, 0);
				gridBagConstraints13.gridx = 1;
				GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
				gridBagConstraints12.gridx = 0;
				gridBagConstraints12.anchor = GridBagConstraints.WEST;
				gridBagConstraints12.insets = new Insets(0, 10, 0, 0);
				gridBagConstraints12.gridy = 4;
				GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
				gridBagConstraints91.fill = GridBagConstraints.NONE;
				gridBagConstraints91.gridx = 2;
				gridBagConstraints91.gridy = 1;
				gridBagConstraints91.anchor = GridBagConstraints.NORTHWEST;
				gridBagConstraints91.weightx = 1.0;
				GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
				gridBagConstraints81.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints81.gridx = 1;
				gridBagConstraints81.gridy = 1;
				gridBagConstraints81.anchor = GridBagConstraints.WEST;
				gridBagConstraints81.insets = new Insets(5, 0, 0, 0);
				gridBagConstraints81.weightx = 1.0;
				GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
				gridBagConstraints71.fill = GridBagConstraints.NONE;
				gridBagConstraints71.anchor = GridBagConstraints.NORTHWEST;
				gridBagConstraints71.insets = new Insets(5, 0, 5, 0);
				gridBagConstraints71.weightx = 1.0;
				GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
				gridBagConstraints61.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints61.anchor = GridBagConstraints.WEST;
				gridBagConstraints61.insets = new Insets(5, 0, 5, 0);
				gridBagConstraints61.weightx = 1.0;
				GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
				gridBagConstraints6.gridx = 3;
				gridBagConstraints6.anchor = GridBagConstraints.SOUTHWEST;
				gridBagConstraints6.insets = new Insets(0, 0, 0, 0);
				gridBagConstraints6.gridy = 0;
				personelLabel = new JLabel();
				personelLabel.setText("Mannskapsbehov:");
				GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
				gridBagConstraints3.gridx = 0;
				gridBagConstraints3.anchor = GridBagConstraints.WEST;
				gridBagConstraints3.insets = new Insets(0, 10, 0, 0);
				gridBagConstraints3.gridy = 1;
				priorityLabel = new JLabel();
				priorityLabel.setText("Prioritet:");
				GridBagConstraints gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.gridx = 0;
				gridBagConstraints.anchor = GridBagConstraints.WEST;
				gridBagConstraints.insets = new Insets(0, 10, 10, 0);
				gridBagConstraints.gridy = 0;
				accuracyLabel = new JLabel();
				accuracyLabel.setText("Nøyaktighet:");
				requirementPanel = new JPanel();
				requirementPanel.setLayout(new GridBagLayout());
				requirementPanel.add(priorityLabel, gridBagConstraints3);
				requirementPanel.add(personelLabel, gridBagConstraints6);
				requirementPanel.add(accuracyLabel, gridBagConstraints);
				requirementPanel.add(getAccuracySlider(), gridBagConstraints61);
				requirementPanel.add(getAccuracyTextField(), gridBagConstraints71);
				requirementPanel.add(getPrioritySlider(), gridBagConstraints81);
				requirementPanel.add(getPriorityTextField(), gridBagConstraints91);
				requirementPanel.add(getPersonelTextField(), gridBagConstraints1);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return requirementPanel;
	}

	/**
	 * This method initializes accuracySlider
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getAccuracySlider() {
		if (accuracySlider == null) {
			try {
				accuracySlider = new JSlider();
				accuracySlider.setMajorTickSpacing(50);
				accuracySlider.setPaintLabels(true);
				accuracySlider.setPreferredSize(new Dimension(583, 40));
				accuracySlider.setMinorTickSpacing(10);
				accuracySlider.setSnapToTicks(true);
				accuracySlider.setPaintTicks(true);
				accuracySlider.setMinimum(0);
				accuracySlider.setMaximum(100);
				accuracySlider.setValue(50);
				accuracySlider.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent e) {
						if (isWorking()) return;
						getAccuracyTextField().setText(accuracySlider.getValue()+"%");
						setAccuracy(accuracySlider.getValue(),false,true);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return accuracySlider;
	}

	/**
	 * This method initializes prioritySlider
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getPrioritySlider() {
		if (prioritySlider == null) {
			try {
				prioritySlider = new JSlider();
				prioritySlider.setPreferredSize(new Dimension(583, 40));
				prioritySlider.setPaintLabels(true);
				prioritySlider.setMinorTickSpacing(0);
				prioritySlider.setMajorTickSpacing(1);
				prioritySlider.setSnapToTicks(true);
				prioritySlider.setMinimum(1);
				prioritySlider.setMaximum(3);
				prioritySlider.setValue(2);
				prioritySlider.setPaintTicks(true);
				prioritySlider.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent e) {
						if (isWorking()) return;
						Integer key = Integer.valueOf(prioritySlider.getValue());
						String text = ((JLabel)prioritySlider.getLabelTable().get(key)).getText();
						getPriorityTextField().setText(text);
						setPriority(getPriority(), false, true);
					}
				});
				Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
				labels.put(new Integer(1), new JLabel("LAV"));
				labels.put(new Integer(2), new JLabel("NORMAL"));
				labels.put(new Integer(3), new JLabel("HØY"));
				prioritySlider.setLabelTable(labels);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return prioritySlider;
	}

	/**
	 * This method initializes accuracyTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getAccuracyTextField() {
		if (accuracyTextField == null) {
			try {
				accuracyTextField = new JTextField();
				accuracyTextField.setPreferredSize(new Dimension(75, 20));
				accuracyTextField.setEditable(true);
				accuracyTextField.setText("50%");
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return accuracyTextField;
	}

	/**
	 * This method initializes priorityTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getPriorityTextField() {
		if (priorityTextField == null) {
			try {
				priorityTextField = new JTextField();
				priorityTextField.setPreferredSize(new Dimension(75, 20));
				priorityTextField.setEditable(true);
				priorityTextField.setText("HØY");
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return priorityTextField;
	}

	/**
	 * This method initializes personelTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getPersonelTextField() {
		if (personelTextField == null) {
			try {
				personelTextField = new JTextField();
				personelTextField.setText("0");
				personelTextField.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) { apply(); }
					public void insertUpdate(DocumentEvent arg0) { apply(); }
					public void removeUpdate(DocumentEvent arg0) { apply(); }
					private void apply() {
						// no mso update allowed?
						if (isWorking()) return;
						// update mso model
						setPersonelNeed(Integer.valueOf(personelTextField.getText()),false,true);						
					}
				});
				personelTextField.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						if (e.getClickCount() == 2){
							NumPadDialog npDialog = wp.getApplication().
								getUIFactory().getNumPadDialog();
							Point p = personelTextField.getLocationOnScreen();
							p.setLocation(p.x + personelTextField.getWidth()-
									npDialog.getWidth(), p.y-npDialog.getHeight()-2);
							npDialog.setLocation(p);
							npDialog.setTextField(personelTextField);
							npDialog.setVisible(true);
						}
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return personelTextField;
	}

	/**
	 * This method initializes criticalQuestionsScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getRemarksScrollPane() {
		if (remarksScrollPane == null) {
			try {
				remarksScrollPane = new JScrollPane();
				remarksScrollPane.setViewportView(getRemarksTextArea());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return remarksScrollPane;
	}

	/**
	 * This method initializes criticalQuestionsTextArea
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getRemarksTextArea() {
		if (remarksTextArea == null) {
			try {
				remarksTextArea = new JTextArea();
				remarksTextArea.setLineWrap(true);
				remarksTextArea.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) { apply(); }
					public void insertUpdate(DocumentEvent arg0) { apply(); }
					public void removeUpdate(DocumentEvent arg0) { apply(); }
					private void apply() {
						// no mso update allowed?
						if (isWorking()) return;
						// update mso model
						setRemarks(remarksTextArea.getText(),false,true);												
					}
				});
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return remarksTextArea;		
	}
	
	@Override
	public boolean setMsoObject(IMsoObjectIf msoObj) {
		if(isWorking()) return false;
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			if (assignment instanceof ISearchIf) {		
				currentAssignment = (ISearchIf)assignment;
				setPriority(currentAssignment.getPriority(),true,false);
				setAccuracy(currentAssignment.getPlannedAccuracy(),true,false);
				setPersonelNeed(currentAssignment.getPlannedPersonnel(),true,false);
				setRemarks(currentAssignment.getRemarks(),true,false);								
			}
		}
		// forward
		setup();
		// success
		return true;
	}	
	
	private void setup() {
		// update icon
		if(currentAssignment!=null) {
			Enum e = MsoUtils.getType(currentAssignment,true);
			iconLabel.setIcon(Utils.getIcon(e));
			titleLabel.setText("<html>Krav til <b>" + 
					MsoUtils.getAssignmentName(currentAssignment, 1).toLowerCase() + "</b></html>");
			getTabbedPane().setEnabled(true);
		}
		else {
			iconLabel.setIcon(null);
			titleLabel.setText("Du må først velge et oppdrag");			
			getTabbedPane().setEnabled(false);
			reset();
		}		
	}
	
	@Override
	public void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		if(isWorking()) return;
		// is same as selected?
		if(msoObject == currentMsoObj) {
			setMsoObject(msoObject);
		}
	}

	@Override
	public void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		if(isWorking()) return;
		// is same as selected?
		if(msoObject == currentMsoObj) {
			// reset selection
			currentMsoFeature =null;
			currentMsoObj =null;
			reset();
		}
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"

