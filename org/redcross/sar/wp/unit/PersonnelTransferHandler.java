package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
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

	private static DataFlavor m_personnelFlavor;

	private static IDiskoWpUnit m_wpUnit;

	public PersonnelTransferHandler(IDiskoWpUnit wp) throws ClassNotFoundException
	{
		if (m_personnelFlavor == null)
		{
			m_personnelFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.redcross.sar.mso.data.IPersonnelIf");
		}

		m_wpUnit = wp;
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
			// Exporting from personnel overview table
			personnel = (IPersonnelIf)table.getValueAt(selectedRow, 2);
		}
		else if(model instanceof UnitPersonnelTableModel)
		{
			// Exporting from unit personnel table
			personnel = (IPersonnelIf)table.getValueAt(selectedRow, 2);
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
			return PersonnelUtilities.canAssignPersonnelToUnit(personnel);
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
				// Unit personnel table is transfer target
				UnitPersonnelTableModel unitModel = (UnitPersonnelTableModel)model;
				// Add personnel to unit personnel list
				try
				{
					IPersonnelIf personnel = (IPersonnelIf)transferable.getTransferData(m_personnelFlavor);
					IPersonnelListIf personnelList = unitModel.getPersonnelList();
					if(personnelList==null || personnelList.exists(personnel))
					{
						// Dont't import if already in list
						return false;
					}
					else
					{
						personnelList.add(personnel);

						// Commit if no new major changes exists
						if(!(m_wpUnit.getNewCallOut() || m_wpUnit.getNewPersonnel() || m_wpUnit.getNewUnit()))
						{
							m_wpUnit.getMsoModel().commit();
						}
					}
					unitModel.fireTableDataChanged();
				}
				catch (UnsupportedFlavorException e)
				{
					e.printStackTrace();
					return false;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					return false;
				}
			}

			return true;
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
				// Drag source was unit personnel table
				UnitPersonnelTableModel unitModel = (UnitPersonnelTableModel)model;
				try
				{
					IPersonnelIf personnel = (IPersonnelIf)data.getTransferData(m_personnelFlavor);
					unitModel.getPersonnelList().remove(personnel);
					unitModel.fireTableDataChanged();
				}
				catch (UnsupportedFlavorException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		// Commit changes right away
		if(!(m_wpUnit.getNewCallOut() || m_wpUnit.getNewPersonnel() || m_wpUnit.getNewUnit()))
		{
			m_wpUnit.getMsoModel().commit();
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
