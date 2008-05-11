package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.AbstractButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.models.POITableModel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class DescriptionDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private DiskoPanel contentPanel = null;
	private JTable poiTable = null;
	private IDiskoWpModule wp = null;
	private POITableModel tableModel = null;
	private ISearchIf currentAssignment = null;

	public DescriptionDialog(IDiskoWpModule wp) {
		
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(),getMyInterest(),getMyLayers());
		
		// prepare objects
		this.wp = wp;
		
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
            this.setPreferredSize(new Dimension(800, 200));
            this.setContentPane(getContentPanel());
			this.pack();
		}
		catch (java.lang.Throwable e) {
			//  Do Something
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
	private DiskoPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DiskoPanel();
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				AbstractButton button = contentPanel.addButton(
						DiskoButtonFactory.createButton("GENERAL.CANCEL", ButtonSize.NORMAL),"cancel");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// hide me!
						setVisible(false);						
					}					
				});
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
	
	@Override
	public int setMsoObject(IMsoObjectIf msoObj) {
		int state = 0;
		if(isWorking()) return state;
		// get area
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			if (assignment instanceof ISearchIf) {		
				currentAssignment = (ISearchIf)assignment;
				state = 1;
			}
		}
		else {
			// set state
			state = -1;
			// reset assignment
			currentAssignment = null;
		}
		// set current area
		getPOITableModel().setArea(area);
		// forward
		setup();
		// success
		return state;
	}	
	
	private void setup() {
		// update icon
		if(currentAssignment!=null) {
			Enum e = MsoUtils.getType(currentAssignment,true);
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"48x48"));
			getContentPanel().setCaptionText("<html>Punktvis beskrivelse av <b>" + 
					MsoUtils.getAssignmentName(currentAssignment, 1).toLowerCase() + "</b></html>");
			getPoiTable().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");			
			getPoiTable().setEnabled(false);
		}		
	}	

}  //  @jve:decl-index=0:visual-constraint="10,10"
