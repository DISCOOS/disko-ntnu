package org.redcross.sar.wp.tactics;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.EnumSet;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.attribute.ComboAttribute;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.panel.AttributesPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.renderer.BundleListCellRenderer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentPriority;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class RequirementDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;
	
	private DefaultPanel contentPanel = null;
	private JPanel requirementPanel = null;
	private BasePanel remarksPanel = null;
	private JTextArea remarksTextArea = null;
	private AttributesPanel attribsPanel = null; 
	private ComboAttribute accuracyCombo;
	private ComboAttribute priorityCombo;
	private ComboAttribute personnelCombo;
	
	private IDiskoWpModule wp = null;
	
	public RequirementDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare objeckts
		this.wp = wp;
		// initialize gui
		initialize();
		// initialise
		setup();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		try {
            this.setPreferredSize(new Dimension(800, 200));
            this.setContentPane(getContentPanel());
            this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	private static EnumSet<IMsoManagerIf.MsoClassCode> getMyInterest() {
		EnumSet<IMsoManagerIf.MsoClassCode> myInterests 
			= EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMyLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers 
			= EnumSet.of(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
	    return myLayers;
	}
	
	/**
	 * This method initializes contentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultPanel() {

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean beforeFinish() {
						// update mso model
						setPersonnel(getPersonnel(),false,true);
						setRemarks(getRemarks(),false,true);
						setPriority(getPriority(), false, true);
						setAccuracy(getAccuracy(),false,true);		
						// finished
						return true;
					}
					
					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {
						// consume changes
						setChangeable(false);
						// initialize
						ISearchIf search = null;
						String remarks = null;
						AssignmentPriority pri = AssignmentPriority.NORMAL;
						int accuracy = 75;
						int personnel = 3;
						// try to get search assignment
						if(msoObj instanceof ISearchIf) {
							search = (ISearchIf)msoObj;
						}
						else {
							// get owning area
							IAreaIf area = MsoUtils.getOwningArea(msoObj);
							// has area?
							if(area!=null) {
								IAssignmentIf assignment = area.getOwningAssignment();
								if (assignment instanceof ISearchIf) {		
									search = (ISearchIf)assignment;	
								}
							}
						}
						
						// has search assignment?
						if(search!=null) {
							pri = search.getPriority();
							accuracy = search.getPlannedAccuracy();
							personnel = search.getPlannedPersonnel();
							remarks = search.getRemarks();
						}

						// update
						super.setMsoObject(search);
						setPriority((pri==null) ? AssignmentPriority.NORMAL : pri ,true,false);
						setAccuracy(accuracy!=0 ? accuracy : 75,true,false);
						setPersonnel(personnel!=0 ? personnel : 3,true,false);
						setRemarks(remarks,true,false);

						// resume changes
						setChangeable(true);
						
						// update
						setDirty(false,false);
						update();
												
					}	
					
					@Override
					public void update() {
						super.update();
						setup();
					}
					
					@Override
					public void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
						// is same as selected?
						if(msoObj == msoObject) {
							setMsoObject(msoObject);
						}
					}

					@Override
					public void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
						// is same as selected?
						if(msoObj == msoObject) {
							// reset selection
							setMsoObject(null);
						}
					}					
					
				};
				contentPanel.setInterests(wp.getMsoModel(),getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());				
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				contentPanel.setScrollBarPolicies(BasePanel.VERTICAL_SCROLLBAR_NEVER, 
								BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
				contentPanel.setBodyComponent(getRequirementPanel());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}
	
	/**
	 * This method initializes requirementPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRequirementPanel() {
		if (requirementPanel == null) {
			try {
				requirementPanel = new JPanel();
				requirementPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				BorderLayout bl = new BorderLayout();
				bl.setHgap(5);
				bl.setVgap(5);				
				requirementPanel.setLayout(bl);
				requirementPanel.add(getRemarksPanel(),BorderLayout.CENTER);
				requirementPanel.add(getAttribsPanel(),BorderLayout.EAST);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return requirementPanel;
	}

	/**
	 * This method initializes AttribsPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private AttributesPanel getAttribsPanel() {
		if (attribsPanel == null) {
			try {
				attribsPanel = new AttributesPanel("Egenskaper","",false,false);
				attribsPanel.setScrollBarPolicies(BasePanel.VERTICAL_SCROLLBAR_NEVER,
						BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
				attribsPanel.setPreferredSize(new Dimension(200,100));
				attribsPanel.setPreferredBodySize(new Dimension(200,100));
				attribsPanel.addAttribute(getPriorityCombo());
				attribsPanel.addAttribute(getAccuracyCombo());
				attribsPanel.addAttribute(getPersonnelCombo());
				Utils.setFixedSize(getPriorityCombo(), 200,25);
				Utils.setFixedSize(getAccuracyCombo(), 200,25);
				Utils.setFixedSize(getPersonnelCombo(), 200,25);
				attribsPanel.addDiskoWorkListener(new IDiskoWorkListener() {

					@Override
					public void onWorkPerformed(DiskoWorkEvent e) {
						if(e.isChange() || e.isFinish())
							setDirty(true);						
					}
					
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attribsPanel;
	}
	
	/**
	 * This method initializes priorityCombo
	 *
	 * @return {@link ComboAttribute}
	 */
	private ComboAttribute getPriorityCombo() {
		if (priorityCombo == null) {
			try {
				priorityCombo = new ComboAttribute("priority", "Prioritet", 90, null, false);
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				AssignmentPriority[] values = AssignmentPriority.values();
				for (int i = 0; i < values.length; i++) {
					model.addElement(values[i]);
				}
				priorityCombo.fill(model);
				JComboBox cb = (JComboBox)priorityCombo.getComponent();
				cb.setRenderer(new BundleListCellRenderer());
				cb.setSelectedIndex(0);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return priorityCombo;
	}

	/**
	 * This method initializes accuracyCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	private ComboAttribute getAccuracyCombo() {
		if (accuracyCombo == null) {
			try {
				accuracyCombo = new ComboAttribute("accuracy", "Nøyaktighet", 90, null, true);
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				for (int i = 1; i < 4; i++) {
					model.addElement(new Integer(i*25));
				}
				accuracyCombo.fill(model);
				JComboBox cb = (JComboBox)accuracyCombo.getComponent();
				cb.setSelectedIndex(0);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return accuracyCombo;
	}	

	/**
	 * This method initializes personnelCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	private ComboAttribute getPersonnelCombo() {
		if (personnelCombo == null) {
			try {
				personnelCombo = new ComboAttribute("personnel", "Antall mnsk", 90, null, true);
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				for (int i = 1; i < 10; i++) {
					model.addElement(new Integer(i));
				}
				personnelCombo.fill(model);
				JComboBox cb = (JComboBox)accuracyCombo.getComponent();
				cb.setSelectedIndex(0);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return personnelCombo;
	}	
	
	/**
	 * This method initializes requirementPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private BasePanel getRemarksPanel() {
		if (remarksPanel == null) {
			try {
				remarksPanel = new BasePanel("Beskrivelse");
				remarksPanel.setScrollBarPolicies(BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED, 
						BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
				remarksPanel.setBodyComponent(getRemarksTextArea());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return remarksPanel;
	}
	
	/**
	 * This method initializes criticalQuestionsTextArea
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getRemarksTextArea() {
		if (remarksTextArea == null) {
			try {
				remarksTextArea = new JTextArea();
				remarksTextArea.setLineWrap(true);
				// add update manager
				remarksTextArea.getDocument().addDocumentListener(new DocumentListener() {

					public void changedUpdate(DocumentEvent e) { change(); }
					public void insertUpdate(DocumentEvent e) { change(); }
					public void removeUpdate(DocumentEvent e) { change(); }
					
					private void change() {
						// consume?
						if(!isChangeable()) return;
						setDirty(true);
					}
					
				});
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return remarksTextArea;		
	}

	public int getAccuracy() {
		Object value = getAccuracyCombo().getValue();
		return value!=null ? Integer.valueOf(value.toString()) : 75;
	}

	public AssignmentPriority getPriority() {
		return (AssignmentPriority)getPriorityCombo().getValue();
	}

	public int getPersonnel() {
		Object value = getPersonnelCombo().getValue();
		return value!=null ? Integer.valueOf(value.toString()) : 3;
	}

	public String getRemarks() {
		return getRemarksTextArea().getText();
	}

	public void setAccuracy(int accuracy) {
		setAccuracy(accuracy,true,true);
	}

	private void setAccuracy(int accuracy, boolean gui, boolean mso) {
		if(gui) {
			getAccuracyCombo().setValue(accuracy);
		}
		if(mso) {
			ISearchIf search = (ISearchIf)getMsoObject();
			if(search!=null) {
				if(search.getPlannedAccuracy()!=accuracy) {
					search.setPlannedAccuracy(accuracy);
				}
			}
		}
	}
	
	public void setPriority(AssignmentPriority priority) {
		setPriority(priority,true,true);
	}

	private void setPriority(AssignmentPriority priority, boolean gui, boolean mso) {		
		if(gui) {
			getPriorityCombo().setValue(priority);
		}
		if(mso) {
			ISearchIf search = (ISearchIf)getMsoObject();
			if(search!=null) {
				if(search.getPriority()!=priority) {
					search.setPriority(priority);

				}
			}
		}
	}
	
	public void setPersonnel(int number) {
		setPersonnel(number,true,true);
	}

	private void setPersonnel(int number, boolean gui, boolean mso) {
		if(gui) {
			getPersonnelCombo().setValue(String.valueOf(number));
		}
		if(mso) {
			ISearchIf search = (ISearchIf)getMsoObject();
			if(search!=null) {
				if(search.getPlannedPersonnel()!=number) {
					search.setPlannedPersonnel(number);
				}
			}
		}
	}
	
	public void setRemarks(String remarks) {
		setRemarks(remarks,true,true);
	}

	private void setRemarks(String remarks, boolean gui, boolean mso) {
		if(gui) {
			getRemarksTextArea().setText(remarks);
			// request focus
			getRemarksTextArea().requestFocus();
		}
		if(mso) {
			ISearchIf search = (ISearchIf)getMsoObject();
			if(search!=null) {
				if(!search.getRemarks().equals(remarks)) {
					search.setRemarks(remarks);
				}
			}
		}
	}	

	private void setup() {

		// consume?
		if(!isChangeable()) return;
		
		// consume changes
		setChangeable(false);
		
		// try to get mso object?
		if(getMsoObject()==null) 
			getContentPanel().setSelectedMsoFeature(wp.getMap());
		
		// get current assignment
		ISearchIf search = (ISearchIf)getMsoObject();
		
		// update icon
		if(search!=null) {
			Enum e = MsoUtils.getType(search,true);
			getContentPanel().setCaptionIcon(
					DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"48x48"));
			getContentPanel().setCaptionText("<html>Krav til <b>" + 
					MsoUtils.getAssignmentName(search, 1).toLowerCase() + "</b></html>");
			getRemarksPanel().setEnabled(true);
			getAttribsPanel().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et oppdrag");			
			getRemarksPanel().setEnabled(false);
			getAttribsPanel().setEnabled(false);
		}		
		getAttribsPanel().update();
		
		// resume changes
		setChangeable(true);
		
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"

