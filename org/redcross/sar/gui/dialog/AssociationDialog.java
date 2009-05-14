package org.redcross.sar.gui.dialog;

import javax.swing.JTextField;
import javax.swing.WindowConstants;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.redcross.sar.gui.document.AutoCompleteDocument;
import org.redcross.sar.gui.event.IAutoCompleteListener;
import org.redcross.sar.gui.field.ComboBoxField;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.mso.data.IAssociationIf;
import org.redcross.sar.util.AssocUtils;
import org.redcross.sar.util.AssocUtils.Association;

public class AssociationDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;
    
	private boolean m_isCancel = false;

	private FieldsPanel m_contentPanel;

	private TextField m_searchTextField;
	private ComboBoxField m_organizationComboBoxField;
	private ComboBoxField m_divisionComboBoxField;
	private ComboBoxField m_departmentComboBoxField;

    
	/**
	 * @param owner
	 */
	public AssociationDialog(Frame owner) {
		// forward
		super(owner);
		// initialize GUI
		initialize();
	}

	private void initialize() {
        this.setModal(true);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setContentPane(getContentPanel());
        this.setPreferredSize(new Dimension(450,163));
        this.pack();
	}

	/**
	 * This method initializes m_contentPanel
	 *
	 * @return org.redcross.sar.gui.panel.FieldsPanel
	 */
	private FieldsPanel getContentPanel() {
		if (m_contentPanel == null) {
			m_contentPanel = new FieldsPanel("Tilhørighet");
			m_contentPanel.setNotScrollBars();
			m_contentPanel.setPreferredExpandedHeight(150);
			m_contentPanel.addField(getSearchTextField());			
			m_contentPanel.addField(getOrganizationTextField());
			m_contentPanel.addField(getDivisionTextField());
			m_contentPanel.addField(getDepartmentTextField());
			m_contentPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("cancel".equalsIgnoreCase(cmd)) {
						m_isCancel = true;
					}					
				}
				
			});
		}
		return m_contentPanel;
	}
	
    private TextField getSearchTextField() {
		if(m_searchTextField==null) {
			m_searchTextField = new TextField("search","Søk",true);
			JTextField inputField = m_searchTextField.getEditComponent();
			AutoCompleteDocument doc = new AutoCompleteDocument(AssocUtils.getAssociations(),inputField);
			inputField.setDocument(doc);
			doc.addAutoCompleteListener(new IAutoCompleteListener() {

				public void onSuggestionFound(AutoCompleteDocument document, String suggestion) {
					if(suggestion==null) {
						getOrganizationTextField().setValue(null);
						getDivisionTextField().setValue(null);
						getDepartmentTextField().setValue(null);
						
					} else {
						Association[] items = AssocUtils.parse(suggestion,true,false);
						getOrganizationTextField().setValue(items[0].getName(1));
						getDivisionTextField().setValue(items[0].getName(2));
						getDepartmentTextField().setValue(items[0].getName(3));
					}
				}
				
			});
		}
		return m_searchTextField;
	}	

    private ComboBoxField getOrganizationTextField() {
		if(m_organizationComboBoxField==null) {
			m_organizationComboBoxField = new ComboBoxField("organization","Organisasjon",false);    		
			m_organizationComboBoxField.fill(AssocUtils.getOrganizationNames());
			m_organizationComboBoxField.setValue("");
			m_organizationComboBoxField.getEditComponent().addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					String[] divs = null;
					if(e.getItem()!=null) {
						divs = AssocUtils.getDivisionNames(e.getItem().toString(),false);
					}
					m_divisionComboBoxField.fill(divs);
					if(m_divisionComboBoxField.getEditComponent().getItemCount()>0)
						m_divisionComboBoxField.getEditComponent().setSelectedIndex(0);
					else
						m_divisionComboBoxField.getEditComponent().setSelectedIndex(-1);
				}
				
			});
		}
		return m_organizationComboBoxField;
	}

	private ComboBoxField getDivisionTextField() {
		if(m_divisionComboBoxField==null) {
			m_divisionComboBoxField = new ComboBoxField("division","Divisjon",false);
			m_divisionComboBoxField.setValue("");
			m_divisionComboBoxField.getEditComponent().addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					String[] deps = null;
					// get selected item
					if(e.getItem()!=null) {
						Object org = m_organizationComboBoxField.getEditComponent().getSelectedItem();
						if(org!=null) deps = AssocUtils.getDepartmentNames(org.toString(),e.getItem().toString(),false);						
					} else {
					}
					m_departmentComboBoxField.fill(deps);
					if(m_departmentComboBoxField.getEditComponent().getItemCount()>0)
						m_departmentComboBoxField.getEditComponent().setSelectedIndex(0);
					else
						m_departmentComboBoxField.getEditComponent().setSelectedIndex(-1);
				}
				
			});
			
		}
		return m_divisionComboBoxField;
	}
	
	private ComboBoxField getDepartmentTextField() {
		if(m_departmentComboBoxField==null) {
			m_departmentComboBoxField = new ComboBoxField("department","Avdeling",false);    		
			m_departmentComboBoxField.setValue("");
		}
		return m_departmentComboBoxField;
	}    
	
	private void load(String search, IAssociationIf entity) {
		// prepare
		getContentPanel().setChangeable(false);
        if(entity!=null) {
        	getSearchTextField().setValue(search);
        	getOrganizationTextField().setValue(entity.getOrganization());
        	getDivisionTextField().setValue(entity.getDivision());
        	getDepartmentTextField().setValue(entity.getDepartment());
        }  else {
        	getOrganizationTextField().setValue(null);
        	getDivisionTextField().setValue(null);
        	getDepartmentTextField().setValue(null);
        }		
		getContentPanel().setChangeable(true);
        setDirty(false);
	}
	
	private void save(IAssociationIf entity) {
		// prepare
        if(entity!=null&&isDirty()) {
        	entity.suspendClientUpdate();
        	entity.setOrganization(getOrganization());
        	entity.setDivision(getDivision());
        	entity.setDepartment(getDepartment());
        	entity.resumeClientUpdate(false);
        }		
	}
	
	private String getOrganization() {
		Object value = getOrganizationTextField().getValue();
		return value!=null?value.toString():null;
	}
	
	private String getDivision() {
		Object value = getDivisionTextField().getValue();
		return value!=null?value.toString():null;
	}

	private String getDepartment() {
		Object value = getDepartmentTextField().getValue();
		return value!=null?value.toString():null;
	}

	public boolean associate(String search, IAssociationIf entity) {
		// prepare
		m_isCancel = false;
		// forward
		load(search,entity);
		// show dialog
		super.setVisible(true);
		// save?
		if(!m_isCancel) save(entity);
		// finished
		return !m_isCancel;
	}
	
	


}  //  @jve:decl-index=0:visual-constraint="10,10"
