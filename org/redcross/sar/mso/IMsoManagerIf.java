package org.redcross.sar.mso;

import org.redcross.sar.mso.data.*;
import org.redcross.sar.util.except.MsoException;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;

import java.util.Calendar;

/**
 * Interface for managing Mso objects
 */
public interface IMsoManagerIf
{
    public static final String bundleName = "org.redcross.sar.mso.properties.MsoManager";

    /**
     * ClassCodes .
     */
    public enum MsoClassCode
    {
        CLASSCODE_NOCLASS,
        CLASSCODE_SYSTEM,
        CLASSCODE_OPERATION,
        CLASSCODE_CMDPOST,
        CLASSCODE_HYPOTHESIS,
        CLASSCODE_BRIEFING,
        CLASSCODE_SUBJECT,
        CLASSCODE_INTELLIGENCE,
        CLASSCODE_POI,
        CLASSCODE_TRACK,
        CLASSCODE_ROUTE,
        CLASSCODE_CALLOUT,
        CLASSCODE_PERSONNEL,
        CLASSCODE_UNIT,
        CLASSCODE_SKETCH,
        CLASSCODE_EQUIPMENT,
        CLASSCODE_ASSIGNMENT,
        CLASSCODE_AREA,
        CLASSCODE_SEARCHAREA,
        CLASSCODE_OPERATIONAREA,
        CLASSCODE_TASK,
        CLASSCODE_CHECKPOINT,
        CLASSCODE_EVENT,
        CLASSCODE_MESSAGE,
        CLASSCODE_MESSAGELINE,
        CLASSCODE_FORECAST,
        CLASSCODE_ENVIRONMENT,
        CLASSCODE_DATASOURCE
    }

