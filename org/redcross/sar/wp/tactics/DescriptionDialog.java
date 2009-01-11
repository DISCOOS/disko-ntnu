package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.util.EnumSet;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class DescriptionDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private IDiskoWpModule wp;

	private DefaultPanel contentPanel;
	private DiskoTable poiTable;
	private POITableModel tableModel;

	public DescriptionDialog(IDiskoWpModule wp) {

		// forward
		super(wp.getApplication().getFrame());

		// prepare objects
		this.wp = wp;

		// initialize GUI
		initialize();

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
						msoObject = assignment;
						if(area==null) {
							getPOITableModel().disconnectAll();
							getPOITableModel().clear();
						}
						else {
							getPOITableModel().connect(wp.getMsoModel(),area.getAreaPOIs(),IPOIIf.POI_COMPARATOR);
							getPOITableModel().load(area.getAreaPOIs());
						}

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
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "32x32"));
				contentPanel.setInterests(wp.getMsoModel(),getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());
				contentPanel.setContainer(getPOITable());

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}


	private POITableModel getPOITableModel() {
		if (tableModel == null) {
			tableModel = new POITableModel();
		}
		return tableModel;
	}

	/**
	 * This method initializes poiTable
	 *
	 * @return javax.swing.JTable
	 */
	private DiskoTable getPOITable() {
		if (poiTable == null) {
			try {
				poiTable = new DiskoTable(getPOITableModel());
				poiTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				poiTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				poiTable.setColumnSelectionAllowed(false);
				poiTable.setShowVerticalLines(false);
				poiTable.setAutoFitWidths(true);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return poiTable;
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
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"32x32"));
			getContentPanel().setCaptionText("Stegvis beskrivelse av <b>" +
					MsoUtils.getAssignmentName(assignment, 1).toLowerCase() + "</b>");
			getPOITable().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "32x32"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");
			getPOITable().setEnabled(false);
		}

		// resume changes
		setChangeable(true);
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
