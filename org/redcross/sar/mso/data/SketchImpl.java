package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;

public class SketchImpl extends AbstractMsoObject implements ISketchIf
{

    public SketchImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
    }

    public static SketchImpl implementationOf(ISketchIf anInterface) throws MsoCastException
    {
        try
        {
            return (SketchImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to SketchImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_SKETCH;
    }

}