package org.redcross.sar.mso.data;

import org.redcross.sar.Application;
import org.redcross.sar.data.IData;
import org.redcross.sar.gui.factory.BasicDiskoFactory;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.MsoCastException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.util.mso.Position;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar, kennetgu
 * Date: 25.jun.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
public class MessageLineImpl extends AbstractMsoObject implements IMessageLineIf
{
    private final AttributeImpl.MsoInteger m_lineNumber = new AttributeImpl.MsoInteger(this, "LineNumber");
    private final AttributeImpl.MsoString m_lineText = new AttributeImpl.MsoString(this, "LineText");
    private final AttributeImpl.MsoCalendar m_operationTime = new AttributeImpl.MsoCalendar(this, "OperationTime");
    private final AttributeImpl.MsoPosition m_linePosition = new AttributeImpl.MsoPosition(this, "LinePosition");
    private final AttributeImpl.MsoEnum<MessageLineType> m_lineType = new AttributeImpl.MsoEnum<MessageLineType>(this, "LineType", 1, MessageLineType.TEXT);

    private final MsoRelationImpl<IPOIIf> m_linePOI = new MsoRelationImpl<IPOIIf>(this, "LinePOI", 0, true, null);
    private final MsoRelationImpl<IUnitIf> m_lineUnit = new MsoRelationImpl<IUnitIf>(this, "LineUnit", 0, true, null);
    private final MsoRelationImpl<IAssignmentIf> m_lineAssignment = new MsoRelationImpl<IAssignmentIf>(this, "LineAssignment", 0, true, null);

    public static String getText(String aKey)
    {
        return Internationalization.getString(Internationalization.getBundle(IMessageLineIf.class), aKey);
    }

    public String getLineTypeText()
    {
        return m_lineType.getInternationalName();
    }

    public MessageLineImpl(IMsoModelIf theMsoModel, IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
        addAttribute(m_lineNumber);
        addAttribute(m_lineText);
        addAttribute(m_lineType);
        addAttribute(m_operationTime);
        addAttribute(m_linePosition);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
        addObject(m_linePOI);
        addObject(m_lineUnit);
        addObject(m_lineAssignment);
    }

