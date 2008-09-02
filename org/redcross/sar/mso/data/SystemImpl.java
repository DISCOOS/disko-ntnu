/**
 *
 */
package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;

import java.util.Collection;

public class SystemImpl extends AbstractMsoObject implements ISystemIf
{
    private final DataSourceListImpl m_dataSourceList = new DataSourceListImpl(this, "DataSourceList", true, 100);

    /*-------------------------------------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------------------------------------*/
    
    public SystemImpl(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(anObjectId);
    }

    /*-------------------------------------------------------------------------------------------
     * Methods that must be implemented
     *-------------------------------------------------------------------------------------------*/
     
	@Override
	protected void defineAttributes() { /* NOP */ }

	@Override
	protected void defineReferences() { /* NOP */ }

	@Override
    protected void defineLists()
    {
        addList(m_dataSourceList);
    }

	@Override
    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        if (anObject instanceof IDataSourceIf)
        {
        	m_dataSourceList.add((IDataSourceIf)anObject);
            return true;
        }
        return false;
    }

    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        if (anObject instanceof IDataSourceIf)
        {
            return m_dataSourceList.remove((IDataSourceIf)anObject);
        }
        return true;
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_SYSTEM;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/


    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public IDataSourceListIf getDataSourceList()
    {
        return m_dataSourceList;
    }

    public IMsoModelIf.ModificationState getDataSourceListState(IDataSourceIf anICmdPostIf)
    {
        return m_dataSourceList.getState(anICmdPostIf);
    }

    public Collection<IDataSourceIf> getDataSourceListItems()
    {
        return m_dataSourceList.getItems();
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified functions
    *-------------------------------------------------------------------------------------------*/

}
