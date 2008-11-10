package org.redcross.sar.wp.messageLog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.document.AlphaNumericDocument;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.mso.panel.UnitTypeInputPanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.NumPadPanel;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

/**
 * The dialog for selecting unit type and number.
 * Dialog loads unit information from resource file org.redcross.sar.mso.data.properties.Unit.properties
 *
 * @author kennetgu
 */
public class ChangeFromDialog extends DefaultDialog implements IEditorIf
{
	private static final long serialVersionUID = 1L;

	private DefaultPanel m_contentPanel;
	
	private JPanel m_leftPanel;
	private JPanel m_inputPanel;
	private UnitTypeInputPanel m_typePanel;
	private NumPadPanel m_numberPanel;
	private JScrollPane m_senderPanel;
	private CommunicatorListPanel m_listPanel;
	private JFormattedTextField m_unitTextField;

	private IDiskoWpMessageLog m_wp;

	/* ========================================================
	 * Constructors
	 * ======================================================== */
	
	/**
	 * @param wp - Message log work process
	 */
	public ChangeFromDialog(IDiskoWpMessageLog wp)
	{
		
		// forward
		super();

		// prepare
		m_wp = wp;			
		
		// initialize GUI
		initialize();

	}	
	/* ========================================================
	 * Public methods
	 * ======================================================== */

	public ICommunicatorIf getSender()
	{
		// get pattern
		Object pattern = getPattern();
		// find
		return pattern!=null ? getListPanel().find(pattern.toString()) : null;
		
	}	
	
	public void setSender(ICommunicatorIf c) {
		
		// forward
		getNumberPanel().setValue(c!=null ? c.getCommunicatorShortName() : "");
		
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
		setVisible(false);
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
        this.setContentPane(getContentPanel());
        this.setMoveable(false);
		this.pack();
		
	}
	
	private DefaultPanel getContentPanel()
	{
		if(m_contentPanel==null) {
			
			// extend default panel
			m_contentPanel = new DefaultPanel(Utils.getHtml("<b>Velg avsender</b>"),true,true,ButtonSize.NORMAL) {
				
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean beforeFinish() {
					
					// allowed?
					if(unitExists()) return true;
					
					// notify
					Utils.showWarning(
								m_wp.getBundleText("NonexistingUnitErrorMessage.text"),
								m_wp.getBundleText("NonexistingUnitErrorDetails.text"));
					
					// not allowed
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
			m_contentPanel.setFitBodyOnResize(true);
			m_contentPanel.setBodyBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			m_contentPanel.setBodyLayout(new BoxLayout((JPanel)m_contentPanel.getBodyComponent(),BoxLayout.X_AXIS));
			m_contentPanel.addBodyChild(getLeftPanel());
			m_contentPanel.addBodyChild(Box.createHorizontalStrut(5));
			m_contentPanel.addBodyChild(getSenderPanel());
			m_contentPanel.addWorkFlowListener(new IWorkFlowListener() {

				@Override
				public void onFlowPerformed(WorkFlowEvent e) {
					
					// forward?
					if(e.isFinish()) change();
					
				}
				
			});
			
		}
		return m_contentPanel;
	}	
	
	private JPanel getLeftPanel() {
		if(m_leftPanel==null) {
			m_leftPanel = new JPanel();
			m_leftPanel.setLayout(new BoxLayout(m_leftPanel,BoxLayout.Y_AXIS));
			m_leftPanel.add(getUnitField());
			m_leftPanel.add(Box.createVerticalStrut(5));
			m_leftPanel.add(getInputPanel());
			
		}
		return m_leftPanel;
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
			m_typePanel.addWorkFlowListener(new IWorkFlowListener() {

				/**
				 * Updates the type filter based on which buttons are pressed in the unit type selection pad
				 */
				public void onFlowPerformed(WorkFlowEvent e) {
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
			m_numberPanel.addWorkFlowListener(new IWorkFlowListener() {

				@Override
				public void onFlowPerformed(WorkFlowEvent e) {
					
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
						
						// forward
						load(false);
						
					}
					else if(e.isFinish()) {
						finish();
					}
				}
				
			});
		}
		return m_numberPanel;
	}	
	
	private JScrollPane getSenderPanel() {
		if(m_senderPanel==null) {
			m_senderPanel = UIFactory.createScrollPane(getListPanel());
			m_senderPanel.setBorder(UIFactory.createBorder());
			m_senderPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); 
			m_senderPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		return m_senderPanel;		
	}
	
	private CommunicatorListPanel getListPanel() {
		if(m_listPanel==null) {
			m_listPanel = new CommunicatorListPanel(m_wp,true);
			m_listPanel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// consume?
					if(!isChangeable()) return;
					// forward?
					if(e.getSource() instanceof AbstractButton) {
						
						// get toggle button
						JToggleButton b = (JToggleButton)e.getSource();
						
						// forward?
						if(b.isSelected()) {
							
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
	
	/**
	 * Checks to see whether the selected unit actually exists in the unit list or not
	 * @return true if a communicator with given type and number exists, false otherwise
	 */
	private boolean unitExists()
	{
		// get pattern
		Object pattern = getPattern();
		// find
		return pattern!=null ? getListPanel().find(pattern.toString())!=null : false;
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
		// forward
		getListPanel().clearSelection();
		getNumberPanel().setValue(null);
		load(true);
		setCaption();
		// resume
		setChangeable(true);		
		
	}
	
	private void load(boolean fromMSO) {

		if(fromMSO) {
			IMessageIf message = (IMessageIf)getContentPanel().getMsoObject();
			ICommunicatorIf c = (message!=null ? message.getSender() : null);
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
	
	private void change()
	{
		// disable listeners
		setChangeable(false);
		
		// get current message, create new if not exists
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
		ICommunicatorIf sender = getSender();
		if(sender != null)
		{
			message.setSender(sender);
		}
		// enable listeners
		setChangeable(true);
		
	}	
	
	private void setCaption() {
		ICommunicatorIf c = getSender();
		if(c==null) {
			getContentPanel().setCaptionText(Utils.getHtml("<b>Velg avsender</b>"));
		}
		else {
			getContentPanel().setCaptionText(Utils.getHtml("<b>Endre avsender</b> - " + MsoUtils.getMsoObjectName(c, 0)));
		}
	}
	
}