package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.util.EnumSet;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class MissionTextDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private DefaultPanel contentPanel = null;
	private JTextArea textArea = null;
	
	private IDiskoWpModule wp = null;
	
	public MissionTextDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare
		this.wp = wp;
		// initialize ui
		initialize();
		// initialise
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

	/**
	 * This method initializes contentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultPanel() {

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean beforeFinish() {
						// update mso model
						setText(textArea.getText(),false,true);
						// finished
						return true;
					}
					
					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {
						// consume changes
						setChangeable(false);
						// initialize
						IOperationAreaIf area = null;
						String remarks = null;
						// set operation area
						if (msoObj instanceof IOperationAreaIf) {
							area = (IOperationAreaIf)msoObj;
							// set current text
							remarks =(area!=null ? area.getRemarks() : null);
						}

						// update
						super.setMsoObject(area);
						setText(remarks,true,false);

						// resume changes
						setChangeable(true);
						
						// update
						setDirty(false,false);
						update();
												
					}	
					
					@Override
					public void update() {
						super.update();
						setup();
					}

					@Override
					public void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
						// is same as selected?
						if(msoObj == msoObject) {
							setMsoObject(msoObject);
						}
					}

					@Override
					public void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
						// is same as selected?
						if(msoObj == msoObject) {
							// reset selection
							setMsoObject(null);
						}
					}	
				};
				
				contentPanel.setInterests(wp.getMsoModel(),getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());				
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				contentPanel.setBodyComponent(getTextArea());
				
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
						if(!isChangeable()) return;
						setDirty(true);
					}
					
				}); 
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return textArea;
	}	
	
	private void setText(String text, boolean gui, boolean mso) {
		// update gui?
		if (gui && !mso) {
			getTextArea().setText(text);
			// request focus
			getTextArea().requestFocus();
		}
		// update mso? (text is update from Mso Update event)
		if(mso) {
			IOperationAreaIf area = (IOperationAreaIf)getMsoObject();
			// has operation object?
			if(area!=null) {
				// update remark
				area.setRemarks(text);
				fireOnWorkChange(area);
			}
		}
	}

	private void setup() {
		// consume?
		if(!isChangeable()) return;
		
		// consume changes
		setChangeable(false);
		
		// try to get mso object?
		if(getMsoObject()==null) 
			getContentPanel().setSelectedMsoFeature(wp.getMap());
		
		// get current area
		IOperationAreaIf area = (IOperationAreaIf)getMsoObject();
		// update icon
		if(area!=null) {
			contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("MAP.POLYGON","48x48"));
			getContentPanel().setCaptionText("<html>Skriv inn ordre fra oppdragsgiver for <b>" + 
					MsoUtils.getOperationAreaName(area,true).toLowerCase() 
					+ "</b></html>");
			getTextArea().setVisible(true);
		}
		else {
			contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et operasjonsområde");			
			getTextArea().setVisible(false);
		}		
		
		// resume changes
		setChangeable(true);
	}	
	
}
