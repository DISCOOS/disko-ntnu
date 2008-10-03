package org.redcross.sar.wp.TestData;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.except.MsoException;
import org.redcross.sar.util.mso.Position;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 16.apr.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
public class BuildTestData
{
    public static void createCmdPost(IMsoModelIf aMsoModel)
    {
        aMsoModel.setLocalUpdateMode();
        IMsoManagerIf msoManager = aMsoModel.getMsoManager();
        IOperationIf testOperation = msoManager.getOperation();
        if (testOperation == null)
        {
            try
            {
                testOperation = msoManager.createOperation("2007-TEST", "0001");
            }
            catch (DuplicateIdException e) // shall not happen
            {
                e.printStackTrace();
            }
        }
        ICmdPostIf cmdPost = msoManager.getCmdPost();
        if (cmdPost == null)
        {
            try
            {
                cmdPost = msoManager.createCmdPost();
            }
            catch (DuplicateIdException e) // shall not happen
            {
                e.printStackTrace();
            }
        }
        if (cmdPost != null)
        {
            cmdPost.setStatus(ICmdPostIf.CmdPostStatus.OPERATING);
        }
        aMsoModel.restoreUpdateMode();
        aMsoModel.commit();
    }

    public static void createUnits(IMsoModelIf aMsoModel)
    {
        ICmdPostIf cmdPost = aMsoModel.getMsoManager().getCmdPost();
        aMsoModel.setLocalUpdateMode();
        IUnitListIf unitList = cmdPost.getUnitList();
        IUnitIf unit;

        try {
			for (int i = 1; i < 3; i++)
			{
			    unit = unitList.createVehicle("St 123" + i);
			    unit.setRemarks("This is a red car");
			    unit.setStatus(IUnitIf.UnitStatus.READY);
			    unit.setCallSign("888" + i);

			    unit = unitList.createVehicle("Su 987" + i);
			    unit.setStatus(IUnitIf.UnitStatus.READY);
			    unit.setCallSign("213" + i);

			    unit = unitList.createBoat("Jupiter" + "_" + i);
			    unit.setStatus(IUnitIf.UnitStatus.READY);
			    unit.setCallSign("999" + i);
			}
		} catch (IllegalOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        aMsoModel.restoreUpdateMode();
        aMsoModel.commit();
    }

    public static void createUnitsAndAssignments(IMsoModelIf aMsoModel)
    {
        ICmdPostIf cmdPost = aMsoModel.getMsoManager().getCmdPost();
        aMsoModel.setLocalUpdateMode();
        IUnitListIf unitList = cmdPost.getUnitList();
        IUnitIf unit;

        try {
			for (int j = 10; j < 30; j++)
			{
			    unit = unitList.createVehicle("St 123" + j);
			    unit.setRemarks("This is a red car");
			    unit.setStatus(IUnitIf.UnitStatus.READY);
			    unit.setCallSign("888" + j);

			    unit = unitList.createVehicle("Su 987" + j);
			    unit.setStatus(IUnitIf.UnitStatus.READY);
			    unit.setCallSign("213" + j);

			    unit = unitList.createBoat("Jupiter" + "_" + j);
			    unit.setStatus(IUnitIf.UnitStatus.READY);
			    unit.setCallSign("999" + j);
			}

			unit = unitList.createVehicle("Sn 30000");
			unit.setStatus(IUnitIf.UnitStatus.EMPTY);

			unit = unitList.createVehicle("Sn 30001");
			unit.setStatus(IUnitIf.UnitStatus.EMPTY);

			unit = unitList.createVehicle("Sn 30002");
			unit.setStatus(IUnitIf.UnitStatus.EMPTY);
		} catch (IllegalOperationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        IAssignmentListIf asgList = cmdPost.getAssignmentList();
        try
        {
            IAssignmentIf asg;
            IAssignmentIf prevAsg = null;
            boolean insertionToggle = false;

            for (int j = 0; j < 10; j++)
            {
                unit = unitList.getUnit(j * 2 + 1);
                for (int i = 0; i < 10; i++)
                {
                    asg = asgList.createSearch();
                    asg.setOwningUnit(IAssignmentIf.AssignmentStatus.DRAFT, null);
                    asg.setOwningUnit(IAssignmentIf.AssignmentStatus.READY, null);
                    ((ISearchIf) asg).setSubType(ISearchIf.SearchSubType.LINE);
                    if (unit != null)
                    {
                        unit.enqueueAssignment(asg, prevAsg);
                    }
                    prevAsg = insertionToggle ? asg : null;
                    insertionToggle = !insertionToggle;
                }

                unit = unitList.getUnit(j * 2 + 2);
                for (int i = 0; i < 8; i++)
                {
                    asg = asgList.createSearch();
                    asg.setOwningUnit(IAssignmentIf.AssignmentStatus.DRAFT, null);
                    asg.setOwningUnit(IAssignmentIf.AssignmentStatus.READY, null);
                    ((ISearchIf) asg).setSubType(ISearchIf.SearchSubType.DOG);
                    if (unit != null)
                    {
                        unit.enqueueAssignment(asg, prevAsg);
                    }
                    prevAsg = insertionToggle ? asg : null;
                    insertionToggle = !insertionToggle;
                }

                for (int i = 0; i < 20; i++)
                {
                    asg = asgList.createSearch();
                    asg.setOwningUnit(IAssignmentIf.AssignmentStatus.DRAFT, null);
                    asg.setOwningUnit(IAssignmentIf.AssignmentStatus.READY, null);
                    switch (i % 3)
                    {
                        case 0:
                            asg.setPriority(IAssignmentIf.AssignmentPriority.LOW);
                            break;
                        case 1:
                            asg.setPriority(IAssignmentIf.AssignmentPriority.NORMAL);
                            break;
                        default:
                            asg.setPriority(IAssignmentIf.AssignmentPriority.HIGH);
                            break;
                    }

                    switch (i % 5)
                    {
                        case 0:
                            ((ISearchIf) asg).setSubType(ISearchIf.SearchSubType.PATROL);
                            break;
                        case 1:
                            ((ISearchIf) asg).setSubType(ISearchIf.SearchSubType.SHORELINE);
                            break;
                        case 2:
                            ((ISearchIf) asg).setSubType(ISearchIf.SearchSubType.DOG);
                            break;
                        case 3:
                            ((ISearchIf) asg).setSubType(ISearchIf.SearchSubType.LINE);
                            break;
                        default:
                            ((ISearchIf) asg).setSubType(ISearchIf.SearchSubType.URBAN);
                            break;
                    }

                }
            }

        }
        catch (MsoException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        aMsoModel.restoreUpdateMode();
        aMsoModel.commit();
    }

    public static void createMessages(IMsoModelIf aMsoModel)
    {
        ICmdPostIf cmdPost = aMsoModel.getMsoManager().getCmdPost();
        aMsoModel.setLocalUpdateMode();

        IMessageLogIf messageLog = cmdPost.getMessageLog();
        IMessageIf message;
        IMessageLineIf messageLine;

        message = messageLog.createMessage();
        message.setCreated(Calendar.getInstance());
        message.setTimeStamp(Calendar.getInstance());
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.TEXT, null, true);
        messageLine.setLineText("Tekst Linje 1. Treng litt meir tekst for å sjekke om lina vert delt eller ikkje. Treng enda litt meir tekst for å sjekke dette");

        message = messageLog.createMessage();
        message.setCreated(Calendar.getInstance());
        message.setTimeStamp(Calendar.getInstance());
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.TEXT, null, true);
        messageLine.setLineText("Tekst Linje 2");

        message = messageLog.createMessage();
        message.setCreated(Calendar.getInstance());
        message.setTimeStamp(Calendar.getInstance());
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.TEXT, null, true);
        messageLine.setLineText("Tekst Linje 2. Test av ei enda lengre line. " +
                "Test av ei enda lengre line. Test av ei enda lengre line. " +
                "Test av ei enda lengre line. Test av ei enda lengre line. " +
                "Test av ei enda lengre line. Test av ei enda lengre line. " +
                "Test av ei enda lengre line. Test av ei enda lengre line. " +
                "Test av ei enda lengre line. Test av ei enda lengre line. " +
                "Test av ei enda lengre line. Test av ei enda lengre line. " +
                "Test av ei enda lengre line. Test av ei enda lengre line. " +
                "Test av ei enda lengre line. Test av ei enda lengre line.");

        message = messageLog.createMessage();
        message.setCreated(Calendar.getInstance());
        message.setTimeStamp(Calendar.getInstance());
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.POSITION, true);
        IPOIIf poi = aMsoModel.getMsoManager().createPOI();
        poi.setPosition(new Position("2342423424", 10, 10));
        messageLine.setLinePOI(poi);
        messageLine = message.findMessageLine(MessageLineType.TEXT, true);
        messageLine.setLineText("Ei melding med eit faktisk POI objekt");

        message = messageLog.createMessage();
        message.setCreated(Calendar.getInstance());
        message.setTimeStamp(Calendar.getInstance());
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.TEXT, true);
        messageLine.setLineText("Ei melding med eit funn");
        messageLine = message.findMessageLine(MessageLineType.POI, true);

        aMsoModel.restoreUpdateMode();
        aMsoModel.commit();
    }

}
