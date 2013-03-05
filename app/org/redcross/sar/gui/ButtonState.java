package org.redcross.sar.gui;

import javax.swing.AbstractButton;

public class ButtonState {

	private Object m_key;

	public boolean useDoClick = true;
	public boolean m_isVisible = false;
	public boolean m_isEnabled = false;
	public boolean m_isSelected = false;
	public boolean m_isColored = false;

	public ButtonState(Object key, AbstractButton button) {
		// prepare
		m_key = key;
		// forward
		save(button);
	}

	public ButtonState(Object key, AbstractButton button, boolean useDoClick) {
		// prepare
		m_key = key;
		// forward
		save(button);
	}

	public Object getKey() {
		return m_key;
	}

	public void save(AbstractButton button) {
		m_isVisible = button.isVisible();
		m_isEnabled = button.isEnabled();
		m_isSelected = button.isSelected();
		if(button.getIcon() instanceof DiskoIcon) {
			m_isColored = ((DiskoIcon)button.getIcon()).isColored();
		}
	}

	public void load(AbstractButton button) {
		// update properties
		button.setVisible(m_isVisible);
		button.setEnabled(m_isEnabled);
		// fire click?
		if (m_isSelected != button.isSelected()) {
			if(useDoClick)
				button.doClick();
			else
				button.setSelected(m_isSelected);
		}
		if(button.getIcon() instanceof DiskoIcon) {
			((DiskoIcon)button.getIcon()).setColored(m_isColored);
		}

	}
};
