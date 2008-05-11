package org.redcross.sar.wp.messageLog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.GotoPanel;
import org.redcross.sar.gui.map.POIPanel;
import org.redcross.sar.gui.map.POITypesPanel;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.POITool;
import org.redcross.sar.map.command.IDrawTool.DrawMode;
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
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.wp.messageLog.ChangeTasksDialog.TaskSubType;

/**
 * Dialog used to update message poi lines when editing the message log. 
 * 
 * @author thomasl
 */
public class MessagePOIPanel extends DiskoPanel implements IEditMessageComponentIf
{
	private final static long serialVersionUID = 1L;

	protected JPanel m_actionsPanel = null;
	protected JButton m_finishButton = null;
	protected JButton m_centerAtButton = null;
	protected JButton m_cancelButton = null;
	protected POIPanel m_poiPanel = null;
	protected GotoPanel m_gotoPanel = null;
	protected POITypesPanel m_typesPanel = null;

	protected IDiskoWpMessageLog m_wp = null;
	
	protected POITool m_tool = null;
	protected POIType[] m_types = null;
	protected HashMap<String,IUnitIf> m_units = null;
	
	private int isWorking = 0;

	/**
	 * @param wp Message log work process
	 * @param poiTypes Which POI types are valid in panel
	 */
	public MessagePOIPanel(IDiskoWpMessageLog wp, POIType[] poiTypes)
	{
		// prepare
		m_wp = wp;
		m_types = poiTypes;
		m_tool = wp.getApplication().getNavBar().getPOITool();
		
		// initialize gui
		initialize();
	}

