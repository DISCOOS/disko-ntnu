package org.redcross.sar.mso;

import org.apache.log4j.Logger;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.ChangeList;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoException;
import org.redcross.sar.util.except.MsoNullPointerException;
import org.redcross.sar.util.except.MsoRuntimeException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;

import java.util.Calendar;
import java.util.EnumSet;

/**
 * An implementation of {@link IMsoManagerIf} interface
 */
public class MsoManagerImpl implements IMsoManagerIf
{
	private final Logger m_logger = Logger.getLogger(MsoManagerImpl.class);
	
	private IMsoModelIf m_model;
	private OperationImpl m_operation;
        
    protected MsoManagerImpl(IMsoModelIf theMsoModel, IMsoEventManagerIf anEventManager)
    {
        m_model = theMsoModel;

    	anEventManager.addRemoteUpdateListener(new IMsoUpdateListenerIf()
        {
			public EnumSet<MsoClassCode> getInterests()
			{
				return EnumSet.allOf(MsoClassCode.class);
			}

			public void handleMsoChangeEvent(ChangeList events)
            {
				for(MsoEvent.Change e : events.getEvents())
				{
	                loggEvent("ServerUpdateListener", e);
				}
            }

		});

        anEventManager.addLocalUpdateListener(new IMsoUpdateListenerIf()
        {

			public EnumSet<MsoClassCode> getInterests()
			{
				return EnumSet.allOf(MsoClassCode.class);
			}

			public void handleMsoChangeEvent(ChangeList events)
            {
				for(MsoEvent.Change e : events.getEvents())
				{
	                loggEvent("ClientUpdateListener", e);
				}
            }

        });
    }


    public static String getClassCodeText(MsoClassCode aClassCode)
    {
        return Internationalization.translate(aClassCode);
    }

    private void loggEvent(String aText, MsoEvent.Change e)
    {
        Object o = e.getSource();
        MsoClassCode classCode = MsoClassCode.CLASSCODE_NOCLASS;
        if (o instanceof IMsoObjectIf)
        {
            classCode = ((IMsoObjectIf) o).getClassCode();
        }
        m_logger.info(aText + ": " + getClassCodeText(classCode));
        
    }

    public IOperationIf createOperation(String aNumberPrefix, String aNumber)
    {
        if (m_operation != null)
        {
            throw new DuplicateIdException("An operation already exists");
        }
        IMsoObjectIf.IObjectIdIf operationId = m_model.getDispatcher().makeObjectId();
        return createOperation(aNumberPrefix, aNumber, operationId);
    }

    public IOperationIf createOperation(String aNumberPrefix, String aNumber, IMsoObjectIf.IObjectIdIf operationId)
    {
        if (m_operation != null && !m_operation.isDeleted())
        {
            throw new DuplicateIdException("An operation already exists");
        }
        m_operation = new OperationImpl(m_model,operationId, aNumberPrefix, aNumber);
        m_operation.setup(true);
        return m_operation;
    }

    /**
     * Test if an operation exists
     *
     * @return <code>true</code> if an operation exists, <code>false</code> otherwise
     */
    public boolean operationExists() {
    	return m_operation!=null && !(isOperationDeleted());
    }

    /**
     * Test if an operation is deleted
     *
     * @return <code>true</code> if operation is deleted, <code>false</code> otherwise
     */
    public boolean isOperationDeleted() {
    	return m_operation!=null && m_operation.isDeleted();
    }

    public IOperationIf getOperation()
    {
        return getExistingOperation();
    }

    private IOperationIf getExistingOperation()
    {
    	if(m_operation==null)
        	throw new MsoRuntimeException("No Operation exists.");
    	if(m_operation.isDeleted())
        	throw new MsoRuntimeException("Operation is deleted.");
    	return m_operation;
    }

    public ISystemIf getSystem()
    {
        return getExistingSystem();
    }

    private ISystemIf getExistingSystem()
    {
        ISystemIf system = getExistingOperation().getSystem();
        if (system == null)
        {

        	//System.out.println("CMDPOST:=null");

        	throw new MsoRuntimeException("No System exists.");

        }
        return system;
    }


    public ICmdPostIf getCmdPost()
    {
        return getExistingCmdPost();
    }

    private ICmdPostIf getExistingCmdPost()
    {
        ICmdPostIf cmdPost = getExistingOperation().getCmdPostList().getHeadObject();
        if (cmdPost == null)
        {

        	//System.out.println("CMDPOST:=null");

        	throw new MsoRuntimeException("No CmdPost exists.");

        }
        return cmdPost;
    }

    public CmdPostImpl getCmdPostImpl()
    {
        return (CmdPostImpl) getCmdPost();
    }

    public IHierarchicalUnitIf getCmdPostUnit()
    {
        return (IHierarchicalUnitIf) getCmdPost();
    }


    public ICommunicatorIf getCmdPostCommunicator()
    {
        return (ICommunicatorIf) getExistingCmdPost();
    }

