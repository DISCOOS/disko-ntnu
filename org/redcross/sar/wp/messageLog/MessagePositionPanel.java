package org.redcross.sar.wp.messageLog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.gui.panel.PositionPanel;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.PositionTool;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.tool.IDiskoTool.IDiskoToolState;
import org.redcross.sar.map.tool.IDrawTool.DrawMode;
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
public class MessagePositionPanel extends DefaultPanel implements IEditMessageComponentIf
{
	private final static long serialVersionUID = 1L;

	protected JPanel m_actionsPanel = null;
	protected DiskoIcon m_finishIcon = null;
	protected DiskoIcon m_cancelIcon = null;
	protected JButton m_finishButton = null;
	protected JButton m_cancelButton = null;
	protected JButton m_centerAtButton = null;
	protected PositionPanel m_positionPanel = null;
	protected GotoPanel m_gotoPanel = null;
	protected DefaultPanel m_unitsPanel = null;
	
	protected IDiskoWpMessageLog m_wp = null;
	
	protected PositionTool m_tool = null;
	protected IDiskoToolState m_toolState = null;

	/**
	 * @param wp Message log work process
	 * @param poiTypes Which POI types are valid in panel
	 */
	public MessagePositionPanel(IDiskoWpMessageLog wp)
	{
		// forward
		super("",false,false);
		
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
		}
		return m_actionsPanel;
	
	}
	
	private JButton getFinishButton() {
		if(m_finishButton==null) {
			// create button
			m_finishButton = DiskoButtonFactory.createButton("GENERAL.FINISH",ButtonSize.NORMAL);			
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
			// add action listener
			m_centerAtButton.addActionListener(new ActionListener() {
				/**
				 * Center map at position
				 */
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
			m_cancelIcon = new DiskoIcon(m_cancelButton.getIcon(),Color.RED,0.4f);
			m_cancelButton.setIcon(m_cancelIcon);
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
				
				// get point
				Position p = panel.getPosition();
							
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
						m_tool.setUnit(unit);				
						
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

		// resume chante
		setChangeable(true);
		
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
				getPositionPanel().setUnit(unit);
				getPositionPanel().setPosition(p);
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
			// center map at position of current unit
			IUnitIf unit = centerAtUnit(true);			
			// load all units?
			if(unit==null) 
				getPositionPanel().loadUnits();
			else
				getPositionPanel().loadUnit(unit);
			// save current state
			m_toolState = m_tool.save();
			// set it the active property panel
			m_tool.setPropertyPanel(getPositionPanel());
			// prepare to draw
			m_tool.setShowDialog(false);
			m_tool.setShowDrawFrame(false);
			m_tool.setMsoData(null, unit, MsoClassCode.CLASSCODE_UNIT);
			m_tool.setDrawMode(unit==null ? DrawMode.MODE_CREATE : DrawMode.MODE_REPLACE);
			// set tool active
			m_wp.getMap().setActiveTool(m_tool,0);
			// show tool
			NavBarPanel bar = m_wp.getApplication().getNavBar();
			List<Enum<?>> types = Utils.getListOf(DiskoToolType.POSITION_TOOL);
			bar.setEnabledButtons(types, true, true);
			bar.setVisibleButtons(types, true, true);
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
		NavBarPanel bar = m_wp.getApplication().getNavBar();
		List<Enum<?>> types = Utils.getListOf(DiskoToolType.POSITION_TOOL);
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
		if(!isChangeable()) return;
		
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
	 * center on position in map
	 */
	public boolean centerAtPosition()
	{
		
		try
		{
			// initialize
			IPoint p = getPositionPanel().getPoint();
			
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
	
	public void update() { 
		
		// update attributes
		m_finishIcon.setColored(isDirty());
		m_cancelIcon.setColored(isDirty());
		m_finishButton.repaint();
		m_cancelButton.repaint();
			
	}
	
}
