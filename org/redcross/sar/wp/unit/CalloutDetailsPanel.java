package org.redcross.sar.wp.unit;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.document.AutoCompleteDocument;
import org.redcross.sar.gui.event.IAutoCompleteListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.AssocUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.AssocUtils.Association;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.work.event.WorkFlowEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

	private static final Logger m_logger = Logger.getLogger(CalloutDetailsPanel.class);
	
	private JButton m_printButton;

	private FieldsPanel m_infoPanel;
	private TextLineField m_titleTextField;
	private TextLineField m_createdTextField;
	private TextLineField m_associationTextField;

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
        getInfoPanel().addWorkFlowListener(wp);
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

	private FieldsPanel getInfoPanel() {
		if(m_infoPanel==null) {
			m_infoPanel = new FieldsPanel(m_resources.getString("UnitInfo.text"),"",false,false);
			m_infoPanel.setColumns(2);
			m_infoPanel.setAutoSave(true);
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

	private TextLineField getTitleTextField() {
		if(m_titleTextField==null) {
			m_titleTextField = new TextLineField("title",m_resources.getString("Title.text"),true);    		
		}
		return m_titleTextField;
	}	

	private TextLineField getCreatedTextField() {
		if(m_createdTextField==null) {
			m_createdTextField = new TextLineField("created",m_resources.getString("Created.text"),false);    		
		}
		return m_createdTextField;
	} 

	private TextLineField getAssociationTextField() {
		if(m_associationTextField==null) {
			m_associationTextField = new TextLineField("association","Tilhørighet",false);
			JTextField inputField = m_associationTextField.getTextField();
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
							m_currentCallout.setOrganization(items[0].getName());
							m_currentCallout.setDivision(items[0].getName());
							m_currentCallout.setDepartment(items[0].getName());
						} else {
							m_currentCallout.setOrganization(null);
							m_currentCallout.setDivision(null);
							m_currentCallout.setDepartment(null);
						}
						m_currentCallout.resumeClientUpdate(false);
						m_associationTextField.setChangeable(true);
						m_wp.onFlowPerformed(new WorkFlowEvent(this,m_currentCallout,WorkFlowEvent.EVENT_CHANGE));
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

			m_personnelTable = new DiskoTable(new CallOutPersonnelTableModel());
			m_personnelTable.setFillsViewportHeight(true);
			m_personnelTable.addMouseListener(new CallOutPersonnelMouseListener());
			m_personnelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			CallOutPersonnelStatusEditor editor = new CallOutPersonnelStatusEditor();
			m_personnelTable.setColumnSelectionAllowed(false);
			m_personnelTable.setRowHeight(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL).height + 10);

			TableColumn column = m_personnelTable.getColumnModel().getColumn(2);
			column.setCellEditor(editor);
			column.setCellRenderer(editor);
			column.setPreferredWidth(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL).width * 2 + 15);
			column.setMaxWidth(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL).width * 2 + 15);

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
			CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
			model.setPersonnelList(m_wp.getMsoModel(),m_currentCallout.getPersonnelList());
			
		} else {
			getInfoPanel().setCaptionText("Velg varsling i listen");
			m_associationTextField.setValue("");
			CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
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
	public boolean isInputValid()
	{
		if(m_currentCallout != null)
		{
			m_infoPanel.setDirty(false);

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
				if(!e.isLoopback())
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

	/**
	 * Personnel data for current call-out in details panel
	 *
	 * @author thomasl, kennetgu
	 */
	private class CallOutPersonnelTableModel extends AbstractMsoTableModel<IPersonnelIf>
	{
		private static final long serialVersionUID = 1L;

		private static final String NAME = "name";
		private static final String STATUS = "status";
		private static final String EDIT = "edit";

		private IPersonnelListIf m_list;

		/* ===============================================================
		 * Constructors
		 * =============================================================== */

		public CallOutPersonnelTableModel()
		{
			// forward
			super(IPersonnelIf.class,false);
			// create table
			create(getNames(),getCaptions());
		}

		/* ===============================================================
		 * MsoTableModel implementation
		 * =============================================================== */

		protected Object getCellValue(int row, String column) {
			// get personnel
			IPersonnelIf personnel = getId(row);
			// translate
			if(NAME.equals(column))
				return MsoUtils.getPersonnelName(personnel, false);
			else if(STATUS.equals(column))
				return personnel.getImportStatusText();
			else if(EDIT.equals(column))
				return personnel;
			// not found
			return null;
		}

		protected void cleanup(IUnitIf id, boolean finalize) {
			if(finalize) m_list = null;
		}

		/* ===============================================================
		 * AbstractTableModel implementation
		 * =============================================================== */

		@Override
		public boolean isCellEditable(int row, int column)
		{
			return column == 2;
		}

		/* ===============================================================
		 * Public methods
		 * =============================================================== */

		public IPersonnelListIf getPersonnelList() {
			return m_list;
		}

		public void setPersonnelList(IMsoModelIf model, IPersonnelListIf list)
		{

			// prepare
			m_list = list;

			// install model?
			if(list!=null) {
				connect(model,list,IPersonnelIf.PERSONNEL_NAME_COMPARATOR);
				load(list);
			}
			else {
				disconnectAll();
				clear();
			}
		}

		public IPersonnelIf getPersonnel(int row)
		{
			return getId(row);
		}

		/* ===============================================================
		 * Helper methods
		 * =============================================================== */

		public String[] getNames() {
			return new String[] {NAME, STATUS, EDIT};
		}

		public String[] getCaptions() {
			return new String[] {"Navn", "Status", "Endre"};
		}

	}

	/**
	 * Column editor for call-out personnel status changes
	 *
	 * @author thomasl
	 */
	private class CallOutPersonnelStatusEditor extends AbstractCellEditor
	implements TableCellEditor, TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private JPanel m_panel;
		private JButton m_arrivedButton;
		private JButton m_releaseButton;

		private int m_editingRow;

		public CallOutPersonnelStatusEditor()
		{
			m_panel = new JPanel();
			m_panel.setBackground(m_personnelTable.getBackground());

			String text = m_resources.getString("ArrivedButton.letter");
			String letter = m_resources.getString("ArrivedButton.text");
			m_arrivedButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_arrivedButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					// Set personnel status to arrived
					CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
					int index = m_personnelTable.convertRowIndexToModel(m_editingRow);
					if(index==-1) return;
					IPersonnelIf personnel = (IPersonnelIf)model.getValueAt(index, 2);
					IPersonnelIf newPersonnelInstance = UnitUtils.arrivedPersonnel(personnel);
					if(newPersonnelInstance != personnel)
					{
						// Personnel was reinstated. Replace reference in call-out
						m_currentCallout.getPersonnelList().remove(personnel);
						if(!m_currentCallout.getPersonnelList().exists(newPersonnelInstance))
						{
							m_currentCallout.getPersonnelList().add(newPersonnelInstance);
						}
					}

					if(!(m_wp.isNewCallOut() || m_wp.isNewPersonnel() || m_wp.isNewUnit()))
					{
				        try {
							// Commit right away if no major updates
							m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(m_currentCallout));
						} catch (TransactionException ex) {
							m_logger.error("Failed to commit callout details changes",ex);
						}            
					}
					fireEditingStopped();
					m_personnelTable.repaint();
				}
			});
			m_panel.add(m_arrivedButton);

			text = m_resources.getString("ReleaseButton.letter");
			letter = m_resources.getString("ReleaseButton.text");
			m_releaseButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_releaseButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Release personnel
					CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
					int index = m_personnelTable.convertRowIndexToModel(m_editingRow);
					if(index==-1) return;
					IPersonnelIf personnel = (IPersonnelIf)model.getValueAt(index, 2);
					UnitUtils.releasePersonnel(personnel);
					if(!m_wp.isNewCallOut())
					{
				        try {
							// Commit right away if no major updates
							m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(m_currentCallout));
						} catch (TransactionException ex) {
							m_logger.error("Failed to commit callout details changes",ex);
						}            
					}
					fireEditingStopped();
					m_personnelTable.repaint();
				}
			});
			m_panel.add(m_releaseButton);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
			updateCell(row);
			return m_panel;
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean arg2, int row, int column)
		{
			m_editingRow = row;
			updateCell(row);
			return m_panel;
		}

		public Object getCellEditorValue()
		{
			return null;
		}

		private void updateCell(int row)
		{
			CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
			int index = m_personnelTable.convertRowIndexToModel(row);
			if(index==-1) return;
			IPersonnelIf personnel = (IPersonnelIf)model.getValueAt(index, 2);

			m_arrivedButton.setSelected(personnel.getStatus() == PersonnelStatus.ARRIVED);
			m_releaseButton.setSelected(personnel.getStatus() == PersonnelStatus.RELEASED);
		}
	}

	private class CallOutPersonnelMouseListener implements MouseListener
	{
		public void mouseClicked(MouseEvent me)
		{
			Point clickedPoint = new Point(me.getX(), me.getY());
			int row = m_personnelTable.rowAtPoint(clickedPoint);
			int index = m_personnelTable.convertRowIndexToModel(row);
			if(index==-1) return;
			CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
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
				// Check if unit is new
				if(m_wp.isNewUnit() || m_wp.isNewCallOut())
				{
					// Abort view change
					return;
				}

				// Change to personnel display
				m_wp.setPersonnelLeft(personnel);
				m_wp.setLeftView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
				m_wp.setPersonnelBottom(personnel);
				m_wp.setBottomView(IDiskoWpUnit.PERSONNEL_ADDITIONAL_VIEW_ID);
			}
		}

		public void mouseEntered(MouseEvent arg0){}
		public void mouseExited(MouseEvent arg0){}
		public void mousePressed(MouseEvent arg0){}
		public void mouseReleased(MouseEvent arg0){}
	}
}
