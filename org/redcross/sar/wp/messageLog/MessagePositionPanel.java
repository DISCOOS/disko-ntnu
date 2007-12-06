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
import org.redcross.sar.gui.PositionPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.PositionTool;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IPOIIf;
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
public class MessagePositionPanel extends JPanel implements IEditMessageComponentIf
{
	private final static long serialVersionUID = 1L;

	protected JButton m_okButton = null;
	protected JButton m_centerAtButton = null;
	protected JButton m_cancelButton = null;
	protected IDiskoWpMessageLog m_wpMessageLog = null;
	protected PositionTool m_tool = null;
	protected JPanel m_buttonPanel = null;
	protected PositionPanel m_positionPanel = null;
	protected HashMap<String,IUnitIf> m_units = null;

	/**
	 * @param wp Message log work process
	 * @param poiTypes Which POI types are valid in panel
	 */
	public MessagePositionPanel(IDiskoWpMessageLog wp)
	{
		// prepare
		m_wpMessageLog = wp;
		m_tool = m_wpMessageLog.getApplication().getNavBar().getPositionTool();
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
		this.add(getPositionPanel(),gbc);
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
				revertPosition();
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
			 * Center at current poi if exists
			 */
			public void actionPerformed(ActionEvent e)
			{
				NumPadDialog numPad = m_wpMessageLog.getApplication().getUIFactory().getNumPadDialog();
				numPad.setVisible(false);
				centerAtPosition(true);
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
				applyPosition();
				MessageLogBottomPanel.showListPanel();
			}
		});
		this.add(m_okButton, gbc);

	}

	/**
	 * Apply position in current message based on values in GUI fields
	 */
	private void applyPosition()
	{
		// get panel
		PositionPanel panel = getPositionPanel();
		
		// get current unit			
		IUnitIf unit = panel.getCurrentUnit();
		
		// has unit?
		if(unit!=null) {
			
			// suspend update events
			m_wpMessageLog.getMsoModel().suspendClientUpdate();
			
			try {
				
				// get point
				Position p = panel.getPOIField().getPosition();
							
				// add or move poi?
				if(p!=null) {
					
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
						track = m_wpMessageLog.getMsoModel().getMsoManager().createTrack();
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
			m_wpMessageLog.getMsoModel().resumeClientUpdate();

		}
		else {
			Utils.showWarning("Du må først velge en enhet");
		}
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
		// do not show dialog in map
		m_tool.setShowDialog(false);
		// show map over log
		MessageLogPanel.showMap();
		// center map at position of current unit
		IUnitIf unit = centerAtPosition(true);
		// get draw tool dialog
		DrawDialog dialog = (DrawDialog)m_tool.getDialog();
		// get position panel 
		PositionPanel panel = getPositionPanel();
		// get attributes
		Object[] attributes = {true,unit};
		// use horizontal layout
		panel.setVertical(false);	
		panel.setVisible(true);
		panel.setButtonsVisible(false);
		panel.setUnitsVisible(true);
		// load all units?
		if(unit==null) 
			panel.loadUnits();
		else
			panel.loadSingleUnitOnly(unit);

		// make it the active property panel
		m_tool.setPropertyPanel(panel);
		// set tool active
		dialog.setActiveTool(m_tool);
		dialog.setToolSet(MsoClassCode.CLASSCODE_UNIT, attributes);
		// show tool
		m_wpMessageLog.getApplication().getNavBar().setVisibleButtons(
				EnumSet.of(DiskoToolType.POSITION_TOOL), true, true);
		// buffer changes. use m_tool.apply() to update the mso model
		m_tool.setBuffered(true);
		// show this panel
		this.setVisible(true);
	}

	public void hideComponent()
	{
		// hide tool
		m_wpMessageLog.getApplication().getNavBar().setVisibleButtons(
				EnumSet.of(DiskoToolType.POSITION_TOOL), false, true);
		// hide num pad
		NumPadDialog numPad = m_wpMessageLog.getApplication().getUIFactory().getNumPadDialog();
		numPad.setVisible(false);
		// hide me
		this.setVisible(false);
		// hide map
        MessageLogPanel.hideMap();
        // deselect
        centerAtPosition(false);
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
		// Update dialog
		updatePosition(message);
	}

	/**
	 * Get the message log work process
	 */
	public IDiskoWpMessageLog getWP()
	{
		return m_wpMessageLog;
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
                	IDiskoMap map = m_wpMessageLog.getMap();
					map.setSelected(unit, isSelected);
					if(isSelected)
						map.centerOnMsoObject(unit);
					map.refreshSelection(unit, map.getSelectionExtent());
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
		// return unit
		return unit;
	}
	
	private PositionPanel getPositionPanel() {
		if(m_positionPanel==null) {

			// create panel
			m_positionPanel = (PositionPanel)m_tool.addPropertyPanel();
			m_positionPanel.setPreferredSize(new Dimension(600,200));
			// set horizontal flow
			m_positionPanel.setVertical(true);
			
		}
		return m_positionPanel;
	
	}		
}