    /**
     * Create a new {@link org.redcross.sar.mso.data.IOperationIf} object.
     * The Big Bang that creates the Universe of MSO Structures (Galaxes) and Objects (stars).
     *
     * @param aNumberPrefix Number prefix types with format yyyy-[text], where yyyy = current year. List of prefix types is given in config file.
     * @param aNumber Variable part of operation number. The readable operation number is a concatenation of Number and NumberPrefix.
     * @return The created object.
     */
    public IOperationIf createOperation(String aNumberPrefix, String aNumber);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IOperationIf} object.
     * The Big Bang that creates the Universe of MSO Structures (Galaxes) and Objects (stars).
     *
     * @param aNumberPrefix Number prefix types with format yyyy-[text], where yyyy = current year. List of prefix types is given in config file.
     * @param aNumber Variable part of operation number. The readable operation number is a concatenation of Number and NumberPrefix.
     * @param aId the id of the operation
     * @return The created object.
     */
    public IOperationIf createOperation(String aNumberPrefix, String aNumber,IMsoObjectIf.IObjectIdIf aId);

    /**
     * Get the {@link org.redcross.sar.mso.data.IOperationIf} object
     *
     * @return The Operation object, null if no object is defined
     */
    public IOperationIf getOperation();

    /**
     * Test if an operation exists
     *
     * @return <code>true</code> if an operation exists, <code>false</code> otherwise 
     */
    public boolean operationExists();
    
    /**
     * Test if an operation is deleted
     *
     * @return <code>true</code> if operation is deleted, <code>false</code> otherwise 
     */
    public boolean isOperationDeleted();
    
    /**
     * Create a new {@link org.redcross.sar.mso.data.ISystemIf} object and add it to the collection of CmdPost objects.
     *
     * @return The created object, with an empty MSO structure.
     */
    public ISystemIf createSystem();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISystemIf} object and add it to the collection of CmdPost objects.
     *
     * @param anObjectId The Object id
     * @return The created object, with an empty MSO structure.
     */
    public ISystemIf createSystem(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Get (Unique) {@link org.redcross.sar.mso.data.ICmdPostIf} object
     *
     * @return The {@link org.redcross.sar.mso.data.ISystemIf} object
     */
    public ISystemIf getSystem();    
    
    /**
     * Create a new {@link org.redcross.sar.mso.data.ICmdPostIf} object and add it to the collection of CmdPost objects.
     *
     * @return The created object, with an empty MSO structure.
     */
    public ICmdPostIf createCmdPost();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ICmdPostIf} object and add it to the collection of CmdPost objects.
     *
     * @param anObjectId The Object id
     * @return The created object, with an empty MSO structure.
     */
    public ICmdPostIf createCmdPost(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Get (Unique) {@link org.redcross.sar.mso.data.ICmdPostIf} object
     *
     * @return The {@link org.redcross.sar.mso.data.ICmdPostIf} object
     */
    public ICmdPostIf getCmdPost();

    /**
     * Get (Unique) {@link org.redcross.sar.mso.data.ICmdPostIf} object as an {@link IHierarchicalUnitIf} object
     *
     * @return The {@link IHierarchicalUnitIf} object
     */
    public IHierarchicalUnitIf getCmdPostUnit();

    /**
     * Get (Unique) {@link org.redcross.sar.mso.data.ICmdPostIf} object as an {@link ICommunicatorIf} object
     *
     * @return The {@link ICommunicatorIf} object
     */
    public ICommunicatorIf getCmdPostCommunicator();

    /**
     * Remove an object from the data structure.
     *
     * @param aMsoObject The object to remove.
     * @return <code>true</code> if the object was deleted, <code>false</code> otherwise
     * @throws org.redcross.sar.util.except.MsoNullPointerException
     *          if aMsoObject is null.
     * @throws org.redcross.sar.util.except.MsoException
     *          if aMsoObject is of type IOperationIf.
     */
    public boolean remove(IMsoObjectIf aMsoObject) throws MsoException;
    
    
//    /**
//     * Might not be needed.
//     */
//    public void commit();

/*    *//**
     * Perform local commit on all objects in the model.
     *//*
    public void postProcessCommit();

    *//**
     * Perform rollback on all objects in the model.
     *//*
    public void rollback();*/

    /**
     * Create a new {@link org.redcross.sar.mso.data.IAreaIf} object and add it to the collection of Area objects.
     *
     * @return The created object.
     */
    public IAreaIf createArea();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IAreaIf} object and add it to the collection of Area objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IAreaIf createArea(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IAssignmentIf} object and add it to the collection of Assignment objects.
     *
     * @return The created object.
     */
    public IAssignmentIf createAssignment();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IAssignmentIf} object and add it to the collection of Assignment objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IAssignmentIf createAssignment(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISearchIf} object and add it to the collection of Assignment objects.
     *
     * @return The created object.
     */
    public ISearchIf createSearch();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISearchIf} object and add it to the collection of Assignment objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public ISearchIf createSearch(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IAssistanceIf} object and add it to the collection of Assignment objects.
     *
     * @return The created object.
     */
    public IAssistanceIf createAssistance();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IAssistanceIf} object and add it to the collection of Assignment objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IAssistanceIf createAssistance(IMsoObjectIf.IObjectIdIf anObjectId);


    /**
     * Create a new {@link org.redcross.sar.mso.data.IBriefingIf} object and add it to the collection of Briefing objects.
     *
     * @return The created object.
     */
    public IBriefingIf createBriefing();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IBriefingIf} object and add it to the collection of Briefing objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IBriefingIf createBriefing(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ICalloutIf} object and add it to the collection of Callout objects.
     *
     * @return The created object.
     */
    public ICalloutIf createCallout();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ICalloutIf} object and add it to the collection of Callout objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public ICalloutIf createCallout(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ICheckpointIf} object and add it to the collection of Checkpoint objects.
     *
     * @return The created object.
     */
    public ICheckpointIf createCheckpoint();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ICheckpointIf} object and add it to the collection of Checkpoint objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public ICheckpointIf createCheckpoint(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IDataSourceIf} object and add it to the collection of DataSource objects.
     *
     * @return The created object.
     */
    public IDataSourceIf createDataSource();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IDataSourceIf} object and add it to the collection of DataSource objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IDataSourceIf createDataSource(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IEnvironmentIf} object and add it to the collection of Environment objects.
     *
     * @param aCalendar The time from which the information is valid.
     * @return The created object.
     */
    public IEnvironmentIf createEnvironment(Calendar aCalendar, String aText);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IEnvironmentIf} object and add it to the collection of Environment objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IEnvironmentIf createEnvironment(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IEquipmentIf} object and add it to the collection of Equipment objects.
     *
     * @return The created object.
     */
    public IEquipmentIf createEquipment();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IEquipmentIf} object and add it to the collection of Equipment objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IEquipmentIf createEquipment(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IForecastIf} object and add it to the collection of Forecast objects.
     *
     * @param aCalendar The time from which the information is valid.
     * @param aText A forecast text
     * @return The created object.
     */
    public IForecastIf createForecast(Calendar aCalendar, String aText);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IForecastIf} object and add it to the collection of Forecast objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IForecastIf createForecast(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IHypothesisIf} object and add it to the collection of Hypothesis objects.
     *
     * @return The created object.
     */
    public IHypothesisIf createHypothesis();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IHypothesisIf} object and add it to the collection of Hypothesis objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IHypothesisIf createHypothesis(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IIntelligenceIf} object and add it to the collection of Intelligence objects.
     *
     * @return The created object.
     */
    public IIntelligenceIf createIntelligence();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IIntelligenceIf} object and add it to the collection of Intelligence objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IIntelligenceIf createIntelligence(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IMessageIf} object and add it to the collection of Message objects.
     *
     * @return The created object.
     */
    public IMessageIf createMessage();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IMessageIf} object and add it to the collection of Message objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IMessageIf createMessage(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IMessageLineIf} object and add it to the collection of Message objects.
     *
     * @return The created object.
     */
    public IMessageLineIf createMessageLine();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IMessageLineIf} object and add it to the collection of Message objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
       public IMessageLineIf createMessageLine(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IOperationAreaIf} object and add it to the collection of OperationArea objects.
     *
     * @return The created object.
     */
    public IOperationAreaIf createOperationArea();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IOperationAreaIf} object and add it to the collection of OperationArea objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IOperationAreaIf createOperationArea(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IPersonnelIf} object and add it to the collection of Personnel objects.
     *
     * @return The created object.
     */
    public IPersonnelIf createPersonnel();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IPersonnelIf} object and add it to the collection of Personnel objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IPersonnelIf createPersonnel(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IPOIIf} object and add it to the collection of POI objects.
     *
     * @return The created object.
     */
    public IPOIIf createPOI();

    /**
     * Create a new {@link org.redcross.sar.mso.data.IPOIIf} object and add it to the collection of POI objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IPOIIf createPOI(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IPOIIf} object and add it to the collection of POI objects.
     *
     * @param aType The POI type
     * @param aPosition The POI position
     * @return The created object.
     */
    public IPOIIf createPOI(IPOIIf.POIType aType, Position aPosition);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IPOIIf} object and add it to the collection of POI objects.
     *
     * @param anObjectId The Object id
     * @param aType The POI type
     * @param aPosition The POI position
     * @return The created object.
     */
    public IPOIIf createPOI(IMsoObjectIf.IObjectIdIf anObjectId, IPOIIf.POIType aType, Position aPosition);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IRouteIf} object and add it to the collection of Route objects.
     *
     * @param aRoute Referred Route object
     * @return The created object.
     */
    public IRouteIf createRoute(Route aRoute);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IRouteIf} object and add it to the collection of Route objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IRouteIf createRoute(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IRouteIf} object and add it to the collection of Route objects.
     *
     * @param anObjectId The Object id
     * @param aRoute Referred Route object
     * @return The created object.
     */
    public IRouteIf createRoute(IMsoObjectIf.IObjectIdIf anObjectId, Route aRoute);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISearchAreaIf} object and add it to the collection of SearchArea objects.
     *
     * @return The created object.
     */
    public ISearchAreaIf createSearchArea();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISearchAreaIf} object and add it to the collection of SearchArea objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public ISearchAreaIf createSearchArea(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISketchIf} object and add it to the collection of Sketch objects.
     *
     * @return The created object.
     */
    public ISketchIf createSketch();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISketchIf} object and add it to the collection of Sketch objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public ISketchIf createSketch(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISubjectIf} object and add it to the collection of Subject objects.
     *
     * @return The created object.
     */
    public ISubjectIf createSubject();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ISubjectIf} object and add it to the collection of Subject objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public ISubjectIf createSubject(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ITaskIf} object and add it to the collection of Task objects.
     *
     * @param aCalendar Task due time
     * @return The created object.
     */
    public ITaskIf createTask(Calendar aCalendar);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ITaskIf} object and add it to the collection of Task objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public ITaskIf createTask(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ITrackIf} object and add it to the collection of Track objects.
     *
     * @return The created object.
     */
    public ITrackIf createTrack();

    /**
     * Create a new {@link org.redcross.sar.mso.data.ITrackIf} object and add it to the collection of Track objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public ITrackIf createTrack(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IVehicleIf} object and add it to the collection of Unit objects.
     *
     * @param anIdentifier The unit's identifier (if needed here??)
     * @return The created object.
     */
    public IVehicleIf createVehicle(String anIdentifier);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IVehicleIf} object and add it to the collection of Unit objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IVehicleIf createVehicle(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IBoatIf} object and add it to the collection of Unit objects.
     *
     * @param anIdentifier The unit's identifier (if needed here??)
     * @return The created object.
     */
    public IBoatIf createBoat(String anIdentifier);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IBoatIf} object and add it to the collection of Unit objects.
     *
     * @param anObjectId The Object id
     * @return The created object.
     */
    public IBoatIf createBoat(IMsoObjectIf.IObjectIdIf anObjectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IDogIf} object and add it to the collection of Unit objects.
     * @param objectId The Object id
     * @return The Created object.
     */
    public IDogIf createDog(IMsoObjectIf.IObjectIdIf objectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IDogIf} object and add it to the collection of Unit objects.
     *
     * @param anIdentifier
     * @return The created object
     */
    public IDogIf createDog(String anIdentifier);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IAircraftIf} object and add it to the collection of Unit objects.
     * @param objectId The Object id
     * @return The Created object.
     */
    public IAircraftIf createAircraft(IMsoObjectIf.IObjectIdIf objectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.IAircraftIf} object and add it to the collection of Unit objects.
     * @param anIdentifier
     * @return The created aircraft
     */
    public IAircraftIf createAircraft(String anIdentifier);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ITeamIf} object and add it to the collection of Unit objects.
     * @param objectId The Object id
     * @return The Created object.
     */
    public ITeamIf createTeam(IMsoObjectIf.IObjectIdIf objectId);

    /**
     * Create a new {@link org.redcross.sar.mso.data.ITeamIf} object and add it to the collection of Unit objects.
     * @param anIdentifier
     * @return The created team
     */
    public ITeamIf createTeam(String anIdentifier);

//    /**
//     * Create a new {@link org.redcross.sar.mso.data.ICmdPostIf} object and add it to the collection of Unit objects.
//     * @param objectId The Object id
//     * @return The Created object.
//     */
//    public ICmdPostIf createCommandPost(IMsoObjectIf.IObjectIdIf objectId);
}
