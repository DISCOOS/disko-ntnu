package org.redcross.sar.gui.field;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JList;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ListSelectorDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.ListSelectorPanel;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoEnum;
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
public class EnumAttribute extends AbstractDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	private Enum<?> m_value;
	private Enum<?>[] m_values;
	private ListSelectorPanel m_selectorPanel;
	private ListSelectorDialog m_selectorDialog;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */
	
	public EnumAttribute(String name, String caption, boolean isEditable) {
		// forward
		super(name,caption,false);
		// forward
		initialize(null,isEditable);
	}
	
	public EnumAttribute(String name, String caption, boolean isEditable, Enum<?>[] values) {
		// forward
		super(name,caption,false);
		// forward
		initialize(values,isEditable);
	}
	
	public EnumAttribute(String name, String caption, boolean isEditable, int width, int height) {
		// forward
		super(name,caption,false,width,height,null);
		// forward
		initialize(null,isEditable);
	}
	
	
	public EnumAttribute(String name, String caption, boolean isEditable, int width, int height, Enum<?> value, Enum<?>[] values) {
		// forward
		super(name,caption,false,width,height,null);
		// forward
		initialize(values,isEditable);
	}
	
	public EnumAttribute(MsoEnum<?> attribute, String caption, boolean isEditable) {
		// forward
		super(attribute, caption, false);
		// forward
		initialize(getAllEnumValues(attribute),isEditable);
	}

	public EnumAttribute(MsoEnum<?> attribute, String caption, Enum<?>[] values,
			boolean isEditable) {
		// forward
		super(attribute, caption, false);
		// forward
		initialize(values,isEditable);
	}
	
	public EnumAttribute(MsoEnum<?> attribute, String caption, 
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, false, width, height);
		// forward
		initialize(getAllEnumValues(attribute),isEditable);
	}
	
	public EnumAttribute(MsoEnum<?> attribute, String caption, 
			boolean isEditable, int width, int height, Enum<?>[] values) {
		// forward
		super(attribute, caption, false, width, height);
		// forward
		initialize(values,isEditable);
	}
	
	private void initialize(Enum<?>[] values, boolean isEditable) {
		// fill values
		setValues(values);
		// forward?
		initalizeEdit();	
		// forward
		setEditable(isEditable);		
	}
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */

	public Component getComponent() {
		if(m_component==null) {
			JFormattedTextField field = new JFormattedTextField();
			field.setEditable(false);
			// save the component
			m_component = field;			
		}
		return m_component;
	}

	public JFormattedTextField getTextField() {
		return (JFormattedTextField)m_component;
	}
	
	public void setAutoSave(boolean auto) {
		m_autoSave = auto;
	}
	
	public boolean getAutoSave() {
		return m_autoSave;
	}	
	
	public Enum<?> getValue() {
		return m_value;
	}
	
	public boolean setValue(Object value) {
		if(value instanceof Enum) {
			// save
			m_value = (Enum<?>)value;
			// get text
			String text = DiskoEnumFactory.getText((Enum<?>)value);
			// update
			getTextField().setText(text);
			// notify change?
			if(isChangeable()) fireOnWorkChange();
			// finished
			return true;
		}
		// failure
		return false;
	}

	
	public Enum<?>[] getValues() {
		return m_values;
	}
	
	public void setValues(Enum<?>[] values) {
		// prepare
		m_values = values;
		// get new model
		DefaultListModel model = new DefaultListModel();
		// get list
		JList list = getSelectorPanel().getList();
		// get current selected value
		Enum<?> current = getValue();
		// fill new values?
		if(values!=null) {
			for (int i = 0; i < values.length; i++) {
				model.addElement(values[i]);
			}
		}
		// update list model
		list.setModel(model);
		// reselect
		if(current!=null)
			list.setSelectedValue(current,true);
		else
			// has new values?
			if(model.getSize()>0)
				list.setSelectedIndex(0);
	}
	
	public void setVisibleRowCount(int rows) {
		((JList)m_component).setVisibleRowCount(rows);
	}
	
	public int getVisibleRowCount() {
		return ((JList)m_component).getVisibleRowCount();
	}
	
	public boolean setMsoAttribute(IAttributeIf<?> attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(attribute instanceof AttributeImpl.MsoEnum) {
				// save attribute
				m_attribute = attribute;
				// update name
				setName(m_attribute.getName());
				// success
				return true;
			}
		}
		// failure
		return false;
	}	
	
	@Override
	public void setEditable(boolean isEditable) {
		// forward
		super.setEditable(isEditable);
		// force
		getTextField().setEditable(false);		
	}
	
	/*==================================================================
	 * Private methods
	 *================================================================== 
	 */
	
	private ListSelectorPanel getSelectorPanel() {
		if(m_selectorPanel==null) {
			m_selectorPanel = getSelectorDialog().getListSelectorPanel();
		}
		return m_selectorPanel;
	}
	
	private ListSelectorDialog getSelectorDialog() {
		if(m_selectorDialog==null) {
			m_selectorDialog = new ListSelectorDialog(Utils.getApp().getFrame());
		}
		return m_selectorDialog;
	}
	
	private void initalizeEdit() {
		
		// initialize gui
		installButton(DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.SMALL), true);
		
		// handle actions
		getButton().addActionListener(new ActionListener() {
	
			@Override
			public void actionPerformed(ActionEvent e) {
				getSelectorDialog().setLocationRelativeTo(getButton(), DefaultDialog.POS_WEST, false, false);
				Enum<?> value = (Enum<?>)getSelectorDialog().select();
				if(value!=null) {
					setValue(value);
				}
				else {
					// consume
					setChangeable(false);
					// forward
					setValue(getValue());
					// resume
					setChangeable(true);
				}
			}
			
		});
	}
	
	/*==================================================================
	 * Private static methods
	 *================================================================== 
	 */
	
	@SuppressWarnings("unchecked")
	private static Enum[] getAllEnumValues(MsoEnum attribute) {
		Enum value = attribute.getValue();
		if(value!=null) {
			EnumSet set = EnumSet.allOf(value.getClass());
			Enum[] values = new Enum[set.size()];
			set.toArray(values);
			return values;
		}
		return null;
	}
}
