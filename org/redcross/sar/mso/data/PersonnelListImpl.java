package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class PersonnelListImpl extends MsoListImpl<IPersonnelIf> implements IPersonnelListIf
{

    public PersonnelListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public PersonnelListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, aSize);
    }

    public IPersonnelIf createPersonnel()
    {
        checkCreateOp();
        return createdUniqueItem(new PersonnelImpl(makeUniqueId()));
    }

    public IPersonnelIf createPersonnel(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IPersonnelIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new PersonnelImpl(anObjectId));
    }
}