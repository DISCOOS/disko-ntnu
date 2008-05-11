package org.redcross.sar.wp.messageLog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.GotoPanel;
import org.redcross.sar.gui.map.PositionPanel;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.PositionTool;
import org.redcross.sar.map.command.IDrawTool.DrawMode;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

/**
 * Dialog used to update message position lines when editing the message log. 
 *
 * @author thomasl
 */
public class MessagePositionPanel extends DiskoPanel implements IEditMessageComponentIf
{
	private final static long serialVersionUID = 1L;

	protected JPanel m_actionsPanel = null;
	protected JButton m_finishButton = null;
	protected JButton m_centerAtButton = null;
	protected JButton m_cancelButton = null;
	protected PositionPanel m_positionPanel = null;
	protected GotoPanel m_gotoPanel = null;
	protected DiskoPanel m_unitsPanel = null;
	
	protected IDiskoWpMessageLog m_wp = null;
	
	protected PositionTool m_tool = null;

	private int isWorking = 0;
	
	/**
	 * @param wp Message log work process
	 * @param poiTypes Which POI types are valid in panel
	 */
	public MessagePositionPanel(IDiskoWpMessageLog wp)
	{
		// prepare
		m_wp = wp;
		m_tool = wp.getApplication().getNavBar().getPositionTool();
		
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
		addBodyComponent(getUnitsPanel());
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
					if(applyPosition())
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
					centerAtPosition(true);
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
					revertPosition();
					// return to list view
					MessageLogBottomPanel.showListPanel();
				}
			});
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

			// create a poi panel and register it with a tool
			m_positionPanel = (PositionPanel)m_tool.addPropertyPanel();
			
		}
		return m_positionPanel;
	
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
			m_gotoPanel.setPreferredSize(new Dimension(275,125));
		}
		return m_gotoPanel;
	}
	
	/**
	 * This method initializes UnitsPanel	
	 * 	
	 * @return javax.swing.JPanel
	 */
	private DiskoPanel getUnitsPanel() {
		if (m_unitsPanel == null) {
			// get from position panel
			m_unitsPanel = getPositionPanel().getUnitsPanel();
			// turn off vertical scrollbar
			m_unitsPanel.setScrollBarPolicies(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			// set preferred size of body component
			m_unitsPanel.setPreferredSize(new Dimension(200,125));
		}
		return m_unitsPanel;
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
	 * Apply position in current message based on values in GUI fields
	 */
	private boolean applyPosition()
	{
		
		// set flag
		setIsWorking();

		// initialize flag
		boolean isSuccess = false;

		// get panel
		PositionPanel panel = getPositionPanel();
		
		// get current unit			
		IUnitIf unit = panel.getCurrentUnit();
		
		// has unit?
		if(unit!=null) {
			
			// suspend update events
			m_wp.getMsoModel().suspendClientUpdate();
			
			try {
				
				// get point
				Position p = panel.getGotoPanel().getPositionField().getPosition();
							
				// add or move poi?
				if(p!=null) {
					
					// get dirty flag
					boolean isDirty = !unit.getPosition().equals(p);					
					
					// is dirty?
					if(isDirty) {
					
						// get flag
						boolean isWorkPoolMode = m_tool.isWorkPoolMode();
						
						// reset 
						m_tool.setWorkPoolMode(false);				
						
						// update tool
						m_tool.setCurrentUnit(unit);				
						
						// get current message, create if not exist
						IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
						
						// Get message line, create if not exist
						IMessageLineIf messageLine = message.findMessageLine(MessageLineType.POSITION, true);
	
						// get current track
						ITrackIf track = unit.getTrack();
						
						// has no track?
						if(track==null) {
							track = m_wp.getMsoModel().getMsoManager().createTrack();
							unit.setTrack(track);
						}
						
						// create track?
						if(track.getGeodata() == null) {
							track.setGeodata(new Track(null, null, 1));
						}
						
						// update position
						unit.setPosition(p);
						
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
								// remove position
								track.removeTrackPoint(timePos);
							}
						}
						
						// log current position
						unit.logPosition();
						
						// resume mode
						m_tool.setWorkPoolMode(isWorkPoolMode);
						
						// notify
						MessageLogBottomPanel.setIsDirty();					
						
					}

					// set flag
					isSuccess = true;
					
				}
				else {
					// notify
					Utils.showWarning("Ingen posisjon er oppgitt");
				}
			} catch (Exception ex) {
				// notify
				Utils.showWarning("Ugyldig koordinat format");
			}
			
			// resume update events
			m_wp.getMsoModel().resumeClientUpdate();

		}
		else {
			Utils.showWarning("Du må først velge en enhet");
		}

		// reset flag
		setIsNotWorking();
		
		// return flag
		return isSuccess;
		
	}
	
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
				IUnitIf unit = line.getLineUnit();
				Position p = line.getLinePosition();
				getPositionPanel().setCurrentUnit(unit);
				getPositionPanel().setCurrentPosition(p);
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
		try {
			// do not show dialog in map
			m_tool.setShowDialog(false);
			// center map at position of current unit
			IUnitIf unit = centerAtPosition(true);			
			// load all units?
			if(unit==null) 
				getPositionPanel().loadUnits();
			else
				getPositionPanel().loadSingleUnitOnly(unit);
			// set it the active property panel
			m_tool.setPropertyPanel(getPositionPanel());
			// update tool attributes
			m_tool.setMsoDrawData(null, unit, MsoClassCode.CLASSCODE_UNIT);
			m_tool.setDrawMode(DrawMode.MODE_REPLACE);
			// set tool active
			m_wp.getMap().setActiveTool(m_tool,true);
			// show tool
			m_wp.getApplication().getNavBar()
				.setVisibleButtons(Utils.getListOf(DiskoToolType.POSITION_TOOL), true, true);
			// show this panel
			this.setVisible(true);
			// show map over log
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
			.setVisibleButtons(Utils.getListOf(DiskoToolType.POSITION_TOOL), false, true);
		// hide num pad
		m_wp.getApplication().getUIFactory().getNumPadDialog().setVisible(false);
		// resume default tool property panel 
		m_tool.setDefaultPropertyPanel();
		// hide map
        MessageLogPanel.hideMap();
		// hide me
		this.setVisible(false);
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
				getPositionPanel().setCurrentUnit(unit);
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
	 * center on position in map
	 */
	public IUnitIf centerAtPosition(boolean isSelected)
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
                	map.resumeNotify();
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
		// return unit
		return unit;
	}
	
}