    public boolean remove(IMsoObjectIf aMsoObject) throws MsoException
    {
        if (aMsoObject == null)
        {
            throw new MsoNullPointerException("Tried to delete a null object");
        }

        if (aMsoObject instanceof IOperationIf)
        {
            throw new MsoException("Mso object of type IOperationIf can not be removed");
        }

        return aMsoObject.delete(true);

    }

    protected boolean clearOperation() {

    	if(operationExists()) {
    		// forward
    		m_model.suspendUpdate();
    		// remove operation
    		try {

	        	/* ==============================================================
                 * FIX: This methods fails if non-deleteable references exists in the
                 * model. This should not happen!
                 * ============================================================== */
                m_operation.delete(true);

            	// remove reference
                m_operation = null;

    	    	// do garbage collection
    	    	Runtime.getRuntime().gc();

			} catch (Exception e) {
				m_logger.error("Failed to clear operation",e);
	        	Utils.showError(e.getMessage());
	            return false;
			}
    		// forward
    		m_model.resumeUpdate();
    		// finished
    		return true;
    	}
    	// nothing changed
    	return false;
    }

    public void resumeUpdate()
    {
    	if(operationExists())
    		getExistingOperation().resumeUpdate(true);
    }

    protected void rollback() throws TransactionException
    {
    	if(operationExists())
    		((OperationImpl)getExistingOperation()).rollback();
    }
    
    public IAreaIf createArea()
    {
        return getExistingCmdPost().getAreaList().createArea(true);
    }

    public IAreaIf createArea(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getAreaList().createArea(anObjectId,true);
    }

    public IAssignmentIf createAssignment()
    {
        return getExistingCmdPost().getAssignmentList().createAssignment();
    }

    public IAssignmentIf createAssignment(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getAssignmentList().createAssignment(anObjectId);
    }

    public ISearchIf createSearch()
    {
        return getExistingCmdPost().getAssignmentList().createSearch();
    }

    public ISearchIf createSearch(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getAssignmentList().createSearch(anObjectId);
    }

    public IAssistanceIf createAssistance()
    {
        return getExistingCmdPost().getAssignmentList().createAssistance();
    }

    public IAssistanceIf createAssistance(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getAssignmentList().createAssistance(anObjectId);
    }

    public IBriefingIf createBriefing()
    {
        return getExistingCmdPost().getBriefingList().createBriefing();
    }

    public IBriefingIf createBriefing(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getBriefingList().createBriefing(anObjectId);
    }

    public ICalloutIf createCallout()
    {
        return getExistingCmdPost().getCalloutList().createCallout();
    }

    public ICalloutIf createCallout(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getCalloutList().createCallout(anObjectId);
    }

    public ICheckpointIf createCheckpoint()
    {
        return getExistingCmdPost().getCheckpointList().createCheckpoint();
    }

    public ICheckpointIf createCheckpoint(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getCheckpointList().createCheckpoint(anObjectId);
    }

    public ISystemIf createSystem()
    {
        if (getExistingSystem() != null)
        {
            throw new DuplicateIdException("The system already exists");
        }
        OperationImpl opr = (OperationImpl)getExistingOperation();
        return opr.createSystem();
    }

    public ISystemIf createSystem(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        OperationImpl opr = (OperationImpl)getExistingOperation();
        return opr.createSystem(anObjectId);
    }

    public ICmdPostIf createCmdPost()
    {
        if (getExistingCmdPost() != null)
        {
            throw new DuplicateIdException("An command post already exists");
        }

        return getExistingOperation().getCmdPostList().createCmdPost();
    }

    public ICmdPostIf createCmdPost(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingOperation().getCmdPostList().createCmdPost(anObjectId);
    }

    public IDataSourceIf createDataSource()
    {
        return getExistingOperation().getSystem().getDataSourceList().createDataSource();
    }

    public IDataSourceIf createDataSource(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingOperation().getSystem().getDataSourceList().createDataSource(anObjectId);
    }

    public IEnvironmentIf createEnvironment(Calendar aCalendar, String aText)
    {
        return getExistingCmdPost().getEnvironmentList().createEnvironment(aCalendar,
                aText);
    }

    public IEnvironmentIf createEnvironment(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getEnvironmentList().createEnvironment(anObjectId);
    }

    public IEquipmentIf createEquipment()
    {
        return getExistingCmdPost().getEquipmentList().createEquipment();
    }

    public IEquipmentIf createEquipment(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getEquipmentList().createEquipment(anObjectId);
    }

    public IForecastIf createForecast(Calendar aCalendar, String aText)
    {
        return getExistingCmdPost().getForecastList().createForecast(aCalendar, aText);
    }

    public IForecastIf createForecast(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getForecastList().createForecast(anObjectId);
    }

    public IHypothesisIf createHypothesis()
    {
        return getExistingCmdPost().getHypothesisList().createHypothesis();
    }

