package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.field.AbstractField;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.menu.NavMenu;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.gui.panel.HeaderPanel;
import org.redcross.sar.gui.panel.POIPanel;
import org.redcross.sar.gui.panel.POITypesPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.POITool;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.map.tool.IMapTool.IMapToolState;
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
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.FlowEvent;
import org.redcross.sar.wp.messageLog.ChangeTasksDialog.TaskSubType;

/**
 * Dialog used to update message poi lines when editing the message log.
 *
 * @author thomasl
 */
public class MessagePOIPanel extends BasePanel implements IEditorIf
{
	private final static long serialVersionUID = 1L;

	protected JPanel m_actionsPanel;
	protected JButton m_finishButton;
	protected JButton m_centerAtButton;
	protected JButton m_cancelButton;
	protected POIPanel m_poiPanel;
	protected GotoPanel m_gotoPanel;
	protected HeaderPanel m_optionsPanel;
	protected POITypesPanel m_typesPanel;
	protected TextField m_nameAttr;

	protected IDiskoWpMessageLog m_wp;

	protected POITool m_tool;
	protected POIType[] m_types;
	protected IMapToolState m_toolState;
	protected HashMap<String,IUnitIf> m_units;

	/**
	 * @param wp Message log work process
	 * @param poiTypes Which POI types are valid in panel
	 */
	public MessagePOIPanel(IDiskoWpMessageLog wp, POIType[] poiTypes)
	{
		// forward
		super(ButtonSize.SMALL);

		// prepare
		m_wp = wp;
		m_types = poiTypes;
		m_tool = wp.getApplication().getNavMenu().getPOITool();

		// initialize gui
		initialize();
	}

