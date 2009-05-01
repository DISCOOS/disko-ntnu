package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;
import org.redcross.sar.util.mso.Polygon;


/**
 * Area of operation. No activities outside this area
 */
public class OperationAreaImpl extends AbstractMsoObject implements IOperationAreaIf
{
    private final AttributeImpl.MsoString m_assignment = new AttributeImpl.MsoString(this, "Assignment");
    private final AttributeImpl.MsoPolygon m_geodata = new AttributeImpl.MsoPolygon(this, "Geodata");
    private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");

    public OperationAreaImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
        addAttribute(m_assignment);
        addAttribute(m_geodata);
        addAttribute(m_remarks);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
    }

    public static OperationAreaImpl implementationOf(IOperationAreaIf anInterface) throws MsoCastException
    {
        try
        {
            return (OperationAreaImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to OperationAreaImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA;
    }

    public void setAssignment(String anAssignment)
    {
        m_assignment.setValue(anAssignment);
    }

    public String getAssignment()
    {
        return m_assignment.getString();
    }

    public IMsoModelIf.ModificationState getAssignmentState()
    {
        return m_assignment.getState();
    }

    public IAttributeIf.IMsoStringIf getAssignmentAttribute()
    {
        return m_assignment;
    }

    public void setGeodata(Polygon aGeodata)
    {
        m_geodata.setValue(aGeodata);
    }

    public Polygon getGeodata()
    {
        return m_geodata.getPolygon();
    }

    public IMsoModelIf.ModificationState getGeodataState()
    {
        return m_geodata.getState();
    }

    public IAttributeIf.IMsoPolygonIf getGeodataAttribute()
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

    public IMsoModelIf.ModificationState getRemarksState()
    {
        return m_remarks.getState();
    }

    public IAttributeIf.IMsoStringIf getRemarksAttribute()
    {
        return m_remarks;
    }

}