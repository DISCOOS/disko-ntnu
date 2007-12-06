package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public interface IIntelligenceListIf extends IMsoListIf<IIntelligenceIf>
{
    public IIntelligenceIf createIntelligence();

    public IIntelligenceIf createIntelligence(IMsoObjectIf.IObjectIdIf anObjectId);

}