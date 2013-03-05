package org.redcross.sar.mso.data;

public interface ICalloutListIf extends IMsoListIf<ICalloutIf>
{

    public ICalloutIf createCallout();

    public ICalloutIf createCallout(IMsoObjectIf.IObjectIdIf anObjectId);

}