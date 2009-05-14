package org.redcross.sar.mso.data;

public interface IIntelligenceListIf extends IMsoListIf<IIntelligenceIf>
{
    public IIntelligenceIf createIntelligence();

    public IIntelligenceIf createIntelligence(IMsoObjectIf.IObjectIdIf anObjectId);

}