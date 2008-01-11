package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.NumPadDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.PositionField;
import org.redcross.sar.gui.renderers.SimpleListCellRenderer;
import org.redcross.sar.map.command.POITool;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.mso.Position;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class POIPanel extends JPanel implements IMsoUpdateListenerIf, 
										IMsoLayerEventListener {

	private static final long serialVersionUID = 1L;
	private IDiskoApplication app = null;
	private POITool tool = null;
	private NumPadDialog numPadDialog = null;
	private PositionField poiField = null;
	private PositionFormatPanel formatPanel = null;
	private JList typeList = null;
	private JTextArea txtArea = null;
	private JPanel coordPanel = null;
	private JSeparator listSeparator = null;
	private JSeparator textSeparator = null;
	private JScrollPane textAreaScrollPane = null;
	private JPanel northPanel = null;
	private JPanel fieldPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton applyButton = null;
	private JScrollPane listScrollPane = null;
	private boolean isVertical = true;
	
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;

	public POIPanel(POITool tool, boolean isVertical) {
		
		// prepare
		this.app = Utils.getApp();
		this.tool = tool;
		this.myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_POI);
		
		// add listeners
		app.getMsoModel().getEventManager().addClientUpdateListener(this);
		
		// set layout information
		this.isVertical = isVertical;
		
		// initialize gui
		initialize();
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
		getTextArea().setText(null);
		getPOIField().setText(null);
		if(getTypeList().getModel().getSize()>0)
			getTypeList().setSelectedIndex(0);
		tool.setCurrentPOI(null);
	}

	/**
	 * This method initializes fieldPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	public JPanel getFieldPanel() {
		if (fieldPanel == null) {
			BorderLayout bl = new BorderLayout();
			bl.setVgap(5);
			bl.setHgap(0);
			fieldPanel = new JPanel();
			fieldPanel.setLayout(bl);
			fieldPanel.add(getPOIField(),BorderLayout.NORTH);
			fieldPanel.add(new JSeparator(JSeparator.HORIZONTAL),BorderLayout.CENTER);
			fieldPanel.add(getFormatPanel(),BorderLayout.SOUTH);
		}
		return fieldPanel;
	}
	
	/**
	 * This method initializes poiField	
	 * 	
	 * @return POIField	
	 */
	public PositionField getPOIField() {
		if (poiField == null) {
			poiField = new PositionField();
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
	public PositionFormatPanel getFormatPanel() {
		if (formatPanel == null) {
			formatPanel = new PositionFormatPanel();
		}
		return formatPanel;
	}
	
	public POIType[] getTypes() {
		ListModel model = getTypeList().getModel();
		POIType[] types = new POIType[model.getSize()];
		for (int i = 0; i < model.getSize(); i++) {
			types[i]=(POIType)model.getElementAt(i);
		}
		return types;
	}
	
	public void setTypes(POIType[] poiTypes) {
		DefaultListModel model = new DefaultListModel();
		POIType current = (POIType)getTypeList().getSelectedValue();
		if(poiTypes!=null) {
			for (int i = 0; i < poiTypes.length; i++) {
				model.addElement(poiTypes[i]);
			}
		}
		getTypeList().setModel(model);
		if(current!=null)
			getTypeList().setSelectedValue(current,true);
		else
			if(model.getSize()>0)
				getTypeList().setSelectedIndex(0);
		// set number of visible rows
		//getTypeList().setVisibleRowCount(Math.max(model.size(),6));
	}

	public POIType getPOIType() {
		return (POIType)getTypeList().getSelectedValue();
	}
	
	public void setPOIType(POIType type) {
		if((POIType)getTypeList().getSelectedValue()!=type) {
			getTypeList().setSelectedValue(type,true);
		}
	}

	public String getRemarks() {
		return txtArea.getText();
	}

	public void setRemarks(String remarks) {
		if (!txtArea.getText()
				.equalsIgnoreCase(remarks)){
			txtArea.setText(remarks);
		}
	}
	
	public void updatePOIField(Point p) {
		poiField.setPoint(p);
	}
	
	public boolean isRemarksVisible() {
		return getTextAreaScrollPane().isVisible();
	}

	public boolean isTypesVisible() {
		return getTypeList().isVisible();
	}
	
	public boolean isButtonsVisible() {
		return getButtonPanel().isVisible();
	}
	
	public void setRemarksVisible(boolean isVisible) {
		getTextAreaScrollPane().setVisible(isVisible);
		textSeparator.setVisible(isVisible && isVertical);
	}

	public void setTypesVisible(boolean isVisible) {
		getTypeList().setVisible(isVisible);
		listSeparator.setVisible(isVisible && isVertical);
	}
	
	public void setButtonsVisible(boolean isVisible) {
		getButtonPanel().setVisible(isVisible);
	}
	
	public IPOIIf getCurrentPOI() {
		return tool.getCurrentPOI();
	}
	
	public void setCurrentPOI(IPOIIf poi) {
		tool.setCurrentPOI(poi);
	}
	
	public void setCurrentPosition(Position p) {
		getPOIField().setPosition(p);
	}
	
	public void setCurrentPoint(Point p) {
		getPOIField().setPoint(p);
	}
	
	private void apply() {
		// get point from coordinates
		Point point = null;
		try {
			point = getPOIField().getPoint();
		} catch (Exception ex) {
			Utils.showWarning("Ugyldig koordinat format");
		}
		// add or move poi?
		if(point!=null) {
			// forward
			if(tool.getCurrentPOI()!=null)
				tool.movePOIAt(point, getPOIType(), getRemarks());
			else
				tool.addPOIAt(point, getPOIType(), getRemarks());
		}
		else 
			Utils.showWarning("Ingen posisjon er valgt");
	}
	
	public void cancel() {
		// has revert data?
		if (getCurrentPOI() != null) {
			try {
				// revert
				setCurrentPOI(getCurrentPOI());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(app.getFrame(),
						"Ugyldig koordinat format", null, 
						JOptionPane.WARNING_MESSAGE);
				ex.printStackTrace();
			}

		} else {
			// revert
			setCurrentPOI(null);
		}		
		// forward
		tool.cancel();
	}
	
	/**
	 * This method initializes listScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getListScrollPane() {
		if (listScrollPane == null) {
			try {
				listScrollPane = new JScrollPane(getTypeList());
				listScrollPane.setWheelScrollingEnabled(true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return listScrollPane;
	}
	
	/**
	 * This method initializes typeList	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JList getTypeList() {
		if (typeList == null) {
            typeList = new JList();
            typeList.setVisibleRowCount(4);
            typeList.setCellRenderer(new SimpleListCellRenderer(Internationalization.getBundle(IPOIIf.class)));
		}
		return typeList;
	}


	/**
	 * This method initializes txtArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getTextArea() {
		if (txtArea == null) {
			txtArea = new JTextArea();
			txtArea.setLineWrap(true);
			txtArea.setPreferredSize(new Dimension(150,100));
		}
		return txtArea;
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
				textSeparator = new JSeparator(JSeparator.HORIZONTAL);


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
			vfl.setVgap(10);
			// set flow layout
			coordPanel.setLayout(vfl); 
			// add components
			coordPanel.add(getFieldPanel());
			coordPanel.add(listSeparator);
			coordPanel.add(getListScrollPane());
			coordPanel.add(textSeparator);
			coordPanel.add(getTextAreaScrollPane());			
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
			coordPanel.add(getTextAreaScrollPane(),BorderLayout.EAST);						
		}
		return coordPanel;
	}

	/**
	 * This method initializes textAreaScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getTextAreaScrollPane() {
		if (textAreaScrollPane == null) {
			try {
				textAreaScrollPane = new JScrollPane();
				textAreaScrollPane.setViewportView(getTextArea());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return textAreaScrollPane;
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
						null, "Punkt av interesse", TitledBorder.DEFAULT_JUSTIFICATION, 
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
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
				cancelButton.setActionCommand("cancel");
				cancelButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
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
		if (createdObject) {
			msoObjectCreated(msoObj,mask);
		}
		// is object modified?
		if ( (addedReference || removedReference || modifiedObject)) {
			msoObjectChanged(msoObj,mask);
		}
		// delete object?
		if (deletedObject) {
			msoObjectDeleted(msoObj,mask);		}
	}

	private void msoObjectCreated(IMsoObjectIf msoObject, int mask) {
		return; /* NOP */
	}
	
	private void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		IPOIIf poi = tool.getCurrentPOI();
		// same as selected?
		if(poi==(IPOIIf)msoObject) {
			// update position
			poiField.setPosition(poi.getPosition());
		}
	}

	private void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		IPOIIf poi = tool.getCurrentPOI();
		// same as selected?
		if(poi==(IPOIIf)msoObject) {
			// reset position
			poiField.setText(null);
		}
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
			IPOIIf poi = (IPOIIf)msoObject;
			setCurrentPOI(poi);
		}
		else {
			setCurrentPOI(null);
		}
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
