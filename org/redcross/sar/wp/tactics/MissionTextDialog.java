package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class MissionTextDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private DefaultDiskoPanel contentPanel = null;
	private JTextArea textArea = null;
	
	private IOperationAreaIf currentOperationArea = null;
	
	public MissionTextDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(), getMyInterest(),getMyLayers());
		// initialize ui
		initialize();
		// get selected mso feature
		setSelectedMsoFeature(wp.getMap());
		// forward
		setup();
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
			e.printStackTrace();
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
				fireOnWorkChange(currentOperationArea,text);
			}
		}
		setIsNotWorking();
	}
	
	/**
	 * This method initializes contentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private DefaultDiskoPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultDiskoPanel();
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				contentPanel.setBodyComponent(getTextArea());
				contentPanel.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						if("finish".equalsIgnoreCase(cmd))
							finish();
						else if("cancel".equalsIgnoreCase(cmd))
							cancel();
					}
					
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
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

					public void changedUpdate(DocumentEvent e) { change(); }
					public void insertUpdate(DocumentEvent e) { change(); }
					public void removeUpdate(DocumentEvent e) { change(); }
					
					private void change() {
						if(isWorking()) return;
						getContentPanel().setDirty(true);
					}
					
				}); 
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return textArea;
	}

	public boolean finish() {
		// no mso update allowed?
		if (isWorking()) return false;
		// update mso model
		setText(textArea.getText(),false,true);
		// reset flag
		getContentPanel().setDirty(false);
		// success
		return true;
	}
		
	public boolean cancel() {
		// not allowed?
		if (isWorking()) return false;
		// reset
		setMsoObject(currentMsoObj);
		// reset flag
		getContentPanel().setDirty(false);
		// finished
		return true;
	}
	
	@Override
	public int setMsoObject(IMsoObjectIf msoObj) {
		int state = 0;
		// consume?
		if(isWorking()) return state;
		// consume changes
		setIsWorking();
		// set operation area
		if (msoObj instanceof IOperationAreaIf) {
			currentOperationArea = (IOperationAreaIf)msoObj;
			state = 1;
			// set current text
			setText((currentOperationArea!=null 
					? currentOperationArea.getRemarks() : null),true,false);
		}
		else {
			state = -1;
			reset();
		}

		getContentPanel().setDirty(false);
		setIsNotWorking();
		
		// forward
		setup();
		// success
		return state;
	}	
	
	private void reset() {
		// not allowed?
		if (isWorking()) return;
		// consume changes
		setIsWorking();
		// reset
		textArea.setText(null);
		// reset flag
		getContentPanel().setDirty(false);
		// finished
		setIsNotWorking();
	}
	
	private void setup() {
		// update icon
		if(currentOperationArea!=null) {
			contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("MAP.POLYGON","48x48"));
			getContentPanel().setCaptionText("<html>Skriv inn ordre fra oppdragsgiver for <b>" + 
					MsoUtils.getOperationAreaName(currentOperationArea,true).toLowerCase() 
					+ "</b></html>");
			getTextArea().setVisible(true);
		}
		else {
			contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et operasjonsområde");			
			getTextArea().setVisible(false);
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
