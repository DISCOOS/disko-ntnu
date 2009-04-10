/**
 *
 */
package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;

import java.util.Collection;

@SuppressWarnings("unchecked")
public class OperationImpl extends AbstractMsoObject implements IOperationIf
{
    private final AttributeImpl.MsoString m_opNumber = new AttributeImpl.MsoString(this, "OpNumber");
    private final AttributeImpl.MsoString m_opNumberPrefix = new AttributeImpl.MsoString(this, "OpNumberPrefix");

    private final MsoReferenceImpl<ISystemIf> m_system = new MsoReferenceImpl<ISystemIf>(this, "System", 1, false);

    private final CmdPostListImpl m_cmdPostList = new CmdPostListImpl(this, "CmdPostList", true);

    public OperationImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, String aNumberPrefix, String aNumber)
    {
        super(theMsoModel, anObjectId);
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
    	addReference(m_system);
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
            return m_cmdPostList.remove((ICmdPostIf)anObject);
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
     * Methods for references
     *-------------------------------------------------------------------------------------------*/

    public void setSystem(ISystemIf aSystem)
    {
        m_system.setReference(aSystem);
    }

    public ISystemIf getSystem()
    {
        return m_system.getReference();
    }

    public IMsoModelIf.ModificationState getSystemState()
    {
        return m_system.getState();
    }

    public IMsoReferenceIf<ISystemIf> getSystemAttribute()
    {
        return m_system;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addCmdPost(ICmdPostIf anICmdPostIf)
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
        return null; /*TODO: Implementer*/
    }

    @Override
    public boolean delete()
    {
        MsoModelImpl.getInstance().suspendClientUpdate();
        m_cmdPostList.deleteAll();
        doDelete();
        MsoModelImpl.getInstance().resumeClientUpdate(true);
        return true;
    }

    public ISystemIf createSystem() {
    	IObjectIdIf id = MsoModelImpl.getInstance().getModelDriver().makeObjectId();
        return createSystem(id);
    }

    public ISystemIf createSystem(IObjectIdIf id) {
    	AbstractMsoObject msoObj = new SystemImpl(m_msoModel, id);
    	msoObj.setup(true);
        return (ISystemIf)msoObj;
    }

}
