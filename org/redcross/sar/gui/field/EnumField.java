package org.redcross.sar.gui.field;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JList;

import org.redcross.sar.Application;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ListSelectorDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.ListSelectorPanel;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoEnum;

/**
 * @author kennetgu
 *
 */
@SuppressWarnings("unchecked")
public class EnumField extends AbstractField<Enum<?>,JFormattedTextField,JTextField> {

	private static final long serialVersionUID = 1L;

	private Enum<?> m_value;
	private Enum<?>[] m_values;
	private ListSelectorPanel m_selectorPanel;
	private ListSelectorDialog m_selectorDialog;

	/*==================================================================
	 * Constructors
	 *================================================================== */

	public EnumField(String name, String caption, boolean isEditable) {
		// forward
		super(name,caption,false);
		// forward
		initialize(null,isEditable);
	}

	public EnumField(String name, String caption, boolean isEditable, Enum<?>[] values) {
		// forward
		super(name,caption,false);
		// forward
		initialize(values,isEditable);
	}

	public EnumField(String name, String caption, boolean isEditable, int width, int height) {
		// forward
		super(name,caption,false,width,height,null);
		// forward
		initialize(null,isEditable);
	}


	public EnumField(String name, String caption, boolean isEditable, int width, int height, Enum<?> value, Enum<?>[] values) {
		// forward
		super(name,caption,false,width,height,null);
		// forward
		initialize(values,isEditable);
	}

	public EnumField(MsoEnum attribute, String caption, boolean isEditable) {
		// forward
		super(attribute, caption, false);
		// forward
		initialize(getAllEnumValues(attribute),isEditable);
	}

	public EnumField(MsoEnum attribute, String caption, Enum<?>[] values,
			boolean isEditable) {
		// forward
		super(attribute, caption, false);
		// forward
		initialize(values,isEditable);
	}

	public EnumField(MsoEnum attribute, String caption,
			boolean isEditable, int width, int height) {
		// forward
		super(attribute, caption, false, width, height);
		// forward
		initialize(getAllEnumValues(attribute),isEditable);
	}

	public EnumField(MsoEnum attribute, String caption,
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
	}

	/* ==================================================================
	 *  anonymous classes
	 * ================================================================== */
	
	private final ActionListener m_actionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// prompt user for selection
			Enum<?> value = (Enum<?>)getSelectorDialog().select();
			// user selected a value?
			if(value!=null) {
				setValue(value);
			}
			else {
				// reset value
				reset();
			}
		}

	};
	
	/* ==================================================================
	 *  Public methods
	 * ================================================================== */

	public JFormattedTextField getEditComponent() {
		if(m_editComponent==null) {
			JFormattedTextField field = new JFormattedTextField();
			field.setEditable(false);
			m_editComponent = field;
		}
		return m_editComponent;
	}

	public JTextField getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createDefaultComponent(false);
		}
		return m_viewComponent;
	}
	
	public Enum<?> getEditValue() {
		return m_value;
	}

	@Override
	protected boolean setNewEditValue(Object value) {
		if(value instanceof Enum) {
			// save
			m_value = (Enum<?>)value;
			// get text
			String text = DiskoEnumFactory.getText((Enum<?>)value);
			// update
			getEditComponent().setText(text);
			getViewComponent().setText(getFormattedText());
			// finished
			return true;
		}
		// failure
		return false;
	}

	@Override
	public String getFormattedText() {
		return getEditComponent().getText();
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
		Enum<?> current = getEditValue();
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

	@Override
	public final void installButton(AbstractButton button, boolean isVisible) {
		// forward
		super.installButton(button, isVisible);
		// handle actions
		getButton().addActionListener(m_actionListener);
		// update selector dialog location
		getSelectorDialog().setSnapToLocation(getButton(), DefaultDialog.POS_WEST, 0, false, false);
	}
	
	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	protected boolean isMsoAttributeSettable(IMsoAttributeIf<?> attr) {
		return (attr instanceof MsoEnum);
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
			m_selectorDialog = new ListSelectorDialog(Application.getFrameInstance());
		}
		return m_selectorDialog;
	}

	private void initalizeEdit() {

		// initialize gui
		installButton(DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.SMALL), true);

	}

	/* ==================================================================
	 *  Private static methods
	 * ================================================================== */

	@SuppressWarnings("unchecked")
	private static Enum[] getAllEnumValues(MsoEnum attribute) {
		Enum value = (Enum)attribute.get();
		if(value!=null) {
			EnumSet set = EnumSet.allOf(value.getClass());
			Enum[] values = new Enum[set.size()];
			set.toArray(values);
			return values;
		}
		return null;
	}
}
