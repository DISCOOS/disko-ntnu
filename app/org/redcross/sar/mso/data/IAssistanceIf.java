package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.IllegalOperationException;

/**
 *
 */
public interface IAssistanceIf extends IAssignmentIf
{
    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public IAreaIf getPlannedIncidentScene();

    public IAreaIf getReportedIncidentScene();

    public void setPlannedIncidentScene(IAreaIf anArea)  throws IllegalOperationException;

    public void setReportedIncidentScene(IAreaIf anArea) throws IllegalOperationException;

}
