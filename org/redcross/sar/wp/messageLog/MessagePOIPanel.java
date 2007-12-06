package org.redcross.sar.wp.messageLog;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoButtonFactory;
import org.redcross.sar.gui.DiskoButtonFactory.ButtonType;
import org.redcross.sar.gui.DrawDialog;
import org.redcross.sar.gui.NumPadDialog;
import org.redcross.sar.gui.POIPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.POITool;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.data.ITaskIf.TaskPriority;
import org.redcross.sar.mso.data.ITaskIf.TaskType;
import org.redcross.sar.wp.messageLog.ChangeTasksDialog.TaskSubType;

/**
 * Dialog used to update message poi lines when editing the message log. 
 * 
 * @author thomasl
 */
public class MessagePOIPanel extends JPanel implements IEditMessageComponentIf
{
	private final static long serialVersionUID = 1L;

	protected JButton m_okButton = null;
	protected JButton m_centerAtButton = null;
	protected JButton m_cancelButton = null;
	protected IDiskoWpMessageLog m_wpMessageLog = null;
	protected POITool m_tool = null;
	protected POIPanel m_poiPanel = null;
	protected POIType[] types = null;
	protected HashMap<String,IUnitIf> m_units = null;

	/**
	 * @param wp Message log work process
	 * @param poiTypes Which POI types are valid in panel
	 */
	public MessagePOIPanel(IDiskoWpMessageLog wp, POIType[] poiTypes)
	{
		// prepare
		m_wpMessageLog = wp;
		types = poiTypes;
		m_tool = m_wpMessageLog.getApplication().getNavBar().getPOITool();
		// initialize gui
		initialize();
	}

	private void initialize()
	{
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		initContents(gbc);
		initButtons(gbc);

		// hide
		this.setVisible(false);
		m_wpMessageLog.getApplication().getUIFactory().getNumPadDialog().setVisible(false);		
        MessageLogPanel.hideMap();

	}


	private void initContents(GridBagConstraints gbc) {
		gbc.gridheight = 3;
		this.add(getPOIPanel(),gbc);
	}	
	
