package org.redcross.sar.wp.unit;

import org.apache.log4j.Logger;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.work.event.FlowEvent;
import org.redcross.sar.wp.unit.UnitDetailsPanel.UnitPersonnelTableModel;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

/**
 * Implements drag and drop for unit/personnel assignment
 *
 * @author thomasl
 */
public class PersonnelTransferHandler extends TransferHandler
{
	private static final long serialVersionUID = 1L;
	private static final Logger m_logger = Logger.getLogger(PersonnelTransferHandler.class);

	private static DataFlavor m_personnelFlavor;

	private static IDiskoWpUnit m_wp;

	public PersonnelTransferHandler(IDiskoWpUnit wp) throws ClassNotFoundException
	{
		if (m_personnelFlavor == null)
		{
			m_personnelFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.redcross.sar.mso.data.IPersonnelIf");
		}

		m_wp = wp;
	}

	/**
	 * Exports personnel from {@link PersonnelTableModel} or {@link UnitPersonnelTableModel}, depending
	 * on which way the user drags.
	 */
	@Override
	protected Transferable createTransferable(JComponent c)
	{
		JTable table = (JTable)c;
		int selectedRow = table.getSelectedRow();
		TableModel model = table.getModel();
		IPersonnelIf personnel = null;
		if(model instanceof PersonnelTableModel)
		{
			// convert to model index
			selectedRow = table.convertRowIndexToModel(selectedRow);
			// get personnel object from selection
			personnel = ((PersonnelTableModel)model).getPersonnel(selectedRow);
		}
		else if(model instanceof UnitPersonnelTableModel)
		{
			// convert to model index
			selectedRow = table.convertRowIndexToModel(selectedRow);
			// get personnel object from selection
			personnel = ((UnitPersonnelTableModel)model).getPersonnel(selectedRow);
		}

		PersonnelTransferable transferable = new PersonnelTransferable(personnel);
		return transferable;
	}

	@Override
	public boolean canImport(TransferSupport support)
	{
		DataFlavor[] flavors = support.getDataFlavors();
        boolean flavorOk = false;

        for (DataFlavor flavor : flavors)
        {
            if (m_personnelFlavor.equals(flavor))
            {
                flavorOk = true;
                break;
            }
        }
        if (!flavorOk)
        {
            return false;
        }

        // Always allowed to remove personnel from a unit (?)
        JTable targetTable = (JTable)support.getComponent();
        if(targetTable.getModel() instanceof PersonnelTableModel)
        {
        	return true;
        }

		// Check for valid personnel transfer. Only applicable when transferring to a unit
        try
		{
			IPersonnelIf personnel = (IPersonnelIf)support.getTransferable().getTransferData(m_personnelFlavor);
			return ResourceUtils.canAssignPersonnelToUnit(personnel);
		}
        catch (UnsupportedFlavorException e)
		{
			e.printStackTrace();
		}
        catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}

	/**
	 * Imports personnel from personnel overview table to unit personnel table
	 */
	@Override
	public boolean importData(TransferSupport support)
	{
		if(canImport(support))
		{
			Transferable transferable = support.getTransferable();
			JTable table = (JTable)support.getComponent();
			TableModel model = table.getModel();

			if(model instanceof UnitPersonnelTableModel)
			{
				// cast to UnitPersonnelTableModel 
				UnitPersonnelTableModel unitModel = (UnitPersonnelTableModel)model;
				
				try
				{
					// get personnel
					IPersonnelIf personnel = (IPersonnelIf)transferable.getTransferData(m_personnelFlavor);

					// get current unit
                    IUnitIf aUnit = m_wp.getEditingUnit();

                    // import possible?
					if(aUnit==null || aUnit.getUnitPersonnel().exists(personnel))
					{
						// Dont't import if already in list
						return false;
					}
					else
					{
						// add to unit
						aUnit.addUnitPersonnel(personnel);

						// notify
						m_wp.onFlowPerformed(new FlowEvent(this,aUnit,FlowEvent.EVENT_CHANGE));
						
					}
					// refresh table
					unitModel.fireTableDataChanged();
					// success
					return true;
				}
				catch (Exception e)
				{
					m_logger.error("Failed to transfer personnel",e);
				}
			}
		}
		return false;
	}

	/**
	 * Removes personnel from unit if source of a completed drag was the unit personnel table
	 */
	@Override
	protected void exportDone(JComponent c, Transferable data, int action)
	{
		if(action == TransferHandler.NONE)
		{
			return;
		}

		if(c instanceof JTable)
		{
			JTable table = (JTable)c;
			TableModel model = table.getModel();
			if(model instanceof UnitPersonnelTableModel)
			{
				// cast to UnitPersonnelTableModel 
				UnitPersonnelTableModel unitModel = (UnitPersonnelTableModel)model;
				
				try
				{
					// get exported personnel
					IPersonnelIf personnel = (IPersonnelIf)data.getTransferData(m_personnelFlavor);

					// get current unit
                    IUnitIf aUnit = m_wp.getEditingUnit();

                    // is a unit selected?
					if(aUnit!=null) {

						// remove personnel from unit
						aUnit.removeUnitPersonnel(personnel);

						// notify
						m_wp.onFlowPerformed(new FlowEvent(this,aUnit,FlowEvent.EVENT_CHANGE));

					}
					// refresh table
					unitModel.fireTableDataChanged();
				}
				catch (Exception e)
				{
					m_logger.error("Failed to transfer personnel",e);
				}
			}
		}
    }

	/**
	 * Defines a transferable personnel class
	 *
	 * @author thomasl
	 */
	class PersonnelTransferable implements Transferable
	{
		private IPersonnelIf m_personnel;

		public PersonnelTransferable(IPersonnelIf personnel)
		{
			m_personnel = personnel;
		}

		public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
		{
			if (!isDataFlavorSupported(flavor))
			{
				throw new UnsupportedFlavorException(flavor);
			}

			return m_personnel;
		}

		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[]{m_personnelFlavor};
		}

		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return m_personnelFlavor.equals(flavor);
		}

	}
}
