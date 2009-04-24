package org.redcross.sar.wp.tactics;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.IDiskoRole;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ElementDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.tool.MsoDrawAdapter;
import org.redcross.sar.map.tool.MsoDrawAdapter.IDrawAdapterListener;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.map.tool.IDrawTool.DrawMode;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.mso.event.MsoEvent.Commit;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.CommitException;
import org.redcross.sar.work.WorkPool;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;
import org.redcross.sar.wp.AbstractDiskoWpModule;

/**
 * Implements the DiskoApTaktikk interface
 *
 * @author geira
 *
 */
public class DiskoWpTacticsImpl extends AbstractDiskoWpModule
		implements IDiskoWpTactics, IDrawAdapterListener {

	private JToggleButton elementToggleButton;
	private JToggleButton listToggleButton;
	private JToggleButton missionToggleButton;
	private JToggleButton hypotheseToggleButton;
	private JToggleButton priorityToggleButton;
	private JToggleButton requirementToggleButton;
	private JToggleButton descriptionToggleButton;
	private JToggleButton unitToggleButton;
	private JToggleButton estimateToggleButton;
	private ArrayList<DefaultDialog> dialogs;
	private MissionTextDialog missionTextDialog;
	private HypothesisDialog hypothesesDialog;
	private PriorityDialog priorityDialog;
	private UnitAllocationDialog unitSelectionDialog;
	private RequirementDialog requirementDialog;
	private EstimateDialog estimateDialog;
	private ListDialog listDialog;
	private PromptDialog draftListDialog;
	private DescriptionDialog descriptionDialog;
	private boolean inferNextElement = false;

	/**
	 * Constructs a DiskoApTaktikkImpl
	 * @param rolle A reference to the DiskoRolle
	 *
	 * @throws IllegalClassFormatException
	 */
	public DiskoWpTacticsImpl() throws IllegalClassFormatException {

		// forward
		super(getWpInterests(),getMapLayers());

		// initialize objects
		dialogs = new ArrayList<DefaultDialog>();

		// initialize GUI
		initialize();

	}

	private static EnumSet<MsoClassCode> getWpInterests() {
		EnumSet<MsoClassCode> list = EnumSet.of(MsoClassCode.CLASSCODE_OPERATION);
    	list.add(MsoClassCode.CLASSCODE_AREA);
    	list.add(MsoClassCode.CLASSCODE_ROUTE);
    	list.add(MsoClassCode.CLASSCODE_POI);
    	list.add(MsoClassCode.CLASSCODE_SEARCHAREA);
    	list.add(MsoClassCode.CLASSCODE_OPERATIONAREA);
    	list.add(MsoClassCode.CLASSCODE_ASSIGNMENT);
    	list.add(MsoClassCode.CLASSCODE_UNIT);
		return list;
	}

	private static List<Enum<?>> getMapLayers() {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		list.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
	    return list;
	}

	private void initialize() {

		// get properties
		assignWpBundle(IDiskoWpTactics.class);

		// forward
		installMap();

		// set map as primary layout component
		MapPanel panel = new MapPanel(getMap());
		panel.setNorthBarVisible(true);
		panel.setSouthBarVisible(true);
		panel.setBorder(UIFactory.createBorder());
		layoutComponent(panel);

		// add layout buttons on sub menu
		layoutButton(getElementToggleButton(), true);
		layoutButton(getListToggleButton(), true);
		layoutButton(getMissionToggleButton(), true);
		layoutButton(getPriorityToggleButton(), true);
		layoutButton(getHypotheseToggleButton(), true);
		layoutButton(getRequirementToggleButton(), true);
		layoutButton(getEstimateToggleButton(), true);
		layoutButton(getDescriptionToggleButton(), true);
		layoutButton(getUnitToggleButton(), true);

		// load primary dialogs
		getMissionTextDialog();
		getHypothesesDialog();
		getRequirementDialog();
		
		// get default codes
		EnumSet<MsoClassCode> defaults = MsoDrawAdapter.getSupport();
		
		// remove edit support for units
		defaults.remove(MsoClassCode.CLASSCODE_UNIT);
		
		// install draw support
		getMap().installEditSupport(defaults);

		// register listeners
		getMap().getDrawAdapter().addWorkFlowListener(this);
		getMap().getDrawAdapter().addDrawAdapterListener(this);

		// get element dialog
		ElementDialog dlg = getMap().getElementDialog();
		
		// make unit not editable
		dlg.getElementPanel().setEditable(MsoClassCode.CLASSCODE_UNIT, false);		
		
		// add edit dialog to dialog list
		dialogs.add(dlg);

	}

	private List<Enum<?>> getDefaultNavBarButtons(boolean left, boolean right) {
		List<Enum<?>> myButtons = Utils.getListNoneOf(MapToolType.class);
		if(left){
			myButtons.add(MapToolType.FREEHAND_TOOL);
			myButtons.add(MapToolType.LINE_TOOL);
			myButtons.add(MapToolType.POI_TOOL);
			myButtons.add(MapToolType.ERASE_TOOL);
		}
		if(right) {
			myButtons.add(MapToolType.ZOOM_IN_TOOL);
			myButtons.add(MapToolType.ZOOM_OUT_TOOL);
			myButtons.add(MapToolType.PAN_TOOL);
			myButtons.add(MapToolType.SELECT_TOOL);
			myButtons.add(MapCommandType.ZOOM_FULL_EXTENT_COMMAND);
			myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
			myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
			myButtons.add(MapCommandType.MAP_TOGGLE_COMMAND);
			myButtons.add(MapCommandType.SCALE_COMMAND);
			myButtons.add(MapCommandType.TOC_COMMAND);
			myButtons.add(MapCommandType.GOTO_COMMAND);
		}
		return myButtons;
	}

	public String getCaption() {
		return getBundleText("TACTICS");
	}

	public void activate(IDiskoRole role) {

		// forward
		super.activate(role);

		// update title bar text
		setFrameText("<" + getElementToggleButton().getToolTipText() + ">");

		// setup of navbar needed?
		if(isNavMenuSetupNeeded()) {
			// get set of tools visible for this wp
			setupNavMenu(getDefaultNavBarButtons(true,true),true);
		}

		// set element if editing
		if (!isChanged()) {
			getMap().getDrawAdapter().nextElement();
		}

	}

	public boolean confirmDeactivate() {

		// prevent reentry
		if(isWorking()) {
			// notify
			showWarning("Vent til arbeidsprosessen er ferdig � arbeide");
			// do not allow to deactivate
			return false;
		}

		// validate data
		if(isChanged()) {

			// prompt user
			String[] options = {"Ferdig","Angre","Ikke bytt"};
			int ans = JOptionPane.showOptionDialog(getApplication().getFrame(),
		                "Det er gjort endringer som kan g� tapt. Velg et tiltak f�r du fortsetter",
		                "Bekreft bytte", JOptionPane.YES_NO_CANCEL_OPTION,
		                JOptionPane.QUESTION_MESSAGE,null,options,options[0]);

			// select action
			switch(ans) {
			case JOptionPane.OK_OPTION:
				// validate data
				if(validate()) {
					//schedule the commit task to work pool
					return doCommitWork(false);
				}
				// allow deactive
				return true;
			case JOptionPane.NO_OPTION:
				//schedule the cancel task to work pool
				return doRollbackWork(false);
			case JOptionPane.CANCEL_OPTION:
				return false;
			}

		}
		// allow deactivate
		return true;
	}

	public void deactivate() {
		// hide visible wp dialogs
		hideDialogs(null);
		// forward
		super.deactivate();
	}

	public boolean commit() {

		// drawing in progress?
		if(getMap().getDrawAdapter().isWorkPending()) {
			getMap().getDrawAdapter().finish(false);
		}

		// validate data
		if(validate()) {
			//schedule the commit task to work pool
			if(doCommitWork(false)) {
				return super.commit();
			}
		}
		// failed
		return false;
	}

	private boolean validate() {

		// valid object count
		int validCount = 0;

		// has work pending?
		if(isChanged()) {

			// loop over all changes
			for(int i=0;i<changeStack.size();i++) {
				// get mso object
				IMsoObjectIf msoObj = changeStack.get(i).getMsoObject();
				// is not deleted
				if(msoObj!=null && !msoObj.hasBeenDeleted()) {
					// dispatch current object type
					if (msoObj instanceof IOperationAreaIf) {
						// always OK
						validCount++;
					}
					else if (msoObj instanceof ISearchAreaIf) {
						ISearchAreaIf searchArea = (ISearchAreaIf) msoObj;
						if (searchArea.getSearchAreaHypothesis() == null) {
							Utils.showMessage("Du m� tilordne en hypotese til alle s�ksomr�der");
							try {
								getMap().setSelected(searchArea,true);
							} catch (Exception e) {
								e.printStackTrace();
							}
							HypothesisDialog dialog = getHypothesesDialog();
							dialog.setSnapToLocation((DiskoMap)getMap(),DefaultDialog.POS_SOUTH, DefaultDialog.SIZE_TO_COMPONENT, true, false);
							dialog.setVisible(true);
							return false;
						}
						// valid state
						validCount++;
					}
					else {
						boolean hasArea = false;
						IAreaIf area = null;
						if (msoObj instanceof IRouteIf) {
							area = MsoUtils.getOwningArea((IRouteIf) msoObj);
							hasArea = (area!=null);
						}
						else if (msoObj instanceof IPOIIf) {
							IPOIIf poi = (IPOIIf)msoObj;
							hasArea = IPOIIf.AREA_SET.contains(poi.getType());
							if(hasArea)
								area = MsoUtils.getOwningArea((IPOIIf) msoObj);
						}
						if(hasArea) {
							if(area==null) {
								Utils.showError("Mso modell er ikke konsistent. IAreaIf mangler for IMsoObjectIf " + msoObj);
								return false;
							}
							else {
								// get owning assignment
								IAssignmentIf assignment = area.getOwningAssignment();
								// has no assignment?
								if(assignment==null) {
									Utils.showError("Mso modell er ikke konsistent. IAssignmentIf mangler for IAreaIf " + msoObj);
									return false;
								}
							}
						}
					}
				}
				// valid state
				validCount++;
			}
		}
		else {
			// notify
			Utils.showMessage("Det er ingen endringer � sende");
		}
		// data is not valid
		return validCount>0;
	}

	public boolean rollback() {
		if(rollback(false)) {
			// forward
			return super.rollback();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.geodata.engine.disko.task.DiskoAp#cancel()
	 */
	private boolean rollback(boolean keep) {

		// validate data
		if(isChanged()) {


			// prompt user
			int ans = Utils.showConfirm("Bekreft avbryt",
						"Dette vil angre alle siste endringer. Vil du fortsette?",
		                JOptionPane.YES_NO_OPTION);

			// do a rollback
			if(ans == JOptionPane.OK_OPTION) {
				//schedule the finish task to work pool
				return doRollbackWork(keep);
			}
		}
		else {
			// notify
			Utils.showMessage("Det er ingen endringer � angre");
		}

		// failed
		return false;

	}

	private void reset(boolean keep) {
		try {
			// hide all dialogs
			hideDialogs(null);
			// reset calling wp
			callingWp = null;
			// keep current object selected?
			if(!keep) clearSelected();
			// select next element?
			if(getMap().getSelectionCount(false)==0) getMap().getDrawAdapter().nextElement();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void clearSelected() {
		try {
			IDiskoMap map = getMap();
			map.suspendNotify();
			getMap().clearSelected();
			getMap().refreshMsoLayers();
			map.resumeNotify();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void hideDialogs(JDialog doNotHide) {
		for (int i = 0; i < dialogs.size(); i++) {
			JDialog dialog = (JDialog) dialogs.get(i);
			if (dialog != doNotHide) {
				dialog.setVisible(false);
			}
		}
		((Component)getMap()).repaint();
		/*
		try {
			// repaint map
			getMap().refreshGraphics(null, getMap().getExtent());
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	private MissionTextDialog getMissionTextDialog() {
		if (missionTextDialog == null) {
			missionTextDialog = new MissionTextDialog(this);
			dialogs.add(missionTextDialog);
			missionTextDialog.addWorkFlowListener(new IWorkFlowListener() {

				@Override
				public void onFlowPerformed(WorkFlowEvent e) {
					if(e.isFinish() && inferNextElement)
						getMap().getDrawAdapter().nextElement();
				}

			});
			missionTextDialog.addWorkFlowListener(this);
		}
		return missionTextDialog;
	}

	private HypothesisDialog getHypothesesDialog() {
		if (hypothesesDialog == null) {
			hypothesesDialog = new HypothesisDialog(this);
			dialogs.add(hypothesesDialog);
			hypothesesDialog.addWorkFlowListener(new IWorkFlowListener() {

				@Override
				public void onFlowPerformed(WorkFlowEvent e) {
					if(e.isFinish() && inferNextElement)
						getMap().getDrawAdapter().nextElement();
				}

			});
			hypothesesDialog.addWorkFlowListener(this);

		}
		return hypothesesDialog;
	}

	private PriorityDialog getPriorityDialog() {
		if (priorityDialog == null) {
			priorityDialog = new PriorityDialog(this);
			priorityDialog.addWorkFlowListener(this);
			dialogs.add(priorityDialog);
		}
		return priorityDialog;
	}

	private RequirementDialog getRequirementDialog() {
		if (requirementDialog == null) {
			requirementDialog = new RequirementDialog(this);
			requirementDialog.addWorkFlowListener(this);
			dialogs.add(requirementDialog);
		}
		return requirementDialog;
	}

	private EstimateDialog getEstimateDialog() {
		if (estimateDialog == null) {
			estimateDialog = new EstimateDialog(this);
			dialogs.add(estimateDialog);
			estimateDialog.addWorkFlowListener(this);
		}
		return estimateDialog;
	}

	private DescriptionDialog getDescriptionDialog() {
		if (descriptionDialog == null) {
			descriptionDialog = new DescriptionDialog(this);
			dialogs.add(descriptionDialog);
		}
		return descriptionDialog;
	}

	private UnitAllocationDialog getUnitSelectionDialog() {
		if (unitSelectionDialog == null) {
			unitSelectionDialog = new UnitAllocationDialog(this);
			dialogs.add(unitSelectionDialog);
			unitSelectionDialog.addWorkFlowListener(this);
		}
		return unitSelectionDialog;
	}

	private ListDialog getListDialog() {
		if (listDialog == null) {
			listDialog = new ListDialog(this);
			listDialog.addWorkFlowListener(this);
			dialogs.add(listDialog);
		}
		return listDialog;
	}

	private PromptDialog getDraftListDialog() {
		if (draftListDialog == null) {
			draftListDialog = new PromptDialog(this);
			draftListDialog.setSnapToLocation((DiskoMap)getMap(),DefaultDialog.POS_CENTER, 0, true, false);
			dialogs.add(draftListDialog);
		}
		return draftListDialog;
	}

	private void showOperationAreaButtons() {

		getListToggleButton().setVisible(true);
		getMissionToggleButton().setVisible(true);
		getHypotheseToggleButton().setVisible(false);
		getPriorityToggleButton().setVisible(false);
		getRequirementToggleButton().setVisible(false);
		getEstimateToggleButton().setVisible(false);
		getDescriptionToggleButton().setVisible(false);
		getUnitToggleButton().setVisible(false);

	}

	private void showSearchAreaButtons() {
		getListToggleButton().setVisible(true);
		getMissionToggleButton().setVisible(false);
		getHypotheseToggleButton().setVisible(true);
		getPriorityToggleButton().setVisible(true);
		getRequirementToggleButton().setVisible(false);
		getEstimateToggleButton().setVisible(false);
		getDescriptionToggleButton().setVisible(false);
		getUnitToggleButton().setVisible(false);

	}

	private void showSearchButtons() {
		getListToggleButton().setVisible(true);
		getMissionToggleButton().setVisible(false);
		getHypotheseToggleButton().setVisible(false);
		getPriorityToggleButton().setVisible(false);
		getRequirementToggleButton().setVisible(true);
		getEstimateToggleButton().setVisible(true);
		getDescriptionToggleButton().setVisible(true);
		getUnitToggleButton().setVisible(true);
	}

	private JToggleButton getElementToggleButton() {
		if (elementToggleButton == null) {
			try {
				Enum<?> e = TacticsActionType.MANAGE_ELEMENTS;
				elementToggleButton = DiskoButtonFactory.createToggleButton(e,ButtonSize.NORMAL,wpBundle);
				elementToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ElementDialog dialog = getMap().getElementDialog();
						hideDialogs(dialog);
						if (elementToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setSnapToLocation(
									elementToggleButton, ElementDialog.POS_WEST, 
									ElementDialog.SIZE_TO_OFF,false,true);
							dialog.setVisible(true);
						}
					}
				});

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return elementToggleButton;
	}

	private JToggleButton getDescriptionToggleButton() {
		if (descriptionToggleButton == null) {
			try {
				Enum<?> key = TacticsActionType.SHOW_DESCRIPTION;
				descriptionToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				descriptionToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DescriptionDialog dialog = getDescriptionDialog();
						hideDialogs(dialog);
						if (descriptionToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						} else {
							dialog.setSnapToLocation((JComponent) getMap(),
									DefaultDialog.POS_SOUTH, DefaultDialog.SIZE_TO_COMPONENT, true, false);
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return descriptionToggleButton;
	}

	private JToggleButton getHypotheseToggleButton() {
		if (hypotheseToggleButton == null) {
			try {
				Enum<?> key = TacticsActionType.SET_HYPOTHESIS;
				hypotheseToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				hypotheseToggleButton.setVisible(false);
				hypotheseToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						HypothesisDialog dialog = getHypothesesDialog();
						hideDialogs(dialog);
						JComponent mapComp = (JComponent) getMap();
						if (hypotheseToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
							hypotheseToggleButton.setSelected(false);
						}
						else {
							dialog.setSnapToLocation(mapComp,DefaultDialog.POS_SOUTH, DefaultDialog.SIZE_TO_COMPONENT, true, false);
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hypotheseToggleButton;
	}

	private JToggleButton getListToggleButton() {
		if (listToggleButton == null) {
			try {
				Enum<?> key = TacticsActionType.SHOW_ASSIGNMENT_LIST;
				listToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				listToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ListDialog dialog = getListDialog();
						hideDialogs(dialog);
						JComponent mapComp = (JComponent) getMap();
						if (listToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setSnapToLocation(mapComp,DefaultDialog.POS_CENTER, 0, true, false);
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return listToggleButton;
	}

	private JToggleButton getMissionToggleButton() {
		if (missionToggleButton == null) {
			try {
				Enum<?> key = TacticsActionType.SHOW_MISSION;
				missionToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				missionToggleButton.setVisible(false);
				missionToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MissionTextDialog dialog = getMissionTextDialog();
						hideDialogs(dialog);
						if (missionToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setSnapToLocation((JComponent) getMap(),
									DefaultDialog.POS_SOUTH, DefaultDialog.SIZE_TO_COMPONENT, true, false);
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return missionToggleButton;
	}

	private JToggleButton getPriorityToggleButton() {
		if (priorityToggleButton == null) {
			try {
				Enum<?> key = TacticsActionType.SET_PRIORITY;
				priorityToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				priorityToggleButton.setVisible(false);
				priorityToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						PriorityDialog dialog = getPriorityDialog();
						hideDialogs(dialog);
						if (priorityToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setSnapToLocation(priorityToggleButton, 
									DefaultDialog.POS_WEST, DefaultDialog.SIZE_TO_OFF,false,true);
							
							/*
							java.awt.Point p = priorityToggleButton.getLocationOnScreen();
							p.setLocation(p.x - dialog.getWidth() - 2, p.y);
							dialog.setLocation(p);
							*/
							
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return priorityToggleButton;
	}

	private JToggleButton getRequirementToggleButton() {
		if (requirementToggleButton == null) {
			try {
				Enum<?> key = TacticsActionType.SET_REQUIREMENT;
				requirementToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				requirementToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						RequirementDialog dialog = getRequirementDialog();
						hideDialogs(dialog);
						if (requirementToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setSnapToLocation((JComponent) getMap(),
									DefaultDialog.POS_SOUTH, DefaultDialog.SIZE_TO_COMPONENT, true, false);
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return requirementToggleButton;
	}

	private JToggleButton getEstimateToggleButton() {
		if (estimateToggleButton == null) {
			try {
				Enum<?> key = TacticsActionType.SHOW_ESTIMATES;
				estimateToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				estimateToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						EstimateDialog dialog = getEstimateDialog();
						hideDialogs(dialog);
						if (estimateToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setSnapToLocation(estimateToggleButton, 
									DefaultDialog.POS_WEST, DefaultDialog.SIZE_TO_OFF,false,true);
							/*
							java.awt.Point p = estimateToggleButton.getLocationOnScreen();
							p.setLocation(p.x - dialog.getWidth() - 2, p.y);
							dialog.setLocation(p);							
							*/
							
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return estimateToggleButton;
	}

	JToggleButton getUnitToggleButton() {
		if (unitToggleButton == null) {
			try {
				Enum<?> key = TacticsActionType.ENQUEUE_TO_UNIT;
				unitToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				unitToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						UnitAllocationDialog dialog = getUnitSelectionDialog();
						hideDialogs(dialog);
						if (unitToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setSnapToLocation(
									(JComponent) getMap(), DefaultDialog.POS_SOUTH, 
									DefaultDialog.SIZE_TO_COMPONENT, true, false);
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return unitToggleButton;
	}

	public void afterOperationChange(){
		// forward
		super.afterOperationChange();
		// forward
		reset(false);
		// update title bar text?
		if(isActive()) setFrameText("<" + getElementToggleButton().getToolTipText() + ">");
	}

	public void handleMsoCommitEvent(Commit e) throws CommitException {
		if(!isChanged()) {
			//select next element
			getMap().getDrawAdapter().nextElement();
		}
	}

	public void onElementChange(Enum<?> element, IMsoObjectIf msoObject) {

		// dispatch element
		if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(element)) {
			// setup buttons
			showOperationAreaButtons();
		}
		else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(element)) {
			// setup buttons
			showSearchAreaButtons();
		}
		else if (MsoClassCode.CLASSCODE_POI.equals(element)) {
			// setup buttons
			if(MsoUtils.getOwningArea(msoObject)!=null) {
				showSearchButtons();
				element = MsoClassCode.CLASSCODE_ROUTE;
			}
			else
				showOperationAreaButtons();
		}
		else if(MsoClassCode.CLASSCODE_ROUTE.equals(element) || element instanceof SearchSubType){

			// setup buttons
			showSearchButtons();

		}
		// update toggle button
		AbstractButton b = getElementToggleButton();
		DiskoButtonFactory.setIcon(b, element, "48x48");
		// set tooltip
		String name = DiskoEnumFactory.getText(element);
		String tooltip = DiskoEnumFactory.getTooltip(TacticsActionType.MANAGE_ELEMENTS);
		tooltip = String.format(tooltip,name);
		b.setToolTipText(tooltip);
		// update frame text?
		if(isActive()) setFrameText("<" + tooltip + ">");
	}

	public void onDrawWorkFinished(DrawMode mode, IMsoObjectIf msoObject) {
		// auto show dialog?
		if(DrawMode.MODE_CREATE.equals(mode)) {
			// reset
			inferNextElement = false;
			// get class code
			MsoClassCode code = msoObject.getMsoClassCode();
			// select correct dialog
			if(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code)) {
				getMissionToggleButton().doClick();
				inferNextElement = true;
			}
			else if(MsoClassCode.CLASSCODE_SEARCHAREA.equals(code)) {
				getHypotheseToggleButton().doClick();
				inferNextElement = true;
			}
			else if(MsoUtils.inAssignment(msoObject)) {
				getRequirementToggleButton().doClick();
				inferNextElement = true;
			}
		}
	}

	public void onDrawModeChanged(DrawMode mode, DrawMode oldMode, IMsoObjectIf msoObject) { /* NOP */ }

	private boolean doCommitWork(boolean keep) {
		try {
			// hide dialogs
			hideDialogs(null);
			// prompt user
			if(getDraftListDialog().prompt()!=-1) {
				// forward work
				WorkPool.getInstance().schedule(new TacticsWork(1,keep));
				// do work
				return true;
			}
			// user canceled
			return false;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean doRollbackWork(boolean keep) {
		try {
			WorkPool.getInstance().schedule(new TacticsWork(2,keep));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	class TacticsWork extends ModuleWork {

		private int m_task = 0;
		private boolean m_keep = false;

		/**
		 * Constructor
		 *
		 * @param task
		 */
		TacticsWork(int task, boolean keep) throws Exception {
			super();
			// prepare
			m_task = task;
			m_keep = keep;
		}

		@Override
		public Boolean doWork() {
			try {
				// dispatch task
				switch(m_task) {
				case 1: commit(); return true;
				case 2: rollback(); return true;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public void beforeDone() {

			try {
				// dispatch task
				switch(m_task) {
				case 1: fireOnWorkCommit(); break;
				case 2: fireOnWorkRollback(); break;
				}
				// cleanup
				reset(m_keep);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		private void commit() {
			try{
				getMsoModel().commit();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		private void rollback() {
			try{
				getMsoModel().rollback();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}


}
