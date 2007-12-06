package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.mso.IGeodataIf;

import java.util.Collection;
import java.util.Iterator;

public interface IAreaIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IAttributeIf.IMsoStringIf getRemarksAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addAreaPOI(IPOIIf anIPOIIf);

    public IPOIListIf getAreaPOIs();

    public IMsoModelIf.ModificationState getAreaPOIsState(IPOIIf anIPOIIf);

    public Collection<IPOIIf> getAreaPOIsItems();

    public void addAreaGeodata(IMsoObjectIf anMsoObjectIf);

    public void addAreaGeodata(int aNr, IMsoObjectIf anMsoObjectIf);

    void setAreaGeodataItem(int index, IMsoObjectIf anMsoObjectIf);

    public IMsoListIf<IMsoObjectIf> getAreaGeodata();

    public IMsoModelIf.ModificationState getAreaGeodataState(IMsoObjectIf anMsoObjectIf);

    public Collection<IMsoObjectIf> getAreaGeodataItems();

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public IAssignmentIf getOwningAssignment();

    /**
     * Verify that the area can be assigned to a given assigmnent.
     *
     * @param anAssignment The assigmnent that shall refer to the area.
     * @throws IllegalOperationException If the assignment cannot be done (verification failed).
     */
    public void verifyAssignable(IAssignmentIf anAssignment) throws IllegalOperationException;

    public IMsoObjectIf getGeodataAt(int anIndex);

    public Iterator<IGeodataIf> getAreaGeodataIterator();
}