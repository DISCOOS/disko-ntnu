package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.ICalloutListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.wp.IDiskoWpModule;

import javax.swing.table.AbstractTableModel;
import java.util.EnumSet;

/**
 * Provides contents for call-out table in overview mode
 *
 * @author thomasl
 */
public class CalloutOverviewTableModel extends AbstractTableModel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;

	private IDiskoWpModule m_wpModule;

	public CalloutOverviewTableModel(IDiskoWpUnit wp)
	{
		m_wpModule = wp;
		wp.getMsoModel().getEventManager().addClientUpdateListener(this);
	}

	public void handleMsoUpdateEvent(Update e)
	{
		fireTableDataChanged();
	}

	EnumSet<IMsoManagerIf.MsoClassCode> interestedIn =
		EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_CALLOUT);
	public boolean hasInterestIn(IMsoObjectIf msoObject)
	{
		return interestedIn.contains(msoObject.getMsoClassCode());
	}

	@Override
    public String getColumnName(int column)
    {
    	return null;
    }

	public int getColumnCount()
	{
		return 2;
	}

	public int getRowCount()
	{
		return m_wpModule.getCmdPost().getCalloutList().size();
	}

	public Object getValueAt(int rowIndex, int columnIndex)
	{
		ICalloutListIf callouts = m_wpModule.getCmdPost().getCalloutList();
		ICalloutIf callout = (ICalloutIf)callouts.getItems().toArray()[rowIndex];
		switch(columnIndex)
		{
		case 0:
			return DTG.CalToDTG(callout.getCreated());
		case 1:
			return callout.getTitle();
		default:
			return null;
		}
	}

	/**
	 * @param index Index of call-out
	 * @return Call-out at given index
	 */
	public ICalloutIf getCallout(int index)
	{
		ICalloutListIf callouts = m_wpModule.getCmdPost().getCalloutList();
		return (ICalloutIf)callouts.getItems().toArray()[index];
	}

}
