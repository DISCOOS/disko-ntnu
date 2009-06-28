package org.redcross.sar.mso.data;

public class PersonnelListImpl extends MsoListImpl<IPersonnelIf> implements IPersonnelListIf
{

    public PersonnelListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IPersonnelIf.class, anOwner, theName, isMain);
    }

    public PersonnelListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IPersonnelIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IPersonnelIf createPersonnel()
    {
        checkCreateOp();
        return createdUniqueItem(new PersonnelImpl(getOwner().getModel(), createUniqueId()));
    }

    public IPersonnelIf createPersonnel(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IPersonnelIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new PersonnelImpl(getOwner().getModel(), anObjectId));
    }
}