package org.redcross.sar.gui.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
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

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderer.IconListCellRenderer;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.tool.PositionTool;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class PositionPanel extends DefaultToolPanel {

	private static final long serialVersionUID = 1L;
	
	private static final String SELECTION_ENABLED = "Velg enhet";
	private static final String SELECTION_DISABLED = "Kan ikke endres";
	
	private static final String CREATE_POSITION = "Skriv inn ny posisjon";
	private static final String UPDATE_POSITION = "Endre posisjon nr %s (DTG %s)";

	private IMsoModelIf msoModel;
	private JButton centerAtButton = null;
	private GotoPanel gotoPanel = null;
	private DefaultPanel unitsPanel = null;
	private JList unitList = null;
	
	private TimePos logEntry = null;
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
			gotoPanel.setGotoButtonVisible(false);
			gotoPanel.addDiskoWorkListener(new IDiskoWorkListener() {

				public void onWorkPerformed(DiskoWorkEvent e) {
					
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
							Point point = MapUtil.getEsriPoint(p, getTool().getMap().getSpatialReference());							
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
            unitList.setCellRenderer(new IconListCellRenderer(0,"32x32"));
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
				getGotoPanel().setCaptionText(String.format(UPDATE_POSITION,index+1,DTG.CalToDTG(logEntry.getTime())));
			}
			else {
				p = unit.getPosition();
				getGotoPanel().setCaptionText(CREATE_POSITION);
			}
		}
		
		// update coordinate panel and tool, this will not result in dirty state!
		try {
			setPosition(p);
			getTool().setPoint(p!=null ? MapUtil.getEsriPoint(p, getTool().getMap().getSpatialReference()) : null);
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
			try {
				System.out.println("getLogEntryIndex:: Unit:="+MsoUtils.getUnitName(unit, true) 
						+ " # Point:="+ MapUtil.getMGRSfromPosition(logEntry.getPosition())
						+ " # DTG:="+ logEntry.getDTG()
						+ " # Index:="+ index);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			centerAtButton = DiskoButtonFactory.createButton("MAP.CENTERAT",ButtonSize.NORMAL);			
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
					getTool().getMap().centerAtPosition(p);
					getTool().getMap().flashPosition(p);

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
	
	public void setLogEntry(TimePos logEntry) {
		this.logEntry = logEntry;
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
		// get command post
		ICmdPostIf cp = msoModel.getMsoManager().getCmdPost();		
		// has command post?
		if(cp!=null) {
			// get unit list
			List<IUnitIf> c = cp.getUnitList().selectItems(
					IUnitIf.ACTIVE_UNIT_SELECTOR, IUnitIf.UNIT_TYPE_AND_NUMBER_COMPARATOR);
			// sort objects
			MsoUtils.sortByName(new ArrayList<IMsoObjectIf>(c),1);
			data = c.toArray();
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
			bFlag = true;
			
			// consume change events
			setChangeable(false);
		
			// get unit
			IUnitIf msoUnit = getUnit();
			
			// add or move poi?
			if (msoUnit == null) {
				Utils.showWarning("Du må først velge en enhet");
			} 
			else {
				// is position valid?
				if(getGotoPanel().getCoordinatePanel().isPositionValid()) {
					// get point from coordinates
					Position p = gotoPanel.getCoordinatePanel().getPosition();
					// has point?
					if(p!=null) {
						try {
							// convert and update tool
							Point point = MapUtil.getEsriPoint(p, getTool().getMap().getSpatialReference());							
							// forward
							getTool().setUnit(msoUnit);
							// set point
							getTool().setPoint(point,true);
							// forward
							getTool().finish();
							// finished
							bFlag = true;
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
		setPosition(null);
		setUnit(null);
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
			
			// update attributes
			getGotoPanel().getCoordinatePanel().setPoint(getTool().getPoint());
			
			// only update if unit is selected
			getGotoPanel().getCoordinatePanel().setEnabled(getUnit()!=null);
			
			// update captions
			getUnitsPanel().setCaptionText(isSingleUnitOnly ? "Kan ikke endres" : "Velg enhet");
			
			
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
		update();
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
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
