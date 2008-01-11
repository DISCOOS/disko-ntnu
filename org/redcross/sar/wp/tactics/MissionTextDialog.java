package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class MissionTextDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPanel = null;
	private JPanel titlePanel = null;
	private JLabel iconLabel = null;
	private JLabel titleLabel = null;
	private JPanel textPanel = null;
	private JScrollPane textAreaScrollPane = null;
	private JTextArea textArea = null;
	private JLabel headerLabel = null;
	
	private IOperationAreaIf currentOperationArea = null;
	
	public MissionTextDialog(IDiskoWpModule wp) {
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
            this.setPreferredSize(new Dimension(600, 200));
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
			// has operation object?
			if(currentOperationArea!=null) {
				// update remark
				currentOperationArea.setRemarks(text);
				fireOnWorkChange(getTextArea(),currentOperationArea,text);
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
				contentPanel = new JPanel();
				contentPanel.setLayout(new BorderLayout());
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				contentPanel.add(getTitlePanel(),BorderLayout.NORTH);
				contentPanel.add(getTextPanel(), BorderLayout.CENTER);
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
	 * This method initializes textPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getTextPanel() {
		if (textPanel == null) {
			try {
				headerLabel = new JLabel();
				textPanel = new JPanel();
				textPanel.setLayout(new BorderLayout());
				textPanel.add(getTextAreaScrollPane(), BorderLayout.CENTER);
				textPanel.add(headerLabel, BorderLayout.NORTH);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return textPanel;
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
		// reset
		currentOperationArea = null;
		// set operation area
		if (msoObj instanceof IOperationAreaIf) {
			currentOperationArea = (IOperationAreaIf)msoObj;
		}
		// set current text
		setText((currentOperationArea!=null 
				? currentOperationArea.getRemarks() : null),true,false);
		// forward
		setup();
		// success
		return true;
	}	
	
	private void setup() {
		// update icon
		if(currentOperationArea!=null) {
			iconLabel.setIcon(DiskoIconFactory.getIcon("MAP.POLYGON","48x48"));
			titleLabel.setText("<html>Skriv inn ordre for <b>" + 
					MsoUtils.getOperationAreaName(currentOperationArea,true).toLowerCase() 
					+ "</b> fra oppdragsgiver</html>");
			getTextPanel().setEnabled(true);
		}
		else {
			iconLabel.setIcon(null);
			titleLabel.setText("Du må først velge et operasjonsområde");			
			getTextPanel().setEnabled(false);
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
			setText(null);
		}
	}	
}
