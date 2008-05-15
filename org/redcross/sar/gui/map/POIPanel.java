package org.redcross.sar.gui.map;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.command.POITool;
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

public class POIPanel extends DiskoPanel implements IPropertyPanel,
				IMsoUpdateListenerIf,  IMsoLayerEventListener{

	private static final long serialVersionUID = 1L;
	
	private POITool tool = null;
	
	private DiskoPanel captionPanel = null;	
	private DiskoPanel actionsPanel = null;
	private JTextArea txtArea = null;
	private JPanel optionsPanel = null;
	private GotoPanel gotoPanel = null;
	private JButton cancelButton = null;
	private JButton applyButton = null;
	private POITypesPanel poiTypesPanel = null;
	private DiskoPanel textAreaPanel = null;
	
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;

	public POIPanel(POITool tool) {
		// forward
		this("Opprette punkt",tool);
	}
	
	public POIPanel(String caption, POITool tool) {
		
		// forward
		super(caption);
		
		// prepare
		this.tool = tool;
		this.myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_POI);
		
		// add listeners
		Utils.getApp().getMsoModel().getEventManager().addClientUpdateListener(this);
		
		// initialize gui
		initialize();
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
	
	public void reset() {
		tool.cancel();
		getTextArea().setText(null);
		getGotoPanel().reset();
		getTypesPanel().reset();
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
			gotoPanel.setPreferredSize(new Dimension(200, 125));			
			
		}
		return gotoPanel;
	}
	
	/**
	 * This method initializes TypesPanel	
	 * 	
	 * @return {@link POITypesPanel}	
	 */
	public POITypesPanel getTypesPanel() {
		if (poiTypesPanel == null) {
			poiTypesPanel = new POITypesPanel();
			poiTypesPanel.setPreferredSize(new Dimension(200, 100));			
		}
		return poiTypesPanel;
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
			txtArea.setPreferredSize(new Dimension(200, 100));			
		}
		return txtArea;
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
			optionsPanel.add(getTypesPanel());
			optionsPanel.add(getTextAreaPanel());
		}
		return optionsPanel;
	}

	/**
	 * This method initializes textAreaScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	public DiskoPanel getTextAreaPanel() {
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
	public POIType[] getTypes() {
		return getTypesPanel().getTypes();
	}
	
	public void setTypes(POIType[] types) {
		getTypesPanel().setTypes(types);
	}

	public POIType getPOIType() {
		return getTypesPanel().getPOIType();
	}
	
	public void setPOIType(POIType type) {
		getTypesPanel().setPOIType(type);		
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
				Utils.showWarning("Ugyldig koordinat format");
			}

		} else {
			// revert
			setCurrentPOI(null);
		}		
		// forward
		tool.cancel();
	}

	public void setMsoObject(IMsoObjectIf msoObject) {		
		// consume?
		if (tool == null || tool.getMap() == null) return;
		try {
			// update caption
			if(tool.getMap().isEditSupportInstalled())
				getCaptionPanel().setCaptionText(tool.getMap().getDrawAdapter().getDescription());
			else 
				getCaptionPanel().setCaptionText(MapUtil.getDrawText(msoObject, 
						tool.getMsoClassCode(), tool.getDrawMode())); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	/* ===========================================
	 * IMsoLayerEventListener implementation
	 * ===========================================
	 */

	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		if (tool.getMap() == null || !e.isFinal()) return; 
		IPOIIf poi = null;
		List<IMsoObjectIf> selection = e.getSelectedMsoObjects();
		if (selection != null && selection.size() > 0) {
			IMsoObjectIf msoObj = selection.get(0);
			if(msoObj instanceof IPOIIf)
				poi =(IPOIIf)msoObj;
		}
		setCurrentPOI(poi);
	}

	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */

	public void update() { /*NOP*/ }
	
	public void addActionListener(ActionListener listener) { /*NOP*/ }
	public void removeActionListener(ActionListener listener) { /*NOP*/ }
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
