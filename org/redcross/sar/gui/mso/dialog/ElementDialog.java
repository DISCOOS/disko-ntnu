package org.redcross.sar.gui.mso.dialog;

import java.awt.Dimension;
import java.awt.Frame;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.mso.panel.ElementPanel;
import org.redcross.sar.gui.mso.panel.ElementPanel.ElementEvent;
import org.redcross.sar.gui.mso.panel.ElementPanel.IElementEventListener;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;

import javax.swing.JList;

public class ElementDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private ElementPanel elementPanel;

	public ElementDialog(Frame frame) {
		// forward
		super(frame);
		// initialize GUI
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		try {
            this.setPreferredSize(new Dimension(606,440));
            this.setContentPane(getElementPanel());
            getElementPanel().addElementListener(new IElementEventListener() {

				public void onElementSelected(ElementEvent e) {
					// forward
					getElementPanel().requestFitToPreferredContentSize(false);
					// forward
					snapTo();	
				}
            	
				public void onElementCenterAt(ElementEvent e) { /*NOP*/ }
				public void onElementDelete(ElementEvent e) { /*NOP*/ }
				public void onElementEdit(ElementEvent e) { /*NOP*/ }

            });
            this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes elementPanel
	 *
	 * @return javax.swing.JPanel
	 */
	public ElementPanel getElementPanel() {
		if (elementPanel == null) {
			try {
				elementPanel = new ElementPanel();

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return elementPanel;
	}

	public JList getElementList() {
		return getElementPanel().getTypeList();
	}

	public JList getObjectList() {
		return getElementPanel().getObjectList();
	}
	public JList getPartList() {
		return getElementPanel().getPartList();
	}

	public SearchSubType getSelectedSubType() {
		Object selected = getElementList().getSelectedValue();
		if(selected instanceof SearchSubType) {
			return (SearchSubType)selected;
		}
		return null;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
