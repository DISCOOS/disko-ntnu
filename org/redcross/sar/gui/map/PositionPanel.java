package org.redcross.sar.gui.map;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderers.IconListCellRenderer;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.command.PositionTool;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.util.mso.Position;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class PositionPanel extends DiskoPanel implements IPropertyPanel,
					IMsoUpdateListenerIf, IMsoLayerEventListener {

	private static final long serialVersionUID = 1L;
	
	private PositionTool tool = null;
	
	private IMsoModelIf msoModel;
	private DiskoPanel captionPanel = null;	
	private DiskoPanel actionsPanel = null;	
	private JPanel optionsPanel = null;	
	private JButton applyButton = null;
	private JButton cancelButton = null;
	private GotoPanel gotoPanel = null;
	private DiskoPanel unitsPanel = null;
	private JList unitList = null;
	
	private boolean isSingleUnitOnly = false;
	
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	
	public PositionPanel(PositionTool tool) {
		// forward
		this("Plassér enhet",tool);
	}
	
	
	public PositionPanel(String caption,PositionTool tool) {
		
		// forward
		super(caption);
		
		// prepare
		this.tool = tool;
		this.msoModel = Utils.getApp().getMsoModel();
		this.myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
		
		// add listeners
		this.msoModel.getEventManager().addClientUpdateListener(this);
		
		// initialize gui
		initialize();
		
		// load units
		loadUnits();
		
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		
		// set preferred body size
		setPreferredBodySize(new Dimension(200,450));
		
		// hide header and borders
		setHeaderVisible(false);
		setBorderVisible(false);
		
		// get body panel
		JPanel panel = (JPanel)getBodyComponent();
		
		// build container
		VerticalFlowLayout vfl = new VerticalFlowLayout();
		vfl.setAlignment(VerticalFlowLayout.LEFT);
		vfl.setHgap(5);
		vfl.setVgap(0);
		panel.setLayout(vfl);
		panel.add(Box.createRigidArea(new Dimension(5,5)));
		panel.add(getCaptionPanel());
		panel.add(Box.createRigidArea(new Dimension(5,5)));
		panel.add(getActionsPanel());
		panel.add(getOptionsPanel());
	}

	public DiskoPanel getCaptionPanel() {
		if (captionPanel == null) {
			try {
				captionPanel = new DiskoPanel(MapUtil.getDrawText(null, null, null));
				captionPanel.setBodyComponent(null);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return captionPanel;
	}

	public DiskoPanel getActionsPanel() {
		if (actionsPanel == null) {
			try {
				actionsPanel = new DiskoPanel("Utfør");
				actionsPanel.addButton(getApplyButton(),"apply");
				actionsPanel.addButton(getCancelButton(),"cancel");
				actionsPanel.setBodyComponent(null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return actionsPanel;
	}

	/**
	 * This method initializes OptionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOptionsPanel() {
		if (optionsPanel == null) {
			// create panel
			optionsPanel = new JPanel();
			// create flow layout
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setAlignment(VerticalFlowLayout.LEFT);
			vfl.setHgap(0);
			vfl.setVgap(5);
			// set flow layout
			optionsPanel.setLayout(vfl); 
			// set preferred size
			optionsPanel.setPreferredSize(new Dimension(200,350));
			// add components
			optionsPanel.add(getGotoPanel());
			optionsPanel.add(getUnitsPanel());
		}
		return optionsPanel;
	}
	
	/**
	 * This method initializes fieldPanel	
	 * 	
	 * @return GotoPanel
	 */
	public GotoPanel getGotoPanel() {
		if (gotoPanel == null) {
			gotoPanel = new GotoPanel("Skriv inn posisjon");
			gotoPanel.setGotoButtonVisible(false);
		}
		return gotoPanel;
	}
	
	/**
	 * This method initializes fieldPanel	
	 * 	
	 * @return GotoPanel
	 */
	public DiskoPanel getUnitsPanel() {
		if (unitsPanel == null) {
			// create
			unitsPanel = new DiskoPanel("Velg enhet");
			// replace body compontent
			unitsPanel.setBodyComponent(getUnitList());
			// set preferred body size
			unitsPanel.setPreferredBodySize(new Dimension(200,200));
		}
		return unitsPanel;
	}
	
	/**
	 * This method initializes unitComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JList getUnitList() {
		if (unitList == null) {
            unitList = new JList();
            unitList.setVisibleRowCount(0);
            unitList.setCellRenderer(new IconListCellRenderer(1,"32x32"));
            unitList.setModel(new DefaultComboBoxModel());
            // add listener
            unitList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {

					// is value adjusting?
					if(e.getValueIsAdjusting()) return;

					// get unit name
					IUnitIf unit = (IUnitIf)unitList.getSelectedValue();
					
					// is unit selected?
					if(unit==null ) {
						// reset panel
						reset();
						// disable options panel
						getOptionsPanel().setEnabled(false);
					}
					else {
						// select unit
						setCurrentUnit(unit);
						// enable poi panel?
						getOptionsPanel().setEnabled(unit!=null);						
					}
				}
            	
            });
		}
		return unitList;
	}

	public JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
				cancelButton.setActionCommand("cancel");
				cancelButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// forward
						cancel();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return cancelButton;
	}
	
	private JButton getApplyButton() {
		if (applyButton == null) {
			try {
				applyButton = DiskoButtonFactory.createButton("GENERAL.FINISH",ButtonSize.NORMAL);
				applyButton.setActionCommand("finish");
				applyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// forward
						apply();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return applyButton;
	}	
	
	public boolean isSingleUnitOnly() {
		return isSingleUnitOnly;
	}
	
	public void loadSingleUnitOnly(IUnitIf unit) {
		// create new model
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		// add unit
		model.addElement(unit);
		// apply to list
		getUnitList().setModel(model);
		// select unit
		setCurrentUnit(unit);
		// set flag
		isSingleUnitOnly = true;
	}
	
	public void loadUnits() {
		// create new model
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		// get command post
		ICmdPostIf cp = msoModel.getMsoManager().getCmdPost();
		// has command post?
		if(cp!=null) {
			// get units
			Collection<IUnitIf> c = cp.getUnitListItems();
			// has units?
			if(c!=null) {
				for(IUnitIf u:c) {
					model.addElement(u);
				}
			}
		}
		// apply to list
		getUnitList().setModel(model);
		// reset flag
		isSingleUnitOnly = false;
	}


	public void cancel() {
		// has revert data?
		if (getCurrentPosition() != null) {
			try {
				// revert
				setCurrentPosition(getCurrentPosition());
			} catch (Exception ex) {
				// consume any exceptions
			}

		} else {
			// revert
			setCurrentPosition(null);
		}		
		// forward
		tool.cancel();
	}
	
	private void apply() {
		// coordinates
		Point point = null;
		try {
			point = getGotoPanel().getPositionField().getPoint();
		} catch (Exception ex) {
			Utils.showWarning("Ugyldig format. Sjekk koordinater og prøv igjen");
		}
		// add or move poi?
		if (tool.getCurrentUnit() == null) {
			Utils.showWarning("Du må først velge en enhet");
		} else {
			tool.setPositionAt(point);
			// forward
			tool.apply();
		}		
	}
	
	public void reset() {
		tool.cancel();
		getGotoPanel().getPositionField().setText(null);
		if(getUnitList().getModel().getSize()>0)
			getUnitList().setSelectedIndex(-1);
	}
	
	public void updatePosition(Point p) {
		getGotoPanel().getPositionField().setPoint(p);
	}
	
	public Position getCurrentPosition() {
		if(tool.getCurrentUnit()!=null)
			return tool.getCurrentUnit().getPosition();
		return null;
	}
	
	public void setCurrentPosition(Position p) {
		if(tool.getCurrentUnit()!=null)
			tool.getCurrentUnit().setPosition(p);
	}
	
	public IUnitIf getCurrentUnit() {
		return tool.getCurrentUnit();
	}
	
	public void setCurrentUnit(IUnitIf unit) {
		// update list
		if(unit!=null) {
			getUnitList().setSelectedValue(unit,true);
		}
		else {
			getUnitList().setSelectedValue(null, false);
		}		
		// set tool unit
		tool.setCurrentUnit((IUnitIf)getUnitList().getSelectedValue());
		// update caption
		//getCaptionPanel().setCaptionText(MsoUtils.getMsoObjectName(unit, 0));

	}	
	
	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}	

	public void handleMsoUpdateEvent(Update e) {
		// get flags
		int mask = e.getEventTypeMask();
        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
		
        // get mso object
        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
        
        // add object?
		if (createdObject && !isSingleUnitOnly) {
			msoObjectCreated(msoObj,mask);
		}
		// is object modified?
		if ( (addedReference || removedReference || modifiedObject)) {
			msoObjectChanged(msoObj,mask);
		}
		// delete object?
		if (deletedObject) {
			msoObjectDeleted(msoObj,mask);		
		}
	}

	private void msoObjectCreated(IMsoObjectIf msoObject, int mask) {
		// get model
		DefaultComboBoxModel model = 
			(DefaultComboBoxModel)getUnitList().getModel();
		// not added?
		if(model.getIndexOf(msoObject)<0) {
			model.addElement((IUnitIf)msoObject);
		}
		getUnitList().setModel(model);
	}
	
	private void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		IUnitIf unit = getCurrentUnit();
		// same as selected?
		if(unit==(IUnitIf)msoObject) {
			setCurrentPosition(unit.getPosition());
		}
	}

	private void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		// cast to IUnitIf
		IUnitIf unit = (IUnitIf)msoObject;
		// get model
		DefaultComboBoxModel model = 
			(DefaultComboBoxModel)getUnitList().getModel();
		// get name of current from local list (name in mso model can be changed)
		// is current?
		boolean isCurrent = (getCurrentUnit()==unit);
		// remove item
		model.removeElement(unit);
		// update list
		getUnitList().setModel(model);
		// reset?
		if(isCurrent)
			setCurrentUnit(null);
	}	
	
	/* ===========================================
	 * IMsoLayerEventListener implementation
	 * ===========================================
	 */

	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		if (tool.getMap() == null || !e.isFinal()) return; 
		com.esri.arcgis.geometry.Point point = null;
		List<IMsoFeature> selection = e.getSelected();
		if (selection != null && selection.size() > 0) {
			IMsoFeature msoFeature = (IMsoFeature)selection.get(0);
			IMsoObjectIf msoObject = msoFeature.getMsoObject();
			if(msoObject instanceof IUnitIf) {
				IUnitIf unit = (IUnitIf)msoObject;
				tool.setCurrentUnit(unit);
				getUnitList().setSelectedValue(unit, true);
				point = (com.esri.arcgis.geometry.Point)msoFeature.getShape();
			}
		}
		getGotoPanel().getPositionField().setPoint(point);
	}
	
	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */

	public void update() { /*NOP*/ }
	
	public void addActionListener(ActionListener listener) { /*NOP*/ }
	public void removeActionListener(ActionListener listener) { /*NOP*/ }

	
}  //  @jve:decl-index=0:visual-constraint="10,10"