    public static MessageLineImpl implementationOf(IMessageLineIf anInterface) throws MsoCastException
    {
        try
        {
            return (MessageLineImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to MessageLineImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_MESSAGELINE;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/


    public void setLineType(MessageLineType aLineType)
    {
        m_lineType.set(aLineType);
    }

    public void setLineType(String aLineType)
    {
        m_lineType.set(aLineType);
    }

    public MessageLineType getLineType()
    {
        return m_lineType.get();
    }

    public IData.DataOrigin getLineTypeState()
    {
        return m_lineType.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<MessageLineType> getLineTypeAttribute()
    {
        return m_lineType;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setLineNumber(int aNumber)
    {
        m_lineNumber.setValue(aNumber);
    }

    public int getLineNumber()
    {
        return m_lineNumber.intValue();
    }

    public IData.DataOrigin getLineNumberState()
    {
        return m_lineNumber.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getLineNumberAttribute()
    {
        return m_lineNumber;
    }


    public void setLineText(String aText)
    {
        m_lineText.setValue(aText);
    }

    public String getLineText()
    {
        return m_lineText.getString();
    }

    public IData.DataOrigin getLineTextState()
    {
        return m_lineText.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getLineTextAttribute()
    {
        return m_lineText;
    }

    public void setLineTime(Calendar anOperationTime)
    {
        m_operationTime.set(anOperationTime);
    }

    public Calendar getLineTime()
    {
        return m_operationTime.getCalendar();
    }

    public IData.DataOrigin getLineTimeState()
    {
        return m_operationTime.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getLineTimeAttribute()
    {
        return m_operationTime;
    }

    public void setLinePosition(Position aPosition)
    {
    	m_linePosition.set(aPosition);
    }

	public Position getLinePosition() {
		return m_linePosition.get();
	}

	public IData.DataOrigin getLinePositionState() {
		return m_linePosition.getOrigin();
	}

    public IMsoAttributeIf.IMsoPositionIf getLinePositionAttribute()
    {
        return m_linePosition;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setLinePOI(IPOIIf aPoi)
    {
        m_linePOI.set(aPoi);
    }

    public IPOIIf getLinePOI()
    {
        return m_linePOI.get();
    }

	public IData.DataOrigin getLinePOIState()
    {
        return m_linePOI.getOrigin();
    }

    public IMsoRelationIf<IPOIIf> getLinePOIAttribute()
    {
        return m_linePOI;
    }

	public void setLineUnit(IUnitIf aUnit) {
		m_lineUnit.set(aUnit);
	}

	public IUnitIf getLineUnit() {
		return m_lineUnit.get();
	}

	public IData.DataOrigin getLineUnitState() {
		return m_lineUnit.getOrigin();
	}

	public IMsoRelationIf<IUnitIf> getLineUnitAttribute() {
		return m_lineUnit;
	}

    public void setLineAssignment(IAssignmentIf anAssignment)
    {
        m_lineAssignment.set(anAssignment);
    }

    public IAssignmentIf getLineAssignment()
    {
        return m_lineAssignment.get();
    }

    public IData.DataOrigin getLineAssignmentState()
    {
        return m_lineAssignment.getOrigin();
    }

    public IMsoRelationIf<IAssignmentIf> geLinetAssignmentAttribute()
    {
        return m_lineAssignment;
    }

    @Override
    public String toString()
    {
        if (m_lineType == null)
        {
            return "";
        }

        // get line type
        MessageLineType type = getLineType();

        // get text template
        String template = getText(BasicDiskoFactory.getKey(type,"text"));

        // translate type to text
        switch (type)
        {
        case TEXT:
			return String.format(template,getLineText());
        case POSITION:

			// get position
			Position p = getLinePosition();

			if(p != null)
			{
				// get unit name
				String unit = MsoUtils.getUnitName(getLineUnit(),false);

				try {
					String mgrs = MapUtil.getMGRSfromPosition(p,5);
					// get zone
					String zone = mgrs.subSequence(0, 3).toString();
					String square = mgrs.subSequence(3, 5).toString();
					String[] coords = MapUtil.getCoords(mgrs.subSequence(5,mgrs.length()).toString());
					String x = coords[0];
					String y = coords[1];
					// get text
					return String.format(template, unit, zone, square, x, y, DTG.CalToDTG(getLineTime()));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
        	break;
        case POI:
			IPOIIf poi = getLinePOI();
			if(poi != null)
			{
				String poiName = MsoUtils.getPOIName(poi,false,true);
				Position pos = getLinePOI().getPosition();
				if(pos != null)
				{
					try {
						String mgrs = MapUtil.getMGRSfromPosition(pos,5);
						// get zone
						String zone = mgrs.subSequence(0, 3).toString();
						String square = mgrs.subSequence(3, 5).toString();
						String[] coords = MapUtil.getCoords(mgrs.subSequence(5,mgrs.length()).toString());
						String x = coords[0];
						String y = coords[1];
						// get text
						return String.format(template, poiName, zone, square, x, y);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			break;
        case ALLOCATED:
        case STARTED:
        case COMPLETED:
        case ABORTED:
        case RECALLED:
			return String.format(template,
					MsoUtils.getAssignmentName(getLineAssignment(),1), DTG.CalToDTG(getLineTime()));
        }
        // failure
        return null;
    }

    private final static SelfSelector<IMessageLineIf, IMessageIf> owningMessageSelector = new SelfSelector<IMessageLineIf, IMessageIf>()
    {
        public boolean select(IMessageIf anObject)
        {
            return (anObject.getMessageLines().exists(m_object));
        }
    };

    public IMessageIf getOwningMessage()
    {
        owningMessageSelector.setSelfObject(this);
        ICmdPostIf cmdPost = Application.getInstance().getMsoModel().getMsoManager().getCmdPost();
        return cmdPost != null ? cmdPost.getMessageLog().selectSingleItem(owningMessageSelector) : null;
    }

}
