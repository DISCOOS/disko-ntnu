package org.redcross.sar.gui.mso.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.EnumSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.DefaultToolPanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.IToolListener;
import org.redcross.sar.map.event.ToolEvent;
import org.redcross.sar.map.event.ToolEvent.ToolEventType;
import org.redcross.sar.map.tool.POITool;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class POIPanel extends DefaultToolPanel {

	private static final long serialVersionUID = 1L;

	private GotoPanel gotoPanel;
	private JButton centerAtButton;
	private POITypesPanel poiTypesPanel;
	private FieldsPanel optionsPanel;
	private TextLineField nameAttr;
	private DefaultPanel remarksPanel;
	private JTextArea remarksArea;

	private IPOIIf msoPOI;

	public POIPanel(POITool tool) {
		// forward
		this("Opprette punkt",tool);
	}

	public POIPanel(String caption, POITool tool) {

		// forward
		super(caption,tool);

		// listen for IPOIIf changes
		setInterests(Utils.getApp().getMsoModel(),
				EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_POI));

		// initialize gui
		initialize();

		// listen for changes in position
		tool.addToolListener(new IToolListener() {

			@Override
			public void onAction(ToolEvent e) {
				if(e.isType(ToolEventType.FINISH_EVENT) && e.getFlags()==1) {
					// suspend events
					setChangeable(false);
					// update position
					getGotoPanel().getCoordinatePanel().setPoint(getTool().getPoint());
					// resume events
					setChangeable(false);
				}

			}

		});

	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {

		// set preferred size
		setPreferredSize(new Dimension(200,550));

		// set preferred body size
		setPreferredBodySize(new Dimension(200,400));

		// get body panel
		JPanel panel = (JPanel)getBodyComponent();

		// set layout
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

		// build container
		addBodyChild(getGotoPanel());
		addBodyChild(Box.createVerticalStrut(5));
		addBodyChild(getOptionsPanel());
		addBodyChild(Box.createVerticalStrut(5));
		addBodyChild(getPOITypesPanel());
		addBodyChild(Box.createVerticalStrut(5));
		addBodyChild(getRemarksPanel());

		// add buttons
		insertButton("finish",getCenterAtButton(),"centerat");

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
				optionsPanel.setPreferredSize(new Dimension(200,80));
				optionsPanel.addField(getNameAttr());
				optionsPanel.addWorkFlowListener(new IWorkFlowListener() {

					@Override
					public void onFlowPerformed(WorkFlowEvent e) {

						// consume?
						if(!isChangeable()) return;

						// is not dirty?
						if(!isDirty()) {

							// update
							setDirty(msoPOI!=null ?
									msoPOI.getName()!=getNameAttr().getValue() : true);

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
	public TextLineField getNameAttr() {
		if (nameAttr == null) {
			nameAttr = new TextLineField("Name","Navn",true,50,25,"");
		}
		return nameAttr;
	}

	/**
	 * This method initializes GotoPanel
	 *
	 * @return javax.swing.JPanel
	 */
	public GotoPanel getGotoPanel() {
		if (gotoPanel == null) {
			gotoPanel = new GotoPanel("Skriv inn posisjon",false);
			gotoPanel.setPreferredSize(new Dimension(200, 140));
			gotoPanel.addWorkFlowListener(new IWorkFlowListener() {

				public void onFlowPerformed(WorkFlowEvent e) {

					// consume?
					if(!isChangeable()) return;

					// consume changes
					setChangeable(false);

					try {

						// get position
						Position p = getGotoPanel().getPosition();

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
	 * This method initializes poiTypesPanel
	 *
	 * @return {@link POITypesPanel}
	 */
	public POITypesPanel getPOITypesPanel() {
		if (poiTypesPanel == null) {
			poiTypesPanel = new POITypesPanel();
			poiTypesPanel.setPreferredSize(new Dimension(200,100));
			poiTypesPanel.addWorkFlowListener(new IWorkFlowListener() {

				public void onFlowPerformed(WorkFlowEvent e) {

					// consume?
					if(!isChangeable()) return;

					// is not dirty?
					if(!isDirty()) {

						// update
						setDirty(msoPOI!=null ?
								msoPOI.getType()!=poiTypesPanel.getPOIType() : true);

					}

				}

			});

		}
		return poiTypesPanel;
	}

	/**
	 * This method initializes remarksPanel
	 *
	 * @return {@link DefaultPanel}
	 */
	public DefaultPanel getRemarksPanel() {
		if (remarksPanel == null) {
			try {
				remarksPanel = new DefaultPanel("Merknader",false,false);
				remarksPanel.setPreferredSize(new Dimension(200, 150));
				remarksPanel.setBodyComponent(getRemarksArea());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return remarksPanel;
	}

	/**
	 * This method initializes txtArea
	 *
	 * @return javax.swing.JTextArea
	 */
	public JTextArea getRemarksArea() {
		if (remarksArea == null) {
			remarksArea = new JTextArea();
			remarksArea.setLineWrap(true);
			remarksArea.setRows(3);
			remarksArea.setPreferredSize(new Dimension(200, 100));
			remarksArea.getDocument().addDocumentListener(new DocumentListener() {

				public void changedUpdate(DocumentEvent e) { change(); }
				public void insertUpdate(DocumentEvent e) { change(); }
				public void removeUpdate(DocumentEvent e) { change(); }

				private void change() {
					// consume?
					if(!isChangeable()) return;
					// notify
					setDirty(true);
				}

			});
		}
		return remarksArea;
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
			// disable update
			setChangeable(false);

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

			// enable update
			setChangeable(true);

		}
	}

	/* ===========================================
	 * Public methods
	 * ===========================================
	 */

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

	public IPOIIf  getPOI() {
		return msoPOI;
	}

	public void setPOI(IPOIIf poi) {
		// replace
		msoPOI = poi;
		// update comments and type
		if(msoPOI!=null) {
			// update panel
			setPOIName(msoPOI.getName());
			setPosition(msoPOI.getPosition());
			setPOIType(msoPOI.getType());
			setRemarks(msoPOI.getRemarks());
		}
		else {
			setPOIName("");
			setPosition(null);
			setPOIType(null);
			setRemarks("");
		}
		// forward
		setDirty(false);
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

	public POIType[] getPOITypes() {
		return getPOITypesPanel().getPOITypes();
	}

	public void setPOITypes(POIType[] types) {
		getPOITypesPanel().setPOITypes(types);
		getPOITypesPanel().setSelectionAllowed(!(types==null || types.length<=1));
	}

	public POIType getPOIType() {
		return getPOITypesPanel().getPOIType();
	}

	public void setPOIType(POIType type) {
		getPOITypesPanel().setPOIType(type);
	}

	public String getRemarks() {
		return getRemarksArea().getText();
	}

	public void setRemarks(String remarks) {
		if (!getRemarksArea().getText().equalsIgnoreCase(remarks)){
			getRemarksArea().setText(remarks);
		}
	}

	public String getPOIName() {
		return getNameAttr().getValue();
	}

	public void setPOIName(String name) {
		getNameAttr().setValue(name);
	}

	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */

	@Override
	public POITool getTool() {
		return (POITool)super.getTool();
	}

	@Override
	public boolean finish() {

		// initialize
		boolean bFlag = false;

		// is possible?
		if(getTool()!=null) {

			// consume change events
			setChangeable(false);

			// is position valid?
			if(getGotoPanel().getCoordinatePanel().isPositionValid()) {

				// get position
				Position p = getGotoPanel().getCoordinatePanel().getPosition();

				// update point?
				if(p!=null) {
					try {
						// convert and update tool
						Point point = MapUtil.getEsriPoint(p.getGeoPos(), getTool().getMap().getSpatialReference());
						getTool().setPoint(point,true);

						// finish working
						bFlag = getTool().finish();

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
		return bFlag;
	}

	public void reset() {
		getTool().reset();
		getRemarksArea().setText(null);
		getGotoPanel().reset();
		getPOITypesPanel().reset();
	}

	public void update() {

		// forward
		super.update();

		// consume?
		if(!isChangeable()) return;

		// consume events
		setChangeable(false);

		try {

			// update caption
			if(getTool().getMap().isEditSupportInstalled())
				setCaptionText(getTool().getMap().getDrawAdapter().getDescription());
			else
				setCaptionText(MapUtil.getDrawText(getTool().getMsoObject(),
						getTool().getMsoCode(), getTool().getDrawMode()));

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
		// consume changes
		setChangeable(false);
		// is valid?
		if(msoObj instanceof IPOIIf)
			setPOI((IPOIIf)msoObj);
		else
			setPOI(null);
		// resume changes
		setChangeable(true);
		// forward
		update();
	}

	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	@Override
	protected void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		// forward?
		if(msoPOI!=null && msoPOI.equals(msoObject))
			setPOI(msoPOI);
	}

	@Override
	protected void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		// forward?
		if(msoPOI!=null && msoPOI.equals(msoObject))
			setPOI(null);
	}


}//  @jve:decl-index=0:visual-constraint="10,10"
