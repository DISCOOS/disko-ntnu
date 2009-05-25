package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.document.AlphaNumericDocument;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.HeaderPanel;
import org.redcross.sar.gui.panel.NumPadPanel;
import org.redcross.sar.gui.panel.UnitTypeInputPanel;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IFlowListener;
import org.redcross.sar.work.event.FlowEvent;

/**
 * Provides a dialog for selecting broadcast or non-broadcast receiver.
 * This dialog also handles sub-dialogs such as field based unit selection,
 * list unit selection and the broadcast dialogs
 *
 * @author kennetgu
 */
public class ChangeToDialog extends DefaultDialog implements IEditorIf
{
	private static final long serialVersionUID = 1L;

	private DefaultPanel m_contentPanel;

	private JPanel m_leftPanel;
	private JPanel m_modePanel;
	private JPanel m_castPanel;
	private JPanel m_stepPanel;
	private JPanel m_inputPanel;
	private JFormattedTextField m_unitTextField;
	private UnitTypeInputPanel m_typePanel;
	private NumPadPanel m_numberPanel;
	private JPanel m_receiverPanel;
	private CommunicatorListPanel m_listPanel;
	private HeaderPanel m_groupPanel;

	private JToggleButton m_broadcastButton;
	private JToggleButton m_unicastButton;
	private JToggleButton m_selectionButton;
	private JToggleButton m_confirmedButton;

	private ButtonGroup m_castButtonGroup;
	private ButtonGroup m_stepButtonGroup;
	private ButtonGroup m_groupButtonGroup;

	private boolean m_isBroadcastMode = false;
	private boolean m_isSelectionMode = true;

	private IDiskoWpMessageLog m_wp;

	private final  CommunicatorListModel m_available = new CommunicatorListModel(true);

	private final List<ICommunicatorIf> m_selection = new ArrayList<ICommunicatorIf>();
	private final List<ICommunicatorIf> m_confirmed = new ArrayList<ICommunicatorIf>();


	/* ========================================================
	 * Constructors
	 * ======================================================== */

	/**
	 * @param wp Message log work process reference
	 */
	public ChangeToDialog(IDiskoWpMessageLog wp)
	{
		// forward
		super();

		// prepare
		m_wp = wp;

		// initialize GUI
		initialize();
	}

	/* ========================================================
	 * Overridden methods
	 * ======================================================== */

	@Override
	public void pack() {

		// update layout
		getStepPanel().setEnabled(m_isBroadcastMode);
		getGroupPanel().setVisible(m_isBroadcastMode);
		getUnicastButton().setSelected(!m_isBroadcastMode);
		getBroadcastButton().setSelected(m_isBroadcastMode);
		getSelectionButton().setSelected(m_isSelectionMode);
		getConfirmedButton().setSelected(!m_isSelectionMode);

		// forward
		super.pack();
	}

	/* ========================================================
	 * Public methods
	 * ======================================================== */

	public boolean isBroadcastMode()
	{
		return m_isBroadcastMode;
	}

	public boolean setBroadcastMode(boolean isBroadcastMode)
	{
		// is changed?
		if(m_isBroadcastMode != isBroadcastMode) {

			// set flag
			m_isBroadcastMode = isBroadcastMode;

			// prepare
			getNumberPanel().setValue(null);
			load(false);
			getListPanel().setSingleSelectionMode(!isBroadcastMode);
			if(isBroadcastMode)
				getListPanel().setRows(6);
			else
				getListPanel().setRows(5);

			// forward
			setCaption();

			// apply
			pack();

			// set focus on search field
			getUnitField().requestFocusInWindow();

			// changed
			return true;

		}

		// set default selection mode ?
		if(!m_isSelectionMode) {
			return setSelectionMode(true);
		}

		// unchanged
		return false;

	}

	public boolean isSelectionMode()
	{
		return m_isSelectionMode;
	}

	public boolean setSelectionMode(boolean isSelectionMode)
	{

		// ensure broadcast mode
		if(!m_isBroadcastMode) setBroadcastMode(true);

		// is changed?
		if(m_isSelectionMode != isSelectionMode) {

			// set flag
			m_isSelectionMode = isSelectionMode;

			// initialize
			getNumberPanel().setValue("");
			showCast(isSelectionMode);

			// apply
			pack();

			// set focus on search field
			getUnitField().requestFocusInWindow();

			// changed
			return true;

		}
		// unchanged
		return false;

	}