	private void initialize()
	{
		// prepare
		setHeaderVisible(false);
		setBorderVisible(false);
		setContainerBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setPreferredSize(new Dimension(400,115));
		setPreferredContainerSize(new Dimension(400,115));

		// hide me
		setVisible(false);

		// hide map
        MessageLogPanel.hideMap();

		// no scrollbars
		setNotScrollBars();

		// create layout
		JPanel inner = new JPanel();
		inner.setLayout(new BoxLayout(inner,BoxLayout.X_AXIS));
		inner.add(getTypesPanel());
		inner.add(Box.createHorizontalStrut(5));
		inner.add(getGotoPanel());
		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer,BoxLayout.Y_AXIS));
		outer.add(getOptionsPanel());
		outer.add(Box.createVerticalStrut(5));
		outer.add(inner);
		setContainerLayout(new BorderLayout(5,5));
		addToContainer(outer,BorderLayout.CENTER);
		addToContainer(getActionsPanel(),BorderLayout.EAST);

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
			m_finishButton = (JButton)getPOIPanel().getButton("finish");

		}
		return m_finishButton;

	}

	private JButton getCenterAtButton() {
		if(m_centerAtButton==null) {
			// create button
			m_centerAtButton = (JButton)getPOIPanel().getButton("centerat");
		}
		return m_centerAtButton;

	}

	private JButton getCancelButton() {
		if(m_cancelButton==null) {
			// create button
			m_cancelButton = (JButton)getPOIPanel().getButton("cancel");
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
			m_poiPanel = (POIPanel)m_tool.addToolPanel();

			// forward work to this
			m_poiPanel.addFlowListener(this);


		}
		return m_poiPanel;
	}

	@Override
	public void onFlowPerformed(FlowEvent e) {

		super.onFlowPerformed(e);

		if(e.isCancel()) {
			// forward
			hideEditor();
			// cancel any changes
			//revert();
			// return to list view
			MessageLogBottomPanel.showListPanel();
		}
		else if(e.isFinish()) {
			// forward
			if(apply()) {
				// forward
				hideEditor();
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
			m_gotoPanel = getPOIPanel().getGotoPanel();
			// hide buttons
			m_gotoPanel.setGotoButtonVisible(false);
			m_gotoPanel.setButtonVisible("toggle",false);
			// turn off vertical scrollbar
			m_gotoPanel.setNotScrollBars();
			// set preferred size of body component
			Utils.setFixedWidth(m_gotoPanel,275);
		}
		return m_gotoPanel;
	}

	/**
	 * This method initializes DTG attribute
	 *
	 * @return javax.swing.JPanel
	 */
	private TextField getNameAttr() {
		if (m_nameAttr == null) {
			// get from position panel
			m_nameAttr = (TextField)getPOIPanel().getOptionsPanel().getField("Name");
			m_nameAttr.setCaptionText("Navn i kart");
			m_nameAttr.setFixedCaptionWidth(80);
		}
		return m_nameAttr;
	}

	/**
	 * This method initializes OptionsPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private HeaderPanel getOptionsPanel() {
		if (m_optionsPanel == null) {
			// create header panel
			m_optionsPanel = new HeaderPanel("",ButtonSize.SMALL,SwingConstants.LEFT);
			// get name attribute
			AbstractField<?,?,?> attr = getNameAttr();
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

	@Override
	public boolean cancel() {
		return getPOIPanel().cancel();
	}

	@Override
	public boolean finish() {
		return getPOIPanel().finish();
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

		// consume
		setChangeable(false);

		// suspend update events
		m_wp.getMsoModel().suspendChange();

		// get panel
		POIPanel panel = getPOIPanel();

		// prevent changes in panel
		panel.setChangeable(false);

		// initialize status flag
		boolean bFlag = false;

		// get added or updated poi
		IPOIIf poi = m_tool.getPOI();

		if(poi!=null && poi.getPosition()!=null) {

			// create message and message line
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			IMessageLineIf messageLine = message.findMessageLine(MessageLineType.POI, true);

			// update line
			messageLine.setLinePOI(poi);

			// get flag
			boolean isIntelligence =
				  (POIType.FINDING.equals(poi.getType())
				|| POIType.SILENT_WITNESS.equals(poi.getType()));

			// update task?
			if(poi!=null && isIntelligence)
				// forward
				schedule(message,poi,TaskType.INTELLIGENCE,TaskSubType.FINDING,TaskPriority.HIGH);

			// is dirty?
			MessageLogBottomPanel.setIsDirty();

			// changed
			bFlag = true;

		}

		// enable changes in panel
		panel.setChangeable(true);

		// resume update
		m_wp.getMsoModel().resumeUpdate();

		// resume changes
		setChangeable(true);

		// finished
		return bFlag;
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
			task.setSourceClass(message.getClassCode());
			task.setCreatingWorkProcess(getName());
			task.setDescription(MsoUtils.getMessageText(message));
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

	public void reset()
	{
		getPOIPanel().reset();
	}

	public void showEditor()
	{
		try {

			// get current tool state
			m_toolState = m_tool.save();

			// show poi in map
			IPOIIf poi = centerAtPOI(true);
			// show tool
			setToolVisible(true);

			// prepare tool
			m_tool.setShowDialog(false);				// do not show tool dialog
			m_tool.setWorkPoolMode(false);				// ensures that mso model is
														// updated on this thread (in sync)
			m_tool.setToolPanel(getPOIPanel());			// ensures that this position panel
														// is used to apply change to mso model
			m_tool.setShowDrawFrame(false);				// do not show draw frame
			// get draw adapter
			m_tool.getDrawAdapter().setup(MsoClassCode.CLASSCODE_POI, poi, true);
			// activate tool
			m_wp.getMap().setActiveTool(m_tool, 0);
			// show panel
			this.setVisible(true);
			// show map
			MessageLogPanel.showMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		NavMenu bar = m_wp.getApplication().getNavMenu();
		List<Enum<?>> types = Utils.getListOf(MapToolType.POI_TOOL);
		bar.setVisibleButtons(types, isVisible, true);
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
	public void setMessage(IMessageIf message)
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
					if(isSelected) map.centerAt(poi);
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
