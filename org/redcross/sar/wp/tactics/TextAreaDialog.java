package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.MsoUtils;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.wp.IDiskoWpModule;

public class TextAreaDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPanel = null;
	private JScrollPane textAreaScrollPane = null;
	private JTextArea textArea = null;
	private JLabel headerLabel = null;
	
	public TextAreaDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(), getMyInterest(),getMyLayers());
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
            this.setPreferredSize(new Dimension(600, 125));
            this.setContentPane(getContentPanel());
            this.pack();
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}
	
	private static EnumSet<IMsoManagerIf.MsoClassCode> getMyInterest() {
		EnumSet<IMsoManagerIf.MsoClassCode> myInterests 
			= EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA);
		myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMyLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers 
			= EnumSet.of(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
	    return myLayers;
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		if(currentMsoObj==null && isVisible) {
			// notfiy user
			JOptionPane.showMessageDialog(getOwner(),
                "Du må velge et objekt i kartet før du kan skrive inn tekst",
                "Objekt mangler", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		super.setVisible(isVisible);
	}

	private String getText() {
		return getTextArea().getText();
	}
	
	private void setText(String text) {
		setText(text,true,true);
	}
	
	private void setText(String text, boolean gui, boolean mso) {
		setIsWorking();
		// update gui?
		if (gui && !mso) {
			getTextArea().setText(text);
		}
		// update mso? (text is update from Mso Update event)
		if(mso) {
			// dispatch current mso object
			if(currentMsoObj instanceof IOperationAreaIf) {
				// update remark
				((IOperationAreaIf)currentMsoObj).setRemarks(text);
				fireOnWorkChange(getTextArea(),currentMsoObj,text);
			}
			else {
				IAreaIf area = MsoUtils.getOwningArea(currentMsoObj);
				if(area!=null) {
					// get assignment
					IAssignmentIf assignment = ((IAreaIf)currentMsoObj).getOwningAssignment();
					// update remark?
					if(!assignment.getRemarks().equals(text)) {
						assignment.setRemarks(text);
						fireOnWorkChange(getTextArea(),currentMsoObj,text);
					}
				}						
			}
		}
		setIsNotWorking();
	}
	
	public void setHeaderText(String text) {
		headerLabel.setText(text);
	}

	/**
	 * This method initializes contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				headerLabel = new JLabel();
				contentPanel = new JPanel();
				contentPanel.setLayout(new BorderLayout());
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				contentPanel.add(getTextAreaScrollPane(), BorderLayout.CENTER);
				contentPanel.add(headerLabel, BorderLayout.NORTH);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
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
				textAreaScrollPane.setViewportView(getTextArea());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return textAreaScrollPane;
	}

	/**
	 * This method initializes inputTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getTextArea() {
		if (textArea == null) {
			try {
				textArea = new JTextArea();
				textArea.setLineWrap(true);
				textArea.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) { apply(); }
					public void insertUpdate(DocumentEvent arg0) { apply(); }
					public void removeUpdate(DocumentEvent arg0) { apply(); }
					private void apply() {
						// no mso update allowed?
						if (isWorking()) return;
						// update mso model
						setText(textArea.getText(),false,true);						
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return textArea;
	}

	@Override
	public boolean setMsoObject(IMsoObjectIf msoObj) {
		if(isWorking()) return false;
		if (msoObj instanceof IOperationAreaIf) {
			IOperationAreaIf opArea = (IOperationAreaIf)msoObj;
			setText(opArea.getRemarks(),true,false);
			return true;
		}
		else {
			// get owning area
	    	IAreaIf area = MsoUtils.getOwningArea(msoObj);
	    	// found?
	    	if(area!=null) {
	    		// get assignment
				IAssignmentIf assignment = area.getOwningAssignment();
				// has assignment?
				if (assignment != null) {
					setText(assignment.getRemarks(),true,false);
					setEnabled(true);
					return true;
				}
	    	}			
		}
		setEnabled(true);
		// reset current text
		setText(null);
		// not selected
		return false;
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
			setText(null);
		}
	}	
}
