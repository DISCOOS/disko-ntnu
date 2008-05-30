package org.redcross.sar.gui.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;

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
import org.redcross.sar.map.tool.PositionTool;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class PositionPanel extends DefaultToolPanel {

	private static final long serialVersionUID = 1L;
	
	private IMsoModelIf msoModel;
	private JButton centerAtButton = null;
	private GotoPanel gotoPanel = null;
	private DefaultPanel unitsPanel = null;
	private JList unitList = null;
	
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
					try {

						// consume?
						if(!isChangeable()) return;
						
						// update tool
						getTool().setPoint(
								gotoPanel.getPositionField().getPoint(
										getTool().getMap().getSpatialReference()));
						
					} catch (AutomationException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
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
			unitsPanel = new DefaultPanel("Velg enhet",false,false);
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
            unitList.setCellRenderer(new IconListCellRenderer(0,"32x32"));
            unitList.setModel(new DefaultComboBoxModel());
            // add listener
            unitList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {

					// consume?
					if(!isChangeable() || e.getValueIsAdjusting()) return;
					
					// consume changes
					setChangeable(false);
					
					// forward
					getTool().setUnit(getUnit());
					
					// resume changes
					setChangeable(true);
					
					// forward
					setDirty(true);
					
				}
            	
            });
		}
		return unitList;
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
				Position p = getGotoPanel().getPositionField().getPosition();
				// center at position?
				if(p!=null) {
					getTool().getMap().centerAtPosition(p);
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
	
	public Point getPoint() {
		try {
			if(getTool()!=null) 
				return getGotoPanel().getPositionField().getPoint(getTool().getMap().getSpatialReference());
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
		getGotoPanel().getPositionField().setPoint(p);
	}
	
	public Position getPosition() {
		return getGotoPanel().getPositionField().getPosition();
	}
	
	public void setPosition(Position p) {
		getGotoPanel().getPositionField().setPosition(p);
	}
	
	public IUnitIf getUnit() {
		return (IUnitIf)getUnitList().getSelectedValue();
	}
	
	public void setUnit(IUnitIf msoUnit) {
		if(msoUnit!=null)
			getUnitList().setSelectedValue(msoUnit,true);
		else
			getUnitList().setSelectedIndex(-1);
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
		// select unit
		setUnit(unit);
		// resume changes
		setChangeable(true);
		// set flag
		isSingleUnitOnly = true;
		// set dirty flag
		setDirty(true);
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
		boolean bFlag = false;
		
		// consume change events
		setChangeable(false);
		
		try {
			// get point from coordinates
			Point point = gotoPanel.getPositionField().getPoint(
					getTool().getMap().getSpatialReference());
			// add or move poi?
			if(point!=null) {
				// get unit
				IUnitIf msoUnit = getUnit();
				// add or move poi?
				if (msoUnit == null) {
					Utils.showWarning("Du må først velge en enhet");
				} else {
					// forward
					getTool().setMsoObject(msoUnit);
					// set point
					getTool().setPoint(point);
					// forward
					getTool().finish();
				}		
				// finished
				bFlag = true;
			}
			else 
				Utils.showWarning("Ingen posisjon er oppgitt");
		} catch (Exception ex) {
			Utils.showWarning("Ugyldig format. Sjekk koordinater og prøv igjen");
		}
		
		// resume change events
		setChangeable(true);
		
		// reset bit?
		if(bFlag) setDirty(false);
		
		// finished
		return bFlag;
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
	
	public void update() {
		
		// forward
		super.update();
		
		// consume changes
		setChangeable(false);
		
		try {
			
			// update attributes
			getGotoPanel().getPositionField().setPoint(getTool().getPoint());
			
			// units state
			getUnitsPanel().setBodyEnabled(getTool().isCreateMode());
			getUnitsPanel().setCaptionText(getTool().isCreateMode() ? "Velg enhet" : "Kan ikke endres");
			
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
		// consume changes
		setChangeable(false);
		// update comments and type
		if(msoObj instanceof IUnitIf)
			setUnit((IUnitIf)msoObj);
		else
			setUnit(null);
		// resume changes
		setChangeable(true);
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
