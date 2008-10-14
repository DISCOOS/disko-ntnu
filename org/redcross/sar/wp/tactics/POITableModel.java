package org.redcross.sar.wp.tactics;

import org.redcross.sar.gui.model.MsoTableModel;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IPOIIf;

public class POITableModel extends MsoTableModel<IPOIIf> {

	private static final long serialVersionUID = 1L;
	
	public static final String SEQNO = "name";
	public static final String TYPE = "type";
	public static final String POSITION = "pos";
	public static final String REMARKS = "edit";
	
	public static final String[] NAMES = new String[] { SEQNO, TYPE, POSITION, REMARKS };
	public static final String[] CAPTIONS = new String[] { "Rekkefølge", "Type", "Posisjon", "Merknad" };
	
	/* ================================================================
	 *  Constructors
	 * ================================================================ */
	
	public POITableModel() {
		// forward
		super(IPOIIf.class,NAMES,CAPTIONS,false);		
	}
	
	/* ================================================================
	 *  MsoTableModel implementation
	 * ================================================================ */

	protected Object getCellValue(int row, String column) {
		
		// translate
		if(SEQNO.equals(column))
			return new Integer(getRowCount()+1);
		else if(TYPE.equals(column))
			return DiskoEnumFactory.getText(getId(row).getType());
		else if(POSITION.equals(column))
			try {
				return MapUtil.formatMGRS(MapUtil.getMGRSfromPosition(getId(row).getPosition(),5),3,5);
			} catch (Exception e) {  
				return "<POS:ERROR>";
			}
		else if(REMARKS.equals(column))
			return getId(row).getRemarks();
		
		// not supported
		return null;

	}
		
	/* ================================================================
	 *  AbstracTableModel implementation
	 * ================================================================ */

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		default:
			return Object.class;
		}
	}

}
