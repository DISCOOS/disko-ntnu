package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.gui.attribute.AbstractDiskoAttribute;
import org.redcross.sar.gui.attribute.DTGAttribute;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.mso.panel.PositionPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.gui.panel.HeaderPanel;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.PositionTool;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.tool.IDiskoTool.IDiskoToolState;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.thread.event.DiskoWorkEvent;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.TimePos;

/**
 * Dialog used to update message position lines when editing the message log. 
 *
 * @author thomasl
 */
public class MessagePositionPanel extends BasePanel implements IEditorIf
{
	private final static long serialVersionUID = 1L;

	protected JPanel m_actionsPanel;
	protected JButton m_finishButton;
	protected JButton m_cancelButton;
	protected JButton m_centerAtButton;
	protected PositionPanel m_positionPanel;
	protected GotoPanel m_gotoPanel;
	protected HeaderPanel m_optionsPanel;	
	protected DTGAttribute m_dtgAttr;	
	protected DefaultPanel m_unitsPanel;
	
	protected IDiskoWpMessageLog m_wp;
	
	protected PositionTool m_tool;
	protected IDiskoToolState m_toolState;

	//protected Calendar logTimeStamp;
	
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
		// prepare
		setHeaderVisible(false);
		setBorderVisible(false);
		setBodyBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setPreferredSize(new Dimension(400,115));
		setPreferredBodySize(new Dimension(400,115));
		
		// hide me
		setVisible(false);

		// hide map
        MessageLogPanel.hideMap();		
        
		// no scrollbars
		setNotScrollBars();
		
