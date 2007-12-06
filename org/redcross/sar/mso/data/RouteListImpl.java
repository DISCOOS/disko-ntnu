package org.redcross.sar.mso.data;

import org.redcross.sar.util.mso.Route;

public class RouteListImpl extends MsoListImpl<IRouteIf> implements IRouteListIf
{

    public RouteListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, aSize);
    }

    public IRouteIf createRoute(Route aRoute)
    {
        checkCreateOp();
        return createdUniqueItem(new RouteImpl(makeUniqueId(), aRoute));
    }

    public IRouteIf createRoute(IMsoObjectIf.IObjectIdIf anObjectId, Route aRoute)
    {
        checkCreateOp();
        IRouteIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new RouteImpl(anObjectId, aRoute));
    }

    public IRouteIf createRoute(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IRouteIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new RouteImpl(anObjectId));
    }

}