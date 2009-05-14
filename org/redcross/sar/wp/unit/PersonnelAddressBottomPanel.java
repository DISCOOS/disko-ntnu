package org.redcross.sar.wp.unit;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

import java.util.EnumSet;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Bottom panel displaying summary info about personnel
 *
 * @author thomasl, kenneth
 */
public class PersonnelAddressBottomPanel extends JPanel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;

    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);


	private IPersonnelIf m_currentPersonnel;

	private FieldsPanel m_infoPanel;
	private TextField m_addressTextField;
	private TextField m_postAreaTextField;
	private TextField m_postNumberTextField;
	private JButton m_centerAtButton;
	
	IDiskoWpUnit m_wp;

	public PersonnelAddressBottomPanel(IDiskoWpUnit wp)
	{
		// prepare
		m_wp = wp;
		// initialize GUI
		initialize();
		// add listeners
		wp.getMsoEventManager().addClientUpdateListener(this);
		getInfoPanel().addWorkFlowListener(wp);
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
			m_infoPanel.setColumns(3);
			m_infoPanel.setBatchMode(false);
			m_infoPanel.setHeaderVisible(false);
			m_infoPanel.setPreferredExpandedHeight(275);
			m_infoPanel.addButton(getCenterAtButton(), "centerat");
			m_infoPanel.suspendLayout();
			m_infoPanel.addField(getAddressTextField());
			m_infoPanel.addField(getPostNumberTextField());
			m_infoPanel.addField(getPostAreaTextField());
			m_infoPanel.setFieldSpanX("address", 3);
			m_infoPanel.setFieldSpanX("postnumber", 1);
			m_infoPanel.setFieldSpanX("postarea", 2);
			m_infoPanel.resumeLayout();
			m_infoPanel.connect(m_wp.getMsoModel(), EnumSet.of(MsoClassCode.CLASSCODE_PERSONNEL));
		}
		return m_infoPanel;
	}
	
	private JButton getCenterAtButton() {
		if(m_centerAtButton==null) {
			m_centerAtButton = DiskoButtonFactory.createButton("MAP.GOTO",ButtonSize.SMALL);
			m_centerAtButton.setEnabled(false);
		}
		return m_centerAtButton;
	}	
	
	private TextField getAddressTextField() {
		if(m_addressTextField==null) {
			m_addressTextField = new TextField("address",m_resources.getString("Address.text"),true);
			m_addressTextField.addWorkFlowListener(new IWorkFlowListener() {
				public void onFlowPerformed(WorkFlowEvent e) {
					if(isSet()&&m_addressTextField.isChangeable()&&e.isChange()&&e.isWorkDoneByAwtComponent()) {
						setAddress();
					}					
				}				
			});
		}
		return m_addressTextField;
	}	

	private TextField getPostAreaTextField() {
		if(m_postAreaTextField==null) {
			m_postAreaTextField = new TextField("postarea",m_resources.getString("PostArea.text"),true);  
			m_postAreaTextField.addWorkFlowListener(new IWorkFlowListener() {
				public void onFlowPerformed(WorkFlowEvent e) {
					if(m_postAreaTextField.isChangeable()&&e.isChange()&&e.isWorkDoneByAwtComponent()) {
						setAddress();
					}					
				}
			});
		}
		return m_postAreaTextField;
	}	
	
	private TextField getPostNumberTextField() {
		if(m_postNumberTextField==null) {
			m_postNumberTextField = new TextField("postnumber",m_resources.getString("PostNumber.text"),true);    		
			m_postNumberTextField.addWorkFlowListener(new IWorkFlowListener() {
				public void onFlowPerformed(WorkFlowEvent e) {
					if(m_postAreaTextField.isChangeable()&&e.isChange()&&e.isWorkDoneByAwtComponent()) {
						setAddress();
					}					
				}
			});
		}
		return m_postNumberTextField;
	}	
		

	/**
	 * Set the personnel the panel is currently displaying
	 * @param personnel
	 */
	public void setPersonnel(IPersonnelIf personnel)
	{
		m_currentPersonnel = personnel;
		updateFieldContents();
    }

	/**
	 * Update field contents with current personnel values
	 */
	private void updateFieldContents()
	{
		// prevent reenty
		getInfoPanel().setChangeable(false);

		if(m_currentPersonnel == null)
		{
			m_addressTextField.setValue("");
			m_postAreaTextField.setValue("");
			m_postNumberTextField.setValue("");
		}
		else
		{
			String[] address = m_currentPersonnel.getAddress().split(";");

			if(address.length == 3)
			{
				m_addressTextField.setValue(address[0]);
				m_postAreaTextField.setValue(address[1]);
				m_postNumberTextField.setValue(address[2]);
			}
			else
			{
				m_addressTextField.setValue("");
				m_postAreaTextField.setValue("");
				m_postNumberTextField.setValue("");
			}
		}

		// resume reenty
		getInfoPanel().setChangeable(true);

	}

    public boolean isChanged() {
    	return m_currentPersonnel!=null?m_currentPersonnel.isChanged():false;
    }
    
    public boolean isSet() {
    	return m_currentPersonnel!=null;
    }
    
	private String getAddress() {
		// Store address fields in single string, separated by ;
		return  m_addressTextField.getValue() + ";" +
				m_postAreaTextField.getValue() + ";" + 
				m_postNumberTextField.getValue();
	}
	
	private void setAddress() {
		// Store address fields in single string, separated by ;
		m_currentPersonnel.suspendClientUpdate();
		m_currentPersonnel.setAddress(getAddress());
		m_currentPersonnel.resumeClientUpdate(true);
		m_wp.onFlowPerformed(new WorkFlowEvent(this,m_currentPersonnel,WorkFlowEvent.EVENT_CHANGE));
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
