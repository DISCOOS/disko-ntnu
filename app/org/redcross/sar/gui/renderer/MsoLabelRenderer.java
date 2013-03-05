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
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoDataIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoRelationIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoEnum;
import org.redcross.sar.mso.util.MsoUtils;

public class MsoLabelRenderer implements IStringConverter, IIconConverter {

	private static final long serialVersionUID = 1L;

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
	
	private JLabel label;
	private int optionsName = 0;
	private int mapIconTo = 0;
	private String iconCatalog;
	private boolean showIcon = false;
	private boolean completeName = false;

	/*
	private Color m_vBg;					// remote data origin view background 
	private Color m_lBg = Color.YELLOW;		// remote data origin background
	private Color m_cBg = Color.RED;		// conflict data origin change background
	private Color m_sBg = Color.GREEN;		// change in remote data background
	*/
	
	private Map<Enum<?>, Icon> icons = new HashMap<Enum<?>, Icon>();
	
	public MsoLabelRenderer(int optionsName, boolean completeName) {
		this(optionsName,completeName,false,"32x32",MAP_ICON_TO_TYPE);
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
		if(value instanceof IMsoDataIf) 
		{
			
			// cast to IMsoDataIf
			IMsoDataIf msoData = (IMsoDataIf)value;
			
			// translate
			switch(msoData.getDataType()) 
			{
			case OBJECT:
				if(completeName)
					return MsoUtils.getCompleteMsoObjectName((IMsoObjectIf)value, optionsName);
				else
					return MsoUtils.getMsoObjectName((IMsoObjectIf)value, optionsName);
			case ENUM:
				return DiskoEnumFactory.getText(((MsoEnum<?>)value).get());
			case ONTO_RELATION:
			case MANY_RELATION:
				IMsoObjectIf msoObj = ((IMsoRelationIf<?>)value).get();
				return msoObj!=null ? toString(msoObj) : "";
			default:
				value = ((IMsoAttributeIf<?>)value).get();
				return value!=null ? value.toString() : "";
				
			}

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
		else if(value instanceof MsoEnum<?>) { 
			e = ((MsoEnum<?>)value).get();
		}
		else if(value instanceof Enum) { 
			e = (Enum<?>)value;
		}
		return selectIcon(e);   
	}
	
	protected IMsoDataIf getData(Object value) 
	{
		// parse value
		return (value instanceof IMsoDataIf ? (IMsoDataIf)value : null);
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
		return icon; 
	}
	
	/**
	 * Set background given the MSO data origin and state
	 * @param value - the data
	 * @param previous - previous data origin
	 * @param ignore - remote data origin should be ignored if <code>true</code> 
	 * @return Returns current origin if MSO data was found. 
	 */
	/*
	protected DataOrigin setBackground(Object value, DataOrigin previous, boolean ignore) {
		// initialize
		DataOrigin origin = DataOrigin.NONE;
		// get MSO data if exists 
		IMsoDataIf data = getData(value);
		// found MSO data?
		if(data!=null) 
		{			
			// get current data origin
			origin = data.getOrigin();
			// get flags
			boolean bFlag = data.isState(DataState.LOOPBACK) 
				  	|| data.isState(DataState.ROLLBACK);
			// set background
			switch(origin) {
			case LOCAL: 
				label.setBackground(m_lBg);
				break;
			case REMOTE:
				// any change?
				if(!(origin.equals(origin) || bFlag)) {
					// set server change indication
					label.setBackground(m_sBg);
				} else if(ignore){
					label.setBackground(m_vBg);
				}
				break;
			case CONFLICT:
				label.setBackground(m_cBg);
				break;
			default:
				label.setBackground(m_vBg);			
				break;
			}
		}
		// finished
		return origin;
	}
	*/
	
}

