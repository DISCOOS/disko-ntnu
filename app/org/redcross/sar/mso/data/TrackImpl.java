package org.redcross.sar.mso.data;

import no.cmr.tools.Log;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

/**
 * Recorded route.
 */
public class TrackImpl extends AbstractMsoObject implements ITrackIf
{
    private final AttributeImpl.MsoTrack m_geodata = new AttributeImpl.MsoTrack(this, "Geodata");
    private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");
    private final AttributeImpl.MsoInteger m_areaSequenceNumber = new AttributeImpl.MsoInteger(this, "AreaSequenceNumber");

    public TrackImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    public TrackImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, Track aTrack)
    {
        super(theMsoModel, anObjectId);
        setGeodata(aTrack);
    }

    protected void defineAttributes()
    {
        addAttribute(m_geodata);
        addAttribute(m_remarks);
        addAttribute(m_areaSequenceNumber);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
    }

    public static TrackImpl implementationOf(ITrackIf anInterface) throws MsoCastException
    {
        try
        {
            return (TrackImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to TrackImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_TRACK;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setGeodata(Track aGeodata)
    {
        m_geodata.set(aGeodata);
    }

    public Track getGeodata()
    {
        return m_geodata.get();
    }

    public IData.DataOrigin getGeodataState()
    {
        return m_geodata.getOrigin();
    }

    public IMsoAttributeIf.IMsoTrackIf getGeodataAttribute()
    {
        return m_geodata;
    }

    public void setRemarks(String aRemarks)
    {
        m_remarks.setValue(aRemarks);
    }

    public String getRemarks()
    {
        return m_remarks.getString();
    }

    public IData.DataOrigin getRemarksState()
    {
        return m_remarks.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute()
    {
        return m_remarks;
    }


    public void setAreaSequenceNumber(int aNumber)
    {
        m_areaSequenceNumber.setValue(aNumber);
    }

    public int getAreaSequenceNumber()
    {
        return m_areaSequenceNumber.intValue();
    }

    public IData.DataOrigin getAreaSequenceNumberState()
    {
        return m_areaSequenceNumber.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getAreaSequenceNumberAttribute()
    {
        return m_areaSequenceNumber;
    }

    /*-------------------------------------------------------------------------------------------
     * Public methods
     *-------------------------------------------------------------------------------------------*/

    public void addTrackPoint(TimePos aTimePos)
    {
        Track t = getGeodata();
        try
        {
            Track tc = (Track)t.clone();
            tc.add(aTimePos);
            setGeodata(tc);
        }
        catch (CloneNotSupportedException e)
        {
            Log.error("CloneNotSupportedException in addTrackPoint, no point added.");
        }
    }

    public void removeTrackPoint(TimePos aTimePos)
    {
        Track t = getGeodata();
        try
        {
            Track tc = (Track)t.clone();
            tc.remove(aTimePos);
            setGeodata(tc);
        }
        catch (CloneNotSupportedException e)
        {
            Log.error("CloneNotSupportedException in removeTrackPoint, no point removed.");
        }
    }

	@Override
	public TimePos getTrackPoint(int index) {
		Track t = getGeodata();
		if(t!=null) {
			return t.get(index);
		}
		return null;
	}

	@Override
	public int getTrackPointCount() {
		Track t = getGeodata();
		if(t!=null) {
			return t.size();
		}
		return 0;
	}

	@Override
	public TimePos getTrackStartPoint() {
		Track t = getGeodata();
		if(t!=null) {
			return t.getStartPoint();
		}
		return null;
	}

	@Override
	public TimePos getTrackStopPoint() {
		Track t = getGeodata();
		if(t!=null) {
			return t.getStopPoint();
		}
		return null;
	}

}