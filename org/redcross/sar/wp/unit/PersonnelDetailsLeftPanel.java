package org.redcross.sar.wp.unit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
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
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
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
			m_changeStatusButton.setIcon(DiskoIconFactory.getIcon(m_wp.getBundleText("AlertedButton.icon"),"32x32"));
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
								m_changeStatusButton.setIcon(DiskoIconFactory.getIcon(m_wp.getBundleText("AlertedButton.icon"),"32x32"));
								m_changeStatusButton.setToolTipText(m_wp.getBundleText("AlertedButton.text"));
								updateFieldContents();
								break;
							case ARRIVED:
								UnitUtils.arrivedPersonnel(m_currentPersonnel);
								m_changeStatusButton.setIcon(DiskoIconFactory.getIcon(m_wp.getBundleText("ArrivedButton.icon"),"32x32"));
								m_changeStatusButton.setToolTipText(m_wp.getBundleText("ArrivedButton.text"));
								break;
							case RELEASED:
								UnitUtils.releasePersonnel(m_currentPersonnel);
								m_changeStatusButton.setIcon(DiskoIconFactory.getIcon(m_wp.getBundleText("ReleasedButton.icon"),"32x32"));
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

	private void initialize()
	{
		// prepare
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		// add panels
		add(getInfoPanel());

	}

	private TextLineField getFullNameTextField() {
		if(m_nameTextField==null) {
			m_nameTextField = new TextLineField("fullname",m_resources.getString("FullName.text"),true);    		
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
					if(!m_associationTextField.isChangeable()) return;
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

	/**
	 * Updates field contents with current personnel attribute values
	 */
	public void updateFieldContents()
	{
		if (m_currentPersonnel != null) {

			// update caption
			getInfoPanel().setCaptionText(MsoUtils.getPersonnelName(m_currentPersonnel, true));
			m_nameTextField.setValue(MsoUtils.getPersonnelName(m_currentPersonnel, false));
			m_cellTextField.setValue(m_currentPersonnel.getTelephone1());

			if(m_wp.getMsoManager().operationExists()) {
				
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
				
			}
			
		} else {

			// update caption
			getInfoPanel().setCaptionText(MsoUtils.getPersonnelName(m_currentPersonnel,true));
			m_nameTextField.setValue("");
			m_cellTextField.setValue("");
			m_propertyComboBoxField.setValue(PersonnelType.VOLUNTEER);
            m_associationTextField.setValue("");
			m_roleTextField.setValue("");
			m_unitTextField.setValue("");
			m_callOutTextField.setValue("");
			m_etaTextField.setValue("");
			m_arrivedTextField.setValue("");
			m_releasedTextField.setValue("");
			m_remarksTextArea.setValue("");
		}
		m_nameTextField.requestFocus();
	}
	
	/**
	 * Updates personnel data in MSO
	 */
	public boolean savePersonnel()
	{
		if (m_currentPersonnel != null)
		{
			String[] fields = m_nameTextField.getValue().split(" ");

			// validate
			if (fields.length > 0)
			{
				m_currentPersonnel.suspendClientUpdate();

				StringBuilder text = new StringBuilder();
				for (int i = 0; i < fields.length - 1; i++)
				{
					text.append(fields[i] + " ");
				}

				String firstName = text.toString().trim();
				String lastName = fields[fields.length - 1].trim();

				// valid name?
				if(firstName.length()!=0 || lastName.length()!=0) {

					if (firstName.toString() != null)
					{
						m_currentPersonnel.setFirstname(firstName);
					}
					m_currentPersonnel.setLastname(lastName);
					
					/*
					String phone = m_cellTextField.getValue();
					m_currentPersonnel.setTelephone1(phone);

					PersonnelType type = (PersonnelType) m_propertyComboBoxField.getValue();
					if (type == null)
					{
						type = PersonnelType.OTHER;
					}
					m_currentPersonnel.setType(type);

					String organization = (String)m_organizationComboBoxField.getValue();
					m_currentPersonnel.setOrganization(organization);

					String department = (String)m_departmentComboBoxField.getValue();
					m_currentPersonnel.setDepartment(department);

					try
					{
						Calendar callout = m_currentPersonnel.getCallOut();
						callout = (callout ==null ? Calendar.getInstance() : callout);
						callout = DTG.DTGToCal(callout.get(Calendar.YEAR),
								callout.get(Calendar.MONTH),m_callOutTextField.getValue());
						m_currentPersonnel.setCallOut(callout);
					}
					catch (IllegalMsoArgumentException e)
					{
					}

					m_currentPersonnel.setEstimatedArrival(parseEstimatedArrival());

					try
					{
						Calendar arrived = m_currentPersonnel.getArrived();
						arrived = (arrived ==null ? Calendar.getInstance() : arrived);
						arrived = DTG.DTGToCal(arrived.get(Calendar.YEAR),
								arrived.get(Calendar.MONTH),m_arrivedTextField.getValue());
						m_currentPersonnel.setArrived(arrived);
					}
					catch (IllegalMsoArgumentException e)
					{
					}

					try
					{
						Calendar released = m_currentPersonnel.getReleased();
						released = (released ==null ? Calendar.getInstance() : released);
						released = DTG.DTGToCal(released.get(Calendar.YEAR),
								released.get(Calendar.MONTH),m_releasedTextField.getValue());
						m_currentPersonnel.setReleased(released);
					}
					catch (IllegalMsoArgumentException e)
					{
					}

					String remarks = m_remarksTextArea.getValue();
					m_currentPersonnel.setRemarks(remarks);
					*/

					m_currentPersonnel.resumeClientUpdate(true);

					// finished!
					return true;

				}
			}
			Utils.showWarning("Begrensning","Fullt navn må oppgis for personell");
		}
		// failed!
		return false;
	}	

	/*
	private void updateEstimatedArrival()
	{
		// Don't update while user is editing
		if (m_estimatedArrivalTextField.hasFocus())
		{
			return;
		}

		Calendar arriving = m_currentPersonnel.getEstimatedArrival();
		if (arriving != null)
		{
			Calendar now = Calendar.getInstance();
			if (arriving.after(now))
			{
				long deltaMin = (arriving.getTimeInMillis() - now.getTimeInMillis()) / 60000;
				long hours = deltaMin / 60;
				long minutes = deltaMin % 60;
				StringBuilder arrivingString = new StringBuilder();
				arrivingString.append("- ");
				if (hours != 0)
				{
					arrivingString.append(hours);
					arrivingString.append(m_resources.getString("Hours.text"));
					arrivingString.append(" ");
				}
				arrivingString.append(minutes);
				arrivingString.append(m_resources.getString("Minutes.text"));
				m_estimatedArrivalTextField.setValue(arrivingString.toString());
			} else
			{
				if (m_currentPersonnel.getStatus() == PersonnelStatus.ARRIVED)
				{
					m_estimatedArrivalTextField.setValue(m_resources.getString("Arrived.text"));
				} else
				{
					m_estimatedArrivalTextField.setValue("");
				}
			}
		} else
		{
			m_estimatedArrivalTextField.setValue("");
		}
	}

	private Calendar parseEstimatedArrival()
	{
		String estimatedArrivalString = m_estimatedArrivalTextField.getValue();
		String[] arrivalStringArray = estimatedArrivalString.split("\\s");
		int hours = 0;
		int minutes = 0;
		String hoursString = m_resources.getString("Hours.text");
		String minutesString = m_resources.getString("Minutes.text");
		for (String s : arrivalStringArray)
		{
			if (s.contains(hoursString))
			{
				// Try to parse hours
				try
				{
					s = s.replaceAll("\\D", "");
					hours = Integer.valueOf(s);
				}
				catch (Exception e)
				{
				}
			} else if (s.contains(minutesString))
			{
				// Try to parse minutes
				try
				{
					s = s.replaceAll("\\D", "");
					minutes = Integer.valueOf(s);
				}
				catch (Exception e)
				{
				}
			}
		}

		Calendar estimatedArrival = Calendar.getInstance();
		estimatedArrival.add(Calendar.HOUR_OF_DAY, hours);
		estimatedArrival.add(Calendar.MINUTE, minutes);
		return estimatedArrival;
	}
	*/

	public void setCaptionText(String text)
	{
		getInfoPanel().setCaptionText(text);
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
	}

	public EnumSet<MsoClassCode> getInterests() {
		return EnumSet.of(MsoClassCode.CLASSCODE_PERSONNEL);
	}

	/**
	 * Update fields if any changes occur in the personnel object
	 */
	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		if(events.isClearAllEvent()) {
			m_currentPersonnel = null;
			updateFieldContents();
		}
		else {
			// loop over all events
			for(MsoEvent.Update e : events.getEvents(MsoClassCode.CLASSCODE_PERSONNEL)) {

				// consume loopback updates
				if(!e.isLoopback())
				{
					IPersonnelIf personnel = (e.getSource() instanceof IPersonnelIf) ?
							(IPersonnelIf) e.getSource() : null;
					if (m_currentPersonnel == personnel)
					{
						updateFieldContents();
					}
				}
			}
		}
	}
}
