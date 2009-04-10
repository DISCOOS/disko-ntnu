package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public interface ICalloutListIf extends IMsoListIf<ICalloutIf>
{

    public ICalloutIf createCallout();

    public ICalloutIf createCallout(IMsoObjectIf.IObjectIdIf anObjectId);

}