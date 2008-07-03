package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.util.EnumSet;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.model.mso.POITableModel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class DescriptionDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;
	private DefaultPanel contentPanel = null;
	private JTable poiTable = null;
	private IDiskoWpModule wp = null;
	private POITableModel tableModel = null;

	public DescriptionDialog(IDiskoWpModule wp) {
		
		// forward
		super(wp.getApplication().getFrame());
		
		// prepare objects
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
			= EnumSet.noneOf(IMsoManagerIf.MsoClassCode.class);
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
				
				// create a default table
				contentPanel = new DefaultPanel("",false,true) { 					

					private static final long serialVersionUID = 1L;

					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {
						// consume changes
						setChangeable(false);
						// initialize
						IAssignmentIf assignment = null;
						// get owning area
						IAreaIf area = MsoUtils.getOwningArea(msoObj);
						if(area!=null) {
							assignment = area.getOwningAssignment();
							if (assignment instanceof ISearchIf)
								assignment = (ISearchIf)assignment;
							else
								assignment = null;
						}
						
						// update
						super.setMsoObject(assignment);
						getPOITableModel().setArea(area);

						// resume changes
						setChangeable(true);
						
						// update
						setDirty(false,false);
						update();
						
					}	
					
					@Override
					public void update() {
						// forward
						setup();
						// finally, consume this to ensure that
						// any dirty state does not update the 
						// button icons accordingly.
					}
					
				};
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				contentPanel.setInterests(wp.getMsoModel(),getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());				
				contentPanel.setBodyComponent(getPoiTable());
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	
	private POITableModel getPOITableModel() {
		if (tableModel == null) {
			tableModel = new POITableModel(wp.getMsoModel());
		}
		return tableModel;
	}

	/**
	 * This method initializes poiTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getPoiTable() {
		if (poiTable == null) {
			try {
				poiTable = new JTable(getPOITableModel());
				poiTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				poiTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				poiTable.setColumnSelectionAllowed(false);
				poiTable.setShowVerticalLines(false);
				setColumnWidths();
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return poiTable;
	}
	
	private void setColumnWidths() {
		for (int i = 0; i < 4; i++) {
			TableColumn column = getPoiTable().getColumnModel().getColumn(i);
			switch(i) {
				case 0: 
					column.setPreferredWidth(15);
					break;
				case 1:
					column.setPreferredWidth(25);
					break;
				case 2:
					column.setPreferredWidth(50);
					break;
				case 3: 
					column.setPreferredWidth(500);
					break;
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
		
		// get mso object
		IAssignmentIf assignment = (IAssignmentIf)getMsoObject();
		
		// update icon
		if(assignment!=null) {
			Enum e = MsoUtils.getType(assignment,true);
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"48x48"));
			getContentPanel().setCaptionText("<html>Stegvis beskrivelse av <b>" + 
					MsoUtils.getAssignmentName(assignment, 1).toLowerCase() + "</b></html>");
			getPoiTable().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");			
			getPoiTable().setEnabled(false);
		}
		
		// resume changes
		setChangeable(true);
	}	
	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
