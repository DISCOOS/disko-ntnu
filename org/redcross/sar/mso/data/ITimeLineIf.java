package org.redcross.sar.mso.data;

import java.util.List;

public interface ITimeLineIf extends IMsoDerivedListIf<ITimeItemIf>
{
    public List<ITimeItemIf> getTimeItems();

    public void print();
}