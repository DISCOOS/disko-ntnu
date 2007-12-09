package org.redcross.sar.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.POIField;
import org.redcross.sar.gui.renderers.IconListCellRenderer;
import org.redcross.sar.map.command.PositionTool;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
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

public class PositionPanel extends JPanel implements IMsoUpdateListenerIf, 
							IMsoLayerEventListener {

	private static final long serialVersionUID = 1L;
	private IDiskoApplication app = null;
	private PositionTool tool = null;
	private IMsoModelIf msoModel;
	private NumPadDialog numPadDialog = null;
	private POIField poiField = null;
	private POIFormatPanel formatPanel = null;
	private JList unitList = null;
	private JPanel coordPanel = null;
	private JSeparator listSeparator = null;
	private JPanel northPanel = null;
	private JPanel fieldPanel = null;
	private JPanel buttonPanel = null;
	private JButton applyButton = null;
	private JButton cancelButton = null;
	private JScrollPane listScrollPane = null;
	private boolean isVertical = true;
	private boolean isSingleUnitOnly = false;
	
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	
	public PositionPanel(PositionTool tool, boolean isVertical) {
		
		// prepare
		this.app = Utils.getApp();
		this.tool = tool;
		this.msoModel = app.getMsoModel();
		this.myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
		
		// add listeners
		app.getMsoModel().getEventManager().addClientUpdateListener(this);
		
		// set layout information
		this.isVertical = isVertical;
		
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
		try {
			// remove all from container
			removeAll();
			// build container
			if(isVertical) {
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setAlignment(VerticalFlowLayout.LEFT);
				vfl.setHgap(0);
				vfl.setVgap(5);
				this.setPreferredSize(new Dimension(200,450));
				this.setLayout(vfl);
				this.add(getNorthPanel());
				this.add(getButtonPanel());
			}
			else {
				BorderLayout bl = new BorderLayout();
				bl.setHgap(5);
				bl.setVgap(0);
				this.setLayout(bl);
				this.add(getNorthPanel(),BorderLayout.CENTER);
				this.add(getButtonPanel(),BorderLayout.EAST);				
			}
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	
	public void cancel() {
		// has revert data?
		if (getCurrentPosition() != null) {
			try {
				// revert
				setCurrentPosition(getCurrentPosition());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(app.getFrame(),
						"Ugyldig koordinat format", null, 
						JOptionPane.WARNING_MESSAGE);
				ex.printStackTrace();
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
			point = getPOIField().getPoint();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(app.getFrame(),
					"Ugyldig MGRS koordinat", null, 
					JOptionPane.WARNING_MESSAGE);
			ex.printStackTrace();
		}
		// add or move poi?
		if (tool.getCurrentUnit() == null) {
			Utils.showWarning("Du må først velge en enhet");
		} else {
			tool.setPositionAt(point);
			// apply change?
			if(tool.isBuffered())
				tool.apply();
		}		
	}
	
	public void setVertical(boolean isVertical) {
		// is changed?
		if(this.isVertical != isVertical) {
			this.isVertical = isVertical;
			// initialize gui
			initialize();
		}
	}
	
	public boolean isVertical() {
		return isVertical;
	}
	
	public void reset() {
		tool.cancel();
		getPOIField().setText(null);
		if(getUnitList().getModel().getSize()>0)
			getUnitList().setSelectedIndex(-1);
	}

	/**
	 * This method initializes listScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getListScrollPane() {
		if (listScrollPane == null) {
			try {
				listScrollPane = new JScrollPane(getUnitList());
				listScrollPane.setWheelScrollingEnabled(true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return listScrollPane;
	}
	
	/**
	 * This method initializes fieldPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	public JPanel getFieldPanel() {
		if (fieldPanel == null) {
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setAlignment(VerticalFlowLayout.LEFT);
			fieldPanel = new JPanel();
			fieldPanel.setLayout(vfl);
			fieldPanel.add(getPOIField(),null);
			fieldPanel.add(new JSeparator(JSeparator.HORIZONTAL),null);
			fieldPanel.add(getFormatPanel(),null);
		}
		return fieldPanel;
	}
	
	/**
	 * This method initializes poiField	
	 * 	
	 * @return POIField	
	 */
	public POIField getPOIField() {
		if (poiField == null) {
			poiField = new POIField();
			poiField.registrate(getFormatPanel());
			/*poiField.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {					
					if (e.getClickCount() == 2){
						NumPadDialog numPadDialog = app.getUIFactory().getNumPadDialog();
						java.awt.Point p = poiField.getLocationOnScreen();
						p.setLocation(p.x + (poiField.getWidth()+7), p.y);
						numPadDialog.setLocation(p);					
						numPadDialog.setTextField(poiField);
						numPadDialog.setVisible(true);	
					}
				}
			});*/
		}
		return poiField;
	}
	
	/**
	 * This method initializes formatPanel	
	 * 	
	 * @return POIFormatPanel
	 */
	public POIFormatPanel getFormatPanel() {
		if (formatPanel == null) {
			formatPanel = new POIFormatPanel();
		}
		return formatPanel;
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

	public void updatePosition(Point p) {
		poiField.setPoint(p);
	}
	
	public boolean isUnitsVisible() {
		return getUnitList().isVisible();
	}
	
	public boolean isButtonsVisible() {
		return getButtonPanel().isVisible();
	}
	
	public void setUnitsVisible(boolean isVisible) {
		getUnitList().setVisible(isVisible);
		listSeparator.setVisible(isVisible && isVertical);
	}
	
	public void setButtonsVisible(boolean isVisible) {
		getButtonPanel().setVisible(isVisible);
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
            unitList.setCellRenderer(new IconListCellRenderer(1));
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
						// disable poi panel
						getCoordPanel().setEnabled(false);
					}
					else {
						// select unit
						setCurrentUnit(unit);
						// enable poi panel?
						getCoordPanel().setEnabled(unit!=null);						
					}
				}
            	
            });
		}
		return unitList;
	}
	
	/**
	 * This method initializes centerPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCoordPanel() {
		if (coordPanel == null) {
			try {

				// create panel
				coordPanel = new JPanel();

				// create separators
				listSeparator = new JSeparator(JSeparator.HORIZONTAL);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		coordPanel.removeAll();
		if(isVertical) {
			
			// create flow layout
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setAlignment(VerticalFlowLayout.LEFT);
			vfl.setHgap(0);
			vfl.setVgap(5);
			// set flow layout
			coordPanel.setLayout(vfl); 
			// add components
			coordPanel.add(getFieldPanel());
			coordPanel.add(listSeparator);
			coordPanel.add(getListScrollPane());			
		}
		else {
			// create flow layout
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(0);
			// set flow layout
			coordPanel.setLayout(bl); 
			// add components
			coordPanel.add(getFieldPanel(),BorderLayout.WEST);
			coordPanel.add(getListScrollPane(),BorderLayout.CENTER);
		}
		return coordPanel;
	}

	/**
	 * This method initializes northPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getNorthPanel() {
		if (northPanel == null) {
			try {
				BorderLayout borderLayout = new BorderLayout();
				northPanel = new JPanel();
				northPanel.setBorder(BorderFactory.createTitledBorder(
						null, "Posisjon", TitledBorder.DEFAULT_JUSTIFICATION, 
						TitledBorder.DEFAULT_POSITION, 
						new Font("Tahoma", Font.PLAIN, 12), 
						new Color(0, 70, 213)));
				northPanel.setLayout(borderLayout);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		northPanel.removeAll();
		northPanel.add(getCoordPanel(), BorderLayout.CENTER);
		return northPanel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				buttonPanel = new JPanel();
				FlowLayout fl = new FlowLayout();
				fl.setHgap(0);
				fl.setVgap(0);
				fl.setAlignment(FlowLayout.RIGHT);
				buttonPanel.setLayout(fl);
				buttonPanel.add(getCancelButton());
				buttonPanel.add(getApplyButton());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}

	public JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = new JButton();
				String iconName = "cancel.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				cancelButton.setIcon(icon);
				Dimension size = app.getUIFactory().getSmallButtonSize();
				cancelButton.setPreferredSize(size);
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
				applyButton = new JButton();
				String iconName = "finish.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				applyButton.setIcon(icon);
				Dimension size = app.getUIFactory().getSmallButtonSize();
				applyButton.setPreferredSize(size);
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
	
	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}	
	
	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		if (tool.getMap() == null) {
			return;
		}
		IMsoFeatureLayer msoLayer = (IMsoFeatureLayer)e.getSource();
		List selection = msoLayer.getSelected();
		if (selection != null && selection.size() > 0) {
			IMsoFeature msoFeature = (IMsoFeature)selection.get(0);
			IMsoObjectIf msoObject = msoFeature.getMsoObject();
			IUnitIf unit = (IUnitIf)msoObject;
			tool.setCurrentUnit(unit);
			getUnitList().setSelectedValue(unit, true);
			com.esri.arcgis.geometry.Point point = 
				(com.esri.arcgis.geometry.Point)msoFeature.getShape();
			getPOIField().setPoint(point);
		}
		else {
			reset();
		}
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
