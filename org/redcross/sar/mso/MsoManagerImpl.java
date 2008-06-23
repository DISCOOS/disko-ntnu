package org.redcross.sar.mso;

import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoNullPointerException;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;

import java.util.Calendar;

/**
 * An implementation of {@link IMsoManagerIf} interface
 */
public class MsoManagerImpl implements IMsoManagerIf
{
    OperationImpl m_operation = null;

    public static String getClasscodeText(MsoClassCode aClassCode)
    {
        return Internationalization.translate(aClassCode);
    }

    private void eventLogg(String aText, MsoEvent.Update e)
    {
        Object o = e.getSource();
        MsoClassCode classcode = MsoClassCode.CLASSCODE_NOCLASS;
        if (o instanceof IMsoObjectIf)
        {
            classcode = ((IMsoObjectIf) o).getMsoClassCode();
        }
    }

    public MsoManagerImpl(IMsoEventManagerIf anEventManager)
    {
        anEventManager.addServerUpdateListener(new IMsoUpdateListenerIf()
        {
            public void handleMsoUpdateEvent(MsoEvent.Update e)
            {
                //To change body of implemented methods use File | Settings | File Templates.
                eventLogg("ServerUpdateListener", e);
            }

            public boolean hasInterestIn(IMsoObjectIf aSource)
            {
                return true;
            }
        });

        anEventManager.addClientUpdateListener(new IMsoUpdateListenerIf()
        {
            public void handleMsoUpdateEvent(MsoEvent.Update e)
            {
                //To change body of implemented methods use File | Settings | File Templates.
                eventLogg("ClientUpdateListener", e);
            }

            public boolean hasInterestIn(IMsoObjectIf aSource)
            {
                return true;
            }
        });
    }

    public IOperationIf createOperation(String aNumberPrefix, String aNumber)
    {
        if (m_operation != null)
        {
            throw new DuplicateIdException("An operation already exists");
        }
        IMsoObjectIf.IObjectIdIf operationId = MsoModelImpl.getInstance().getModelDriver().makeObjectId();
        return createOperation(aNumberPrefix, aNumber, operationId);
    }

    public IOperationIf createOperation(String aNumberPrefix, String aNumber, IMsoObjectIf.IObjectIdIf operationId)
    {
        if (m_operation != null)
        {
            throw new DuplicateIdException("An operation already exists");
        }
        m_operation = new OperationImpl(operationId, aNumberPrefix, aNumber);
        m_operation.setupReferences();
        m_operation.resumeClientUpdate();
        return m_operation;
    }


    public IOperationIf getOperation()
    {
        return m_operation;
    }

    public ICmdPostIf getCmdPost()
    {
        return m_operation != null ? m_operation.getCmdPostList().getItem() : null;
    }

    private ICmdPostIf getExistingCmdPost()
    {
        ICmdPostIf retVal = getCmdPost();
        if (retVal == null)
        {
            
        	System.out.println("CMDPOST:=null");
            
        	//throw new MsoException("No CmdPost exists.");
        }
        return retVal;
    }

    public CmdPostImpl getCmdPostImpl()
    {
        return (CmdPostImpl) getCmdPost();
    }

    public IHierarchicalUnitIf getCmdPostUnit()
    {
        return (IHierarchicalUnitIf) m_operation.getCmdPostList().getItem();
    }


    public ICommunicatorIf getCmdPostCommunicator()
    {
        return (ICommunicatorIf) m_operation.getCmdPostList().getItem();
    }

    public boolean remove(IMsoObjectIf aMsoObject) throws MsoNullPointerException
    {
        if (aMsoObject == null)
        {
            throw new MsoNullPointerException("Try to delete a null object");
        }

        if (aMsoObject instanceof OperationImpl)
        {
            if (aMsoObject == m_operation)
            {
                // This methods fails if non-deleteable references exist in the model... 
                boolean ret = m_operation.deleteObject();
            	
            	// remove reference
                m_operation = null;
                
    	    	// do garbage collection
    	    	Runtime.getRuntime().gc();
                
                return ret;
            }
            return false;
        } else
        {
            return aMsoObject.deleteObject();
        }
    }

    public void resumeClientUpdate()
    {
        if (m_operation != null)
            m_operation.resumeClientUpdates();
    }

    public void postProcessCommit()
    {
        if (m_operation != null)
            m_operation.postProcessCommit();
    }

    public void rollback()
    {
        if (m_operation != null)
            m_operation.rollback();
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
        return getExistingCmdPost().getCheckpointList().createCheckpoint(
                anObjectId);
    }

    public ICmdPostIf createCmdPost()
    {
        return m_operation.getCmdPostList().createCmdPost();
    }

    public ICmdPostIf createCmdPost(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return m_operation.getCmdPostList().createCmdPost(anObjectId);
    }

    public IDataSourceIf createDataSource()
    {
        return getExistingCmdPost().getDataSourceList().createDataSource();
    }

    public IDataSourceIf createDataSource(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getExistingCmdPost().getDataSourceList().createDataSource(anObjectId);
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
