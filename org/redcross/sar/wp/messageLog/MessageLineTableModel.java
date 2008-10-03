package org.redcross.sar.wp.messageLog;

import org.redcross.sar.mso.data.*;

import org.redcross.sar.gui.model.DiskoTableModel;
import javax.swing.table.TableModel;
import java.util.LinkedList;
import java.util.List;

/**
 * Table model for displaying message lines at top level edit panel
 *
 * @author thomasl
 */
public class MessageLineTableModel extends DiskoTableModel
{
	private final static long serialVersionUID = 1L;

	protected List<IMessageLineIf> m_messageLines = null;
	protected IDiskoWpMessageLog m_wpMessageLog = null;

	/**
	 * @param wp Message log work process
	 */
	public MessageLineTableModel(IDiskoWpMessageLog wp)
	{
		m_messageLines = new LinkedList<IMessageLineIf>();

		m_wpMessageLog = wp;
	}

	/**
	 * Returns number of columns, always 1
	 */
	public int getColumnCount()
	{
		return 1;
	}

	/**
	 *
	 */
	@Override
	public String getColumnName(int column)
	{
		return null;
	}

	/**
	 * Returns number of rows, which is the number of message lines in the model
	 */
	public int getRowCount()
	{
		return m_messageLines.size();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		// translate column index to class
		switch(col) {
		case 0: return IMessageLineIf.class;
		default: return Object.class;
		}
	}
	
	/**
	 * {@link TableModel#getValueAt(int, int)}
	 */
	public Object getValueAt(int row, int col)
	{
		if(!(m_messageLines.isEmpty() || row>=m_messageLines.size()))
		{
			// get message line
			return m_messageLines.get(row);
		}
		
		// failed
		return null;
	}
	
	/*
	private String getMessageText(IMessageLineIf line) {
		
		// initialize
		String lineText = null;
		
		// dispatch message line type
		switch(line.getLineType())
		{
		case TEXT:
		{
			lineText = String.format(m_wpMessageLog.getBundleText("ListItemText.text"),
					line.getLineText());
		}
		break;
		case POSITION:
		{
			// get posiyion
			Position p = line.getLinePosition();
			
			if(p != null)
			{
				// get unit name
				String unit = MsoUtils.getUnitName(line.getLineUnit(),false);

				try {
					String mgrs = MapUtil.getMGRSfromPosition(p);
					// get zone
					String zone = mgrs.subSequence(0, 3).toString();
					String square = mgrs.subSequence(3, 5).toString();
					String x = mgrs.subSequence(5, 10).toString();
					String y = mgrs.subSequence(10, 15).toString();
					// get text
					lineText = String.format(m_wpMessageLog.getBundleText("ListItemPOI.text"),
							unit, zone, square, x, y, DTG.CalToDTG(line.getOperationTime()));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		break;
		case POI:
		{
			IPOIIf poi = line.getLinePOI();
			if(poi != null)
			{
				String type = poi.getTypeText();
				Position pos = line.getLinePOI().getPosition();
				if(pos != null)
				{
					try {
						String mgrs = MapUtil.getMGRSfromPosition(pos);
						// get zone
						String zone = mgrs.subSequence(0, 3).toString();
						String square = mgrs.subSequence(3, 5).toString();
						String x = mgrs.subSequence(5, 10).toString();
						String y = mgrs.subSequence(10, 15).toString();
						// get text
						lineText = String.format(m_wpMessageLog.getBundleText("ListItemFinding.text"),
								type, zone, square, x, y);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		break;
		case Allocated:
		{
			IAssignmentIf assignment = line.getLineAssignment();
			lineText = String.format(m_wpMessageLog.getBundleText("ListItemAllocated.text"),
					MsoUtils.getAssignmentName(assignment,1), DTG.CalToDTG(line.getOperationTime()));

		}
		break;
		case STARTED:
		{
			IAssignmentIf assignment = line.getLineAssignment();
			lineText = String.format(m_wpMessageLog.getBundleText("ListItemStarted.text"),
					MsoUtils.getAssignmentName(assignment,1), DTG.CalToDTG(line.getOperationTime()));
		}
		break;
		case COMPLETE:
		{
			IAssignmentIf assignment = line.getLineAssignment();
			lineText = String.format(m_wpMessageLog.getBundleText("ListItemCompleted.text"),
					MsoUtils.getAssignmentName(assignment,1), DTG.CalToDTG(line.getOperationTime()));
		}
		break;
		}

		return lineText;		
	}
 	*/
	
	/**
	 * Remove all message lines from line list model
	 */
	public void clearMessageLines()
	{
		m_messageLines.clear();
	}

	/**
	 * Add a message line to the list model
	 * @param messageLine
	 */
	public void addMessageLine(IMessageLineIf messageLine)
	{
		m_messageLines.add(messageLine);
	}

	/**
	 * Get a message line from the list model
	 * @param index Line number
	 * @return The message line
	 */
	public IMessageLineIf getMessageLine(int index)
	{
		return m_messageLines.get(index);
	}

}
