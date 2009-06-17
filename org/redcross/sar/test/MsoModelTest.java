package org.redcross.sar.test;

import org.apache.log4j.Logger;
import org.redcross.sar.mso.IDispatcherIf;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.SaraDispatcherImpl;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.except.MsoException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.mso.Position;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar, kenneth
 * Date: 16.apr.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
public class MsoModelTest
{
    private static final Logger m_logger = Logger.getLogger(MsoModelTest.class);

    private IMsoModelIf m_model;
    private IDispatcherIf m_dispatcher;
    
    public static void main(String args[])
    {
    	MsoModelTest test = new MsoModelTest();
    	test.run();
    }
    
	public MsoModelTest() {
		
		//initiate model driver    	
		getMsoModel().getDispatcher().initiate();
		
	}

	public IMsoModelIf getMsoModel()
	{
		if (m_model == null)
		{
			try {
				m_model = new MsoModelImpl(getDispatcher());
			} catch (Exception e) {
				System.out.println("Failed to create MsoModelImpl instance");
				e.printStackTrace();
			}
		}
		return m_model;
	}
    
	public IDispatcherIf getDispatcher() {
		if(m_dispatcher == null) {
	        m_dispatcher = new SaraDispatcherImpl();
		}
		return m_dispatcher;
	}
	
	public void run() {
		// create a new operation
		getDispatcher().createNewOperation();
		// print active operation
		System.out.println("Operation: " + getDispatcher().getActiveOperationID());
		// create a command post
		createUnits(getMsoModel());
	}
    
    public void createCmdPost(IMsoModelIf aMsoModel)
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
        try {
            aMsoModel.commit();
		} catch (TransactionException ex) {
			m_logger.error("Failed to commit test data",ex);
		}
		
    }

    public void createUnits(IMsoModelIf aMsoModel)
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
        try {
            aMsoModel.commit();
		} catch (TransactionException ex) {
			m_logger.error("Failed to commit test data changes",ex);
		}            
    }

    public void createUnitsAndAssignments(IMsoModelIf aMsoModel)
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
        try {
            aMsoModel.commit();
		} catch (TransactionException ex) {
			m_logger.error("Failed to commit test data changes",ex);
		}            
    }

    public void createMessages(IMsoModelIf aMsoModel)
    {
        ICmdPostIf cmdPost = aMsoModel.getMsoManager().getCmdPost();
        aMsoModel.setLocalUpdateMode();

        IMessageLogIf messageLog = cmdPost.getMessageLog();
        IMessageIf message;
        IMessageLineIf messageLine;

        message = messageLog.createMessage();
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.TEXT, null, true);
        messageLine.setLineText("Tekst Linje 1. Treng litt meir tekst for å sjekke om lina vert delt eller ikkje. Treng enda litt meir tekst for å sjekke dette");

        message = messageLog.createMessage();
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.TEXT, null, true);
        messageLine.setLineText("Tekst Linje 2");

        message = messageLog.createMessage();
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
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.POSITION, true);
        IPOIIf poi = aMsoModel.getMsoManager().createPOI();
        poi.setPosition(new Position("2342423424", 10, 10));
        messageLine.setLinePOI(poi);
        messageLine = message.findMessageLine(MessageLineType.TEXT, true);
        messageLine.setLineText("Ei melding med eit faktisk POI objekt");

        message = messageLog.createMessage();
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        messageLine = message.findMessageLine(MessageLineType.TEXT, true);
        messageLine.setLineText("Ei melding med eit funn");
        messageLine = message.findMessageLine(MessageLineType.POI, true);

        aMsoModel.restoreUpdateMode();
        try {
            aMsoModel.commit();
		} catch (TransactionException ex) {
			m_logger.error("Failed to commit test data changes",ex);
		}            
    }

}
