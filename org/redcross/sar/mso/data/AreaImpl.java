package org.redcross.sar.mso.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.redcross.sar.Application;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.except.InvalidReferenceException;
import org.redcross.sar.util.except.MsoCastException;
import org.redcross.sar.util.mso.IGeodataIf;

/**
 * Strip of field to search
 */
public class AreaImpl extends AbstractMsoObject implements IAreaIf
{
    private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");
    private final POIListImpl m_areaPOIs = new POIListImpl(this, "AreaPOIs", false, 2, 2);
    private final MsoListImpl<IMsoObjectIf> m_areaGeodata = new MsoListImpl<IMsoObjectIf>(IMsoObjectIf.class, this, "AreaGeodata", false);

    private final boolean m_hostile;

    public AreaImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, boolean hostile)
    {
        super(theMsoModel, anObjectId);
        this.m_hostile = hostile;
    }

    protected void defineAttributes()
    {
        addAttribute(m_remarks);
    }

    protected void defineLists()
    {
        addList(m_areaPOIs);
        addList(m_areaGeodata);
    }

    protected void defineObjects()
    {
    }

    public void addListReference(IMsoObjectIf anObject, String aReferenceListName) throws InvalidReferenceException
    {
        if (anObject instanceof IRouteIf || anObject instanceof ITrackIf)
        {
            m_areaGeodata.add(anObject);
        }
        if (anObject instanceof IPOIIf)
        {
            if ("AreaPOIs".equals(aReferenceListName))
            {
                m_areaPOIs.add((IPOIIf) anObject);
            }
            if ("AreaGeodata".equals(aReferenceListName))
            {
                m_areaGeodata.add(anObject);
            }
        }
    }

    public void removeListReference(IMsoObjectIf anObject, String aReferenceListName) throws InvalidReferenceException
    {
        if (anObject instanceof IRouteIf || anObject instanceof ITrackIf)
        {
            m_areaGeodata.remove(anObject);
        }
        if (anObject instanceof IPOIIf)
        {
            if ("AreaPOIs".equals(aReferenceListName))
            {
            	// remove area POIs?
            	if(m_hostile) {
	        		POIType type = ((IPOIIf)anObject).getType();
	        		if(IPOIIf.AREA_SET.contains(type)) {
	        			// delete object
	        			if(!anObject.delete(true)) throw new InvalidReferenceException("Reference can not be deleted");
	        		}
            	}
                m_areaPOIs.remove((IPOIIf) anObject);
            }
            if ("AreaGeodata".equals(aReferenceListName))
            {
            	// remove area POIs?
            	if(m_hostile) {
            		anObject.delete(true);
            	}
            	else {
            		m_areaGeodata.remove(anObject);
            	}
            }
        }
    }

    public static AreaImpl implementationOf(IAreaIf anInterface) throws MsoCastException
    {
        try
        {
            return (AreaImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to AreaImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_AREA;
    }


    public void setRemarks(String aRemarks)
    {
        m_remarks.setValue(aRemarks);
    }

    public String getRemarks()
    {
        return m_remarks.getString();
    }

    public IMsoModelIf.ModificationState getRemarksState()
    {
        return m_remarks.getState();
    }

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute()
    {
        return m_remarks;
    }

    /*-------------------------------------------------------------------------------------------
     * Overridden Methods
     *-------------------------------------------------------------------------------------------*/

    @Override
    public boolean delete(boolean deep)
    {
        if (isDeletable())
        {
        	suspendClientUpdate();
        	// is hostile?
        	if(m_hostile) {
	        	// delete area POIs
	        	for(IPOIIf it:m_areaPOIs.getObjects()) {
	        		POIType type = it.getType();
	        		if(IPOIIf.AREA_SET.contains(type)) {
	        			// delete object
	        			it.delete(true);
	        		}
	        	}
	        	// delete geodata
	        	for(IMsoObjectIf it:m_areaGeodata.getObjects()) {
	    			// delete object
	        		it.delete(true);
	        	}
        	}
        	resumeClientUpdate(true);
        	// forward
            return super.delete(deep);
        }
        return false;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addAreaPOI(IPOIIf anIPOIIf)
    {
        m_areaPOIs.add(anIPOIIf);
    }

    public IPOIListIf getAreaPOIs()
    {
        return m_areaPOIs;
    }

    public IMsoModelIf.ModificationState getAreaPOIsState(IPOIIf anIPOIIf)
    {
        return m_areaPOIs.getState(anIPOIIf);
    }

    public Collection<IPOIIf> getAreaPOIsItems()
    {
        return m_areaPOIs.getObjects();
    }


    public void setAreaGeodataItem(int anIndex, IMsoObjectIf anMsoObjectIf)
    {
        IMsoObjectIf oldItem = getGeodataAt(anIndex);
        if (oldItem != null)
        {
            m_areaGeodata.remove(oldItem);
        }
        addAreaGeodata(anIndex, anMsoObjectIf);
    }

    public void addAreaGeodata(IMsoObjectIf anMsoObjectIf)
    {
        addAreaGeodata(getNextGeodataSequenceNumber(), anMsoObjectIf);
    }

    public void addAreaGeodata(int aNr, IMsoObjectIf anMsoObjectIf)
    {
        if (setAreaSequenceNumber(anMsoObjectIf, aNr))
        {
            m_areaGeodata.add(anMsoObjectIf);
        }
        registerModifiedData(this,m_msoModel.getUpdateMode(),true,false,false); // in order to generate an event that the map draw tools recognize
    }

    private boolean setAreaSequenceNumber(IMsoObjectIf anMsoObjectIf, int aNr)
    {
        if (anMsoObjectIf instanceof IPOIIf)
        {
            ((IPOIIf) anMsoObjectIf).setAreaSequenceNumber(aNr);
            return true;
        } else if (anMsoObjectIf instanceof ITrackIf)
        {
            ((ITrackIf) anMsoObjectIf).setAreaSequenceNumber(aNr);
            return true;
        } else if (anMsoObjectIf instanceof IRouteIf)
        {
            ((IRouteIf) anMsoObjectIf).setAreaSequenceNumber(aNr);
            return true;
        } else
        {
            return false;
        }
    }

    public IMsoListIf<IMsoObjectIf> getAreaGeodata()
    {
        return m_areaGeodata;
    }

    public IMsoModelIf.ModificationState getAreaGeodataState(IMsoObjectIf anMsoObjectIf)
    {
        return m_areaGeodata.getState(anMsoObjectIf);
    }

    public Collection<IMsoObjectIf> getAreaGeodataItems()
    {
        return m_areaGeodata.getObjects();
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    private final static SelfSelector<IAreaIf, IAssignmentIf> owningAssigmentSelector = new SelfSelector<IAreaIf, IAssignmentIf>()
    {
        public boolean select(IAssignmentIf anObject)
        {
            return (anObject.getPlannedArea() == m_object || anObject.getReportedArea() == m_object); // todo Check status as well?
        }
    };

    public IAssignmentIf getOwningAssignment()
    {
        owningAssigmentSelector.setSelfObject(this);
        ICmdPostIf cmdPost = Application.getInstance().getMsoModel().getMsoManager().getCmdPost();
        return cmdPost != null ? cmdPost.getAssignmentList().selectSingleItem(owningAssigmentSelector) : null;
    }

    public void verifyAssignable(IAssignmentIf anAssignment) throws IllegalOperationException
    {
        IAssignmentIf asg = getOwningAssignment();
        if (asg != null && asg != anAssignment)
        {
            throw new IllegalOperationException("Area " + this + " is already allocated to an assigment.");
        }
    }

    private int getNextPOISequenceNumber()
    {
        int retVal = 0;
        for (IPOIIf ml : m_areaPOIs.getObjects())
        {
            if (ml.getAreaSequenceNumber() > retVal)
            {
                retVal = ml.getAreaSequenceNumber();
            }
        }
        return retVal + 1;
    }

    private int getNextGeodataSequenceNumber()
    {
        int retVal = -1;
        for (IMsoObjectIf ml : m_areaGeodata.getObjects())
        {
            int i = getAreaSequenceNumber(ml);

            if (i > retVal)
            {
                retVal = i;
            }
        }
        return retVal + 1;
    }

    private int getAreaSequenceNumber(IMsoObjectIf ml)
    {
        if (ml instanceof IPOIIf)                   // todo sjekk etter endring av GeoCollection
        {
            IPOIIf ml1 = (IPOIIf) ml;
            return ((IPOIIf) ml).getAreaSequenceNumber();
        }
        if (ml instanceof ITrackIf)
        {
            ITrackIf ml1 = (ITrackIf) ml;
            return ml1.getAreaSequenceNumber();
        }
        if (ml instanceof IRouteIf)
        {
            IRouteIf ml1 = (IRouteIf) ml;
            return ml1.getAreaSequenceNumber();
        }
        return 0;
    }

    public IMsoObjectIf getGeodataAt(int anIndex)
    {
        int i = 0;
        for (IMsoObjectIf ml : m_areaGeodata.getObjects())
        {
            if (getAreaSequenceNumber(ml) == anIndex)
            {
                return ml;
            }
        }
        return null;
    }

    public Iterator<IGeodataIf> getAreaGeodataIterator()
    {
        List<IMsoObjectIf> x = m_areaGeodata.selectItems(
        new Selector<IMsoObjectIf>()
        {
            public boolean select(IMsoObjectIf anObject)
            {
                return true;
            }
        },
        new Comparator<IMsoObjectIf>()
        {
            public int compare(IMsoObjectIf o1, IMsoObjectIf o2)
            {
                return getAreaSequenceNumber(o1) - getAreaSequenceNumber(o2);
            }
        });

        List<IGeodataIf> y = new ArrayList<IGeodataIf>();
        for (IMsoObjectIf m1 : x)
        {
            if (m1 instanceof IRouteIf)
            {
                y.add(((IRouteIf) m1).getGeodata());
            } else if (m1 instanceof ITrackIf)
            {
                y.add(((ITrackIf) m1).getGeodata());
            } else if (m1 instanceof IPOIIf)
            {
                y.add(((IPOIIf) m1).getPosition());
            }
        }
        return y.iterator();
    }

}