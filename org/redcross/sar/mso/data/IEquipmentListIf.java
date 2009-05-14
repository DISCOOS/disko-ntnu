package org.redcross.sar.mso.data;

public interface IEquipmentListIf extends IMsoListIf<IEquipmentIf>
{
    public IEquipmentIf createEquipment();

    public IEquipmentIf createEquipment(IMsoObjectIf.IObjectIdIf anObjectId);

}