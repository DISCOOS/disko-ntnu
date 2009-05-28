package org.redcross.sar.gui.renderer;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.redcross.sar.gui.IIconConverter;
import org.redcross.sar.gui.IStringConverter;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;

public class MsoLabelRenderer implements IStringConverter, IIconConverter {

	/**
	 * Map icon to IMsoObject type  
	 */
	public static final int MAP_ICON_TO_TYPE = 0;
	/**
	 * Map icon to IMsoObject sub type if exists. If no sub type exists, map to type instead   
	 */
	public static final int MAP_ICON_TO_SUBTYPE = 1;
	/**
	 * Map icon to IMsoObject status if exists
	 */
	public static final int MAP_ICON_TO_STATUS = 2;
	
	private static final long serialVersionUID = 1L;

	private JLabel label;
	private int optionsName = 0;
	private int mapIconTo = 0;
	private String iconCatalog;
	private boolean showIcon = false;
	private boolean completeName = false;

	private Map<Enum<?>, Icon> icons = new HashMap<Enum<?>, Icon>();
	
	public MsoLabelRenderer(int optionsName, boolean completeName) {
		this(optionsName,completeName,false,"48x48",MAP_ICON_TO_TYPE);
	}
	
	public MsoLabelRenderer(int optionsName, boolean completeName, boolean showIcon, String iconCatalog, int mapIconTo)
	{
		label = UIFactory.createLabelRenderer();
		label.setOpaque(true);
		this.optionsName = optionsName;
		this.completeName = completeName;
		this.showIcon = showIcon;
		this.iconCatalog = iconCatalog;
		this.mapIconTo = mapIconTo;
	}

	public JLabel getRenderer(Object value) {

		// update label
		label.setText(toString(value));
		if(showIcon) 
		{
			label.setIcon(toIcon(value));
		}

		// finished
		return label;
	}

	@Override
	public String toString(Object value) {
		// dispatch object
		if(value instanceof IMsoObjectIf) {
			// cast to IMsoObjectIf
			IMsoObjectIf msoObject = (IMsoObjectIf)value;
			// get name
			if(completeName)
				return MsoUtils.getCompleteMsoObjectName(msoObject, optionsName);
			else
				return MsoUtils.getMsoObjectName(msoObject, optionsName);

		}
		else if(value instanceof Enum) {
			return DiskoEnumFactory.getText((Enum<?>)value);
		}
		return value!=null?value.toString():"";
	}

	@Override
	public Icon toIcon(Object value) {
		
		// initialize
		Enum<?> e = null;
		
		// get proper enum object
		if(value instanceof IMsoObjectIf) {
			// cast to IMsoObjectIf
			IMsoObjectIf msoObject = (IMsoObjectIf)value;
			// translate icon type
			switch(mapIconTo) {
			case MAP_ICON_TO_TYPE:
				e = MsoUtils.getType(msoObject, false);
				break;
			case MAP_ICON_TO_SUBTYPE: 
				e = MsoUtils.getType(msoObject, true);
				break;
			case MAP_ICON_TO_STATUS: 
				e = MsoUtils.getStatus(msoObject);
				break;
			}
		}
		else if(value instanceof Enum) { 
			e = (Enum<?>)value;
		}
		return selectIcon(e); //DiskoIconFactory.getIcon("MAP.POI", "32x32");//  
	}
	
	private Icon selectIcon(Enum<?> e) {
		
		// initialize
		Icon icon = null;
		
		// valid?
		if(e!=null) {
			// has icon?
			if(icons.containsKey(e)) {
				icon = icons.get(e);
			}
			else {
				// get icon
				icon = DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),iconCatalog);
				// found icon?
				if(icon!=null) {
					icons.put(e,icon);
				}
			}
		}
		return icon; //DiskoIconFactory.getIcon("SEARCH.PATROL",iconCatalog); //icon //  //DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),iconCatalog);
	}
	
}

