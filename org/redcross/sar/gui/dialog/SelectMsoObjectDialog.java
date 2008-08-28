/**
 * 
 */
package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.redcross.sar.gui.renderer.MsoIconListCellRenderer;
import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * @author kennetgu
 *
 */
public class SelectMsoObjectDialog extends ListSelectorDialog {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public SelectMsoObjectDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// forward
		initialize();
		
	}
	
	private void initialize() {
		// render 
		getListSelectorPanel().getList().setCellRenderer(new MsoIconListCellRenderer(1,true,"32x32"));
	}
	
	
	public void load(IMsoObjectIf[] objects) {
		// get new model
		DefaultListModel model = new DefaultListModel();
		// get list
		JList list = getListSelectorPanel().getList();
		// get current selected value
		IMsoObjectIf current = (IMsoObjectIf)list.getSelectedValue();
		// fill new values?
		if(objects!=null) {
			for (int i = 0; i < objects.length; i++) {
				model.addElement(objects[i]);
			}
		}
		// update list model
		list.setModel(model);
		// reselect
		if(current!=null)
			list.setSelectedValue(current,true);
		else
			if(model.getSize()>0) list.setSelectedIndex(0);
		
	}
	
	@Override
	public IMsoObjectIf select() {
		return (IMsoObjectIf)super.select();
	}
	
	

}  //  @jve:decl-index=0:visual-constraint="23,0"
