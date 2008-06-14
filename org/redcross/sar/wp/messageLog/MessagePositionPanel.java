package org.redcross.sar.wp.messageLog;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.gui.panel.PositionPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.tool.PositionTool;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.tool.IDiskoTool.IDiskoToolState;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.TimePos;

/**
 * Dialog used to update message position lines when editing the message log. 
 *
 * @author thomasl
 */
public class MessagePositionPanel extends BasePanel implements IEditMessageComponentIf
{
	private final static long serialVersionUID = 1L;

	protected JPanel m_actionsPanel = null;
	protected JButton m_finishButton = null;
	protected JButton m_cancelButton = null;
	protected JButton m_centerAtButton = null;
	protected PositionPanel m_positionPanel = null;
	protected GotoPanel m_gotoPanel = null;
	protected DefaultPanel m_unitsPanel = null;
	
	protected IDiskoWpMessageLog m_wp = null;
	
	protected PositionTool m_tool = null;
	protected IDiskoToolState m_toolState = null;

	protected Calendar logTimeStamp = null;
	
	/**
	 * @param wp Message log work process
	 * @param poiTypes Which POI types are valid in panel
	 */
	public MessagePositionPanel(IDiskoWpMessageLog wp)
	{
		// forward
		super();
		
		// prepare
		m_wp = wp;
		m_tool = wp.getApplication().getNavBar().getPositionTool();
		
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
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		// set layout
		setBodyLayout(new BoxLayout((JComponent)getBodyComponent(),BoxLayout.X_AXIS));
		
		// add empty border
		setBodyBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// add components (BorderLayout is default)
		addBodyChild(getUnitsPanel());
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
			m_actionsPanel.add(Box.createVerticalGlue());
		}
		return m_actionsPanel;
	
	}
	
	private JButton getFinishButton() {
		if(m_finishButton==null) {
			// create button
			m_finishButton = (JButton)getPositionPanel().getButton("finish");	
			
		}
		return m_finishButton;
	
	}
	
	private JButton getCenterAtButton() {
		if(m_centerAtButton==null) {
			// create button
			m_centerAtButton = (JButton)getPositionPanel().getButton("centerat");
		}
		return m_centerAtButton;
	
	}

	private JButton getCancelButton() {
		if(m_cancelButton==null) {
			// create button
			m_cancelButton = (JButton)getPositionPanel().getButton("cancel");
		}
		return m_cancelButton;
	
	}	
	
	/**
	 * This method initializes PositionPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	private PositionPanel getPositionPanel() {
		if(m_positionPanel==null) {

			// create a new PositionPanel and register it with the tool
			m_positionPanel = (PositionPanel)m_tool.addToolPanel();
			// forward work to this
			m_positionPanel.addDiskoWorkListener(this);
			
		}
		return m_positionPanel;
	
	}
	
	@Override
	public void onWorkPerformed(DiskoWorkEvent e) {
		
		super.onWorkPerformed(e);
		
		if(e.isCancel()) {
			// cancel any changes
			revertPosition();
			// return to list view
			MessageLogBottomPanel.showListPanel();
		}
		else if(e.isFinish()) {
			// forward
			if(apply()) MessageLogBottomPanel.showListPanel();										
		}

	}
	
	/**
	 * This method initializes GotoPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	private GotoPanel getGotoPanel() {
		if (m_gotoPanel == null) {
			// get from position panel
			m_gotoPanel = getPositionPanel().getGotoPanel();
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
	 * This method initializes UnitsPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	private DefaultPanel getUnitsPanel() {
		if (m_unitsPanel == null) {
			// get from position panel
			m_unitsPanel = getPositionPanel().getUnitsPanel();
			// turn off vertical scrollbar
			m_unitsPanel.setScrollBarPolicies(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// set preferred size of body component
			m_unitsPanel.setPreferredSize(new Dimension(200,115));
		}
		return m_unitsPanel;
	}	
	
	/**
	 * Apply position in current message based on values in GUI fields
	 */
	private boolean apply()
	{
		
		// consume?
		if(!isChangeable()) return false;

		// consume changes
		setChangeable(false);
		
		// initialize flag
		boolean isSuccess = false;

		// get panel
		PositionPanel panel = getPositionPanel();
		
		// get current unit			
		IUnitIf unit = panel.getUnit();
		
		// has unit?
		if(unit!=null) {
			
			// suspend update events
			m_wp.getMsoModel().suspendClientUpdate();
			
			try {
				
				// use panel instead of unit. updated position				
				// may not be the unit position. A track position
				// might be updated if passed logEntry was found 
				// in unit track
				Position p = panel.getPosition();
							
				// add or move poi?
				if(p!=null) {
					
					// get current message, create if not exist
					IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
					
					// Get message line, create if not exist
					IMessageLineIf messageLine = message.findMessageLine(MessageLineType.POSITION, true);

					// message line just created?
					if(messageLine.getLineUnit()==null) {

						// save
						messageLine.setLineUnit(unit);
						messageLine.setOperationTime(logTimeStamp);
						messageLine.setLinePosition(p);
											
					}
					else {
						// update position
						messageLine.setLinePosition(p);					
					}
					
					// notify
					MessageLogBottomPanel.setIsDirty();					

					// set flag
					isSuccess = true;
					
				}
				else {
					// notify
					Utils.showWarning("Ingen posisjon er oppgitt");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			// resume update events
			m_wp.getMsoModel().resumeClientUpdate();

		}
		else {
			Utils.showWarning("Du må først velge en enhet");
		}

		// resume change
		setChangeable(true);
		
		// return flag
		return isSuccess;
		
	}

	/*
	private boolean apply()
	{
		
		// consume?
		if(!isChangeable()) return false;

		// consume changes
		setChangeable(false);
		
		// initialize flag
		boolean isSuccess = false;

		// get panel
		PositionPanel panel = getPositionPanel();
		
		// get current unit			
		IUnitIf unit = panel.getUnit();
		
		// has unit?
		if(unit!=null) {
			
			// suspend update events
			m_wp.getMsoModel().suspendClientUpdate();
			
			try {
				
				// get point
				Position p = unit.getPosition();
							
				// add or move poi?
				if(p!=null) {
					
					// get current message, create if not exist
					IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
					
					// Get message line, create if not exist
					IMessageLineIf messageLine = message.findMessageLine(MessageLineType.POSITION, true);

					// get current track
					ITrackIf track = unit.getTrack();
					
					// has no track?
					if(track==null) {
						// get command post
						ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
						// create new track
						track = cmdPost.getTrackList().createTrack();
						// set track reference in unit
						unit.setTrack(track);
					}
					
					// create track?
					if(track.getGeodata() == null) {
						track.setGeodata(new Track(null, null, 1));
					}
					
					// update position
					unit.logPosition();
					
					// message line just created?
					if(messageLine.getLineUnit()==null) {

						// save
						messageLine.setLineUnit(unit);
						messageLine.setOperationTime(Calendar.getInstance());
						messageLine.setLinePosition(p);
											
					}
					else {
						// get current time position
						TimePos timePos = new TimePos(
								messageLine.getLinePosition(),
								messageLine.getOperationTime());
						// update position
						messageLine.setLinePosition(p);					
						// update last position in track?
						if(track!=null) {
							track.removeTrackPoint(timePos);
						}
					}
					
					// log current position
					unit.logPosition();
					
					// notify
					MessageLogBottomPanel.setIsDirty();					

					// set flag
					isSuccess = true;
					
				}
				else {
					// notify
					Utils.showWarning("Ingen posisjon er oppgitt");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			// resume update events
			m_wp.getMsoModel().resumeClientUpdate();

		}
		else {
			Utils.showWarning("Du må først velge en enhet");
		}

		// resume chante
		setChangeable(true);
		
		// return flag
		return isSuccess;
		
	}
	*/
	/**
	 * Reverts contents of text fields to what is stored in MSO
	 */
	private void revertPosition()
	{
		// get current message, do not create if not exist
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		
		// has message?
		if(message != null)
		{
			// get position line, do not create if not exist
			IMessageLineIf line = message.findMessageLine(MessageLineType.POSITION, false);

			// has line
			if(line != null)
			{
				getPositionPanel().setUnit(line.getLineUnit());
			}
		}
	}

	public void clearContents()
	{
		getPositionPanel().reset();
	}

	/**
	 * Show map if show in map button is selected
	 */
	public void showComponent()
	{
		// initialize
		boolean bFlag = false;
		
		// disable update
		setChangeable(false);	
		
		try {
			// initialize
			TimePos logEntry = null;
			
			// center map at position of current unit
			IUnitIf unit = centerAtUnit(true);
			
			// show tool
			setToolVisible(true);
			// prepare tool
			m_tool.setShowDialog(false);				// do not show tool dialog 
			m_tool.setWorkPoolMode(false);				// ensures that mso model is 
														// updated on this thread (in sync)
			m_tool.setToolPanel(getPositionPanel());	// ensures that this position panel 
														// is used to apply change to mso model
			m_tool.setShowDrawFrame(false);				// do not show draw frame			
			// update draw adapter
			m_tool.getDrawAdapter().setup(MsoClassCode.CLASSCODE_UNIT, null, unit, true);
			
			/* ==================================================================
			 * IMPORTANT: 
			 * 
			 * It it important that updates complies to the position logging 
			 * regime. Unit positions should only be logged if the units has
			 * truly moved from one position to another. If the position change
			 * is a correction (user update of wrong position input), thus a 
			 * change that do not reflect a move in the terrain, the old input
			 * should be updated: no new log action should be performed!
			 *  
			 * If the exists no MessagePositionLine, entered position should 
			 * update the unit position, and then be logged (added to track)
			 * 
			 * This is done by setting the following position tool attributes:
			 * 
			 * UPDATETRACKPOSITION:=null;
			 * LOGPOSITION:=true;
			 * 
			 * If there already exist a message with a MessagePositionLine, 
			 * THE NEW UNIT POSITION SHOULD NOT BE LOGGED, IT SHOULD BE UPDATED. 
			 * 
			 * Update in this context implies
			 * 
			 * 1. Update unit position if current unit position is the last 
			 *    logged
			 * 2. Update unit track log if current unit position exist in log, 
			 *    else, new position should be added to log.
			 * 
			 * This is done by setting the following position tool attributes:
			 * 
			 * UPDATETRACKPOSITION:=line.getLinePosition()
			 * LOGPOSITION:=false;
			 * 
			 * ================================================================== */
			
			// get current message, do not create if not exist
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
			// get position message line, do not create if not exist
			IMessageLineIf line = message!=null ? 
					message.findMessageLine(MessageLineType.POSITION, false) : null;
					
			// comply to logging regime
			if(line==null) {
				logTimeStamp = Calendar.getInstance();
				m_tool.setAttribute(logTimeStamp,"LOGTIMESTAMP");
				m_tool.setAttribute(null,"UPDATETRACKPOSITION");
				m_tool.setAttribute(true,"LOGPOSITION");
			}
			else {
				// get log time stamp
				logTimeStamp = line.getOperationTime();
				// re-create time position
				logEntry = new TimePos( line.getLinePosition(), logTimeStamp);
				System.out.println("logEntry:={"+MapUtil.getMGRSfromPosition(line.getLinePosition()) + "," + DTG.CalToDTG(logTimeStamp)+"}");
				// update
				m_tool.setAttribute(logTimeStamp,"LOGTIMESTAMP");
				m_tool.setAttribute(logEntry,"UPDATETRACKPOSITION");
				m_tool.setAttribute(false,"LOGPOSITION");
			}
					
			// prepare tool panel
			getPositionPanel().setLogEntry(logEntry);
			if(unit==null) {
				// load all
				getPositionPanel().loadUnits();
				// infer unit from message?
				if(message!=null && message.getSender() instanceof IUnitIf) {
					// cast sender to IUnitIf
					unit = (IUnitIf)message.getSender();
					// select unit
					getPositionPanel().setUnit(unit);
				}
			}
			else {
				// load single unit
				getPositionPanel().loadUnit(unit);
			}
			// success
			bFlag = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// enable update
		setChangeable(true);
		// success?
		if(bFlag) {
			// enable update
			getPositionPanel().update();
			// show this panel
			this.setVisible(true);
			// show map over log
			MessageLogPanel.showMap();			
		}
	}

	public void hideComponent()
	{
		// hide tool
		setToolVisible(false);
		// hide map
        MessageLogPanel.hideMap();
		// hide me
		this.setVisible(false);
    }

	private void setToolVisible(boolean isVisible) {
		NavBarPanel bar = m_wp.getApplication().getNavBar();
		List<Enum<?>> types = Utils.getListOf(DiskoToolType.POSITION_TOOL);
		bar.setVisibleButtons(types, isVisible, true);		
	}
	
	private void updatePosition(IMessageIf message)
	{
		
		// create or get current fining message line
		IMessageLineIf messageLine = message.findMessageLine(MessageLineType.POSITION, false);

		try
		{
			// Update components
			if(messageLine == null)
			{
				// Message don't have a POI message line
				getPositionPanel().reset();
			}
			else
			{
				// get current message line
				IUnitIf unit = messageLine.getLineUnit();
				// forward
				getPositionPanel().setUnit(unit);
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
		if(!isChangeable() || !isVisible()) return;
		
		// Update dialog
		updatePosition(message);
	}

	/**
	 * Get the message log work process
	 */
	public IDiskoWpMessageLog getWP()
	{
		return m_wp;
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
	 * center map at unit
	 */
	public IUnitIf centerAtUnit(boolean isSelected)
	{
		// initialize
		IUnitIf unit = null;
		
		// get current message, do not create if not exist
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		
		// has message?
		if(message != null)
		{
			
			// get position message line, do not create if not exist
			IMessageLineIf line = message.findMessageLine(MessageLineType.POSITION, false);
			
			// has line?
			if(line != null)
			{
				// get unit
				unit = line.getLineUnit();
			}
				
			// Select unit object in map?
			if(unit != null)
			{
				try
				{
                	IDiskoMap map = m_wp.getMap();
                	map.suspendNotify();
					map.setSelected(unit, isSelected);
					if(isSelected)
						map.centerAtMsoObject(unit);
					map.refreshMsoLayers();
                	map.consumeNotify();
				}
				catch (AutomationException ex)
				{
					ex.printStackTrace();
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		}
		// finished
		return unit;
	}	
}
