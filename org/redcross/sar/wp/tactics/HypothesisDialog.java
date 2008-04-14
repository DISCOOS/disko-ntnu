package org.redcross.sar.wp.tactics;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collection;
import java.util.EnumSet;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.models.HypothesisListModel;
import org.redcross.sar.gui.renderers.HypothesisListCellRenderer;
import org.redcross.sar.gui.renderers.SimpleListCellRenderer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IHypothesisIf;
import org.redcross.sar.mso.data.IHypothesisIf.HypothesisStatus;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;


public class HypothesisDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private IMsoModelIf msoModel = null;
	private DiskoPanel contentPanel = null;
	private JPanel hypothesisPanel = null;
	private JPanel buttonPanel = null;
	private JButton newButton = null;
	private JScrollPane listScrollPane = null;
	private JList hypothesisList = null;
	private JPanel centerPanel = null;
	private JScrollPane textAreaScrollPane = null;
	private JTextArea descriptionTextArea = null;
	private JPanel propertiesPanel;
	private JLabel statusLabel;
	private JLabel priorityLabel;
	private JComboBox priorityComboBox;
	private JComboBox statusComboBox;
	private String[] labels = null;

	private ISearchAreaIf currentSearchArea = null;
	private IHypothesisIf selectedHypothesis = null;

	public HypothesisDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(),getMyInterest(),getMyLayers());
		// setup objects
		this.msoModel = wp.getMsoModel();
		// initialize ui
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
			createlabels();
			this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(900, 300));
            this.pack();
			loadHypotheses();
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}

	private static EnumSet<IMsoManagerIf.MsoClassCode> getMyInterest() {
		EnumSet<IMsoManagerIf.MsoClassCode> myInterests 
			= EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA);
		myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_HYPOTHESIS);
		return myInterests;
	}
	
	private static EnumSet<IMsoFeatureLayer.LayerCode> getMyLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers 
			= EnumSet.of(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
	    return myLayers;
	}

	private void loadHypotheses() {
		Collection<IHypothesisIf> c = msoModel.getMsoManager().
			getCmdPost().getHypothesisListItems();
		DefaultListModel model = new DefaultListModel();
		for(IHypothesisIf h:c) {
			model.addElement(Utils.translate(IMsoManagerIf
					.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber());
		}
		hypothesisList.setModel(model);
	}
	
	private void addHypothesis(IHypothesisIf h) {
		DefaultListModel model = (DefaultListModel)hypothesisList.getModel();
		model.addElement(Utils.translate(IMsoManagerIf
				.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber());
	}
	
	private void removeHypothesis(IHypothesisIf h) {
		DefaultListModel model = (DefaultListModel)hypothesisList.getModel();
		model.removeElement(Utils.translate(IMsoManagerIf
				.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber());
	}
	
	private IHypothesisIf getHypothesis(String name) {
		
		if(name==null || name.isEmpty()) return null;
		
		Collection<IHypothesisIf> c = msoModel.getMsoManager()
			.getCmdPost().getHypothesisListItems();
	
		for(IHypothesisIf h:c) {
			if(name.equalsIgnoreCase(getHypothesisName(h))) { 
				// found!
				return h;
			}
		}
		return null;
	}
	
	private String getHypothesisName(IHypothesisIf h) {
		return h!=null ? Utils.translate(IMsoManagerIf.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber() : null;
	}
	
	private void createlabels() {
		labels = new String[5];
		labels[0] = Utils.translate("PRIMARY_SEARCH_AREA");
		labels[1] = Utils.translate("SECONDARY_SEARCH_AREA");
		labels[2] = Utils.translate("PRIORITY3_SEARCH_AREA");
		labels[3] = Utils.translate("PRIORITY4_SEARCH_AREA");
		labels[4] = Utils.translate("PRIORITY5_SEARCH_AREA");
	}

	public IHypothesisIf getSelectedHypothesis() {
		return selectedHypothesis;
	}

	private void setHypotesis(IHypothesisIf hypothesis, boolean gui, boolean mso) {
		setIsWorking();
		boolean enable = false;
		if(hypothesis!=null) {
			// update gui?
			if (gui) {
				// get hypothesis name
				String name = Utils.translate(IMsoManagerIf
						.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+hypothesis.getNumber();
				// update all
				getHypothesisList().setSelectedValue(name, true);
				getDescriptionTextArea().setText(hypothesis.getDescription());
				getPriorityComboBox().setSelectedIndex(hypothesis.getPriorityIndex());
				getStatusComboBox().setSelectedItem(hypothesis.getStatus());
			}
			// update mso?
			if(mso) {
				// get search area
				if(currentSearchArea!=null) {
					// any change?
					if (!hypothesis.equals(currentSearchArea.getSearchAreaHypothesis())) {
						currentSearchArea.setSearchAreaHypothesis(hypothesis);
						fireOnWorkChange(hypothesisList,currentSearchArea,hypothesis);
					}
				}
			}
			enable = true;
		}
		// update selection
		selectedHypothesis = hypothesis;
		// update enabled state
		getDescriptionTextArea().setEnabled(enable);						
		// finished
		setIsNotWorking();
	}
	
	private void setDescription(String text, boolean gui, boolean mso) {
		setIsWorking();
		// has hypothesis?
		if(selectedHypothesis!=null) {
			// update gui?
			if (gui) {
				getDescriptionTextArea().setText(text);
			}
			// update mso?
			if(mso) { 
				if (!selectedHypothesis.getDescription().equals(text)) {
					selectedHypothesis.setDescription(text);
					fireOnWorkChange(getDescriptionTextArea(),selectedHypothesis,text);
				}
			}
		}
		setIsNotWorking();
	}
	
	private void setPriority(int priority, boolean gui, boolean mso) {
		setIsWorking();
		// has hypothesis?
		if(selectedHypothesis!=null) {
			// update gui?
			if (gui) {
				getPriorityComboBox().setSelectedIndex(priority-1);
			}
			// update mso?
			if(mso) { 
				if (selectedHypothesis.getPriorityIndex() != priority) {
					selectedHypothesis.setPriorityIndex(priority);
					fireOnWorkChange(priorityComboBox,selectedHypothesis,priority);
				}
			}
		}
		setIsNotWorking();
	}
	
	private void setStatus(HypothesisStatus status, boolean gui, boolean mso) {
		setIsWorking();
		// has hypothesis?
		if(selectedHypothesis!=null) {
			// update gui?
			if (gui) {
				getStatusComboBox().setSelectedItem(status);
			}
			// update mso?
			if(mso) { 
				if (selectedHypothesis.getStatus() != status) {
					selectedHypothesis.setStatus(status);
					fireOnWorkChange(statusComboBox,selectedHypothesis,status);
				}
			}
		}
		setIsNotWorking();
	}
	
	/**
	 * This method initializes contentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private DiskoPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DiskoPanel();
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				AbstractButton button = contentPanel.addButton(
						DiskoButtonFactory.createButton("GENERAL.CANCEL", ButtonSize.NORMAL),"cancel");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// cancel any pending changes
						cancel();
						// hide me!
						setVisible(false);						
					}					
				});
				contentPanel.setContent(getHypothesisPanel());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	/**
	 * This method initializes hypothesisPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getHypothesisPanel() {
		if (hypothesisPanel == null) {
			try {
				hypothesisPanel = new JPanel();
				hypothesisPanel.setLayout(new BorderLayout());
				hypothesisPanel.add(getListScrollPane(), BorderLayout.WEST);
				hypothesisPanel.add(getCenterPanel(), BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return hypothesisPanel;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				FlowLayout flowLayout = new FlowLayout();
				flowLayout.setAlignment(FlowLayout.RIGHT);
				buttonPanel = new JPanel();
				buttonPanel.setLayout(flowLayout);
				buttonPanel.add(getNewButton(), null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}

	/**
	 * This method initializes newButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getNewButton() {
		if (newButton == null) {
			try {
				newButton = new JButton();
				newButton.setPreferredSize(new Dimension(50, 50));
				newButton.setText("NY");
				newButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						if(isWorking()) return;
						// create a new Hypothesis
						ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
						setHypotesis(cmdPost.getHypothesisList().createHypothesis(), true, true);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return newButton;
	}

	/**
	 * This method initializes listScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getListScrollPane() {
		if (listScrollPane == null) {
			try {
				listScrollPane = new JScrollPane();
				listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				listScrollPane.setPreferredSize(new Dimension(150, 150));
				listScrollPane.setViewportView(getHypothesisList());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return listScrollPane;
	}

	/**
	 * This method initializes hypothesisList
	 *
	 * @return javax.swing.JList
	 */
	private JList getHypothesisList() {
		if (hypothesisList == null) {
			try {
				HypothesisListModel listModel = new HypothesisListModel(msoModel);
				hypothesisList = new JList(listModel);
				hypothesisList.setCellRenderer(new HypothesisListCellRenderer());
				hypothesisList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				hypothesisList.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting() || isWorking()) {
							return;
						}
						// forward
						IHypothesisIf hyp = getHypothesis((String)getHypothesisList().getSelectedValue()); 
						setHypotesis(hyp, false, true);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return hypothesisList;
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
				centerPanel.setLayout(new BorderLayout());
				centerPanel.add(getButtonPanel(), BorderLayout.WEST);
				centerPanel.add(getTextAreaScrollPane(), BorderLayout.CENTER);
				centerPanel.add(getPropertiesPanel(), BorderLayout.EAST);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return centerPanel;
	}

	/**
	 * This method initializes textAreaScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getTextAreaScrollPane() {
		if (textAreaScrollPane == null) {
			try {
				textAreaScrollPane = new JScrollPane();
				textAreaScrollPane.setViewportView(getDescriptionTextArea());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return textAreaScrollPane;
	}

	/**
	 * This method initializes descriptionTextArea
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getDescriptionTextArea() {
		if (descriptionTextArea == null) {
			try {
				descriptionTextArea = new JTextArea();
				descriptionTextArea.setLineWrap(true);
				descriptionTextArea.setEnabled(false);
				// add updata manager
				descriptionTextArea.addFocusListener(new FocusAdapter() {
					
					@Override
					public void focusLost(FocusEvent e) {						
						// forward
						super.focusLost(e);						
						// forward?
						if(e.getOppositeComponent() != getContentPanel().getButton("cancel")) {
							// update mso model
							Runnable r = new Runnable() {
	
								public void run() {
									finish();
								}
								
							};
							SwingUtilities.invokeLater(r);
						}
						
					}
					
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return descriptionTextArea;
	}

	/**
	 * This method initializes propertiesPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPropertiesPanel() {
		if (propertiesPanel == null) {
			try {
				GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
				gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints11.gridy = 1;
				gridBagConstraints11.weightx = 1.0;
				gridBagConstraints11.anchor = GridBagConstraints.WEST;
				gridBagConstraints11.insets = new Insets(10, 0, 10, 0);
				gridBagConstraints11.gridx = 1;
				GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
				gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints1.gridy = 0;
				gridBagConstraints1.weightx = 1.0;
				gridBagConstraints1.anchor = GridBagConstraints.WEST;
				gridBagConstraints1.insets = new Insets(10, 0, 0, 0);
				gridBagConstraints1.gridx = 1;
				GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
				gridBagConstraints2.gridx = 0;
				gridBagConstraints2.insets = new Insets(10, 5, 0, 5);
				gridBagConstraints2.anchor = GridBagConstraints.WEST;
				gridBagConstraints2.gridy = 0;
				GridBagConstraints gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.gridx = 0;
				gridBagConstraints.anchor = GridBagConstraints.WEST;
				gridBagConstraints.insets = new Insets(10, 5, 10, 0);
				gridBagConstraints.gridy = 1;
				statusLabel = new JLabel();
				statusLabel.setText("Status:");
				priorityLabel = new JLabel();
				priorityLabel.setText("Prioritet:");
				propertiesPanel = new JPanel();
				propertiesPanel.setLayout(new GridBagLayout());
				propertiesPanel.setPreferredSize(new Dimension(175, 150));
				propertiesPanel.add(priorityLabel, gridBagConstraints2);
				propertiesPanel.add(statusLabel, gridBagConstraints);
				propertiesPanel.add(getPriorityComboBox(), gridBagConstraints1);
				propertiesPanel.add(getStatusComboBox(), gridBagConstraints11);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return propertiesPanel;
	}

	/**
	 * This method initializes priorityComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getPriorityComboBox() {
		if (priorityComboBox == null) {
			try {
				priorityComboBox = new JComboBox();
				for (int i = 1; i < 6; i++) {
					priorityComboBox.addItem(new Integer(i));
				}
				priorityComboBox.setSelectedIndex(0);
				priorityComboBox.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						if (isWorking()) return;
						setPriority(priorityComboBox.getSelectedIndex()+1, false, true);
					}
				});

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return priorityComboBox;
	}

	/**
	 * This method initializes statusComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getStatusComboBox() {
		if (statusComboBox == null) {
			try {
				statusComboBox = new JComboBox();
				statusComboBox.setRenderer(new SimpleListCellRenderer());
				HypothesisStatus[] values = HypothesisStatus.values();
				for (int i = 0; i < values.length; i++) {
					statusComboBox.addItem(values[i]);
				}
				statusComboBox.setSelectedIndex(0);
				statusComboBox.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						if (isWorking()) return;
						setStatus((HypothesisStatus)statusComboBox.getSelectedItem(), false, true);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return statusComboBox;
	}

	@Override
	public boolean setMsoObject(IMsoObjectIf msoObj) {
		if(isWorking()) return false;
		// reset
		currentSearchArea = null;
		// get search area
		if (msoObj instanceof ISearchAreaIf) {
			// save reference
			currentSearchArea = (ISearchAreaIf)msoObj;
		}
		// get hypothesis
		IHypothesisIf h = (currentSearchArea!=null ? currentSearchArea.getSearchAreaHypothesis() : null);
		// select value
		if(h!=null)
			getHypothesisList().setSelectedValue(getHypothesisName(h),true);
		else 
			cancel();
		// forward
		setup();
		// success
		return true;
	}	
	
	private void setup() {
		// update icon
		if(currentSearchArea!=null) {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("MAP.POLYGON", "48x48"));			
			getContentPanel().setCaptionText("<html>Oppgi hypotese for <b>" + 
					MsoUtils.getSearchAreaName(currentSearchArea).toLowerCase() + "</b></html>");
			getHypothesisList().setEnabled(true);
			getNewButton().setEnabled(true);
			getTextAreaScrollPane().setEnabled(true);
			getDescriptionTextArea().setEnabled(true);
			getPriorityComboBox().setEnabled(true);
			getStatusComboBox().setEnabled(true);			
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et søkeområde");			
			getHypothesisList().setEnabled(false);
			getNewButton().setEnabled(false);
			getTextAreaScrollPane().setEnabled(false);
			getDescriptionTextArea().setEnabled(false);
			getPriorityComboBox().setEnabled(false);
			getStatusComboBox().setEnabled(false);			
		}		
	}	
	
	@Override
	public void msoObjectCreated(IMsoObjectIf msoObject, int mask) {
		if(msoObject instanceof IHypothesisIf) {
			addHypothesis((IHypothesisIf)msoObject);
		}
	}

	@Override
	public void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		// is same as selected?
		if(msoObject == currentMsoObj) {
			setMsoObject(msoObject);
		}
		else if(msoObject instanceof IHypothesisIf) {
			setHypotesis((IHypothesisIf)msoObject,true,false);
		}
	}

	@Override
	public void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		// is same as selected?
		if(msoObject == currentMsoObj) {
			// reset selection
			currentMsoFeature =null;
			currentMsoObj =null;
			getHypothesisList().setSelectedIndex(0);
		}
		else if(msoObject instanceof IHypothesisIf) {
			removeHypothesis((IHypothesisIf)msoObject);
		}
	}		
	
	public boolean finish() {
		// no mso update allowed?
		if (!isWorking()) {
			setDescription(getDescriptionTextArea().getText(),false,true);
			return true;
		}
		return false;
	}
	
	public void cancel() {
		getDescriptionTextArea().setText(null);
		getHypothesisList().clearSelection();
		getPriorityComboBox().setSelectedItem(0);
		getStatusComboBox().setSelectedItem(0);
	}
		
}  //  @jve:decl-index=0:visual-constraint="10,10"
