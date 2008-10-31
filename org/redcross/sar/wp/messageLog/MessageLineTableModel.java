package org.redcross.sar.wp.messageLog;

import org.redcross.sar.mso.data.*;
import org.redcross.sar.gui.model.AbstractMsoTableModel;

import java.util.LinkedList;
import java.util.List;

/**
 * Table model for displaying message lines at top level edit panel
 *
 * @author thomasl
 */
public class MessageLineTableModel extends AbstractMsoTableModel<IMessageLineIf>
{
	private final static long serialVersionUID = 1L;

	public static final String NAME = "name";
	
	public static final String[] NAMES = new String[] { NAME };
	public static final String[] CAPTIONS = new String[] { "Meldingslinje" };	
	
	protected List<IMessageLineIf> m_lines = new LinkedList<IMessageLineIf>();

	/* =========================================================
	 * Constructors
	 * ========================================================= */
	
	public MessageLineTableModel()
	{
		// forward
		super(IMessageLineIf.class,NAMES,CAPTIONS,false);
	}

	/* ================================================================
	 *  MsoTableModel implementation
	 * ================================================================ */

	protected Object getCellValue(int row, String column) {
		
		// translate
		if(NAME.equals(column))
			return getId(row);
		
		// not supported
		return null;

	}	
	
	/* =========================================================
	 * AbstractTableModel implementation
	 * ========================================================= */
	
	@Override
	public Class<?> getColumnClass(int col) {
		// translate column index to class
		switch(col) {
		case 0: return IMessageLineIf.class;
		default: return Object.class;
		}
	}
	
	/* =========================================================
	 * Public methods
	 * ========================================================= */

	/**
	 * Get a message line from the list model
	 * @param int row - The message line row index
	 * @return IMessageLineIf
	 */
	public IMessageLineIf getMessageLine(int row)
	{
		return getId(row);
	}

}
