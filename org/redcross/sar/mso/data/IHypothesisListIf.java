package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

/**
 *
 */
public interface IHypothesisListIf extends IMsoListIf<IHypothesisIf>
{
    public IHypothesisIf createHypothesis();

    public IHypothesisIf createHypothesis(IMsoObjectIf.IObjectIdIf anObjectId);
}