		// create layout
		JPanel inner = new JPanel();
		inner.setLayout(new BoxLayout(inner,BoxLayout.X_AXIS));
		inner.add(getUnitsPanel());
		inner.add(Box.createHorizontalStrut(5));
		inner.add(getGotoPanel());
		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer,BoxLayout.Y_AXIS));
		outer.add(getOptionsPanel());
		outer.add(Box.createVerticalStrut(5));
		outer.add(inner);
		setBodyLayout(new BorderLayout(5,5));
		addBodyChild(outer,BorderLayout.CENTER);
		addBodyChild(getActionsPanel(),BorderLayout.EAST);		
		
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
			// hide map
	        MessageLogPanel.hideMap();
			// return to list view
			MessageLogBottomPanel.showListPanel();
		}
		else if(e.isFinish()) {
			// forward
			if(apply()) {
				// hide map
		        MessageLogPanel.hideMap();
		        // show message line list
				MessageLogBottomPanel.showListPanel();										
			}
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
			m_gotoPanel.setNotScrollBars();
			// set preferred size of body component
			Utils.setFixedWidth(m_gotoPanel,275);
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
			m_unitsPanel.setPreferredSize(new Dimension(100,115));
		}
		return m_unitsPanel;
	}	
	
	/**
	 * This method initializes DTG attribute	
	 * 	
	 * @return javax.swing.JPanel
	 */
	private DTGAttribute getDTGAttr() {
		if (m_dtgAttr == null) {
			// get from position panel
			m_dtgAttr = (DTGAttribute)getPositionPanel().getOptionsPanel().getAttribute("DTG");
			m_dtgAttr.setCaptionText("Når");
			m_dtgAttr.setFixedCaptionWidth(80);
			
		}
		return m_dtgAttr;
	}
	
	private HeaderPanel getOptionsPanel() {
		if (m_optionsPanel == null) {
			// create header panel
			m_optionsPanel = new HeaderPanel("",ButtonSize.SMALL,SwingConstants.LEFT);
			// get name attribute
			AbstractDiskoAttribute attr = getDTGAttr();
			// prepare layout
			attr.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			attr.setCaptionColor(Color.WHITE, Color.LIGHT_GRAY);
			// get from position panel
			m_optionsPanel.addItem(attr);
			// set preferred size of body component
			Utils.setFixedHeight(m_optionsPanel, 35);
			
		}
		return m_optionsPanel;
	}
	
	@Override
	public boolean cancel() {
		return getPositionPanel().cancel();
	}
	
	@Override
	public boolean finish() {
		return getPositionPanel().finish();
	}
	
	@Override
	public void reset() {
		getPositionPanel().reset();
	}
	
	/**
	 * Apply changes to message line
	 */
	private boolean apply()
	{		

		/* ======================================================================
		 * Apply changes to a new or existing message position line
		 * 
		 * IMPORTANT: Position and time stamp is updated by 
		 * PositionPanel().finish(). Because PositionTool() is not in work 
		 * pool mode, the result is available at the invocation of this method.
		 * ====================================================================== */
		
		// consume?
		if(!isChangeable()) return false;

		// consume changes
		setChangeable(false);
		
		// initialize flag
		boolean isSuccess = false;

		// get panel
		PositionPanel panel = getPositionPanel();
		
		// suspend update events
		m_wp.getMsoModel().suspendClientUpdate();
		
		// get current message, create if not exist
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
		
		// Get message line, create if not exist
		IMessageLineIf line = message.findMessageLine(MessageLineType.POSITION, true);
		
		// message line just created?
		if(line.getLineUnit()==null) {

			// save unit
			line.setLineUnit(panel.getUnit());			
								
		}
		
		// update position
		line.setOperationTime((Calendar)getDTGAttr().getValue());
		line.setLinePosition(panel.getPosition());
		
		// update log entry
		panel.setLogEntry(new TimePos( line.getLinePosition(), line.getOperationTime()));		
		
		// notify
		MessageLogBottomPanel.setIsDirty();					

		// set flag
		isSuccess = true;
		
		// resume update events
		m_wp.getMsoModel().resumeClientUpdate();

		// resume change
		setChangeable(true);
		
		// return flag
		return isSuccess;
		
	}

	public void resetEditor()
	{
		getPositionPanel().reset();
	}

	/**
	 * Show map if show in map button is selected
	 */
	public void showEditor()
	{
		// initialize
		boolean bFlag = false;
		
		// disable update
		setChangeable(false);	
		
		try {
			
			// center map at position of current unit
			IUnitIf unit = centerAtUnit(true);
			
			// get current tool state
			m_toolState = m_tool.save();
			
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
			m_tool.getDrawAdapter().setup(MsoClassCode.CLASSCODE_UNIT, unit, true);
			
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
			
			// forward
			update(message);
			
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

	public void hideEditor()
	{
		// hide tool
		setToolVisible(false);
		// resume old tool state
		m_tool.load(m_toolState);
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
	
	private void update(IMessageIf message)
	{
		
		// get position message line, do not create if not exist
		IMessageLineIf line = message!=null ? 
				message.findMessageLine(MessageLineType.POSITION, false) : null;
				
		try
		{

			// comply to logging regime
			if(line==null) {
				// Message don't have a POI message line
				getPositionPanel().reset();				
				// reset log entry and initialize log time stamp
				getPositionPanel().setLogEntry(null);
				getDTGAttr().load();
			}
			else {
				// get log time stamp
				Calendar time = line.getOperationTime();
				// set log time stamp
				getPositionPanel().setLogEntry(new TimePos( line.getLinePosition(), time));
				// get unit
				IUnitIf unit = line.getLineUnit();
				// try to auto-select unit?
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
			}
			
			

		}
		catch(Exception e){}
	}


	/**
	 * Update position fields with message POI position. Zoom to POI
	 */
	public void setMessage(IMessageIf message)
	{
		// consume?
		if(!isChangeable() || !isVisible()) return;
		
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
	 * Set the tool for the current work process map
	 */
	public void setMapTool()
	{
		IDiskoMap map = m_wp.getMap();
		if(map.getActiveTool()!=m_tool) {
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
