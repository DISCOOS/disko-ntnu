package org.redcross.sar.wp.messageLog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.gui.panel.POIPanel;
import org.redcross.sar.gui.panel.POITypesPanel;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.POITool;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.tool.IDiskoTool.IDiskoToolState;
import org.redcross.sar.map.tool.IDrawTool.DrawMode;
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
public class MessagePOIPanel extends DefaultPanel implements IEditMessageComponentIf
{
	private final static long serialVersionUID = 1L;

	protected JPanel m_actionsPanel = null;
	protected DiskoIcon m_finishIcon = null;
	protected DiskoIcon m_cancelIcon = null;
	protected JButton m_finishButton = null;
	protected JButton m_centerAtButton = null;
	protected JButton m_cancelButton = null;
	protected POIPanel m_poiPanel = null;
	protected GotoPanel m_gotoPanel = null;
	protected POITypesPanel m_typesPanel = null;

	protected IDiskoWpMessageLog m_wp = null;
	
	protected POITool m_tool = null;
	protected POIType[] m_types = null;
	protected IDiskoToolState m_toolState = null;
	protected HashMap<String,IUnitIf> m_units = null;
	
	/**
	 * @param wp Message log work process
	 * @param poiTypes Which POI types are valid in panel
	 */
	public MessagePOIPanel(IDiskoWpMessageLog wp, POIType[] poiTypes)
	{
		// forward
		super("",false,false);
		
		// prepare
		m_wp = wp;
		m_types = poiTypes;
		m_tool = wp.getApplication().getNavBar().getPOITool();
		
		// initialize gui
		initialize();
	}

