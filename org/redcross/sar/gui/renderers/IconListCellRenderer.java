package org.redcross.sar.gui.renderers;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.redcross.sar.app.Utils;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;


public class IconListCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	private int options = 0;
	private String catalog = "48x48";
	
	private HashMap<Enum, Icon> icons = null;

	public IconListCellRenderer(int options, String catalog)
	{
		super.setOpaque(true);
		this.options = options;
		this.catalog = catalog;
		this.icons = new HashMap<Enum, Icon>();
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus)
	{

		// initialize
		Icon icon = null;
		String text = "<Tom>";
		Enum e = null;

		// dispatch object
		if(value instanceof IMsoObjectIf) {
			// cast to IMsoObjectIf
			IMsoObjectIf msoObject = (IMsoObjectIf)value;
			// get name
			text = MsoUtils.getMsoObjectName(msoObject, options);
			// get icon
			if(msoObject instanceof IAreaIf) {
				IAreaIf area = (IAreaIf)msoObject;
				if (area.getOwningAssignment() instanceof ISearchIf) {
					e = ((ISearchIf)area.getOwningAssignment()).getSubType();
				}
			}
			else if(msoObject instanceof IUnitIf) {
				IUnitIf unit = (IUnitIf)msoObject;
				e = unit.getType();
			}
			else {
				e = msoObject.getMsoClassCode();
			}
		}
		else if(value instanceof Enum) { 
			text = Utils.translate(value);
			e = (Enum)value;
		}

		// update text
		setText(text);

		// update icon?
		if(e!=null) {
			// select icon
			icon = selectIcon(e);
			// set icon to label
			setIcon(icon);
		}

		// update selection state
		if (isSelected){
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} 
		else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		return this;
	}

	private Icon selectIcon(Enum e) {
		// initialize
		Icon icon = null;
		// has icon?
		if(icons.containsKey(e)) {
			return icons.get(e);
		}
		else {
			// get icon
			icon = Utils.getEnumIcon(e,catalog);
			// found icon?
			if(icon!=null) {
				icons.put(e,icon);
			}
		}
		return icon;		
	}
}

