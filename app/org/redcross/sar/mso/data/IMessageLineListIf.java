package org.redcross.sar.mso.data;

/**
 * User: vinjar
 * Date: 25.jun.2007
 */

/**
 *
 */
public interface IMessageLineListIf extends IMsoListIf<IMessageLineIf>
{
    public IMessageLineIf createMessageLine();

    public IMessageLineIf createMessageLine(IMsoObjectIf.IObjectIdIf anObjectId);


}
