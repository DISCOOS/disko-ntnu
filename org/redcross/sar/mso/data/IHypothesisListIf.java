package org.redcross.sar.mso.data;

public interface IHypothesisListIf extends IMsoListIf<IHypothesisIf>
{
    public IHypothesisIf createHypothesis();

    public IHypothesisIf createHypothesis(IMsoObjectIf.IObjectIdIf anObjectId);
}
