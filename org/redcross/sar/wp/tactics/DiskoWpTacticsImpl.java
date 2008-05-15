package org.redcross.sar.wp.tactics;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.esri.arcgis.interop.AutomationException;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.ElementDialog;
import org.redcross.sar.gui.map.MapFilterBar;
import org.redcross.sar.gui.map.MapStatusBar;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.DrawAdapter.IDrawAdapterListener;
import org.redcross.sar.map.command.IDrawTool.DrawMode;
import org.redcross.sar.map.command.IDiskoCommand.DiskoCommandType;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.mso.event.MsoEvent.Commit;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.except.CommitException;
import org.redcross.sar.wp.AbstractDiskoWpModule;
  
/**
 * Implements the DiskoApTaktikk interface
 *
 * @author geira
 *
 */
public class DiskoWpTacticsImpl extends AbstractDiskoWpModule
		implements IDiskoWpTactics, IDrawAdapterListener {

	//private Dimension buttonSize = null;
	private JToggleButton elementToggleButton = null;
	private JToggleButton listToggleButton = null;
	private JToggleButton missionToggleButton = null;
	private JToggleButton hypotheseToggleButton = null;
	private JToggleButton priorityToggleButton = null;
	private JToggleButton searchRequirementToggleButton = null;
	private JToggleButton descriptionToggleButton = null;
	private JToggleButton unitToggleButton = null;
	private JToggleButton estimateToggleButton = null;
	private ArrayList<DiskoDialog> dialogs = null;
	private MissionTextDialog missionTextDialog = null;
	private HypothesisDialog hypothesesDialog = null;
	private PriorityDialog priorityDialog = null;
	private UnitSelectionDialog unitSelectionDialog = null;
	private SearchRequirementDialog searchRequirementDialog = null;
	private EstimateDialog estimateDialog = null;
	private ListDialog listDialog = null;
	private DraftListDialog draftListDialog = null;
	private DescriptionDialog descriptionDialog = null;
	
	/**
	 * Constructs a DiskoApTaktikkImpl
	 * @param rolle A reference to the DiskoRolle
	 */
	public DiskoWpTacticsImpl(IDiskoRole rolle) {
		// forward
		super(rolle,getWpInterests(),getMapLayers());
		
		// initialize objects
		dialogs = new ArrayList<DiskoDialog>();
		// init GUI
		initialize();
	}

	private static EnumSet<MsoClassCode> getWpInterests() {
		EnumSet<MsoClassCode> myInterests = EnumSet.of(MsoClassCode.CLASSCODE_OPERATION);
    	myInterests.add(MsoClassCode.CLASSCODE_AREA);    	
    	myInterests.add(MsoClassCode.CLASSCODE_ROUTE);
    	myInterests.add(MsoClassCode.CLASSCODE_POI);
    	myInterests.add(MsoClassCode.CLASSCODE_SEARCHAREA);
    	myInterests.add(MsoClassCode.CLASSCODE_OPERATIONAREA);
    	myInterests.add(MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMapLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers;
		myLayers = EnumSet.of(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
	    return myLayers;
	}
		
	private void initialize() {
		assignWpBundle(IDiskoWpTactics.class);		
		DiskoMap map = (DiskoMap) getMap();
		layoutComponent(DiskoMap.createPanel(map, new MapStatusBar(), 
				new MapFilterBar(), BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
		layoutButton(getElementToggleButton(), true);
		layoutButton(getListToggleButton(), true);
		layoutButton(getMissionToggleButton(), true);
		layoutButton(getPriorityToggleButton(), true);
		layoutButton(getHypotheseToggleButton(), true);
		layoutButton(getSearchRequirementToggleButton(), true);
		layoutButton(getEstimateToggleButton(), true);
		layoutButton(getDescriptionToggleButton(), true);
		layoutButton(getUnitToggleButton(), true);

		// install draw support
		getMap().installEditSupport();
		
		// register listeners
		getMap().getDrawAdapter().addDrawAdapterListener(this);
		
		// add map dialogs
		dialogs.add(getMap().getDrawDialog());
		dialogs.add(getMap().getElementDialog());
		
	}
	
	private List<Enum<?>> getDefaultNavBarButtons(boolean left, boolean right) {
		List<Enum<?>> myButtons = Utils.getListNoneOf(DiskoToolType.class);
		if(left){		
			myButtons.add(DiskoToolType.FREEHAND_TOOL);
			myButtons.add(DiskoToolType.LINE_TOOL);
			myButtons.add(DiskoToolType.POI_TOOL);
			myButtons.add(DiskoToolType.ERASE_TOOL);
		}
		if(right) {
			myButtons.add(DiskoToolType.ZOOM_IN_TOOL);
			myButtons.add(DiskoToolType.ZOOM_OUT_TOOL);
			myButtons.add(DiskoToolType.PAN_TOOL);
			myButtons.add(DiskoToolType.SELECT_FEATURE_TOOL);
			myButtons.add(DiskoCommandType.ZOOM_FULL_EXTENT_COMMAND);
			myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
			myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
			myButtons.add(DiskoCommandType.MAP_TOGGLE_COMMAND);
			myButtons.add(DiskoCommandType.SCALE_COMMAND);
			myButtons.add(DiskoCommandType.TOC_COMMAND);
			myButtons.add(DiskoCommandType.GOTO_COMMAND);
		}
		return myButtons;		
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see com.geodata.engine.disko.task.DiskoAp#getCaption()
	 */
	public String getCaption() {
		return getBundleText("Caption");
	}

	public void activated() {
		
		// forward
		super.activated();
		
		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get set of tools visible for this wp
			setupNavBar(getDefaultNavBarButtons(true,true),true);
		}
				
		// set element if editing
		if (!isChanged()) {
			getMap().getDrawAdapter().nextElement();
		}
		
		// show map
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DiskoMap map = (DiskoMap) getMap();
				map.setVisible(true);
			}			
		});
		
	}

	public boolean confirmDeactivate() {
		
		// prevent reentry
		if(isWorking()) {
			// notify
			showWarning("Vent til arbeidsprosessen er ferdig å arbeide");
			// do not allow to deactivate
			return false;
		}
		
		// validate data
		if(isChanged()) {
							
			// prompt user
			String[] options = {"Ferdig","Angre","Ikke bytt"};
			int ans = JOptionPane.showOptionDialog(getApplication().getFrame(),
		                "Det er gjort endringer som kan gå tapt. Velg et tiltak før du fortsetter",
		                "Bekreft bytte", JOptionPane.YES_NO_CANCEL_OPTION, 
		                JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
			
			// select action
			switch(ans) {
			case JOptionPane.OK_OPTION:
				// validate data
				if(validate()) {
					//schedule the finish task to work pool
					return doFinishWork(false);						
				}
				// allow deactive
				return true;
			case JOptionPane.NO_OPTION:
				//schedule the cancel task to work pool
				return doCancelWork(false);						
			case JOptionPane.CANCEL_OPTION:
				return false;
			}			
			
		}		
		// allow deactivate
		return true;
	}
	
	public void deactivated() {
		super.deactivated();
		// hide map
		DiskoMap map = (DiskoMap) getMap();
		map.setVisible(false);
		hideDialogs(null);
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see com.geodata.engine.disko.task.DiskoAp#finish()
	 */
	public boolean finish() {
		
		// prevent reentry
		if(isWorking()) return false;
		
		// validate data
		if(validate()) {
			//schedule the finish task to work pool
			return doFinishWork(false);						
		}
		// failed
		return false;
	}

	private boolean validate() {
		
		// valid object count
		int validCount = 0;

		// has work pending?
		if(changeList.size()>0) {
			
			// loop over all changes
			for(int i=0;i<changeList.size();i++) {
				// get mso object
				IMsoObjectIf msoObj = changeList.get(i).getMsoObject();
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
							showWarning("Du må tilordne en hypotese til alle søksområder");
							try {
								getMap().setSelected(searchArea,true);
							} catch (Exception e) {
								e.printStackTrace();
							}
							HypothesisDialog dialog = getHypothesesDialog(); 
							dialog.setLocationRelativeTo((DiskoMap)getMap(),DiskoDialog.POS_SOUTH, true,true);
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
							hasArea = (poi.getType() == POIType.START ||
									poi.getType() == POIType.VIA || 
									poi.getType() == POIType.STOP);
							if(hasArea)
								area = MsoUtils.getOwningArea((IPOIIf) msoObj);
						}
						if(hasArea) {
							if(area==null) {
								showWarning("Mso modell er ikke konsistent. IAreaIf mangler for IMsoObjectIf " + msoObj);
								return false;
							}
							else {
								// get owning assignment
								IAssignmentIf assignment = area.getOwningAssignment();
								// has no assignment?
								if(assignment==null) {
									showWarning("Mso modell er ikke konsistent. IAssignmentIf mangler for IAreaIf " + msoObj);						
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
			showWarning("Det er ingen endringer å sende");
		}
		// data is not valid
		return validCount>0;
	}
	/*
	 * (non-Javadoc)
	 * @see com.geodata.engine.disko.task.DiskoAp#cancel()
	 */
	public boolean cancel(boolean keep) {
		
		// prevent reentry
		if(isWorking()) return false;
		
		// validate data
		if(isChanged()) {
				
			
			// prompt user
			int ans = JOptionPane.showConfirmDialog(getApplication().getFrame(),
		                "Dette vil angre alle siste endringer. Vil du fortsette?",
		                "Bekreft avbryt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			// do a rollback
			if(ans == JOptionPane.OK_OPTION) {
				//schedule the finish task to work pool
				return doCancelWork(keep);						
			}
		}
		
		// failed
		return false;
		
	}

	public boolean cancel() {
		// forward
		return cancel(false);		
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
	}

	private MissionTextDialog getMissionTextDialog() {
		if (missionTextDialog == null) {
			missionTextDialog = new MissionTextDialog(this);
			missionTextDialog.addDiskoWorkEventListener(this);
			dialogs.add(missionTextDialog);
		}
		return missionTextDialog;
	}

	private HypothesisDialog getHypothesesDialog() {
		if (hypothesesDialog == null) {
			hypothesesDialog = new HypothesisDialog(this);
			hypothesesDialog.addDiskoWorkEventListener(this);
			dialogs.add(hypothesesDialog);
		}
		return hypothesesDialog;
	}

	private PriorityDialog getPriorityDialog() {
		if (priorityDialog == null) {
			priorityDialog = new PriorityDialog(this);
			priorityDialog.addDiskoWorkEventListener(this);
			dialogs.add(priorityDialog);
		}
		return priorityDialog;
	}

	private SearchRequirementDialog getSearchRequirementDialog() {
		if (searchRequirementDialog == null) {
			searchRequirementDialog = new SearchRequirementDialog(this);
			searchRequirementDialog.addDiskoWorkEventListener(this);
			dialogs.add(searchRequirementDialog);
		}
		return searchRequirementDialog;
	}

	private EstimateDialog getEstimateDialog() {
		if (estimateDialog == null) {
			estimateDialog = new EstimateDialog(this);
			estimateDialog.addDiskoWorkEventListener(this);
			dialogs.add(estimateDialog);
		}
		return estimateDialog;
	}

	private DescriptionDialog getDescriptionDialog() {
		if (descriptionDialog == null) {
			descriptionDialog = new DescriptionDialog(this);
			descriptionDialog.addDiskoWorkEventListener(this);
			dialogs.add(descriptionDialog);
		}
		return descriptionDialog;
	}

	private UnitSelectionDialog getUnitSelectionDialog() {
		if (unitSelectionDialog == null) {
			unitSelectionDialog = new UnitSelectionDialog(this);
			unitSelectionDialog.addDiskoWorkEventListener(this);
			dialogs.add(unitSelectionDialog);
		}
		return unitSelectionDialog;
	}

	private ListDialog getListDialog() {
		if (listDialog == null) {
			listDialog = new ListDialog(this);
			listDialog.addDiskoWorkEventListener(this);
			dialogs.add(listDialog);
		}
		return listDialog;
	}

	private DraftListDialog getDraftListDialog() {
		if (draftListDialog == null) {
			draftListDialog = new DraftListDialog(this);
			draftListDialog.addDiskoWorkEventListener(this);
			draftListDialog.setLocationRelativeTo((DiskoMap)getMap(),DiskoDialog.POS_CENTER, false, true);
			dialogs.add(draftListDialog);
		}
		return draftListDialog;
	}
	
	private void showOperationAreaButtons() {
		
		getListToggleButton().setVisible(true);
		getMissionToggleButton().setVisible(true);
		getHypotheseToggleButton().setVisible(false);
		getPriorityToggleButton().setVisible(false);
		getSearchRequirementToggleButton().setVisible(false);
		getEstimateToggleButton().setVisible(false);
		getDescriptionToggleButton().setVisible(false);
		getUnitToggleButton().setVisible(false);

		// apply change of visible button
		//NavBar navBar = getApplication().getNavBar();
		//navBar.setVisibleButtons(getDefaultNavBarButtons(true, false),true,true);
		
	}

	private void showSearchAreaButtons() {
		getListToggleButton().setVisible(true);
		getMissionToggleButton().setVisible(false);
		getHypotheseToggleButton().setVisible(true);
		getPriorityToggleButton().setVisible(true);
		getSearchRequirementToggleButton().setVisible(false);
		getEstimateToggleButton().setVisible(false);
		getDescriptionToggleButton().setVisible(false);
		getUnitToggleButton().setVisible(false);

		// apply change of visible button
		//NavBar navBar = getApplication().getNavBar();
		//navBar.setVisibleButtons(getDefaultNavBarButtons(true, false),true,true);
		
	}

	private void showSearchButtons() {
		getListToggleButton().setVisible(true);
		getMissionToggleButton().setVisible(false);
		getHypotheseToggleButton().setVisible(false);
		getPriorityToggleButton().setVisible(false);
		getSearchRequirementToggleButton().setVisible(true);
		getEstimateToggleButton().setVisible(true);
		getDescriptionToggleButton().setVisible(true);
		getUnitToggleButton().setVisible(true);
	}
	
	private JToggleButton getElementToggleButton() {
		if (elementToggleButton == null) {
			try {
				Enum e = TacticsActionType.MANAGE_ELEMENTS;
				elementToggleButton = DiskoButtonFactory.createToggleButton(e, ButtonSize.NORMAL,wpBundle);
				elementToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ElementDialog dialog = getMap().getElementDialog();
						hideDialogs(dialog);
						if (elementToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							java.awt.Point p = elementToggleButton.getLocationOnScreen();
							p.setLocation(p.x - dialog.getWidth() - 2, p.y);
							dialog.setLocation(p);
							dialog.setVisible(true);							
						}
					}
				});
				getApplication().getFrame().addComponentListener(new ComponentListener() {
					public void componentHidden(ComponentEvent arg0) {
						update(true);
					}
					public void componentMoved(ComponentEvent arg0) {
						update(false);
					}
					public void componentResized(ComponentEvent arg0) {
						update(false);
					}
					public void componentShown(ComponentEvent arg0) {
						update(false);
					}					
					private void update(boolean hidden) {
						ElementDialog dialog = getMap().getElementDialog();
						if (hidden) {
							dialog.setVisible(false);
						}
						else if(elementToggleButton.isSelected() && dialog.isVisible() && elementToggleButton.isVisible()) {
							java.awt.Point p = elementToggleButton.getLocationOnScreen();
							p.setLocation(p.x - dialog.getWidth() - 2, p.y);
							dialog.setLocation(p);
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
				Enum key = TacticsActionType.SHOW_DESCRIPTION;
				descriptionToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				descriptionToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DescriptionDialog dialog = getDescriptionDialog();
						hideDialogs(dialog);
						if (descriptionToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						} else {
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true, true);
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
				Enum key = TacticsActionType.SET_HYPOTHESIS;
				hypotheseToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				hypotheseToggleButton.setVisible(false);
				hypotheseToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						HypothesisDialog dialog = getHypothesesDialog();
						hideDialogs(dialog);
						JComponent mapComp = (JComponent) getMap();
						if (hypotheseToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setLocationRelativeTo(mapComp,DiskoDialog.POS_SOUTH, true, true);
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
				Enum key = TacticsActionType.SHOW_ASSIGNMENT_LIST;
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
							dialog.setLocationRelativeTo(mapComp,DiskoDialog.POS_CENTER, false, true);
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
				Enum key = TacticsActionType.SHOW_MISSION;
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
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true, true);
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
				Enum key = TacticsActionType.SET_PRIORITY;
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
							java.awt.Point p = priorityToggleButton.getLocationOnScreen();
							p.setLocation(p.x - dialog.getWidth() - 2, p.y);
							dialog.setLocation(p);
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

	private JToggleButton getSearchRequirementToggleButton() {
		if (searchRequirementToggleButton == null) {
			try {
				Enum key = TacticsActionType.SET_REQUIREMENT;
				searchRequirementToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				searchRequirementToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SearchRequirementDialog dialog = getSearchRequirementDialog();
						hideDialogs(dialog);
						if (searchRequirementToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true, true);
							dialog.setVisible(true);
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return searchRequirementToggleButton;
	}

	private JToggleButton getEstimateToggleButton() {
		if (estimateToggleButton == null) {
			try {
				Enum key = TacticsActionType.SHOW_ESTIMATES;
				estimateToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				estimateToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						EstimateDialog dialog = getEstimateDialog();
						hideDialogs(dialog);
						if (estimateToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true, true);
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
				Enum key = TacticsActionType.ENQUEUE_TO_UNIT;
				unitToggleButton = DiskoButtonFactory.createToggleButton(key, ButtonSize.NORMAL, wpBundle);
				unitToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						UnitSelectionDialog dialog = getUnitSelectionDialog();
						hideDialogs(dialog);
						if (unitToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true, true);
							/*
							java.awt.Point p = unitToggleButton.getLocationOnScreen();
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
		return unitToggleButton;
	}

	public void afterOperationChange(){
		super.afterOperationChange();
		if(isChanged())
			cancel(false);
		else
			reset(false);
	}

	public void handleMsoCommitEvent(Commit e) throws CommitException {
		if(!isChanged()) {
			//select next element
			getMap().getDrawAdapter().nextElement();
		}
	}	
	
	public void onElementChange(Enum element, IMsoObjectIf msoObject) {

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
			showOperationAreaButtons();
		}
		else if(element instanceof SearchSubType){ 
			
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
		// update frame text
		setFrameText("<" + tooltip + ">");
	}

	public void onDrawFinished(DrawMode mode, IMsoObjectIf msoObject) {
		// auto show dialog?
		if(DrawMode.MODE_CREATE.equals(mode)) {
			// get class code
			MsoClassCode code = msoObject.getMsoClassCode();
			// select correct dialog
			if(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code)) {
				getMissionToggleButton().doClick();
			}
			else if(MsoClassCode.CLASSCODE_SEARCHAREA.equals(code)) {
				getHypotheseToggleButton().doClick();
			}
			else if(MsoUtils.inAssignment(msoObject)) {
				getSearchRequirementToggleButton().doClick();
			}
		}
	}
	
	public void onDrawModeChange(DrawMode mode, DrawMode oldMode, IMsoObjectIf msoObject) {
		// TODO Implement status bar notifications		
	}

	private boolean doFinishWork(boolean keep) {
		try {
			// hide dialogs
			hideDialogs(null);
			// prompt user 
			if(getDraftListDialog().prompt()!=-1) {
				// forward work
				DiskoWorkPool.getInstance().schedule(new TacticsWork(1,keep));
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
	
	private boolean doCancelWork(boolean keep) {
		try {
			DiskoWorkPool.getInstance().schedule(new TacticsWork(2,keep));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	class TacticsWork extends ModuleWork<Boolean> {

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
				case 1: finish(); return true;
				case 2: cancel(); return true;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public void done() {
			
			try {
				// dispatch task
				switch(m_task) {
				case 1: fireOnWorkFinish(); break;
				case 2: fireOnWorkCancel(); break;
				}
				// cleanup
				reset(m_keep);	
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
			// do the rest
			super.done();
		}
		
		private void finish() {
			try{
				getMsoModel().commit();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private void cancel() {
			try{
				getMsoModel().rollback();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}


}
