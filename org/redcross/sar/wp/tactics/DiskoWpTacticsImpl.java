package org.redcross.sar.wp.tactics;

import com.esri.arcgis.interop.AutomationException;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkEventListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DrawDialog;
import org.redcross.sar.gui.ElementDialog;
import org.redcross.sar.gui.MapStatusBar;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.DrawAdapter;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.DrawAdapter.DrawMode;
import org.redcross.sar.map.DrawAdapter.IDrawAdapterListener;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.MsoUtils;
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
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.except.CommitException;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.EnumSet;
  
/**
 * Implements the DiskoApTaktikk interface
 *
 * @author geira
 *
 */
public class DiskoWpTacticsImpl extends AbstractDiskoWpModule
		implements IDiskoWpTactics, IDiskoWorkEventListener, IDrawAdapterListener {

	private Dimension buttonSize = null;
	private JToggleButton elementToggleButton = null;
	private JToggleButton listToggleButton = null;
	private JToggleButton missionToggleButton = null;
	private JToggleButton hypotheseToggleButton = null;
	private JToggleButton priorityToggleButton = null;
	private JToggleButton requirementToggleButton = null;
	private JToggleButton descriptionToggleButton = null;
	private JToggleButton unitToggleButton = null;
	private JToggleButton estimateToggleButton = null;
	private DrawAdapter drawAdapter = null;
	private DrawDialog drawDialog = null;
	private ElementDialog elementDialog = null;
	private ArrayList<DiskoDialog> dialogs = null;
	private TextAreaDialog textAreaDialog = null;
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
        buttonSize = getApplication().getUIFactory().getLargeButtonSize();

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
		loadProperties("properties");
		DiskoMap map = (DiskoMap) getMap();
		layoutComponent(MapStatusBar.createPanelWithMapAndStatusBar(map, 
				new MapStatusBar(), BorderLayout.NORTH, 
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
		layoutButton(getElementToggleButton(), true);
		layoutButton(getListToggleButton(), true);
		layoutButton(getMissionToggleButton(), true);
		layoutButton(getPriorityToggleButton(), true);
		layoutButton(getHypotheseToggleButton(), true);
		layoutButton(getRequirementToggleButton(), true);
		layoutButton(getEstimateToggleButton(), true);
		layoutButton(getDescriptionToggleButton(), true);
		layoutButton(getUnitToggleButton(), true);
		//isInitializing = false;
	}
	
	private EnumSet<DiskoToolType> getDefaultNavBarButtons(boolean left, boolean right) {
		EnumSet<DiskoToolType> myButtons = 
			EnumSet.noneOf(DiskoToolType.class);
		if(left){		
			myButtons.add(DiskoToolType.FREEHAND_TOOL);
			myButtons.add(DiskoToolType.LINE_TOOL);
			myButtons.add(DiskoToolType.POI_TOOL);
			myButtons.add(DiskoToolType.ERASE_COMMAND);
		}
		if(right) {
			myButtons.add(DiskoToolType.ZOOM_IN_TOOL);
			myButtons.add(DiskoToolType.ZOOM_OUT_TOOL);
			myButtons.add(DiskoToolType.PAN_TOOL);
			myButtons.add(DiskoToolType.SELECT_FEATURE_TOOL);
			myButtons.add(DiskoToolType.ZOOM_FULL_EXTENT_COMMAND);
			myButtons.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
			myButtons.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
			myButtons.add(DiskoToolType.MAP_TOGGLE_COMMAND);
			myButtons.add(DiskoToolType.SCALE_COMMAND);
			myButtons.add(DiskoToolType.TOC_COMMAND);
			myButtons.add(DiskoToolType.GOTO_COMMAND);
		}
		return myButtons;		
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see com.geodata.engine.disko.task.DiskoAp#getName()
	 */
	public String getName() {
		return "Taktikk";
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
			getDrawAdapter().selectElement();
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

	public void onWorkCancel(DiskoWorkEvent e) {
		return;
	}

	public void onWorkFinish(DiskoWorkEvent e) {
		return;
	}

	public void onWorkChange(DiskoWorkEvent e) {
		fireOnWorkChange(e);
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
							dialog.setLocationRelativeTo((DiskoMap)getMap(),DiskoDialog.POS_SOUTH, true);
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
				return doCancelWork(false);						
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
			// select next element
			getDrawAdapter().selectElement();
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

	private DrawAdapter getDrawAdapter() {
		if(drawAdapter == null) {
			// create draw adapter
			drawAdapter = new DrawAdapter(getApplication());
			// register objects
			drawAdapter.register((DiskoMap)getMap());
			drawAdapter.register(getDrawDialog());
			drawAdapter.register(getElementDialog());			
			// register listeners
			drawAdapter.addDrawAdapterListener(this);			
			drawAdapter.addDiskoWorkEventListener(this);			
		}
		return drawAdapter;
	}
	
	private DrawDialog getDrawDialog() {
		if(drawDialog == null) {
			
			// get draw dialog
	        NavBar navBar = getApplication().getNavBar();
			drawDialog = (DrawDialog)navBar.getDrawHostTool().getDialog();		
									
		}
		return drawDialog;
	}
	
	private ElementDialog getElementDialog() {
		if (elementDialog == null) {
			
			// create
			elementDialog = new ElementDialog(getApplication().getFrame());
			elementDialog.setIsToggable(false);

			// add to dialogs
			dialogs.add(elementDialog);
		}
		return elementDialog;
	}

	private TextAreaDialog getTextAreaDialog() {
		if (textAreaDialog == null) {
			textAreaDialog = new TextAreaDialog(this);
			textAreaDialog.setIsToggable(false);
			textAreaDialog.addDiskoWorkEventListener(this);
			dialogs.add(textAreaDialog);
		}
		return textAreaDialog;
	}

	private HypothesisDialog getHypothesesDialog() {
		if (hypothesesDialog == null) {
			hypothesesDialog = new HypothesisDialog(this);
			hypothesesDialog.setIsToggable(false);
			hypothesesDialog.addDiskoWorkEventListener(this);
			dialogs.add(hypothesesDialog);
		}
		return hypothesesDialog;
	}

	private PriorityDialog getPriorityDialog() {
		if (priorityDialog == null) {
			priorityDialog = new PriorityDialog(this);
			priorityDialog.setIsToggable(false);
			priorityDialog.addDiskoWorkEventListener(this);
			dialogs.add(priorityDialog);
		}
		return priorityDialog;
	}

	private SearchRequirementDialog getSearchRequirementDialog() {
		if (searchRequirementDialog == null) {
			searchRequirementDialog = new SearchRequirementDialog(this);
			searchRequirementDialog.setIsToggable(false);
			searchRequirementDialog.addDiskoWorkEventListener(this);
			dialogs.add(searchRequirementDialog);
		}
		return searchRequirementDialog;
	}

	private EstimateDialog getEstimateDialog() {
		if (estimateDialog == null) {
			estimateDialog = new EstimateDialog(this);
			estimateDialog.setIsToggable(false);
			estimateDialog.addDiskoWorkEventListener(this);
			dialogs.add(estimateDialog);
		}
		return estimateDialog;
	}

	private DescriptionDialog getDescriptionDialog() {
		if (descriptionDialog == null) {
			descriptionDialog = new DescriptionDialog(this);
			descriptionDialog.setIsToggable(false);
			descriptionDialog.addDiskoWorkEventListener(this);
			dialogs.add(descriptionDialog);
		}
		return descriptionDialog;
	}

	private UnitSelectionDialog getUnitSelectionDialog() {
		if (unitSelectionDialog == null) {
			unitSelectionDialog = new UnitSelectionDialog(this);
			unitSelectionDialog.setIsToggable(false);
			unitSelectionDialog.addDiskoWorkEventListener(this);
			dialogs.add(unitSelectionDialog);
		}
		return unitSelectionDialog;
	}

	private ListDialog getListDialog() {
		if (listDialog == null) {
			listDialog = new ListDialog(this);
			listDialog.setIsToggable(false);
			listDialog.addDiskoWorkEventListener(this);
			dialogs.add(listDialog);
		}
		return listDialog;
	}

	private DraftListDialog getDraftListDialog() {
		if (draftListDialog == null) {
			draftListDialog = new DraftListDialog(this);
			draftListDialog.setIsToggable(false);
			draftListDialog.addDiskoWorkEventListener(this);
			draftListDialog.setLocationRelativeTo((DiskoMap)getMap());
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

		// apply change of visible button
		//NavBar navBar = getApplication().getNavBar();
		//navBar.setVisibleButtons(getDefaultNavBarButtons(true, false),true,true);
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

		// apply change of visible button
		//NavBar navBar = getApplication().getNavBar();
		//navBar.setVisibleButtons(getDefaultNavBarButtons(true, false),true,true);
		
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

		// apply change of visible button
		//NavBar navBar = getApplication().getNavBar();
		//navBar.setVisibleButtons(getDefaultNavBarButtons(true, false),true,true);
	}
	
	private JToggleButton getElementToggleButton() {
		if (elementToggleButton == null) {
			try {
				elementToggleButton = new JToggleButton();
				elementToggleButton.setPreferredSize(buttonSize);
				elementToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						/*
						if (isChanged()) {
							Enum element = (Enum) getElementDialog().getElementList().getSelectedValue();
							showWarning("Du må avslutte "+Utils.translate(element)+" først");
							return;
						}
						*/
						ElementDialog dialog = getElementDialog();
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
				descriptionToggleButton = new JToggleButton();
				Enum key = TacticsTaskType.DESCRIPTION_TASK;
				ImageIcon icon = Utils.getIcon(key);
				if (icon != null) {
					descriptionToggleButton.setIcon(icon);
				} else {
					descriptionToggleButton.setText(key.name());
				}
				descriptionToggleButton.setPreferredSize(buttonSize);
				descriptionToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DescriptionDialog dialog = getDescriptionDialog();
						hideDialogs(dialog);
						if (descriptionToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						} else {
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true);
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
				hypotheseToggleButton = new JToggleButton();
				Enum key = TacticsTaskType.HYPOTHESIS_TASK;
				ImageIcon icon = Utils.getIcon(key);
				if (icon != null) {
					hypotheseToggleButton.setIcon(icon);
				} else {
					hypotheseToggleButton.setText(key.name());
				}
				hypotheseToggleButton.setPreferredSize(buttonSize);
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
							dialog.setLocationRelativeTo(mapComp,DiskoDialog.POS_SOUTH, true);
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
				listToggleButton = new JToggleButton();
				Enum key = TacticsTaskType.LIST_TASK;
				ImageIcon icon = Utils.getIcon(key);
				if (icon != null) {
					listToggleButton.setIcon(icon);
				} else {
					listToggleButton.setText(key.name());
				}
				listToggleButton.setPreferredSize(buttonSize);
				listToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ListDialog dialog = getListDialog();
						hideDialogs(dialog);
						JComponent mapComp = (JComponent) getMap();
						if (listToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setLocationRelativeTo(mapComp);
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
				missionToggleButton = new JToggleButton();
				Enum key = TacticsTaskType.MISSON_TASK;
				ImageIcon icon = Utils.getIcon(key);
				if (icon != null) {
					missionToggleButton.setIcon(icon);
				} else {
					missionToggleButton.setText(key.name());
				}
				missionToggleButton.setPreferredSize(buttonSize);
				missionToggleButton.setVisible(false);
				missionToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TextAreaDialog dialog = getTextAreaDialog();
						dialog.setHeaderText("Aksjonens oppdrag:");
						hideDialogs(dialog);
						if (missionToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true);
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
				priorityToggleButton = new JToggleButton();
				Enum key = TacticsTaskType.PRIORITY_TASK;
				ImageIcon icon = Utils.getIcon(key);
				if (icon != null) {
					priorityToggleButton.setIcon(icon);
				} else {
					priorityToggleButton.setText(key.name());
				}
				priorityToggleButton.setPreferredSize(buttonSize);
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

	private JToggleButton getRequirementToggleButton() {
		if (requirementToggleButton == null) {
			try {
				requirementToggleButton = new JToggleButton();
				Enum key = TacticsTaskType.REQUIREMENT_TASK;
				ImageIcon icon = Utils.getIcon(key);
				if (icon != null) {
					requirementToggleButton.setIcon(icon);
				} else {
					requirementToggleButton.setText(key.name());
				}
				requirementToggleButton.setPreferredSize(buttonSize);
				requirementToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SearchRequirementDialog dialog = getSearchRequirementDialog();
						hideDialogs(dialog);
						if (requirementToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true);
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
				estimateToggleButton = new JToggleButton();
				Enum key = TacticsTaskType.ESTIMATE_TASK;
				ImageIcon icon = Utils.getIcon(key);
				if (icon != null) {
					estimateToggleButton.setIcon(icon);
				estimateToggleButton.setToolTipText(Utils.getIconText(key));
				}
				estimateToggleButton.setPreferredSize(buttonSize);
				estimateToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						EstimateDialog dialog = getEstimateDialog();
						hideDialogs(dialog);
						if (estimateToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							dialog.setLocationRelativeTo((JComponent) getMap(),
									DiskoDialog.POS_SOUTH, true);
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
				unitToggleButton = new JToggleButton();
				Enum key = TacticsTaskType.UNIT_TASK;
				ImageIcon icon = Utils.getIcon(key);
				if (icon != null) {
					unitToggleButton.setIcon(icon);
				} else {
					unitToggleButton.setText(key.name());
				}
				unitToggleButton.setPreferredSize(buttonSize);
				unitToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						UnitSelectionDialog dialog = getUnitSelectionDialog();
						hideDialogs(dialog);
						if (unitToggleButton.isSelected() && dialog.isVisible()) {
							dialog.setVisible(false);
						}
						else {
							java.awt.Point p = unitToggleButton.getLocationOnScreen();
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
			getDrawAdapter().selectElement();
		}
	}	
	
	public void onElementChange(Enum element) {

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
		JToggleButton button = getElementToggleButton();
		button.setIcon(Utils.getIcon(element));
		button.setToolTipText(Utils.translate(element));
		// update frame text
		setFrameText("<Metode: "+Utils.translate(element)+">");
	}

	public void onDrawModeChange(DrawMode mode, DrawMode oldMode) {
		// TODO Implement status bar notifications		
	}
	
	public void onWorkChange(Object worker, IMsoObjectIf msoObj, Object data) {
		// forward
		fireOnWorkChange(worker,msoObj,data);		
	}

	private boolean doFinishWork(boolean keep) {
		try {			
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
