package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.NumPadDialog;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class EstimateDialog extends DiskoDialog implements IMsoLayerEventListener {

	private static final long serialVersionUID = 1L;	
	private JPanel contentPanel = null;
	private JPanel titlePanel = null;
	private JLabel iconLabel = null;
	private JLabel titleLabel = null;
	private JPanel estimatePanel = null;
	private IDiskoWpModule wp = null;
	private JLabel timeLabel = null;
	private JTextField timeTextField = null;
	private ISearchIf currentAssignment = null;

	public EstimateDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(),getMyInterest(),getMyLayers());
		// prepare objects
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
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(800,110));
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
	
	@Override
	public void setVisible(boolean isVisible) {
		if(currentMsoObj==null && isVisible) {
			// notfiy user
			JOptionPane.showMessageDialog(getOwner(),
                "Du må velge et oppdrag i kartet før du kan estimere gåtid i terreng",
                "Objekt mangler", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// allow
		super.setVisible(isVisible);
	}
	
	private void reset() {
		getTimeTextField().setText("");
	}
	
	public int getEstimatedTime() {
		try {
			return Integer.parseInt(getTimeTextField().getText());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
		}
		return 0;
	}
	
	public void setEstimatedTime(int eta) {
		setEstimatedTime(eta,true,true);
	}

	private void setEstimatedTime(int eta, boolean gui, boolean mso) {
		setIsWorking();
		if(gui) {
			getTimeTextField().setText(String.valueOf(eta));
		}
		if(mso) {
			if(currentAssignment!=null) {
				if(currentAssignment.getPlannedProgress()!=eta) {
					currentAssignment.setPlannedProgress(eta);
					fireOnWorkChange(timeTextField,currentAssignment,eta);
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
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new JPanel();
				contentPanel.setLayout(new BorderLayout());
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				contentPanel.add(getTitlePanel(),BorderLayout.NORTH);
				contentPanel.add(getEstimatePanel(), BorderLayout.CENTER);
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
	 * This method initializes estimatePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getEstimatePanel() {
		if (estimatePanel == null) {
			try {
				GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
				gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
				gridBagConstraints1.gridy = 0;
				gridBagConstraints1.weightx = 1.0;
				gridBagConstraints1.anchor = GridBagConstraints.WEST;
				gridBagConstraints1.gridx = 1;
				GridBagConstraints gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.gridx = 0;
				gridBagConstraints.gridy = 0;
				timeLabel = new JLabel();
				timeLabel.setText("Estimert tidsbruk:");
				estimatePanel = new JPanel();
				estimatePanel.setLayout(new GridBagLayout());
				estimatePanel.add(timeLabel, gridBagConstraints);
				estimatePanel.add(getTimeTextField(), gridBagConstraints1);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return estimatePanel;
	}

	/**
	 * This method initializes timeTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTimeTextField() {
		if (timeTextField == null) {
			try {
				timeTextField = new JTextField();
				timeTextField.setText("0");
				timeTextField.setPreferredSize(new Dimension(100, 20));
				timeTextField.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) { apply(); }
					public void insertUpdate(DocumentEvent arg0) { apply(); }
					public void removeUpdate(DocumentEvent arg0) { apply(); }
					private void apply() {
						// no mso update allowed?
						if (isWorking()) return;
						// update mso model
						setEstimatedTime(Integer.getInteger(timeTextField.getText()),false,true);						
					}
				});
				timeTextField.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {					
						if (isWorking()) return;
						if (e.getClickCount() == 2){
							NumPadDialog npDialog = wp.getApplication().
								getUIFactory().getNumPadDialog();
							Point p = timeTextField.getLocationOnScreen();
							p.setLocation(p.x + timeTextField.getWidth()-
									npDialog.getWidth(), p.y-npDialog.getHeight()-2);
							npDialog.setLocation(p);					
							npDialog.setTextField(timeTextField);
							npDialog.setVisible(true);	
						}
					}
				});	
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return timeTextField;
	}

	@Override
	public boolean setMsoObject(IMsoObjectIf msoObj) {
		if(isWorking()) return false;
		// reset
		currentAssignment = null;
		// get owning area
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			if (assignment instanceof ISearchIf) {		
				currentAssignment = (ISearchIf)assignment;
				setEstimatedTime(currentAssignment.getPlannedProgress(),false,true);
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
			titleLabel.setText("<html>Estimer tidsforbruk for <b>" + 
					MsoUtils.getAssignmentName(currentAssignment, 1).toLowerCase() + "</b></html>");
			getTimeTextField().setEnabled(true);
		}
		else {
			iconLabel.setIcon(null);
			titleLabel.setText("Du må først velge et oppdrag");			
			getTimeTextField().setEnabled(false);
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
