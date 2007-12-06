package org.redcross.sar.wp.logistics;

import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.wp.IDiskoWpModule;

public interface IDiskoWpLogistics extends IDiskoWpModule
{
    public final static String bundleName = "org.redcross.sar.wp.logistics.logistics";

    public boolean confirmTransfer(IAssignmentIf anAssignment, IAssignmentIf.AssignmentStatus aTargetStatus, IUnitIf aTargetUnit);

    public boolean transfer(IAssignmentIf anAssignment, IAssignmentIf.AssignmentStatus aTargetStatus, IUnitIf aTargetUnit);
    
    public void showTransferWarning();

}