	private void initialize()
	{
		// hide borders
		setBorderVisible(false);
		
		// hide me
		setVisible(false);

		// hide map
        MessageLogPanel.hideMap();		
        
		// turn off vertical scrollbar
		setScrollBarPolicies(
				JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		// set layout
		setBodyLayout(new BoxLayout((JComponent)getBodyComponent(),BoxLayout.X_AXIS));
		
		// add empty border
		setBodyBordrer(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		// add components (BorderLayout is default)
		addBodyComponent(getTypesPanel());
		addBodyComponent(Box.createHorizontalStrut(5));
		addBodyComponent(getGotoPanel());
		addBodyComponent(Box.createHorizontalStrut(5));
		addBodyComponent(getActionsPanel());
		
	}
	
	private JPanel getActionsPanel() {
		if(m_actionsPanel==null) {
			// create panel
			m_actionsPanel = new JPanel();
			// set layout manager on y-axis
			m_actionsPanel.setLayout(new BoxLayout(m_actionsPanel,BoxLayout.Y_AXIS));
			// add buttons
			m_actionsPanel.add(getCancelButton());
			m_actionsPanel.add(getCenterAtButton());
			m_actionsPanel.add(getFinishButton());
		}
		return m_actionsPanel;
	
	}
	
	private JButton getFinishButton() {
		if(m_finishButton==null) {
			// create button
			m_finishButton = DiskoButtonFactory.createButton("GENERAL.OK",ButtonSize.NORMAL);
			// add action listener
			m_finishButton.addActionListener(new ActionListener() {
				/**
				 * Add/update POI in current message
				 */
				public void actionPerformed(ActionEvent e){
					if(applyPOI())
						MessageLogBottomPanel.showListPanel();				
				}
			});

		}
		return m_finishButton;
	
	}
	
	private JButton getCenterAtButton() {
		if(m_centerAtButton==null) {
			// create button
			m_centerAtButton = DiskoButtonFactory.createButton("MAP.CENTERAT",ButtonSize.NORMAL);
			// add action listener
			m_centerAtButton.addActionListener(new ActionListener() {
				/**
				 * Center map at position
				 */
				public void actionPerformed(ActionEvent e){
					centerAtPOI(true);
				}
			});

		}
		return m_centerAtButton;
	
	}

	private JButton getCancelButton() {
		if(m_cancelButton==null) {
			// create button
			m_cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
			// add action listener
			m_cancelButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					// cancel any changes
					revertPOI();
					// return to list view
					MessageLogBottomPanel.showListPanel();
				}
			});
		}
		return m_cancelButton;
	
	}
	
	/**
	 * This method initializes POIPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	private POIPanel getPOIPanel() {
		if (m_poiPanel == null) {

			// create a poi panel and register it with a tool
			m_poiPanel = (POIPanel)m_tool.addPropertyPanel();			

		}
		return m_poiPanel;
	}
	
	/**
	 * This method initializes GotoPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	private GotoPanel getGotoPanel() {
		if (m_gotoPanel == null) {
			// get from position panel
			m_gotoPanel = getPOIPanel().getGotoPanel();
			// get hide goto button			
			m_gotoPanel.setGotoButtonVisible(false);
			// turn off vertical scrollbar
			m_gotoPanel.setScrollBarPolicies(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// set preferred size of body component
			m_gotoPanel.setPreferredSize(new Dimension(275,125));
		}
		return m_gotoPanel;
	}
	
	/**
	 * This method initializes TypesPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	private POITypesPanel getTypesPanel() {
		if (m_typesPanel == null) {
			// get from position panel
			m_typesPanel = getPOIPanel().getTypesPanel();
			// turn off vertical scrollbar
			m_typesPanel.setScrollBarPolicies(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// set preferred size of body component
			m_typesPanel.setPreferredSize(new Dimension(100,125));
			
		}
		return m_typesPanel;
	}
	
	private boolean isWorking() {
		return (isWorking>0);
	}

	private int setIsWorking() {
		isWorking++;
		return isWorking; 
	}
	
	private int setIsNotWorking() {
		if(isWorking>0) {
			isWorking--;
		}
		return isWorking; 
	}
	
	/**
	 * apply POI position and type in current message based on values in GUI fields
	 */
	private boolean applyPOI()
	{

		// set flag
		setIsWorking();
		
		// initialize flag
		boolean isSuccess = false;
		
		// suspend update events
		m_wp.getMsoModel().suspendClientUpdate();
		
		// get panel
		POIPanel panel = getPOIPanel();
		
		try {
			
			// get point
			Point point = panel.getGotoPanel().getPositionField().getPoint();
			
			// add or move poi?
			if(point!=null) {
				
				// initialize flag
				boolean isDirty = false;
				
				// get current message, do create if not exists
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);

				// Get message line, do not create if not exist
				IMessageLineIf messageLine = (message!=null ? message.findMessageLine(MessageLineType.POI, false) : null);

				// get poi
				IPOIIf poi = (messageLine!=null ? messageLine.getLinePOI() : null);
				
				// get flag
				boolean isWorkPoolMode = m_tool.isWorkPoolMode();
				
				// get poi type
				POIType poiType = panel.getPOIType();
				
				// reset flag
				m_tool.setWorkPoolMode(false);				
				
				// new line created just now?
				if(poi == null)
				{	
					// add new poi
					m_tool.addPOIAt(point, poiType, null);
					
					// get added poi
					poi = m_tool.getCurrentPOI();
					
					// create message?
					if(message==null) {
						// create message and message line
						message = MessageLogBottomPanel.getCurrentMessage(true);	
						
					}
					
					// get message line, create if not exists
					messageLine = message.findMessageLine(MessageLineType.POI, true);
					
					// update line
					messageLine.setLinePOI(poi);
					
				}
				else {
					
					// get flag
					isDirty = !poi.getType().equals(poiType) || 
							!poi.getPosition().equals(panel.getGotoPanel().getPositionField().getPosition());					
					
					// update poi
					m_tool.setCurrentPOI(poi);
					
					// add new poi
					m_tool.movePOIAt(point, poiType, null);
					
				}
				
				// resume mode
				m_tool.setWorkPoolMode(isWorkPoolMode);				
				
				// get flag
				boolean isIntelligence = (POIType.FINDING.equals(poi.getType()) || 
						POIType.SILENT_WITNESS.equals(poi.getType()));
				
				// update task?
				if(poi!=null && isIntelligence)
					// forward
					isDirty = isDirty || setTask(message,poi,TaskType.INTELLIGENCE,TaskSubType.FINDING,TaskPriority.HIGH);
				
				// is dirty?
				if(isDirty)
					MessageLogBottomPanel.setIsDirty();
				
				// set flag
				isSuccess = true;
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
		m_wp.getMsoModel().resumeClientUpdate();
		
		// reset flag
		setIsNotWorking();
		
		// return flag
		return isSuccess;
	}

	private boolean setTask(IMessageIf message, IPOIIf poi, TaskType type, TaskSubType subType, TaskPriority priority) {
		
		// initialize
		boolean isDirty = false;
		
		// Need to add/update task
		ITaskIf task = null;
		
		String match = m_wp.getBundleText("TaskSubType."+subType.toString()+".text");
		
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
			task = m_wp.getMsoManager().createTask(Calendar.getInstance());
			task.setCreated(Calendar.getInstance());
			task.setAlert(Calendar.getInstance());
			task.setPriority(priority);
			task.setResponsibleRole(null);
			task.setType(type);
			task.setSourceClass(message.getMsoClassCode());
			task.setCreatingWorkProcess(getName());
			task.setDescription(MsoUtils.getMessageText(message,Internationalization.getBundle(IDiskoWpMessageLog.class)));
			task.setDependentObject(message.getSender());
			message.addMessageTask(task);
			// set flag
			isDirty = true;
			
		}

		// get task text
		String text = String.format(match, DiskoEnumFactory.getText(poi.getType()));
		
		// any change?
		isDirty = isDirty || (text!=null && !text.equals(task.getTaskText()));
		
		// Update task text
		task.setTaskText(text);
		
		// return flag
		return isDirty;
		
	}
	
	/**
	 * Reverts contents of text fields to what is stored in MSO
	 */
	private void revertPOI()
	{
		// suspend update events
		m_wp.getMsoModel().suspendClientUpdate();
		
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
		m_wp.getMsoModel().resumeClientUpdate();
	}


	public void clearContents()
	{
		getPOIPanel().reset();
	}

	public void showComponent()
	{
		try {
			// do not show dialog in map
			m_tool.setShowDialog(false);
			// show poi in map
			IPOIIf poi = centerAtPOI(true);
			// update layout
			getPOIPanel().setTypes(m_types);
			// make it the active property panel
			m_tool.setPropertyPanel(getPOIPanel());
			// update tool attributes
			m_tool.setMsoDrawData(null, poi, MsoClassCode.CLASSCODE_POI);
			m_tool.setDrawMode(DrawMode.MODE_REPLACE);
			// activate tool
			m_wp.getMap().setActiveTool(m_tool, true);
			// show tool
			m_wp.getApplication().getNavBar().setVisibleButtons(
					Utils.getListOf(DiskoToolType.POI_TOOL), true, true);
			// show panel
			this.setVisible(true);
			// show map
			MessageLogPanel.showMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void hideComponent()
	{
		// hide tool
		m_wp.getApplication().getNavBar()
			.setVisibleButtons(Utils.getListOf(DiskoToolType.POI_TOOL), false, true);
		// hide num pad
		m_wp.getApplication().getUIFactory().getNumPadDialog().setVisible(false);
		// resume default tool property panel 
		m_tool.setDefaultPropertyPanel();
		// hide map
        MessageLogPanel.hideMap();
		// hide me
		this.setVisible(false);

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
		// consume?
		if(isWorking()) return;
		
		// Update dialog
		updatePOI(message);
	}

	/**
	 * Get the message log work process
	 */
	public IDiskoWpMessageLog getWP()
	{
		return m_wp;
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
		IDiskoMap map = m_wp.getMap();
		try
		{
			map.setActiveTool(m_tool,true);
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
                	IDiskoMap map = m_wp.getMap();
                	map.suspendNotify();
					map.setSelected(poi, isSelected);
					if(isSelected) 
						map.centerAtMsoObject(poi);
					map.refreshMsoLayers();
                	map.resumeNotify();
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
	
}
