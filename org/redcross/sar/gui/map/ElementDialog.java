package org.redcross.sar.gui.map;

import java.awt.Dimension;
import java.awt.Frame;

import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;

import javax.swing.JList;

public class ElementDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	
	private ElementPanel elementPanel = null;
	
	public ElementDialog(Frame frame) {
		super(frame);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		try {
            this.setContentPane(getElementPanel());
            this.setPreferredSize(new Dimension(600,530));
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
				// never allow scroll bars, this allows the 
				// lists to manage scrolling
				elementPanel.setScrollBarPolicies(
						DefaultDiskoPanel.VERTICAL_SCROLLBAR_NEVER, 
						DefaultDiskoPanel.HORIZONTAL_SCROLLBAR_NEVER);
				
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
