package org.redcross.sar.wp.unit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.redcross.sar.Application;
import org.redcross.sar.gui.dialog.AssociationDialog;
import org.redcross.sar.gui.document.AutoCompleteDocument;
import org.redcross.sar.gui.event.IAutoCompleteListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.field.ComboBoxField;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextAreaField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.renderer.BundleListCellRenderer;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.AssocUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.AssocUtils.Association;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

/**
 * JPanel displaying team details.
 * Responsible for storing personnel in MSO.
 *
 * @author thomasl, kenneth
 */
public class PersonnelDetailsLeftPanel extends JPanel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;

	private static final Logger m_logger = Logger.getLogger(PersonnelDetailsLeftPanel.class);
	private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	private static final ImageIcon m_alertedIcon = DiskoIconFactory.getIcon("STATUS.ALERTED","32x32");
	private static final ImageIcon m_arrivedIcon = DiskoIconFactory.getIcon("STATUS.ARRIVED","32x32");
	private static final ImageIcon m_releasedIcon = DiskoIconFactory.getIcon("STATUS.RELEASED","32x32");
	
	private IDiskoWpUnit m_wp;
	private IPersonnelIf m_currentPersonnel;

	private JButton m_changeStatusButton;

	private FieldsPanel m_infoPanel;
	private TextLineField m_nameTextField;
	private TextLineField m_cellTextField;
	private ComboBoxField m_propertyComboBoxField;
    private TextLineField m_associationTextField;
	private TextLineField m_roleTextField;
	private TextLineField m_unitTextField;
	private DTGField m_etaTextField;
	private DTGField m_callOutTextField;
	private DTGField m_arrivedTextField;
	private DTGField m_releasedTextField;
	private TextAreaField m_remarksTextArea;

	public PersonnelDetailsLeftPanel(IDiskoWpUnit wp)
	{
		// prepare
		m_wp = wp;
		// initialize GUI
		initialize();
		// add listeners
		wp.getMsoEventManager().addClientUpdateListener(this);
		wp.getMsoEventManager().addClientUpdateListener(getInfoPanel());
        getInfoPanel().addWorkFlowListener(new IWorkFlowListener() {
			public void onFlowPerformed(WorkFlowEvent e) {
				// only forward MSO changes
				if(e.isMsoData()) m_wp.onFlowPerformed(e);				
			}        	
        });
	}

	private void initialize()
	{
		// prepare
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		// add panels
		add(getInfoPanel());

	}
	
	private FieldsPanel getInfoPanel() {
		if(m_infoPanel==null) {
			m_infoPanel = new FieldsPanel(m_resources.getString("PersonnelInfo.text"),"",false,false);
			m_infoPanel.setColumns(2);
			m_infoPanel.setPreferredExpandedHeight(275);
			m_infoPanel.addButton(getChangeStatusButton(), "status");
			m_infoPanel.suspendLayout();
			m_infoPanel.addField(getFullNameTextField());
			m_infoPanel.addField(getCellPhoneTextField());
			m_infoPanel.addField(getPropertyTextField());
			m_infoPanel.addField(getAssociationTextField());
			m_infoPanel.addField(getRoleTextField());
			m_infoPanel.addField(getUnitTextField());
			m_infoPanel.addField(getCallOutTextField());
			m_infoPanel.addField(getArrivedTextField());
			m_infoPanel.addField(getETATextField());
			m_infoPanel.addField(getReleasedTextField());
			m_infoPanel.addField(getRemarksTextArea());
			m_infoPanel.setFieldSpanX("fullname", 2);
			m_infoPanel.setFieldSpanX("cellphone", 2);
			m_infoPanel.setFieldSpanX("property", 2);
			m_infoPanel.setFieldSpanX("association", 2);
			m_infoPanel.setFieldSpanX("remarks", 2);			
			m_infoPanel.resumeLayout();
			m_infoPanel.setInterests(m_wp.getMsoModel(), EnumSet.of(MsoClassCode.CLASSCODE_PERSONNEL));
		}
		return m_infoPanel;
	}

	private JButton getChangeStatusButton() {
		if(m_changeStatusButton==null) {
			m_changeStatusButton = DiskoButtonFactory.createButton(ButtonSize.SMALL);
			m_changeStatusButton.setIcon(m_alertedIcon);
			m_changeStatusButton.setToolTipText(m_wp.getBundleText("AlertedButton.text"));
			m_changeStatusButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(m_currentPersonnel != null)
					{
						try {
							String command = e.getActionCommand();
							PersonnelStatus newStatus = PersonnelStatus.valueOf(command);

							switch(newStatus)
							{
							case ON_ROUTE:
								m_currentPersonnel = UnitUtils.callOutPersonnel(m_currentPersonnel);
								m_changeStatusButton.setIcon(m_alertedIcon);
								m_changeStatusButton.setToolTipText(m_wp.getBundleText("AlertedButton.text"));
								updateFieldContents();
								break;
							case ARRIVED:
								UnitUtils.arrivedPersonnel(m_currentPersonnel);
								m_changeStatusButton.setIcon(m_arrivedIcon);
								m_changeStatusButton.setToolTipText(m_wp.getBundleText("ArrivedButton.text"));
								break;
							case RELEASED:
								UnitUtils.releasePersonnel(m_currentPersonnel);
								m_changeStatusButton.setIcon(m_releasedIcon);
								m_changeStatusButton.setToolTipText(m_wp.getBundleText("ReleasedButton.text"));
							}
						}
						catch(Exception ex) {
							m_logger.error("Failed to handle status change",ex);
						}
					}
				}
			});
		}
		return m_changeStatusButton;
	}	

	private TextLineField getFullNameTextField() {
		if(m_nameTextField==null) {
			m_nameTextField = new TextLineField("fullname",m_resources.getString("FullName.text"),true);
			m_nameTextField.addWorkFlowListener(new IWorkFlowListener() {

				@Override
				public void onFlowPerformed(WorkFlowEvent e) {
					// is changed?
					if(isSet()&&m_nameTextField.isChangeable() && e.isChange() && e.isWorkDoneByAwtComponent()) {
						String[] names = getNames(false);
						if(names!=null) {
							m_currentPersonnel.suspendClientUpdate();
							m_currentPersonnel.setFirstname(names[0]);
							m_currentPersonnel.setLastname(names[1]);					
							m_currentPersonnel.resumeClientUpdate(true);							
							m_wp.onFlowPerformed(new WorkFlowEvent(this,m_currentPersonnel,WorkFlowEvent.EVENT_CHANGE));
						}
					}
					
				}
				
			});
		}
		return m_nameTextField;
	}

	private TextLineField getCellPhoneTextField() {
		if(m_cellTextField==null) {
			m_cellTextField = new TextLineField("cellphone",m_resources.getString("CellularPhone.text"),true);    		
		}
		return m_cellTextField;
	}

	private ComboBoxField getPropertyTextField() {
		if(m_propertyComboBoxField==null) {
			m_propertyComboBoxField = new ComboBoxField("property",m_resources.getString("Property.text"),false);    		
			m_propertyComboBoxField.fill(PersonnelType.values());
			ResourceBundle personnelResources = Internationalization.getBundle(IPersonnelIf.class);
			m_propertyComboBoxField.getComboBox().setRenderer(new BundleListCellRenderer(personnelResources));
		}
		return m_propertyComboBoxField;
	}

    private TextLineField getAssociationTextField() {
		if(m_associationTextField==null) {
		    m_associationTextField = new TextLineField("association","Tilhørighet",true);
			JTextField inputField = m_associationTextField.getTextField();
			AutoCompleteDocument doc = new AutoCompleteDocument(AssocUtils.getAssociations(-1,"{l:n} {l:s}"),inputField);
			inputField.setDocument(doc);
			doc.addAutoCompleteListener(new IAutoCompleteListener() {

				public void onSuggestionFound(AutoCompleteDocument document, String suggestion) {
					if(!isSet() || !m_associationTextField.isChangeable()) return;
					Association[] items = null;
					if(suggestion!=null) {
						items = AssocUtils.parse(suggestion,false,false);
					}
					if(m_currentPersonnel!=null) {
						m_associationTextField.setChangeable(false);
						m_currentPersonnel.suspendClientUpdate();
						if(items!=null) {
							m_currentPersonnel.setOrganization(items[0].getName());
							m_currentPersonnel.setDivision(items[0].getName());
							m_currentPersonnel.setDepartment(items[0].getName());
						} else {
							m_currentPersonnel.setOrganization(null);
							m_currentPersonnel.setDivision(null);
							m_currentPersonnel.setDepartment(null);
						}
						m_currentPersonnel.resumeClientUpdate(false);
						m_associationTextField.setChangeable(true);
						m_wp.onFlowPerformed(new WorkFlowEvent(this,m_currentPersonnel,WorkFlowEvent.EVENT_CHANGE));
					}
				}
				
			});
			m_associationTextField.setButtonVisible(true);
			m_associationTextField.addButtonActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if(m_currentPersonnel!=null) {
						AssociationDialog dlg = new AssociationDialog(Application.getInstance());
						dlg.setLocationRelativeTo(Application.getInstance());
						if(dlg.associate(getAssociationTextField().getValue(),m_currentPersonnel)) {
							updateFieldContents();
						}
					}
				}
				
			});
		}
		return m_associationTextField;
	}	

	private TextLineField getRoleTextField() {
		if(m_roleTextField==null) {
			m_roleTextField = new TextLineField("role",m_resources.getString("Role.text"),false);    		
		}
		return m_roleTextField;
	}

	private TextLineField getUnitTextField() {
		if(m_unitTextField==null) {
			m_unitTextField = new TextLineField("unit",m_resources.getString("Unit.text"),false);    		
		}
		return m_unitTextField;
	}

	private DTGField getCallOutTextField() {
		if(m_callOutTextField==null) {
			m_callOutTextField = new DTGField("callout",m_resources.getString("CallOut.text"),false);    		
		}
		return m_callOutTextField;
	}

	private DTGField getETATextField() {
		if(m_etaTextField==null) {
			m_etaTextField = new DTGField("expectedarrival",m_resources.getString("ExpectedArrival.text"),true);    		
		}
		return m_etaTextField;
	}

	private DTGField getArrivedTextField() {
		if(m_arrivedTextField==null) {
			m_arrivedTextField = new DTGField("arrived",m_resources.getString("Arrived.text"),false);    		
		}
		return m_arrivedTextField;
	}

	private DTGField getReleasedTextField() {
		if(m_releasedTextField==null) {
			m_releasedTextField = new DTGField("released",m_resources.getString("Released.text"),false);    		
		}
		return m_releasedTextField;
	}

	private TextAreaField getRemarksTextArea() {
		if(m_remarksTextArea==null) {
			m_remarksTextArea = new TextAreaField("remarks",m_resources.getString("Notes.text"),true);
			m_remarksTextArea.setFixedHeight(100);
		}
		return m_remarksTextArea;
	}

    public boolean isChanged() {
    	return m_currentPersonnel!=null?m_currentPersonnel.isChanged():false;
    }
    
    public boolean isNew() {
    	return m_currentPersonnel!=null?!m_currentPersonnel.isCreated():false;
    }
    
    public boolean isSet() {
    	return m_currentPersonnel!=null;
    }
    
	/*
	 * Setters and getters
	 */
	public IPersonnelIf getPersonnel()
	{
		return m_currentPersonnel;
	}

	public void setPersonnel(IPersonnelIf personnel)
	{
		m_currentPersonnel = personnel;
        if(personnel!=null) {
        	getCellPhoneTextField().setMsoAttribute(personnel.getTelephone1Attribute());
        	getPropertyTextField().setMsoAttribute(personnel.getTypeAttribute());
        	getCallOutTextField().setMsoAttribute(personnel.getCallOutAttribute());
        	getETATextField().setMsoAttribute(personnel.getEstimatedArrivalAttribute());
        	getArrivedTextField().setMsoAttribute(personnel.getArrivedAttribute());
        	getReleasedTextField().setMsoAttribute(personnel.getReleasedAttribute());
        	getRemarksTextArea().setMsoAttribute(personnel.getRemarksAttribute());
        	getCellPhoneTextField().reset();
        	getPropertyTextField().reset();
        	getCallOutTextField().reset();
        	getETATextField().reset();
        	getArrivedTextField().reset();
        	getReleasedTextField().reset();
        }  else {
        	getCellPhoneTextField().clearMsoAttribute("");
        	getPropertyTextField().clearMsoAttribute(PersonnelType.VOLUNTEER);
        	getCallOutTextField().clearMsoAttribute("");
        	getETATextField().clearMsoAttribute("");
        	getArrivedTextField().clearMsoAttribute("");
        	getReleasedTextField().clearMsoAttribute("");
        	getRemarksTextArea().clearMsoAttribute("");
        }
		m_nameTextField.requestFocus();
	}	
	
	/**
	 * Updates field contents with current personnel attribute values
	 */
	public void updateFieldContents()
	{
		// prevent reenty
		getInfoPanel().setChangeable(false);

		if (m_currentPersonnel != null) {

			// update caption
			getInfoPanel().setCaptionText(MsoUtils.getPersonnelName(m_currentPersonnel, true));
			m_nameTextField.setValue(MsoUtils.getPersonnelName(m_currentPersonnel, false));

			IUnitIf unit = m_currentPersonnel.getOwningUnit();
			m_unitTextField.setValue(MsoUtils.getUnitName(unit));
			
			if (unit != null)
			{
				if (unit.getUnitLeader() == m_currentPersonnel)
				{
					m_roleTextField.setValue(m_resources.getString("Leader.text"));
				} else {
					m_roleTextField.setValue(m_resources.getString("Personnel.text"));
				}
			} else {
				m_roleTextField.setValue("");
			}
			
			// Get next status for
			PersonnelStatus status = m_currentPersonnel.getStatus();
			PersonnelStatus[] values = PersonnelStatus.values();
			status = values[(status.ordinal() + 1) % values.length];
			if (status == PersonnelStatus.IDLE)
			{
				// not possible to send personnel status back to idle, set to next
				status = values[(status.ordinal() + 1) % values.length];
			}
			m_changeStatusButton.setActionCommand(status.name());
			
            if(getAssociationTextField().isChangeable()) {
	            if(m_currentPersonnel.getOrganization()!=null) {
		            Association assoc = AssocUtils.getOrganization(m_currentPersonnel.getOrganization());
		            assoc.setPattern("{1:n}");
		            if(m_currentPersonnel.getDivision()!=null) {
		            	assoc = AssocUtils.getDivision(assoc,m_currentPersonnel.getDivision());
			            assoc.setPattern("{2:n} {2:s}");
		                if(m_currentPersonnel.getDepartment()!=null) { 
		                	assoc = AssocUtils.getDepartment(assoc,m_currentPersonnel.getDepartment());
				            assoc.setPattern("{3:n} {3:s}");
		                }
		            }
		            getAssociationTextField().setChangeable(false);
		            getAssociationTextField().setValue(assoc.getText());
		            getAssociationTextField().setChangeable(true);
	        	} else {
		            getAssociationTextField().setChangeable(false);
		            getAssociationTextField().setValue("");
		            getAssociationTextField().setChangeable(true);        		
	        	}
            }
            			
		} else {

			// prevent reenty
			getInfoPanel().setChangeable(false);
			
			// update caption
			getInfoPanel().setCaptionText(MsoUtils.getPersonnelName(m_currentPersonnel,true));
			m_nameTextField.setValue("");
			m_roleTextField.setValue("");
            m_associationTextField.setValue("");
			m_unitTextField.setValue("");
			
		}

		// resume reenty
		getInfoPanel().setChangeable(true);

	}
	
    /**
     * validate input data
     */
	public boolean isInputValid()
	{
		if (m_currentPersonnel != null)
		{
			// validate
			if (getNames(true)!=null)
			{
				return true;
			}
			Utils.showWarning("Begrensning","Fullt navn må oppgis for personell");			
		}
		// failed!
		return false;
	}	
	
	private String[] getNames(boolean validate) {
		
		String[] fields = m_nameTextField.getValue().split(" ");

		// validate
		if (fields.length > 0)
		{
			StringBuilder text = new StringBuilder();
			for (int i = 0; i < fields.length - 1; i++)
			{
				text.append(fields[i] + " ");
			}

			String firstName = text.toString().trim();
			String lastName = fields[fields.length - 1].trim();

			// get name array?
			if(!validate || (firstName.length()!=0 || lastName.length()!=0)) {
				
				// finished!
				return new String[]{firstName,lastName};

			}
		}
		return validate?null:new String[2];
	}

	public void setCaptionText(String text)
	{
		getInfoPanel().setCaptionText(text);
	}

	public EnumSet<MsoClassCode> getInterests() {
		return EnumSet.of(MsoClassCode.CLASSCODE_PERSONNEL);
	}

	/**
	 * Update fields if any changes occur in the personnel object
	 */
	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		if(events.isClearAllEvent()) {
			setPersonnel(null);
			updateFieldContents();
		}
		else {
			// loop over all events
			for(MsoEvent.Update e : events.getEvents(MsoClassCode.CLASSCODE_PERSONNEL)) {

				// consume loopback updates
				if(!e.isLoopback())
				{
					// get personnel reference
					IPersonnelIf personnel = 
							(e.getSource() instanceof IPersonnelIf) ?
							(IPersonnelIf) e.getSource() : null;
							
					// is object modified?
					if (e.isChangeReferenceEvent()) {
						updateFieldContents();
					}
					else if (e.isModifyObjectEvent()) {
						updateFieldContents();
					}

					// delete object?
					if (e.isDeleteObjectEvent() && personnel == m_currentPersonnel) {
			    		setPersonnel(null);
			    		updateFieldContents();
					}

				}
			}
		}
	}
}
