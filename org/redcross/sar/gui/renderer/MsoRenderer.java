package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JLabel;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;

public class MsoRenderer extends JLabel {

	private static final long serialVersionUID = 1L;

	private int options = 0;
	private boolean complete = false;

	public MsoRenderer(int options, boolean complete)
	{
		super.setOpaque(true);
		this.options = options;
		this.complete = complete;
	}

	protected Component getRenderer(Object value) {

		// initialize
		String text = "<Tom>";

		// dispatch object
		if(value instanceof IMsoObjectIf) {
			// cast to IMsoObjectIf
			IMsoObjectIf msoObject = (IMsoObjectIf)value;

			// get name
			if(complete)
				text = MsoUtils.getCompleteMsoObjectName(msoObject, options);
			else
				text = MsoUtils.getMsoObjectName(msoObject, options);

		}
		else if(value instanceof Enum) {
			text = DiskoEnumFactory.getText((Enum<?>)value);
		}

		// update text
		setText(text);

		// finished
		return this;
	}

	/* =======================================================
	 * Increased performance (See DefaultTableCellRenderer).
	 * ======================================================= */

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) { /* NOP */ }

	@Override
	public void firePropertyChange(String propertyName, char oldValue,
			char newValue) { /* NOP */ }

	@Override
	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) { /* NOP */ }

	@Override
	public void revalidate() { /* NOP */ }

	@Override
	public void repaint() { /* NOP */ }

	@Override
	public void repaint(int x, int y, int width, int height) { /* NOP */ }

	@Override
	public void repaint(long tm) { /* NOP */ }

	@Override
	public void validate() { /* NOP */ }

}