    public IHypothesisIf createHypothesis(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getHypothesisList().createHypothesis(anObjectId);
    }

    public IIntelligenceIf createIntelligence()
    {
        return getExistingCmdPost().getIntelligenceList().createIntelligence();
    }

    public IIntelligenceIf createIntelligence(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getIntelligenceList().createIntelligence(anObjectId);
    }

    public IMessageIf createMessage()
    {
        return getExistingCmdPost().getMessageLog().createMessage();
    }

    public IMessageIf createMessage(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getMessageLog().createMessage(anObjectId);
    }

    public IMessageLineIf createMessageLine()
    {
        return getExistingCmdPost().getMessageLines().createMessageLine();
    }

    public IMessageLineIf createMessageLine(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getMessageLines().createMessageLine(anObjectId);
    }

    public IOperationAreaIf createOperationArea()
    {
        return getExistingCmdPost().getOperationAreaList().createOperationArea();
    }

    public IOperationAreaIf createOperationArea(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getOperationAreaList().createOperationArea(
                anObjectId);
    }

    public IPersonnelIf createPersonnel()
    {
        return getExistingCmdPost().getAttendanceList().createPersonnel();
    }

    public IPersonnelIf createPersonnel(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getAttendanceList().createPersonnel(anObjectId);
    }

    public IPOIIf createPOI()
    {
        return getExistingCmdPost().getPOIList().createPOI();
    }

    public IPOIIf createPOI(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getPOIList().createPOI(anObjectId);
    }

    public IPOIIf createPOI(IPOIIf.POIType aType, Position aPosition)
    {
        return getExistingCmdPost().getPOIList().createPOI(aType, aPosition);
    }

    public IPOIIf createPOI(IMsoObjectIf.IObjectIdIf anObjectId, IPOIIf.POIType aType, Position aPosition)
    {
        return getExistingCmdPost().getPOIList().createPOI(anObjectId, aType, aPosition);
    }

    public IRouteIf createRoute(Route aRoute)
    {
        return getExistingCmdPost().getRouteList().createRoute(aRoute);
    }

    public IRouteIf createRoute(IMsoObjectIf.IObjectIdIf anObjectId, Route aRoute)
    {
        return getExistingCmdPost().getRouteList().createRoute(anObjectId, aRoute);
    }


    public IRouteIf createRoute(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getRouteList().createRoute(anObjectId);
    }

    public ISearchAreaIf createSearchArea()
    {
        return getExistingCmdPost().getSearchAreaList().createSearchArea();
    }

    public ISearchAreaIf createSearchArea(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getSearchAreaList().createSearchArea(
                anObjectId);
    }

    public ISketchIf createSketch()
    {
        return getExistingCmdPost().getSketchList().createSketch();
    }

    public ISketchIf createSketch(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getSketchList().createSketch(
                anObjectId);
    }

    public ISubjectIf createSubject()
    {
        return getExistingCmdPost().getSubjectList().createSubject();
    }

    public ISubjectIf createSubject(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getSubjectList().createSubject(anObjectId);
    }

    public ITaskIf createTask(Calendar aCalendar)
    {
        return getExistingCmdPost().getTaskList().createTask(aCalendar);
    }

    public ITaskIf createTask(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getTaskList().createTask(anObjectId);
    }

    public ITrackIf createTrack()
    {
        return getExistingCmdPost().getTrackList().createTrack();
    }

    public ITrackIf createTrack(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getTrackList().createTrack(anObjectId);
    }

    public IVehicleIf createVehicle(String anIdentifier)
    {
        return getExistingCmdPost().getUnitList().createVehicle(anIdentifier);
    }

    public IVehicleIf createVehicle(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getUnitList().createVehicle(anObjectId);
    }

    public IBoatIf createBoat(String anIdentifier)
    {
        return getExistingCmdPost().getUnitList().createBoat(anIdentifier);
    }

    public IBoatIf createBoat(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getUnitList().createBoat(anObjectId);
    }

    public IDogIf createDog(String anIdentifier)
    {
        return getExistingCmdPost().getUnitList().createDog(anIdentifier);
    }

    public IDogIf createDog(IMsoObjectIf.IObjectIdIf objectId)
    {
        return getExistingCmdPost().getUnitList().createDog(objectId);
    }

    public IAircraftIf createAircraft(String anIdentifier)
    {
        return getExistingCmdPost().getUnitList().createAircraft(anIdentifier);
    }

    public IAircraftIf createAircraft(IMsoObjectIf.IObjectIdIf objectId)
    {
        return getExistingCmdPost().getUnitList().createAircraft(objectId);
    }

    public ITeamIf createTeam(String anIdentifier)
    {
        return getExistingCmdPost().getUnitList().createTeam(anIdentifier);
    }

    public ITeamIf createTeam(IMsoObjectIf.IObjectIdIf objectId)
    {
        return getExistingCmdPost().getUnitList().createTeam(objectId);
    }
}