	private void initButtons(GridBagConstraints gbc)
	{
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.gridx++;
		
		m_cancelButton = DiskoButtonFactory.createNormalButton(ButtonType.CancelButton);
		m_cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				NumPadDialog numPad = m_wpMessageLog.getApplication().getUIFactory().getNumPadDialog();
				numPad.setVisible(false);
				revertPOI();
				MessageLogBottomPanel.showListPanel();
			}
		});
		this.add(m_cancelButton, gbc);

		gbc.gridy = 1;		
		m_centerAtButton = DiskoButtonFactory.createNormalButton();
		m_centerAtButton.setIcon(Utils.getIcon("IconEnum.CENTERAT.icon"));
		m_centerAtButton.setToolTipText(Utils.getProperty("IconEnum.CENTERAT.text"));
		m_centerAtButton.addActionListener(new ActionListener()
		{
			/**
			 * Add/update POI in current message
			 */
			public void actionPerformed(ActionEvent e)
			{
				NumPadDialog numPad = m_wpMessageLog.getApplication().getUIFactory().getNumPadDialog();
				numPad.setVisible(false);
				centerAtPOI(true);
			}
		});
		this.add(m_centerAtButton, gbc);
		
		gbc.gridy = 2;		
		m_okButton = DiskoButtonFactory.createNormalButton(ButtonType.OkButton);
		m_okButton.addActionListener(new ActionListener()
		{
			/**
			 * Add/update POI in current message
			 */
			public void actionPerformed(ActionEvent e)
			{
				NumPadDialog numPad = m_wpMessageLog.getApplication().getUIFactory().getNumPadDialog();
				numPad.setVisible(false);
				applyPOI();
				MessageLogBottomPanel.showListPanel();
			}
		});
		this.add(m_okButton, gbc);

	}

	/**
	 * apply POI position and type in current message based on values in GUI fields
	 */
	private void applyPOI()
	{

		// suspend update events
		m_wpMessageLog.getMsoModel().suspendClientUpdate();
		
		// get panel
		POIPanel panel = getPOIPanel();
		
		try {
			// get point
			Point point = panel.getPOIField().getPoint();
			
			// add or move poi?
			if(point!=null) {
				
				// get current message, create if not exists
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);

				// Get message line, create if not exist
				IMessageLineIf messageLine = message.findMessageLine(MessageLineType.POI, true);

				// Create new POI and message line if POI message line did not exists
				IPOIIf poi = messageLine.getLinePOI();
				
				// get flag
				boolean isWorkPoolMode = m_tool.isWorkPoolMode();
				
				// get poi type
				POIType poiType = panel.getPOIType();
				
				// update poi
				m_tool.setCurrentPOI(poi);
				
				// reset flag
				m_tool.setWorkPoolMode(false);				
				
				// new line created just now?
				if(poi == null)
				{	
					// add new poi
					m_tool.addPOIAt(point, poiType, null);
					
					// get added poi
					poi = m_tool.getCurrentPOI();
					
					// update linste
					messageLine.setLinePOI(poi);
				}
				else {
					// add new poi
					m_tool.movePOIAt(point, poiType, null);					
				}
				// resume mode
				m_tool.setWorkPoolMode(isWorkPoolMode);
				// update task?
				if(poi!=null && (POIType.FINDING.equals(poi.getType()) || 
						POIType.SILENT_WITNESS.equals(poi.getType())))
					setTask(message,poi,TaskType.INTELLIGENCE,TaskSubType.FINDING,TaskPriority.HIGH);
			}
			else {
				// notify
				Utils.showWarning("Ingen posisjon er valgt");
			}
		} catch (Exception ex) {
			// notify
			Utils.showWarning("Ugyldig koordinat format");
		}

		// resume update
		m_wpMessageLog.getMsoModel().resumeClientUpdate();
	}

	private void setTask(IMessageIf message, IPOIIf poi, TaskType type, TaskSubType subType, TaskPriority priority) {
		
		// Need to add/update task
		ITaskIf task = null;
		
		String match = m_wpMessageLog.getText("TaskSubType."+subType.toString()+".text");
		
		// Check for existing tasks
		for(ITaskIf messageTask : message.getMessageTasksItems())
		{
			if(messageTask.getType() == type)
			{
				// Check to see if task is a finding task
				String taskText = messageTask.getTaskText().split(":")[0];
				if(taskText.equals(match.split(":")[0]))
				{
					// Message has a intelligence task, update this
					task = messageTask;
				}
			}
		}
		
		// If message does not have a finding task, create new
		if(task == null)
		{
			task = m_wpMessageLog.getMsoManager().createTask(Calendar.getInstance());
			task.setCreated(Calendar.getInstance());
			task.setAlert(Calendar.getInstance());
			task.setPriority(priority);
			task.setResponsibleRole(null);
			task.setType(type);
			task.setSourceClass(message.getMsoClassCode());
			task.setCreatingWorkProcess(getName());
			// task.setDependentObject(message.getSender());
			message.addMessageTask(task);
		}
		
		// Update task text
		task.setTaskText(String.format(match, Utils.getIconText(poi.getType())));
		
	}
	
	/**
	 * Reverts contents of text fields to what is stored in MSO
	 */
	private void revertPOI()
	{
		// suspend update events
		m_wpMessageLog.getMsoModel().suspendClientUpdate();
		
		// get current message, do not create if not exist
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		
		// has message?
		if(message != null)
		{
			// get message line, do not create if not exist
			IMessageLineIf line =  message.findMessageLine(MessageLineType.POI, false);

			// has line
			if(line != null)
			{
				IPOIIf poi = line.getLinePOI();
				getPOIPanel().setCurrentPOI(poi);
			}
		}
		// resume update
		m_wpMessageLog.getMsoModel().resumeClientUpdate();
	}


	public void clearContents()
	{
		getPOIPanel().reset();
	}

	public void showComponent()
	{
		// do not show dialog in map
		m_tool.setShowDialog(false);
		// show map
		MessageLogPanel.showMap();
		// show poi in map
		IPOIIf poi = centerAtPOI(true);
		// get draw tool
		DrawDialog dialog = (DrawDialog)m_tool.getDialog();
		// get poi panel
		POIPanel panel = getPOIPanel();
		// get attributes (isUpdateMode,isNotAreaPoi,no owner,current poi)
		Object[] attributes = {true,false,null,poi};
		// update layout
		panel.setTypes(types);
		panel.setButtonsVisible(false);
		panel.setTypesVisible(true);
		panel.setRemarksVisible(false);
		panel.setVertical(false);
		// make it the active property panel
		m_tool.setPropertyPanel(panel);
		// set tool active
		dialog.setActiveTool(m_tool);
		dialog.setToolSet(MsoClassCode.CLASSCODE_POI, attributes);
		// show tool
		m_wpMessageLog.getApplication().getNavBar().setVisibleButtons(
				EnumSet.of(DiskoToolType.POI_TOOL), true, true);
		// buffer changes. use m_tool.apply() to update the mso model
		m_tool.setBuffered(true);
		// show panel
		this.setVisible(true);
	}

	public void hideComponent()
	{
		// hide tool
		m_wpMessageLog.getApplication().getNavBar().setVisibleButtons(
				EnumSet.of(DiskoToolType.POI_TOOL), false, true);
		// hide num pad
		NumPadDialog numPad = m_wpMessageLog.getApplication().getUIFactory().getNumPadDialog();
		numPad.setVisible(false);
		// apply change directly to mso model
		m_tool.setBuffered(false);
		// hide me
		this.setVisible(false);
		// hide map
        MessageLogPanel.hideMap();
        // deselect
        centerAtPOI(false);

    }

	private void updatePOI(IMessageIf message)
	{
		// create or get current fining message line
		IMessageLineIf messageLine = message.findMessageLine(MessageLineType.POI, false);

		try
		{
			// Update components
			if(messageLine == null)
			{
				// Message don't have a POI message line
				getPOIPanel().reset();
			}
			else
			{
				// get current message line
				IPOIIf poi = messageLine.getLinePOI();
				// forward
				getPOIPanel().setCurrentPOI(poi);
			}
		}
		catch(Exception e){}
	}

	/**
	 * Update position fields with message POI position. Zoom to POI
	 */
	public void newMessageSelected(IMessageIf message)
	{
		// Update dialog
		updatePOI(message);
	}

	/**
	 * Get the message log work process
	 */
	public IDiskoWpMessageLog getWP()
	{
		return m_wpMessageLog;
	}

	/**
	 * @return POI type selected in combo box
	 */
	public POIType getPOIType()
	{
		return getPOIPanel().getPOIType();
	}

	/**
	 * Set the tool for the current work process map
	 */
	public void setMapTool()
	{
		IDiskoMap map = m_wpMessageLog.getMap();
		try
		{
			map.setCurrentToolByRef(m_tool,true);
		}
		catch (AutomationException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * Set selection for POI in map
	 */
	public IPOIIf centerAtPOI(boolean isSelected)
	{
		// initialize
		IPOIIf poi = null;
		
		// Get message, do not create if not exist
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		
		// has message?
		if(message != null)
		{
			
			// get poi message line, do not create if not exist
			IMessageLineIf line = message.findMessageLine(MessageLineType.POI, false);
			
			// has line
			if(line != null)
			{
				poi = line.getLinePOI();
			}

			// Select POI object in map
			if(poi != null)
			{
				try
				{
                	IDiskoMap map = m_wpMessageLog.getMap();
                	map.suspendNotify();
					map.setSelected(poi, isSelected);
					if(isSelected) 
						map.centerOnMsoObject(poi);
                	map.resumeNotify();
					map.refreshSelection(poi, map.getSelectionExtent());
				}
				catch (AutomationException e1)
				{
					e1.printStackTrace();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
		// return current poi
		return poi;
	}
	
	private POIPanel getPOIPanel() {
		if(m_poiPanel==null) {

			// create panel
			m_poiPanel = (POIPanel)m_tool.addPropertyPanel();
			m_poiPanel.setPreferredSize(new Dimension(600,200));

			// set horizontal flow
			m_poiPanel.setVertical(true);
						
		}
		return m_poiPanel;
	
	}	
	
}
