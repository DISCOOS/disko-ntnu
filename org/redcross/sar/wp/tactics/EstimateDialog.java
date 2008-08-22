package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import org.redcross.sar.app.Utils;
import org.redcross.sar.ds.DiskoDecisionSupport;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.gui.attribute.NumericAttribute;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.TrackDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.AttributesPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class EstimateDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;	

	private AttributesPanel contentPanel = null;
	private NumericAttribute attrEta = null;
	private TrackDialog trackDialog = null;
	
	private IDiskoWpModule wp = null;

	public EstimateDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare
		this.wp = wp;
		// initialize GUI
		initialize();
		// initialize
		setup();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		try {
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(400,150));
            this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
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
	private AttributesPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new AttributesPanel("Egenskaper",
						"Ingen egenskaper funnet",false,true) {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {
						// consume changes
						setChangeable(false);
						// initialize
						IAttributeIf<?> eta = null;
						IAssignmentIf assignment = null;
						// get owning area
						IAreaIf area = MsoUtils.getOwningArea(msoObj);
						if(area!=null) {
							assignment = area.getOwningAssignment();
							if (assignment instanceof ISearchIf)
								eta = ((ISearchIf)assignment).getTimeEstimatedFinishedAttribute();
							else
								assignment = null;
						}
						
						// update
						super.setMsoObject(assignment);
						getEtaAttribute().setMsoAttribute(eta);

						// resume changes
						setChangeable(true);

						// update
						setDirty(false,false);						
						update();
						
						// request focus
						getEtaAttribute().getTextField().requestFocus();
					}	
					
					@Override
					public void update() {
						super.update();
						setup();
					}
										
				};				
				contentPanel.setInterests(wp.getMsoModel(),getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());				
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				contentPanel.setScrollBarPolicies(BasePanel.VERTICAL_SCROLLBAR_NEVER,
						BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
				contentPanel.setPreferredBodySize(new Dimension(400,100));
				contentPanel.addAttribute(getEtaAttribute());
				Utils.setFixedSize(getEtaAttribute(), 400,32);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	/**
	 * This method initializes EtaAttribute	
	 * 	
	 * @return NumericAttribute	
	 */
	private NumericAttribute getEtaAttribute() {
		if (attrEta == null) {
			try {
				
				// create attribute
				attrEta = new NumericAttribute("ETA","Estimert tidsbruk",120,"000000",false);
				
				// set numeric properties
				attrEta.setMaxDigits(6);
				attrEta.setDecimalPrecision(0);
				attrEta.setAllowNegative(false);
				attrEta.setButton(DiskoButtonFactory.createButton("GENERAL.VIEW", ButtonSize.SMALL), true);
				attrEta.getButton().setVisible(true);
				attrEta.getButton().setEnabled(true);
				attrEta.getButton().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if(getMsoObject()!=null) {
							try {
								IAssignmentIf assignment = (IAssignmentIf)getMsoObject();
								String oprID = Utils.getApp().getMsoModel().getModelDriver().getActiveOperationID();
								RouteCostEstimator estimator = (RouteCostEstimator)DiskoDecisionSupport.getInstance().getEstimator(RouteCostEstimator.class, oprID);
								getTrackDialog().load(wp.getMap(),assignment,estimator.getCost(assignment).getEstimatedTrack());
								getTrackDialog().setVisible(true);
							} catch (Exception ex) {
								// TODO Auto-generated catch block
								ex.printStackTrace();
							}
						}
						
					}
					
				});
				
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attrEta;
	}

	/**
	 * This method initializes trackDialog
	 * 	
	 * @return TrackDialog
	 */
	public TrackDialog getTrackDialog() {
		if (trackDialog == null) {
			// create panels
			trackDialog = new TrackDialog(Utils.getApp().getFrame());
			trackDialog.setLocationRelativeTo(Utils.getApp().getFrame());

		}
		return trackDialog;
	}
	
	private void setup() {

		// consume?
		if(!isChangeable()) return;
		
		// consume changes
		setChangeable(false);
		
		// try to get mso object?
		if(getMsoObject()==null)
			getContentPanel().setSelectedMsoFeature(wp.getMap());
		
		// get mso object
		IAssignmentIf assignment = (IAssignmentIf)getMsoObject();
		
		// update icon
		if(assignment!=null) {
			Enum<?> e = MsoUtils.getType(assignment,true);
			getContentPanel().setCaptionIcon(
					DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"48x48"));
			getContentPanel().setCaptionText("<html>Estimer tidsforbruk for <b>" + 
					MsoUtils.getAssignmentName(assignment, 1).toLowerCase() + "</b></html>");
			getEtaAttribute().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");			
			getEtaAttribute().setEnabled(false);
		}		
		getContentPanel().update();
		
		// resume changes
		setChangeable(true);
		
		// load from mso
		getEtaAttribute().load();
				
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
