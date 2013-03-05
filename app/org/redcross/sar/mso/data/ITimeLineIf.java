package org.redcross.sar.mso.data;

import java.util.List;

public interface ITimeLineIf extends IMsoCoListIf<ITimeItemIf>
{
    public List<ITimeItemIf> getTimeItems();

    public void print();
}