package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.IllegalOperationException;

/**
 * Subject assistance assignment
 */
public class AssistanceImpl extends AssignmentImpl implements IAssistanceIf
{
    public AssistanceImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel, anObjectId, aNumber);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
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

    @Override
    protected AssignmentType getTypeBySubclass()
    {
        return AssignmentType.ASSISTANCE;
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public IAreaIf getPlannedIncidentScene()
    {
        return getPlannedArea();
    }

    public IAreaIf getReportedIncidentScene()
    {
        return getReportedArea();
    }

    public void setPlannedIncidentScene(IAreaIf anArea) throws IllegalOperationException
    {
        setPlannedArea(anArea);
    }

    public void setReportedIncidentScene(IAreaIf anArea) throws IllegalOperationException
    {
        setReportedArea(anArea);
    }

}