	private void initialize()
	{
		// hide header and borders
		setHeaderVisible(false);
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
		setBodyBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		// add components (BorderLayout is default)
		addBodyChild(getTypesPanel());
		addBodyChild(Box.createHorizontalStrut(5));
		addBodyChild(getGotoPanel());
		addBodyChild(Box.createHorizontalStrut(5));
		addBodyChild(getActionsPanel());
		
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
			m_finishIcon = new DiskoIcon(m_finishButton.getIcon(),Color.GREEN,0.4f);
			m_finishButton.setIcon(m_finishIcon);
			// add action listener
			m_finishButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					if(apply()) MessageLogBottomPanel.showListPanel();				
				}
			});

		}
		return m_finishButton;
	
	}
	
	private JButton getCenterAtButton() {
		if(m_centerAtButton==null) {
			// create button
			m_centerAtButton = DiskoButtonFactory.createButton("MAP.CENTERAT",ButtonSize.NORMAL);
			m_cancelIcon = new DiskoIcon(m_cancelButton.getIcon(),Color.RED,0.4f);
			m_cancelButton.setIcon(m_cancelIcon);
			// add action listener
			m_centerAtButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					centerAtPosition();
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
					revert();
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
			m_gotoPanel.setPreferredSize(new Dimension(275,115));
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
			m_typesPanel = getPOIPanel().getPOITypesPanel();
			// turn off vertical scrollbar
			m_typesPanel.setScrollBarPolicies(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// set preferred size of body component
			m_typesPanel.setPreferredSize(new Dimension(100,115));
			
		}
		return m_typesPanel;
	}
	
	/**
	 * apply POI position and type in current message based on values in GUI fields
	 */
	private boolean apply()
	{

		// consume?
		if(!isChangeable()) return false;
		
		// consume
		setChangeable(false);
		
		// initialize flag
		boolean isSuccess = false;
		
		// suspend update events
		m_wp.getMsoModel().suspendClientUpdate();
		
		// get panel
		POIPanel panel = getPOIPanel();
		
		// prevent changes in panel
		panel.setChangeable(false);
		
		// ensure that work is done synchroniously
		boolean isWorkPoolMode = m_tool.setWorkPoolMode(false);				

		// get current point
		Point point = panel.getPoint();
		
		try {
			
			// any change?
			if(point!=null && point.isEmpty()) {
				
				// initialize flag
				boolean isDirty = panel.isDirty();
				
				// get current message, do create if not exists
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);

				// Get message line, do not create if not exist
				IMessageLineIf messageLine = (message!=null ? message.findMessageLine(MessageLineType.POI, false) : null);

				// get poi
				IPOIIf poi = (messageLine!=null ? messageLine.getLinePOI() : null);
				
				// prepare to set poi
				panel.setPOI(poi);
				panel.setRemarks(null);
				
				// apply changes?
				if(panel.finish()) {
				
					// get added poi
					poi = m_tool.getPOI();
					
					// create message?
					if(message==null) {
						// create message and message line
						message = MessageLogBottomPanel.getCurrentMessage(true);	
						// get message line, create if not exists
						messageLine = message.findMessageLine(MessageLineType.POI, true);							
						// update line
						messageLine.setLinePOI(poi);														
					}
					// get flag
					boolean isIntelligence = 
						  (POIType.FINDING.equals(poi.getType()) 
						|| POIType.SILENT_WITNESS.equals(poi.getType()));
					
					// update task?
					if(poi!=null && isIntelligence)
						// forward
						isDirty = isDirty || schedule(message,poi,TaskType.INTELLIGENCE,TaskSubType.FINDING,TaskPriority.HIGH);
					
					// is dirty?
					if(isDirty)
						MessageLogBottomPanel.setIsDirty();
					
					// set flag
					isSuccess = true;					
				}									
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			

		// resume work pool mode
		m_tool.setWorkPoolMode(isWorkPoolMode);				
		
		// enable changes in panel
		panel.setChangeable(true);
		
		// resume update
		m_wp.getMsoModel().resumeClientUpdate();
		
		// resume changes
		setChangeable(true);
		
		// return flag
		return isSuccess;
	}

	private boolean schedule(IMessageIf message, IPOIIf poi, TaskType type, TaskSubType subType, TaskPriority priority) {
		
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
	private void revert()
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
				getPOIPanel().setPOI(poi);
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
			// save current state
			m_toolState = m_tool.save();
			// update panels
			getPOIPanel().setPOITypes(m_types);
			// make it the active property panel
			m_tool.setPropertyPanel(getPOIPanel());
			// prepare to draw
			m_tool.setMsoData(null, poi, MsoClassCode.CLASSCODE_POI);
			m_tool.setDrawMode(poi==null ? DrawMode.MODE_CREATE : DrawMode.MODE_REPLACE);
			// activate tool
			m_wp.getMap().setActiveTool(m_tool, 0);
			// show tool
			NavBarPanel bar = m_wp.getApplication().getNavBar();
			List<Enum<?>> types = Utils.getListOf(DiskoToolType.POI_TOOL);
			bar.setEnabledButtons(types, true, true);
			bar.setVisibleButtons(types, true, true);
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
		NavBarPanel bar = m_wp.getApplication().getNavBar();
		List<Enum<?>> types = Utils.getListOf(DiskoToolType.POI_TOOL);
		bar.setEnabledButtons(types, false, true);
		bar.setVisibleButtons(types, false, true);
		// hide num pad
		m_wp.getApplication().getUIFactory().getNumPadDialog().setVisible(false);
		// resume old tool state
		m_tool.load(m_toolState);
		// hide map
        MessageLogPanel.hideMap();
		// hide me
		this.setVisible(false);

    }

	private void update(IMessageIf message)
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
				getPOIPanel().setPOI(poi);
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
		if(!isChangeable()) return;
		
		// Update dialog
		update(message);
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
		try {
			map.setActiveTool(m_tool,0);
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
	
	/**
	 * center on position in map
	 */
	public boolean centerAtPosition()
	{
		
		try
		{
			// initialize
			IPoint p = getPOIPanel().getPoint();
			
			// can center at point?
			if(p!=null) {
				// forward
	        	m_wp.getMap().centerAt(p);
	        	// success
	        	return true;
			}
		}
		catch (AutomationException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		// return unit
		return false;
	}

	
	public void update() { 
		
		// update attributes
		m_finishIcon.setColored(isDirty());
		m_cancelIcon.setColored(isDirty());
		m_finishButton.repaint();
		m_cancelButton.repaint();
			
	}
}
