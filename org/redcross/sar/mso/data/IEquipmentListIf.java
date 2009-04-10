package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public interface IEquipmentListIf extends IMsoListIf<IEquipmentIf>
{
    public IEquipmentIf createEquipment();

    public IEquipmentIf createEquipment(IMsoObjectIf.IObjectIdIf anObjectId);

}