    /**
     * Register that the broadcast message is not received by communicator. <p/>
     *
     * This will automatically set <code>isBroadcast()</code> flag <code>true</code>.
     *
     * @param ICommunicatorIf c - The receiver that has not confirmed the message.
     *
     * @return <code>true</code> if succeeded, false otherwise
     */
    public boolean setUnconfirmed(ICommunicatorIf c) {
    	// forward
    	setBroadcastMode(true);
    	// remove from confirmed stack
        popCast(c,false,true);
        // add to unconfirmed stack
        return pushCast(c, true,true);

    }
    /**
     * Register that the broadcast message is received by communicator. <p/>
     *
     * This will automatically set <code>isBroadcast()</code> flag <code>true</code>
     * if the number of receivers is greater than 1.
     *
     * @param ICommunicatorIf c - The receiver to transfer.
     *
     * @return <code>true</code> if succeeded, false otherwise.
     */
    public boolean setConfirmed(ICommunicatorIf c) {
        // initialize flag
        boolean bFlag = false;
    	// translate
    	if(getReceivers().size()==0) {
    		setReceiver(c);
    		bFlag = true;
    	}
    	else {
        	// remove from unconfirmed stack
        	pushCast(c,false,true);
            // add to confirmed stack
            bFlag = popCast(c, true,true);
    	}
    	// forward
    	setBroadcastMode(getReceivers().size()>1);
    	// finished
    	return bFlag;
    }

    /**
     * Remove a receiver. If message is a broadcast, the receiver is
     * removed from unconfirmed or confirmed states accordingly.
     *
     * @param ICommunicatorIf c - The receiver to remove from message
     */
    public void removeReceiver(ICommunicatorIf c) {
    	// forward
    	popCast(c, true,true);
    	popCast(c, false,true);
    }

    /**
     * Get the confirmed receiver of a unicast message, or the first receiver in a broadcast message.
     *
     * @return ICommunicatorIf - the receiver
     */
	public ICommunicatorIf getReceiver() {
		List<ICommunicatorIf> list = getReceivers();
		return list.size()>0 ? list.get(0) : null;
	}

    /**
     * Set a single receiver. This will reset all broadcast information and
     * <code>isBroadcast()</code> to <code>false</code>
     *
     * @param ICommunicatorIf - communicatorIf
     */
	public void setReceiver(ICommunicatorIf c) {

		// set unicast mode
		setBroadcastMode(false);

		// forward
		getNumberPanel().setValue(c!=null ? c.getCommunicatorShortName() : "");

	}

    /**
     * Get all receivers of the message
     *
     * @return ICommunicatorIf - the receiver
     */
	public List<ICommunicatorIf> getReceivers()
	{
		// create list
		List<ICommunicatorIf> list = new ArrayList<ICommunicatorIf>(1);
		// translate
		if(m_isBroadcastMode) {
			list.addAll(m_selection);
			list.addAll(m_confirmed);
		}
		else {
			// get pattern
			Object pattern = getPattern();
			// find
			ICommunicatorIf c = pattern!=null ? getListPanel().find(pattern.toString()) : null;
			// add?
			if(c!=null) list.add(c);
		}
		return list;
	}

	/* ==========================================================
	 * IEditorIf implementation
	 * ==========================================================*/

	public void showEditor()
	{
		// show me
		setVisible(true);

		// forward
		load();

		// set focus on search field
		getUnitField().requestFocusInWindow();

	}

	public void hideEditor()
	{
		this.setVisible(false);
	}

	public void setMessage(IMessageIf message)
	{
		getContentPanel().setMsoObject(message);
	}

	public void reset()
	{
		getContentPanel().setMsoObject(null);
	}

	/* ========================================================
	 * Helper methods
	 * ======================================================== */

	private void initialize() {

		// prepare
		m_castButtonGroup = new ButtonGroup();
		m_stepButtonGroup = new ButtonGroup();
		m_groupButtonGroup = new ButtonGroup();

		// create dialog
        this.setContentPane(getContentPanel());
        this.setMoveable(false);

        // size to content
		this.pack();

	}

