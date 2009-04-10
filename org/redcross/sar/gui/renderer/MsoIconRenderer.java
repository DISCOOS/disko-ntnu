package org.redcross.sar.gui.renderer;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.Icon;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IUnitIf;


public class MsoIconRenderer extends MsoRenderer {

	private static final long serialVersionUID = 1L;

	private String catalog = "48x48";
	
	private HashMap<Enum<?>, Icon> icons = null;

	public MsoIconRenderer(int options, boolean complete, String catalog)
	{
		super(options,complete);
		this.catalog = catalog;
		this.icons = new HashMap<Enum<?>, Icon>();
	}

	private Icon selectIcon(Enum<?> e) {
		// initialize
		Icon icon = null;
		// has icon?
		if(icons.containsKey(e)) {
			return icons.get(e);
		}
		else {
			// get icon
			icon = DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),catalog);
			// found icon?
			if(icon!=null) {
				icons.put(e,icon);
			}
		}
		return icon;		
	}	
	
	protected Component getRenderer(Object value) {
		
		// initialize
		Icon icon = null;
		Enum<?> e = null;

		// forward
		super.getRenderer(value);
		
		// get proper enum
		if(value instanceof IMsoObjectIf) {
			
			// cast to IMsoObjectIf
			IMsoObjectIf msoObject = (IMsoObjectIf)value;

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
			e = (Enum<?>)value;
		}

		// update icon?
		if(e!=null) {
			// select icon
			icon = selectIcon(e);
			// set icon to label
			setIcon(icon);
		}
		else {
			setIcon(null);
		}

		// finished
		return this;		
	}	

}

