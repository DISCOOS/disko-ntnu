/**
 *
 */
package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;

import java.util.Collection;

public class SystemImpl extends AbstractMsoObject implements ISystemIf
{
    private final DataSourceListImpl m_dataSourceList = new DataSourceListImpl(this, "DataSourceList", true, 100);

    /*-------------------------------------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------------------------------------*/

    public SystemImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    /*-------------------------------------------------------------------------------------------
     * Methods that must be implemented
     *-------------------------------------------------------------------------------------------*/

	@Override
	protected void defineAttributes() { /* NOP */ }

	@Override
	protected void defineObjects() { /* NOP */ }

	@Override
    protected void defineLists()
    {
        addList(m_dataSourceList);
    }

	@Override
    public void addListRelation(IMsoObjectIf anObject, String aReferenceListName)
    {
        if (anObject instanceof IDataSourceIf)
        {
        	m_dataSourceList.add((IDataSourceIf)anObject);
        }
    }

    public void removeListRelation(IMsoObjectIf anObject, String aReferenceListName)
    {
        if (anObject instanceof IDataSourceIf)
        {
            m_dataSourceList.remove((IDataSourceIf)anObject);
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
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

    public IData.DataOrigin getDataSourceListState(IDataSourceIf anICmdPostIf)
    {
        return m_dataSourceList.getOrigin(anICmdPostIf);
    }

    public Collection<IDataSourceIf> getDataSourceListItems()
    {
        return m_dataSourceList.getObjects();
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified functions
    *-------------------------------------------------------------------------------------------*/

}
