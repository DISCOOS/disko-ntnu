package org.redcross.sar.wp.unit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.renderer.BundleListCellRenderer;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.mso.DTG;



/**
 * Panel displaying personnel details at bottom of main panel
 *
 * @author thomasl
 */
public class PersonnelDetailsBottomPanel extends JPanel implements IMsoUpdateListenerIf, ITickEventListenerIf
{
    private static final long serialVersionUID = 1L;

    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

    private JTextField m_nameTextField;
    private JTextField m_calloutTextField;
    private JTextField m_cellularTextField;
    private JComboBox m_propertyComboBox;
    private JTextField m_estimatedArrivalTextField;
    private JTextField m_remarksTextField;
    private JTextField m_arrivedTextField;
    private JTextField m_organizationTextField;
    private JTextField m_departmentTextField;
    private JTextField m_releasedTextField;

    private IPersonnelIf m_currentPersonnel;

    private static final int UPDATE_INTERVAL = 60000;
    private long m_timeCounter;

    IDiskoWpUnit m_wp;

    public PersonnelDetailsBottomPanel(IDiskoWpUnit wp)
    {
    	// prepare
    	m_wp = wp;
    	// initialize GUI
        initialize();
        // add listeners
        wp.getMsoEventManager().addClientUpdateListener(this);
    }

    private void initialize()
    {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridy = 0;

        m_nameTextField = new JTextField();
        m_nameTextField.setEditable(false);
        gbc.gridwidth = 3;
        layoutComponent(0, m_resources.getString("FullName.text"), m_nameTextField, gbc, 0);

        m_calloutTextField = new JTextField();
        m_calloutTextField.setEditable(false);
        layoutComponent(4, m_resources.getString("CallOut.text"), m_calloutTextField, gbc, 1);

        m_cellularTextField = new JTextField();
        m_cellularTextField.setEditable(false);
        layoutComponent(0, m_resources.getString("CellularPhone.text"), m_cellularTextField, gbc, 0);

        ResourceBundle personnelResources = Internationalization.getBundle(IPersonnelIf.class);
        m_propertyComboBox = new JComboBox(PersonnelType.values());
        m_propertyComboBox.setEnabled(false);
        m_propertyComboBox.setRenderer(new BundleListCellRenderer(personnelResources));
        layoutComponent(2, m_resources.getString("Property.text"), m_propertyComboBox, gbc, 0);

        m_estimatedArrivalTextField = new JTextField();
        m_estimatedArrivalTextField.setEditable(false);
        layoutComponent(4, m_resources.getString("ExpectedArrival.text"), m_estimatedArrivalTextField, gbc, 1);

        m_remarksTextField = new JTextField();
        m_remarksTextField.setEditable(false);
        gbc.gridwidth = 3;
        layoutComponent(0, m_resources.getString("Notes.text"), m_remarksTextField, gbc, 0);

        m_arrivedTextField = new JTextField();
        m_arrivedTextField.setEditable(false);
        layoutComponent(4, m_resources.getString("Arrived.text"), m_arrivedTextField, gbc, 1);

        m_organizationTextField = new JTextField();
        m_organizationTextField.setEditable(false);
        layoutComponent(0, m_resources.getString("Organization.text"), m_organizationTextField, gbc, 0);

        m_departmentTextField = new JTextField();
        m_departmentTextField.setEditable(false);
        layoutComponent(2, m_resources.getString("Department.text"), m_departmentTextField, gbc, 0);

        m_releasedTextField = new JTextField();
        m_releasedTextField.setEditable(false);
        layoutComponent(4, m_resources.getString("Released.text"), m_releasedTextField, gbc, 1);

        JScrollPane mainPanel = UIFactory.createScrollPane(this);
        mainPanel.setViewportView(this);
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
     * Set the current personnel
     *
     * @param personnel
     */
    public void setPersonnel(IPersonnelIf personnel)
    {
        m_currentPersonnel = personnel;
    }

    /**
     * Set field contents to that of the current personnel
     */
    public void updateFieldContents()
    {
        if (m_currentPersonnel == null)
        {
            // Clear contents
            m_nameTextField.setText("");
            m_calloutTextField.setText("");
            m_cellularTextField.setText("");
            m_propertyComboBox.setSelectedItem(null);
            m_estimatedArrivalTextField.setText("");
            m_remarksTextField.setText("");
            m_arrivedTextField.setText("");
            m_organizationTextField.setText("");
            m_departmentTextField.setText("");
            m_releasedTextField.setText("");
        } else
        {
            String name = m_currentPersonnel.getFirstName() + " " + m_currentPersonnel.getLastName();
            m_nameTextField.setText(name);

            String callout = m_currentPersonnel.getCallOut() == null ? ""
                    : DTG.CalToDTG(m_currentPersonnel.getCallOut());
            m_calloutTextField.setText(callout);

            m_cellularTextField.setText(m_currentPersonnel.getTelephone1());

            m_propertyComboBox.setSelectedItem(m_currentPersonnel.getType());

            updateEstimatedArrival();

            m_remarksTextField.setText(m_currentPersonnel.getRemarks());

            String arrived = m_currentPersonnel.getArrived() == null ? ""
                    : DTG.CalToDTG(m_currentPersonnel.getArrived());
            m_arrivedTextField.setText(arrived);

            m_organizationTextField.setText(m_currentPersonnel.getOrganization());

            m_departmentTextField.setText(m_currentPersonnel.getDepartment());

            if (m_currentPersonnel.getStatus() == PersonnelStatus.RELEASED)
            {
                m_releasedTextField.setText(m_resources.getString("Yes.text"));
            } else
            {
                m_releasedTextField.setText(m_resources.getString("No.text"));
            }
        }
    }

    private void updateEstimatedArrival()
    {
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

	public EnumSet<MsoClassCode> getInterests() {
		return EnumSet.of(MsoClassCode.CLASSCODE_PERSONNEL);
	}

    /**
     * Update field contents if MSO object changes
     */
	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		if(events.isClearAllEvent()) {
			setPersonnel(null);
            updateFieldContents();
		}
		else
		{
			// loop over all events
			for(MsoEvent.Update e : events.getEvents(MsoClassCode.CLASSCODE_PERSONNEL))
			{
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
					}				}
			}
		}
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
    	if(m_wp.getMsoManager().operationExists()) {
	        ICmdPostIf cmdPost = m_wp.getCmdPost();
	        if (cmdPost == null)
	        {
	            return;
	        }

	        updateEstimatedArrival();
    	}
    }

    public void setTimeCounter(long counter)
    {
        m_timeCounter = counter;
	}
}
