package org.redcross.sar.wp.unit;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.util.Internationalization;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Bottom panel displaying summary info about personnel
 *
 * @author thomasl
 */
public class PersonnelAddressBottomPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);


	private IPersonnelIf m_currentPersonnel;

	private JTextField m_addressTextField;
	private JTextField m_postAreaTextField;
	private JTextField m_postNumberTextField;
	private JButton m_showInMapButton;

	public PersonnelAddressBottomPanel()
	{
		initialize();
	}

	private void initialize()
	{
		this.setLayout(new GridBagLayout());
		GridBagConstraints topLevelConstraints = new GridBagConstraints();
		topLevelConstraints.fill = GridBagConstraints.BOTH;
		topLevelConstraints.gridheight = 1;
		topLevelConstraints.weightx = 1.0;

		// Top left
		JPanel topLeftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints topLeftConstraints = new GridBagConstraints();
		topLeftConstraints.insets = new Insets(4, 4, 4, 4);
		topLeftConstraints.fill = GridBagConstraints.HORIZONTAL;

		topLeftConstraints.weightx = 0.0;
		topLeftPanel.add(new JLabel(m_resources.getString("Address.text")), topLeftConstraints);
		topLeftConstraints.gridwidth = 3;
		topLeftConstraints.gridx = 1;
		topLeftConstraints.weightx = 1.0;
		m_addressTextField = new JTextField();
		topLeftPanel.add(m_addressTextField, topLeftConstraints);

		topLeftConstraints.gridy = 1;
		topLeftConstraints.gridx = 0;
		topLeftConstraints.gridwidth = 1;
		topLeftConstraints.weightx = 0.0;
		topLeftPanel.add(new JLabel(m_resources.getString("PostArea.text")), topLeftConstraints);
		topLeftConstraints.gridx = 1;
		topLeftConstraints.weightx = 1.0;
		m_postAreaTextField = new JTextField();
		topLeftPanel.add(m_postAreaTextField, topLeftConstraints);

		topLeftConstraints.gridx = 2;
		topLeftConstraints.weightx = 0.0;
		topLeftPanel.add(new JLabel(m_resources.getString("PostNumber.text")), topLeftConstraints);
		topLeftConstraints.gridx = 3;
		topLeftConstraints.weightx = 1.0;
		m_postNumberTextField = new JTextField();
		topLeftPanel.add(m_postNumberTextField, topLeftConstraints);

		topLeftConstraints.gridx = 4;
		topLeftConstraints.gridy = 0;
		topLeftConstraints.gridheight = 2;
		topLeftConstraints.weightx = 0.0;
		m_showInMapButton = DiskoButtonFactory.createButton("MAP.GOTO", ButtonSize.NORMAL);
		m_showInMapButton.setVisible(false);
		topLeftPanel.add(m_showInMapButton, topLeftConstraints);

		topLevelConstraints.gridwidth = 2;
		topLevelConstraints.weighty = 0.5;
		this.add(topLeftPanel, topLevelConstraints);

		// Bottom left
		JPanel bottomLeftPanel = new JPanel();
		topLevelConstraints.gridy = 1;
		topLevelConstraints.weighty = 1.0;
		this.add(bottomLeftPanel, topLevelConstraints);

		// Right
		JPanel rightPanel = new JPanel();

		topLevelConstraints.gridx = 2;
		topLevelConstraints.gridy = 0;
		topLevelConstraints.gridwidth = 1;
		topLeftConstraints.gridheight = 3;
		this.add(rightPanel, topLevelConstraints);
	}

	/**
	 * Set the personnel the panel is currently displaying
	 * @param personnel
	 */
	public void setPersonnel(IPersonnelIf personnel)
	{
		m_currentPersonnel = personnel;
		setEnabled(personnel!=null);
	}

	/**
	 * Update field contents with current personnel values
	 */
	public void updateFieldContents()
	{
		if(m_currentPersonnel == null)
		{
			m_addressTextField.setText("");
			m_postAreaTextField.setText("");
			m_postNumberTextField.setText("");
			m_showInMapButton.setEnabled(false);
		}
		else
		{
			String[] address = m_currentPersonnel.getAddress().split(";");

			if(address.length == 3)
			{
				m_addressTextField.setText(address[0]);
				m_postAreaTextField.setText(address[1]);
				m_postNumberTextField.setText(address[2]);
			}
			else
			{
				m_addressTextField.setText("");
				m_postAreaTextField.setText("");
				m_postNumberTextField.setText("");
			}

			m_showInMapButton.setEnabled(true);
		}
	}

	/**
	 * Save field contents to current personnel
	 */
	public void savePersonnel()
	{
		if(m_currentPersonnel != null)
		{
			m_currentPersonnel.suspendClientUpdate();

			// Store address fields in single string, separated by ;
			String address = m_addressTextField.getText() + ";" +
				m_postAreaTextField.getText() + ";" + m_postNumberTextField.getText();

			m_currentPersonnel.setAddress(address);

			m_currentPersonnel.resumeClientUpdate();
		}
	}
}
