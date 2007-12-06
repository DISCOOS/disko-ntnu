package org.redcross.sar.mso.data;

import org.redcross.sar.util.mso.Route;

public interface IRouteListIf extends IMsoListIf<IRouteIf>
{
    public IRouteIf createRoute(Route aRoute);

    public IRouteIf createRoute(IMsoObjectIf.IObjectIdIf anObjectId, Route aRoute);

    public IRouteIf createRoute(IMsoObjectIf.IObjectIdIf anObjectId);

}