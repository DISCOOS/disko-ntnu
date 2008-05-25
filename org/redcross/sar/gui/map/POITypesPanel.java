package org.redcross.sar.gui.map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;

import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.renderers.SimpleListCellRenderer;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.util.Internationalization;

public class POITypesPanel extends DefaultDiskoPanel {

	private static final long serialVersionUID = 1L;

	private static final String SELECTION_ENABLED = "Velg type punkt";
	private static final String SELECTION_DISABLED = "Endring er ikke lov";

	private JList typeList = null;

	public POITypesPanel() {
		this("");
	}

	public POITypesPanel(String caption) {
		
		// forward
		super(caption,false,false);
		
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
			this.setCaptionText(SELECTION_ENABLED);
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

	public POIType[] getPOITypes() {
		ListModel model = getTypeList().getModel();
		POIType[] types = new POIType[model.getSize()];
		for (int i = 0; i < model.getSize(); i++) {
			types[i]=(POIType)model.getElementAt(i);
		}
		return types;
	}
	
	public void setPOITypes(POIType[] poiTypes) {
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
		// ensure selected?
		if(getTypeList().getModel().getSize()>0 && getTypeList().getSelectedIndex()==-1)
			getTypeList().setSelectedIndex(0);
		// return selected value
		return (POIType)getTypeList().getSelectedValue();
	}
	
	public void setPOIType(POIType type) {
		if(type==null)
			getTypeList().setSelectedIndex(-1);
		else
			getTypeList().setSelectedValue(type,true);
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

	public boolean isSelectionAllowed() {
		return getTypeList().isEnabled();
	}
	
	public void setSelectionAllowed(boolean isAllowed) {
		getTypeList().setEnabled(isAllowed);
		setCaptionText(isAllowed ? SELECTION_ENABLED : SELECTION_DISABLED);
	}
	

}  //  @jve:decl-index=0:visual-constraint="10,10"
