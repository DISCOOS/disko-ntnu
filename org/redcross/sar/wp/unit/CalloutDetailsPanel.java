package org.redcross.sar.wp.unit;

import org.redcross.sar.gui.document.AutoCompleteDocument;
import org.redcross.sar.gui.event.IAutoCompleteListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.FieldPane;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.AssocUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.AssocUtils.Association;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.work.event.FlowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.EnumSet;
import java.util.ResourceBundle;

/**
 * JPanel displaying alert details
 *
 * @author thomasl
 */
public class CalloutDetailsPanel extends JPanel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;

	private JButton m_printButton;

	private FieldPane m_infoPanel;
	private TextField m_titleTextField;
	private TextField m_createdTextField;
	private TextField m_associationTextField;

	private BasePanel m_personnelPanel;
	private DiskoTable m_personnelTable;

	private ICalloutIf m_currentCallout;

	private IDiskoWpUnit m_wp;

	private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	public CalloutDetailsPanel(IDiskoWpUnit wp)
	{
    	// prepare
        m_wp = wp;
        // initialize GUI
        initialize();
        // add listeners
        wp.getMsoEventManager().addClientUpdateListener(this);
        wp.getMsoEventManager().addClientUpdateListener(getInfoPanel());
        getInfoPanel().addFlowListener(wp);
	}

    private void initialize()
    {
    	// prepare
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        // add panels
        add(getInfoPanel());
        add(Box.createVerticalStrut(5));
        add(getPersonnelPanel());        
        
    }

	private FieldPane getInfoPanel() {
		if(m_infoPanel==null) {
			m_infoPanel = new FieldPane(m_resources.getString("UnitInfo.text"),"",false,false);
			m_infoPanel.setColumns(2);
			m_infoPanel.setBufferMode(false);
			m_infoPanel.setPreferredExpandedHeight(175);
			m_infoPanel.setMinimumSize(new Dimension(175,200));
			m_infoPanel.addButton(getPrintButton(), "print");
			m_infoPanel.suspendLayout();
			m_infoPanel.addField(getTitleTextField());
			m_infoPanel.addField(getCreatedTextField());
			m_infoPanel.addField(getAssociationTextField());
			m_infoPanel.resumeLayout();
			m_infoPanel.connect(m_wp.getMsoModel(), EnumSet.of(MsoClassCode.CLASSCODE_CALLOUT));
		}
		return m_infoPanel;
	}	

	private JButton getPrintButton() {
		if(m_printButton==null) {
			m_printButton = DiskoButtonFactory.createButton("GENERAL.PRINT",ButtonSize.SMALL);
			m_printButton.setEnabled(false);
			// TODO: Implement callout printout 
		}
		return m_printButton;
	}    

	private TextField getTitleTextField() {
		if(m_titleTextField==null) {
			m_titleTextField = new TextField("title",m_resources.getString("Title.text"),true);    		
		}
		return m_titleTextField;
	}	

	private TextField getCreatedTextField() {
		if(m_createdTextField==null) {
			m_createdTextField = new TextField("created",m_resources.getString("Created.text"),false);    		
		}
		return m_createdTextField;
	} 

	private TextField getAssociationTextField() {
		if(m_associationTextField==null) {
			m_associationTextField = new TextField("association","Tilhørighet",false);
			JTextField inputField = m_associationTextField.getEditComponent();
			AutoCompleteDocument doc = new AutoCompleteDocument(AssocUtils.getAssociations(-1,"{l:n} {l:s}"),inputField);
			inputField.setDocument(doc);
			doc.addAutoCompleteListener(new IAutoCompleteListener() {

				public void onSuggestionFound(AutoCompleteDocument document, String suggestion) {
					if(!isSet() || m_associationTextField.isChangeable()) return;
					Association[] items = null;
					if(suggestion!=null) {
						items = AssocUtils.parse(suggestion,false,false);
					}
					if(m_currentCallout!=null) {
						m_associationTextField.setChangeable(false);
						m_currentCallout.suspendClientUpdate();
						if(items!=null) {
							m_currentCallout.setOrganization(items[0].getName(1));
							m_currentCallout.setDivision(items[0].getName(2));
							m_currentCallout.setDepartment(items[0].getName(3));
						} else {
							m_currentCallout.setOrganization(null);
							m_currentCallout.setDivision(null);
							m_currentCallout.setDepartment(null);
						}
						m_currentCallout.resumeClientUpdate(false);
						m_associationTextField.setChangeable(true);
						m_wp.onFlowPerformed(new FlowEvent(this,m_currentCallout,FlowEvent.EVENT_CHANGE));
					}
				}

			});
		}
		return m_associationTextField;
	}		

	private BasePanel getPersonnelPanel() {
		if(m_personnelPanel==null) {
			m_personnelPanel = new BasePanel(m_resources.getString("Personnel.text"),ButtonSize.SMALL);
			m_personnelPanel.setHeaderVisible(false);
			m_personnelPanel.setContainer(getPersonnelTable());
		}
		return m_personnelPanel;
	}

	private DiskoTable getPersonnelTable() {
		if(m_personnelTable==null) {

    		// get button size
    		Dimension d = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
    		
			// create
			CalloutPersonnelTableModel model = new CalloutPersonnelTableModel();
    		model.setColumnAlignment(2, SwingConstants.CENTER);
    		model.setColumnFixedWidth(2, d.width*2+15);
			m_personnelTable = new DiskoTable(model);
			m_personnelTable.setFillsViewportHeight(true);
			m_personnelTable.setAutoFitWidths(true);
			m_personnelTable.addMouseListener(new CallOutPersonnelMouseListener());
			m_personnelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);			
			m_personnelTable.setColumnSelectionAllowed(false);
			m_personnelTable.setRowHeight(d.height + 10);
			
			// install editor
			TableEditorFactory.installCalloutPersonnelEditor(m_personnelTable, m_wp, 2);
			
			// no header
			m_personnelTable.setTableHeader(null);

		}
		return m_personnelTable;
	}    

	public ICalloutIf getCallOut()
	{
		return m_currentCallout;
	}

	public void setCallOut(ICalloutIf callout)
	{
		m_currentCallout = callout;
        if(callout!=null) {
        	getTitleTextField().setMsoAttribute(callout.getTitleAttribute());
        	getCreatedTextField().setMsoAttribute(callout.getCreatedAttribute());
        	getTitleTextField().reset();
        	getCreatedTextField().reset();
        }  else {
        	getTitleTextField().clearMsoAttribute("");
        	getCreatedTextField().clearMsoAttribute("");
        }
	}

	/**
	 * Update field contents based on current call-out
	 */
	public void updateFieldContents()
	{
		// prevent reenty
		getInfoPanel().setChangeable(false);
		
		if(m_currentCallout != null) {

            // update caption
            getInfoPanel().setCaptionText(m_resources.getString("CallOut.text") + " " + DTG.CalToDTG(m_currentCallout.getCreated()));
			
            if(getAssociationTextField().isChangeable()) {
	            if(m_currentCallout.getOrganization()!=null) {
		            Association assoc = AssocUtils.getOrganization(m_currentCallout.getOrganization());
		            assoc.setPattern("{1:n}");
		            if(m_currentCallout.getDivision()!=null) {
		            	assoc = AssocUtils.getDivision(assoc,m_currentCallout.getDivision());
			            assoc.setPattern("{2:n} {2:s}");
		                if(m_currentCallout.getDepartment()!=null) { 
		                	assoc = AssocUtils.getDepartment(assoc,m_currentCallout.getDepartment());
				            assoc.setPattern("{3:n} {3:s}");
		                }
		            }
		            getAssociationTextField().setChangeable(false);
		            getAssociationTextField().setValue(assoc.getText());
		            getAssociationTextField().setChangeable(true);
	        	} else {
		            getAssociationTextField().setChangeable(false);
		            getAssociationTextField().setValue("");
		            getAssociationTextField().setChangeable(true);        		
	        	}
            }
            CalloutPersonnelTableModel model = (CalloutPersonnelTableModel)m_personnelTable.getModel();
			model.setPersonnelList(m_wp.getMsoModel(),m_currentCallout.getPersonnelList());
			
		} else {
			getInfoPanel().setCaptionText("Velg varsling i listen");
			m_associationTextField.setValue("");
			CalloutPersonnelTableModel model = (CalloutPersonnelTableModel)m_personnelTable.getModel();
			model.setPersonnelList(m_wp.getMsoModel(),null);			
		}

		// resume reenty
		getInfoPanel().setChangeable(true);

	}

    public boolean isChanged() {
    	return m_currentCallout!=null?m_currentCallout.isChanged():false;
    }
    
    public boolean isNew() {
    	return m_currentCallout!=null?!m_currentCallout.isCreated():false;
    }
    
    public boolean isSet() {
    	return m_currentCallout!=null;
    }
    
	/**
	 * Updates attributes
	 */
	public boolean isEditValid()
	{
		if(isSet())
		{
			// TODO: Implement validation
		}
		// success!
		return true;
	}
	
	public EnumSet<MsoClassCode> getInterests() {
		return EnumSet.of(MsoClassCode.CLASSCODE_CALLOUT);
	}

	/**
	 * Update fields if any changes occur in the callout object
	 */
	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		if(events.isClearAllEvent()) {
			setCallOut(null);
			updateFieldContents();
		}
		else {
			// loop over all events
			for(MsoEvent.Update e : events.getEvents(MsoClassCode.CLASSCODE_CALLOUT)) {

				// consume loopback updates
				if(!e.isLoopbackMode())
				{
					// get callout reference
					ICalloutIf callout = 
							(e.getSource() instanceof ICalloutIf) ?
							(ICalloutIf) e.getSource() : null;
					// is object modified?
					if (e.isChangeReferenceEvent()) {
						updateFieldContents();
					}
					else if (e.isModifyObjectEvent()) {
						updateFieldContents();
					}

					// delete object?
					if (e.isDeleteObjectEvent() && callout == m_currentCallout) {
			    		setCallOut(null);
			    		updateFieldContents();
					}
				}
			}
		}
	}	

	private class CallOutPersonnelMouseListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent me)
		{
			Point clickedPoint = new Point(me.getX(), me.getY());
			int row = m_personnelTable.rowAtPoint(clickedPoint);
			int index = m_personnelTable.convertRowIndexToModel(row);
			if(index==-1) return;
			CalloutPersonnelTableModel model = (CalloutPersonnelTableModel)m_personnelTable.getModel();
			IPersonnelIf personnel = model.getPersonnel(index);

			int clickCount = me.getClickCount();

			if(clickCount == 1)
			{
				// Display personnel info in bottom panel
				m_wp.setPersonnelBottom(personnel);
				m_wp.setBottomView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
			}
			else if(clickCount == 2)
			{
				// Change to personnel display
				if(m_wp.setPersonnelLeft(personnel)) {
					m_wp.setPersonnelBottom(personnel);
					m_wp.setLeftView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
					m_wp.setBottomView(IDiskoWpUnit.PERSONNEL_ADDITIONAL_VIEW_ID);
				}
			}
		}
	}
}
