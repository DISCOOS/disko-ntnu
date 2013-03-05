package org.redcross.sar.gui.field;

import org.redcross.sar.util.AssocUtils;
import org.redcross.sar.util.AssocUtils.Association;

/**
 * Class that parse association string into association objects, 
 * and formats association objects into string. 
 *    
 * @author kenneth
 *
 */

public class AssocFieldParser implements IFieldParser<String,Object[]> {

	@Override
	public String format(Object[] values) {
        if(values[0]!=null) {
            Association assoc = AssocUtils.getOrganization(getText(values[0]));
            //assoc.setPattern("{1:n}");
            if(values[1]!=null) {
            	assoc = AssocUtils.getDivision(assoc,getText(values[1]));
	            //assoc.setPattern("{2:n} {2:s}");
                if(values[2]!=null) { 
                	assoc = AssocUtils.getDepartment(assoc,getText(values[2]));
		            //assoc.setPattern("{3:n} {3:s}");
                }
            }
            return assoc.getText();
    	} else {
            return null;
    	}
	}

	@Override
	public String[] parse(String value) {
		String[] values = new String[3];
		if(value!=null) 
		{
			Association[] items = AssocUtils.parse(value,false,false);
			if(items!=null && items.length>0)
			{
				int i=2;
				Association assoc = items[0];
				while(assoc!=null)
				{
					values[i--] = assoc.getText();
					assoc = assoc.getParent();
				}
			}
		}
		return values;
	}
	
	private String getText(Object value) {
		return value!=null?value.toString():null;
	}
	
}
