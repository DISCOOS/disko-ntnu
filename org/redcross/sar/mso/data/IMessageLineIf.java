package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.mso.Position;

import java.util.Calendar;
import java.util.Comparator;

/**
 * User: vinjar
 * Date: 25.jun.2007
 */

/**
 *
 */
public interface IMessageLineIf extends IMsoObjectIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.MessageLine";

    public enum MessageLineType
    {
        TEXT,
        POSITION,
        POI,
        ASSIGNED,
        STARTED,
        COMPLETED
    }

    public static final Comparator<IMessageLineIf> MESSAGE_LINE_TIME_COMPARATOR = new Comparator<IMessageLineIf>()
    {
        public int compare(IMessageLineIf o1, IMessageLineIf o2)
        {
            return o2.getOperationTime().compareTo(o1.getOperationTime()); // Sort according to latest time
        }
    };

    public static final Comparator<IMessageLineIf> LINE_NUMBER_COMPARATOR = new Comparator<IMessageLineIf>()
    {
        public int compare(IMessageLineIf m1, IMessageLineIf m2)
        {
            return m1.getLineNumber() - m2.getLineNumber();
        }
    };


    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setLineType(MessageLineType aLineType);

    public void setLineType(String aLineType);

    public MessageLineType getLineType();

    public IMsoModelIf.ModificationState getLineTypeState();

    public IAttributeIf.IMsoEnumIf<MessageLineType> getLineTypeAttribute();

    public String getLineTypeText();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setLineNumber(int aNumber);

    public int getLineNumber();

    public IMsoModelIf.ModificationState getLineNumberState();

    public IAttributeIf.IMsoIntegerIf getLineNumberAttribute();

    public void setLineText(String aText);

    public String getLineText();

    public IMsoModelIf.ModificationState getLineTextState();

    public IAttributeIf.IMsoStringIf getLineTextAttribute();

    public void setOperationTime(Calendar anOperationTime);

    public Calendar getOperationTime();

    public IMsoModelIf.ModificationState getOperationTimeState();

    public IAttributeIf.IMsoCalendarIf getOperationTimeAttribute();

    public void setLinePosition(Position aPosition);
    
    public Position getLinePosition();

    public IMsoModelIf.ModificationState getLinePositionState();
    
    public IAttributeIf.IMsoPositionIf getLinePositionAttribute();
    
    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setLinePOI(IPOIIf aPoi);

    public IPOIIf getLinePOI();

    public IMsoModelIf.ModificationState getLinePOIState();

    public IMsoReferenceIf<IPOIIf> getLinePOIAttribute();

    public void setLineUnit(IUnitIf aUnit);
    
    public IUnitIf getLineUnit();

    public IMsoModelIf.ModificationState getLineUnitState();
    
    public IMsoReferenceIf<IUnitIf> getLineUnitAttribute();
    
    public void setLineAssignment(IAssignmentIf anAssignment);

    public IAssignmentIf getLineAssignment();

    public IMsoModelIf.ModificationState getLineAssignmentState();

    public IMsoReferenceIf<IAssignmentIf> geLinetAssignmentAttribute();

    public IMessageIf getOwningMessage();
}
