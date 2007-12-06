/**
 *
 */
package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;

import java.util.Collection;

public class OperationImpl extends AbstractMsoObject implements IOperationIf
{
    private final AttributeImpl.MsoString m_opNumber = new AttributeImpl.MsoString(this, "OpNumber");
    private final AttributeImpl.MsoString m_opNumberPrefix = new AttributeImpl.MsoString(this, "OpNumberPrefix");

    private final CmdPostListImpl m_cmdPostList = new CmdPostListImpl(this, "CmdPostList", true);

    public OperationImpl(IMsoObjectIf.IObjectIdIf anObjectId, String aNumberPrefix, String aNumber)
    {
        super(anObjectId);
        setOpNumberPrefix(aNumberPrefix);
        setOpNumber(aNumber);
    }

    protected void defineAttributes()
    {
        addAttribute(m_opNumber);
        addAttribute(m_opNumberPrefix);
    }

    protected void defineLists()
    {
        addList(m_cmdPostList);
    }

    protected void defineReferences()
    {
    }

    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        if (anObject instanceof ICmdPostIf)
        {
            m_cmdPostList.add((ICmdPostIf)anObject);
            return true;
        }
        return false;
    }

    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        if (anObject instanceof ICmdPostIf)
        {
            return m_cmdPostList.removeReference((ICmdPostIf)anObject);
        }
        return true;
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATION;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setOpNumber(String anOpNumber)
    {
        m_opNumber.setValue(anOpNumber);
    }

    public String getNumber()
    {
        return m_opNumber.getString();
    }

    public IMsoModelIf.ModificationState getOpNumberState()
    {
        return m_opNumber.getState();
    }

    public IAttributeIf.IMsoStringIf getOpNumberAttribute()
    {
        return m_opNumber;
    }

    public void setOpNumberPrefix(String aNumberPrefix)
    {
        m_opNumberPrefix.setValue(aNumberPrefix);
    }

    public String getOpNumberPrefix()
    {
        return m_opNumberPrefix.getString();
    }

    public IMsoModelIf.ModificationState getOpNumberPrefixState()
    {
        return m_opNumberPrefix.getState();
    }

    public IAttributeIf.IMsoStringIf getOpNumberPrefixAttribute()
    {
        return m_opNumberPrefix;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addCmdPostList(ICmdPostIf anICmdPostIf)
    {
        m_cmdPostList.add(anICmdPostIf);
    }

    public ICmdPostListIf getCmdPostList()
    {
        return m_cmdPostList;
    }

    public IMsoModelIf.ModificationState getCmdPostListState(ICmdPostIf anICmdPostIf)
    {
        return m_cmdPostList.getState(anICmdPostIf);
    }

    public Collection<ICmdPostIf> getCmdPostListItems()
    {
        return m_cmdPostList.getItems();
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified functions
    *-------------------------------------------------------------------------------------------*/

    public String getOperationNumber()
    {
        return null; /*todo Implementer*/
    }

    @Override
    public boolean deleteObject()
    {
        MsoModelImpl.getInstance().suspendClientUpdate();
        m_cmdPostList.deleteAll();
        MsoModelImpl.getInstance().resumeClientUpdate();
        return true;
    }
}
