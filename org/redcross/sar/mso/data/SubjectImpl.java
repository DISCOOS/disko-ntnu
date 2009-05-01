package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;

public class SubjectImpl extends AbstractPerson implements ISubjectIf
{
    private final AttributeImpl.MsoString m_description = new AttributeImpl.MsoString(this, "Description");

    public SubjectImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_description);
    }

    @Override
    protected void defineLists()
    {
        super.defineLists();
    }

    @Override
    protected void defineObjects()
    {
        super.defineObjects();
    }

    public static SubjectImpl implementationOf(ISubjectIf anInterface) throws MsoCastException
    {
        try
        {
            return (SubjectImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to SubjectImpl");
        }
    }

//    public String toString()
//    {
//        return super.toString() + " " + m_description.getAttrValue();
//    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_SUBJECT;
    }


}