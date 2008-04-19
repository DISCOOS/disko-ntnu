package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
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
import org.redcross.sar.util.mso.Position;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class POIPanel extends JPanel implements IMsoUpdateListenerIf, 
										IMsoLayerEventListener {

	private static final long serialVersionUID = 1L;
	
	private IDiskoApplication app = null;
	private POITool tool = null;
	private JTextArea txtArea = null;
	private JPanel coordPanel = null;
	private JPanel northPanel = null;
	private GotoPanel gotoPanel = null;
	private DiskoPanel buttonsPanel = null;
	private JButton cancelButton = null;
	private JButton applyButton = null;
	private POITypePanel poiTypePanel = null;
	private DiskoPanel textAreaPanel = null;
	
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
				vfl.setHgap(5);
				vfl.setVgap(5);
				this.setPreferredSize(new Dimension(200,450));
				this.setLayout(vfl);
				this.add(getButtonsPanel());
				this.add(getNorthPanel());
			}
			else {
				BorderLayout bl = new BorderLayout();
				bl.setHgap(5);
				bl.setVgap(5);
				this.setLayout(bl);
				this.add(getButtonsPanel(),BorderLayout.NORTH);				
				this.add(getNorthPanel(),BorderLayout.CENTER);
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
		getGotoPanel().reset();
		getTypePanel().reset();
		tool.setCurrentPOI(null);
	}

	/**
	 * This method initializes GotoPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	public GotoPanel getGotoPanel() {
		if (gotoPanel == null) {
			gotoPanel = new GotoPanel("Skriv inn posisjon");
			gotoPanel.setGotoButtonVisible(false);
		}
		return gotoPanel;
	}
	
	public POIType[] getTypes() {
		return getTypePanel().getTypes();
	}
	
	public void setTypes(POIType[] types) {
		getTypePanel().setTypes(types);
	}

	public POIType getPOIType() {
		return getTypePanel().getPOIType();
	}
	
	public void setPOIType(POIType type) {
		getTypePanel().setPOIType(type);		
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
		getGotoPanel().getPositionField().setPoint(p);
	}
	
	public boolean isRemarksVisible() {
		return getTextAreaPanel().isVisible();
	}

	public boolean isTypesVisible() {
		return getTypePanel().isVisible();
	}
	
	public boolean isButtonsVisible() {
		return getButtonsPanel().isVisible();
	}
	
	public void setRemarksVisible(boolean isVisible) {
		getTextAreaPanel().setVisible(isVisible);
	}

	public void setTypesVisible(boolean isVisible) {
		getTypePanel().setVisible(isVisible);
	}
	
	public void setButtonsVisible(boolean isVisible) {
		getButtonsPanel().setVisible(isVisible);
	}
	
	public IPOIIf getCurrentPOI() {
		return tool.getCurrentPOI();
	}
	
	public void setCurrentPOI(IPOIIf poi) {
		tool.setCurrentPOI(poi);
	}
	
	public void setCurrentPosition(Position p) {
		getGotoPanel().getPositionField().setPosition(p);
	}
	
	public void setCurrentPoint(Point p) {
		getGotoPanel().getPositionField().setPoint(p);
	}
	
	private void apply() {
		// get point from coordinates
		Point point = null;
		try {
			point = getGotoPanel().getPositionField().getPoint();
		} catch (Exception ex) {
			Utils.showWarning("Ugyldig format. Sjekk koordinater og prøv igjen");
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
			Utils.showWarning("Ingen posisjon er oppgitt");
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
	 * This method initializes POITypePanel	
	 * 	
	 * @return {@link POITypePanel}	
	 */
	public POITypePanel getTypePanel() {
		if (poiTypePanel == null) {
			poiTypePanel = new POITypePanel();
		}
		return poiTypePanel;
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
			txtArea.setRows(5);
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
			coordPanel.add(getGotoPanel());
			coordPanel.add(getTypePanel());
			coordPanel.add(getTextAreaPanel());			
		}
		else {
			// create flow layout
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(0);
			// set flow layout
			coordPanel.setLayout(bl); 
			// add components
			coordPanel.add(getGotoPanel(),BorderLayout.WEST);
			coordPanel.add(getTypePanel(),BorderLayout.CENTER);
			coordPanel.add(getTextAreaPanel(),BorderLayout.EAST);						
		}
		return coordPanel;
	}

	/**
	 * This method initializes textAreaScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private DiskoPanel getTextAreaPanel() {
		if (textAreaPanel == null) {
			try {
				textAreaPanel = new DiskoPanel("Merknader");
				textAreaPanel.setBodyComponent(getTextArea());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return textAreaPanel;
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
				northPanel.setLayout(borderLayout);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		northPanel.removeAll();
		northPanel.add(getCoordPanel(), BorderLayout.CENTER);
		return northPanel;
	}

	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			try {
				buttonsPanel = new DiskoPanel("Utfør");
				buttonsPanel.addButton(getApplyButton(),"apply");
				buttonsPanel.addButton(getCancelButton(),"cancel");
				buttonsPanel.setBodyComponent(null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonsPanel;
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
			getGotoPanel().getPositionField().setPosition(poi.getPosition());
		}
	}

	private void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		IPOIIf poi = tool.getCurrentPOI();
		// same as selected?
		if(poi==(IPOIIf)msoObject) {
			// reset position
			getGotoPanel().getPositionField().setText(null);
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
