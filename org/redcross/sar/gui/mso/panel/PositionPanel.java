package org.redcross.sar.gui.mso.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.DefaultToolPanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.gui.renderer.MsoIconListCellRenderer;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.IToolListener;
import org.redcross.sar.map.event.ToolEvent;
import org.redcross.sar.map.event.ToolEvent.ToolEventType;
import org.redcross.sar.map.tool.PositionTool;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class PositionPanel extends DefaultToolPanel {

	private static final long serialVersionUID = 1L;
	
	private static final String SELECTION_ENABLED = "Velg enhet";
	private static final String SELECTION_DISABLED = "Enhet (låst)";
	
	private static final String CREATE_POSITION = "Skriv inn ny posisjon";
	private static final String UPDATE_POSITION = "Endre posisjon nr %s";

	private IMsoModelIf msoModel;
	private JButton centerAtButton;
	private GotoPanel gotoPanel;
	private DefaultPanel unitsPanel;
	private JList unitList;
	private FieldsPanel optionsPanel;
	private DTGField dtgAttr;
	
	private TimePos logEntry;
	private boolean isSingleUnitOnly = false;
	
	public PositionPanel(PositionTool tool) {
		// forward
		this("Plassér enhet",tool);
	}
	
	public PositionPanel(String caption,PositionTool tool) {
		
		// forward
		super(caption,tool);
		
		// prepare
		this.msoModel = Utils.getApp().getMsoModel();
		
		// listen for IUnitIf changes
		setInterests(Utils.getApp().getMsoModel(),
				EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT));
		
		// initialize gui
		initialize();
						
		// listen for changes in position
		tool.addToolListener(new IToolListener() {

			@Override
			public void onAction(ToolEvent e) {
				if(e.isType(ToolEventType.FINISH_EVENT) && e.getFlags()==1) {
					// update position
					getGotoPanel().getCoordinatePanel().setPoint(getTool().getPoint());					
				}				
				
			}
			
		});
		
		// load units
		loadUnits();
		
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		
		// set preferred body size
		setPreferredBodySize(new Dimension(200,350));
		
		// get body panel
		JPanel panel = (JPanel)getBodyComponent();
		
		// set layout
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

		// build container
		addBodyChild(Box.createVerticalStrut(5));
		addBodyChild(getGotoPanel());
		addBodyChild(Box.createVerticalStrut(5));
		addBodyChild(getUnitsPanel());
		addBodyChild(Box.createVerticalStrut(5));
		
		// add buttons
		insertButton("finish",getCenterAtButton(),"centerat");

	}

	/**
	 * This method initializes fieldPanel	
	 * 	
	 * @return GotoPanel
	 */
	public GotoPanel getGotoPanel() {
		if (gotoPanel == null) {
			gotoPanel = new GotoPanel("Skriv inn posisjon",false);
			gotoPanel.addWorkFlowListener(new IWorkFlowListener() {

				public void onFlowPerformed(WorkFlowEvent e) {
					
					// consume?
					if(!isChangeable()) return;

					// consume changes
					setChangeable(false);
					
					try {
					
						// get position
						Position p = getGotoPanel().getCoordinatePanel().getPosition();
						
						// has point?
						if(p!=null) {
							// convert and update tool
							Point point = MapUtil.getEsriPoint(p.getGeoPos(), getTool().getMap().getSpatialReference());							
							getTool().setPoint(point,true);							
						}
						
					
					} catch (AutomationException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					
					// resume changes
					setChangeable(true);
					
				}
				
			});
		}
		return gotoPanel;
	}
	
	/**
	 * This method initializes fieldPanel	
	 * 	
	 * @return GotoPanel
	 */
	public DefaultPanel getUnitsPanel() {
		if (unitsPanel == null) {
			// create
			unitsPanel = new DefaultPanel(SELECTION_ENABLED,false,false);
			// replace body component
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
            unitList.setCellRenderer(new MsoIconListCellRenderer(0,false,"32x32"));
            unitList.setModel(new DefaultComboBoxModel());
            
            // add listener
            unitList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {

					// consume?
					if(!isChangeable() || e.getValueIsAdjusting()) return;
					
					// forward
					setSelectedUnit();
					
					// forward
					setDirty(true,false);
					
					// force a update
					update();
					
				}
            	
            });
		}
		return unitList;
	}

	/**
	 * This method initializes optionsPanel	
	 * 	
	 * @return {@link FieldsPanel}
	 */
	public FieldsPanel getOptionsPanel() {
		if (optionsPanel == null) {
			try {
				optionsPanel = new FieldsPanel("Egenskaper","Ingen egenskaper funnet",false,false);
				optionsPanel.setPreferredSize(new Dimension(200,25));	
				optionsPanel.addField(getDTGAttr());
				optionsPanel.addWorkFlowListener(new IWorkFlowListener() {

					@Override
					public void onFlowPerformed(WorkFlowEvent e) {

						// consume?
						if(!isChangeable()) return;
						
						// is not dirty?
						if(!isDirty()) {
						
							// update
							setDirty(logEntry!=null ? 
									!logEntry.getTime().equals(getDTGAttr().getValue()) : true);
							
						}
						
					}
					
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return optionsPanel;
	}

	/**
	 * This method initializes nameAttr	
	 * 	
	 * @return {@link TextLineField}
	 */
	public DTGField getDTGAttr() {
		if (dtgAttr == null) {			
			dtgAttr = new DTGField("DTG","DTG",true,35,25,Calendar.getInstance());
		}
		return dtgAttr;
	}	
	
	private void setSelectedUnit() {
		
		// initialize
		Position p = null;
		
		// consume changes
		setChangeable(false);
		
		// get unit and tool
		IUnitIf unit = getUnit();
		PositionTool tool = getTool(); 
		
		// forward
		tool.setUnit(unit);
		
		// has unit?
		if(unit!=null) {
			// try to get log entry index
			int index = getLogEntryIndex(unit);
			// update position with log entry?
			if(index!=-1) {
				p = new Position("",logEntry.getPosition());
				getGotoPanel().setCaptionText(String.format(UPDATE_POSITION,index+1));
			}
			else {
				p = unit.getPosition();
				getGotoPanel().setCaptionText(CREATE_POSITION);
			}
		}
		
		// update coordinate panel and tool, this will not result in dirty state!
		try {
			setPosition(p);
			getTool().setPoint(p!=null ? MapUtil.getEsriPoint(p.getGeoPos(), getTool().getMap().getSpatialReference()) : null);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// resume changes
		setChangeable(true);

	}
	
	private int getLogEntryIndex(IUnitIf unit) {
		int index = -1;
		if(logEntry!=null && unit!=null) {
			ITrackIf msoTrack = unit.getTrack();
			if(msoTrack!=null) {
				Track track = msoTrack.getGeodata();
				if(track!=null)
					return track.find(logEntry);
			}
		}
		return index;
	}
	
	/**
	 * This method initializes CenterAtButton	
	 * 	
	 * @return {@link JButton}
	 */
	private JButton getCenterAtButton() {
		if (centerAtButton == null) {
			centerAtButton = DiskoButtonFactory.createButton("MAP.CENTERAT",ButtonSize.SMALL);			
			centerAtButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// forward
					centerAt();
				}
				
			});
		}
		return centerAtButton;
	}
	
	/* ===========================================
	 * Private methods
	 * ===========================================
	 */
	
	private void centerAt() {
		// has map?
		if(getTool().getMap()!=null) {						
			try {
				// get position 
				Position p = getGotoPanel().getCoordinatePanel().getPosition();
				// center at position?
				if(p!=null) {
					getTool().getMap().centerAtPosition(p.getGeoPos());
					getTool().getMap().flashPosition(p.getGeoPos());

				}
				else
					Utils.showWarning("Du må oppgi korrekte koordinater");
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}
	
	/* ===========================================
	 * Public methods
	 * ===========================================
	 */
	
	public TimePos getLogEntry() {
		return logEntry;
	}
	
	public void getLastKnownPosition() {
		IUnitIf unit = getUnit();
		if(unit!=null) {
			setLogEntry(unit.getLastKnownPosition());
		}
	}
	
	public void setLogEntry(TimePos logEntry) {
		this.logEntry = logEntry;
		getDTGAttr().setValue(logEntry!=null ? logEntry.getTime() : Calendar.getInstance());
	}
	
	
	public Point getPoint() {
		try {
			if(getTool()!=null) 
				return getGotoPanel().getCoordinatePanel().getPoint(getTool().getMap().getSpatialReference());
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed!
		return null;
	}
	
	public void setPoint(Point p) {
		getGotoPanel().getCoordinatePanel().setPoint(p);
	}
	
	public Position getPosition() {
		return getGotoPanel().getCoordinatePanel().getPosition();
	}
	
	public void setPosition(Position p) {
		getGotoPanel().getCoordinatePanel().setPosition(p);
	}
	
	public IUnitIf getUnit() {
		return (IUnitIf)getUnitList().getSelectedValue();
	}
	
	public void setUnit(IUnitIf msoUnit) {
		setChangeable(false);
		if(msoUnit!=null) {
			getUnitList().setSelectedValue(msoUnit,true);
		}
		else {
			getUnitList().clearSelection();
		}
		setChangeable(true);
		setSelectedUnit();
		update();
	}
	
	public boolean isSingleUnitOnly() {
		return isSingleUnitOnly;
	}
	
	public void loadUnit(IUnitIf unit) {
		// consume changes
		setChangeable(false);
		// create new model
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		// add unit
		model.addElement(unit);
		// apply to list
		getUnitList().setModel(model);
		// resume changes
		setChangeable(true);
		// select unit
		setUnit(unit);
		// set flag
		isSingleUnitOnly = true;
		// update caption
		getUnitsPanel().setCaptionText(SELECTION_DISABLED);
	}
	
	public void loadUnits() {
		// initialize
		Object[] data = null;
		// get data?
		if(msoModel.getMsoManager().operationExists()) {
			// get command post
			ICmdPostIf cp = msoModel.getMsoManager().getCmdPost();		
			// has command post?
			if(cp!=null) {
				// get unit list
				List<IUnitIf> c = cp.getUnitList().selectItems(
						IUnitIf.ACTIVE_SELECTOR, IUnitIf.TYPE_AND_NUMBER_COMPARATOR);
				// sort objects
				MsoUtils.sortByName(new ArrayList<IMsoObjectIf>(c),1);
				data = c.toArray();
			}
		}
		// create new model
		DefaultComboBoxModel model = data != null ? new DefaultComboBoxModel(data) : new DefaultComboBoxModel();
		// apply to list
		getUnitList().setModel(model);
		// reset flag
		isSingleUnitOnly = false;
		// update caption
		getUnitsPanel().setCaptionText(SELECTION_ENABLED);
	}
	
	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */

	@Override
	public PositionTool getTool() {
		return (PositionTool)super.getTool();
	}
		
	@Override
	public boolean finish() {
		
		// initialize
		boolean bFlag = isDirty();
		
		// any change?
		if(bFlag) {

			// reset flag
			bFlag = false;
			
			// consume change events
			setChangeable(false);
		
			// get unit
			IUnitIf msoUnit = getUnit();
			
			// get time stamp
			Calendar time = getDTGAttr().getValue();
			
			// is a unit selected?
			if (msoUnit == null) {
				Utils.showWarning("Du må først velge en enhet");
			} 
			// is a valid time stamp supplied?
			else if(time == null){
				Utils.showWarning("Du må oppgi et tidspunkt (DTG)");
			} 
			else {
				// is position valid?
				if(getGotoPanel().getCoordinatePanel().isPositionValid()) {
					// get point from coordinates
					Position p = gotoPanel.getCoordinatePanel().getPosition();
					// has point?
					if(p!=null) {
						try {													
							
							// get tool
							PositionTool tool = getTool();
							
							// set flag
							bFlag = true;
							
							/*
							// try to get log entry index
							int index = getLogEntryIndex(msoUnit);
							
							// will last logged position be updated?
							if(index==-1 || index==msoUnit.getTrack().getTrackPointCount()-1) {
								// prompt user
								int ans = Utils.showConfirm("Endring", "Du er i ferd med å endre den siste kjente posisjonen til " + 
										MsoUtils.getUnitName(msoUnit, false) + ". Vil du fortsette?", 0);
								// update continue state
								bFlag = (ans == JOptionPane.YES_OPTION);
							}
							*/
							
							// continue?
							//if(bFlag) {
							
								// reset flag
								bFlag = false;
								
								// comply to logging regime
								if(logEntry==null) {
									tool.setAttribute(getDTGAttr().getValue(),"LOGTIMESTAMP");
									tool.setAttribute(null,"UPDATETRACKPOSITION");
									tool.setAttribute(true,"LOGPOSITION");
								}
								else {															
									// update log
									tool.setAttribute(getDTGAttr().getValue(),"LOGTIMESTAMP");
									tool.setAttribute(logEntry,"UPDATETRACKPOSITION");
									tool.setAttribute(false,"LOGPOSITION");								
								}							
								
								// convert and update tool
								Point point = MapUtil.getEsriPoint(p.getGeoPos(), tool.getMap().getSpatialReference());
																							
								// forward
								tool.setUnit(msoUnit);
								
								// set point
								tool.setPoint(point,true);		
								
								// finish working
								bFlag = getTool().finish();
									
							//}
							
						} catch (AutomationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}		
					else 
						Utils.showWarning("Ingen posisjon er oppgitt");
				}
				else {
					Utils.showWarning("Oppgitt posisjon finnes ikke");
				}
					
			}
			
			// resume change events
			setChangeable(true);
			
		}
		
		// work performed?
		if(bFlag) {
			// reset bit
			setDirty(false);
			// notify
			fireOnWorkFinish(this, msoObject);
		}
		
		// finished
		return false;
	}
	
	public void reset() {
		// consume change events
		setChangeable(false);
		// forward
		getTool().reset();
		setUnit(null);
		setPoint(getTool().getMap().getCenterPoint());
		getDTGAttr().setValue(logEntry!=null ? logEntry.getTime() : Calendar.getInstance());
		loadUnits();
		setDirty(false);
		// resume change events
		setChangeable(true);
	}	
	
	@Override
	public void update() {
		
		// forward
		super.update();
		
		// consume?
		if(!isChangeable()) return;
		
		// consume changes
 		setChangeable(false);
		
		try {
			
			// only update if unit is selected
			getGotoPanel().getCoordinatePanel().setEnabled(getUnit()!=null);
			
			// update captions
			getUnitsPanel().setCaptionText(isSingleUnitOnly ? SELECTION_DISABLED : SELECTION_ENABLED);
			
			// initialize map?
			if(getGotoPanel().getMap()==null) getGotoPanel().setMap(getTool().getMap());
			// initialize point?
			IPoint p = getGotoPanel().getPoint();
			if(p==null || p.isEmpty()) getGotoPanel().setPoint();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// resume events
		setChangeable(true);
	}
	
	@Override
	public void setMsoObject(IMsoObjectIf msoObj) {		
		// consume?
		if (!isChangeable()) return;
		// update comments and type
		if(msoObj instanceof IUnitIf)
			setUnit((IUnitIf)msoObj);
		else
			setUnit(null);
		// finished
		update();
	}

	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	@Override
	protected void msoObjectCreated(IMsoObjectIf msoObject, int mask) {
		// consume?
		if(!isSingleUnitOnly) return;
		// get model
		DefaultComboBoxModel model = 
			(DefaultComboBoxModel)getUnitList().getModel();
		// not added?
		if(model.getIndexOf(msoObject)<0) {
			model.addElement((IUnitIf)msoObject);
		}
	}
	
	@Override
	protected void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		// forward
		setUnit((IUnitIf)msoObject);
	}

	@Override
	protected void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		// cast to IUnitIf
		IUnitIf unit = (IUnitIf)msoObject;
		// get model
		DefaultComboBoxModel model = 
			(DefaultComboBoxModel)getUnitList().getModel();
		// is current?
		boolean isCurrent = (getUnit()==unit);
		// remove item
		model.removeElement(unit);
		// update list
		getUnitList().setModel(model);
		// reset?
		if(isCurrent) setUnit(null);
	}
	
	@Override
	protected void msoObjectClearAll(IMsoObjectIf msoObject, int mask) {
		loadUnits();
	}
	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
