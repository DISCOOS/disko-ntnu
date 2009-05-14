/**
 *
 */
package org.redcross.sar.gui.field;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoBoolean;


/**
 * @author kennetgu
 *
 */
public class CheckBoxField extends AbstractField<Boolean,JCheckBox,JTextField> {

	private static final long serialVersionUID = 1L;

	/*==================================================================
	 * Constructors
	 *==================================================================  */

	public CheckBoxField(String name, String caption, boolean isEditable,
			int width, int height, Object value) {
		super(name, caption, isEditable, width, height, value);
	}

	public CheckBoxField(String name, String caption, boolean isEditable) {
		super(name, caption, isEditable);
	}

	public CheckBoxField(MsoBoolean attribute, String caption,
			boolean isEditable) {
		super(attribute, caption, isEditable);
	}

	public CheckBoxField(MsoBoolean attribute, String caption, boolean isEditable,
			int width, int height) {
		super(attribute, caption, isEditable, width, height);
	}

	/*==================================================================
	 * Public methods
	 *==================================================================
	 */

	public JCheckBox getEditComponent() {
		if(m_editComponent==null) {
			JCheckBox cb = new JCheckBox();
			m_editComponent = cb;
			m_editComponent.setEnabled(isEditable());
			cb.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					onEditValueChanged();
				}

			});
		}
		return m_editComponent;
	}
	
	public JTextField getViewComponent() {
		if(m_viewComponent==null) {
			m_viewComponent = createDefaultComponent(false);
		}
		return m_viewComponent;
	}
	
	public void setBatchMode(boolean isBatchMode) {
		m_isBatchMode = isBatchMode;
	}

	public boolean isBatchMode() {
		return m_isBatchMode;
	}

	@Override
	public Boolean getEditValue() {
		return ((JCheckBox)m_editComponent).isSelected();
	}

	@Override
	protected boolean setNewEditValue(Object value) {
		boolean bFlag = false;
		if(value instanceof String)
			bFlag = Boolean.valueOf((String)value);
		else if(value instanceof Boolean)
			bFlag = Boolean.valueOf((Boolean)value);
		else {
			// failure
			return false;
		}
		// update
		getEditComponent().setSelected(bFlag);
		getViewComponent().setText(getFormattedText());
		// success
		return true;
	}
	
	@Override
	public String getFormattedText() {
		return DiskoStringFactory.getSelectionText(getEditComponent().isSelected());
	}
	
	/* ====================================================================
	 * Protected methods
	 * ==================================================================== */
	
	protected boolean isMsoAttributeSettable(IMsoAttributeIf<?> attr) {
		return (attr instanceof MsoBoolean);
	}

}
