package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Calendar;
import java.util.EnumSet;

import javax.swing.BorderFactory;

import org.redcross.sar.gui.attribute.DTGAttribute;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.AttributesPanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.NumPadPanel;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.thread.event.DiskoWorkEvent;
import org.redcross.sar.thread.event.IDiskoWorkListener;
import org.redcross.sar.util.Utils;

/**
 * Creates the dialog for changing DTG in message log edit mode.
 * 
 * @author thomasl
 */
public class ChangeDTGDialog extends DefaultDialog implements IEditorIf
{
	private static final long serialVersionUID = 1L;

	private boolean m_isTabletMode = true;

	private DefaultPanel m_contentPanel;
	private AttributesPanel m_attributesPanel;
	private NumPadPanel m_numPadPanel;
	private DTGAttribute m_createdAttr;
	private DTGAttribute m_timeAttr;
	
	private IDiskoWpMessageLog m_wp;

	/* ==========================================================
	 * Constructors
	 * ==========================================================*/
	
	public ChangeDTGDialog(IDiskoWpMessageLog wp)
	{
		// forward
		super();
		
		// prepare
		m_wp = wp;

		// initialize GUI
		initialize();
		
	}

	/* ==========================================================
	 * Public methods
	 * ==========================================================*/

	/**
	 * Get time stamp
	 * 
	 * @return Calendar time stamp
	 */
	public Calendar getTime()
	{
		return m_timeAttr.getValue();
	}

	/**
	 * Sets the created text field
	 * @param created
	 */
	public void setCreated(Calendar created)
	{
		getCreatedAttr().setValue(created);
	}

	/**
	 * Sets the time text field
	 * @param calendar
	 */
	public void setTime(Calendar time)
	{
		time = (time==null) ? Calendar.getInstance() : time;
		getTimeAttr().setValue(time);
	}
	
	/* ==========================================================
	 * Overridden methods
	 * ==========================================================*/
	
	@Override
	public boolean isFocusable()
	{
		return true;
	}

	/* ==========================================================
	 * IEditorIf implementation
	 * ==========================================================*/

	/**
	 * Sets the DTG field, get it from the message
	 */
	public void setMessage(IMessageIf message)
	{

		// forward 
		getContentPanel().setMsoObject(message);
		
	}

	/**
	 * Displays a numpad if in tablet mode
	 */
	public void showEditor()
	{
		
		// show input?
		getNumPadPanel().setVisible(m_isTabletMode);
		
		// fit to current content
		this.pack();
		
		// show me
		this.setVisible(true);
		
		// request focus on input field
		getTimeAttr().getTextField().requestFocusInWindow();

		
	}

	public void hideEditor()
	{
		this.setVisible(false);
	}

	/**
	 * Reset created and time fields
	 */
	public void resetEditor()
	{
		Calendar time = Calendar.getInstance();
		setCreated(time);
		setTime(time);
	}
	
	/* ==========================================================
	 * Helper methods
	 * ==========================================================*/
	
	private void initialize()
	{
		// prepare
        this.setContentPane(getContentPanel());
        this.setMoveable(false);
		this.pack();

	}

	private DefaultPanel getContentPanel()
	{
		if(m_contentPanel==null) {
			
			// extend default panel
			m_contentPanel = new DefaultPanel("<b>Endre DTG for melding</b>",true,true,ButtonSize.NORMAL) {
				
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean beforeFinish() {
					
					// allowed?
					if(getTime()!=null) return true;
					
					// notify
					Utils.showWarning(m_wp.getBundleText("InvalidDTG.header"), 
							m_wp.getBundleText("InvalidDTG.details"));
					
					// not allowed
					return false;
					
				}
							
				@Override
				public void setMsoObject(IMsoObjectIf msoObj) {
					
					// forward
					super.setMsoObject(msoObj);
					
					// has message?
					if(msoObj instanceof IMessageIf) {
						// cast to IMessageIf
						IMessageIf message = (IMessageIf)msoObj;
				    	// forward
						setCreated(message.getCreated());
						setTime(message.getTimeStamp());
					}
					else {
						resetEditor();
					}					
					
				}

				@Override
				protected void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
					setMessage((IMessageIf)msoObj);
				}

				@Override
				protected void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
					resetEditor();
				}		
				
				@Override
				protected void msoObjectClearAll(IMsoObjectIf msoObj, int mask) {
					resetEditor();
				}

				
			};
			m_contentPanel.setRequestHideOnCancel(true);
			m_contentPanel.setRequestHideOnFinish(true);
			m_contentPanel.setBodyLayout(new BorderLayout(5,5));
			m_contentPanel.setBodyBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			m_contentPanel.addBodyChild(getAttributesPanel(),BorderLayout.CENTER);
			m_contentPanel.addBodyChild(getNumPadPanel(),BorderLayout.EAST);
			m_contentPanel.addDiskoWorkListener(new IDiskoWorkListener() {

				@Override
				public void onWorkPerformed(DiskoWorkEvent e) {
					
					// forward?
					if(e.isFinish()) 
						change();
					
				}
				
			});
			m_contentPanel.setInterests(m_wp.getMsoModel(), EnumSet.of(MsoClassCode.CLASSCODE_MESSAGE));
			
		}
		return m_contentPanel;
	}
	
	private AttributesPanel getAttributesPanel()
	{
		if (m_attributesPanel == null) {
			// initialize
			m_attributesPanel = new AttributesPanel("Dato-Tid-Gruppe","Ingen egenskaper definert",false,false);
			m_attributesPanel.setHeaderVisible(false);
			m_attributesPanel.setPreferredSize(new Dimension(180,250));
			m_attributesPanel.setNotScrollBars();			
			// add attributes
			m_attributesPanel.addAttribute(getCreatedAttr());
			m_attributesPanel.addAttribute(getTimeAttr());
			// add listeners
			m_attributesPanel.addDiskoWorkListener(getContentPanel());			
			// initialize contents
			resetEditor();			
		}
		return m_attributesPanel;
	}

	private DTGAttribute getCreatedAttr() {
		if(m_createdAttr==null) {
			m_createdAttr = new DTGAttribute("created",m_wp.getBundleText("ChangeDTGDialogCreated.text"),false);
		}
		return m_createdAttr;
	}
	
	private DTGAttribute getTimeAttr() {
		if(m_timeAttr==null) {
			m_timeAttr = new DTGAttribute("time",m_wp.getBundleText("ChangeDTGDialogTime.text"),true);
			Utils.setFixedHeight(m_timeAttr, 35);			
		}
		return m_timeAttr;
	}
		
	private NumPadPanel getNumPadPanel() {
		if(m_numPadPanel==null) {
			m_numPadPanel = new NumPadPanel("Tastatur",false,false);
			m_numPadPanel.setHeaderVisible(false);
			m_numPadPanel.setInputVisible(false);
			m_numPadPanel.setInputField(getTimeAttr().getTextField(), false);
			m_numPadPanel.addDiskoWorkListener(getContentPanel());
		}
		return m_numPadPanel;
	}
	
	private void change()
	{
		// get current message, create new if not exists
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
		// update time stamp
		message.setTimeStamp(getTime());
	}	

}
