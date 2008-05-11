package org.redcross.sar.gui.map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;

import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.renderers.SimpleListCellRenderer;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.util.Internationalization;

public class POITypesPanel extends DiskoPanel {

	private static final long serialVersionUID = 1L;

	private JList typeList = null;

	public POITypesPanel() {
		
		// initialize gui
		initialize();
		
	}

	public POITypesPanel(String caption) {
		
		// initialize gui
		initialize();
		
		// force caption
		setCaptionText(caption);
		
	}
	
	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		try {
			this.setCaptionText("Velg type punkt");
			this.setBodyComponent(getTypeList());
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	public void reset() {
		if(getTypeList().getModel().getSize()>0)
			getTypeList().setSelectedIndex(0);
	}

	public POIType[] getTypes() {
		ListModel model = getTypeList().getModel();
		POIType[] types = new POIType[model.getSize()];
		for (int i = 0; i < model.getSize(); i++) {
			types[i]=(POIType)model.getElementAt(i);
		}
		return types;
	}
	
	public void setTypes(POIType[] poiTypes) {
		DefaultListModel model = new DefaultListModel();
		POIType current = (POIType)getTypeList().getSelectedValue();
		if(poiTypes!=null) {
			for (int i = 0; i < poiTypes.length; i++) {
				model.addElement(poiTypes[i]);
			}
		}
		getTypeList().setModel(model);
		if(current!=null)
			getTypeList().setSelectedValue(current,true);
		else
			if(model.getSize()>0)
				getTypeList().setSelectedIndex(0);
	}

	public POIType getPOIType() {
		return (POIType)getTypeList().getSelectedValue();
	}
	
	public void setPOIType(POIType type) {
		if((POIType)getTypeList().getSelectedValue()!=type) {
			getTypeList().setSelectedValue(type,true);
		}
	}
	
	/**
	 * This method initializes typeList	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JList getTypeList() {
		if (typeList == null) {
            typeList = new JList();
            typeList.setVisibleRowCount(4);
            typeList.setCellRenderer(new SimpleListCellRenderer(Internationalization.getBundle(IPOIIf.class)));
		}
		return typeList;
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
