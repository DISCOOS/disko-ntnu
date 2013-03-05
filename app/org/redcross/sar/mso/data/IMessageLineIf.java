package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.util.mso.Position;

import java.util.Calendar;
import java.util.Comparator;
import java.util.EnumSet;

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
        ALLOCATED,
        STARTED,
        COMPLETED,
        ABORTED,
        RECALLED
    }

    /**
     * Often used sets
     */

    public static final EnumSet<MessageLineType> ASSIGNMENT_CHANGE_SET
    		= EnumSet.range(MessageLineType.ALLOCATED, MessageLineType.RECALLED);

    /**
     * Often used selectors
     */

    public static final Selector<IMessageLineIf> ALL_SELECTOR = new Selector<IMessageLineIf>()
	{
		public boolean select(IMessageLineIf anObject)
		{
			return true;
		}
	};

    public static final Selector<IMessageLineIf> ASSIGNMENT_CHANGE_SELECTOR = new Selector<IMessageLineIf>()
	{
		public boolean select(IMessageLineIf anObject)
		{
			return ASSIGNMENT_CHANGE_SET.contains(anObject.getLineType());
		}
	};

    /**
     * Often used comparators
     */

    public static final Comparator<IMessageLineIf> MESSAGE_LINE_TIME_COMPARATOR = new Comparator<IMessageLineIf>()
    {
        public int compare(IMessageLineIf o1, IMessageLineIf o2)
        {
            return o2.getLineTime().compareTo(o1.getLineTime()); // Sort according to latest time
        }
    };

    public static final Comparator<IMessageLineIf> LINE_NUMBER_COMPARATOR = new Comparator<IMessageLineIf>()
    {
        public int compare(IMessageLineIf m1, IMessageLineIf m2)
        {
            return m1.getLineNumber() - m2.getLineNumber();
        }
    };

    public static final Comparator<IMessageLineIf> LINE_TYPE_COMPARATOR = new Comparator<IMessageLineIf>()
    {
        public int compare(IMessageLineIf m1, IMessageLineIf m2)
        {
            return m1.getLineType().compareTo(m2.getLineType());
        }
    };

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setLineType(MessageLineType aLineType);

    public void setLineType(String aLineType);

    public MessageLineType getLineType();

    public IData.DataOrigin getLineTypeState();

    public IMsoAttributeIf.IMsoEnumIf<MessageLineType> getLineTypeAttribute();

    public String getLineTypeText();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setLineNumber(int aNumber);

    public int getLineNumber();

    public IData.DataOrigin getLineNumberState();

    public IMsoAttributeIf.IMsoIntegerIf getLineNumberAttribute();

    public void setLineText(String aText);

    public String getLineText();

    public IData.DataOrigin getLineTextState();

    public IMsoAttributeIf.IMsoStringIf getLineTextAttribute();

    public void setLineTime(Calendar anOperationTime);

    public Calendar getLineTime();

    public IData.DataOrigin getLineTimeState();

    public IMsoAttributeIf.IMsoCalendarIf getLineTimeAttribute();

    public void setLinePosition(Position aPosition);

    public Position getLinePosition();

    public IData.DataOrigin getLinePositionState();

    public IMsoAttributeIf.IMsoPositionIf getLinePositionAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setLinePOI(IPOIIf aPoi);

    public IPOIIf getLinePOI();

    public IData.DataOrigin getLinePOIState();

    public IMsoRelationIf<IPOIIf> getLinePOIAttribute();

    public void setLineUnit(IUnitIf aUnit);

    public IUnitIf getLineUnit();

    public IData.DataOrigin getLineUnitState();

    public IMsoRelationIf<IUnitIf> getLineUnitAttribute();

    public void setLineAssignment(IAssignmentIf anAssignment);

    public IAssignmentIf getLineAssignment();

    public IData.DataOrigin getLineAssignmentState();

    public IMsoRelationIf<IAssignmentIf> geLinetAssignmentAttribute();

    public IMessageIf getOwningMessage();
}
