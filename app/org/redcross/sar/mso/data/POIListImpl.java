package org.redcross.sar.mso.data;

import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.util.mso.Position;

public class POIListImpl extends MsoListImpl<IPOIIf> implements IPOIListIf
{

    public POIListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IPOIIf.class, anOwner, theName, isMain);
    }

    public POIListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IPOIIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public POIListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int theCardinality, int aSize)
    {
        super(IPOIIf.class, anOwner, theName, isMain, theCardinality, aSize);
    }

    public IPOIIf createPOI()
    {
        checkCreateOp();
        return createdUniqueItem(new POIImpl(getOwner().getModel(), createUniqueId()));
    }

    public IPOIIf createPOI(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IPOIIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new POIImpl(getOwner().getModel(), anObjectId));
    }

    public IPOIIf createPOI(IPOIIf.POIType aType, Position aPosition)
    {
        checkCreateOp();
        return createdUniqueItem(new POIImpl(getOwner().getModel(), createUniqueId(), aType, aPosition));
    }

    public IPOIIf createPOI(IMsoObjectIf.IObjectIdIf anObjectId, IPOIIf.POIType aType, Position aPosition)
    {
        checkCreateOp();
        IPOIIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new POIImpl(getOwner().getModel(), anObjectId, aType, aPosition));
    }

    public int getNextSequenceNumber() {
    	int retVal=-1;
    	for(IPOIIf poi : getItems(m_created.values())) {
			retVal = Math.max(poi.getAreaSequenceNumber(), retVal);
    	}
    	return retVal + 1;
    }

    public int getNextSequenceNumber(POIType type) {
    	if(type==null)
    		return getNextSequenceNumber();
    	else {
	    	int retVal=-1;
	    	for(IPOIIf poi : getItems(m_created.values())) {
	    		if(poi.getType().equals(type)) {
	    			retVal = Math.max(poi.getAreaSequenceNumber(), retVal);
	    		}
	    	}
	    	return retVal + 1;
    	}
    }


}