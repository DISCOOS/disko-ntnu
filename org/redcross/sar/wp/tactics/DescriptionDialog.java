package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableColumn;

import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.models.POITableModel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.MsoUtils;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.wp.IDiskoWpModule;

public class DescriptionDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPanel = null;
	private JScrollPane tableScrollPane = null;
	private JTable poiTable = null;
	private IDiskoWpModule wp = null;
	private POITableModel tableModel = null;

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
            this.setPreferredSize(new Dimension(800, 150));
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
	
	@Override
	public void setVisible(boolean isVisible) {
		if(currentMsoObj==null && isVisible) {
			// notfiy user
			JOptionPane.showMessageDialog(getOwner(),
                "Du må velge et oppdrag i kartet før du kan endre beskrivelse",
                "Objekt mangler", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		super.setVisible(isVisible);
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
				contentPanel.add(getTableScrollPane(), BorderLayout.CENTER);
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	/**
	 * This method initializes tableScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getTableScrollPane() {
		if (tableScrollPane == null) {
			try {
				tableScrollPane = new JScrollPane();
				tableScrollPane.setViewportView(getPoiTable());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return tableScrollPane;
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
			column.setResizable(false);
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
	public boolean setMsoObject(IMsoObjectIf msoObj) {
		if(isWorking()) return false;
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		if(area!=null) {
    		getPOITableModel().setArea(area);
			return true;
		}
		// reset current values
		getPOITableModel().setArea(null);
		// not selected
		return false;
	}		

}  //  @jve:decl-index=0:visual-constraint="10,10"
