package org.redcross.sar.mso.data;

public class EquipmentListImpl extends MsoListImpl<IEquipmentIf> implements IEquipmentListIf
{

    public EquipmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IEquipmentIf.class, anOwner, theName, isMain);
    }

    public EquipmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IEquipmentIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IEquipmentIf createEquipment()
    {
        checkCreateOp();
        return createdUniqueItem(new EquipmentImpl(getOwner().getModel(), makeUniqueId()));
    }

    public IEquipmentIf createEquipment(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IEquipmentIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new EquipmentImpl(getOwner().getModel(), anObjectId));
    }


}