package org.redcross.sar.wp.unit;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderer.BundleListCellRenderer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelType;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Calendar;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * JPanel displaying team details.
 * Responsible for storing personnel in MSO.
 *
 * @author thomasl
 */
public class PersonnelDetailsLeftPanel extends JPanel implements IMsoUpdateListenerIf, ITickEventListenerIf
{
    private static final long serialVersionUID = 1L;
    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

    private IPersonnelIf m_currentPersonnel = null;
    private IDiskoWpUnit m_wpUnit;

    private JLabel m_topLabel;
    private JButton m_changeStatusButton;

    private JTextField m_nameTextField;
    private JTextField m_cellTextField;
    private JComboBox m_propertyComboBox;
    private JTextField m_organizationTextField;
    private JTextField m_departmentTextField;
    private JTextField m_roleTextField;
    private JTextField m_unitTextField;
    private JTextField m_calloutTextField;
    private JTextField m_estimatedArrivalTextField;
    private JTextField m_arrivedTextField;
    private JTextField m_releasedTextField;
    private JTextArea m_remarksTextArea;

    private static final long UPDATE_INTERVAL = 60000;
    private long m_timeCounter;

	public PersonnelDetailsLeftPanel(IDiskoWpUnit wp)
	{
		m_wpUnit = wp;
		m_wpUnit.getMsoEventManager().addClientUpdateListener(this);
		m_wpUnit.addTickEventListener(this);
		initialize();
	}

