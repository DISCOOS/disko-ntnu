package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.JPanel;

import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.attribute.NumericAttribute;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class EstimateDialog extends DiskoDialog implements IMsoLayerEventListener {

	private static final long serialVersionUID = 1L;	
	private DefaultDiskoPanel contentPanel = null;
	private JPanel estimatePanel = null;
	private NumericAttribute attrEta = null;
	private ISearchIf currentAssignment = null;

	public EstimateDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(),getMyInterest(),getMyLayers());
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
	
	public String getEstimatedTime() {
		try {
			return (String)getEtaAttribute().getValue();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
		}
		return "0";
	}
	
	
	public void setEstimatedTime(int eta) {
		getEtaAttribute().setValue(eta);
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
				contentPanel.setBodyComponent(getEstimatePanel());
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

	public boolean finish() {
		// consume?
		if (isWorking()) return false;
		// forward
		boolean bFlag = attrEta.save();
		// reset flag
		getContentPanel().setDirty(false);
		// hide?
		if(bFlag) setVisible(false);
		// finished
		return bFlag;
	}
	
	public boolean cancel() {
		// consume?
		if (isWorking()) return false;
		// forward
		boolean bFlag = attrEta.load();
		// reset flag
		getContentPanel().setDirty(false);
		// hide
		setVisible(false); 
		// finished
		return bFlag;
	}
	
	/**
	 * This method initializes estimatePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getEstimatePanel() {
		if (estimatePanel == null) {
			try {
				estimatePanel = new JPanel();
				estimatePanel.setLayout(new BorderLayout());
				estimatePanel.add(getEtaAttribute(), BorderLayout.CENTER);
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
				
				// add disko work listener
				attrEta.addDiskoWorkListener(this);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attrEta;
	}

	@Override
	public int setMsoObject(IMsoObjectIf msoObj) {
		int state = 0;
		if(isWorking()) return state;
		// consume changes
		setIsWorking();
		// initialize
		IAttributeIf eta = null;
		// get owning area
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			if (assignment instanceof ISearchIf) {
				state = 1;
				currentAssignment = (ISearchIf)assignment;
				eta = currentAssignment.getPlannedProgressAttribute();
			}
		}
		else {
			state = -1;
			currentAssignment = null;			
		}
		
		// update
		getEtaAttribute().setMsoAttribute(eta);

		// reset state 
		setIsNotWorking();
		getContentPanel().setDirty(false);
		
		// forward
		setup();
		
		// success
		return state;
	}	
	
	private void setup() {
		// update icon
		if(currentAssignment!=null) {
			Enum e = MsoUtils.getType(currentAssignment,true);
			getContentPanel().setCaptionIcon(
					DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"48x48"));
			getContentPanel().setCaptionText("<html>Estimer tidsforbruk for <b>" + 
					MsoUtils.getAssignmentName(currentAssignment, 1).toLowerCase() + "</b></html>");
			getEtaAttribute().setEnabled(true);
		}
		else {
			contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");			
			getEtaAttribute().setEnabled(false);
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
			// forward
			setMsoObject(null);
		}
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
