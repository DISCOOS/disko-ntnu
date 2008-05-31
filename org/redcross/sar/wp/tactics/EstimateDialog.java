package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.attribute.NumericAttribute;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.panel.AttributesPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
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

	private DefaultPanel contentPanel = null;
	private AttributesPanel estimatePanel = null;
	private NumericAttribute attrEta = null;
	
	private IDiskoWpModule wp = null;

	public EstimateDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare
		this.wp = wp;
		// initialize gui
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
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(400,125));
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
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultPanel() {
					
					private static final long serialVersionUID = 1L;

					@Override
					protected boolean beforeCancel() {
						// forward
						attrEta.load();
						// success
						return true; 
					}

					@Override
					protected boolean beforeFinish() {
						// forward
						return attrEta.save();
					}

					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {
						// consume changes
						setChangeable(false);
						// initialize
						IAttributeIf eta = null;
						IAssignmentIf assignment = null;
						// get owning area
						IAreaIf area = MsoUtils.getOwningArea(msoObj);
						if(area!=null) {
							assignment = area.getOwningAssignment();
							if (assignment instanceof ISearchIf)
								eta = ((ISearchIf)assignment).getPlannedProgressAttribute();
							else
								assignment = null;
						}
						
						// update
						super.setMsoObject(assignment);
						getEtaAttribute().setMsoAttribute(eta);

						// resume changes
						setChangeable(true);
						
						// update
						setDirty(false);						
						
						// request focus
						getEtaAttribute().getTextField().requestFocus();
					}	
					
					@Override
					public void update() {
						super.update();
						setup();
					}
					
					@Override
					public void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
						// is same as selected?
						if(msoObject == this.msoObject) {
							setMsoObject(msoObject);
						}
					}

					@Override
					public void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
						// is same as selected?
						if(msoObject == this.msoObject) {
							// forward
							setMsoObject(null);
						}
					}
					
					
				};				
				contentPanel.setInterests(wp.getMsoModel(),getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());				
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				contentPanel.setBodyComponent(getEstimatePanel());
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	/**
	 * This method initializes estimatePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private AttributesPanel getEstimatePanel() {
		if (estimatePanel == null) {
			try {
				estimatePanel = new AttributesPanel("","",false,false);
				estimatePanel.setHeaderVisible(false);
				estimatePanel.setLayout(new BoxLayout(estimatePanel,BoxLayout.Y_AXIS));
				estimatePanel.setScrollBarPolicies(BasePanel.VERTICAL_SCROLLBAR_NEVER,
						BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
				estimatePanel.addAttribute(getEtaAttribute());
				estimatePanel.addDiskoWorkListener(getContentPanel());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return estimatePanel;
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
				attrEta = new NumericAttribute("ETA","Estimert tidsbruk:",150,"000000",true);
				
				// set numeric properties
				attrEta.setMaxDigits(6);
				attrEta.setDecimalPrecision(0);
				attrEta.setAllowNegative(false);				
				
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attrEta;
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
			Enum e = MsoUtils.getType(assignment,true);
			getContentPanel().setCaptionIcon(
					DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"48x48"));
			getContentPanel().setCaptionText("<html>Estimer tidsforbruk for <b>" + 
					MsoUtils.getAssignmentName(assignment, 1).toLowerCase() + "</b></html>");
			getEtaAttribute().setEnabled(true);
		}
		else {
			contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");			
			getEtaAttribute().setEnabled(false);
		}		
		getEstimatePanel().update();
		
		// resume changes
		setChangeable(true);
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
