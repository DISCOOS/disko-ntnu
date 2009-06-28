package org.redcross.sar.mso.data;
/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 25.jun.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
public class MessageLineListImpl extends MsoListImpl<IMessageLineIf> implements IMessageLineListIf
{
    public MessageLineListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IMessageLineIf.class, anOwner, theName, isMain);
    }

    public MessageLineListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IMessageLineIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IMessageLineIf createMessageLine()
    {
        checkCreateOp();
        return createdUniqueItem(new MessageLineImpl(getOwner().getModel(), createUniqueId()));
    }

    public IMessageLineIf createMessageLine(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IMessageLineIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new MessageLineImpl(getOwner().getModel(), anObjectId));
    }
}