	private void initialize()
	{
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;

		// Top
		gbc.gridwidth = 4;
		gbc.gridheight = 1;
		JPanel topPanel = new JPanel(new BorderLayout());
		m_topLabel = new JLabel();
		topPanel.add(m_topLabel, BorderLayout.CENTER);
		
		m_changeStatusButton = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
		m_changeStatusButton.setIcon(DiskoIconFactory.getIcon(m_wpUnit.getBundleText("AlertedButton.icon"),"48x48"));
		m_changeStatusButton.setToolTipText(m_wpUnit.getBundleText("AlertedButton.text"));
		m_changeStatusButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(m_currentPersonnel != null)
				{
					try {
						String command = arg0.getActionCommand();
						PersonnelStatus newStatus = PersonnelStatus.valueOf(command);

						switch(newStatus)
						{
						case ON_ROUTE:
							m_currentPersonnel = PersonnelUtilities.callOutPersonnel(m_currentPersonnel);
							m_changeStatusButton.setIcon(DiskoIconFactory.getIcon(m_wpUnit.getBundleText("AlertedButton.icon"),"48x48"));
							m_changeStatusButton.setToolTipText(m_wpUnit.getBundleText("AlertedButton.text"));
							updateFieldContents();
							break;
						case ARRIVED:
							PersonnelUtilities.arrivedPersonnel(m_currentPersonnel);
							m_changeStatusButton.setIcon(DiskoIconFactory.getIcon(m_wpUnit.getBundleText("ArrivedButton.icon"),"48x48"));
							m_changeStatusButton.setToolTipText(m_wpUnit.getBundleText("ArrivedButton.text"));
							break;
						case RELEASED:
							PersonnelUtilities.releasePersonnel(m_currentPersonnel);
							m_changeStatusButton.setIcon(DiskoIconFactory.getIcon(m_wpUnit.getBundleText("ReleasedButton.icon"),"48x48"));
							m_changeStatusButton.setToolTipText(m_wpUnit.getBundleText("ReleasedButton.text"));
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		topPanel.add(m_changeStatusButton, BorderLayout.EAST);
		this.add(topPanel, gbc);
		gbc.gridy++;

		// Name
		m_nameTextField = new JTextField();
		gbc.gridwidth = 3;
		layoutComponent(0, m_resources.getString("FullName.text"), m_nameTextField, gbc, 1);

		// Cell
		m_cellTextField = new JTextField();
		gbc.gridwidth = 3;
		layoutComponent(0, m_resources.getString("CellularPhone.text"), m_cellTextField, gbc, 1);

		// Property
		m_propertyComboBox = new JComboBox(PersonnelType.values());
		m_propertyComboBox.setSelectedItem(null);
		ResourceBundle personnelResources = Internationalization.getBundle(IPersonnelIf.class);
		m_propertyComboBox.setRenderer(new BundleListCellRenderer(personnelResources));
		gbc.gridwidth = 3;
		layoutComponent(0, m_resources.getString("Property.text"), m_propertyComboBox, gbc, 1);

		// Organization
		m_organizationTextField = new JTextField();
		gbc.gridwidth = 3;
		layoutComponent(0, m_resources.getString("Organization.text"), m_organizationTextField, gbc, 1);

		// Department
		m_departmentTextField = new JTextField();
		gbc.gridwidth = 3;
		layoutComponent(0, m_resources.getString("Department.text"), m_departmentTextField, gbc, 1);

		// Role
		gbc.gridy++;
		m_roleTextField = new JTextField();
		m_roleTextField.setEditable(false);
		layoutComponent(0, m_resources.getString("Role.text"), m_roleTextField, gbc, 0);

		// Unit
		m_unitTextField = new JTextField();
		m_unitTextField.setEditable(false);
		layoutComponent(2, m_resources.getString("Unit.text"), m_unitTextField, gbc, 1);

		// Call-out
		m_calloutTextField = new JTextField();
		layoutComponent(0, m_resources.getString("CallOut.text"), m_calloutTextField, gbc, 0);

		// Expected
		m_estimatedArrivalTextField = new JTextField();
		layoutComponent(2, m_resources.getString("ExpectedArrival.text"), m_estimatedArrivalTextField, gbc, 1);

		// Arrived
		m_arrivedTextField = new JTextField();
		layoutComponent(0, m_resources.getString("Arrived.text"), m_arrivedTextField, gbc, 0);

		// Released
		m_releasedTextField = new JTextField();
		m_releasedTextField.setEditable(false);
		layoutComponent(2, m_resources.getString("Released.text"), m_releasedTextField, gbc, 1);

		// Remarks
		m_remarksTextArea = new JTextArea();
		m_remarksTextArea.setBorder(BorderFactory.createLineBorder(Color.lightGray));
//		m_remarksTextArea.setRows(8);
		m_remarksTextArea.setWrapStyleWord(true);
		m_remarksTextArea.setLineWrap(true);
		JScrollPane notesScrollPane = new JScrollPane(m_remarksTextArea);
		gbc.gridwidth = 3;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		layoutComponent(0, m_resources.getString("Notes.text"), notesScrollPane, gbc, 1);
		
	}

	private void layoutComponent(int column, String label, JComponent component, GridBagConstraints gbc, int height)
	{
		gbc.weightx = 1.0;
		gbc.gridheight = Math.max(1, height);
		gbc.gridx = column + 1;
		this.add(component, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = column;
		gbc.gridwidth = 1;
		this.add(new JLabel(label), gbc);

		gbc.gridy += height;
	}
	
    /**
     * Updates personnel data in MSO
     */
    public boolean savePersonnel()
    {
        if (m_currentPersonnel != null)
        {
            String[] fields = m_nameTextField.getText().split(" ");
            
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
	
	                String phone = m_cellTextField.getText();
		            m_currentPersonnel.setTelephone1(phone);
		
		            PersonnelType type = (PersonnelType) m_propertyComboBox.getSelectedItem();
		            if (type == null)
		            {
		                type = PersonnelType.OTHER;
		            }
		            m_currentPersonnel.setType(type);
		
		            String organization = m_organizationTextField.getText();
		            m_currentPersonnel.setOrganization(organization);
		
		            String department = m_departmentTextField.getText();
		            m_currentPersonnel.setDepartment(department);
		
		            try
		            {
		                Calendar callout = DTG.DTGToCal(m_calloutTextField.getText());
		                m_currentPersonnel.setCallOut(callout);
		            }
		            catch (IllegalMsoArgumentException e)
		            {
		            }
		
		            m_currentPersonnel.setEstimatedArrival(parseEstimatedArrival());
		
		            try
		            {
		                Calendar arrived = DTG.DTGToCal(m_arrivedTextField.getText());
		                m_currentPersonnel.setArrived(arrived);
		            }
		            catch (IllegalMsoArgumentException e)
		            {
		            }
		
		            try
		            {
		                Calendar released = DTG.DTGToCal(m_releasedTextField.getText());
		                m_currentPersonnel.setReleased(released);
		            }
		            catch (IllegalMsoArgumentException e)
		            {
		            }
		
		            String remarks = m_remarksTextArea.getText();
		            m_currentPersonnel.setRemarks(remarks);
		
		            m_currentPersonnel.resumeClientUpdate();
		            
		            // finished!
		            return true;
		            
                }
            }
        	Utils.showWarning("Begrensning","Fullt navn må oppgis for personell");
        }
        // failed!
        return false;
    }

    /**
     * Updates field contents with current personnel attribute values
     */
    public void updateFieldContents()
    {
        if (m_currentPersonnel == null)
        {
            m_topLabel.setText("");
            m_nameTextField.setText("");
            m_cellTextField.setText("");
            m_propertyComboBox.setSelectedItem(PersonnelType.VOLUNTEER);
            m_organizationTextField.setText("");
            m_departmentTextField.setText("");
            m_roleTextField.setText("");
            m_unitTextField.setText("");
            m_calloutTextField.setText("");
            m_estimatedArrivalTextField.setText("");
            m_arrivedTextField.setText("");
            m_releasedTextField.setText("");
            m_remarksTextArea.setText("");
            //m_changeStatusButton.setText("");
        } else
        {
            m_topLabel.setText(m_currentPersonnel.getFirstname() + " " + m_currentPersonnel.getLastname() +
                    " (" + m_currentPersonnel.getStatusText() + ")");
            m_nameTextField.setText(m_currentPersonnel.getFirstname() + " " + m_currentPersonnel.getLastname());
            m_cellTextField.setText(m_currentPersonnel.getTelephone1());
            m_propertyComboBox.setSelectedItem(m_currentPersonnel.getType());
            m_organizationTextField.setText(m_currentPersonnel.getOrganization());
            m_departmentTextField.setText(m_currentPersonnel.getDepartment());

            if(m_wpUnit.getMsoManager().operationExists()) {
	            IUnitIf personnelUnit = null;
	            for (IUnitIf unit : m_wpUnit.getMsoManager().getCmdPost().getUnitListItems())
	            {
	                if (unit.getStatus() != UnitStatus.RELEASED)
	                {
	                    if (unit.getUnitPersonnel().contains(m_currentPersonnel))
	                    {
	                        personnelUnit = unit;
	                        break;
	                    }
	                }
	            }
	            m_unitTextField.setText(personnelUnit == null ? "" : personnelUnit.getTypeAndNumber());
	
	            if (personnelUnit != null)
	            {
	                if (personnelUnit.getUnitLeader() == m_currentPersonnel)
	                {
	                    m_roleTextField.setText(m_resources.getString("Leader.text"));
	                } else
	                {
	                    m_roleTextField.setText(m_resources.getString("Personnel.text"));
	                }
	            } else
	            {
	                m_roleTextField.setText("");
	            }
	
	            m_calloutTextField.setText(DTG.CalToDTG(m_currentPersonnel.getCallOut()));
	
	            updateEstimatedArrival();
	
	            if (m_currentPersonnel.getStatus() == PersonnelStatus.RELEASED)
	            {
	                m_releasedTextField.setText(m_resources.getString("Yes.text"));
	            } else
	            {
	                m_releasedTextField.setText(m_resources.getString("No.text"));
	            }
	
	            m_remarksTextArea.setText(m_currentPersonnel.getRemarks());
	
	            // Get next status for
	            PersonnelStatus status = m_currentPersonnel.getStatus();
	            PersonnelStatus[] values = PersonnelStatus.values();
	            status = values[(status.ordinal() + 1) % values.length];
	            if (status == PersonnelStatus.IDLE)
	            {
	                // Not possible to send personnel status back to idle, set to next
	                status = values[(status.ordinal() + 1) % values.length];
	            }
	            //m_changeStatusButton.setText(Internationalization.translate(status));
	            m_changeStatusButton.setActionCommand(status.name());
            }
        }
        m_nameTextField.requestFocus();
    }

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
                m_estimatedArrivalTextField.setText(arrivingString.toString());
            } else
            {
                if (m_currentPersonnel.getStatus() == PersonnelStatus.ARRIVED)
                {
                    m_estimatedArrivalTextField.setText(m_resources.getString("Arrived.text"));
                } else
                {
                    m_estimatedArrivalTextField.setText("");
                }
            }
        } else
        {
            m_estimatedArrivalTextField.setText("");
        }
    }

    private Calendar parseEstimatedArrival()
    {
        String estimatedArrivalString = m_estimatedArrivalTextField.getText();
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

    /*
      * Setters and getters
      */
    public IPersonnelIf getPersonnel()
    {
        return m_currentPersonnel;
    }

    public void setTopLabelText(String text)
    {
        m_topLabel.setText(text);
    }

    public void setPersonnel(IPersonnelIf personnel)
    {
        m_currentPersonnel = personnel;
    }
    
    /**
     * Update fields if any changes occur in the personnel object
     */
    public void handleMsoUpdateEvent(Update e)
    {
        IPersonnelIf personnel = (e.getSource() instanceof IPersonnelIf) ? 
        		(IPersonnelIf) e.getSource() : null;
        if (m_currentPersonnel == personnel)
        {
            updateFieldContents();
        }
    }    
    
	public boolean hasInterestIn(IMsoObjectIf msoObject, UpdateMode mode) 
	{
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
        return msoObject.getMsoClassCode() == IMsoManagerIf.MsoClassCode.CLASSCODE_PERSONNEL;
    }

    public long getInterval()
    {
        return UPDATE_INTERVAL;
    }

    public long getTimeCounter()
    {
        return m_timeCounter;
    }

    /**
     * Update time dependent fields
     */
    public void handleTick(TickEvent e)
    {
    	if(m_wpUnit.getMsoManager().operationExists()) {
	        ICmdPostIf cmdPost = m_wpUnit.getCmdPost();
	        if (cmdPost == null)
	        {
	            return;
	        }
	
	        // Don't update if adding new personnel
	        if (m_currentPersonnel != null && !m_wpUnit.getNewPersonnel())
	        {
	            updateEstimatedArrival();
	        }
    	}
    }

    public void setTimeCounter(long counter)
    {
        m_timeCounter = counter;
    }
}
