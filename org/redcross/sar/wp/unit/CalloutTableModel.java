package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.ICalloutListIf;
import org.redcross.sar.mso.data.ICmdPostIf;
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
public class CalloutTableModel extends AbstractTableModel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;

	private IDiskoWpModule m_wpModule;

	public CalloutTableModel(IDiskoWpUnit wp)
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
	
	public boolean hasInterestIn(IMsoObjectIf msoObject, UpdateMode mode) 
	{
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
		return interestedIn.contains(msoObject.getMsoClassCode());
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Varsling";
		case 1:
			return "DTG";
		default:
			return null;
		}
	}

	public int getColumnCount()
	{
		return 2;
	}

	public int getRowCount()
	{
		ICmdPostIf cmdPost = m_wpModule.getCmdPost();
		return (cmdPost!=null ? cmdPost.getCalloutList().size() : 0);
	}

	public Object getValueAt(int rowIndex, int columnIndex)
	{
		ICmdPostIf cmdPost = m_wpModule.getCmdPost();
		if(cmdPost==null) return null;
		ICalloutListIf callouts = m_wpModule.getCmdPost().getCalloutList();
		Object[] data = (Object[])callouts.getItems().toArray();
		if(data==null || data.length<=rowIndex) return null;
		ICalloutIf callout = (ICalloutIf)data[rowIndex];
		switch(columnIndex)
		{
		case 0:
			return DTG.CalToDTG(callout.getCreated());
		case 1:
			return callout.getTitle();
		case 2:
			return callout;
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
		ICmdPostIf cmdPost = m_wpModule.getCmdPost();		
		return (cmdPost!=null ? (ICalloutIf)cmdPost.getCalloutList().getItems().toArray()[index] : null);
	}

}
