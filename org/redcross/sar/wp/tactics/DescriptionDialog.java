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
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableColumn;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.models.POITableModel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.MsoUtils;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.wp.IDiskoWpModule;

public class DescriptionDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPanel = null;
	private JPanel titlePanel = null;
	private JLabel iconLabel = null;
	private JLabel titleLabel = null;
	private JPanel descriptionPanel = null;
	private JScrollPane tableScrollPane = null;
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
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				contentPanel.add(getTitlePanel(),BorderLayout.NORTH);
				contentPanel.add(getDescriptionPanel(), BorderLayout.CENTER);
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
	 * This method initializes descriptionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDescriptionPanel() {
		if (descriptionPanel == null) {
			try {
				descriptionPanel = new JPanel();
				descriptionPanel.setLayout(new BorderLayout());
				descriptionPanel.add(getTableScrollPane(), BorderLayout.CENTER);
				descriptionPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return descriptionPanel;
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
		// get area
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		if(area!=null) {
			IAssignmentIf assignment = area.getOwningAssignment();
			if (assignment instanceof ISearchIf) {		
				currentAssignment = (ISearchIf)assignment;
			}
		}
		// set current area
		getPOITableModel().setArea(area);
		// forward
		setup();
		// success
		return true;
	}	
	
	private void setup() {
		// update icon
		if(currentAssignment!=null) {
			Enum e = MsoUtils.getType(currentAssignment,true);
			iconLabel.setIcon(Utils.getIcon(e));
			titleLabel.setText("<html>Punktvis beskrivelse av <b>" + 
					MsoUtils.getAssignmentName(currentAssignment, 1).toLowerCase() + "</b></html>");
			getDescriptionPanel().setEnabled(true);
		}
		else {
			iconLabel.setIcon(null);
			titleLabel.setText("Du må først velge et oppdrag");			
			getDescriptionPanel().setEnabled(false);
		}		
	}	

}  //  @jve:decl-index=0:visual-constraint="10,10"