	private DefaultPanel getContentPanel()
	{
		if(m_contentPanel==null) {

			// extend default panel
			m_contentPanel = new DefaultPanel(Utils.getHtml("<b>Velg mottaker</b>"),true,true,ButtonSize.NORMAL) {

				private static final long serialVersionUID = 1L;

				@Override
				protected boolean beforeFinish() {

					// allowed?
					if(unitsExists()) return true;

					// translate mode
					if(m_isBroadcastMode) {

						// notify
						Utils.showWarning(
								m_wp.getBundleText("NoSelectionErrorMessage.text"),
								m_wp.getBundleText("NoSelectionErrorDetails.text"));

					}
					else {

						// notify
						Utils.showWarning(
								m_wp.getBundleText("NonexistingUnitErrorMessage.text"),
								m_wp.getBundleText("NonexistingUnitErrorDetails.text"));
					}

					// only allowed
					return false;

				}

				@Override
				public void setMsoObject(IMsoObjectIf msoObj) {

					// forward
					super.setMsoObject(msoObj);

					// consume?
					if(!isChangeable()) return;

					// forward
					load();

				}

				@Override
				protected void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
					load();
				}

			};
			m_contentPanel.setRequestHideOnFinish(true);
			m_contentPanel.setRequestHideOnCancel(true);
			m_contentPanel.setContainerBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			m_contentPanel.setContainerLayout(new BoxLayout((JPanel)m_contentPanel.getContainer(),BoxLayout.X_AXIS));
			m_contentPanel.addToContainer(getLeftPanel());
			m_contentPanel.addToContainer(Box.createHorizontalStrut(5));
			m_contentPanel.addToContainer(getReceiverPanel());
			m_contentPanel.addFlowListener(new IFlowListener() {

				@Override
				public void onFlowPerformed(FlowEvent e) {

					// forward?
					if(e.isFinish()) change();

				}

			});

		}
		return m_contentPanel;
	}

	private JPanel getModePanel() {
		if(m_modePanel==null) {
			m_modePanel = new JPanel();
			m_modePanel.setLayout(new BoxLayout(m_modePanel,BoxLayout.X_AXIS));
			m_modePanel.setBorder(UIFactory.createBorder());
			m_modePanel.add(getCastPanel());
			m_modePanel.add(new JSeparator(JSeparator.VERTICAL));
			m_modePanel.add(getStepPanel());

		}
		return m_modePanel;
	}

	private JPanel getLeftPanel() {
		if(m_leftPanel==null) {
			m_leftPanel = new JPanel();
			m_leftPanel.setLayout(new BoxLayout(m_leftPanel,BoxLayout.Y_AXIS));
			m_leftPanel.add(getModePanel());
			m_leftPanel.add(Box.createVerticalStrut(5));
			m_leftPanel.add(Box.createVerticalGlue());
			m_leftPanel.add(getUnitField());
			m_leftPanel.add(Box.createVerticalStrut(5));
			m_leftPanel.add(getInputPanel());

		}
		return m_leftPanel;
	}

	private JPanel getCastPanel() {
		if(m_castPanel==null) {
			m_castPanel = new JPanel();
			m_castPanel.setLayout(new BoxLayout(m_castPanel,BoxLayout.X_AXIS));
			m_castPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			m_castPanel.add(getUnicastButton());
			m_castPanel.add(Box.createHorizontalStrut(5));
			m_castPanel.add(getBroadcastButton());

		}
		return m_castPanel;
	}

	private JPanel getStepPanel() {
		if(m_stepPanel==null) {
			m_stepPanel = new JPanel() {

				private static final long serialVersionUID = 1L;

				@Override
				public void setEnabled(boolean isEnabled) {
					super.setEnabled(isEnabled);
					getSelectionButton().setEnabled(isEnabled);
					getConfirmedButton().setEnabled(isEnabled);
				}

			};
			m_stepPanel.setLayout(new BoxLayout(m_stepPanel,BoxLayout.X_AXIS));
			m_stepPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			m_stepPanel.add(getSelectionButton());
			m_stepPanel.add(Box.createHorizontalStrut(5));
			m_stepPanel.add(getConfirmedButton());

		}
		return m_stepPanel;
	}

	private JFormattedTextField getUnitField() {
		if(m_unitTextField==null) {
			m_unitTextField = new JFormattedTextField();
			m_unitTextField.setDocument(new AlphaNumericDocument());
			Utils.setFixedHeight(m_unitTextField, 35);
		}
		return m_unitTextField;
	}

	private JPanel getInputPanel() {
		if(m_inputPanel==null) {
			m_inputPanel = new JPanel();
			m_inputPanel.setLayout(new BoxLayout(m_inputPanel,BoxLayout.X_AXIS));
			m_inputPanel.add(getTypePanel());
			m_inputPanel.add(Box.createHorizontalStrut(5));
			m_inputPanel.add(getNumberPanel());
		}
		return m_inputPanel;
	}

	private UnitTypeInputPanel getTypePanel()
	{
		if(m_typePanel==null) {
			m_typePanel = new UnitTypeInputPanel("Type",3);
			m_typePanel.setHeaderVisible(false);
			m_typePanel.addFlowListener(new IFlowListener() {

				/**
				 * Updates the type filter based on which buttons are pressed in the unit type selection pad
				 */
				public void onFlowPerformed(FlowEvent e) {
					// consume?
					if(!isChangeable()) return;
					// get type
					UnitType type = getTypePanel().getType();
					// forward
					getNumberPanel().setValue(type!=null ? DiskoEnumFactory.getText(type, "letter") : "");
				}

			});
		}
		return m_typePanel;
	}

	private NumPadPanel getNumberPanel() {
		if(m_numberPanel==null) {
			m_numberPanel = new NumPadPanel("Nummer",false,false);
			m_numberPanel.setHeaderVisible(false);
			m_numberPanel.setInputVisible(false);
			m_numberPanel.setInputField(getUnitField(), false);
			m_numberPanel.addFlowListener(new IFlowListener() {

				@Override
				public void onFlowPerformed(FlowEvent e) {

					// consume?
					if(!isChangeable()) return;

					// forward?
					if(e.isChange()) {

						// disable listeners
						setChangeable(false);

						// get prefix
						String prefix = getNumberPanel().getPrefix();

						// update type panel
						getTypePanel().setType(getType(prefix!=null && prefix.length()==1 ? prefix.charAt(0) : '0'));

						// enable listeners
						setChangeable(true);

						// forward?
						if(m_isBroadcastMode) {
							// forward
							showCast(m_isSelectionMode);
						}
						else {
							load(false);
						}

					}
					else if(e.isFinish()) {
						// forward?
						if(m_isBroadcastMode) {
							// mark button
							for(ICommunicatorIf it : getListPanel().getCommunicators())
							{
								if(getListPanel().isMarked(it)) {
									pushCast(it, m_isSelectionMode,false);
								}
							}
							// clear marked
							getListPanel().clearMarked();
							// move next?
							if(m_isSelectionMode) {
								setSelectionMode(false);
								return;
							}
						}
						// forward
						finish();
					}
				}

			});
		}
		return m_numberPanel;
	}

	private JPanel getReceiverPanel() {
		if(m_receiverPanel==null) {
			m_receiverPanel = new JPanel(new BorderLayout(5,5));
			m_receiverPanel.setBorder(BorderFactory.createEmptyBorder());
			JScrollPane pane = UIFactory.createScrollPane(getListPanel());
			pane.setBorder(UIFactory.createBorder());
			pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			m_receiverPanel.add(pane,BorderLayout.CENTER);
			m_receiverPanel.add(getGroupPanel(),BorderLayout.SOUTH);
		}
		return m_receiverPanel;
	}

	private CommunicatorListPanel getListPanel() {
		if(m_listPanel==null) {
			m_listPanel = new CommunicatorListPanel(m_wp,m_isBroadcastMode);
			m_listPanel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// forward?
					if(e.getSource() instanceof JToggleButton) {
						// consume?
						if(!isChangeable()) return;
						// cast to abstract button
						JToggleButton b = (JToggleButton)e.getSource();
						// translate
						if(m_isBroadcastMode)
						{
							// get communicator
							ICommunicatorIf c = getListPanel().getCommunicator(b);

							// push or pop change?
							if(b.isSelected())
							{
								pushCast(c,m_isSelectionMode,false);
							}
							else
							{
								popCast(c,m_isSelectionMode,false);
							}
						}
						else if(b.isSelected()) {

							// disable listeners
							setChangeable(false);

							// get communicator
							ICommunicatorIf sender = getListPanel().getCommunicator(b);

							// update type panel
							getNumberPanel().setValue(sender.getCommunicatorShortName());

							// enable listeners
							setChangeable(true);

							// forward
							finish();

						}
					}

				}

			});

		}
		return m_listPanel;
	}

	private HeaderPanel getGroupPanel() {
		if(m_groupPanel==null) {

			m_groupPanel = new HeaderPanel("Velg gruppevis",ButtonSize.NORMAL);

        	// add left buttons
        	addGroupButton(DiskoButtonFactory.createToggleButton("GENERAL.ALL",ButtonSize.NORMAL),"ALL");
        	addGroupButton(DiskoButtonFactory.createToggleButton("GENERAL.NONE",ButtonSize.NORMAL),"NONE");

        	// get unit types
            UnitType[] types = UnitType.values();

            // add all unit types
            for(int i=0;i<types.length;i++) {

            	// get type
            	UnitType type = types[i];

            	// create button
            	AbstractButton button = DiskoButtonFactory.createToggleButton(type,ButtonSize.NORMAL);

            	// forward
            	addGroupButton(button,type.name());

            }

		}
		return m_groupPanel;
	}

	private void addGroupButton(AbstractButton b, String command) {

    	// add to panel
		getGroupPanel().addButton(b, command);

		// disable focusable
		b.setFocusable(false);

    	// add to button group
    	m_groupButtonGroup.add(b);

		// listen for button actions
		b.addActionListener(m_groupButtonListener);

	}

	private JToggleButton getUnicastButton()
	{
		if(m_unicastButton==null) {

			String text = m_wp.getBundleText("UnicastButton.text");
			m_unicastButton = DiskoButtonFactory.createToggleButton(text,text,null,ButtonSize.NORMAL,25,0);
			m_unicastButton.setFocusable(false);
			m_unicastButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// prompt?
					if(getReceivers().size()>0) {
						int ans = Utils.showConfirm("Bekreftelse",
								"Du er i ferd med å avbryte et fellesoppkall. Alle registringer vil bli fjernet. " +
								"Vil du fortsette?",
								JOptionPane.YES_NO_OPTION);
						// consume?
						if(ans==JOptionPane.NO_OPTION) {
							m_broadcastButton.setSelected(true);
							return;
						}
					}
					// set mode
					setBroadcastMode(false);
				}
			});
			m_castButtonGroup.add(m_unicastButton);
		}
		return m_unicastButton;

	}

	private JToggleButton getBroadcastButton()
	{
		if(m_broadcastButton==null) {

			String text = m_wp.getBundleText("BroadcastButton.text");
			m_broadcastButton = DiskoButtonFactory.createToggleButton(text,text,null,ButtonSize.NORMAL,25,0);
			m_broadcastButton.setFocusable(false);
			m_broadcastButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// set mode
					setBroadcastMode(true);
				}
			});
			m_castButtonGroup.add(m_broadcastButton);
		}
		return m_broadcastButton;
	}

	private JToggleButton getSelectionButton()
	{
		if(m_selectionButton==null) {

			String text = m_wp.getBundleText("SelectionButton.text");
			m_selectionButton = DiskoButtonFactory.createToggleButton(text,text,null,ButtonSize.NORMAL,25,0);
			m_selectionButton.setFocusable(false);
			m_selectionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			m_selectionButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(m_isBroadcastMode) {
						// forward
						setSelectionMode(true);

					}
				}
			});
			m_stepButtonGroup.add(m_selectionButton);

		}
		return m_selectionButton;
	}

	private JToggleButton getConfirmedButton()
	{
		if(m_confirmedButton==null) {

			String text = m_wp.getBundleText("ConfirmButton.text");
			m_confirmedButton = DiskoButtonFactory.createToggleButton(text,text,null,ButtonSize.NORMAL,25,0);
			m_confirmedButton.setFocusable(false);
			m_confirmedButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			m_confirmedButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(m_isBroadcastMode) {
						// forward
						setSelectionMode(false);
					}
				}
			});
			m_stepButtonGroup.add(m_confirmedButton);

		}
		return m_confirmedButton;
	}

	private boolean pushCast(ICommunicatorIf c, boolean isSelectionMode, boolean show) {
		boolean bFlag = false;
		if(isSelectionMode)
		{
			if(!m_selection.contains(c)) bFlag = m_selection.add(c);
		}
		else
		{
			if(!m_confirmed.contains(c)) bFlag = m_confirmed.add(c);
		}
		// update view?
		if(bFlag) {
			if(show)
				showCast(isSelectionMode);
			else
				setGroupSelections();
		}
		// finished
		return bFlag;
	}

	private boolean popCast(ICommunicatorIf c, boolean isSelectionMode, boolean show) {
		boolean bFlag = false;
		if(isSelectionMode)
		{
			if(m_selection.contains(c)) bFlag = m_selection.remove(c);
		}
		else
		{
			if(m_confirmed.contains(c)) bFlag = m_confirmed.remove(c);
		}
		// update view?
		if(bFlag) {
			if(show)
				showCast(isSelectionMode);
			else
				setGroupSelections();
		}
		return false;
	}

	private void pushCast(UnitType type, boolean isSelectionMode) {

		// allowed?
		if(m_isBroadcastMode)
		{

			/* ===================================================
			 * Step 1 - select appropriate list
			 * =================================================== */

			// initialize
			List<ICommunicatorIf> list;

			// get list
			if(isSelectionMode)
			{
				list = m_selection;
			}
			else
			{
				list = m_confirmed;
			}

			/* ===================================================
			 * Step 2 - add all communicators of same type to list
			 * =================================================== */

			// all units?
			if(type==null) {
				list.clear();
				list.addAll(m_available.getElements());
			}
			else
			{
				for(ICommunicatorIf c : m_available.getElements())
				{
					boolean isSelected = false;
					if(c instanceof ICmdPostIf)
					{
						isSelected = (!list.contains(c) && type == UnitType.CP);
					}
					else if(c instanceof IUnitIf)
					{
						isSelected = (!list.contains(c) && ((IUnitIf)c).getType() == type);
					}
					if(isSelected)
					{
						list.add(c);
					}
				}
			}

			/* ===================================================
			 * Step 3 - update view
			 * =================================================== */

			showCast(isSelectionMode);

		}
	}

	private void popCast(UnitType type, boolean isSelectionMode)
	{
		// allowed?
		if(m_isBroadcastMode)
		{

			/* ===================================================
			 * Step 1 - select appropriate list
			 * =================================================== */

			// initialize
			List<ICommunicatorIf> list;

			// get list
			if(isSelectionMode)
			{
				list = m_selection;
			}
			else
			{
				list = m_confirmed;
			}

			/* ===================================================
			 * Step 2 - remove all communicators of same type from
			 * =================================================== */

			// all units?
			if(type==null) {
				list.clear();
				list.removeAll(m_available.getElements());
			}
			else
			{
				for(ICommunicatorIf c : m_available.getElements())
				{
					boolean isSelected = false;
					if(c instanceof ICmdPostIf)
					{
						isSelected = (list.contains(c) && type == UnitType.CP);
					}
					else if(c instanceof IUnitIf)
					{
						isSelected = (list.contains(c) && ((IUnitIf)c).getType() == type);
					}
					if(isSelected)
					{
						list.remove(c);
					}
				}
			}

			/* ===================================================
			 * Step 3 - update view
			 * =================================================== */

			showCast(isSelectionMode);

		}
	}

	private void initCast(boolean isSelectionMode, boolean all, boolean show) {

		// clear lists
		if(isSelectionMode || all) m_selection.clear();
		if(!isSelectionMode || all) m_confirmed.clear();

		// get current message
		IMessageIf message = (IMessageIf)getContentPanel().getMsoObject();

		// message exists?
		if(message!=null) {

			// populate from message
			if(isSelectionMode || all) {
				m_selection.addAll(message.getConfirmedReceiversItems());
				m_selection.addAll(message.getUnconfirmedReceiversItems());
			}
			if(!isSelectionMode || all) {
				m_confirmed.addAll(message.getConfirmedReceiversItems());
			}

		}

		// forward?
		if(show) {
			showCast(isSelectionMode);
		}

	}

	private void showCast(boolean isSelectionMode) {

		if(m_isBroadcastMode)
		{
			if(isSelectionMode)
			{
				getListPanel().load();
				getListPanel().clearSelection();
				getListPanel().setSelected(m_selection, true);
			}
			else
			{
				getListPanel().load(m_selection);
				getListPanel().clearSelection();
				getListPanel().setSelected(m_confirmed, true);
			}
			setGroupSelections();
			setPatternSelections();
		}
		setCaption();
	}

	private void setCaption() {
		String text = "";
		if(m_isBroadcastMode) {

			text = "<b>Fellesoppkall</b> - " + String.format(
					m_wp.getBundleText("BroadcastStatusLabel.text"),
					m_confirmed.size(),m_selection.size());

		}
		else {

			ICommunicatorIf c = getReceiver();
			if(c==null)
				text = Utils.getHtml("<b>Velg mottaker</b>");
			else
				text = "<b>Endre mottaker</b> - " + MsoUtils.getMsoObjectName(c, 1);

		}
		// update caption
		getContentPanel().setCaptionText(text);

	}

	private UnitType getType(String name) {
		// get type
		try
		{
			if(name!=null && name.length()!=0)
				return UnitType.valueOf(name);
		}
		catch(Exception ex) { /*Consume*/ }
		// not found
		return null;
	}

	/**
	 * Checks to see whether selected units actually exists in the unit list or not
	 *
	 * @return true if a communicator with given type and number exists, false otherwise
	 */
	private boolean unitsExists()
	{
		if(m_isBroadcastMode)
		{
			return getReceivers().size()>0;
		}
		else
		{
			// get pattern
			Object pattern = getPattern();
			// find
			return pattern!=null ? getListPanel().find(pattern.toString())!=null : false;
		}
	}

	private UnitType getType(char prefix) {
		UnitType[] types = UnitType.values();
		for(int i=0 ; i<types.length; i++) {
			String letter = DiskoEnumFactory.getText(types[i],"letter");
			if(letter!=null && !letter.isEmpty() && letter.charAt(0)==prefix)
				return types[i];
		}
		// not found
		return null;
	}

	private Object getPattern() {

		// detect type of load operation
		Object value = getNumberPanel().getValue();

		// has pattern?
		if(value!=null) {

			// get current
			int number = getNumberPanel().getNumber();
			String prefix = getNumberPanel().getPrefix();

			// get flags
			boolean validNumber = (number!=-1);
			boolean validPrefix = (prefix!=null && !prefix.isEmpty());

			// prefix only?
			if(!validNumber && validPrefix) {
				value = ".*" + prefix + ".*";
			}
			// number only?
			else if(validNumber && !validPrefix){
				value = ".*" + number  + ".*";
			}
			else if(validNumber && validPrefix){
				value = ".*" + prefix + ".*" + number;
			}
		}
		return value;
	}

	private void load() {
		// suspend
		setChangeable(false);
		// update
		getListPanel().clearSelection();
		getNumberPanel().setValue(null);
		load(true);
		setCaption();
		// resume
		setChangeable(true);
	}

	private void load(boolean fromMSO)
	{

		// get message
		IMessageIf message = (IMessageIf)getContentPanel().getMsoObject();

		// update broadcast mode?
		if(fromMSO && message!=null) {
			setBroadcastMode(message.isBroadcast());
		}

		// load data
		if(m_isBroadcastMode)
		{
			// try to load from message?
			if(fromMSO) {
				initCast(m_isSelectionMode,true,true);
			}

			// set confirmed mode?
			if(m_selection.size()>0) setSelectionMode(false);

			// forward
			setGroupSelections();

			// clear pattern
			getNumberPanel().setValue("");

		}
		else
		{

			// clear selections
			m_selection.clear();
			m_confirmed.clear();

			if(fromMSO) {
				ICommunicatorIf c = (message!=null ? message.getReceiver() : null);
				setChangeable(false);
				getNumberPanel().setValue(c!=null ? c.getCommunicatorShortName() : "");
				setChangeable(true);
			}

			// get pattern
			Object pattern = getPattern();

			// forward
			if(pattern instanceof String) {
				getListPanel().load(pattern.toString());
			}
			else {
				getListPanel().load();
			}

		}

	}

	private void setGroupSelections() {

		// allowed?
		if(m_isBroadcastMode) {

			// disable listeners
			setChangeable(false);

			// get receivers
			List<ICommunicatorIf> selection = getListPanel().getSelection();
			CommunicatorListModel available = m_isSelectionMode ? m_available : getListPanel().getModel();

			// forward
			m_groupButtonGroup.clearSelection();

			// none?
			if(selection.size()==0) {
				getGroupPanel().getButton("NONE").setSelected(true);
			}
			else if(selection.size()==available.getSize()) {
				getGroupPanel().getButton("ALL").setSelected(true);
			}
			else {
				// get all types
				UnitType[] types = UnitType.values();
				// search
				for(int i=0; i<types.length; i++) {
					List<ICommunicatorIf> group = available.findAll(types[i]);
					if(selection.containsAll(group) && group.size()>0) {
						getGroupPanel().getButton(types[i].name()).setSelected(true);
					}
				}
			}

			// enable listeners
			setChangeable(true);

		}

	}

	private void setPatternSelections() {
		// get pattern
		String pattern = getPattern().toString();
		// clear already marked
		getListPanel().clearMarked();
		// has pattern?
		if(!pattern.isEmpty()) {
			// find component
			List<ICommunicatorIf> list = getListPanel().findAll(pattern.toString());
			// mark button
			for(ICommunicatorIf it : list)
			{
				JToggleButton b = getListPanel().getButton(it);
				if(b!=null) {
					getListPanel().setMarked(it,true,Color.BLUE);
					JScrollPane pane = (JScrollPane)getReceiverPanel().getComponent(0);
					pane.getViewport().scrollRectToVisible(b.getBounds());
				}
			}
		}
	}

	private void change()
	{

		// disable listeners
		setChangeable(false);

		// get message, create if no exists
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true,m_isBroadcastMode);

		// translate
		if(m_isBroadcastMode) {

			// update message
			message.setBroadcast(true);

			// get lists
			List<ICommunicatorIf> unconfirmed = getUnconfirmed();
			List<ICommunicatorIf> receivers = new ArrayList<ICommunicatorIf>(message.getReceivers());

			// synchronize?
			if(receivers.size()>0)
			{
				// remove unselected
				for(ICommunicatorIf it : receivers)
				{
					if(!(unconfirmed.contains(it) || m_confirmed.contains(it)))
					{
						message.removeReceiver(it);
					}
				}

			}
			// add selection list
			for(ICommunicatorIf it : unconfirmed)
			{
				if(!receivers.contains(it))
				{
					message.setUnconfirmed(it);
				}
			}
			// add confirmation list
			for(ICommunicatorIf it : m_confirmed)
			{
				if(!receivers.contains(it))
				{
					message.setConfirmed(it);
				}
			}

		}
		else
		{
			// update message
			message.setBroadcast(false);
			message.setReceiver(getReceiver());
		}

		// enable listeners
		setChangeable(true);

	}

	private List<ICommunicatorIf> getUnconfirmed() {
		List<ICommunicatorIf> list = new ArrayList<ICommunicatorIf>();
		// get unconfirmed
		for(ICommunicatorIf it : m_selection)
		{
			if(!m_confirmed.contains(it))
				list.add(it);
		}
		return list;
	}

	/* ========================================================
	 * Anonymous classes
	 * ======================================================== */

	private ActionListener m_groupButtonListener = new ActionListener() {

		public void actionPerformed(ActionEvent e){
			// forward?
			if(e.getSource() instanceof AbstractButton) {
				// cast to JToggleButton
				AbstractButton b = (AbstractButton)e.getSource();
				// translate
				if(m_isBroadcastMode) {
					// get command
					String cmd = b.getActionCommand();
					// get flag
					boolean isSelected = b.isSelected();
					// translate
					if("DUMMY".equalsIgnoreCase(cmd))
						return;
					else if("ALL".equalsIgnoreCase(cmd)) {
						if(isSelected)
							pushCast((UnitType)null,m_isSelectionMode);
						else
							popCast((UnitType)null,m_isSelectionMode);
					}
					else if("NONE".equalsIgnoreCase(cmd)) {
						initCast(m_isSelectionMode,false,true);
					}
					else
					{
						UnitType type = getType(b.getActionCommand());
						if(isSelected)
							pushCast((UnitType)type,m_isSelectionMode);
						else
							popCast((UnitType)type,m_isSelectionMode);
					}
					setGroupSelections();
				}
			}
		}

	};

}
